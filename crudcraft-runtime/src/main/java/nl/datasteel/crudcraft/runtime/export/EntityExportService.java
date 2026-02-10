/*
 * Copyright (c) 2025 CrudCraft contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.datasteel.crudcraft.runtime.export;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for fetching entities with optimized relationship loading for export.
 * Uses JPA Criteria API to build dynamic queries with JOIN FETCH for efficient loading.
 */
public class EntityExportService {
    
    private static final Logger log = LoggerFactory.getLogger(EntityExportService.class);
    
    private final EntityManager entityManager;
    private final EntityMetadataRegistry metadataRegistry;
    
    /**
     * Creates a new entity export service.
     *
     * @param entityManager the entity manager
     * @param metadataRegistry the metadata registry
     */
    public EntityExportService(EntityManager entityManager, EntityMetadataRegistry metadataRegistry) {
        this.entityManager = entityManager;
        this.metadataRegistry = metadataRegistry;
    }
    
    /**
     * Fetches entities with optimized relationship loading.
     * Uses JOIN FETCH for ManyToOne and OneToOne relationships to avoid N+1 queries.
     *
     * @param entityClass the entity class
     * @param exportRequest the export request
     * @param pageRequest the page request
     * @param <T> the entity type
     * @return page of entities
     */
    @Transactional(readOnly = true)
    public <T> Page<T> fetchWithRelationships(Class<T> entityClass, 
                                               ExportRequest exportRequest, 
                                               PageRequest pageRequest) {
        EntityMetadata metadata = metadataRegistry.getMetadata(entityClass);
        
        // Build criteria query with JOINs for ManyToOne and OneToOne relationships
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        // Add JOIN FETCH for non-collection relationships
        addJoinFetches(root, metadata, exportRequest);
        
        query.select(root).distinct(true);
        
        // Execute query with pagination
        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageRequest.getOffset());
        typedQuery.setMaxResults(pageRequest.getPageSize());
        
        List<T> results = typedQuery.getResultList();
        
        // Batch load collections for all entities
        if (!results.isEmpty()) {
            batchLoadCollections(results, metadata, exportRequest);
        }
        
        // Get total count
        long total = countTotal(entityClass);
        
        return new PageImpl<>(results, pageRequest, total);
    }
    
    /**
     * Adds JOIN FETCH clauses for ManyToOne and OneToOne relationships.
     *
     * @param root the query root
     * @param metadata the entity metadata
     * @param exportRequest the export request
     */
    private void addJoinFetches(Root<?> root, EntityMetadata metadata, ExportRequest exportRequest) {
        for (EntityFieldMetadata field : metadata.getExportableFields()) {
            // Only fetch if field should be included
            if (!exportRequest.shouldIncludeField(field.getName())) {
                continue;
            }
            
            // Only add JOIN FETCH for non-collection relationships
            if (field.getType() == EntityFieldMetadata.FieldType.MANY_TO_ONE ||
                field.getType() == EntityFieldMetadata.FieldType.ONE_TO_ONE) {
                try {
                    root.fetch(field.getName(), JoinType.LEFT);
                } catch (IllegalArgumentException e) {
                    // Field might not be fetchable (e.g., @Transient or mapped incorrectly)
                    // Log at DEBUG level and continue without fetching
                    log.debug("Could not add JOIN FETCH for field '{}' on entity '{}': {}", 
                        field.getName(), metadata.getEntityClass().getName(), e.getMessage());
                }
            }
        }
    }
    
    /**
     * Batch loads collections for all entities to avoid N+1 queries.
     *
     * @param entities the entities to load collections for
     * @param metadata the entity metadata
     * @param exportRequest the export request
     * @param <T> the entity type
     */
    private <T> void batchLoadCollections(List<T> entities, EntityMetadata metadata, 
                                           ExportRequest exportRequest) {
        for (EntityFieldMetadata field : metadata.getExportableFields()) {
            // Check if this is a collection field that should be included
            if (!field.isCollection() || !exportRequest.shouldIncludeField(field.getName())) {
                continue;
            }
            
            // Build a query to fetch all collection elements for these entities
            try {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                @SuppressWarnings("unchecked")
                CriteriaQuery<T> query = (CriteriaQuery<T>) cb.createQuery(metadata.getEntityClass());
                Root<T> root = query.from((Class<T>) metadata.getEntityClass());
                
                // Fetch the collection
                Fetch<T, ?> fetch = root.fetch(field.getName(), JoinType.LEFT);
                
                // Filter to only our entities
                query.where(root.in((Collection<T>) entities));
                query.select(root);
                
                entityManager.createQuery(query).getResultList();
            } catch (Exception e) {
                // If batch loading fails, collections will be lazy-loaded individually
                // This is less efficient but ensures the export still works
                // Log at WARN level since this defeats the N+1 optimization
                log.warn("Batch loading failed for collection field '{}' on entity '{}'. " +
                    "Falling back to lazy loading (may cause N+1 queries): {}", 
                    field.getName(), metadata.getEntityClass().getName(), e.getMessage());
                log.debug("Batch loading exception details", e);
            }
        }
    }
    
    /**
     * Counts the total number of entities.
     *
     * @param entityClass the entity class
     * @param <T> the entity type
     * @return the total count
     */
    private <T> long countTotal(Class<T> entityClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(cb.count(root));
        return entityManager.createQuery(query).getSingleResult();
    }
}

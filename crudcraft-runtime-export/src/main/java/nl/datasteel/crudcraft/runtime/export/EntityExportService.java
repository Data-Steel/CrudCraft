/*
 * Copyright (c) 2026 CrudCraft contributors
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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import nl.datasteel.crudcraft.runtime.export.config.ExportProperties;
import nl.datasteel.crudcraft.runtime.metadata.EntityFieldMetadata;
import nl.datasteel.crudcraft.runtime.metadata.EntityMetadata;
import nl.datasteel.crudcraft.runtime.metadata.EntityMetadataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service for fetching entities with optimized relationship loading for export. Uses JPA Criteria
 * API to build dynamic queries with JOIN FETCH for efficient loading.
 */
public class EntityExportService {

    private static final Logger log = LoggerFactory.getLogger(EntityExportService.class);
    private static final String STRICT_FETCH_PROPERTY = "crudcraft.export.must-fetch";

    private final EntityManager entityManager;
    private final EntityMetadataRegistry metadataRegistry;
    private final ExportProperties exportProperties;

    /**
     * Creates a new entity export service.
     *
     * @param entityManager the entity manager
     * @param metadataRegistry the metadata registry
     */
    public EntityExportService(
            @NonNull EntityManager entityManager,
            @NonNull EntityMetadataRegistry metadataRegistry) {
        this(entityManager, metadataRegistry, new ExportProperties());
    }

    /**
     * Creates a new entity export service.
     *
     * @param entityManager the entity manager
     * @param metadataRegistry the metadata registry
     * @param exportProperties export configuration properties
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects managed JPA and registry collaborators.")
    public EntityExportService(
            @NonNull EntityManager entityManager,
            @NonNull EntityMetadataRegistry metadataRegistry,
            @NonNull ExportProperties exportProperties) {
        this.entityManager = entityManager;
        this.metadataRegistry = metadataRegistry;
        this.exportProperties = Objects.requireNonNull(exportProperties, "exportProperties");
    }

    /**
     * Fetches entities with optimized relationship loading. Uses JOIN FETCH for ManyToOne and
     * OneToOne relationships to avoid N+1 queries.
     *
     * @param entityClass the entity class
     * @param exportRequest the export request
     * @param pageRequest the page request
     * @param specification optional specification applied to the export query
     * @param <T> the entity type
     * @return page of entities
     */
    @Transactional(readOnly = true)
    public <T> @NonNull Page<T> fetchWithRelationships(
            @NonNull Class<T> entityClass,
            @NonNull ExportRequest exportRequest,
            @NonNull PageRequest pageRequest,
            Specification<T> specification) {
        EntityMetadata metadata = metadataRegistry.getMetadata(entityClass);
        validateRequest(metadata, exportRequest);

        // Build criteria query with JOINs for ManyToOne and OneToOne relationships
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        // Add JOIN FETCH for non-collection relationships
        addJoinFetches(root, metadata, exportRequest);

        query.select(root).distinct(true);
        if (specification != null) {
            var predicate = specification.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }

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
        long total = countTotal(entityClass, specification);

        return new PageImpl<>(results, pageRequest, total);
    }

    /**
     * Backward-compatible overload without an explicit specification.
     *
     * @param entityClass entity class
     * @param exportRequest export request
     * @param pageRequest page request
     * @param <T> entity type
     * @return entity page
     */
    @Transactional(readOnly = true)
    public <T> @NonNull Page<T> fetchWithRelationships(
            @NonNull Class<T> entityClass,
            @NonNull ExportRequest exportRequest,
            @NonNull PageRequest pageRequest) {
        return fetchWithRelationships(entityClass, exportRequest, pageRequest, null);
    }

    /**
     * Adds JOIN FETCH clauses for ManyToOne and OneToOne relationships.
     *
     * @param root the query root
     * @param metadata the entity metadata
     * @param exportRequest the export request
     */
    private void addJoinFetches(
            Root<?> root, EntityMetadata metadata, ExportRequest exportRequest) {
        for (EntityFieldMetadata field : metadata.getExportableFields()) {
            // Only fetch if field itself or any of its descendants should be included
            if (!exportRequest.shouldIncludeField(field.getName())
                    && !exportRequest.hasIncludedDescendants(field.getName())) {
                continue;
            }

            // Only add JOIN FETCH for non-collection relationships
            if (field.getType() == EntityFieldMetadata.FieldType.MANY_TO_ONE
                    || field.getType() == EntityFieldMetadata.FieldType.ONE_TO_ONE) {
                if (!relationshipDepthAllowed(metadata, exportRequest, field)) {
                    continue;
                }
                try {
                    root.fetch(field.getName(), JoinType.LEFT);
                } catch (IllegalArgumentException e) {
                    Map<String, String> context = fetchContext(metadata, field);
                    if (mustFetchRelationships()) {
                        throw new ExportFetchException(
                                "Required export JOIN FETCH could not be added.",
                                context,
                                e);
                    }
                    log.warn(
                            "Could not add JOIN FETCH for export relationship; continuing with"
                                    + " lazy loading. context={} reason={}",
                            context,
                            e.getMessage());
                    log.debug("JOIN FETCH failure details", e);
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
    private <T> void batchLoadCollections(
            List<T> entities, EntityMetadata metadata, ExportRequest exportRequest) {
        for (EntityFieldMetadata field : metadata.getExportableFields()) {
            // Check if this is a collection field that should be included or has included
            // descendants
            if (!field.isCollection()
                    || (!exportRequest.shouldIncludeField(field.getName())
                            && !exportRequest.hasIncludedDescendants(field.getName()))) {
                continue;
            }
            if (!relationshipDepthAllowed(metadata, exportRequest, field)) {
                continue;
            }

            // Build a query to fetch all collection elements for these entities
            try {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                Class<T> entityClass = entityClass(metadata);
                CriteriaQuery<T> query = cb.createQuery(entityClass);
                Root<T> root = query.from(entityClass);

                // Fetch the collection
                root.fetch(field.getName(), JoinType.LEFT);

                // Filter to only our entities
                query.where(root.in(entities));
                query.select(root);

                entityManager.createQuery(query).getResultList();
            } catch (IllegalArgumentException | PersistenceException e) {
                // If batch loading fails, collections will be lazy-loaded individually
                // This is less efficient but ensures the export still works
                // Log at WARN level since this defeats the N+1 optimization
                log.warn(
                        "Batch loading failed for export collection; falling back to lazy loading"
                                + " may cause N+1 queries. context={} reason={}",
                        fetchContext(metadata, field),
                        e.getMessage());
                log.debug("Batch loading exception details", e);
            }
        }
    }

    private void validateRequest(EntityMetadata metadata, ExportRequest exportRequest) {
        int requestedDepth = requestedDepth(metadata, exportRequest);
        int maxDepth = effectiveMaxDepth(exportRequest);
        if (requestedDepth > maxDepth) {
            throw new ExportDepthExceededException(
                    "Requested export field depth exceeds the configured maximum.",
                    Map.of(
                            "entity",
                            metadata.getEntityClass().getName(),
                            "requested_depth",
                            String.valueOf(requestedDepth),
                            "max_depth",
                            String.valueOf(maxDepth),
                            "docs",
                            "docs/feature-guides/export/README.md"));
        }

        for (String fieldPath : requestedFieldPaths(exportRequest)) {
            String rootField = fieldPath.split("\\.", 2)[0];
            EntityFieldMetadata field = metadata.getField(rootField).orElse(null);
            if (field == null || !field.isExportable()) {
                throw new ExportSecurityException(
                        "Requested field is not exportable.",
                        Map.of(
                                "entity",
                                metadata.getEntityClass().getName(),
                                "field",
                                fieldPath,
                                "allowed",
                                metadata.getExportableFields().stream()
                                        .map(EntityFieldMetadata::getName)
                                        .toList()
                                        .toString(),
                                "docs",
                                "docs/feature-guides/export/README.md"));
            }
        }
    }

    private Set<String> requestedFieldPaths(ExportRequest exportRequest) {
        Set<String> paths = new LinkedHashSet<>();
        paths.addAll(exportRequest.getIncludeFields());
        paths.addAll(exportRequest.getExcludeFields());
        return paths;
    }

    private int requestedDepth(EntityMetadata metadata, ExportRequest exportRequest) {
        return requestedFieldPaths(exportRequest).stream()
                .mapToInt(path -> requestedDepth(metadata, path))
                .max()
                .orElse(0);
    }

    private int requestedDepth(EntityMetadata metadata, String path) {
        if (path.isBlank()) {
            return 0;
        }
        String[] parts = path.split("\\.");
        EntityFieldMetadata rootField = metadata.getField(parts[0]).orElse(null);
        if (rootField == null || !isNestedField(rootField)) {
            return Math.max(0, parts.length - 1);
        }
        return Math.max(1, parts.length - 1);
    }

    private boolean relationshipDepthAllowed(
            EntityMetadata metadata, ExportRequest exportRequest, EntityFieldMetadata field) {
        int maxDepth = effectiveMaxDepth(exportRequest);
        if (maxDepth >= 1) {
            return true;
        }
        boolean explicitlyRequested =
                exportRequest.getIncludeFields().contains(field.getName())
                        || exportRequest.hasIncludedDescendants(field.getName());
        if (explicitlyRequested) {
            throw new ExportDepthExceededException(
                    "Requested export relationship exceeds the configured maximum depth.",
                    Map.of(
                            "entity",
                            metadata.getEntityClass().getName(),
                            "field",
                            field.getName(),
                            "requested_depth",
                            "1",
                            "max_depth",
                            String.valueOf(maxDepth),
                            "docs",
                            "docs/feature-guides/export/README.md"));
        }
        return false;
    }

    private int effectiveMaxDepth(ExportRequest exportRequest) {
        return exportRequest.getEffectiveMaxDepth(exportProperties.getMaxDepth());
    }

    private static boolean isNestedField(EntityFieldMetadata field) {
        return field.isRelationship()
                || field.isCollection()
                || field.getType() == EntityFieldMetadata.FieldType.EMBEDDED;
    }

    private Map<String, String> fetchContext(EntityMetadata metadata, EntityFieldMetadata field) {
        return Map.of(
                "entity",
                metadata.getEntityClass().getName(),
                "field",
                field.getName(),
                "field_type",
                field.getType().name(),
                "strict_property",
                STRICT_FETCH_PROPERTY);
    }

    private boolean mustFetchRelationships() {
        return exportProperties.isMustFetch() || Boolean.getBoolean(STRICT_FETCH_PROPERTY);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> entityClass(EntityMetadata metadata) {
        return (Class<T>) metadata.getEntityClass();
    }

    /**
     * Counts the total number of entities.
     *
     * @param entityClass the entity class
     * @param <T> the entity type
     * @return the total count
     */
    private <T> long countTotal(Class<T> entityClass, Specification<T> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(cb.count(root));
        if (specification != null) {
            var predicate = specification.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        return entityManager.createQuery(query).getSingleResult();
    }
}

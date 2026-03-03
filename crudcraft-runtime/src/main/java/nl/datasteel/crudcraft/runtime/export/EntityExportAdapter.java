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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Adapter that converts entity-based exports to map-based exports for use with ExportUtil.
 * This class bridges entity mode with the existing DTO-based export infrastructure.
 */
public class EntityExportAdapter {
    
    private final EntityExportService entityExportService;
    private final EntitySerializer entitySerializer;
    
    /**
     * Creates a new entity export adapter.
     *
     * @param entityExportService the service for fetching entities
     * @param entitySerializer the serializer for converting entities to maps
     */
    public EntityExportAdapter(EntityExportService entityExportService, EntitySerializer entitySerializer) {
        this.entityExportService = entityExportService;
        this.entitySerializer = entitySerializer;
    }
    
    /**
     * Creates an iterator that fetches entities and converts them to maps for export.
     *
     * @param entityClass the entity class
     * @param exportRequest the export request
     * @param limit the maximum number of entities to export
     * @param pageSize the page size for fetching
     * @param <T> the entity type
     * @return an iterator of maps representing entities
     */
    public <T> Iterator<Map<String, Object>> createIterator(Class<T> entityClass, 
                                                              ExportRequest exportRequest,
                                                              int limit,
                                                              int pageSize) {
        return new EntityMapIterator<>(entityClass, exportRequest, limit, pageSize);
    }
    
    /**
     * Iterator that lazily fetches entities in pages and converts them to maps.
     *
     * @param <T> the entity type
     */
    private class EntityMapIterator<T> implements Iterator<Map<String, Object>> {
        private final Class<T> entityClass;
        private final ExportRequest exportRequest;
        private final int maxItems;
        private final int pageSize;
        
        private int page = 0;
        private int index = 0;
        private List<Map<String, Object>> current = List.of();
        private int fetched = 0;
        
        EntityMapIterator(Class<T> entityClass, ExportRequest exportRequest, int maxItems, int pageSize) {
            this.entityClass = entityClass;
            this.exportRequest = exportRequest;
            this.maxItems = maxItems;
            this.pageSize = pageSize;
        }
        
        private void fetch() {
            if (fetched >= maxItems) {
                current = List.of();
                return;
            }
            
            // Fetch entities with optimized relationship loading
            Page<T> entityPage = entityExportService.fetchWithRelationships(
                entityClass, exportRequest, PageRequest.of(page++, pageSize));
            
            // Convert entities to maps
            List<Map<String, Object>> maps = entityPage.getContent().stream()
                .map(entity -> entitySerializer.serialize(entity, exportRequest))
                .toList();
            
            if (maps.isEmpty()) {
                current = List.of();
                fetched = maxItems;
                return;
            }
            
            if (fetched + maps.size() > maxItems) {
                maps = maps.subList(0, maxItems - fetched);
                fetched = maxItems;
            } else {
                fetched += maps.size();
            }
            
            current = maps;
            index = 0;
        }
        
        @Override
        public boolean hasNext() {
            if (index >= current.size()) {
                fetch();
            }
            return index < current.size();
        }
        
        @Override
        public Map<String, Object> next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            return current.get(index++);
        }
    }
}

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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializes entity objects to Map structures for export, applying field filtering
 * and relationship traversal based on ExportRequest configuration.
 */
public class EntitySerializer {
    
    private static final Logger log = LoggerFactory.getLogger(EntitySerializer.class);
    
    private final EntityMetadataRegistry metadataRegistry;
    
    /**
     * Creates a new entity serializer.
     *
     * @param metadataRegistry the metadata registry
     */
    public EntitySerializer(EntityMetadataRegistry metadataRegistry) {
        this.metadataRegistry = metadataRegistry;
    }
    
    /**
     * Serializes an entity to a map, applying field filtering.
     *
     * @param entity the entity to serialize
     * @param exportRequest the export request with field filters
     * @return map representation of the entity
     */
    public Map<String, Object> serialize(Object entity, ExportRequest exportRequest) {
        return serializeInternal(entity, exportRequest, "", 0);
    }
    
    /**
     * Internal serialization method with depth tracking.
     *
     * @param entity the entity to serialize
     * @param exportRequest the export request
     * @param pathPrefix the current path prefix for nested fields
     * @param depth the current nesting depth
     * @return map representation
     */
    private Map<String, Object> serializeInternal(Object entity, ExportRequest exportRequest, 
                                                   String pathPrefix, int depth) {
        if (entity == null) {
            return null;
        }
        
        // Check depth limit: allow serialization at maxDepth, stop only when depth exceeds it
        if (depth > exportRequest.getEffectiveMaxDepth()) {
            return null;
        }
        
        EntityMetadata metadata = metadataRegistry.getMetadata(entity.getClass());
        Map<String, Object> result = new LinkedHashMap<>();
        
        for (EntityFieldMetadata fieldMetadata : metadata.getExportableFields()) {
            String fieldName = fieldMetadata.getName();
            String fieldPath = pathPrefix.isEmpty() ? fieldName : pathPrefix + "." + fieldName;
            
            // Determine if this is a nested field (collection / relationship / embedded)
            boolean isNestedField = fieldMetadata.isCollection()
                    || fieldMetadata.isRelationship()
                    || fieldMetadata.getType() == EntityFieldMetadata.FieldType.EMBEDDED;

            boolean includeField = exportRequest.shouldIncludeField(fieldPath);
            boolean hasIncludedDescendants = isNestedField && exportRequest.hasIncludedDescendants(fieldPath);

            // Skip fields that are neither directly included nor have included descendants
            if (!includeField && !hasIncludedDescendants) {
                continue;
            }
            
            Object value = getFieldValue(entity, fieldMetadata.getField());
            
            if (value == null) {
                if (includeField) {
                    result.put(fieldName, null);
                }
                continue;
            } else if (fieldMetadata.isCollection()) {
                // Handle collections
                List<Object> serializedList = new ArrayList<>();
                Collection<?> collection = (Collection<?>) value;
                for (Object item : collection) {
                    if (isEntity(item)) {
                        Map<String, Object> serializedItem = serializeInternal(
                            item, exportRequest, fieldPath, depth + 1);
                        if (serializedItem != null) {
                            serializedList.add(serializedItem);
                        }
                    } else {
                        serializedList.add(item);
                    }
                }
                result.put(fieldName, serializedList);
            } else if (fieldMetadata.isRelationship() || 
                      fieldMetadata.getType() == EntityFieldMetadata.FieldType.EMBEDDED) {
                // Handle nested objects
                Map<String, Object> nested = serializeInternal(
                    value, exportRequest, fieldPath, depth + 1);
                if (nested != null) {
                    result.put(fieldName, nested);
                }
            } else {
                // Scalar value
                result.put(fieldName, value);
            }
        }
        
        return result;
    }
    
    /**
     * Gets the value of a field from an entity using reflection.
     *
     * @param entity the entity
     * @param field the field
     * @return the field value
     */
    private Object getFieldValue(Object entity, Field field) {
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            log.error("Failed to access field '{}' on entity class '{}': {}", 
                field.getName(), entity.getClass().getName(), e.getMessage());
            throw new RuntimeException(String.format(
                "Failed to access field '%s' on entity '%s'", 
                field.getName(), entity.getClass().getName()), e);
        }
    }
    
    /**
     * Checks if an object is an entity (has metadata registered).
     *
     * @param obj the object to check
     * @return true if it's an entity
     */
    private boolean isEntity(Object obj) {
        if (obj == null) {
            return false;
        }
        try {
            metadataRegistry.getMetadata(obj.getClass());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}

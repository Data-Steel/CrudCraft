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

import java.util.List;
import java.util.Optional;

/**
 * Metadata about an entity class, used for dynamic export functionality.
 * Contains information about all fields including relationships.
 */
public class EntityMetadata {
    
    private final Class<?> entityClass;
    private final List<EntityFieldMetadata> fields;
    
    /**
     * Creates entity metadata.
     *
     * @param entityClass the entity class
     * @param fields list of field metadata
     */
    public EntityMetadata(Class<?> entityClass, List<EntityFieldMetadata> fields) {
        this.entityClass = entityClass;
        this.fields = List.copyOf(fields);
    }
    
    public Class<?> getEntityClass() {
        return entityClass;
    }
    
    public List<EntityFieldMetadata> getFields() {
        return fields;
    }
    
    /**
     * Finds a field by name.
     *
     * @param name the field name
     * @return the field metadata, or empty if not found
     */
    public Optional<EntityFieldMetadata> getField(String name) {
        return fields.stream()
                .filter(f -> f.getName().equals(name))
                .findFirst();
    }
    
    /**
     * Gets all exportable fields.
     *
     * @return list of exportable fields
     */
    public List<EntityFieldMetadata> getExportableFields() {
        return fields.stream()
                .filter(EntityFieldMetadata::isExportable)
                .toList();
    }
}

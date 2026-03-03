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

/**
 * Metadata about a field in an entity, used for dynamic export functionality.
 */
public class EntityFieldMetadata {
    
    /**
     * Type of the field.
     */
    public enum FieldType {
        /** Scalar/primitive field (String, Integer, etc.) */
        SCALAR,
        /** Embedded object */
        EMBEDDED,
        /** ManyToOne relationship */
        MANY_TO_ONE,
        /** OneToOne relationship */
        ONE_TO_ONE,
        /** OneToMany relationship (collection) */
        ONE_TO_MANY,
        /** ManyToMany relationship (collection) */
        MANY_TO_MANY
    }
    
    private final String name;
    private final Field field;
    private final FieldType type;
    private final Class<?> targetType;
    private final boolean exportable;
    
    /**
     * Creates field metadata.
     *
     * @param name the field name
     * @param field the Java field
     * @param type the field type
     * @param targetType the target class (for relationships)
     * @param exportable whether the field can be exported
     */
    public EntityFieldMetadata(String name, Field field, FieldType type, 
                               Class<?> targetType, boolean exportable) {
        this.name = name;
        this.field = field;
        this.type = type;
        this.targetType = targetType;
        this.exportable = exportable;
    }
    
    public String getName() {
        return name;
    }
    
    public Field getField() {
        return field;
    }
    
    public FieldType getType() {
        return type;
    }
    
    public Class<?> getTargetType() {
        return targetType;
    }
    
    public boolean isExportable() {
        return exportable;
    }
    
    public boolean isCollection() {
        return type == FieldType.ONE_TO_MANY || type == FieldType.MANY_TO_MANY;
    }
    
    public boolean isRelationship() {
        return type != FieldType.SCALAR && type != FieldType.EMBEDDED;
    }
}

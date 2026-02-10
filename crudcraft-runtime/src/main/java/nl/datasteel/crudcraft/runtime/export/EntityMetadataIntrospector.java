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

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import nl.datasteel.crudcraft.annotations.export.ExportExclude;

/**
 * Introspects entity classes to extract metadata for dynamic export functionality.
 * Uses Java reflection and JPA annotations to determine field types and relationships.
 */
public class EntityMetadataIntrospector {
    
    /**
     * Introspects an entity class and extracts metadata.
     *
     * @param entityClass the entity class to introspect
     * @return entity metadata
     * @throws IllegalArgumentException if the class is not an entity
     */
    public EntityMetadata introspect(Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Class " + entityClass.getName() + " is not an @Entity");
        }
        
        List<EntityFieldMetadata> fields = new ArrayList<>();
        collectFields(entityClass, fields);
        
        return new EntityMetadata(entityClass, fields);
    }
    
    /**
     * Collects all fields from the class and its superclasses.
     *
     * @param clazz the class to inspect
     * @param fields the list to populate with field metadata
     */
    private void collectFields(Class<?> clazz, List<EntityFieldMetadata> fields) {
        if (clazz == null || clazz == Object.class) {
            return;
        }
        
        // Recursively collect from superclass first
        collectFields(clazz.getSuperclass(), fields);
        
        // Process declared fields
        for (Field field : clazz.getDeclaredFields()) {
            if (shouldIncludeField(field)) {
                EntityFieldMetadata metadata = createFieldMetadata(field);
                if (metadata != null) {
                    fields.add(metadata);
                }
            }
        }
    }
    
    /**
     * Determines if a field should be included in metadata.
     *
     * @param field the field to check
     * @return true if the field should be included
     */
    private boolean shouldIncludeField(Field field) {
        int modifiers = field.getModifiers();
        
        // Exclude static and transient fields
        if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
            return false;
        }
        
        // Exclude JPA transient fields
        if (field.isAnnotationPresent(Transient.class)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Creates field metadata for a given field.
     *
     * @param field the field to process
     * @return field metadata, or null if the field cannot be processed
     */
    private EntityFieldMetadata createFieldMetadata(Field field) {
        String name = field.getName();
        boolean exportable = !field.isAnnotationPresent(ExportExclude.class);
        
        // Determine field type
        EntityFieldMetadata.FieldType fieldType;
        Class<?> targetType;
        
        if (field.isAnnotationPresent(Embedded.class) || 
            field.getType().isAnnotationPresent(Embeddable.class)) {
            fieldType = EntityFieldMetadata.FieldType.EMBEDDED;
            targetType = field.getType();
        } else if (field.isAnnotationPresent(ManyToOne.class)) {
            fieldType = EntityFieldMetadata.FieldType.MANY_TO_ONE;
            targetType = field.getType();
        } else if (field.isAnnotationPresent(OneToOne.class)) {
            fieldType = EntityFieldMetadata.FieldType.ONE_TO_ONE;
            targetType = field.getType();
        } else if (field.isAnnotationPresent(OneToMany.class)) {
            fieldType = EntityFieldMetadata.FieldType.ONE_TO_MANY;
            targetType = getCollectionElementType(field);
        } else if (field.isAnnotationPresent(ManyToMany.class)) {
            fieldType = EntityFieldMetadata.FieldType.MANY_TO_MANY;
            targetType = getCollectionElementType(field);
        } else {
            fieldType = EntityFieldMetadata.FieldType.SCALAR;
            targetType = field.getType();
        }
        
        return new EntityFieldMetadata(name, field, fieldType, targetType, exportable);
    }
    
    /**
     * Extracts the element type from a collection field.
     *
     * @param field the collection field
     * @return the element type, or Object.class if it cannot be determined
     */
    private Class<?> getCollectionElementType(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType paramType) {
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                return (Class<?>) typeArgs[0];
            }
        }
        return Object.class;
    }
}

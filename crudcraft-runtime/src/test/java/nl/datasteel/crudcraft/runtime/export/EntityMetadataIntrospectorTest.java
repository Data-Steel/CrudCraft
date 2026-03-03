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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.export.ExportExclude;
import org.junit.jupiter.api.Test;

class EntityMetadataIntrospectorTest {
    
    @Entity
    static class TestEntity {
        @Id
        private Long id;
        
        private String name;
        
        @ExportExclude
        private String secretField;
        
        @ManyToOne
        private TestEntity parent;
        
        @OneToMany(mappedBy = "parent")
        private Set<TestEntity> children = new HashSet<>();
    }
    
    @Test
    void shouldIntrospectEntityMetadata() {
        EntityMetadataIntrospector introspector = new EntityMetadataIntrospector();
        EntityMetadata metadata = introspector.introspect(TestEntity.class);
        
        assertNotNull(metadata);
        assertEquals(TestEntity.class, metadata.getEntityClass());
        assertFalse(metadata.getFields().isEmpty());
    }
    
    @Test
    void shouldDetectScalarFields() {
        EntityMetadataIntrospector introspector = new EntityMetadataIntrospector();
        EntityMetadata metadata = introspector.introspect(TestEntity.class);
        
        EntityFieldMetadata nameField = metadata.getField("name").orElseThrow();
        assertEquals(EntityFieldMetadata.FieldType.SCALAR, nameField.getType());
        assertTrue(nameField.isExportable());
    }
    
    @Test
    void shouldDetectExportExcludeAnnotation() {
        EntityMetadataIntrospector introspector = new EntityMetadataIntrospector();
        EntityMetadata metadata = introspector.introspect(TestEntity.class);
        
        EntityFieldMetadata secretField = metadata.getField("secretField").orElseThrow();
        assertFalse(secretField.isExportable());
    }
    
    @Test
    void shouldDetectManyToOneRelationship() {
        EntityMetadataIntrospector introspector = new EntityMetadataIntrospector();
        EntityMetadata metadata = introspector.introspect(TestEntity.class);
        
        EntityFieldMetadata parentField = metadata.getField("parent").orElseThrow();
        assertEquals(EntityFieldMetadata.FieldType.MANY_TO_ONE, parentField.getType());
        assertTrue(parentField.isRelationship());
        assertFalse(parentField.isCollection());
    }
    
    @Test
    void shouldDetectOneToManyRelationship() {
        EntityMetadataIntrospector introspector = new EntityMetadataIntrospector();
        EntityMetadata metadata = introspector.introspect(TestEntity.class);
        
        EntityFieldMetadata childrenField = metadata.getField("children").orElseThrow();
        assertEquals(EntityFieldMetadata.FieldType.ONE_TO_MANY, childrenField.getType());
        assertTrue(childrenField.isRelationship());
        assertTrue(childrenField.isCollection());
    }
}

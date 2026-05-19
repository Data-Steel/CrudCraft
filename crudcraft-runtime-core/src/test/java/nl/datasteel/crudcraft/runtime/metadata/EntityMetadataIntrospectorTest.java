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

package nl.datasteel.crudcraft.runtime.metadata;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import nl.datasteel.crudcraft.annotations.export.ExportExclude;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class EntityMetadataIntrospectorTest {

    private final EntityMetadataIntrospector introspector = new EntityMetadataIntrospector();

    @Test
    void introspectRejectsNonEntityClasses() {
        assertThrows(IllegalArgumentException.class, () -> introspector.introspect(String.class));
    }

    @Test
    void introspectClassifiesFieldsAndSkipsTransientOnes() {
        EntityMetadata metadata = introspector.introspect(ChildEntity.class);

        assertEquals(ChildEntity.class, metadata.getEntityClass());
        assertTrue(metadata.getField("id").isPresent());
        assertTrue(metadata.getField("baseField").isPresent());
        assertTrue(metadata.getField("plain").isPresent());
        assertTrue(metadata.getField("embeddedAddress").isPresent());
        assertTrue(metadata.getField("embeddedByType").isPresent());
        assertTrue(metadata.getField("manyToOne").isPresent());
        assertTrue(metadata.getField("oneToOne").isPresent());
        assertTrue(metadata.getField("oneToMany").isPresent());
        assertTrue(metadata.getField("manyToMany").isPresent());
        assertFalse(metadata.getField("staticField").isPresent());
        assertFalse(metadata.getField("transientField").isPresent());
        assertFalse(metadata.getField("jpaTransientField").isPresent());

        assertField(metadata, "plain", EntityFieldMetadata.FieldType.SCALAR, String.class);
        assertField(metadata, "embeddedAddress", EntityFieldMetadata.FieldType.EMBEDDED, Address.class);
        assertField(
                metadata,
                "embeddedByType",
                EntityFieldMetadata.FieldType.EMBEDDED,
                EmbeddedByType.class);
        assertField(
                metadata,
                "manyToOne",
                EntityFieldMetadata.FieldType.MANY_TO_ONE,
                RelatedEntity.class);
        assertField(
                metadata,
                "oneToOne",
                EntityFieldMetadata.FieldType.ONE_TO_ONE,
                RelatedEntity.class);
        assertField(
                metadata,
                "oneToMany",
                EntityFieldMetadata.FieldType.ONE_TO_MANY,
                RelatedEntity.class);
        assertField(
                metadata,
                "manyToMany",
                EntityFieldMetadata.FieldType.MANY_TO_MANY,
                RelatedEntity.class);
        assertField(metadata, "rawManyToMany", EntityFieldMetadata.FieldType.MANY_TO_MANY, Object.class);
        assertField(
                metadata,
                "wildcardOneToMany",
                EntityFieldMetadata.FieldType.ONE_TO_MANY,
                Object.class);
        assertFalse(metadata.getField("hidden").orElseThrow().isExportable());
    }

    @Test
    void collectFieldsAcceptsNullAndObjectInput() throws Exception {
        Method method =
                EntityMetadataIntrospector.class.getDeclaredMethod(
                        "collectFields", Class.class, List.class);
        method.setAccessible(true);
        List<EntityFieldMetadata> fields = new ArrayList<>();

        assertDoesNotThrow(() -> method.invoke(introspector, null, fields));
        assertDoesNotThrow(() -> method.invoke(introspector, Object.class, fields));
        assertTrue(fields.isEmpty());
    }

    private static void assertField(
            EntityMetadata metadata,
            String name,
            EntityFieldMetadata.FieldType type,
            Class<?> targetType) {
        EntityFieldMetadata field = metadata.getField(name).orElseThrow();

        assertEquals(type, field.getType());
        assertEquals(targetType, field.getTargetType());
    }

    @Entity
    private static class BaseEntity {
        private Long id;
        private String baseField;
    }

    @Entity
    private static final class ChildEntity extends BaseEntity {
        static String staticField;
        transient String transientField;
        @Transient private String jpaTransientField;
        @ExportExclude private String hidden;
        private String plain;
        @Embedded private Address embeddedAddress;
        private EmbeddedByType embeddedByType;
        @ManyToOne private RelatedEntity manyToOne;
        @OneToOne private RelatedEntity oneToOne;
        @OneToMany private List<RelatedEntity> oneToMany;
        @OneToMany private List<? extends RelatedEntity> wildcardOneToMany;
        @ManyToMany private List<RelatedEntity> manyToMany;

        @ManyToMany
        @SuppressWarnings("rawtypes")
        private List rawManyToMany;
    }

    @Embeddable
    private static final class Address {
        private String street;
    }

    @Embeddable
    private static final class EmbeddedByType {
        private String note;
    }

    @Entity
    private static final class RelatedEntity {
        private Long id;
    }
}

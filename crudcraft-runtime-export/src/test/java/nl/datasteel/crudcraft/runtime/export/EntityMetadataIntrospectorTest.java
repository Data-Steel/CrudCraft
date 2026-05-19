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

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import java.util.List;
import nl.datasteel.crudcraft.annotations.export.ExportExclude;
import nl.datasteel.crudcraft.runtime.metadata.EntityFieldMetadata;
import nl.datasteel.crudcraft.runtime.metadata.EntityMetadata;
import nl.datasteel.crudcraft.runtime.metadata.EntityMetadataIntrospector;
import org.junit.jupiter.api.Test;

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

        assertTrue(metadata.getField("id").isPresent());
        assertTrue(metadata.getField("baseField").isPresent());
        assertTrue(metadata.getField("embeddedAddress").isPresent());
        assertTrue(metadata.getField("embeddedByType").isPresent());
        assertTrue(metadata.getField("manyToOne").isPresent());
        assertTrue(metadata.getField("oneToOne").isPresent());
        assertTrue(metadata.getField("oneToMany").isPresent());
        assertTrue(metadata.getField("manyToMany").isPresent());
        assertFalse(metadata.getField("staticField").isPresent());
        assertFalse(metadata.getField("transientField").isPresent());
        assertFalse(metadata.getField("jpaTransientField").isPresent());

        EntityFieldMetadata embedded = metadata.getField("embeddedAddress").orElseThrow();
        EntityFieldMetadata embeddedByType = metadata.getField("embeddedByType").orElseThrow();
        EntityFieldMetadata manyToOne = metadata.getField("manyToOne").orElseThrow();
        EntityFieldMetadata oneToOne = metadata.getField("oneToOne").orElseThrow();
        EntityFieldMetadata oneToMany = metadata.getField("oneToMany").orElseThrow();
        EntityFieldMetadata manyToMany = metadata.getField("manyToMany").orElseThrow();
        EntityFieldMetadata rawManyToMany = metadata.getField("rawManyToMany").orElseThrow();
        EntityFieldMetadata wildcardOneToMany =
                metadata.getField("wildcardOneToMany").orElseThrow();
        EntityFieldMetadata hidden = metadata.getField("hidden").orElseThrow();

        assertEquals(EntityFieldMetadata.FieldType.EMBEDDED, embedded.getType());
        assertEquals(EntityFieldMetadata.FieldType.EMBEDDED, embeddedByType.getType());
        assertEquals(EntityFieldMetadata.FieldType.MANY_TO_ONE, manyToOne.getType());
        assertEquals(EntityFieldMetadata.FieldType.ONE_TO_ONE, oneToOne.getType());
        assertEquals(EntityFieldMetadata.FieldType.ONE_TO_MANY, oneToMany.getType());
        assertEquals(EntityFieldMetadata.FieldType.MANY_TO_MANY, manyToMany.getType());
        assertEquals(RelatedEntity.class, oneToMany.getTargetType());
        assertEquals(RelatedEntity.class, manyToMany.getTargetType());
        assertEquals(Object.class, rawManyToMany.getTargetType());
        assertEquals(Object.class, wildcardOneToMany.getTargetType());
        assertFalse(hidden.isExportable());
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

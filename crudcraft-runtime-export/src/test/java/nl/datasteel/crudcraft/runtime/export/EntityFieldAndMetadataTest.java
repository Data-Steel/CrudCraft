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

import jakarta.persistence.Entity;
import java.lang.reflect.Field;
import java.util.List;
import nl.datasteel.crudcraft.runtime.metadata.EntityFieldMetadata;
import nl.datasteel.crudcraft.runtime.metadata.EntityMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


class EntityFieldAndMetadataTest {

    @Test
    void fieldMetadataReportsTypeAndRelationshipFlags() throws NoSuchFieldException {
        Field nameField = DemoEntity.class.getDeclaredField("name");
        EntityFieldMetadata scalar =
                new EntityFieldMetadata(
                        "name",
                        nameField,
                        EntityFieldMetadata.FieldType.SCALAR,
                        String.class,
                        true);
        EntityFieldMetadata manyToMany =
                new EntityFieldMetadata(
                        "tags",
                        nameField,
                        EntityFieldMetadata.FieldType.MANY_TO_MANY,
                        String.class,
                        true);

        assertEquals("name", scalar.getName());
        assertSame(nameField, scalar.getField());
        assertEquals(String.class, scalar.getTargetType());
        assertTrue(scalar.isExportable());
        assertFalse(scalar.isCollection());
        assertFalse(scalar.isRelationship());

        assertTrue(manyToMany.isCollection());
        assertTrue(manyToMany.isRelationship());
    }

    @Test
    void metadataCanFindFieldsAndFilterExportableOnes() throws NoSuchFieldException {
        EntityFieldMetadata exportable =
                new EntityFieldMetadata(
                        "id",
                        DemoEntity.class.getDeclaredField("id"),
                        EntityFieldMetadata.FieldType.SCALAR,
                        Long.class,
                        true);
        EntityFieldMetadata hidden =
                new EntityFieldMetadata(
                        "secret",
                        DemoEntity.class.getDeclaredField("secret"),
                        EntityFieldMetadata.FieldType.SCALAR,
                        String.class,
                        false);

        EntityMetadata metadata = new EntityMetadata(DemoEntity.class, List.of(exportable, hidden));

        assertEquals(DemoEntity.class, metadata.getEntityClass());
        assertEquals(List.of(exportable, hidden), metadata.getFields());
        assertTrue(metadata.getField("id").isPresent());
        assertTrue(metadata.getField("missing").isEmpty());
        assertEquals(List.of(exportable), metadata.getExportableFields());
    }

    @Entity
    private static final class DemoEntity {
        private Long id;
        private String name;
        private String secret;
    }
}

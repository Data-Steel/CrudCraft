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

import jakarta.persistence.Entity;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class EntityFieldAndMetadataTest {

    @Test
    void fieldMetadataReportsTypeAndRelationshipFlags() throws NoSuchFieldException {
        Field nameField = DemoEntity.class.getDeclaredField("name");
        EntityFieldMetadata scalar = metadata("name", nameField, EntityFieldMetadata.FieldType.SCALAR);
        EntityFieldMetadata embedded =
                metadata("name", nameField, EntityFieldMetadata.FieldType.EMBEDDED);
        EntityFieldMetadata oneToMany =
                metadata("tags", nameField, EntityFieldMetadata.FieldType.ONE_TO_MANY);
        EntityFieldMetadata manyToMany =
                metadata("tags", nameField, EntityFieldMetadata.FieldType.MANY_TO_MANY);
        EntityFieldMetadata oneToOne =
                metadata("owner", nameField, EntityFieldMetadata.FieldType.ONE_TO_ONE);

        assertEquals("name", scalar.getName());
        assertSame(nameField, scalar.getField());
        assertEquals(String.class, scalar.getTargetType());
        assertTrue(scalar.isExportable());
        assertFalse(scalar.isCollection());
        assertFalse(scalar.isRelationship());
        assertFalse(embedded.isRelationship());
        assertTrue(oneToMany.isCollection());
        assertTrue(manyToMany.isCollection());
        assertTrue(oneToOne.isRelationship());
    }

    @Test
    void metadataCanFindFieldsFilterExportableOnesAndDefensivelyCopy()
            throws NoSuchFieldException {
        EntityFieldMetadata exportable =
                metadata(
                        "id",
                        DemoEntity.class.getDeclaredField("id"),
                        EntityFieldMetadata.FieldType.SCALAR,
                        Long.class,
                        true);
        EntityFieldMetadata hidden =
                metadata(
                        "secret",
                        DemoEntity.class.getDeclaredField("secret"),
                        EntityFieldMetadata.FieldType.SCALAR,
                        String.class,
                        false);
        List<EntityFieldMetadata> fields = new ArrayList<>(List.of(exportable, hidden));

        EntityMetadata metadata = new EntityMetadata(DemoEntity.class, fields);
        fields.clear();

        assertEquals(DemoEntity.class, metadata.getEntityClass());
        assertEquals(List.of(exportable, hidden), metadata.getFields());
        assertThrows(UnsupportedOperationException.class, () -> metadata.getFields().clear());
        assertTrue(metadata.getField("id").isPresent());
        assertTrue(metadata.getField("missing").isEmpty());
        assertEquals(List.of(exportable), metadata.getExportableFields());
    }

    private static EntityFieldMetadata metadata(
            String name, Field field, EntityFieldMetadata.FieldType type) {
        return metadata(name, field, type, String.class, true);
    }

    private static EntityFieldMetadata metadata(
            String name,
            Field field,
            EntityFieldMetadata.FieldType type,
            Class<?> targetType,
            boolean exportable) {
        return new EntityFieldMetadata(name, field, type, targetType, exportable);
    }

    @Entity
    private static final class DemoEntity {
        private Long id;
        private String name;
        private String secret;
    }
}

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

package nl.datasteel.crudcraft.codegen.descriptor;

import java.util.LinkedHashMap;
import java.util.Map;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SchemaMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SchemaMetadataTest {

    @Test
    void nullAdditionalPropertiesAreStoredAsEmptyMap() {
        SchemaMetadata metadata = new SchemaMetadata(null, null, null);

        assertTrue(metadata.additionalProperties().isEmpty());
        assertTrue(metadata.isEmpty());
    }

    @Test
    void additionalPropertiesAreCopiedAndMakeMetadataNonEmpty() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("format", "uuid");
        SchemaMetadata metadata = new SchemaMetadata(null, null, properties);
        properties.put("example", "ignored");

        assertEquals(Map.of("format", "uuid"), metadata.additionalProperties());
        assertFalse(metadata.isEmpty());
        assertThrows(
                UnsupportedOperationException.class,
                () -> metadata.additionalProperties().put("x", "y"));
    }

    @Test
    void descriptionOrExampleMakesMetadataNonEmpty() {
        assertFalse(new SchemaMetadata("Name", null, Map.of()).isEmpty());
        assertFalse(new SchemaMetadata(null, "Alice", Map.of()).isEmpty());
    }
}

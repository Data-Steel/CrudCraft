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

package nl.datasteel.crudcraft.codegen.projection;

import java.util.List;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectionMetadataTest {

    @Test
    void exposesDtoAndAttributeContract() {
        TestDto dto = new TestDto();
        ProjectionMetadata<TestDto> nested = new TestMetadata(List.of());
        ProjectionMetadata.Attribute attribute =
                new TestAttribute("roles", nested, true, (target, values) -> {
                    ((TestDto) target).roles = values;
                });
        ProjectionMetadata<TestDto> metadata = new TestMetadata(List.of(attribute));
        List<String> values = List.of("admin");

        attribute.mutator().accept(dto, values);

        assertEquals(TestDto.class, metadata.dtoType());
        assertEquals(List.of(attribute), metadata.attributes());
        assertEquals("roles", attribute.path());
        assertSame(nested, attribute.nested());
        assertTrue(attribute.collection());
        assertEquals(values, dto.roles);
    }

    @Test
    void exposesSimpleAttributeContract() {
        ProjectionMetadata.Attribute attribute =
                new TestAttribute("email", null, false, ProjectionMetadataTest::ignoreValues);

        assertEquals("email", attribute.path());
        assertNull(attribute.nested());
        assertFalse(attribute.collection());
    }

    private static void ignoreValues(Object target, List<?> values) {
        // No-op mutator for a simple non-collection attribute.
    }

    private record TestMetadata(List<ProjectionMetadata.Attribute> attributes)
            implements ProjectionMetadata<TestDto> {

        @Override
        public Class<TestDto> dtoType() {
            return TestDto.class;
        }
    }

    private record TestAttribute(
            String path,
            ProjectionMetadata<?> nested,
            boolean collection,
            BiConsumer<Object, List<?>> mutator)
            implements ProjectionMetadata.Attribute {
    }

    private static final class TestDto {
        private List<?> roles;
    }
}

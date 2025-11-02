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
package nl.datasteel.crudcraft.projection.mapping;

import java.util.List;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;
import nl.datasteel.crudcraft.projection.mapping.dto.AddressDto;
import nl.datasteel.crudcraft.projection.mapping.dto.UserDto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProjectionFieldResolverTest {

    static class AnnotatedDto {
        @ProjectionField("entity.name")
        private String custom;
        private String age;
        public AnnotatedDto(String custom, String age) {
            this.custom = custom; this.age = age; }
    }

    static class MissingFieldDto {
        public MissingFieldDto(String missing) { }
    }

    interface NoCtor { }

    static class RawCollectionDto {
        private List values;
        public RawCollectionDto(List values) { this.values = values; }
    }

    static class NonDtoNested {
        private Plain plain;
        static class Plain { }
        public NonDtoNested(Plain plain) { this.plain = plain; }
    }

    @Test
    void resolveMapsByAnnotationAndConvention() {
        ProjectionFieldResolver resolver = new ProjectionFieldResolver();
        var mappings = resolver.resolve(AnnotatedDto.class);
        assertEquals(2, mappings.size());
        assertEquals("entity.name", mappings.get(0).path());
        assertEquals("age", mappings.get(1).path());
        assertFalse(mappings.get(0).collection());
        assertNull(mappings.get(0).nestedType());
    }

    @Test
    void resolveThrowsWhenFieldMissing() {
        ProjectionFieldResolver resolver = new ProjectionFieldResolver();
        assertThrows(IllegalStateException.class, () -> resolver.resolve(MissingFieldDto.class));
    }

    @Test
    void resolveThrowsWhenNoConstructor() {
        ProjectionFieldResolver resolver = new ProjectionFieldResolver();
        assertThrows(IllegalStateException.class, () -> resolver.resolve(NoCtor.class));
    }

    @Test
    void resolveDetectsCollectionsAndNestedDtos() {
        ProjectionFieldResolver resolver = new ProjectionFieldResolver();
        var mappings = resolver.resolve(UserDto.class);
        assertEquals(3, mappings.size());
        assertFalse(mappings.get(0).collection());
        assertNull(mappings.get(0).nestedType());
        assertTrue(mappings.get(1).collection());
        assertEquals(AddressDto.class, mappings.get(1).nestedType());
        assertTrue(mappings.get(2).collection());
        assertNull(mappings.get(2).nestedType());
    }

    @Test
    void resolveHandlesRawCollections() {
        ProjectionFieldResolver resolver = new ProjectionFieldResolver();
        var mappings = resolver.resolve(RawCollectionDto.class);
        assertTrue(mappings.getFirst().collection());
        assertNull(mappings.getFirst().nestedType());
    }

    @Test
    void resolveIgnoresNonDtoNestedTypes() {
        ProjectionFieldResolver resolver = new ProjectionFieldResolver();
        var mappings = resolver.resolve(NonDtoNested.class);
        assertNull(mappings.getFirst().nestedType());
    }
}

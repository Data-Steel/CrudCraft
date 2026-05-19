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

package nl.datasteel.crudcraft.runtime.projection.mapping.dto;

import java.util.List;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;
import nl.datasteel.crudcraft.runtime.projection.mapping.ProjectionFieldResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ProjectionFieldResolverTest {

    private final ProjectionFieldResolver resolver = new ProjectionFieldResolver();

    @Test
    void resolvesByParameterNameAndProjectionFieldValue() {
        List<ProjectionFieldResolver.FieldMapping> mappings = resolver.resolve(ByNameDto.class);

        assertEquals(3, mappings.size());
        assertEquals("title", mappings.get(0).path());
        assertFalse(mappings.get(0).collection());
        assertNull(mappings.get(0).nestedType());

        assertEquals("author.name", mappings.get(1).path());
        assertFalse(mappings.get(1).collection());
        assertNull(mappings.get(1).nestedType());

        assertEquals("child", mappings.get(2).path());
        assertFalse(mappings.get(2).collection());
        assertEquals(ChildDto.class, mappings.get(2).nestedType());
    }

    @Test
    void resolvesUsingCollectionDtoTieBreakerAndWildcardElement() {
        List<ProjectionFieldResolver.FieldMapping> mappings =
                resolver.resolve(CollectionTieBreakerDto.class);

        assertEquals(1, mappings.size());
        assertTrue(mappings.get(0).collection());
        assertEquals(ChildDto.class, mappings.get(0).nestedType());
        assertEquals("children", mappings.get(0).path());

        List<ProjectionFieldResolver.FieldMapping> wildcard =
                resolver.resolve(WildcardCollectionDto.class);
        assertEquals(1, wildcard.size());
        assertTrue(wildcard.get(0).collection());
        assertEquals(ChildDto.class, wildcard.get(0).nestedType());
        assertEquals("wildcardChildren", wildcard.get(0).path());
    }

    @Test
    void resolvesUsingAnnotatedTieBreakerAndDeclaredIndexFallback() {
        List<ProjectionFieldResolver.FieldMapping> annotated =
                resolver.resolve(AnnotatedTieBreakerDto.class);
        assertEquals("preferred.path", annotated.get(0).path());

        List<ProjectionFieldResolver.FieldMapping> fallback =
                resolver.resolve(IndexFallbackDto.class);
        assertEquals("alpha", fallback.get(0).path());
        assertEquals("beta", fallback.get(1).path());
    }

    @Test
    void declaredIndexFallbackUsesOriginalFieldOrderWhenEarlierFieldWasConsumed() {
        List<ProjectionFieldResolver.FieldMapping> mappings =
                resolver.resolve(ShiftedFallbackDto.class);

        assertEquals(3, mappings.size());
        assertEquals("alpha", mappings.get(0).path());
        assertEquals("beta", mappings.get(1).path());
        assertEquals("gamma", mappings.get(2).path());
    }

    @Test
    void declaredIndexFallbackHandlesMissingOriginalSlotByUsingRemainingIndex() {
        List<ProjectionFieldResolver.FieldMapping> mappings =
                resolver.resolve(ConsumedIndexSlotDto.class);

        assertEquals(2, mappings.size());
        assertEquals("beta", mappings.get(0).path());
        assertEquals("gamma", mappings.get(1).path());
    }

    @Test
    void declaredIndexFallbackSupportsIndexZeroWhenNoTypeMatchExists() {
        List<ProjectionFieldResolver.FieldMapping> mappings =
                resolver.resolve(IndexZeroFallbackDto.class);

        assertEquals(2, mappings.size());
        assertEquals("alpha", mappings.get(0).path());
        assertEquals("beta", mappings.get(1).path());
    }

    @Test
    void resolvesUsingConstructorWithMostParameters() {
        List<ProjectionFieldResolver.FieldMapping> mappings =
                resolver.resolve(MultiConstructorDto.class);

        assertEquals(2, mappings.size());
        assertEquals("first", mappings.get(0).path());
        assertEquals("second", mappings.get(1).path());
    }

    @Test
    void resolvesCollectionElementDefaultsToObjectWhenRawOrNestedGeneric() {
        List<ProjectionFieldResolver.FieldMapping> mappings =
                resolver.resolve(RawCollectionsDto.class);

        assertEquals(2, mappings.size());
        assertTrue(mappings.get(0).collection());
        assertNull(mappings.get(0).nestedType());

        assertTrue(mappings.get(1).collection());
        assertNull(mappings.get(1).nestedType());
    }

    @Test
    void throwsWhenTypeHasNoConstructors() {
        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> resolver.resolve(NoConstructorType.class));

        assertTrue(exception.getMessage().contains("No constructor found"));
    }

    static class ByNameDto {
        private final String title;

        @ProjectionField("author.name")
        private final String authorName;

        private final ChildDto child;

        ByNameDto(String title, String authorName, ChildDto child) {
            this.title = title;
            this.authorName = authorName;
            this.child = child;
        }
    }

    static class CollectionTieBreakerDto {
        private final List<String> labels;
        private final List<ChildDto> children;

        CollectionTieBreakerDto(List<?> values) {
            this.labels = List.of();
            this.children = List.of();
        }
    }

    static class WildcardCollectionDto {
        private final List<? extends ChildDto> wildcardChildren;

        WildcardCollectionDto(List<? extends ChildDto> wildcardChildren) {
            this.wildcardChildren = wildcardChildren;
        }
    }

    static class AnnotatedTieBreakerDto {
        @ProjectionField("preferred.path")
        private final String preferred;

        private final String other;

        AnnotatedTieBreakerDto(String input) {
            this.preferred = input;
            this.other = input;
        }
    }

    static class IndexFallbackDto {
        private final String alpha;
        private final String beta;

        IndexFallbackDto(String first, String second) {
            this.alpha = first;
            this.beta = second;
        }
    }

    static class RawCollectionsDto {
        @SuppressWarnings("rawtypes")
        private final List raw;

        private final List<List<String>> nestedGeneric;

        @SuppressWarnings("rawtypes")
        RawCollectionsDto(List raw, List<List<String>> nestedGeneric) {
            this.raw = raw;
            this.nestedGeneric = nestedGeneric;
        }
    }

    static class ShiftedFallbackDto {
        private final String alpha;
        private final String beta;
        private final String gamma;

        ShiftedFallbackDto(String alpha, String second, String gamma) {
            this.alpha = alpha;
            this.beta = second;
            this.gamma = gamma;
        }
    }

    static class MultiConstructorDto {
        private final String first;
        private final String second;

        MultiConstructorDto(String only) {
            this.first = only;
            this.second = only;
        }

        MultiConstructorDto(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }

    static class ConsumedIndexSlotDto {
        private final String alpha;
        private final Integer beta;
        private final String gamma;

        ConsumedIndexSlotDto(Integer first, Object second) {
            this.alpha = "a";
            this.beta = first;
            this.gamma = second == null ? "" : second.toString();
        }
    }

    static class IndexZeroFallbackDto {
        private final String alpha;
        private final Integer beta;

        IndexZeroFallbackDto(Object first, Integer second) {
            this.alpha = first == null ? "" : first.toString();
            this.beta = second;
        }
    }

    interface NoConstructorType {}

    static class ChildDto {
        private final String name;

        ChildDto(String name) {
            this.name = name;
        }
    }
}

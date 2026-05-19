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

package nl.datasteel.crudcraft.runtime.projection.mapping;

import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Selection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ProjectionMapperTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void constructBuildsSelectionsForSimpleNestedAndCollectionMappings() {
        ProjectionFieldResolver resolver = mock(ProjectionFieldResolver.class);
        ProjectionMapper mapper = new ProjectionMapper(resolver);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        From root = mock(From.class);
        Join authorJoin = mock(Join.class);
        Path titlePath = mock(Path.class);
        Path nestedNamePath = mock(Path.class);
        Expression collectionSelection = mock(Expression.class);
        CompoundSelection nestedSelection = mock(CompoundSelection.class);
        CompoundSelection resultSelection = mock(CompoundSelection.class);

        when(resolver.resolve(ParentDto.class))
                .thenReturn(
                        List.of(
                                new ProjectionFieldResolver.FieldMapping("title", false, null),
                                new ProjectionFieldResolver.FieldMapping(
                                        "author", false, ChildDto.class),
                                new ProjectionFieldResolver.FieldMapping("tags", true, null)));
        when(resolver.resolve(ChildDto.class))
                .thenReturn(List.of(new ProjectionFieldResolver.FieldMapping("name", false, null)));

        when(root.get("title")).thenReturn(titlePath);
        when(root.join("author", JoinType.LEFT)).thenReturn(authorJoin);
        when(authorJoin.get("name")).thenReturn(nestedNamePath);
        when(cb.literal(Collections.emptyList())).thenReturn(collectionSelection);
        when(cb.construct(eq(ChildDto.class), any(Selection[].class))).thenReturn(nestedSelection);
        when(cb.construct(eq(ParentDto.class), any(Selection[].class))).thenReturn(resultSelection);

        Selection<ParentDto> selection = mapper.construct(cb, root, ParentDto.class);

        assertSame(resultSelection, selection);
        verify(root).get("title");
        verify(root).join("author", JoinType.LEFT);
        verify(authorJoin).get("name");
        verify(cb).literal(Collections.emptyList());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void constructResolvesDottedSimplePathViaJoins() {
        ProjectionFieldResolver resolver = mock(ProjectionFieldResolver.class);
        ProjectionMapper mapper = new ProjectionMapper(resolver);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        From root = mock(From.class);
        Join addressJoin = mock(Join.class);
        Path cityPath = mock(Path.class);
        CompoundSelection resultSelection = mock(CompoundSelection.class);

        when(resolver.resolve(AddressDto.class))
                .thenReturn(
                        List.of(
                                new ProjectionFieldResolver.FieldMapping(
                                        "address.city", false, null)));
        when(root.join("address", JoinType.LEFT)).thenReturn(addressJoin);
        when(addressJoin.get("city")).thenReturn(cityPath);
        when(cb.construct(eq(AddressDto.class), any(Selection[].class)))
                .thenReturn(resultSelection);

        Selection<AddressDto> selection = mapper.construct(cb, root, AddressDto.class);

        assertSame(resultSelection, selection);
        verify(root).join("address", JoinType.LEFT);
        verify(addressJoin).get("city");
    }

    static class ParentDto {
        ParentDto(String title, ChildDto author, List<String> tags) {}
    }

    static class ChildDto {
        ChildDto(String name) {}
    }

    static class AddressDto {
        AddressDto(String city) {}
    }
}

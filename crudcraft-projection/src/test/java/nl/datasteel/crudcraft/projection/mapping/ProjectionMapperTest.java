/*
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.projection.mapping;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.PathBuilder;
import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Selection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class ProjectionMapperTest {

    // DTOs with constructors that match the expressions used by the mapper
    static class NestedDto {
        NestedDto(Object street) {}
    }
    static class RootDto {
        RootDto(Object name, NestedDto child, Object tags) {}
    }

    @Test
    void constructCriteriaHandlesCollectionsAndNestedTypes() {
        var resolver = mock(ProjectionFieldResolver.class);
        var simple = new ProjectionFieldResolver.FieldMapping("name", false, null);
        var nested = new ProjectionFieldResolver.FieldMapping("child", false, NestedDto.class);
        var collection = new ProjectionFieldResolver.FieldMapping("tags", true, null);

        when(resolver.resolve(RootDto.class)).thenReturn(List.of(simple, nested, collection));
        when(resolver.resolve(NestedDto.class))
                .thenReturn(List.of(new ProjectionFieldResolver.FieldMapping("street", false, null)));

        var mapper = new ProjectionMapper(resolver);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        From<?, ?> from = mock(From.class);
        Join<?, ?> join = mock(Join.class);

        @SuppressWarnings("unchecked") Path<Object> namePath = mock(Path.class);
        @SuppressWarnings("unchecked") Path<Object> streetPath = mock(Path.class);

        // IMPORTANT: use jakarta.persistence.criteria.Expression here (JPA Expression)
        @SuppressWarnings("unchecked") jakarta.persistence.criteria.Expression<List<?>> literalExpr = mock(jakarta.persistence.criteria.Expression.class);
        @SuppressWarnings("unchecked") CompoundSelection<NestedDto> nestedSel = mock(CompoundSelection.class);
        @SuppressWarnings("unchecked") CompoundSelection<RootDto> rootSel = mock(CompoundSelection.class);

        // stubbing
        when(from.get("name")).thenReturn(namePath);
        doReturn(join).when(from).join("child", JoinType.LEFT);
        when(join.get("street")).thenReturn(streetPath);

        // avoid generics inference issues
        doReturn(literalExpr).when(cb).literal(anyList());

        when(cb.construct(eq(NestedDto.class), any(jakarta.persistence.criteria.Selection[].class)))
                .thenReturn(nestedSel);
        when(cb.construct(eq(RootDto.class), any(jakarta.persistence.criteria.Selection[].class)))
                .thenReturn(rootSel);

        // act
        Selection<?> result = mapper.construct(cb, from, RootDto.class);

        // assert
        assertSame(rootSel, result);

        var listCap = ArgumentCaptor.forClass(List.class);
        verify(cb).literal(listCap.capture());
        assertTrue(listCap.getValue().isEmpty());

        verify(from).get("name");
        verify(from).join("child", JoinType.LEFT);
        verify(join).get("street");
    }

    @Test
    void constructQuerydslHandlesMappings() {
        var resolver = mock(ProjectionFieldResolver.class);
        var simple = new ProjectionFieldResolver.FieldMapping("name", false, null);
        var nested = new ProjectionFieldResolver.FieldMapping("child", false, NestedDto.class);
        var collection = new ProjectionFieldResolver.FieldMapping("tags", true, null);

        when(resolver.resolve(RootDto.class)).thenReturn(List.of(simple, nested, collection));
        when(resolver.resolve(NestedDto.class)).thenReturn(
                List.of(new ProjectionFieldResolver.FieldMapping("street", false, null)));

        var mapper = new ProjectionMapper(resolver);
        PathBuilder<Object> root = new PathBuilder<>(Object.class, "root");

        // this will now find RootDto(Object, NestedDto, Object) and NestedDto(Object)
        Expression<RootDto> expr = mapper.construct(root, RootDto.class);
        assertNotNull(expr);
    }
}

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
package nl.datasteel.crudcraft.projection.impl.querydsl;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.function.BiConsumer;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadata;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.projection.mapping.ProjectionMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MetadataQuerydslProjectionBuilderTest {

    public static class TestDto {
        public TestDto(Object children) {}
    }

    public static class NestedDto {
        public NestedDto() {}
    }


    record Meta<D>(Class<D> type, List<ProjectionMetadata.Attribute> attrs) implements ProjectionMetadata<D> {
        @Override public Class<D> dtoType() { return type; }
        @Override public List<ProjectionMetadata.Attribute> attributes() { return attrs; }
    }
    record Attr(String path, ProjectionMetadata<?> nested, boolean collection,
                BiConsumer<Object,List<?>> mutator) implements ProjectionMetadata.Attribute {
        @Override public String path() { return path; }
        @Override public ProjectionMetadata<?> nested() { return nested; }
        @Override public boolean collection() { return collection; }
        @Override public BiConsumer<Object,List<?>> mutator() { return mutator; }
    }

    @Test
    void buildAddsJoinsForNestedAndDotPaths() {
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        ProjectionMetadata<NestedDto> nested = new Meta<>(NestedDto.class, List.of());
        ProjectionMetadata<TestDto> meta = new Meta<>(TestDto.class, List.of(
                new Attr("name", null, false, null),
                new Attr("nested", nested, false, null),
                new Attr("company.name", null, false, null),
                new Attr("children", null, true, (a,b)->{})
        ));
        when(registry.getMetadata(TestDto.class)).thenReturn(meta);
        when(registry.getMetadata(NestedDto.class)).thenReturn(nested);

        MetadataQuerydslProjectionBuilder builder = new MetadataQuerydslProjectionBuilder(registry);

        JPAQueryFactory factory = mock(JPAQueryFactory.class);

        JPAQuery<?> query = mock(JPAQuery.class, RETURNS_SELF);

        when(factory.from(any(PathBuilder.class))).thenReturn((JPAQuery) query);

        builder.build(factory, Object.class, TestDto.class, mock(ProjectionQuery.class));

        ArgumentCaptor<PathBuilder> captor = ArgumentCaptor.forClass(PathBuilder.class);
        verify(query, times(2)).leftJoin(captor.capture());
        List<PathBuilder> paths = captor.getAllValues();
        assertEquals(2, paths.size());
        assertTrue(paths.get(0).toString().contains("nested") || paths.get(1).toString().contains("nested"));
        assertTrue(paths.get(0).toString().contains("company") || paths.get(1).toString().contains("company"));

        verify(query).select(any(Expression.class));
        verify(query).distinct();
    }

    @Test
    void buildDeduplicatesJoinsForRepeatedPaths() {
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        ProjectionMetadata<TestDto> meta = new Meta<>(TestDto.class, List.of(
                new Attr("child.name", null, false, null),
                new Attr("child.age", null, false, null)
        ));
        when(registry.getMetadata(TestDto.class)).thenReturn(meta);

        MetadataQuerydslProjectionBuilder builder = new MetadataQuerydslProjectionBuilder(registry);

        JPAQueryFactory factory = mock(JPAQueryFactory.class);

        JPAQuery<?> query = mock(JPAQuery.class, RETURNS_SELF);

        when(factory.from(any(PathBuilder.class))).thenReturn((JPAQuery) query);

        builder.build(factory, Object.class, TestDto.class, mock(ProjectionQuery.class));

        verify(query).select(any(Expression.class));
        verify(query).distinct();
        verify(query, times(1)).leftJoin(any(PathBuilder.class));
    }

    @Test
    void constructUsesConstantForCollections() {
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        ProjectionMetadata<TestDto> meta = new Meta<>(TestDto.class, List.of(
                new Attr("children", null, true, (a,b)->{})
        ));
        when(registry.getMetadata(TestDto.class)).thenReturn(meta);
        MetadataQuerydslProjectionBuilder builder = new MetadataQuerydslProjectionBuilder(registry);

        PathBuilder<?> root = new PathBuilder<>(Object.class, "root");
        Expression<TestDto> expr = builder.construct(root, TestDto.class);

        ConstructorExpression<?> ce = (ConstructorExpression<?>) expr;
        assertEquals(1, ce.getArgs().size());
        assertEquals(Expressions.nullExpression().toString(), ce.getArgs().getFirst().toString());
    }

    @Test
    void constructFallsBackToMapperWhenNoMetadata() {
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        ProjectionMapper mapper = mock(ProjectionMapper.class);
        MetadataQuerydslProjectionBuilder builder = new MetadataQuerydslProjectionBuilder(registry, mapper);
        PathBuilder<?> root = new PathBuilder<>(Object.class, "root");
        Expression<TestDto> expr = mock(Expression.class);
        when(mapper.construct(root, TestDto.class)).thenReturn(expr);

        builder.construct(root, TestDto.class);
        verify(mapper).construct(root, TestDto.class);
    }
}

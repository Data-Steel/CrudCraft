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
package nl.datasteel.crudcraft.projection.impl.jpa;

import jakarta.persistence.criteria.*;
import java.util.List;
import java.util.function.BiConsumer;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadata;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.projection.mapping.ProjectionMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MetadataCriteriaProjectionBuilderTest {

    static class TestDto { }
    static class ChildDto { }

    record Meta<D>(Class<D> type, List<ProjectionMetadata.Attribute> attrs)
            implements ProjectionMetadata<D> {
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
    @SuppressWarnings({"unchecked","rawtypes"})
    void buildConstructsCriteriaQuery() {
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        when(registry.getMetadata(TestDto.class)).thenReturn(new Meta<>(TestDto.class, List.of()));
        MetadataCriteriaProjectionBuilder builder = new MetadataCriteriaProjectionBuilder(registry);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<TestDto> cq = mock(CriteriaQuery.class);
        Root<Object> root = mock(Root.class);
        CompoundSelection<TestDto> sel = mock(CompoundSelection.class);

        when(cb.createQuery(TestDto.class)).thenReturn(cq);
        when(cq.from(Object.class)).thenReturn(root);
        when(cb.construct(eq(TestDto.class), any(Selection[].class))).thenReturn(sel);
        when(cq.select(sel)).thenReturn(cq);
        when(cq.distinct(true)).thenReturn(cq);

        builder.build(cb, Object.class, TestDto.class, mock(ProjectionQuery.class));

        verify(cb).createQuery(TestDto.class);
        verify(cq).from(Object.class);
        verify(cb).construct(eq(TestDto.class), any(Selection[].class));
        verify(cq).select(sel);
        verify(cq).distinct(true);
    }

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    void constructUsesMetadataAndCreatesJoinForNested() {
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);

        ProjectionMetadata<ChildDto> childMeta = new Meta<>(ChildDto.class, List.of());
        ProjectionMetadata<TestDto> meta = new Meta<>(TestDto.class, List.of(
                new Attr("child", childMeta, false, null),
                new Attr("values", null, true, (a,b)->{})
        ));
        when(registry.getMetadata(TestDto.class)).thenReturn(meta);
        when(registry.getMetadata(ChildDto.class)).thenReturn(childMeta);

        MetadataCriteriaProjectionBuilder builder = new MetadataCriteriaProjectionBuilder(registry);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        From<?,?> from = mock(From.class);
        Join<?,?> join = mock(Join.class);

        when(from.join("child", JoinType.LEFT)).thenReturn((Join) join);

        Expression<List<?>> emptyExpr = mock(Expression.class);
        doReturn(emptyExpr).when(cb).literal(anyList());

        CompoundSelection<ChildDto> childSel = mock(CompoundSelection.class);
        CompoundSelection<TestDto> dtoSel = mock(CompoundSelection.class);
        when(cb.construct(eq(ChildDto.class), any(Selection[].class))).thenReturn(childSel);
        when(cb.construct(eq(TestDto.class),   any(Selection[].class))).thenReturn(dtoSel);

        Selection<TestDto> out = builder.construct(cb, from, TestDto.class);

        assertSame(dtoSel, out);
        verify(from).join("child", JoinType.LEFT);
        verify(cb).construct(eq(ChildDto.class), any(Selection[].class));
        verify(cb).construct(eq(TestDto.class),  any(Selection[].class));
    }

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    void constructReusesJoinForRepeatedDotPaths() {
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        ProjectionMetadata<TestDto> meta = new Meta<>(TestDto.class, List.of(
                new Attr("child.name", null, false, null),
                new Attr("child.age",  null, false, null)
        ));
        when(registry.getMetadata(TestDto.class)).thenReturn(meta);

        MetadataCriteriaProjectionBuilder builder = new MetadataCriteriaProjectionBuilder(registry);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        From<?,?> from = mock(From.class);
        Join<?,?> join = mock(Join.class);

        when(from.join("child", JoinType.LEFT)).thenReturn((Join) join);
        Path<?> namePath = mock(Path.class);
        Path<?> agePath  = mock(Path.class);
        when(join.get("name")).thenReturn((Path) namePath);
        when(join.get("age")).thenReturn((Path) agePath);

        CompoundSelection<TestDto> dtoSel = mock(CompoundSelection.class);
        when(cb.construct(eq(TestDto.class), any(Selection[].class))).thenReturn(dtoSel);

        builder.construct(cb, from, TestDto.class);

        verify(from, times(1)).join("child", JoinType.LEFT);
        verify(cb).construct(eq(TestDto.class), any(Selection[].class));
    }

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    void constructFallsBackToMapperWhenNoMetadata() {
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        // getMetadata(TestDto.class) -> null by default
        ProjectionMapper mapper = mock(ProjectionMapper.class);
        MetadataCriteriaProjectionBuilder builder = new MetadataCriteriaProjectionBuilder(registry, mapper);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        From<?,?> from = mock(From.class);

        Selection<TestDto> mapped = mock(Selection.class);
        when(mapper.construct(cb, from, TestDto.class)).thenReturn(mapped);

        Selection<TestDto> out = builder.construct(cb, from, TestDto.class);

        assertSame(mapped, out);
        verify(mapper).construct(cb, from, TestDto.class);
        verifyNoInteractions(from);
    }
}

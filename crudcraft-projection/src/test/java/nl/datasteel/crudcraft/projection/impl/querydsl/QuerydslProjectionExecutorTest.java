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

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.Type;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadata;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.projection.api.ProjectionResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QuerydslProjectionExecutorTest {

    static class Entity { Long id; String name; }
    static class Dto { List<ChildDto> children = new ArrayList<>(); }
    static class ChildDto { }

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

    @SuppressWarnings("unchecked")
    @Test
    void projectAppliesFilterSortAndPaging() {
        JPAQueryFactory factory = mock(JPAQueryFactory.class);
        JPAQuery<Tuple> main = mock(JPAQuery.class, RETURNS_SELF);
        JPAQuery<Long> count = mock(JPAQuery.class, RETURNS_SELF);
        when(factory.select(any(Expression.class), any(Expression.class))).thenReturn(main);
        // help generics a bit for the single-select:
        when(factory.select(Mockito.<Expression<Long>>any())).thenReturn(count);
        when(main.from(any(PathBuilder.class))).thenReturn(main);
        when(count.from(any(PathBuilder.class))).thenReturn(count);

        Tuple tuple = mock(Tuple.class);
        when(tuple.get(any(Expression.class))).thenReturn(1L, new Dto());
        when(main.fetch()).thenReturn(List.of(tuple));
        when(count.fetchOne()).thenReturn(1L);

        QuerydslProjectionBuilder builder = mock(QuerydslProjectionBuilder.class);
        Expression<Dto> dtoExpr = mock(Expression.class);
        when(builder.construct(any(PathBuilder.class), eq(Dto.class))).thenReturn(dtoExpr);

        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        Metamodel metamodel = mock(Metamodel.class);
        EntityType<Entity> entityType = mock(EntityType.class);
        Type idType = mock(Type.class);
        SingularAttribute idAttr = mock(SingularAttribute.class);
        when(metamodel.entity(Entity.class)).thenReturn(entityType);
        when(entityType.getIdType()).thenReturn(idType);
        when(idType.getJavaType()).thenReturn(Long.class);
        when(entityType.getId(Long.class)).thenReturn(idAttr);
        when(idAttr.getName()).thenReturn("id");

        QuerydslProjectionExecutor executor = new QuerydslProjectionExecutor(factory, metamodel, builder, registry);
        ProjectionQuery<Entity> query = mock(ProjectionQuery.class);
        Predicate predicate = mock(Predicate.class);
        Pageable pageable = PageRequest.of(1,2, Sort.by("name"));
        when(query.asPredicate()).thenReturn(Optional.of(predicate));
        when(query.pageable()).thenReturn(pageable);

        ProjectionResult<Dto> result = executor.project(Entity.class, Dto.class, query);
        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements()); // <- changed
        verify(main).where(predicate);
        verify(main).orderBy(any(OrderSpecifier.class));
        verify(main).offset(2);
        verify(main).limit(2);
    }

    static class NonComp { Long id; Object value; }

    @SuppressWarnings("unchecked")
    @Test
    void projectThrowsOnNonComparableSort() {
        JPAQueryFactory factory = mock(JPAQueryFactory.class);
        JPAQuery<Tuple> main = mock(JPAQuery.class, RETURNS_SELF);
        when(factory.select(any(Expression.class), any(Expression.class))).thenReturn(main);
        when(main.from(any(PathBuilder.class))).thenReturn(main);

        QuerydslProjectionBuilder builder = mock(QuerydslProjectionBuilder.class);
        Expression<Object> dtoExpr = mock(Expression.class);
        when(builder.construct(any(PathBuilder.class), any())).thenReturn(dtoExpr);

        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        Metamodel metamodel = mock(Metamodel.class);
        EntityType<NonComp> entityType = mock(EntityType.class);
        Type idType = mock(Type.class);
        SingularAttribute idAttr = mock(SingularAttribute.class);
        when(metamodel.entity(NonComp.class)).thenReturn(entityType);
        when(entityType.getIdType()).thenReturn(idType);
        when(idType.getJavaType()).thenReturn(Long.class);
        when(entityType.getId(Long.class)).thenReturn(idAttr);
        when(idAttr.getName()).thenReturn("id");

        QuerydslProjectionExecutor executor = new QuerydslProjectionExecutor(factory, metamodel, builder, registry);
        ProjectionQuery<NonComp> query = mock(ProjectionQuery.class);
        when(query.asPredicate()).thenReturn(Optional.empty());
        when(query.pageable()).thenReturn(PageRequest.of(0,1, Sort.by("value")));

        assertThrows(IllegalArgumentException.class,
                () -> executor.project(NonComp.class, Object.class, query));
    }

    @SuppressWarnings("unchecked")
    @Test
    void projectSkipsFilterAndPagingWhenAbsent() {
        JPAQueryFactory factory = mock(JPAQueryFactory.class);
        JPAQuery<Tuple> main = mock(JPAQuery.class, RETURNS_SELF);
        JPAQuery<Long> count = mock(JPAQuery.class, RETURNS_SELF);
        when(factory.select(any(Expression.class), any(Expression.class))).thenReturn(main);
        when(factory.select(Mockito.<Expression<Long>>any())).thenReturn(count);
        when(main.from(any(PathBuilder.class))).thenReturn(main);
        when(count.from(any(PathBuilder.class))).thenReturn(count);
        when(main.fetch()).thenReturn(List.of());
        when(count.fetchOne()).thenReturn(0L);

        QuerydslProjectionBuilder builder = mock(QuerydslProjectionBuilder.class);
        Expression<Dto> dtoExpr = mock(Expression.class);
        when(builder.construct(any(PathBuilder.class), eq(Dto.class))).thenReturn(dtoExpr);

        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        Metamodel metamodel = mock(Metamodel.class);
        EntityType<Entity> entityType = mock(EntityType.class);
        Type idType = mock(Type.class);
        SingularAttribute idAttr = mock(SingularAttribute.class);
        when(metamodel.entity(Entity.class)).thenReturn(entityType);
        when(entityType.getIdType()).thenReturn(idType);
        when(idType.getJavaType()).thenReturn(Long.class);
        when(entityType.getId(Long.class)).thenReturn(idAttr);
        when(idAttr.getName()).thenReturn("id");

        QuerydslProjectionExecutor executor = new QuerydslProjectionExecutor(factory, metamodel, builder, registry);
        ProjectionQuery<Entity> query = mock(ProjectionQuery.class);
        when(query.asPredicate()).thenReturn(Optional.empty());
        when(query.pageable()).thenReturn(null);

        ProjectionResult<Dto> result = executor.project(Entity.class, Dto.class, query);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements()); // <- changed
        verify(main, never()).where(any(Predicate.class));
        verify(main, never()).orderBy(any(OrderSpecifier.class));
        verify(main, never()).offset(anyLong());
        verify(main, never()).limit(anyLong());
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    @Test
    void projectReturnsZeroWhenCountNull() {
        JPAQueryFactory factory = mock(JPAQueryFactory.class);
        JPAQuery<Tuple> main = mock(JPAQuery.class, RETURNS_SELF);
        JPAQuery<Long> count = mock(JPAQuery.class, RETURNS_SELF);
        when(factory.select(any(Expression.class), any(Expression.class))).thenReturn(main);
        when(factory.select(Mockito.<Expression<Long>>any())).thenReturn(count);
        when(main.from(any(PathBuilder.class))).thenReturn(main);
        when(count.from(any(PathBuilder.class))).thenReturn(count);
        when(main.fetch()).thenReturn(List.of());
        when(count.fetchOne()).thenReturn(null);

        QuerydslProjectionBuilder builder = mock(QuerydslProjectionBuilder.class);
        Expression<Dto> dtoExpr = mock(Expression.class);
        when(builder.construct(any(PathBuilder.class), eq(Dto.class))).thenReturn(dtoExpr);

        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        Metamodel metamodel = mock(Metamodel.class);
        EntityType<Entity> entityType = mock(EntityType.class);
        Type idType = mock(Type.class);
        SingularAttribute idAttr = mock(SingularAttribute.class);
        when(metamodel.entity(Entity.class)).thenReturn(entityType);
        when(entityType.getIdType()).thenReturn(idType);
        when(idType.getJavaType()).thenReturn(Long.class);
        when(entityType.getId(Long.class)).thenReturn(idAttr);
        when(idAttr.getName()).thenReturn("id");

        QuerydslProjectionExecutor executor = new QuerydslProjectionExecutor(factory, metamodel, builder, registry);
        ProjectionQuery<Entity> query = mock(ProjectionQuery.class);
        when(query.asPredicate()).thenReturn(Optional.empty());
        when(query.pageable()).thenReturn(Pageable.unpaged());

        ProjectionResult<Dto> result = executor.project(Entity.class, Dto.class, query);
        assertEquals(0, result.totalElements()); // <- changed
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    @Test
    void projectHydratesCollections() {
        JPAQueryFactory factory = mock(JPAQueryFactory.class);
        JPAQuery<Tuple> main = mock(JPAQuery.class, RETURNS_SELF);
        JPAQuery<Long> count = mock(JPAQuery.class, RETURNS_SELF);
        // use raw/wildcard for joinQuery to avoid generics headaches
        JPAQuery joinQuery = mock(JPAQuery.class, RETURNS_SELF);

        when(factory.select(any(Expression.class), any(Expression.class))).thenReturn(main);
        when(factory.select(Mockito.<Expression<Long>>any())).thenReturn(count);
        when(main.from(any(PathBuilder.class))).thenReturn(main);
        when(count.from(any(PathBuilder.class))).thenReturn(count);

        // from(...) used for hydration path
        when(factory.from(any(PathBuilder.class))).thenReturn(joinQuery);
        when(joinQuery.leftJoin(any(PathBuilder.class))).thenReturn(joinQuery);
        when(joinQuery.where(any(Predicate.class))).thenReturn(joinQuery);

        Tuple tuple = mock(Tuple.class);
        Dto dto = new Dto();
        ChildDto child = new ChildDto();
        when(tuple.get(any(Expression.class))).thenReturn(1L, dto);
        when(main.fetch()).thenReturn(List.of(tuple));
        when(count.fetchOne()).thenReturn(1L);

        // transform(...) returns Map<Object, Map<Object, Object>>
        when(joinQuery.transform(any())).thenReturn(Map.of(1L, Map.of(10L, child)));

        QuerydslProjectionBuilder builder = mock(QuerydslProjectionBuilder.class);
        Expression<Dto> dtoExpr = mock(Expression.class);
        Expression<ChildDto> childExpr = mock(Expression.class);
        when(builder.construct(any(PathBuilder.class), eq(Dto.class))).thenReturn(dtoExpr);
        when(builder.construct(any(PathBuilder.class), eq(ChildDto.class))).thenReturn(childExpr);

        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        ProjectionMetadata<ChildDto> childMeta = new Meta<>(ChildDto.class, List.of());
        ProjectionMetadata<Dto> meta = new Meta<>(Dto.class, List.of(
                new Attr("children", childMeta, true,
                        (obj, list) -> ((Dto) obj).children = (List<ChildDto>) list)
        ));
        when(registry.getMetadata(Dto.class)).thenReturn(meta);
        when(registry.getMetadata(ChildDto.class)).thenReturn(childMeta);

        Metamodel metamodel = mock(Metamodel.class);
        EntityType<Entity> entityType = mock(EntityType.class);
        Type idType = mock(Type.class);
        SingularAttribute idAttr = mock(SingularAttribute.class);
        when(metamodel.entity(Entity.class)).thenReturn(entityType);
        when(entityType.getIdType()).thenReturn(idType);
        when(idType.getJavaType()).thenReturn(Long.class);
        when(entityType.getId(Long.class)).thenReturn(idAttr);
        when(idAttr.getName()).thenReturn("id");

        QuerydslProjectionExecutor executor = new QuerydslProjectionExecutor(factory, metamodel, builder, registry);
        ProjectionQuery<Entity> query = mock(ProjectionQuery.class);
        when(query.asPredicate()).thenReturn(Optional.empty());
        when(query.pageable()).thenReturn(Pageable.unpaged());

        ProjectionResult<Dto> result = executor.project(Entity.class, Dto.class, query);
        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements()); // <- changed
        assertEquals(1, result.content().get(0).children.size());
        assertSame(child, result.content().get(0).children.get(0));
        verify(factory).from(any(PathBuilder.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void projectAppliesDescendingSort() {
        JPAQueryFactory factory = mock(JPAQueryFactory.class);
        JPAQuery<Tuple> main = mock(JPAQuery.class, RETURNS_SELF);
        JPAQuery<Long> count = mock(JPAQuery.class, RETURNS_SELF);
        when(factory.select(any(Expression.class), any(Expression.class))).thenReturn(main);
        when(factory.select(Mockito.<Expression<Long>>any())).thenReturn(count);
        when(main.from(any(PathBuilder.class))).thenReturn(main);
        when(count.from(any(PathBuilder.class))).thenReturn(count);

        Tuple tuple = mock(Tuple.class);
        when(tuple.get(any(Expression.class))).thenReturn(1L, new Dto());
        when(main.fetch()).thenReturn(List.of(tuple));
        when(count.fetchOne()).thenReturn(1L);

        QuerydslProjectionBuilder builder = mock(QuerydslProjectionBuilder.class);
        Expression<Dto> dtoExpr = mock(Expression.class);
        when(builder.construct(any(PathBuilder.class), eq(Dto.class))).thenReturn(dtoExpr);

        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        Metamodel metamodel = mock(Metamodel.class);
        EntityType<Entity> entityType = mock(EntityType.class);
        Type idType = mock(Type.class);
        SingularAttribute idAttr = mock(SingularAttribute.class);
        when(metamodel.entity(Entity.class)).thenReturn(entityType);
        when(entityType.getIdType()).thenReturn(idType);
        when(idType.getJavaType()).thenReturn(Long.class);
        when(entityType.getId(Long.class)).thenReturn(idAttr);
        when(idAttr.getName()).thenReturn("id");

        QuerydslProjectionExecutor executor = new QuerydslProjectionExecutor(factory, metamodel, builder, registry);
        ProjectionQuery<Entity> query = mock(ProjectionQuery.class);
        when(query.asPredicate()).thenReturn(Optional.empty());
        when(query.pageable()).thenReturn(PageRequest.of(0,1, Sort.by(Sort.Order.desc("name"))));

        executor.project(Entity.class, Dto.class, query);

        ArgumentCaptor<OrderSpecifier> captor = ArgumentCaptor.forClass(OrderSpecifier.class);
        verify(main).orderBy(captor.capture());
        assertEquals(Order.DESC, captor.getValue().getOrder());
    }

    private static Class<?> invokeBoxIfPrimitive(Class<?> input) throws Exception {
        Method m = QuerydslProjectionExecutor.class.getDeclaredMethod("boxIfPrimitive", Class.class);
        m.setAccessible(true);

        Object target = null;
        if (!Modifier.isStatic(m.getModifiers())) {
            // If the helper isn't static in your code, create a minimal instance
            target = new QuerydslProjectionExecutor(
                    Mockito.mock(JPAQueryFactory.class),
                    Mockito.mock(Metamodel.class),
                    Mockito.mock(QuerydslProjectionBuilder.class),
                    Mockito.mock(ProjectionMetadataRegistry.class)
            );
        }

        return (Class<?>) m.invoke(target, new Object[]{ input });
    }

    @Test
    void boxesAllJavaPrimitives() throws Exception {
        assertSame(Integer.class, invokeBoxIfPrimitive(int.class));
        assertSame(Long.class, invokeBoxIfPrimitive(long.class));
        assertSame(Double.class, invokeBoxIfPrimitive(double.class));
        assertSame(Float.class, invokeBoxIfPrimitive(float.class));
        assertSame(Boolean.class, invokeBoxIfPrimitive(boolean.class));
        assertSame(Character.class, invokeBoxIfPrimitive(char.class));
        assertSame(Short.class, invokeBoxIfPrimitive(short.class));
        assertSame(Byte.class, invokeBoxIfPrimitive(byte.class));
    }

    @Test
    void leavesReferenceTypesUnchanged() throws Exception {
        assertSame(String.class, invokeBoxIfPrimitive(String.class));
        assertSame(Object.class, invokeBoxIfPrimitive(Object.class));
        assertSame(java.math.BigDecimal.class, invokeBoxIfPrimitive(java.math.BigDecimal.class));
    }

    @Test
    void returnsObjectClassWhenNullInput() throws Exception {
        // If your implementation returns something else for null, adjust this expectation.
        assertSame(Object.class, invokeBoxIfPrimitive(null));
    }
}

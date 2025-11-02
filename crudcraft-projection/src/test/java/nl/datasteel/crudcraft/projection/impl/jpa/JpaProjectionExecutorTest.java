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

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.Type;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadata;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.projection.api.ProjectionResult;
import nl.datasteel.crudcraft.projection.impl.CollectionHydrator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaProjectionExecutorTest {

    static class Entity { Long id; String name; }
    static class Dto { }

    // Helper: stub the JPA Metamodel so getIdPath(...) can resolve the id attribute
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void stubMetamodelForId(EntityManager em, Class<?> entityClass, String idName, Class<?> idTypeClass) {
        Metamodel mm = mock(Metamodel.class);
        when(em.getMetamodel()).thenReturn(mm);

        EntityType et = mock(EntityType.class);
        when(mm.entity(entityClass)).thenReturn(et);

        Type idType = mock(Type.class);
        when(et.getIdType()).thenReturn(idType);
        when(idType.getJavaType()).thenReturn(idTypeClass);

        SingularAttribute idAttr = mock(SingularAttribute.class);
        when(et.getId(idTypeClass)).thenReturn(idAttr);
        when(idAttr.getName()).thenReturn(idName);
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    @Test
    void projectAppliesSpecificationSortingAndPaging() {
        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Object[]> cq = mock(CriteriaQuery.class);
        Root<Entity> root = mock(Root.class);
        Selection dtoSelection = mock(Selection.class);
        Path<Object> idPath = mock(Path.class);
        TypedQuery<Object[]> typed = mock(TypedQuery.class);

        // metamodel -> id name "id"
        stubMetamodelForId(em, Entity.class, "id", Long.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Object[].class)).thenReturn(cq);
        when(cq.from(Entity.class)).thenReturn(root);

        // Avoid generic capture issues on getJavaType()
        doReturn((Class) Entity.class).when(root).getJavaType();

        CriteriaProjectionBuilder builder = mock(CriteriaProjectionBuilder.class);
        when(builder.construct(cb, root, Dto.class)).thenReturn(dtoSelection);

        when(root.get("id")).thenReturn(idPath);
        when(cq.multiselect(idPath, dtoSelection)).thenReturn(cq);
        when(cq.distinct(true)).thenReturn(cq);

        Specification<Entity> spec = mock(Specification.class);
        Predicate pred = mock(Predicate.class);
        when(spec.toPredicate(root, cq, cb)).thenReturn(pred);
        ProjectionQuery<Entity> query = mock(ProjectionQuery.class);
        when(query.asSpecification()).thenReturn(Optional.of(spec));
        Pageable pageable = PageRequest.of(1, 2, Sort.by("name"));
        when(query.pageable()).thenReturn(pageable);

        Path<Object> namePath = mock(Path.class);
        when(root.get("name")).thenReturn(namePath);
        jakarta.persistence.criteria.Order order = mock(jakarta.persistence.criteria.Order.class);
        when(cb.asc(namePath)).thenReturn(order);

        when(em.createQuery(cq)).thenReturn(typed);
        when(typed.getResultList()).thenReturn(List.<Object[]>of(new Object[]{1L, new Dto()}));

        // count query
        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<Entity> countRoot = mock(Root.class);
        doReturn((Class) Entity.class).when(countRoot).getJavaType();
        TypedQuery<Long> countTyped = mock(TypedQuery.class);
        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(countQuery.from(Entity.class)).thenReturn(countRoot);
        jakarta.persistence.criteria.Expression<Long> countExpr = mock(jakarta.persistence.criteria.Expression.class);
        when(cb.count(countRoot)).thenReturn(countExpr);
        when(countQuery.select(countExpr)).thenReturn(countQuery);
        when(em.createQuery(countQuery)).thenReturn(countTyped);
        when(countTyped.getSingleResult()).thenReturn(1L);

        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        ProjectionMetadata metadata = mock(ProjectionMetadata.class);
        when(registry.getMetadata(Dto.class)).thenReturn(metadata);

        try (MockedStatic<CollectionHydrator> hydrator = mockStatic(CollectionHydrator.class)) {
            JpaProjectionExecutor executor = new JpaProjectionExecutor(em, builder, registry);
            ProjectionResult<Dto> result = executor.project(Entity.class, Dto.class, query);

            assertEquals(1, result.content().size());
            assertEquals(1, result.totalElements());

            verify(cq).where(pred);
            ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
            verify(cq).orderBy(captor.capture());
            List<?> captured = captor.getValue();
            assertEquals(1, captured.size());
            assertSame(order, captured.getFirst());
            verify(typed).setFirstResult(2);
            verify(typed).setMaxResults(2);
            hydrator.verify(() -> CollectionHydrator.hydrateCollections(eq(Entity.class), eq(metadata), anyMap(), any()));
        }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    @Test
    void projectSkipsHydrationWhenNoMetadata() {
        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Object[]> cq = mock(CriteriaQuery.class);
        Root<Entity> root = mock(Root.class);
        Selection dtoSelection = mock(Selection.class);
        Path<Object> idPath = mock(Path.class);
        TypedQuery<Object[]> typed = mock(TypedQuery.class);

        stubMetamodelForId(em, Entity.class, "id", Long.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Object[].class)).thenReturn(cq);
        when(cq.from(Entity.class)).thenReturn(root);
        doReturn((Class) Entity.class).when(root).getJavaType();

        CriteriaProjectionBuilder builder = mock(CriteriaProjectionBuilder.class);
        when(builder.construct(cb, root, Dto.class)).thenReturn(dtoSelection);
        when(root.get("id")).thenReturn(idPath);
        when(cq.multiselect(idPath, dtoSelection)).thenReturn(cq);
        when(cq.distinct(true)).thenReturn(cq);

        ProjectionQuery<Entity> query = mock(ProjectionQuery.class);
        when(query.asSpecification()).thenReturn(Optional.empty());
        when(query.pageable()).thenReturn(Pageable.unpaged());

        when(em.createQuery(cq)).thenReturn(typed);
        when(typed.getResultList()).thenReturn(List.<Object[]>of(new Object[]{1L, new Dto()}));

        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<Entity> countRoot = mock(Root.class);
        doReturn((Class) Entity.class).when(countRoot).getJavaType();
        TypedQuery<Long> countTyped = mock(TypedQuery.class);
        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(countQuery.from(Entity.class)).thenReturn(countRoot);
        jakarta.persistence.criteria.Expression<Long> countExpr = mock(jakarta.persistence.criteria.Expression.class);
        when(cb.count(countRoot)).thenReturn(countExpr);
        when(countQuery.select(countExpr)).thenReturn(countQuery);
        when(em.createQuery(countQuery)).thenReturn(countTyped);
        when(countTyped.getSingleResult()).thenReturn(1L);

        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        when(registry.getMetadata(Dto.class)).thenReturn(null);

        try (MockedStatic<CollectionHydrator> hydrator = mockStatic(CollectionHydrator.class)) {
            JpaProjectionExecutor executor = new JpaProjectionExecutor(em, builder, registry);
            ProjectionResult<Dto> result = executor.project(Entity.class, Dto.class, query);
            assertEquals(1, result.content().size());
            hydrator.verifyNoInteractions();
        }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    @Test
    void projectIgnoresNullPredicateAndPageable() {
        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Object[]> cq = mock(CriteriaQuery.class);
        Root<Entity> root = mock(Root.class);
        Selection dtoSelection = mock(Selection.class);
        Path<Object> idPath = mock(Path.class);
        TypedQuery<Object[]> typed = mock(TypedQuery.class);

        stubMetamodelForId(em, Entity.class, "id", Long.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Object[].class)).thenReturn(cq);
        when(cq.from(Entity.class)).thenReturn(root);
        doReturn((Class) Entity.class).when(root).getJavaType();

        CriteriaProjectionBuilder builder = mock(CriteriaProjectionBuilder.class);
        when(builder.construct(cb, root, Dto.class)).thenReturn(dtoSelection);
        when(root.get("id")).thenReturn(idPath);
        when(cq.multiselect(idPath, dtoSelection)).thenReturn(cq);
        when(cq.distinct(true)).thenReturn(cq);

        Specification<Entity> spec = mock(Specification.class);
        when(spec.toPredicate(root, cq, cb)).thenReturn(null);
        ProjectionQuery<Entity> query = mock(ProjectionQuery.class);
        when(query.asSpecification()).thenReturn(Optional.of(spec));
        when(query.pageable()).thenReturn(null);

        when(em.createQuery(cq)).thenReturn(typed);
        when(typed.getResultList()).thenReturn(List.<Object[]>of(new Object[]{1L, new Dto()}));

        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<Entity> countRoot = mock(Root.class);
        doReturn((Class) Entity.class).when(countRoot).getJavaType();
        TypedQuery<Long> countTyped = mock(TypedQuery.class);
        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(countQuery.from(Entity.class)).thenReturn(countRoot);
        jakarta.persistence.criteria.Expression<Long> countExpr = mock(jakarta.persistence.criteria.Expression.class);
        when(cb.count(countRoot)).thenReturn(countExpr);
        when(countQuery.select(countExpr)).thenReturn(countQuery);
        when(em.createQuery(countQuery)).thenReturn(countTyped);
        when(countTyped.getSingleResult()).thenReturn(1L);

        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        when(registry.getMetadata(Dto.class)).thenReturn(null);

        JpaProjectionExecutor executor = new JpaProjectionExecutor(em, builder, registry);
        executor.project(Entity.class, Dto.class, query);

        // Disambiguate overloaded methods in verify
        verify(cq, never()).where((Predicate) any());
        verify(cq, never()).orderBy(anyList());
        verify(typed, never()).setFirstResult(anyInt());
        verify(typed, never()).setMaxResults(anyInt());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void projectSortsByNestedPropertyDescending() {
        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Object[]> cq = mock(CriteriaQuery.class);
        Root<Entity> root = mock(Root.class);
        Selection dtoSelection = mock(Selection.class);
        Path<Object> idPath = mock(Path.class);
        TypedQuery<Object[]> typed = mock(TypedQuery.class);

        stubMetamodelForId(em, Entity.class, "id", Long.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Object[].class)).thenReturn(cq);
        when(cq.from(Entity.class)).thenReturn(root);
        doReturn((Class) Entity.class).when(root).getJavaType();

        CriteriaProjectionBuilder builder = mock(CriteriaProjectionBuilder.class);
        when(builder.construct(cb, root, Dto.class)).thenReturn(dtoSelection);
        when(root.get("id")).thenReturn(idPath);
        when(cq.multiselect(idPath, dtoSelection)).thenReturn(cq);
        when(cq.distinct(true)).thenReturn(cq);
        when(cq.orderBy(anyList())).thenReturn(cq);

        Path<Object> childPath = mock(Path.class);
        Path<Object> namePath = mock(Path.class);
        when(root.get("child")).thenReturn(childPath);
        when(childPath.get("name")).thenReturn(namePath);
        jakarta.persistence.criteria.Order order = mock(jakarta.persistence.criteria.Order.class);
        when(cb.desc(namePath)).thenReturn(order);

        ProjectionQuery<Entity> query = mock(ProjectionQuery.class);
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Order.desc("child.name")));
        when(query.asSpecification()).thenReturn(Optional.empty());
        when(query.pageable()).thenReturn(pageable);

        when(em.createQuery(cq)).thenReturn(typed);
        when(typed.getResultList()).thenReturn(List.<Object[]>of(new Object[]{1L, new Dto()}));

        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<Entity> countRoot = mock(Root.class);
        doReturn((Class) Entity.class).when(countRoot).getJavaType();
        TypedQuery<Long> countTyped = mock(TypedQuery.class);
        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(countQuery.from(Entity.class)).thenReturn(countRoot);
        jakarta.persistence.criteria.Expression<Long> countExpr = mock(jakarta.persistence.criteria.Expression.class);
        when(cb.count(countRoot)).thenReturn(countExpr);
        when(countQuery.select(countExpr)).thenReturn(countQuery);
        when(em.createQuery(countQuery)).thenReturn(countTyped);
        when(countTyped.getSingleResult()).thenReturn(1L);

        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        when(registry.getMetadata(Dto.class)).thenReturn(null);

        JpaProjectionExecutor executor = new JpaProjectionExecutor(em, builder, registry);
        executor.project(Entity.class, Dto.class, query);

        verify(root).get("child");
        verify(childPath).get("name");
        verify(cb).desc(namePath);
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(cq).orderBy(captor.capture());
        assertEquals(order, captor.getValue().get(0));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void projectSkipsHydrationWhenNoRows() {
        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Object[]> cq = mock(CriteriaQuery.class);
        Root<Entity> root = mock(Root.class);
        Selection dtoSelection = mock(Selection.class);
        Path<Object> idPath = mock(Path.class);
        TypedQuery<Object[]> typed = mock(TypedQuery.class);

        stubMetamodelForId(em, Entity.class, "id", Long.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Object[].class)).thenReturn(cq);
        when(cq.from(Entity.class)).thenReturn(root);
        doReturn((Class) Entity.class).when(root).getJavaType();

        CriteriaProjectionBuilder builder = mock(CriteriaProjectionBuilder.class);
        when(builder.construct(cb, root, Dto.class)).thenReturn(dtoSelection);
        when(root.get("id")).thenReturn(idPath);
        when(cq.multiselect(idPath, dtoSelection)).thenReturn(cq);
        when(cq.distinct(true)).thenReturn(cq);

        ProjectionQuery<Entity> query = mock(ProjectionQuery.class);
        when(query.asSpecification()).thenReturn(Optional.empty());
        when(query.pageable()).thenReturn(Pageable.unpaged());

        when(em.createQuery(cq)).thenReturn(typed);
        when(typed.getResultList()).thenReturn(List.<Object[]>of());

        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<Entity> countRoot = mock(Root.class);
        doReturn((Class) Entity.class).when(countRoot).getJavaType();
        TypedQuery<Long> countTyped = mock(TypedQuery.class);
        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(countQuery.from(Entity.class)).thenReturn(countRoot);
        jakarta.persistence.criteria.Expression<Long> countExpr = mock(jakarta.persistence.criteria.Expression.class);
        when(cb.count(countRoot)).thenReturn(countExpr);
        when(countQuery.select(countExpr)).thenReturn(countQuery);
        when(em.createQuery(countQuery)).thenReturn(countTyped);
        when(countTyped.getSingleResult()).thenReturn(0L);

        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        ProjectionMetadata metadata = mock(ProjectionMetadata.class);
        when(registry.getMetadata(Dto.class)).thenReturn(metadata);

        try (MockedStatic<CollectionHydrator> hydrator = mockStatic(CollectionHydrator.class)) {
            JpaProjectionExecutor executor = new JpaProjectionExecutor(em, builder, registry);
            ProjectionResult<Dto> result = executor.project(Entity.class, Dto.class, query);
            assertTrue(result.content().isEmpty());
            hydrator.verifyNoInteractions();
        }
    }
}

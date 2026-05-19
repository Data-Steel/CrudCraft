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

package nl.datasteel.crudcraft.runtime.export;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import nl.datasteel.crudcraft.runtime.metadata.EntityFieldMetadata;
import nl.datasteel.crudcraft.runtime.metadata.EntityMetadata;
import nl.datasteel.crudcraft.runtime.metadata.EntityMetadataRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class EntityExportServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void fetchWithRelationshipsBuildsQueryAndLoadsCollections() throws Exception {
        EntityManager entityManager = mock(EntityManager.class);
        EntityMetadataRegistry metadataRegistry = mock(EntityMetadataRegistry.class);
        EntityExportService service = new EntityExportService(entityManager, metadataRegistry);
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setIncludeFields(Set.of("owner", "children"));

        EntityMetadata metadata = createMetadata();
        when(metadataRegistry.getMetadata(DemoEntity.class)).thenReturn(metadata);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<DemoEntity> query = mock(CriteriaQuery.class);
        Root<DemoEntity> root = mock(Root.class);
        TypedQuery<DemoEntity> typedQuery = mock(TypedQuery.class);

        CriteriaQuery<DemoEntity> collectionQuery = mock(CriteriaQuery.class);
        Root<DemoEntity> collectionRoot = mock(Root.class);
        Predicate inPredicate = mock(Predicate.class);
        TypedQuery<DemoEntity> collectionTypedQuery = mock(TypedQuery.class);

        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<DemoEntity> countRoot = mock(Root.class);
        Expression<Long> countExpression = mock(Expression.class);
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(DemoEntity.class)).thenReturn(query).thenReturn(collectionQuery);
        when(cb.createQuery(Long.class)).thenReturn(countQuery);

        when(query.from(DemoEntity.class)).thenReturn(root);
        when(query.select(root)).thenReturn(query);
        when(query.distinct(true)).thenReturn(query);
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(10)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(new DemoEntity()));

        when(collectionQuery.from(DemoEntity.class)).thenReturn(collectionRoot);
        when(collectionRoot.in(any(List.class))).thenReturn(inPredicate);
        when(collectionQuery.where(inPredicate)).thenReturn(collectionQuery);
        when(collectionQuery.select(collectionRoot)).thenReturn(collectionQuery);
        when(entityManager.createQuery(collectionQuery)).thenReturn(collectionTypedQuery);
        when(collectionTypedQuery.getResultList()).thenReturn(List.of(new DemoEntity()));

        when(countQuery.from(DemoEntity.class)).thenReturn(countRoot);
        when(cb.count(countRoot)).thenReturn(countExpression);
        when(countQuery.select(countExpression)).thenReturn(countQuery);
        when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(25L);

        Page<DemoEntity> result =
                service.fetchWithRelationships(
                        DemoEntity.class, exportRequest, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals(25L, result.getTotalElements());
        verify(root, times(1)).fetch(eq("owner"), eq(JoinType.LEFT));
        verify(collectionRoot, times(1)).fetch(eq("children"), eq(JoinType.LEFT));
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchWithRelationshipsContinuesWhenFetchOrBatchFails() throws Exception {
        EntityManager entityManager = mock(EntityManager.class);
        EntityMetadataRegistry metadataRegistry = mock(EntityMetadataRegistry.class);
        EntityExportService service = new EntityExportService(entityManager, metadataRegistry);
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setIncludeFields(Set.of("owner", "children"));

        EntityMetadata metadata = createMetadata();
        when(metadataRegistry.getMetadata(DemoEntity.class)).thenReturn(metadata);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<DemoEntity> query = mock(CriteriaQuery.class);
        Root<DemoEntity> root = mock(Root.class);
        TypedQuery<DemoEntity> typedQuery = mock(TypedQuery.class);

        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<DemoEntity> countRoot = mock(Root.class);
        Expression<Long> countExpression = mock(Expression.class);
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(DemoEntity.class))
                .thenReturn(query)
                .thenThrow(new PersistenceException("batch fail"));
        when(cb.createQuery(Long.class)).thenReturn(countQuery);

        when(query.from(DemoEntity.class)).thenReturn(root);
        when(query.select(root)).thenReturn(query);
        when(query.distinct(true)).thenReturn(query);
        doThrow(new IllegalArgumentException("cannot fetch"))
                .when(root)
                .fetch(eq("owner"), eq(JoinType.LEFT));
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(5)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(new DemoEntity()));

        when(countQuery.from(DemoEntity.class)).thenReturn(countRoot);
        when(cb.count(countRoot)).thenReturn(countExpression);
        when(countQuery.select(countExpression)).thenReturn(countQuery);
        when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(1L);

        Page<DemoEntity> result =
                service.fetchWithRelationships(
                        DemoEntity.class, exportRequest, PageRequest.of(0, 5));

        assertEquals(1L, result.getTotalElements());
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchWithRelationshipsSupportsDescendantIncludeAndEmptyResults() throws Exception {
        EntityManager entityManager = mock(EntityManager.class);
        EntityMetadataRegistry metadataRegistry = mock(EntityMetadataRegistry.class);
        EntityExportService service = new EntityExportService(entityManager, metadataRegistry);
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setIncludeFields(Set.of("owner.id", "oneToOne"));

        EntityMetadata metadata = createMetadata();
        when(metadataRegistry.getMetadata(DemoEntity.class)).thenReturn(metadata);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<DemoEntity> query = mock(CriteriaQuery.class);
        Root<DemoEntity> root = mock(Root.class);
        TypedQuery<DemoEntity> typedQuery = mock(TypedQuery.class);

        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<DemoEntity> countRoot = mock(Root.class);
        Expression<Long> countExpression = mock(Expression.class);
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(DemoEntity.class)).thenReturn(query);
        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(query.from(DemoEntity.class)).thenReturn(root);
        when(query.select(root)).thenReturn(query);
        when(query.distinct(true)).thenReturn(query);
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(5)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        when(countQuery.from(DemoEntity.class)).thenReturn(countRoot);
        when(cb.count(countRoot)).thenReturn(countExpression);
        when(countQuery.select(countExpression)).thenReturn(countQuery);
        when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(0L);

        Page<DemoEntity> result =
                service.fetchWithRelationships(
                        DemoEntity.class, exportRequest, PageRequest.of(0, 5));

        assertEquals(0, result.getContent().size());
        verify(root, times(1)).fetch(eq("owner"), eq(JoinType.LEFT));
        verify(root, times(1)).fetch(eq("oneToOne"), eq(JoinType.LEFT));
        verify(root, times(0)).fetch(eq("children"), eq(JoinType.LEFT));
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchWithRelationshipsSkipsBatchLoadingWhenCollectionIsNotRequested() throws Exception {
        EntityManager entityManager = mock(EntityManager.class);
        EntityMetadataRegistry metadataRegistry = mock(EntityMetadataRegistry.class);
        EntityExportService service = new EntityExportService(entityManager, metadataRegistry);
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setIncludeFields(Set.of("owner"));

        EntityMetadata metadata = createMetadata();
        when(metadataRegistry.getMetadata(DemoEntity.class)).thenReturn(metadata);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<DemoEntity> query = mock(CriteriaQuery.class);
        Root<DemoEntity> root = mock(Root.class);
        TypedQuery<DemoEntity> typedQuery = mock(TypedQuery.class);

        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<DemoEntity> countRoot = mock(Root.class);
        Expression<Long> countExpression = mock(Expression.class);
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(DemoEntity.class)).thenReturn(query);
        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(query.from(DemoEntity.class)).thenReturn(root);
        when(query.select(root)).thenReturn(query);
        when(query.distinct(true)).thenReturn(query);
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(5)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(new DemoEntity()));

        when(countQuery.from(DemoEntity.class)).thenReturn(countRoot);
        when(cb.count(countRoot)).thenReturn(countExpression);
        when(countQuery.select(countExpression)).thenReturn(countQuery);
        when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(1L);

        Page<DemoEntity> result =
                service.fetchWithRelationships(
                        DemoEntity.class, exportRequest, PageRequest.of(0, 5));

        assertEquals(1, result.getContent().size());
        verify(root, times(1)).fetch(eq("owner"), eq(JoinType.LEFT));
        verify(root, never()).fetch(eq("children"), eq(JoinType.LEFT));
        verify(cb, times(1)).createQuery(DemoEntity.class);
    }

    @Test
    void fetchWithRelationshipsRejectsExplicitRelationshipWhenMaxDepthIsZero() throws Exception {
        EntityManager entityManager = mock(EntityManager.class);
        EntityMetadataRegistry metadataRegistry = mock(EntityMetadataRegistry.class);
        EntityExportService service = new EntityExportService(entityManager, metadataRegistry);
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setMaxDepth(0);
        exportRequest.setIncludeFields(Set.of("owner"));

        when(metadataRegistry.getMetadata(DemoEntity.class)).thenReturn(createMetadata());

        assertThrows(
                ExportDepthExceededException.class,
                () ->
                        service.fetchWithRelationships(
                                DemoEntity.class, exportRequest, PageRequest.of(0, 5)));
        verify(entityManager, never()).getCriteriaBuilder();
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchWithRelationshipsSkipsImplicitRelationshipsWhenMaxDepthIsZero() throws Exception {
        EntityManager entityManager = mock(EntityManager.class);
        EntityMetadataRegistry metadataRegistry = mock(EntityMetadataRegistry.class);
        EntityExportService service = new EntityExportService(entityManager, metadataRegistry);
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setMaxDepth(0);

        EntityMetadata metadata = createMetadata();
        when(metadataRegistry.getMetadata(DemoEntity.class)).thenReturn(metadata);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<DemoEntity> query = mock(CriteriaQuery.class);
        Root<DemoEntity> root = mock(Root.class);
        TypedQuery<DemoEntity> typedQuery = mock(TypedQuery.class);
        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<DemoEntity> countRoot = mock(Root.class);
        Expression<Long> countExpression = mock(Expression.class);
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(DemoEntity.class)).thenReturn(query);
        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(query.from(DemoEntity.class)).thenReturn(root);
        when(query.select(root)).thenReturn(query);
        when(query.distinct(true)).thenReturn(query);
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(5)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(new DemoEntity()));
        when(countQuery.from(DemoEntity.class)).thenReturn(countRoot);
        when(cb.count(countRoot)).thenReturn(countExpression);
        when(countQuery.select(countExpression)).thenReturn(countQuery);
        when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(1L);

        Page<DemoEntity> result =
                service.fetchWithRelationships(
                        DemoEntity.class, exportRequest, PageRequest.of(0, 5));

        assertEquals(1, result.getContent().size());
        verify(root, never()).fetch(eq("owner"), eq(JoinType.LEFT));
        verify(root, never()).fetch(eq("oneToOne"), eq(JoinType.LEFT));
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchWithRelationshipsBatchLoadsCollectionWhenOnlyDescendantsAreRequested()
            throws Exception {
        EntityManager entityManager = mock(EntityManager.class);
        EntityMetadataRegistry metadataRegistry = mock(EntityMetadataRegistry.class);
        EntityExportService service = new EntityExportService(entityManager, metadataRegistry);
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setIncludeFields(Set.of("children.id"));

        EntityMetadata metadata = createMetadata();
        when(metadataRegistry.getMetadata(DemoEntity.class)).thenReturn(metadata);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<DemoEntity> query = mock(CriteriaQuery.class);
        Root<DemoEntity> root = mock(Root.class);
        TypedQuery<DemoEntity> typedQuery = mock(TypedQuery.class);

        CriteriaQuery<DemoEntity> collectionQuery = mock(CriteriaQuery.class);
        Root<DemoEntity> collectionRoot = mock(Root.class);
        Predicate inPredicate = mock(Predicate.class);
        TypedQuery<DemoEntity> collectionTypedQuery = mock(TypedQuery.class);

        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<DemoEntity> countRoot = mock(Root.class);
        Expression<Long> countExpression = mock(Expression.class);
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(DemoEntity.class)).thenReturn(query).thenReturn(collectionQuery);
        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(query.from(DemoEntity.class)).thenReturn(root);
        when(query.select(root)).thenReturn(query);
        when(query.distinct(true)).thenReturn(query);
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(5)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(new DemoEntity()));

        when(collectionQuery.from(DemoEntity.class)).thenReturn(collectionRoot);
        when(collectionRoot.in(any(List.class))).thenReturn(inPredicate);
        when(collectionQuery.where(inPredicate)).thenReturn(collectionQuery);
        when(collectionQuery.select(collectionRoot)).thenReturn(collectionQuery);
        when(entityManager.createQuery(collectionQuery)).thenReturn(collectionTypedQuery);
        when(collectionTypedQuery.getResultList()).thenReturn(List.of(new DemoEntity()));

        when(countQuery.from(DemoEntity.class)).thenReturn(countRoot);
        when(cb.count(countRoot)).thenReturn(countExpression);
        when(countQuery.select(countExpression)).thenReturn(countQuery);
        when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(1L);

        Page<DemoEntity> result =
                service.fetchWithRelationships(
                        DemoEntity.class, exportRequest, PageRequest.of(0, 5));

        assertEquals(1, result.getContent().size());
        verify(collectionRoot, times(1)).fetch(eq("children"), eq(JoinType.LEFT));
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchWithRelationshipsAppliesSpecificationToSelectAndCountQueries() throws Exception {
        EntityManager entityManager = mock(EntityManager.class);
        EntityMetadataRegistry metadataRegistry = mock(EntityMetadataRegistry.class);
        EntityExportService service = new EntityExportService(entityManager, metadataRegistry);
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setIncludeFields(Set.of("owner"));

        EntityMetadata metadata = createMetadata();
        when(metadataRegistry.getMetadata(DemoEntity.class)).thenReturn(metadata);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<DemoEntity> query = mock(CriteriaQuery.class);
        Root<DemoEntity> root = mock(Root.class);
        TypedQuery<DemoEntity> typedQuery = mock(TypedQuery.class);
        Predicate selectPredicate = mock(Predicate.class);

        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        Root<DemoEntity> countRoot = mock(Root.class);
        Expression<Long> countExpression = mock(Expression.class);
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);
        Predicate countPredicate = mock(Predicate.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(DemoEntity.class)).thenReturn(query);
        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(query.from(DemoEntity.class)).thenReturn(root);
        when(query.select(root)).thenReturn(query);
        when(query.distinct(true)).thenReturn(query);
        when(query.where(selectPredicate)).thenReturn(query);
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(5)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(new DemoEntity()));

        when(countQuery.from(DemoEntity.class)).thenReturn(countRoot);
        when(cb.count(countRoot)).thenReturn(countExpression);
        when(countQuery.select(countExpression)).thenReturn(countQuery);
        when(countQuery.where(countPredicate)).thenReturn(countQuery);
        when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(1L);

        Specification<DemoEntity> specification = mock(Specification.class);
        when(specification.toPredicate(root, query, cb)).thenReturn(selectPredicate);
        when(specification.toPredicate(countRoot, countQuery, cb)).thenReturn(countPredicate);

        Page<DemoEntity> result =
                service.fetchWithRelationships(
                        DemoEntity.class, exportRequest, PageRequest.of(0, 5), specification);

        assertEquals(1, result.getContent().size());
        verify(query, times(1)).where(selectPredicate);
        verify(countQuery, times(1)).where(countPredicate);
    }

    private static EntityMetadata createMetadata() throws NoSuchFieldException {
        Field ownerField = DemoEntity.class.getDeclaredField("owner");
        Field childrenField = DemoEntity.class.getDeclaredField("children");
        Field oneToOneField = DemoEntity.class.getDeclaredField("oneToOne");
        Field ignoredField = DemoEntity.class.getDeclaredField("ignored");

        EntityFieldMetadata owner =
                new EntityFieldMetadata(
                        "owner",
                        ownerField,
                        EntityFieldMetadata.FieldType.MANY_TO_ONE,
                        OwnerEntity.class,
                        true);
        EntityFieldMetadata children =
                new EntityFieldMetadata(
                        "children",
                        childrenField,
                        EntityFieldMetadata.FieldType.ONE_TO_MANY,
                        OwnerEntity.class,
                        true);
        EntityFieldMetadata oneToOne =
                new EntityFieldMetadata(
                        "oneToOne",
                        oneToOneField,
                        EntityFieldMetadata.FieldType.ONE_TO_ONE,
                        OwnerEntity.class,
                        true);
        EntityFieldMetadata ignored =
                new EntityFieldMetadata(
                        "ignored",
                        ignoredField,
                        EntityFieldMetadata.FieldType.SCALAR,
                        String.class,
                        true);
        return new EntityMetadata(DemoEntity.class, List.of(owner, children, oneToOne, ignored));
    }

    private static final class DemoEntity {
        private OwnerEntity owner;
        private List<OwnerEntity> children;
        private OwnerEntity oneToOne;
        private String ignored;
    }

    private static final class OwnerEntity {}
}

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

package nl.datasteel.crudcraft.runtime.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import nl.datasteel.crudcraft.runtime.Identified;
import nl.datasteel.crudcraft.runtime.InternalApi;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import nl.datasteel.crudcraft.runtime.exception.CrudCraftConfigurationException;
import nl.datasteel.crudcraft.runtime.exception.MapperException;
import nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapperCustomizer;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
import nl.datasteel.crudcraft.runtime.service.fixture.CrossPackagePrivateFieldCarrier;
import nl.datasteel.crudcraft.runtime.service.projection.ProjectionAdapter;
import nl.datasteel.crudcraft.runtime.service.strategy.QueryExecutionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SuppressWarnings({"unchecked", "rawtypes"})
class AbstractCrudServiceCrudOperationsTest {

    private JpaRepository<TestEntity, UUID> repository;
    private QueryExecutionStrategy<TestEntity> queryExecutionStrategy;
    private TestService service;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeEach
    void setUp() {
        repository =
                (JpaRepository<TestEntity, UUID>)
                        mock(
                                JpaRepository.class,
                                org.mockito.Mockito.withSettings()
                                        .extraInterfaces(JpaSpecificationExecutor.class));
        queryExecutionStrategy =
                (QueryExecutionStrategy<TestEntity>) mock(QueryExecutionStrategy.class);
        service = new TestService(repository, new TestMapper());
        service.setQueryExecutorForTests(queryExecutionStrategy);
        service.extensions.add(new AppendingExtension());
    }

    @Test
    void entityManagerInjectionIsIdempotentAndResetsCachedIdMetadata() throws Exception {
        EntityManager firstManager = mock(EntityManager.class);
        EntityManager sameManager = mock(EntityManager.class);
        EntityManager differentManager = mock(EntityManager.class);
        Metamodel metamodel = mock(Metamodel.class);
        Metamodel differentMetamodel = mock(Metamodel.class);
        when(firstManager.getMetamodel()).thenReturn(metamodel);
        when(sameManager.getMetamodel()).thenReturn(metamodel);
        when(differentManager.getMetamodel()).thenReturn(differentMetamodel);
        java.lang.reflect.Field idNameField =
                CoreCrudOperations.class.getDeclaredField("idAttributeName");
        idNameField.setAccessible(true);
        idNameField.set(service, "cached");

        service.setEntityManager(firstManager);
        service.setEntityManager(sameManager);

        assertNull(idNameField.get(service));
        assertThrows(NullPointerException.class, () -> service.setEntityManager(null));
        assertThrows(IllegalStateException.class, () -> service.setEntityManager(differentManager));
    }

    @Test
    void findAllAndSearchUseFallbackPaths() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(new TestEntity(UUID.randomUUID(), "value"))));
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class), eq(AltProjection.class)))
                .thenReturn(new PageImpl<>(List.of(new AltProjection("alt"))));

        Page<TestResponse> full = service.findAll(pageable);
        Page<TestRef> ref = service.findAllRef(pageable);
        Page<AltProjection> alt = service.findAll(pageable, AltProjection.class);
        Page<TestResponse> searched = service.search(new SearchRequest(), pageable);
        Page<TestRef> searchedRef = service.searchRef(new SearchRequest(), pageable);
        Page<AltProjection> searchedProjection =
                service.search(new SearchRequest(), pageable, AltProjection.class);

        assertEquals(1, full.getTotalElements());
        assertEquals("value-ar", full.getContent().getFirst().value());
        assertEquals(1, ref.getTotalElements());
        assertEquals("value-ref-ar", ref.getContent().getFirst().value());
        assertEquals(1, alt.getTotalElements());
        assertEquals("alt-ar", alt.getContent().getFirst().value());
        assertEquals(1, searched.getTotalElements());
        assertEquals(1, searchedRef.getTotalElements());
        assertEquals(1, searchedProjection.getTotalElements());
    }

    @Test
    void findByMethodsAndExistencePaths() {
        UUID id = UUID.randomUUID();
        TestEntity entity = new TestEntity(id, "one");
        when(queryExecutionStrategy.findAll(any())).thenReturn(List.of(entity));
        when(queryExecutionStrategy.findOne(any()))
                .thenReturn(Optional.of(entity))
                .thenReturn(Optional.of(entity))
                .thenReturn(Optional.empty());
        when(queryExecutionStrategy.findOne(any(), eq(AltProjection.class)))
                .thenReturn(Optional.empty());
        when(queryExecutionStrategy.exists(any()))
                .thenReturn(true)
                .thenReturn(false)
                .thenReturn(true);
        when(queryExecutionStrategy.count(any())).thenReturn(7L);
        when(repository.getReferenceById(id)).thenReturn(entity);

        assertEquals(1, service.findByIds(List.of(id)).size());
        assertTrue(service.findByIdOptional(id).isPresent());
        assertEquals("one-ar", service.findById(id).value());
        assertThrows(
                ResourceNotFoundException.class, () -> service.findById(id, AltProjection.class));
        assertThrows(ResourceNotFoundException.class, () -> service.findById(UUID.randomUUID()));
        assertEquals(entity, service.findReferenceById(id));
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.findReferenceById(UUID.randomUUID()));
        assertTrue(service.existsById(id));
        assertEquals(7L, service.count());
    }

    @Test
    void mutatingOperationsInvokeHooksAndExtensions() {
        UUID id = UUID.randomUUID();
        TestRequest request = new TestRequest(id, "seed");
        TestEntity existing = new TestEntity(id, "old");
        AtomicReference<Integer> extensionBeforeSaveCount = new AtomicReference<>(0);
        AtomicReference<Integer> extensionBeforeDeleteCount = new AtomicReference<>(0);
        service.extensions.add(
                new CrudRuntimeExtension<>() {
                    @Override
                    public void beforeSave(TestEntity entity) {
                        extensionBeforeSaveCount.updateAndGet(count -> count + 1);
                    }

                    @Override
                    public void beforeDelete(TestEntity entity) {
                        extensionBeforeDeleteCount.updateAndGet(count -> count + 1);
                    }
                });
        when(queryExecutionStrategy.findOne(any())).thenReturn(Optional.of(existing));
        when(queryExecutionStrategy.exists(any())).thenReturn(true).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TestResponse created = service.create(request);
        TestResponse updated = service.update(id, request);
        TestResponse patched = service.patch(id, request);
        TestResponse upsertedUpdate = service.upsert(request);
        TestResponse upsertedCreate = service.upsert(new TestRequest(null, "new"));
        List<TestResponse> createdAll =
                service.createAll(List.of(new TestRequest(null, "a"), new TestRequest(null, "b")));
        List<TestResponse> upsertedAll = service.upsertAll(List.of(request));
        List<TestResponse> updatedAll = service.updateAll(List.of(new Identified<>(id, request)));
        List<TestResponse> patchedAll = service.patchAll(List.of(new Identified<>(id, request)));
        service.delete(id);
        service.deleteAllByIds(List.of(id));

        assertEquals("seed-bc-ar", created.value());
        assertEquals("seed-bu-ar", updated.value());
        assertEquals("seed-bu-patch-ar", patched.value());
        assertEquals("seed-bu-ar", upsertedUpdate.value());
        assertEquals("new-bc-ar", upsertedCreate.value());
        assertEquals(2, createdAll.size());
        assertEquals(1, upsertedAll.size());
        assertEquals(1, updatedAll.size());
        assertEquals(1, patchedAll.size());
        assertNotNull(updatedAll.getFirst());
        assertNotNull(patchedAll.getFirst());
        assertEquals(10, service.preSaveCount);
        assertEquals(10, service.postSaveCount);
        assertEquals(2, service.preDeleteCount);
        assertEquals(2, service.postDeleteCount);
        assertEquals(10, extensionBeforeSaveCount.get());
        assertEquals(2, extensionBeforeDeleteCount.get());
        verify(repository, times(2)).delete(any());
    }

    @Test
    void mapperCustomizerRunsAfterGeneratedMapperWithoutEditingMapper() {
        UUID id = UUID.randomUUID();
        TestService customService = new TestService(repository, new TestMapper());
        customService.setQueryExecutorForTests(queryExecutionStrategy);
        customService.mapperCustomizer =
                new EntityMapperCustomizer<>() {
                    @Override
                    public TestEntity afterFromRequest(TestEntity entity, TestRequest request) {
                        entity.value = entity.value + "-createhook";
                        return entity;
                    }

                    @Override
                    public TestEntity afterUpdate(TestEntity entity, TestRequest request) {
                        entity.value = entity.value + "-updatehook";
                        return entity;
                    }

                    @Override
                    public TestEntity afterPatch(TestEntity entity, TestRequest request) {
                        entity.value = entity.value + "-patchhook";
                        return entity;
                    }

                    @Override
                    public TestResponse afterToResponse(
                            TestResponse response, TestEntity entity) {
                        return new TestResponse(response.id(), response.value() + "-responsehook");
                    }

                    @Override
                    public TestRef afterToRef(TestRef ref, TestEntity entity) {
                        return new TestRef(ref.id(), ref.value() + "-refhook");
                    }
                };
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(queryExecutionStrategy.findOne(any()))
                .thenReturn(Optional.of(new TestEntity(id, "old")))
                .thenReturn(Optional.of(new TestEntity(id, "old")))
                .thenReturn(Optional.of(new TestEntity(id, "read")));
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(new TestEntity(id, "ref"))));

        TestResponse created = customService.create(new TestRequest(id, "seed"));
        TestResponse updated = customService.update(id, new TestRequest(id, "seed"));
        TestResponse patched = customService.patch(id, new TestRequest(id, "seed"));
        TestResponse found = customService.findById(id);
        TestRef ref = customService.findAllRef(PageRequest.of(0, 1)).getContent().getFirst();

        assertEquals("seed-createhook-responsehook", created.value());
        assertEquals("seed-updatehook-responsehook", updated.value());
        assertEquals("seed-patch-patchhook-responsehook", patched.value());
        assertEquals("read-responsehook", found.value());
        assertEquals("ref-ref-refhook", ref.value());
    }

    @Test
    void upsertCreatesWhenGeneratedRequestDtoHasNoReadableIdProperty() {
        TestService generatedDtoService = new TestService(repository, new MissingIdMapper());
        generatedDtoService.setQueryExecutorForTests(queryExecutionStrategy);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TestResponse response = generatedDtoService.upsert(new TestRequest(null, "generated"));

        assertEquals("generated", response.value());
        verify(queryExecutionStrategy, never()).exists(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void constructorRejectsRepositoryWithoutJpaSpecificationExecutor() {
        JpaRepository<TestEntity, UUID> plainRepository =
                (JpaRepository<TestEntity, UUID>) mock(JpaRepository.class);

        CrudCraftConfigurationException thrown =
                assertThrows(
                        CrudCraftConfigurationException.class,
                        () -> new TestService(plainRepository, new TestMapper()));

        assertTrue(thrown.getMessage().contains("JpaSpecificationExecutor"));
    }

    @Test
    void mapperFailuresIncludeOperationContext() {
        TestService failingService = new TestService(repository, new FailingFromRequestMapper());

        MapperException thrown =
                assertThrows(
                        MapperException.class,
                        () -> failingService.create(new TestRequest(null, "broken")));

        assertEquals("create.fromRequest", thrown.getContext().get("operation"));
        assertEquals(TestEntity.class.getName(), thrown.getContext().get("entity"));
        assertEquals(TestRequest.class.getName(), thrown.getContext().get("request"));
        assertTrue(thrown.getMessage().contains("operation=create.fromRequest"));
        assertTrue(thrown.getCause() instanceof IllegalArgumentException);
        verify(repository, never()).save(any());
    }

    @Test
    void mapperFailuresWithNullRequestAreReportedAsNullContext() {
        TestService failingService = new TestService(repository, new FailingFromRequestMapper());

        MapperException thrown = assertThrows(MapperException.class, () -> failingService.create(null));

        assertEquals("null", thrown.getContext().get("request"));
        assertTrue(thrown.getCause() instanceof IllegalArgumentException);
        verify(repository, never()).save(any());
    }

    @Test
    void mapperExceptionFailuresWithNullRequestAreReportedAsNullContext() {
        TestService failingService =
                new TestService(repository, new MapperExceptionThrowingFromRequestMapper());

        MapperException thrown = assertThrows(MapperException.class, () -> failingService.create(null));

        assertEquals("null", thrown.getContext().get("request"));
        assertTrue(thrown.getCause() instanceof MapperException);
        verify(repository, never()).save(any());
    }

    @Test
    void splitServiceBaseClassesAreMarkedInternalApi() {
        assertTrue(CoreCrudOperations.class.isAnnotationPresent(InternalApi.class));
        assertTrue(ProjectionService.class.isAnnotationPresent(InternalApi.class));
        assertTrue(KeysetPaginationService.class.isAnnotationPresent(InternalApi.class));
        assertTrue(KeysetCursorCodec.class.isAnnotationPresent(InternalApi.class));
    }

    @Test
    void keysetValidationRejectsInvalidInputs() {
        assertThrows(
                BadRequestException.class,
                () -> service.findAllKeyset(null, 0, null, Sort.by("value"), TestResponse.class));
        assertThrows(
                BadRequestException.class,
                () -> service.findAllKeyset(null, 1, null, null, TestResponse.class));
        assertThrows(
                BadRequestException.class,
                () -> service.findAllKeyset(null, 1, null, Sort.unsorted(), TestResponse.class));
        assertThrows(
                BadRequestException.class,
                () ->
                        service.findAllKeyset(
                                null,
                                1,
                                null,
                                Sort.by(Sort.Order.asc("value"), Sort.Order.asc("id")),
                                TestResponse.class));
        assertThrows(
                BadRequestException.class,
                () ->
                        service.findAllKeyset(
                                null, 1, "not-a-cursor", Sort.by("value"), TestResponse.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void keysetRejectsUnsupportedSortPropertyWhenEntityMetamodelPresent() {
        Metamodel metamodel = mock(Metamodel.class);
        ManagedType<TestEntity> type = (ManagedType<TestEntity>) mock(ManagedType.class);
        service.metamodel = metamodel;
        when(metamodel.managedType(TestEntity.class)).thenReturn(type);
        when(type.getAttribute("missing")).thenThrow(new IllegalArgumentException("missing"));

        assertThrows(
                BadRequestException.class,
                () -> service.findAllKeyset(null, 1, null, Sort.by("missing"), TestResponse.class));
    }

    @Test
    void keysetRejectsProjectionWhenCursorFieldsAreMissing() {
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class), eq(AltProjection.class)))
                .thenReturn(
                        new PageImpl<>(
                                List.of(
                                        new AltProjection("a"),
                                        new AltProjection("b"),
                                        new AltProjection("c"))));

        assertThrows(
                BadRequestException.class,
                () -> service.findAllKeyset(null, 2, null, Sort.by("value"), AltProjection.class));
        verify(queryExecutionStrategy, times(1))
                .findAll(any(), any(PageRequest.class), eq(AltProjection.class));
        verify(queryExecutionStrategy, times(0)).findAll(any(), any(PageRequest.class));
    }

    @Test
    void keysetWithNoNextPageReturnsNullCursor() {
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class)))
                .thenReturn(
                        new PageImpl<>(
                                List.of(
                                        new TestEntity(UUID.randomUUID(), "a"),
                                        new TestEntity(UUID.randomUUID(), "b"))));

        KeysetPage<TestResponse> page =
                service.findAllKeyset(null, 2, null, Sort.by("value"), null);

        assertEquals(2, page.content().size());
        assertNull(page.nextCursor());
    }

    @Test
    void keysetBuildsEncodedCursorWhenProjectionContainsSortAndId() {
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class)))
                .thenReturn(
                        new PageImpl<>(
                                List.of(
                                        new TestEntity(UUID.randomUUID(), "a"),
                                        new TestEntity(UUID.randomUUID(), "b"),
                                        new TestEntity(UUID.randomUUID(), "c"))));

        KeysetPage<TestResponse> page =
                service.findAllKeyset(null, 2, null, Sort.by("value"), TestResponse.class);

        assertEquals(2, page.content().size());
        assertNotNull(page.nextCursor());
        assertFalse(page.nextCursor().isBlank());
    }

    @Test
    void keysetBuildsCursorFromFieldOnlyProjectionWithoutFallback() {
        final class FieldOnlyProjection {
            private final UUID id;
            private final String value;

            private FieldOnlyProjection(UUID id, String value) {
                this.id = id;
                this.value = value;
            }
        }
        when(queryExecutionStrategy.findAll(
                        any(), any(PageRequest.class), eq(FieldOnlyProjection.class)))
                .thenReturn(
                        new PageImpl<>(
                                List.of(
                                        new FieldOnlyProjection(UUID.randomUUID(), "a"),
                                        new FieldOnlyProjection(UUID.randomUUID(), "b"),
                                        new FieldOnlyProjection(UUID.randomUUID(), "c"))));
        KeysetPage<FieldOnlyProjection> page =
                service.findAllKeyset(null, 2, null, Sort.by("value"), FieldOnlyProjection.class);

        assertEquals(2, page.content().size());
        assertNotNull(page.nextCursor());
        assertFalse(page.nextCursor().isBlank());
    }

    @Test
    void keysetRejectsProjectionWhenBoundaryEntityFallbackWouldBeRequired() {
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class), eq(AltProjection.class)))
                .thenReturn(
                        new PageImpl<>(
                                List.of(
                                        new AltProjection("a"),
                                        new AltProjection("b"),
                                        new AltProjection("c"))));
        assertThrows(
                BadRequestException.class,
                () -> service.findAllKeyset(null, 2, null, Sort.by("value"), AltProjection.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void keysetRejectsCursorWithInvalidTypedValues() throws Exception {
        String cursor =
                KeysetCursorCodec.encode("value", "ASC", "b", "not-a-uuid");
        Specification<TestEntity> spec =
                (Specification<TestEntity>)
                        invoke(
                                service,
                                "buildKeysetCursorSpec",
                                new Class[] {String.class, Sort.Order.class},
                                cursor,
                                Sort.Order.asc("value"));

        Root<TestEntity> root = mock(Root.class);
        jakarta.persistence.criteria.Path<Object> valuePath =
                mock(jakarta.persistence.criteria.Path.class);
        jakarta.persistence.criteria.Path<Object> idPath =
                mock(jakarta.persistence.criteria.Path.class);
        when(root.get("value")).thenReturn(valuePath);
        when(root.get("id")).thenReturn(idPath);
        when(valuePath.getJavaType()).thenReturn((Class) String.class);
        when(idPath.getJavaType()).thenReturn((Class) UUID.class);

        assertThrows(
                BadRequestException.class,
                () ->
                        spec.toPredicate(
                                root, mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @SuppressWarnings("unchecked")
    @Test
    void defaultReadDtosBypassProjectionAdapterAndCustomProjectionsUseIt() {
        ProjectionAdapter adapter = mock(ProjectionAdapter.class);
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(ProjectionAdapter.class)).thenReturn(adapter);
        service.setApplicationContext(context);

        when(adapter.supports(TestResponse.class)).thenReturn(true);
        when(adapter.supports(TestRef.class)).thenReturn(true);
        when(adapter.supports(AltProjection.class)).thenReturn(true);
        when(adapter.projectList(eq(TestEntity.class), eq(AltProjection.class), any()))
                .thenReturn(List.of(new AltProjection("projected")));
        when(queryExecutionStrategy.findAll(any()))
                .thenReturn(List.of(new TestEntity(UUID.randomUUID(), "mapped")));
        when(queryExecutionStrategy.findOne(any()))
                .thenReturn(Optional.of(new TestEntity(UUID.randomUUID(), "mapped")));

        assertEquals(1, service.findByIds(List.of(UUID.randomUUID())).size());
        assertTrue(service.findByIdOptional(UUID.randomUUID()).isPresent());
        assertEquals("mapped-ar", service.findById(UUID.randomUUID()).value());
        assertEquals("mapped-ref-ar", service.findById(UUID.randomUUID(), TestRef.class).value());
        assertEquals(
                "projected-ar", service.findById(UUID.randomUUID(), AltProjection.class).value());
        verify(adapter, never()).projectList(eq(TestEntity.class), eq(TestResponse.class), any());
        verify(adapter, never()).projectList(eq(TestEntity.class), eq(TestRef.class), any());
    }

    @Test
    void unsupportedSearchRequestFailsFast() {
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(new TestEntity(UUID.randomUUID(), "ok"))));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.search(new Object(), PageRequest.of(0, 5)));
        service.search(null, PageRequest.of(0, 5));
        Page<TestResponse> searched =
                service.search(new SearchRequest(), PageRequest.of(0, 5), TestResponse.class);
        assertEquals(1, searched.getContent().size());
        verify(queryExecutionStrategy)
                .findAll(argThat(specification -> specification != null), any(PageRequest.class));
    }

    @Test
    void defaultHookImplementationsExecuteWithoutOverrides() {
        PlainService plain = new PlainService(repository, new TestMapper());
        plain.setQueryExecutorForTests(queryExecutionStrategy);
        UUID id = UUID.randomUUID();
        TestRequest request = new TestRequest(id, "plain");
        TestEntity entity = new TestEntity(id, "old");
        when(queryExecutionStrategy.findOne(any())).thenReturn(Optional.of(entity));
        when(queryExecutionStrategy.exists(any())).thenReturn(true);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.getReferenceById(id)).thenReturn(entity);

        TestResponse created = plain.create(request);
        assertNotNull(created);
        plain.update(id, request);
        plain.patch(id, request);
        plain.delete(id);
        assertEquals(entity, plain.findReferenceById(id));
    }

    @Test
    void loadEntityFailureModesAreExposed() {
        when(queryExecutionStrategy.findOne(any())).thenReturn(Optional.empty());
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.update(UUID.randomUUID(), new TestRequest(null, "x")));
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.patch(UUID.randomUUID(), new TestRequest(null, "x")));
        assertThrows(ResourceNotFoundException.class, () -> service.delete(UUID.randomUUID()));
    }

    @Test
    void findByIdWithEmptyProjectionListThrowsNotFound() {
        ProjectionAdapter adapter = mock(ProjectionAdapter.class);
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(ProjectionAdapter.class)).thenReturn(adapter);
        when(adapter.supports(AltProjection.class)).thenReturn(true);
        when(adapter.projectList(eq(TestEntity.class), eq(AltProjection.class), any()))
                .thenReturn(List.of());
        service.setApplicationContext(context);

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.findById(UUID.randomUUID(), AltProjection.class));
    }

    @Test
    void findByIdProjectionFallsBackWhenAdapterDoesNotSupportProjection() {
        ProjectionAdapter adapter = mock(ProjectionAdapter.class);
        ApplicationContext context = mock(ApplicationContext.class);
        UUID id = UUID.randomUUID();
        when(context.getBean(ProjectionAdapter.class)).thenReturn(adapter);
        when(adapter.supports(AltProjection.class)).thenReturn(false);
        when(queryExecutionStrategy.findOne(any(), eq(AltProjection.class)))
                .thenReturn(Optional.of(new AltProjection("fallback")));
        service.setApplicationContext(context);

        AltProjection projection = service.findById(id, AltProjection.class);

        assertEquals("fallback-ar", projection.value());
        verify(adapter, never()).projectList(any(), any(), any());
        verify(queryExecutionStrategy).findOne(any(), eq(AltProjection.class));
    }

    @Test
    void contextAndProjectionCachesHandleConcurrentResolution() throws Exception {
        ProjectionAdapter adapter = mock(ProjectionAdapter.class);
        when(adapter.supports(AltProjection.class)).thenReturn(false);

        CountDownLatch projectionEntered = new CountDownLatch(1);
        CountDownLatch projectionRelease = new CountDownLatch(1);
        ApplicationContext projectionContext = mock(ApplicationContext.class);
        when(projectionContext.getBean(ProjectionAdapter.class))
                .thenAnswer(
                        invocation -> {
                            projectionEntered.countDown();
                            projectionRelease.await(2, TimeUnit.SECONDS);
                            return adapter;
                        });
        service.setApplicationContext(projectionContext);
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class), eq(AltProjection.class)))
                .thenReturn(new PageImpl<>(List.of(new AltProjection("fallback"))));
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        AtomicReference<Throwable> firstProjectionError = new AtomicReference<>();
        AtomicReference<Throwable> secondProjectionError = new AtomicReference<>();
        Thread projectionThread =
                new Thread(
                        () -> {
                            try {
                                service.findAll(PageRequest.of(0, 1), AltProjection.class);
                            } catch (Throwable throwable) {
                                firstProjectionError.set(throwable);
                            }
                        });
        projectionThread.start();
        assertTrue(projectionEntered.await(2, TimeUnit.SECONDS));
        Thread projectionSecond =
                new Thread(
                        () -> {
                            try {
                                service.findAll(PageRequest.of(0, 1), AltProjection.class);
                            } catch (Throwable throwable) {
                                secondProjectionError.set(throwable);
                            }
                        });
        projectionSecond.start();
        projectionRelease.countDown();
        projectionThread.join();
        projectionSecond.join();
        assertNull(firstProjectionError.get());
        assertNull(secondProjectionError.get());

        CountDownLatch extEntered = new CountDownLatch(1);
        CountDownLatch extRelease = new CountDownLatch(1);
        ApplicationContext extensionContext = mock(ApplicationContext.class);
        CrudRuntimeExtension<TestEntity, TestRequest> extension = new AppendingExtension();
        when(extensionContext.getBeansOfType(CrudRuntimeExtension.class))
                .thenAnswer(
                        invocation -> {
                            extEntered.countDown();
                            extRelease.await(2, TimeUnit.SECONDS);
                            return Map.of("ext", extension);
                        });
        service.setApplicationContext(extensionContext);
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(new TestEntity(UUID.randomUUID(), "v"))));

        AtomicReference<Throwable> firstExtError = new AtomicReference<>();
        Thread extThread =
                new Thread(
                        () -> {
                            try {
                                service.findAll(PageRequest.of(0, 1));
                            } catch (Throwable throwable) {
                                firstExtError.set(throwable);
                            }
                        });
        extThread.start();
        assertTrue(extEntered.await(2, TimeUnit.SECONDS));
        Thread extSecond = new Thread(() -> service.findAll(PageRequest.of(0, 1)));
        extSecond.start();
        extRelease.countDown();
        extThread.join();
        extSecond.join();
        assertNull(firstExtError.get());
    }

    @Test
    void reflectionCoversInternalUtilityBranches() throws Exception {
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(new TestEntity(UUID.randomUUID(), "vv"))));
        when(queryExecutionStrategy.findOne(any()))
                .thenReturn(Optional.of(new TestEntity(UUID.randomUUID(), "one")))
                .thenReturn(Optional.of(new TestEntity(UUID.randomUUID(), "one-ref")));
        service.findAll((Specification<TestEntity>) null, PageRequest.of(0, 1), null);
        service.findById(UUID.randomUUID(), null);
        service.findById(UUID.randomUUID(), TestRef.class);
        service.findAllKeyset(null, 1, " ", Sort.by("value"), TestResponse.class);

        assertNull(
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        String.class,
                        null));
        assertEquals(
                "x",
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        String.class,
                        "x"));
        assertEquals(
                12L,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        Long.class,
                        "12"));
        assertEquals(
                12L,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        long.class,
                        "12"));
        assertEquals(
                7,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        Integer.class,
                        "7"));
        assertEquals(
                7,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        int.class,
                        "7"));
        assertEquals(
                4.5D,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        Double.class,
                        "4.5"));
        assertEquals(
                4.5D,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        double.class,
                        "4.5"));
        assertEquals(
                3.5F,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        Float.class,
                        "3.5"));
        assertEquals(
                3.5F,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        float.class,
                        "3.5"));
        UUID uuid = UUID.randomUUID();
        assertEquals(
                uuid,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        UUID.class,
                        uuid.toString()));
        Instant instant = Instant.now();
        assertEquals(
                instant,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        Instant.class,
                        instant.toString()));
        LocalDateTime dateTime = LocalDateTime.now().withNano(0);
        assertEquals(
                dateTime,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        LocalDateTime.class,
                        dateTime.toString()));
        LocalDate date = LocalDate.now();
        assertEquals(
                date,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        LocalDate.class,
                        date.toString()));
        assertEquals(
                true,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        Boolean.class,
                        "true"));
        assertEquals(
                true,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        boolean.class,
                        "true"));
        assertEquals(
                TestEnum.ONE,
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        TestEnum.class,
                        "ONE"));
        assertEquals(
                "raw",
                invoke(
                        service,
                        "parseValue",
                        new Class[] {Class.class, String.class},
                        Object.class,
                        "raw"));

        String encoded =
                (String)
                        invoke(
                                service,
                                "encodeCursor",
                                new Class[] {
                                    String.class, String.class, Object.class, Object.class
                                },
                                "value",
                                "ASC",
                                "alpha",
                                "id");
        Object decoded = invoke(service, "decodeCursor", new Class[] {String.class}, encoded);
        assertEquals("value", invoke(decoded, "property", new Class[] {}));
        assertThrows(
                BadRequestException.class,
                () -> invoke(service, "decodeCursor", new Class[] {String.class}, "%%%"));

        assertNull(
                invoke(
                        service,
                        "combine",
                        new Class[] {Specification.class, Specification.class},
                        null,
                        null));
        Specification<TestEntity> spec = (root, query, cb) -> cb.conjunction();
        assertEquals(
                spec,
                invoke(
                        service,
                        "combine",
                        new Class[] {Specification.class, Specification.class},
                        spec,
                        null));
        assertEquals(
                spec,
                invoke(
                        service,
                        "combine",
                        new Class[] {Specification.class, Specification.class},
                        null,
                        spec));
        assertNotNull(
                invoke(
                        service,
                        "combine",
                        new Class[] {Specification.class, Specification.class},
                        spec,
                        spec));

        String ascCursor =
                (String)
                        invoke(
                                service,
                                "encodeCursor",
                                new Class[] {
                                    String.class, String.class, Object.class, Object.class
                                },
                                "value",
                                "ASC",
                                "b",
                                UUID.randomUUID().toString());
        String descCursor =
                (String)
                        invoke(
                                service,
                                "encodeCursor",
                                new Class[] {
                                    String.class, String.class, Object.class, Object.class
                                },
                                "value",
                                "DESC",
                                "b",
                                UUID.randomUUID().toString());
        @SuppressWarnings("unchecked")
        Specification<TestEntity> ascSpec =
                (Specification<TestEntity>)
                        invoke(
                                service,
                                "buildKeysetCursorSpec",
                                new Class[] {String.class, Sort.Order.class},
                                ascCursor,
                                Sort.Order.asc("value"));
        @SuppressWarnings("unchecked")
        Specification<TestEntity> descSpec =
                (Specification<TestEntity>)
                        invoke(
                                service,
                                "buildKeysetCursorSpec",
                                new Class[] {String.class, Sort.Order.class},
                                descCursor,
                                Sort.Order.desc("value"));

        Root<TestEntity> root = mock(Root.class);
        jakarta.persistence.criteria.Path<Object> valuePath =
                mock(jakarta.persistence.criteria.Path.class);
        jakarta.persistence.criteria.Path<Object> idPath =
                mock(jakarta.persistence.criteria.Path.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder ascBuilder = mock(CriteriaBuilder.class);
        CriteriaBuilder descBuilder = mock(CriteriaBuilder.class);
        Predicate predicate = mock(Predicate.class);
        when(ascBuilder.greaterThan(
                        any(jakarta.persistence.criteria.Expression.class), any(Comparable.class)))
                .thenReturn(predicate);
        when(ascBuilder.equal(any(), any())).thenReturn(predicate);
        when(ascBuilder.and(any(), any())).thenReturn(predicate);
        when(ascBuilder.or(any(), any())).thenReturn(predicate);
        when(descBuilder.lessThan(
                        any(jakarta.persistence.criteria.Expression.class), any(Comparable.class)))
                .thenReturn(predicate);
        when(descBuilder.equal(any(), any())).thenReturn(predicate);
        when(descBuilder.and(any(), any())).thenReturn(predicate);
        when(descBuilder.or(any(), any())).thenReturn(predicate);

        when(root.get("value")).thenReturn(valuePath);
        when(root.get("id")).thenReturn(idPath);
        when(valuePath.getJavaType()).thenReturn((Class) String.class);
        when(idPath.getJavaType()).thenReturn((Class) UUID.class);

        assertNotNull(ascSpec.toPredicate(root, query, ascBuilder));
        assertNotNull(descSpec.toPredicate(root, query, descBuilder));
        verify(ascBuilder, times(2))
                .greaterThan(
                        any(jakarta.persistence.criteria.Expression.class), any(Comparable.class));
        verify(ascBuilder, never())
                .lessThan(
                        any(jakarta.persistence.criteria.Expression.class), any(Comparable.class));
        verify(descBuilder, times(2))
                .lessThan(
                        any(jakarta.persistence.criteria.Expression.class), any(Comparable.class));
        verify(descBuilder, never())
                .greaterThan(
                        any(jakarta.persistence.criteria.Expression.class), any(Comparable.class));

        Metamodel metamodel = mock(Metamodel.class);
        @SuppressWarnings("unchecked")
        ManagedType<TestEntity> managedType = (ManagedType<TestEntity>) mock(ManagedType.class);
        Attribute<?, ?> attribute = mock(Attribute.class);
        service.metamodel = metamodel;
        when(metamodel.managedType(TestEntity.class)).thenReturn(managedType);
        when(managedType.getAttribute("value")).thenReturn((Attribute) attribute);
        when(attribute.isCollection()).thenReturn(false);
        invoke(service, "validateSort", new Class[] {Sort.class}, Sort.by("value"));
        verify(metamodel, never()).managedType(String.class);

        when(managedType.getAttribute("unknown")).thenThrow(new IllegalArgumentException("x"));
        assertThrows(
                BadRequestException.class,
                () ->
                        invoke(
                                service,
                                "validateSort",
                                new Class[] {Sort.class},
                                Sort.by("unknown")));

        when(attribute.isCollection()).thenReturn(true);
        assertThrows(
                BadRequestException.class,
                () ->
                        invoke(
                                service,
                                "validateSort",
                                new Class[] {Sort.class},
                                Sort.by("value.name")));

        invoke(service, "validateSort", new Class[] {Sort.class}, (Object) null);

        when(attribute.isCollection()).thenReturn(false);
        when(attribute.getJavaType()).thenReturn((Class) String.class);
        when(metamodel.managedType(String.class)).thenThrow(new IllegalArgumentException("nested"));
        assertThrows(
                BadRequestException.class,
                () ->
                        invoke(
                                service,
                                "validateSort",
                                new Class[] {Sort.class},
                                Sort.by("value.name")));

        @SuppressWarnings("unchecked")
        Specification<TestEntity> byIdSpec =
                (Specification<TestEntity>)
                        invoke(service, "byId", new Class[] {Object.class}, UUID.randomUUID());
        @SuppressWarnings("unchecked")
        Specification<TestEntity> byIdsSpec =
                (Specification<TestEntity>)
                        invoke(
                                service,
                                "byIds",
                                new Class[] {java.util.Collection.class},
                                List.of(UUID.randomUUID()));
        Root<TestEntity> idRoot = mock(Root.class);
        jakarta.persistence.criteria.Path<Object> idOnlyPath =
                mock(jakarta.persistence.criteria.Path.class);
        CriteriaBuilder idBuilder = mock(CriteriaBuilder.class);
        Predicate idPredicate = mock(Predicate.class);
        service.metamodel = null;
        java.lang.reflect.Field idNameField =
                CoreCrudOperations.class.getDeclaredField("idAttributeName");
        idNameField.setAccessible(true);
        idNameField.set(service, null);
        when(idRoot.get(anyString())).thenReturn(idOnlyPath);
        when(idBuilder.equal(any(jakarta.persistence.criteria.Expression.class), any()))
                .thenReturn(idPredicate);
        when(idOnlyPath.in(any(java.util.Collection.class))).thenReturn(idPredicate);
        assertDoesNotThrow(
                () -> byIdSpec.toPredicate(idRoot, mock(CriteriaQuery.class), idBuilder));
        assertEquals(
                idPredicate, byIdsSpec.toPredicate(idRoot, mock(CriteriaQuery.class), idBuilder));

        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.From<Object, Object> from =
                (jakarta.persistence.criteria.From<Object, Object>)
                        mock(jakarta.persistence.criteria.From.class);
        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.Path<Object> nestedPath =
                (jakarta.persistence.criteria.Path<Object>)
                        mock(jakarta.persistence.criteria.Path.class);
        when(from.get("id")).thenReturn(nestedPath);
        assertNotNull(
                invoke(
                        service,
                        "resolvePath",
                        new Class[] {jakarta.persistence.criteria.From.class, String.class},
                        from,
                        "id"));

        class InnerHolder {
            private String value = null;
        }
        class NestedHolder {
            private final InnerHolder inner = new InnerHolder();

            private InnerHolder getInner() {
                return inner;
            }
        }
        assertNull(
                invoke(
                        service,
                        "extractPathValue",
                        new Class[] {Object.class, String.class},
                        new NestedHolder(),
                        "inner.value"));
        assertNull(
                invoke(
                        service,
                        "extractPathValue",
                        new Class[] {Object.class, String.class},
                        new NestedHolder(),
                        "inner.value.any"));

        class ThrowingGetter {
            public String getValue() {
                throw new IllegalStateException("boom");
            }
        }
        assertThrows(
                IllegalStateException.class,
                () ->
                        invoke(
                                service,
                                "readProperty",
                                new Class[] {Object.class, String.class},
                                new ThrowingGetter(),
                                "value"));

        class NoGetterField {
            private final String value = "ok";
        }
        class GoodGetter {
            public String getValue() {
                return "good";
            }
        }
        assertEquals(
                "good",
                invoke(
                        service,
                        "readProperty",
                        new Class[] {Object.class, String.class},
                        new GoodGetter(),
                        "value"));
        assertEquals(
                "ok",
                invoke(
                        service,
                        "readProperty",
                        new Class[] {Object.class, String.class},
                        new NoGetterField(),
                        "value"));
        assertThrows(
                IllegalStateException.class,
                () ->
                        invoke(
                                service,
                                "readField",
                                new Class[] {Object.class, String.class},
                                new Object(),
                                "missing"));

        assertNull(
                invoke(
                        service,
                        "tryBuildCursorFromProjection",
                        new Class[] {List.class, Sort.Order.class},
                        null,
                        Sort.Order.asc("value")));
        assertNull(
                invoke(
                        service,
                        "tryBuildCursorFromProjection",
                        new Class[] {List.class, Sort.Order.class},
                        List.of(),
                        Sort.Order.asc("value")));
        assertNull(
                invoke(
                        service,
                        "tryBuildCursorFromProjection",
                        new Class[] {List.class, Sort.Order.class},
                        List.of(new AltProjection(null)),
                        Sort.Order.asc("value")));

        Metamodel idMetamodel = mock(Metamodel.class);
        @SuppressWarnings("unchecked")
        EntityType<TestEntity> entityType = (EntityType<TestEntity>) mock(EntityType.class);
        jakarta.persistence.metamodel.Type<?> idType =
                mock(jakarta.persistence.metamodel.Type.class);
        SingularAttribute<?, ?> singularId = mock(SingularAttribute.class);
        service.metamodel = idMetamodel;
        when(idMetamodel.entity(TestEntity.class)).thenReturn(entityType);
        org.mockito.Mockito.doReturn(idType).when(entityType).getIdType();
        org.mockito.Mockito.doReturn(UUID.class).when(idType).getJavaType();
        org.mockito.Mockito.doReturn(singularId).when(entityType).getId(UUID.class);
        when(singularId.getName()).thenReturn("id");
        assertEquals("id", invoke(service, "resolveIdAttributeNameInternal", new Class[] {}));

        java.lang.reflect.Field idNameField2 =
                CoreCrudOperations.class.getDeclaredField("idAttributeName");
        idNameField2.setAccessible(true);
        idNameField2.set(service, null);
        AtomicReference<Object> idResult = new AtomicReference<>();
        Thread thread =
                new Thread(
                        () -> {
                            try {
                                idResult.set(
                                        invoke(service, "resolveIdAttributeName", new Class[] {}));
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        });
        thread.start();
        idNameField2.set(service, "preset");
        thread.join(1000L);
        assertFalse(thread.isAlive(), "resolver thread did not finish in time");
        Object resolvedId = idResult.get();
        assertTrue(
                "id".equals(resolvedId) || "preset".equals(resolvedId),
                "expected id or preset but got: " + resolvedId);
    }

    @SuppressWarnings("unchecked")
    @Test
    void reflectionCoversRemainingNestedSortAndCursorNullIdBranch() throws Exception {
        record NullableIdProjection(UUID id, String value) {}
        record NullableValueProjection(UUID id, String value) {}
        final class ThrowingProjection {
            private UUID getId() {
                return UUID.randomUUID();
            }

            private String getValue() {
                throw new IllegalStateException("boom");
            }
        }
        assertNull(
                invoke(
                        service,
                        "tryBuildCursorFromProjection",
                        new Class[] {List.class, Sort.Order.class},
                        List.of(new NullableIdProjection(null, "v")),
                        Sort.Order.asc("value")));
        assertNull(
                invoke(
                        service,
                        "tryBuildCursorFromProjection",
                        new Class[] {List.class, Sort.Order.class},
                        List.of(new NullableValueProjection(UUID.randomUUID(), null)),
                        Sort.Order.asc("value")));
        assertNull(
                invoke(
                        service,
                        "tryBuildCursorFromProjection",
                        new Class[] {List.class, Sort.Order.class},
                        List.of(new ThrowingProjection()),
                        Sort.Order.asc("value")));

        class NoSuchFieldCarrier {}
        class ParentFieldCarrier {
            private final String inherited = "inherited";
        }
        class ChildFieldCarrier extends ParentFieldCarrier {}
        assertThrows(
                IllegalStateException.class,
                () ->
                        invoke(
                                service,
                                "readField",
                                new Class[] {Object.class, String.class},
                                new NoSuchFieldCarrier(),
                                "missing"));
        assertEquals(
                "inherited",
                invoke(
                        service,
                        "readField",
                        new Class[] {Object.class, String.class},
                        new ChildFieldCarrier(),
                        "inherited"));
        assertEquals(
                "cross",
                invoke(
                        service,
                        "readField",
                        new Class[] {Object.class, String.class},
                        new ExternalFieldCarrier(),
                        "value"));
        assertEquals(
                "cross-package",
                invoke(
                        service,
                        "readField",
                        new Class[] {Object.class, String.class},
                        new CrossPackagePrivateFieldCarrier(),
                        "value"));

        service.metamodel = null;
        invoke(service, "validateSort", new Class[] {Sort.class}, Sort.by("value"));

        Metamodel metamodel = mock(Metamodel.class);
        ManagedType<TestEntity> rootType = (ManagedType<TestEntity>) mock(ManagedType.class);
        ManagedType<String> nestedType = (ManagedType<String>) mock(ManagedType.class);
        Attribute<?, ?> valueAttribute = mock(Attribute.class);
        Attribute<?, ?> nestedAttribute = mock(Attribute.class);

        service.metamodel = metamodel;
        when(metamodel.managedType(TestEntity.class)).thenReturn(rootType);
        when(rootType.getAttribute("value")).thenReturn((Attribute) valueAttribute);
        when(valueAttribute.isCollection()).thenReturn(false);
        when(valueAttribute.getJavaType()).thenReturn((Class) String.class);
        when(metamodel.managedType(String.class)).thenReturn(nestedType);
        when(nestedType.getAttribute("name")).thenReturn((Attribute) nestedAttribute);
        when(nestedAttribute.isCollection()).thenReturn(false);
        when(nestedAttribute.getJavaType())
                .thenThrow(
                        new AssertionError(
                                "terminal nested sort attribute type must not be resolved"));

        invoke(service, "validateSort", new Class[] {Sort.class}, Sort.unsorted());
        invoke(service, "validateSort", new Class[] {Sort.class}, Sort.by("value.name"));
        invoke(service, "validateSort", new Class[] {Sort.class}, Sort.by("."));
    }

    @Test
    void contextExtensionsAreAppliedFromApplicationContextAndCached() {
        CrudRuntimeExtension<TestEntity, TestRequest> contextExtension =
                new CrudRuntimeExtension<>() {
                    @Override
                    public <P> P afterRead(P dto) {
                        if (dto instanceof TestResponse response) {
                            return (P) new TestResponse(response.id(), response.value() + "-ctx");
                        }
                        return dto;
                    }
                };

        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeansOfType(CrudRuntimeExtension.class))
                .thenReturn(Map.of("ctx", contextExtension));
        service.setApplicationContext(context);
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(new TestEntity(UUID.randomUUID(), "value"))));

        Page<TestResponse> first = service.findAll(PageRequest.of(0, 1));
        Page<TestResponse> second = service.findAll(PageRequest.of(0, 1));

        assertEquals("value-ctx-ar", first.getContent().getFirst().value());
        assertEquals("value-ctx-ar", second.getContent().getFirst().value());
        verify(context, times(1)).getBeansOfType(CrudRuntimeExtension.class);
    }

    @Test
    void countUsesContextReadFilterAndCachesContextExtensions() {
        @SuppressWarnings("unchecked")
        Specification<TestEntity> readFilter =
                (Specification<TestEntity>) mock(Specification.class);
        CrudRuntimeExtension<TestEntity, TestRequest> contextExtension =
                new CrudRuntimeExtension<>() {
                    @Override
                    public Specification<TestEntity> readFilter(Class<TestEntity> entityClass) {
                        return readFilter;
                    }
                };
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeansOfType(CrudRuntimeExtension.class))
                .thenReturn(Map.of("ctx", contextExtension));
        when(queryExecutionStrategy.count(readFilter)).thenReturn(3L);
        service.setApplicationContext(context);

        assertEquals(3L, service.count());
        assertEquals(3L, service.count());
        verify(queryExecutionStrategy, times(2)).count(readFilter);
        verify(context, times(1)).getBeansOfType(CrudRuntimeExtension.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void existsByIdUsesByIdPredicate() {
        Predicate expectedPredicate = mock(Predicate.class);
        Root<TestEntity> root = mock(Root.class);
        jakarta.persistence.criteria.Path<Object> idPath =
                mock(jakarta.persistence.criteria.Path.class);
        CriteriaBuilder criteriaBuilder =
                mock(
                        CriteriaBuilder.class,
                        invocation -> {
                            if ("equal".equals(invocation.getMethod().getName())) {
                                return expectedPredicate;
                            }
                            return org.mockito.Answers.RETURNS_DEFAULTS.answer(invocation);
                        });
        when(root.get(anyString())).thenReturn(idPath);
        when(queryExecutionStrategy.exists(any()))
                .thenAnswer(
                        invocation -> {
                            Specification<TestEntity> spec = invocation.getArgument(0);
                            return expectedPredicate.equals(
                                    spec.toPredicate(
                                            root, mock(CriteriaQuery.class), criteriaBuilder));
                        });

        assertTrue(service.existsById(UUID.randomUUID()));
    }

    @Test
    void existsByIdPropagatesFalseFromQueryExecutor() {
        when(queryExecutionStrategy.exists(any())).thenReturn(false);
        assertTrue(!service.existsById(UUID.randomUUID()));
    }

    private static Object invoke(
            Object target, String methodName, Class<?>[] signature, Object... args)
            throws Exception {
        Method method = findMethod(target.getClass(), methodName, signature);
        try {
            return method.invoke(target, args);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw ex;
        }
    }

    private static Method findMethod(Class<?> type, String methodName, Class<?>[] signature)
            throws NoSuchMethodException {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName, signature);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        try {
            Method method = KeysetCursorCodec.class.getDeclaredMethod(methodName, signature);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
            // Report the original target type below.
        }
        throw new NoSuchMethodException(type.getName() + "." + methodName);
    }

    private static final class SearchRequest implements SpecificationProvider<TestEntity> {
        @Override
        public Specification<TestEntity> toSpecification() {
            return (root, query, cb) -> cb.conjunction();
        }
    }

    private enum TestEnum {
        ONE
    }

    private static final class TestEntity {
        @Id private UUID id;
        private String value;

        private TestEntity(UUID id, String value) {
            this.id = id;
            this.value = value;
        }

        private UUID getId() {
            return id;
        }

        private String getValue() {
            return value;
        }
    }

    private record TestRequest(UUID id, String value) {}

    private record TestResponse(UUID id, String value) {}

    private record TestRef(UUID id, String value) {}

    private record AltProjection(String value) {}

    private static class TestMapper
            implements EntityMapper<TestEntity, TestRequest, TestResponse, TestRef, UUID> {

        @Override
        public TestEntity fromRequest(TestRequest request) {
            return new TestEntity(
                    request.id() != null ? request.id() : UUID.randomUUID(), request.value());
        }

        @Override
        public TestEntity update(TestEntity entity, TestRequest request) {
            entity.value = request.value();
            return entity;
        }

        @Override
        public TestEntity patch(TestEntity entity, TestRequest request) {
            entity.value = request.value() + "-patch";
            return entity;
        }

        @Override
        public TestResponse toResponse(TestEntity entity) {
            return new TestResponse(entity.id, entity.value);
        }

        @Override
        public TestRef toRef(TestEntity entity) {
            return new TestRef(entity.id, entity.value + "-ref");
        }

        @Override
        public UUID getIdFromRequest(TestRequest request) {
            return request.id();
        }
    }

    private static final class MissingIdMapper extends TestMapper {
        @Override
        public UUID getIdFromRequest(TestRequest request) {
            throw new MapperException(
                    "Failed to read 'id' property from request DTO: " + request.getClass(),
                    new NotReadablePropertyException(request.getClass(), "id"));
        }
    }

    private static final class FailingFromRequestMapper extends TestMapper {
        @Override
        public TestEntity fromRequest(TestRequest request) {
            throw new IllegalArgumentException("cannot map");
        }
    }

    private static final class MapperExceptionThrowingFromRequestMapper extends TestMapper {
        @Override
        public TestEntity fromRequest(TestRequest request) {
            throw new MapperException("mapper boom", new IllegalStateException("nested"));
        }
    }

    private static final class AppendingExtension
            implements CrudRuntimeExtension<TestEntity, TestRequest> {
        @Override
        public <P> P afterRead(P dto) {
            if (dto instanceof TestResponse response) {
                return (P) new TestResponse(response.id(), response.value() + "-ar");
            }
            if (dto instanceof TestRef ref) {
                return (P) new TestRef(ref.id(), ref.value() + "-ar");
            }
            if (dto instanceof AltProjection projection) {
                return (P) new AltProjection(projection.value() + "-ar");
            }
            return dto;
        }

        @Override
        public TestRequest beforeCreate(TestRequest request) {
            return new TestRequest(request.id(), request.value() + "-bc");
        }

        @Override
        public TestRequest beforeUpdate(TestRequest request, TestEntity existing) {
            return new TestRequest(request.id(), request.value() + "-bu");
        }
    }

    private static final class TestService
            extends AbstractCrudService<TestEntity, TestRequest, TestResponse, TestRef, UUID> {

        private final List<CrudRuntimeExtension<TestEntity, TestRequest>> extensions =
                new ArrayList<>();
        private int preSaveCount;
        private int postSaveCount;
        private int preDeleteCount;
        private int postDeleteCount;
        private EntityMapperCustomizer<TestEntity, TestRequest, TestResponse, TestRef>
                mapperCustomizer = EntityMapperCustomizer.noOp();

        private TestService(
                JpaRepository<TestEntity, UUID> repository,
                EntityMapper<TestEntity, TestRequest, TestResponse, TestRef, UUID> mapper) {
            super(repository, mapper, TestEntity.class, TestResponse.class, TestRef.class);
        }

        @Override
        protected List<CrudRuntimeExtension<TestEntity, TestRequest>> runtimeExtensions() {
            return extensions;
        }

        @Override
        protected EntityMapperCustomizer<TestEntity, TestRequest, TestResponse, TestRef>
                mapperCustomizer() {
            return mapperCustomizer;
        }

        @Override
        protected void preSave(TestEntity entity, TestRequest request) {
            preSaveCount++;
        }

        @Override
        protected void postSave(TestEntity entity) {
            postSaveCount++;
        }

        @Override
        protected void preDelete(TestEntity entity) {
            preDeleteCount++;
        }

        @Override
        protected void postDelete(TestEntity entity) {
            postDeleteCount++;
        }
    }

    private static final class PlainService
            extends AbstractCrudService<TestEntity, TestRequest, TestResponse, TestRef, UUID> {
        private PlainService(
                JpaRepository<TestEntity, UUID> repository,
                EntityMapper<TestEntity, TestRequest, TestResponse, TestRef, UUID> mapper) {
            super(repository, mapper, TestEntity.class, TestResponse.class, TestRef.class);
        }
    }
}

final class ExternalFieldCarrier {
    private final String value = "cross";
}

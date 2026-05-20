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
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
import nl.datasteel.crudcraft.runtime.service.strategy.QueryExecutionStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class AbstractCrudServiceConcurrencyTest {

    private static final int THREADS = 16;

    @SuppressWarnings("unchecked")
    @Test
    void concurrentMetamodelResolutionPublishesOneCachedIdAttribute() throws Exception {
        TestService service = service();
        EntityManager entityManager = mock(EntityManager.class);
        Metamodel metamodel = mock(Metamodel.class);
        EntityType<TestEntity> entityType = (EntityType<TestEntity>) mock(EntityType.class);
        jakarta.persistence.metamodel.Type<?> idType =
                mock(jakarta.persistence.metamodel.Type.class);
        SingularAttribute<?, ?> idAttribute = mock(SingularAttribute.class);
        when(entityManager.getMetamodel()).thenReturn(metamodel);
        when(metamodel.entity(TestEntity.class)).thenReturn(entityType);
        org.mockito.Mockito.doReturn(idType).when(entityType).getIdType();
        org.mockito.Mockito.doReturn(UUID.class).when(idType).getJavaType();
        org.mockito.Mockito.doReturn(idAttribute).when(entityType).getId(UUID.class);
        when(idAttribute.getName()).thenReturn("uuid");
        service.setEntityManager(entityManager);

        List<String> results = runConcurrently(() -> invokeIdAttributeName(service));

        assertEquals(THREADS, results.size());
        assertTrue(results.stream().allMatch("uuid"::equals));
        verify(metamodel, times(1)).entity(TestEntity.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void concurrentExtensionResolutionUsesOneApplicationContextLookup() throws Exception {
        TestService service = service();
        QueryExecutionStrategy<TestEntity> queryExecutionStrategy =
                (QueryExecutionStrategy<TestEntity>) mock(QueryExecutionStrategy.class);
        service.setQueryExecutorForTests(queryExecutionStrategy);
        when(queryExecutionStrategy.findAll(any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(new TestEntity(UUID.randomUUID(), "seed"))));
        CrudRuntimeExtension<TestEntity, TestRequest> extension =
                new CrudRuntimeExtension<>() {
                    @Override
                    public <P> P afterRead(P dto) {
                        if (dto instanceof TestResponse response) {
                            return (P) new TestResponse(response.id(), response.value() + "-ctx");
                        }
                        return dto;
                    }
                };
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeansOfType(CrudRuntimeExtension.class))
                .thenReturn(Map.of("extension", extension));
        service.setApplicationContext(applicationContext);

        List<String> values =
                runConcurrently(
                        () -> {
                            Page<TestResponse> page = service.findAll(PageRequest.of(0, 1));
                            return page.getContent().getFirst().value();
                        });

        assertEquals(THREADS, values.size());
        assertTrue(values.stream().allMatch("seed-ctx"::equals));
        verify(applicationContext, times(1)).getBeansOfType(CrudRuntimeExtension.class);
    }

    @Test
    void stressReadsOneThousandRequestsAcrossHundredEntitiesWithoutSharedStateLeakage()
            throws Exception {
        int entityCount = 128;
        int requestCount = 1024;
        TestService service = service();
        service.setQueryExecutorForTests(new InMemoryQueryExecutionStrategy(testEntities(entityCount)));

        List<Integer> sizes =
                runConcurrently(
                        requestCount,
                        () -> {
                            Page<TestResponse> page = service.findAll(PageRequest.of(0, 128));
                            assertNotNull(page.getContent().getFirst().id());
                            return page.getContent().size();
                        });

        assertEquals(requestCount, sizes.size());
        assertTrue(sizes.stream().allMatch(size -> size == entityCount));
    }

    private static <T> List<T> runConcurrently(Callable<T> task) throws Exception {
        return runConcurrently(THREADS, task);
    }

    private static <T> List<T> runConcurrently(int requestCount, Callable<T> task)
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(Math.min(THREADS, requestCount));
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (int i = 0; i < requestCount; i++) {
                futures.add(
                        executor.submit(
                                () -> {
                                    ready.countDown();
                                    start.await();
                                    return task.call();
                                }));
            }
            ready.await();
            start.countDown();
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(30, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    private static String invokeIdAttributeName(TestService service) throws Exception {
        Method method = CoreCrudOperations.class.getDeclaredMethod("resolveIdAttributeName");
        method.setAccessible(true);
        return (String) method.invoke(service);
    }

    private static List<TestEntity> testEntities(int entityCount) {
        List<TestEntity> entities = new ArrayList<>();
        for (int i = 0; i < entityCount; i++) {
            entities.add(new TestEntity(UUID.randomUUID(), "value-" + i));
        }
        return List.copyOf(entities);
    }

    @SuppressWarnings("unchecked")
    private static TestService service() {
        JpaRepository<TestEntity, UUID> repository =
                mock(
                        JpaRepository.class,
                        org.mockito.Mockito.withSettings()
                                .extraInterfaces(JpaSpecificationExecutor.class));
        return new TestService(repository, new TestMapper());
    }

    private static final class TestEntity {
        @Id private UUID id;
        private String value;

        private TestEntity(UUID id, String value) {
            this.id = id;
            this.value = value;
        }
    }

    private record TestRequest(UUID id, String value) {}

    private record TestResponse(UUID id, String value) {}

    private record TestRef(UUID id, String value) {}

    private static final class TestMapper
            implements EntityMapper<TestEntity, TestRequest, TestResponse, TestRef, UUID> {
        @Override
        public TestEntity fromRequest(TestRequest request) {
            return new TestEntity(request.id(), request.value());
        }

        @Override
        public TestEntity update(TestEntity entity, TestRequest request) {
            entity.value = request.value();
            return entity;
        }

        @Override
        public TestEntity patch(TestEntity entity, TestRequest request) {
            entity.value = request.value();
            return entity;
        }

        @Override
        public TestResponse toResponse(TestEntity entity) {
            return new TestResponse(entity.id, entity.value);
        }

        @Override
        public TestRef toRef(TestEntity entity) {
            return new TestRef(entity.id, entity.value);
        }

        @Override
        public UUID getIdFromRequest(TestRequest request) {
            return request.id();
        }
    }

    private static final class InMemoryQueryExecutionStrategy
            implements QueryExecutionStrategy<TestEntity> {
        private final List<TestEntity> entities;

        private InMemoryQueryExecutionStrategy(List<TestEntity> entities) {
            this.entities = entities;
        }

        @Override
        public Page<TestEntity> findAll(
                org.springframework.data.jpa.domain.Specification<TestEntity> spec,
                org.springframework.data.domain.Pageable pageable) {
            return new PageImpl<>(entities, pageable, entities.size());
        }

        @Override
        public List<TestEntity> findAll(
                org.springframework.data.jpa.domain.Specification<TestEntity> spec) {
            return entities;
        }

        @Override
        public <R> Page<R> findAll(
                org.springframework.data.jpa.domain.Specification<TestEntity> spec,
                org.springframework.data.domain.Pageable pageable,
                Class<R> projection) {
            return Page.empty(pageable);
        }

        @Override
        public <R> List<R> findAll(
                org.springframework.data.jpa.domain.Specification<TestEntity> spec,
                Class<R> projection) {
            return List.of();
        }

        @Override
        public java.util.Optional<TestEntity> findOne(
                org.springframework.data.jpa.domain.Specification<TestEntity> spec) {
            return entities.stream().findFirst();
        }

        @Override
        public <R> java.util.Optional<R> findOne(
                org.springframework.data.jpa.domain.Specification<TestEntity> spec,
                Class<R> projection) {
            return java.util.Optional.empty();
        }

        @Override
        public boolean exists(org.springframework.data.jpa.domain.Specification<TestEntity> spec) {
            return !entities.isEmpty();
        }

        @Override
        public long count(org.springframework.data.jpa.domain.Specification<TestEntity> spec) {
            return entities.size();
        }
    }

    private static final class TestService
            extends AbstractCrudService<TestEntity, TestRequest, TestResponse, TestRef, UUID> {
        private TestService(
                JpaRepository<TestEntity, UUID> repository,
                EntityMapper<TestEntity, TestRequest, TestResponse, TestRef, UUID> mapper) {
            super(repository, mapper, TestEntity.class, TestResponse.class, TestRef.class);
        }
    }
}

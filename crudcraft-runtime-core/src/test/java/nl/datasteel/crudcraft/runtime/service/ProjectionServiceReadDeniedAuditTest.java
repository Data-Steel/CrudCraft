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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
import nl.datasteel.crudcraft.runtime.service.strategy.QueryExecutionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ProjectionServiceReadDeniedAuditTest {

    private QueryExecutionStrategy<TestEntity> queryExecutionStrategy;
    private ReadDeniedAuditHook auditHook;
    private TestService service;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeEach
    void setUp() {
        JpaRepository<TestEntity, UUID> repository =
                (JpaRepository<TestEntity, UUID>)
                        mock(
                                JpaRepository.class,
                                org.mockito.Mockito.withSettings()
                                        .extraInterfaces(JpaSpecificationExecutor.class));
        queryExecutionStrategy =
                (QueryExecutionStrategy<TestEntity>) mock(QueryExecutionStrategy.class);
        auditHook = mock(ReadDeniedAuditHook.class);
        service = new TestService(repository, new TestMapper());
        service.setQueryExecutorForTests(queryExecutionStrategy);

        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeansOfType(ReadDeniedAuditHook.class))
                .thenReturn(Map.of("auditHook", auditHook));
        service.setApplicationContext(context);
    }

    @Test
    void findByIdAuditsDeniedReadWhenEntityExistsButIsHidden() {
        UUID id = UUID.randomUUID();
        when(queryExecutionStrategy.findOne(any())).thenReturn(Optional.empty());
        when(queryExecutionStrategy.exists(any())).thenReturn(true).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));

        verify(queryExecutionStrategy, times(1)).findOne(any());
        verify(queryExecutionStrategy, times(2)).exists(any());
        verify(auditHook, times(1)).onReadDenied(TestEntity.class, id, "findById");
    }

    @Test
    void findByIdSkipsAuditWhenEntityDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(queryExecutionStrategy.findOne(any())).thenReturn(Optional.empty());
        when(queryExecutionStrategy.exists(any())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));

        verify(queryExecutionStrategy, times(1)).findOne(any());
        verify(queryExecutionStrategy, times(1)).exists(any());
        verify(auditHook, times(0)).onReadDenied(any(), any(), any());
    }

    private static final class TestService
            extends AbstractCrudService<TestEntity, TestRequest, TestResponse, TestResponse, UUID> {
        private TestService(
                JpaRepository<TestEntity, UUID> repository,
                EntityMapper<TestEntity, TestRequest, TestResponse, TestResponse, UUID> mapper) {
            super(repository, mapper, TestEntity.class, TestResponse.class, TestResponse.class);
        }

        @Override
        protected List<CrudRuntimeExtension<TestEntity, TestRequest>> runtimeExtensions() {
            return List.of(
                    new CrudRuntimeExtension<>() {
                        @Override
                        public org.springframework.data.jpa.domain.Specification<TestEntity> readFilter(
                                Class<TestEntity> entityType) {
                            return (root, query, cb) -> cb.disjunction();
                        }
                    });
        }
    }

    private record TestEntity(UUID id, String value) {}

    private record TestRequest(String value) {}

    private record TestResponse(UUID id, String value) {}

    private static final class TestMapper
            implements EntityMapper<TestEntity, TestRequest, TestResponse, TestResponse, UUID> {

        @Override
        public TestEntity fromRequest(TestRequest request) {
            return new TestEntity(null, request.value());
        }

        @Override
        public TestEntity update(TestEntity entity, TestRequest request) {
            return entity;
        }

        @Override
        public TestEntity patch(TestEntity entity, TestRequest request) {
            return entity;
        }

        @Override
        public TestResponse toResponse(TestEntity entity) {
            return new TestResponse(entity.id(), entity.value());
        }

        @Override
        public TestResponse toRef(TestEntity entity) {
            return new TestResponse(entity.id(), entity.value());
        }

        @Override
        public UUID getIdFromRequest(TestRequest request) {
            return null;
        }
    }
}

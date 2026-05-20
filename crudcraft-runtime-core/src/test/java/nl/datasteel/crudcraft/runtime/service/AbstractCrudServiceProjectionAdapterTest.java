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
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import nl.datasteel.crudcraft.runtime.service.projection.ProjectionAdapter;
import nl.datasteel.crudcraft.runtime.service.strategy.QueryExecutionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class AbstractCrudServiceProjectionAdapterTest {

    private JpaRepository<TestEntity, UUID> repository;
    private QueryExecutionStrategy<TestEntity> queryExecutionStrategy;
    private TestService service;
    private ApplicationContext context;

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
        context = mock(ApplicationContext.class);
        service.setApplicationContext(context);
    }

    @Test
    void customProjectionUsesProjectionAdapterWhenAvailable() {
        ProjectionAdapter adapter = mock(ProjectionAdapter.class);
        when(adapter.supports(CursorProjection.class)).thenReturn(true);
        when(adapter.projectPage(eq(TestEntity.class), eq(CursorProjection.class), any(), any()))
                .thenReturn(new PageImpl<>(List.of(new CursorProjection(UUID.randomUUID(), "p"))));
        when(context.getBean(ProjectionAdapter.class)).thenReturn(adapter);

        Page<CursorProjection> result = service.findAll(PageRequest.of(0, 5), CursorProjection.class);

        assertEquals(1, result.getTotalElements());
        verify(adapter).projectPage(eq(TestEntity.class), eq(CursorProjection.class), any(), any());
        verify(queryExecutionStrategy, never()).findAll(any(), any(), eq(CursorProjection.class));
    }

    @Test
    void searchFallsBackWhenProjectionAdapterMissing() {
        when(context.getBean(ProjectionAdapter.class)).thenThrow(mock(BeansException.class));
        when(queryExecutionStrategy.findAll(
                        any(),
                        org.mockito.ArgumentMatchers.any(
                                org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new TestEntity(UUID.randomUUID(), "fallback"))));

        Page<TestResponse> result = service.findAll(PageRequest.of(0, 5), TestResponse.class);

        assertEquals(1, result.getTotalElements());
        verify(queryExecutionStrategy)
                .findAll(
                        any(),
                        org.mockito.ArgumentMatchers.any(
                                org.springframework.data.domain.Pageable.class));
    }

    @Test
    void customProjectionFallsBackWhenAdapterDoesNotSupportProjection() {
        ProjectionAdapter adapter = mock(ProjectionAdapter.class);
        when(adapter.supports(CursorProjection.class)).thenReturn(false);
        when(context.getBean(ProjectionAdapter.class)).thenReturn(adapter);
        when(queryExecutionStrategy.findAll(any(), any(), eq(CursorProjection.class)))
                .thenReturn(new PageImpl<>(List.of(new CursorProjection(UUID.randomUUID(), "db"))));

        Page<CursorProjection> result = service.findAll(PageRequest.of(0, 5), CursorProjection.class);

        assertEquals(1, result.getTotalElements());
        verify(adapter, never()).projectPage(any(), any(), any(), any());
        verify(queryExecutionStrategy).findAll(any(), any(), eq(CursorProjection.class));
    }

    @Test
    void keysetRejectsProjectionWithoutCursorFields() {
        when(context.getBean(ProjectionAdapter.class)).thenThrow(mock(BeansException.class));
        when(queryExecutionStrategy.findAll(
                        any(),
                        org.mockito.ArgumentMatchers.any(
                                org.springframework.data.domain.Pageable.class)))
                .thenReturn(
                        new PageImpl<>(
                                List.of(
                                        new TestEntity(UUID.randomUUID(), "a"),
                                        new TestEntity(UUID.randomUUID(), "b"),
                                        new TestEntity(UUID.randomUUID(), "c"))))
                .thenReturn(new PageImpl<>(List.of(new TestEntity(UUID.randomUUID(), "b"))));

        assertThrows(
                BadRequestException.class,
                () ->
                        service.findAllKeyset(
                                null,
                                2,
                                null,
                                Sort.by(Sort.Direction.ASC, "value"),
                                TestResponse.class));
        verify(queryExecutionStrategy, times(1))
                .findAll(
                        any(),
                        org.mockito.ArgumentMatchers.any(
                                org.springframework.data.domain.Pageable.class));
    }

    @Test
    void keysetBuildsCursorFromProjectionWithoutBoundaryQuery() {
        ProjectionAdapter adapter = mock(ProjectionAdapter.class);
        when(adapter.supports(CursorProjection.class)).thenReturn(true);
        when(adapter.projectPage(eq(TestEntity.class), eq(CursorProjection.class), any(), any()))
                .thenReturn(
                        new PageImpl<>(
                                List.of(
                                        new CursorProjection(UUID.randomUUID(), "a"),
                                        new CursorProjection(UUID.randomUUID(), "b"),
                                        new CursorProjection(UUID.randomUUID(), "c"))));
        when(context.getBean(ProjectionAdapter.class)).thenReturn(adapter);

        KeysetPage<CursorProjection> page =
                service.findAllKeyset(
                        null,
                        2,
                        null,
                        Sort.by(Sort.Direction.ASC, "value"),
                        CursorProjection.class);

        assertNotNull(page.nextCursor());
        assertEquals(2, page.content().size());
        verify(adapter).projectPage(eq(TestEntity.class), eq(CursorProjection.class), any(), any());
        verify(queryExecutionStrategy, never())
                .findAll(
                        any(),
                        org.mockito.ArgumentMatchers.any(
                                org.springframework.data.domain.Pageable.class));
    }

    private static final class TestEntity {
        private UUID id;
        private String value;

        private TestEntity(UUID id, String value) {
            this.id = id;
            this.value = value;
        }
    }

    private record TestRequest(String value) {}

    private record TestResponse(String value) {}

    private record CursorProjection(UUID id, String value) {}

    private static final class TestMapper
            implements EntityMapper<TestEntity, TestRequest, TestResponse, TestResponse, UUID> {

        @Override
        public TestEntity fromRequest(TestRequest request) {
            return new TestEntity(null, request.value());
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
            return new TestResponse(entity.value);
        }

        @Override
        public TestResponse toRef(TestEntity entity) {
            return new TestResponse(entity.value);
        }

        @Override
        public UUID getIdFromRequest(TestRequest request) {
            return null;
        }
    }

    private static final class TestService
            extends AbstractCrudService<TestEntity, TestRequest, TestResponse, TestResponse, UUID> {
        private TestService(
                JpaRepository<TestEntity, UUID> repository,
                EntityMapper<TestEntity, TestRequest, TestResponse, TestResponse, UUID> mapper) {
            super(repository, mapper, TestEntity.class, TestResponse.class, TestResponse.class);
        }
    }
}

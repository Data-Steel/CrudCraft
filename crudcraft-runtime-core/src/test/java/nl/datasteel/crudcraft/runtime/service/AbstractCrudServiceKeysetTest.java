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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import nl.datasteel.crudcraft.runtime.service.strategy.QueryExecutionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class AbstractCrudServiceKeysetTest {

    private QueryExecutionStrategy<TestEntity> queryExecutionStrategy;
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
        service = new TestService(repository, new TestMapper());
        service.setQueryExecutorForTests(queryExecutionStrategy);
    }

    @Test
    void keysetRejectsCursorSortFieldMismatch() {
        String cursor = encodeCursor("status", "ASC", "active", UUID.randomUUID().toString());
        assertThrows(
                BadRequestException.class,
                () ->
                        service.findAllKeyset(
                                null, 2, cursor, Sort.by("name"), TestProjection.class));
    }

    @Test
    void keysetRejectsCursorSortDirectionMismatch() {
        String cursor = encodeCursor("name", "ASC", "alice", UUID.randomUUID().toString());
        assertThrows(
                BadRequestException.class,
                () ->
                        service.findAllKeyset(
                                null,
                                2,
                                cursor,
                                Sort.by(Sort.Order.desc("name")),
                                TestProjection.class));
    }

    @Test
    void keysetUsesProjectionBoundaryCursorWhenPossible() {
        when(queryExecutionStrategy.findAll(
                        any(),
                        org.mockito.ArgumentMatchers.any(
                                org.springframework.data.domain.Pageable.class)))
                .thenReturn(
                        new PageImpl<>(
                                List.of(
                                        new TestEntity(UUID.randomUUID(), "alice"),
                                        new TestEntity(UUID.randomUUID(), "bob"),
                                        new TestEntity(UUID.randomUUID(), "charlie"))));

        KeysetPage<TestProjection> page =
                service.findAllKeyset(null, 2, null, Sort.by("name"), TestProjection.class);

        assertNotNull(page.nextCursor());
        verify(queryExecutionStrategy, times(1))
                .findAll(
                        any(),
                        org.mockito.ArgumentMatchers.any(
                                org.springframework.data.domain.Pageable.class));
    }

    @Test
    void keysetAppliesIdTieBreakerToStableSort() {
        when(queryExecutionStrategy.findAll(
                        any(),
                        org.mockito.ArgumentMatchers.any(
                                org.springframework.data.domain.Pageable.class)))
                .thenReturn(
                        new PageImpl<>(
                                List.of(
                                        new TestEntity(UUID.randomUUID(), "alice"),
                                        new TestEntity(UUID.randomUUID(), "alice"),
                                        new TestEntity(UUID.randomUUID(), "bob"))));

        service.findAllKeyset(null, 2, null, Sort.by("name"), TestProjection.class);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(queryExecutionStrategy).findAll(any(), pageableCaptor.capture());
        List<String> sortProperties =
                pageableCaptor.getValue().getSort().stream().map(Sort.Order::getProperty).toList();
        assertEquals(List.of("name", "id"), sortProperties);
    }

    @Test
    void generatedCursorCanBeUsedForNextPageWithSameSort() {
        when(queryExecutionStrategy.findAll(
                        any(),
                        org.mockito.ArgumentMatchers.any(
                                org.springframework.data.domain.Pageable.class)))
                .thenReturn(
                        new PageImpl<>(
                                List.of(
                                        new TestEntity(UUID.randomUUID(), "alice"),
                                        new TestEntity(UUID.randomUUID(), "bob"),
                                        new TestEntity(UUID.randomUUID(), "charlie"))))
                .thenReturn(new PageImpl<>(List.of(new TestEntity(UUID.randomUUID(), "charlie"))));

        KeysetPage<TestProjection> firstPage =
                service.findAllKeyset(null, 2, null, Sort.by("name"), TestProjection.class);

        assertNotNull(firstPage.nextCursor());

        service.findAllKeyset(
                null, 2, firstPage.nextCursor(), Sort.by("name"), TestProjection.class);

        verify(queryExecutionStrategy, times(2))
                .findAll(
                        any(),
                        org.mockito.ArgumentMatchers.any(
                                org.springframework.data.domain.Pageable.class));
    }

    private String encodeCursor(
            String property, String direction, String sortValue, String idValue) {
        String payload = property + "\n" + direction + "\n" + sortValue + "\n" + idValue;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private static final class TestEntity {
        private UUID id;
        private String name;

        private TestEntity() {}

        private TestEntity(UUID id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private record TestProjection(UUID id, String name) {}

    private record TestRequest(String value) {}

    private static final class TestMapper
            implements EntityMapper<TestEntity, TestRequest, TestProjection, TestProjection, UUID> {
        @Override
        public TestEntity fromRequest(TestRequest request) {
            return new TestEntity();
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
        public TestProjection toResponse(TestEntity entity) {
            return new TestProjection(entity.id, entity.name);
        }

        @Override
        public TestProjection toRef(TestEntity entity) {
            return new TestProjection(entity.id, entity.name);
        }

        @Override
        public UUID getIdFromRequest(TestRequest request) {
            return null;
        }
    }

    private static final class TestService
            extends AbstractCrudService<
                    TestEntity, TestRequest, TestProjection, TestProjection, UUID> {
        private TestService(
                JpaRepository<TestEntity, UUID> repository,
                EntityMapper<TestEntity, TestRequest, TestProjection, TestProjection, UUID>
                        mapper) {
            super(repository, mapper, TestEntity.class, TestProjection.class, TestProjection.class);
        }
    }
}

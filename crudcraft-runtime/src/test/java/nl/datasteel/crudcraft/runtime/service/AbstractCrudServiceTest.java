/**
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.runtime.service;

import nl.datasteel.crudcraft.runtime.Identified;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.querydsl.core.types.Predicate;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import nl.datasteel.crudcraft.runtime.search.SearchRequest;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import nl.datasteel.crudcraft.runtime.service.strategy.QueryExecutionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

class AbstractCrudServiceTest {

    static class Entity { Long id; }
    static class Dto { Long id; }
    static class Resp { Long id; }
    static class Ref { Long id; }

    @SuppressWarnings("unchecked")
    JpaRepository<Entity, Long> repo = mock(JpaRepository.class, withSettings().extraInterfaces(JpaSpecificationExecutor.class));
    @SuppressWarnings("unchecked")
    EntityMapper<Entity, Dto, Resp, Ref, Long> mapper = mock(EntityMapper.class);
    QueryExecutionStrategy<Entity> executor = mock(QueryExecutionStrategy.class);

    static class TestService extends AbstractCrudService<Entity, Dto, Resp, Ref, Long> {
        List<RowSecurityHandler<?>> handlers;
        TestService(JpaRepository<Entity, Long> r, EntityMapper<Entity, Dto, Resp, Ref, Long> m) {
            super(r, m, Entity.class, Resp.class, Ref.class);
        }
        void setExecutor(QueryExecutionStrategy<Entity> exec) throws Exception {
            Field f = AbstractCrudService.class.getDeclaredField("queryExecutor");
            f.setAccessible(true);
            f.set(this, exec);
        }
        @Override
        protected List<RowSecurityHandler<?>> rowSecurityHandlers() { return handlers; }
        @Override protected void preSave(Entity e, Dto d) { /* hook for coverage */ }
        @Override protected void postSave(Entity e) { }
        @Override protected void preDelete(Entity e) { }
        @Override protected void postDelete(Entity e) { }
    }

    TestService service;

    @BeforeEach
    void setup() throws Exception {
        service = new TestService(repo, mapper);
        service.setExecutor(executor);
    }

    @Test
    void constructorWithoutQuerydslUsesJpaStrategy() throws Exception {
        TestService s = new TestService(repo, mapper);
        Field f = AbstractCrudService.class.getDeclaredField("queryExecutor");
        f.setAccessible(true);
        Object actual = f.get(s);
        assertTrue(actual instanceof nl.datasteel.crudcraft.runtime.service.strategy.JpaSpecificationExecutionStrategy);
    }

    @Test
    void constructorWithQuerydslUsesQuerydslStrategy() throws Exception {
        JpaRepository<Entity, Long> repo2 = mock(JpaRepository.class,
                withSettings().extraInterfaces(org.springframework.data.querydsl.QuerydslPredicateExecutor.class,
                        JpaSpecificationExecutor.class));
        TestService s = new TestService(repo2, mapper);
        Field f = AbstractCrudService.class.getDeclaredField("queryExecutor");
        f.setAccessible(true);
        Object actual = f.get(s);
        assertTrue(actual instanceof nl.datasteel.crudcraft.runtime.service.strategy.QuerydslExecutionStrategy);
    }

    @Test
    void findAllDelegatesToExecutorWithSecurity() {
        Predicate pred = mock(Predicate.class);
        Specification<Entity> spec = (root, q, cb) -> cb.conjunction();
        RowSecurityHandler<Entity> handler = mock(RowSecurityHandler.class);
        when(handler.rowFilterPredicate()).thenReturn(pred);
        when(handler.rowFilter()).thenReturn(spec);
        service.handlers = List.of(handler);
        when(executor.findAll(eq(pred), eq(spec), eq(PageRequest.of(0,1)), eq(Resp.class)))
                .thenReturn(new PageImpl<>(List.of(new Resp())));
        service.findAll(PageRequest.of(0,1), null);
        verify(executor).findAll(eq(pred), eq(spec), any(PageRequest.class), eq(Resp.class));
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(executor.findOne(any(), any(), eq(Resp.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void findByIdOptionalReturnsValue() {
        Resp resp = new Resp();
        when(executor.findOne(any(), any(), eq(Resp.class))).thenReturn(Optional.of(resp));
        assertSame(resp, service.findByIdOptional(1L).orElse(null));
    }

    @Test
    void findReferenceByIdChecksExistence() {
        when(executor.exists(any(), any())).thenReturn(true);
        Entity ref = new Entity();
        when(repo.getReferenceById(1L)).thenReturn(ref);
        assertSame(ref, service.findReferenceById(1L));
    }

    @Test
    void findReferenceByIdThrowsWhenMissing() {
        when(executor.exists(any(), any())).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.findReferenceById(1L));
    }

    @Test
    void createUsesMapperAndRepository() {
        Entity entity = new Entity();
        Resp resp = new Resp();
        when(mapper.fromRequest(any())).thenReturn(entity);
        when(repo.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(resp);
        assertSame(resp, service.create(new Dto()));
    }

    @Test
    void updateCallsFieldSecurityAndMapper() {
        Entity entity = new Entity();
        Resp resp = new Resp();
        when(executor.findOne(any(), any())).thenReturn(Optional.of(entity));
        when(mapper.fromRequest(any())).thenReturn(entity);
        when(mapper.toResponse(any())).thenReturn(resp);
        try (MockedStatic<nl.datasteel.crudcraft.runtime.security.FieldSecurityUtil> fs =
                     mockStatic(nl.datasteel.crudcraft.runtime.security.FieldSecurityUtil.class)) {
            when(repo.save(any())).thenReturn(entity);
            service.update(1L, new Dto());
            fs.verify(() -> nl.datasteel.crudcraft.runtime.security.FieldSecurityUtil.filterWrite(any(), any()));
        }
    }

    @Test
    void deleteUsesRepository() {
        Entity entity = new Entity();
        when(executor.findOne(any(), any())).thenReturn(Optional.of(entity));
        service.delete(1L);
        verify(repo).delete(entity);
    }

    @Test
    void deleteThrowsWhenEntityMissing() {
        when(executor.findOne(any(), any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    void existsByIdDelegatesToExecutor() {
        when(executor.exists(any(), any())).thenReturn(true);
        assertTrue(service.existsById(1L));
    }

    @Test
    void existsByIdReturnsFalse() {
        when(executor.exists(any(), any())).thenReturn(false);
        assertFalse(service.existsById(1L));
    }

    @Test
    void existsByIdAppliesRowSecurity() {
        Predicate rowPred = mock(Predicate.class);
        Specification<Entity> rowSpec = (root, q, cb) -> cb.conjunction();
        RowSecurityHandler<Entity> handler = mock(RowSecurityHandler.class);
        when(handler.rowFilterPredicate()).thenReturn(rowPred);
        when(handler.rowFilter()).thenReturn(rowSpec);
        service.handlers = List.of(handler);
        when(executor.exists(any(), any())).thenReturn(true);

        assertTrue(service.existsById(1L));
        verify(handler).rowFilterPredicate();
        verify(handler).rowFilter();
        verify(executor).exists(any(Predicate.class), any(Specification.class));
    }

    @Test
    void countDelegatesToExecutor() {
        when(executor.count(any(), any())).thenReturn(5L);
        assertEquals(5L, service.count());
    }

    @Test
    void findAllWithoutSecurityUsesNulls() {
        when(executor.findAll(isNull(), any(), any(PageRequest.class), eq(Resp.class)))
                .thenReturn(new PageImpl<>(List.of()));
        service.findAll(PageRequest.of(0, 1), null);
        verify(executor).findAll(isNull(), any(), any(PageRequest.class), eq(Resp.class));
    }

    @Test
    void deleteAllByIdsStopsOnMissingId() {
        Entity found = new Entity();
        when(executor.findOne(any(), any())).thenReturn(Optional.of(found)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteAllByIds(List.of(1L,2L)));
        verify(repo).delete(found);
    }

    @Test
    void searchCombinesSearchAndSecurity() {
        Predicate searchPred = mock(Predicate.class);
        Specification<Entity> searchSpec = (root, q, cb) -> cb.disjunction();
        SearchRequest<Entity> request = new SearchRequest<>() {
            @Override public Predicate toPredicate() { return searchPred; }
            @Override public Specification<Entity> toSpecification() { return searchSpec; }
        };
        Predicate rowPred = mock(Predicate.class);
        Specification<Entity> rowSpec = (root, q, cb) -> cb.conjunction();
        RowSecurityHandler<Entity> handler = mock(RowSecurityHandler.class);
        when(handler.rowFilterPredicate()).thenReturn(rowPred);
        when(handler.rowFilter()).thenReturn(rowSpec);
        service.handlers = List.of(handler);
        when(executor.findAll(any(), any(), any(PageRequest.class), eq(Resp.class)))
                .thenReturn(new PageImpl<>(List.of(new Resp())));

        service.search(request, PageRequest.of(0,1));

        verify(handler).rowFilterPredicate();
        verify(handler).rowFilter();
        verify(executor).findAll(
                nullable(Predicate.class),
                any(Specification.class),
                any(PageRequest.class),
                eq(Resp.class)
        );
    }

    @Test
    void searchRefCombinesSearchAndSecurity() {
        Predicate searchPred = mock(Predicate.class);
        Specification<Entity> searchSpec = (root, q, cb) -> cb.disjunction();
        SearchRequest<Entity> request = new SearchRequest<>() {
            @Override public Predicate toPredicate() { return searchPred; }
            @Override public Specification<Entity> toSpecification() { return searchSpec; }
        };
        Predicate rowPred = mock(Predicate.class);
        Specification<Entity> rowSpec = (root, q, cb) -> cb.conjunction();
        RowSecurityHandler<Entity> handler = mock(RowSecurityHandler.class);
        when(handler.rowFilterPredicate()).thenReturn(rowPred);
        when(handler.rowFilter()).thenReturn(rowSpec);
        service.handlers = List.of(handler);
        when(executor.findAll(any(), any(), any(PageRequest.class), eq(Ref.class)))
                .thenReturn(new PageImpl<>(List.of(new Ref())));

        service.searchRef(request, PageRequest.of(0,1));

        verify(handler).rowFilterPredicate();
        verify(handler).rowFilter();
        verify(executor).findAll(
                nullable(Predicate.class),
                any(Specification.class),
                any(PageRequest.class),
                eq(Ref.class)
        );
    }

    @Test
    void findByIdsDelegatesWithPredicate() {
        when(executor.findAll(any(), any(), eq(Resp.class))).thenReturn(List.of(new Resp()));
        List<Resp> result = service.findByIds(List.of(1L, 2L));
        assertEquals(1, result.size());
        verify(executor).findAll(any(Predicate.class), any(Specification.class), eq(Resp.class));
    }

    @Test
    void createAllInvokesSecurityAndHooks() throws Exception {
        RowSecurityHandler<Entity> handler = mock(RowSecurityHandler.class);
        Dto d1 = new Dto();
        Dto d2 = new Dto();
        Entity e1 = new Entity();
        Entity e2 = new Entity();
        when(mapper.fromRequest(d1)).thenReturn(e1);
        when(mapper.fromRequest(d2)).thenReturn(e2);
        when(repo.saveAll(List.of(e1, e2))).thenReturn(List.of(e1, e2));
        when(mapper.toResponse(e1)).thenReturn(new Resp());
        when(mapper.toResponse(e2)).thenReturn(new Resp());
        TestService spySvc = spy(new TestService(repo, mapper));
        spySvc.setExecutor(executor);
        spySvc.handlers = List.of(handler);
        List<Resp> res = spySvc.createAll(List.of(d1, d2));
        assertEquals(2, res.size());
        verify(handler).apply(e1);
        verify(handler).apply(e2);
        verify(spySvc).preSave(e1, null);
        verify(spySvc).preSave(e2, null);
        verify(spySvc).postSave(e1);
        verify(spySvc).postSave(e2);
    }

    @Test
    void upsertCreatesWhenIdMissing() throws Exception {
        TestService spySvc = spy(new TestService(repo, mapper));
        spySvc.setExecutor(executor);
        Dto dto = new Dto();
        when(mapper.getIdFromRequest(dto)).thenReturn(null);
        Resp resp = new Resp();
        doReturn(resp).when(spySvc).create(dto);
        assertSame(resp, spySvc.upsert(dto));
        verify(spySvc).create(dto);
        verify(spySvc, never()).update(any(), any());
    }

    @Test
    void upsertUpdatesWhenIdExists() throws Exception {
        TestService spySvc = spy(new TestService(repo, mapper));
        spySvc.setExecutor(executor);
        Dto dto = new Dto();
        when(mapper.getIdFromRequest(dto)).thenReturn(5L);
        doReturn(true).when(spySvc).existsById(5L);
        Resp resp = new Resp();
        doReturn(resp).when(spySvc).update(5L, dto);
        assertSame(resp, spySvc.upsert(dto));
        verify(spySvc).update(5L, dto);
    }

    @Test
    void upsertAllDelegatesToUpsert() throws Exception {
        TestService spySvc = spy(new TestService(repo, mapper));
        spySvc.setExecutor(executor);
        Dto d1 = new Dto();
        Dto d2 = new Dto();
        doReturn(new Resp()).when(spySvc).upsert(any());
        List<Resp> res = spySvc.upsertAll(List.of(d1, d2));
        assertEquals(2, res.size());
        verify(spySvc, times(2)).upsert(any());
    }

    @Test
    void updateAllDelegatesToUpdate() throws Exception {
        TestService spySvc = spy(new TestService(repo, mapper));
        spySvc.setExecutor(executor);
        Identified<Long, Dto> a = new Identified<>(1L, new Dto());
        Identified<Long, Dto> b = new Identified<>(2L, new Dto());
        doReturn(new Resp()).when(spySvc).update(eq(1L), any());
        doReturn(new Resp()).when(spySvc).update(eq(2L), any());
        List<Resp> res = spySvc.updateAll(List.of(a, b));
        assertEquals(2, res.size());
        verify(spySvc).update(1L, a.getData());
        verify(spySvc).update(2L, b.getData());
    }

    @Test
    void patchAllDelegatesToPatch() throws Exception {
        TestService spySvc = spy(new TestService(repo, mapper));
        spySvc.setExecutor(executor);
        Identified<Long, Dto> a = new Identified<>(1L, new Dto());
        Identified<Long, Dto> b = new Identified<>(2L, new Dto());
        doReturn(new Resp()).when(spySvc).patch(eq(1L), any());
        doReturn(new Resp()).when(spySvc).patch(eq(2L), any());
        List<Resp> res = spySvc.patchAll(List.of(a, b));
        assertEquals(2, res.size());
        verify(spySvc).patch(1L, a.getData());
        verify(spySvc).patch(2L, b.getData());
    }
}

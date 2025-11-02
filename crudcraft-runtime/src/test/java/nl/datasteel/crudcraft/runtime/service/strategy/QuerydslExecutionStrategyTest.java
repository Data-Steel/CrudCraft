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

package nl.datasteel.crudcraft.runtime.service.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import java.util.List;
import java.util.Optional;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

class QuerydslExecutionStrategyTest {

    static class Entity { Long id; String name; }
    static class Response { }
    static class Ref { }
    interface NameOnly { String getName(); }

    QuerydslPredicateExecutor<Entity> repo = mock(QuerydslPredicateExecutor.class);
    JpaSpecificationExecutor<Entity> specRepo = mock(JpaSpecificationExecutor.class);
    @SuppressWarnings("unchecked")
    EntityMapper<Entity, Object, Response, Ref, Object> mapper = mock(EntityMapper.class);
    Specification<Entity> spec = (root, q, cb) -> cb.conjunction();
    Predicate pred = Expressions.asBoolean(true).isTrue();

    QuerydslExecutionStrategy<Entity, Response, Ref> strategy =
            new QuerydslExecutionStrategy<>(repo, mapper, Response.class, Ref.class, specRepo);

    @Test
    void findAllPageUsesSpecRepoWhenPredicateNull() {
        Page<Entity> page = new PageImpl<>(List.of(new Entity()));
        when(specRepo.findAll(spec, PageRequest.of(0,1))).thenReturn(page);
        Page<Entity> result = strategy.findAll(null, spec, PageRequest.of(0,1));
        assertSame(page, result);
        verify(specRepo).findAll(spec, PageRequest.of(0,1));
        verifyNoInteractions(repo);
    }

    @Test
    void findAllPageUsesRepoWhenPredicatePresent() {
        Page<Entity> page = new PageImpl<>(List.of(new Entity()));
        when(repo.findAll(any(Predicate.class), eq(PageRequest.of(0,1)))).thenReturn(page);
        Page<Entity> result = strategy.findAll(pred, spec, PageRequest.of(0,1));
        assertSame(page, result);
    }

    @Test
    void findAllListUsesSpecRepoWhenPredicateNull() {
        when(specRepo.findAll(spec)).thenReturn(List.of(new Entity()));
        List<Entity> list = strategy.findAll(null, spec);
        assertEquals(1, list.size());
    }

    @Test
    void findAllListUsesRepoWhenPredicatePresent() {
        when(repo.findAll(any(Predicate.class))).thenReturn(List.of(new Entity()));
        List<Entity> list = strategy.findAll(pred, spec);
        assertEquals(1, list.size());
    }

    @Test
    void findAllPageProjectionInterfaceUsesRepoFindBy() {
        Page<NameOnly> page = new PageImpl<>(List.of(mock(NameOnly.class)));
        when(repo.findBy(any(), any())).thenReturn(page);
        Page<NameOnly> result = strategy.findAll(pred, spec, PageRequest.of(0,1), NameOnly.class);
        assertSame(page, result);
    }

    @Test
    void findAllPageProjectionResponseUsesMapper() {
        Page<Entity> page = new PageImpl<>(List.of(new Entity()));
        when(repo.findAll(any(Predicate.class), eq(PageRequest.of(0,1)))).thenReturn(page);
        when(mapper.toResponse(any())).thenReturn(new Response());
        Page<Response> result = strategy.findAll(pred, spec, PageRequest.of(0,1), Response.class);
        assertEquals(1, result.getTotalElements());
        verify(mapper).toResponse(any());
    }

    @Test
    void findAllPageProjectionRefUsesMapper() {
        Page<Entity> page = new PageImpl<>(List.of(new Entity()));
        when(repo.findAll(any(Predicate.class), eq(PageRequest.of(0,1)))).thenReturn(page);
        when(mapper.toRef(any())).thenReturn(new Ref());
        Page<Ref> result = strategy.findAll(pred, spec, PageRequest.of(0,1), Ref.class);
        assertEquals(1, result.getTotalElements());
        verify(mapper).toRef(any());
    }

    @Test
    void findAllPageProjectionUnsupportedThrows() {
        Page<Entity> page = new PageImpl<>(List.of(new Entity()));
        when(repo.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);
        assertThrows(UnsupportedOperationException.class,
                () -> strategy.findAll(pred, spec, PageRequest.of(0,1), String.class));
    }

    @Test
    void findAllListProjectionInterfaceUsesRepoFindBy() {
        when(repo.findBy(any(), any())).thenReturn(List.of(mock(NameOnly.class)));
        List<NameOnly> result = strategy.findAll(pred, spec, NameOnly.class);
        assertEquals(1, result.size());
    }

    @Test
    void findAllListProjectionResponseUsesMapper() {
        when(repo.findAll(any(Predicate.class))).thenReturn(List.of(new Entity()));
        when(mapper.toResponse(any())).thenReturn(new Response());
        List<Response> result = strategy.findAll(pred, spec, Response.class);
        assertEquals(1, result.size());
    }

    @Test
    void findAllListProjectionRefUsesMapper() {
        when(repo.findAll(any(Predicate.class))).thenReturn(List.of(new Entity()));
        when(mapper.toRef(any())).thenReturn(new Ref());
        List<Ref> result = strategy.findAll(pred, spec, Ref.class);
        assertEquals(1, result.size());
    }

    @Test
    void findAllListProjectionUnsupportedThrows() {
        when(repo.findAll(any(Predicate.class))).thenReturn(List.of(new Entity()));
        assertThrows(UnsupportedOperationException.class,
                () -> strategy.findAll(pred, spec, String.class));
    }

    @Test
    void findOneUsesSpecRepoWhenPredicateNull() {
        when(specRepo.findOne(spec)).thenReturn(Optional.of(new Entity()));
        assertTrue(strategy.findOne(null, spec).isPresent());
    }

    @Test
    void findOneUsesRepoWhenPredicatePresent() {
        when(repo.findOne(any())).thenReturn(Optional.of(new Entity()));
        assertTrue(strategy.findOne(pred, spec).isPresent());
    }

    @Test
    void findOneProjectionInterfaceUsesRepoFindBy() {
        when(repo.findBy(any(), any())).thenReturn(Optional.of(mock(NameOnly.class)));
        Optional<NameOnly> result = strategy.findOne(pred, spec, NameOnly.class);
        assertTrue(result.isPresent());
    }

    @Test
    void findOneProjectionResponseUsesMapper() {
        when(repo.findOne(any())).thenReturn(Optional.of(new Entity()));
        when(mapper.toResponse(any())).thenReturn(new Response());
        Optional<Response> result = strategy.findOne(pred, spec, Response.class);
        assertTrue(result.isPresent());
        verify(mapper).toResponse(any());
    }

    @Test
    void findOneProjectionUnsupportedThrows() {
        when(repo.findOne(any())).thenReturn(Optional.of(new Entity()));
        assertThrows(UnsupportedOperationException.class,
                () -> strategy.findOne(pred, spec, String.class));
    }

    @Test
    void existsUsesSpecRepoWhenPredicateNull() {
        when(specRepo.count(spec)).thenReturn(1L);
        assertTrue(strategy.exists(null, spec));
    }

    @Test
    void existsUsesRepoWhenPredicatePresent() {
        when(repo.exists(any())).thenReturn(true);
        assertTrue(strategy.exists(pred, spec));
    }

    @Test
    void countUsesSpecRepoWhenPredicateNull() {
        when(specRepo.count(spec)).thenReturn(2L);
        assertEquals(2L, strategy.count(null, spec));
    }

    @Test
    void countUsesRepoWhenPredicatePresent() {
        when(repo.count(any())).thenReturn(3L);
        assertEquals(3L, strategy.count(pred, spec));
    }
}

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
import static org.mockito.Mockito.*;

import com.querydsl.core.types.Predicate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

class JpaSpecificationExecutionStrategyTest {

    JpaSpecificationExecutor<Object> repo = mock(JpaSpecificationExecutor.class);
    JpaSpecificationExecutionStrategy<Object> strategy = new JpaSpecificationExecutionStrategy<>(repo);
    Specification<Object> spec = (root, query, cb) -> cb.conjunction();
    Predicate pred = mock(Predicate.class);

    @Test
    void findAllPageDelegatesToRepository() {
        Page<Object> page = new PageImpl<>(List.of("a"));
        when(repo.findAll(spec, PageRequest.of(0, 10))).thenReturn(page);
        Page<Object> result = strategy.findAll(pred, spec, PageRequest.of(0, 10));
        assertSame(page, result);
    }

    @Test
    void findAllListDelegatesToRepository() {
        when(repo.findAll(spec)).thenReturn(List.of("a"));
        List<Object> list = strategy.findAll(pred, spec);
        assertEquals(1, list.size());
    }

    @Test
    void findAllPageProjectionUsesFindBy() {
        Page<String> page = new PageImpl<>(List.of("a"));
        when(repo.findBy(eq(spec), any())).thenReturn(page);
        Page<String> result = strategy.findAll(pred, spec, PageRequest.of(0, 1), String.class);
        assertSame(page, result);
    }

    @Test
    void findAllListProjectionUsesFindBy() {
        when(repo.findBy(eq(spec), any())).thenReturn(List.of("a"));
        List<String> result = strategy.findAll(pred, spec, String.class);
        assertEquals(1, result.size());
    }

    @Test
    void findOneDelegatesToRepository() {
        when(repo.findOne(spec)).thenReturn(Optional.of("a"));
        Optional<Object> result = strategy.findOne(pred, spec);
        assertTrue(result.isPresent());
    }

    @Test
    void findOneProjectionUsesFindBy() {
        when(repo.findBy(eq(spec), any())).thenReturn(Optional.of("a"));
        Optional<String> result = strategy.findOne(pred, spec, String.class);
        assertEquals("a", result.orElse(null));
    }

    @Test
    void existsReturnsTrueWhenCountPositive() {
        when(repo.count(spec)).thenReturn(1L);
        assertTrue(strategy.exists(pred, spec));
    }

    @Test
    void existsReturnsFalseWhenCountZero() {
        when(repo.count(spec)).thenReturn(0L);
        assertFalse(strategy.exists(pred, spec));
    }

    @Test
    void countDelegatesToRepository() {
        when(repo.count(spec)).thenReturn(5L);
        assertEquals(5L, strategy.count(pred, spec));
    }
}

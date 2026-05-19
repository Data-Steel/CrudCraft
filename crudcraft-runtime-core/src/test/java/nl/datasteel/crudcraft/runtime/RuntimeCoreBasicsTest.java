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

package nl.datasteel.crudcraft.runtime;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import nl.datasteel.crudcraft.runtime.controller.response.ErrorResponse;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import nl.datasteel.crudcraft.runtime.exception.BulkOperationException;
import nl.datasteel.crudcraft.runtime.exception.CrudCraftRuntimeException;
import nl.datasteel.crudcraft.runtime.exception.DataIntegrityException;
import nl.datasteel.crudcraft.runtime.exception.DuplicateResourceException;
import nl.datasteel.crudcraft.runtime.exception.ForbiddenException;
import nl.datasteel.crudcraft.runtime.exception.MapperException;
import nl.datasteel.crudcraft.runtime.exception.NotImplementedException;
import nl.datasteel.crudcraft.runtime.exception.OperationNotAllowedException;
import nl.datasteel.crudcraft.runtime.exception.PreconditionFailedException;
import nl.datasteel.crudcraft.runtime.exception.RelationshipException;
import nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException;
import nl.datasteel.crudcraft.runtime.exception.TooManyRequestsException;
import nl.datasteel.crudcraft.runtime.exception.UnauthorizedException;
import nl.datasteel.crudcraft.runtime.service.BulkResult;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
import nl.datasteel.crudcraft.runtime.service.strategy.JpaSpecificationExecutionStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class RuntimeCoreBasicsTest {

    @Test
    void identifiedBeanGettersAndSettersWork() {
        Identified<Long, String> identified = new Identified<>();
        identified.setId(11L);
        identified.setData("payload");
        assertEquals(11L, identified.getId());
        assertEquals("payload", identified.getData());

        Identified<Long, String> fromCtor = new Identified<>(12L, "data");
        assertEquals(12L, fromCtor.getId());
        assertEquals("data", fromCtor.getData());
    }

    @Test
    void paginatedResponseMakesDefensiveCopyAndHandlesNullContent() {
        List<String> mutable = new ArrayList<>();
        mutable.add("a");
        PaginatedResponse<String> response =
                new PaginatedResponse<>(mutable, 1, 2, 3, 4, false, true);
        mutable.add("b");
        assertEquals(1, response.content().size());
        assertEquals("a", response.content().getFirst());
        assertThrows(UnsupportedOperationException.class, () -> response.content().add("x"));

        PaginatedResponse<String> nullContent =
                new PaginatedResponse<>(null, 0, 1, 1, 0, true, true);
        assertTrue(nullContent.content().isEmpty());
        assertThrows(
                IllegalArgumentException.class,
                () -> new PaginatedResponse<>(List.of(), -1, 1, 1, 0, false, false));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PaginatedResponse<>(List.of(), 0, -1, 1, 0, false, false));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PaginatedResponse<>(List.of(), 0, 1, -1, 0, false, false));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PaginatedResponse<>(List.of(), 0, 1, 1, -1, false, false));
    }

    @Test
    void errorResponseAndExceptionConstructorsExposeMessages() {
        Throwable cause = new IllegalStateException("cause");
        assertEquals("bad", new BadRequestException("bad").getMessage());
        assertEquals("bad2", new BadRequestException("bad2", cause).getMessage());
        assertEquals("bulk", new BulkOperationException("bulk", List.of(cause)).getMessage());
        assertEquals(
                1, new BulkOperationException("bulk", List.of(cause)).getItemExceptions().size());
        assertEquals("base", new TestRuntimeException("base").getMessage());
        assertEquals("base2", new TestRuntimeException("base2", cause).getMessage());
        assertEquals("di", new DataIntegrityException("di", cause).getMessage());
        assertEquals("dup", new DuplicateResourceException("dup").getMessage());
        assertEquals("forbidden", new ForbiddenException("forbidden").getMessage());
        assertEquals("mapper", new MapperException("mapper", cause).getMessage());
        BadRequestException contextualBadRequest =
                new BadRequestException("bad", Map.of("field", "name"));
        assertEquals(Map.of("field", "name"), contextualBadRequest.getContext());
        assertEquals("bad [field=name]", contextualBadRequest.getMessage());
        assertThrows(
                UnsupportedOperationException.class,
                () -> contextualBadRequest.getContext().put("field", "other"));

        MapperException contextualMapperException =
                new MapperException(
                        "mapper", Map.of("operation", "create.fromRequest"), cause);
        assertEquals(
                Map.of("operation", "create.fromRequest"),
                contextualMapperException.getContext());
        assertTrue(contextualMapperException.getMessage().contains("create.fromRequest"));
        assertEquals(cause, contextualMapperException.getCause());

        TestRuntimeException nullContext =
                new TestRuntimeException("base3", (Map<String, String>) null);
        assertTrue(nullContext.getContext().isEmpty());
        assertEquals("base3", nullContext.getMessage());
        assertEquals("nyi", new NotImplementedException("nyi").getMessage());
        assertEquals("op", new OperationNotAllowedException("op").getMessage());
        assertEquals("pre", new PreconditionFailedException("pre").getMessage());
        assertEquals("rel", new RelationshipException("rel", cause).getMessage());
        assertEquals("nf", new ResourceNotFoundException("nf").getMessage());
        assertEquals("rate", new TooManyRequestsException("rate").getMessage());
        assertEquals("unauth", new UnauthorizedException("unauth").getMessage());

        ErrorResponse response =
                new ErrorResponse(400, "Bad Request", "invalid", Instant.now(), "/x");
        assertEquals(400, response.status());
        assertEquals("invalid", response.message());
    }

    @Test
    void bulkResultNormalizesNullsAndDefensivelyCopiesLists() {
        BulkResult<String> empty = new BulkResult<>(null, null);
        assertTrue(empty.succeeded().isEmpty());
        assertTrue(empty.failed().isEmpty());
        assertFalse(empty.hasFailures());

        List<String> successes = new ArrayList<>(List.of("created"));
        List<BulkResult.Failure> failures =
                new ArrayList<>(List.of(new BulkResult.Failure(2, "invalid")));
        BulkResult<String> result = new BulkResult<>(successes, failures);
        successes.add("late");
        failures.clear();

        assertEquals(List.of("created"), result.succeeded());
        assertEquals(1, result.failed().size());
        assertEquals(2, result.failed().getFirst().index());
        assertEquals("invalid", result.failed().getFirst().message());
        assertTrue(result.hasFailures());
        assertThrows(UnsupportedOperationException.class, () -> result.succeeded().add("x"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> result.failed().add(new BulkResult.Failure(3, "conflict")));
        assertTrue(BulkResult.empty().succeeded().isEmpty());
    }

    @Test
    void crudRuntimeExtensionDefaultsAreNoOps() {
        CrudRuntimeExtension<Object, String> extension = new CrudRuntimeExtension<>() {};
        assertEquals("a", extension.afterRead("a"));
        assertEquals("b", extension.beforeCreate("b"));
        assertEquals("c", extension.beforeUpdate("c", new Object()));
        assertEquals(null, extension.readFilter(Object.class));
        extension.beforeSave(new Object());
        extension.beforeDelete(new Object());
    }

    @SuppressWarnings("unchecked")
    @Test
    void jpaSpecificationExecutionStrategyDelegatesToRepository() {
        JpaSpecificationExecutor<Object> repository = mock(JpaSpecificationExecutor.class);
        JpaSpecificationExecutionStrategy<Object> strategy =
                new JpaSpecificationExecutionStrategy<>(repository);
        Specification<Object> spec = (root, query, cb) -> cb.conjunction();
        PageRequest pageable = PageRequest.of(0, 10);

        when(repository.findAll(spec, pageable)).thenReturn(new PageImpl<>(List.of("x")));
        when(repository.findAll(spec)).thenReturn(List.of("x"));
        when(repository.findOne(spec)).thenReturn(java.util.Optional.of("one"));
        when(repository.count(spec)).thenReturn(2L);

        assertEquals(1, strategy.findAll(spec, pageable).getTotalElements());
        assertEquals(1, strategy.findAll(spec).size());
        assertEquals("one", strategy.findOne(spec).orElseThrow());
        assertEquals(2L, strategy.count(spec));

        verify(repository).findAll(spec, pageable);
        verify(repository).findAll(spec);
        verify(repository).findOne(spec);
        verify(repository).count(spec);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void jpaSpecificationExecutionStrategyCoversProjectionAndNullSpecPaths() {
        JpaSpecificationExecutor<Object> repository = mock(JpaSpecificationExecutor.class);
        JpaSpecificationExecutionStrategy<Object> strategy =
                new JpaSpecificationExecutionStrategy<>(repository);
        PageRequest pageable = PageRequest.of(0, 3);

        when(repository.findBy(
                        org.mockito.ArgumentMatchers.<Specification<Object>>any(),
                        any(Function.class)))
                .thenReturn(new PageImpl<>(List.of("p")))
                .thenReturn(List.of("l"))
                .thenReturn(Optional.of("o"))
                .thenReturn(Optional.of("e"));

        assertEquals(1, strategy.findAll(null, pageable, String.class).getTotalElements());
        assertEquals(1, strategy.findAll(null, String.class).size());
        assertEquals("o", strategy.findOne(null, String.class).orElseThrow());
        assertTrue(strategy.exists(null));

        verify(repository, org.mockito.Mockito.times(4))
                .findBy(
                        org.mockito.ArgumentMatchers.<Specification<Object>>any(),
                        any(Function.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void jpaSpecificationExecutionStrategyExecutesProjectionFunctions() {
        JpaSpecificationExecutor<Object> repository = mock(JpaSpecificationExecutor.class);
        JpaSpecificationExecutionStrategy<Object> strategy =
                new JpaSpecificationExecutionStrategy<>(repository);
        Specification<Object> spec = (root, query, cb) -> cb.conjunction();
        PageRequest pageable = PageRequest.of(0, 2);

        SpecificationFluentQuery<Object> baseQuery =
                (SpecificationFluentQuery<Object>) mock(SpecificationFluentQuery.class);
        SpecificationFluentQuery<String> projectionQuery =
                (SpecificationFluentQuery<String>) mock(SpecificationFluentQuery.class);

        when(baseQuery.as(String.class)).thenReturn(projectionQuery);
        when(projectionQuery.page(pageable)).thenReturn(new PageImpl<>(List.of("p")));
        when(projectionQuery.all()).thenReturn(List.of("l"));
        when(projectionQuery.first()).thenReturn(Optional.of("first"));
        when(baseQuery.first()).thenReturn(Optional.of("exists"));

        when(repository.findBy(
                        org.mockito.ArgumentMatchers.<Specification<Object>>any(),
                        any(Function.class)))
                .thenAnswer(
                        invocation -> {
                            Function function = invocation.getArgument(1);
                            return function.apply(baseQuery);
                        });

        assertEquals(1, strategy.findAll(spec, pageable, String.class).getTotalElements());
        assertEquals(1, strategy.findAll(spec, String.class).size());
        assertEquals("first", strategy.findOne(spec, String.class).orElseThrow());
        assertTrue(strategy.exists(spec));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void jpaSpecificationExecutionStrategyExistsFalseWhenNoFirstElement() {
        JpaSpecificationExecutor<Object> repository = mock(JpaSpecificationExecutor.class);
        JpaSpecificationExecutionStrategy<Object> strategy =
                new JpaSpecificationExecutionStrategy<>(repository);
        Specification<Object> spec = (root, query, cb) -> cb.conjunction();
        SpecificationFluentQuery<Object> baseQuery =
                (SpecificationFluentQuery<Object>) mock(SpecificationFluentQuery.class);
        when(baseQuery.first()).thenReturn(Optional.empty());
        when(repository.findBy(
                        org.mockito.ArgumentMatchers.<Specification<Object>>any(),
                        any(Function.class)))
                .thenAnswer(
                        invocation -> {
                            Function function = invocation.getArgument(1);
                            return function.apply(baseQuery);
                        });

        assertFalse(strategy.exists(spec));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void jpaSpecificationExecutionStrategyEnsuresNonNullSpecificationWhenMissing() {
        JpaSpecificationExecutor<Object> repository = mock(JpaSpecificationExecutor.class);
        JpaSpecificationExecutionStrategy<Object> strategy =
                new JpaSpecificationExecutionStrategy<>(repository);
        SpecificationFluentQuery<Object> baseQuery =
                (SpecificationFluentQuery<Object>) mock(SpecificationFluentQuery.class);
        SpecificationFluentQuery<String> projectionQuery =
                (SpecificationFluentQuery<String>) mock(SpecificationFluentQuery.class);
        when(baseQuery.as(String.class)).thenReturn(projectionQuery);
        when(projectionQuery.all()).thenReturn(List.of("l"));

        AtomicReference<Specification<Object>> captured = new AtomicReference<>();
        when(repository.findBy(
                        org.mockito.ArgumentMatchers.<Specification<Object>>any(),
                        any(Function.class)))
                .thenAnswer(
                        invocation -> {
                            captured.set(invocation.getArgument(0));
                            Function function = invocation.getArgument(1);
                            return function.apply(baseQuery);
                        });

        strategy.findAll(null, String.class);
        Specification<Object> spec = captured.get();
        assertTrue(spec != null);

        jakarta.persistence.criteria.Root<Object> root =
                mock(jakarta.persistence.criteria.Root.class);
        jakarta.persistence.criteria.CriteriaQuery<?> query =
                mock(jakarta.persistence.criteria.CriteriaQuery.class);
        jakarta.persistence.criteria.CriteriaBuilder cb =
                mock(jakarta.persistence.criteria.CriteriaBuilder.class);
        jakarta.persistence.criteria.Predicate predicate =
                mock(jakarta.persistence.criteria.Predicate.class);
        when(cb.conjunction()).thenReturn(predicate);

        assertEquals(predicate, spec.toPredicate(root, query, cb));
    }

    private static final class TestRuntimeException extends CrudCraftRuntimeException {
        private TestRuntimeException(String message) {
            super(message);
        }

        private TestRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }

        private TestRuntimeException(String message, Map<String, String> context) {
            super(message, context);
        }
    }
}

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

package nl.datasteel.crudcraft.runtime.search;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import nl.datasteel.crudcraft.runtime.service.CrudQueryOperations;
import nl.datasteel.crudcraft.runtime.service.KeysetPage;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class SearchOperationsTest {

    @Test
    void utilityConstructorIsPrivate() throws Exception {
        var constructor = SearchOperations.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertTrue(constructor.newInstance() instanceof SearchOperations);
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsNullServiceForProjectionSearch() {
        assertThrows(
                NullPointerException.class,
                () ->
                        SearchOperations.searchProjection(
                                null, null, PageRequest.of(0, 1), TestProjection.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsNullProjectionForProjectionSearch() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);

        assertThrows(
                NullPointerException.class,
                () -> SearchOperations.searchProjection(service, null, PageRequest.of(0, 1), null));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsNullServiceForKeysetSearch() {
        assertThrows(
                NullPointerException.class,
                () ->
                        SearchOperations.searchKeyset(
                                null, null, 10, null, Sort.unsorted(), TestProjection.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsNullProjectionForKeysetSearch() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);

        assertThrows(
                NullPointerException.class,
                () ->
                        SearchOperations.searchKeyset(
                                service, null, 10, null, Sort.unsorted(), null));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsUnknownSearchFieldBeforeQueryExecution() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> request =
                request(
                        List.of(
                                new SearchRequest.SearchCriterion(
                                        "status", SearchOperator.EQUALS)));

        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () ->
                        SearchOperations.searchProjection(
                                service, request, PageRequest.of(0, 10), TestProjection.class));

        assertTrue(thrown.getMessage().contains("Unsupported search field"));
        assertTrue(thrown.getContext().get("requested").contains("status"));
        assertTrue(thrown.getContext().get("allowed").contains("name"));
        verify(service, never()).findAll(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsUnsupportedOperatorBeforeQueryExecution() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> request =
                request(List.of(new SearchRequest.SearchCriterion("name", SearchOperator.GT)));

        assertThrows(
                BadRequestException.class,
                () ->
                        SearchOperations.searchProjection(
                                service, request, PageRequest.of(0, 10), TestProjection.class));

        verify(service, never()).findAll(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchProjectionPassesNullSpecificationWhenRequestIsNull() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("unsupported"));
        Page<TestProjection> expected = new PageImpl<>(List.of(new TestProjection("A")));
        when(service.findAll(isNull(), eq(pageable), eq(TestProjection.class)))
                .thenReturn(expected);

        Page<TestProjection> actual =
                SearchOperations.searchProjection(service, null, pageable, TestProjection.class);

        assertSame(expected, actual);
        verify(service).findAll(isNull(), eq(pageable), eq(TestProjection.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchProjectionAllowsNullPageableAndUsesUnsortedValidationPath() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> request =
                request(List.of(), Set.of("name"), Set.of("name"), Map.of());
        Page<TestProjection> expected = new PageImpl<>(List.of(new TestProjection("B")));
        when(service.findAll(any(), isNull(), eq(TestProjection.class))).thenReturn(expected);

        Page<TestProjection> actual =
                SearchOperations.searchProjection(service, request, null, TestProjection.class);

        assertSame(expected, actual);
        verify(service).findAll(any(), isNull(), eq(TestProjection.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchProjectionPassesRequestSpecificationWhenProvided() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        Pageable pageable = PageRequest.of(0, 10);
        Specification<TestEntity> specification = (root, query, cb) -> cb.conjunction();
        SearchRequest<TestEntity> request =
                new SearchRequest<>() {
                    @Override
                    public Specification<TestEntity> toSpecification() {
                        return specification;
                    }
                };
        Page<TestProjection> expected = new PageImpl<>(List.of(new TestProjection("B2")));
        when(service.findAll(eq(specification), eq(pageable), eq(TestProjection.class)))
                .thenReturn(expected);

        Page<TestProjection> actual =
                SearchOperations.searchProjection(service, request, pageable, TestProjection.class);

        assertSame(expected, actual);
        verify(service).findAll(eq(specification), eq(pageable), eq(TestProjection.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchDelegatesToProjectionPath() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> request = request(List.of(), Set.of(), Set.of(), Map.of());
        Pageable pageable = PageRequest.of(1, 5);
        Page<TestProjection> expected = new PageImpl<>(List.of(new TestProjection("C")));
        when(service.findAll(any(), eq(pageable), eq(TestProjection.class))).thenReturn(expected);

        Page<TestProjection> actual =
                SearchOperations.search(service, request, pageable, TestProjection.class);

        assertSame(expected, actual);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchRefDelegatesToProjectionPath() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> request = request(List.of(), Set.of(), Set.of(), Map.of());
        Pageable pageable = PageRequest.of(1, 5);
        Page<TestProjection> expected = new PageImpl<>(List.of(new TestProjection("D")));
        when(service.findAll(any(), eq(pageable), eq(TestProjection.class))).thenReturn(expected);

        Page<TestProjection> actual =
                SearchOperations.searchRef(service, request, pageable, TestProjection.class);

        assertSame(expected, actual);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchKeysetDelegatesWhenSortIsAllowed() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> request = request(List.of(), Set.of(), Set.of("name"), Map.of());
        Sort sort = Sort.by(Sort.Order.asc("name"));
        KeysetPage<TestProjection> expected =
                new KeysetPage<>(List.of(new TestProjection("E")), "next-cursor");
        when(service.findAllKeyset(any(), eq(25), eq("cursor"), eq(sort), eq(TestProjection.class)))
                .thenReturn(expected);

        KeysetPage<TestProjection> actual =
                SearchOperations.searchKeyset(
                        service, request, 25, "cursor", sort, TestProjection.class);

        assertSame(expected, actual);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchKeysetAllowsNullSort() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> request = request(List.of(), Set.of(), Set.of("name"), Map.of());
        KeysetPage<TestProjection> expected =
                new KeysetPage<>(List.of(new TestProjection("N")), null);
        when(service.findAllKeyset(any(), eq(5), isNull(), isNull(), eq(TestProjection.class)))
                .thenReturn(expected);

        KeysetPage<TestProjection> actual =
                SearchOperations.searchKeyset(
                        service, request, 5, null, null, TestProjection.class);

        assertSame(expected, actual);
        verify(service).findAllKeyset(any(), eq(5), isNull(), isNull(), eq(TestProjection.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsUnsupportedSortField() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> request = request(List.of(), Set.of(), Set.of("name"), Map.of());

        assertThrows(
                BadRequestException.class,
                () ->
                        SearchOperations.searchProjection(
                                service,
                                request,
                                PageRequest.of(0, 10, Sort.by("createdAt")),
                                TestProjection.class));

        verify(service, never()).findAll(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchKeysetRejectsUnsupportedSortFieldBeforeQueryExecution() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> request = request(List.of(), Set.of(), Set.of("name"), Map.of());

        assertThrows(
                BadRequestException.class,
                () ->
                        SearchOperations.searchKeyset(
                                service,
                                request,
                                25,
                                null,
                                Sort.by("createdAt"),
                                TestProjection.class));

        verify(service, never()).findAllKeyset(any(), anyInt(), any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void allowsSortWhenAllowedSortPathsAreNullOrEmpty() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        Page<TestProjection> expected = new PageImpl<>(List.of(new TestProjection("F")));
        when(service.findAll(any(), any(), eq(TestProjection.class))).thenReturn(expected);

        SearchRequest<TestEntity> nullSortPathsRequest =
                request(List.of(), Set.of(), null, Map.of());
        SearchRequest<TestEntity> emptySortPathsRequest =
                request(List.of(), Set.of(), Set.of(), Map.of());
        Pageable pageable = PageRequest.of(0, 10, Sort.by("anyField"));

        assertDoesNotThrow(
                () ->
                        SearchOperations.searchProjection(
                                service, nullSortPathsRequest, pageable, TestProjection.class));
        assertDoesNotThrow(
                () ->
                        SearchOperations.searchProjection(
                                service, emptySortPathsRequest, pageable, TestProjection.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void allowsCriteriaWhenAllowedSearchPathsAreEmpty() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> emptyAllowedPathsRequest =
                request(
                        List.of(
                                new SearchRequest.SearchCriterion(
                                        "unknown", SearchOperator.EQUALS)),
                        Set.of(),
                        Set.of(),
                        Map.of());
        when(service.findAll(any(), any(), eq(TestProjection.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertDoesNotThrow(
                () ->
                        SearchOperations.searchProjection(
                                service,
                                emptyAllowedPathsRequest,
                                PageRequest.of(0, 5),
                                TestProjection.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsNullRequestedCriteriaListBeforeQueryExecution() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> request = request(null, Set.of("name"), Set.of(), Map.of());
        when(service.findAll(any(), any(), eq(TestProjection.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThrows(
                NullPointerException.class,
                () ->
                        SearchOperations.searchProjection(
                                service, request, PageRequest.of(0, 5), TestProjection.class));
        verify(service, never()).findAll(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsNullAllowedSearchOperatorsMapBeforeQueryExecution() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> request =
                request(
                        List.of(new SearchRequest.SearchCriterion("name", SearchOperator.GT)),
                        Set.of("name"),
                        Set.of(),
                        null);
        when(service.findAll(any(), any(), eq(TestProjection.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThrows(
                NullPointerException.class,
                () ->
                        SearchOperations.searchProjection(
                                service, request, PageRequest.of(0, 5), TestProjection.class));
        verify(service, never()).findAll(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void allowsCriteriaWhenSpecificOperatorSetIsEmptyOrContainsOperator() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        when(service.findAll(any(), any(), eq(TestProjection.class)))
                .thenReturn(new PageImpl<>(List.of()));
        SearchRequest<TestEntity> emptyOperators =
                request(
                        List.of(new SearchRequest.SearchCriterion("state", SearchOperator.GT)),
                        Set.of("state"),
                        Set.of(),
                        Map.of("state", Set.of()));
        SearchRequest<TestEntity> matchingOperator =
                request(
                        List.of(new SearchRequest.SearchCriterion("score", SearchOperator.GT)),
                        Set.of("score"),
                        Set.of(),
                        Map.of("score", Set.of(SearchOperator.GT)));

        assertDoesNotThrow(
                () ->
                        SearchOperations.searchProjection(
                                service,
                                emptyOperators,
                                PageRequest.of(0, 5),
                                TestProjection.class));
        assertDoesNotThrow(
                () ->
                        SearchOperations.searchProjection(
                                service,
                                matchingOperator,
                                PageRequest.of(0, 5),
                                TestProjection.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsUnsupportedOperatorWithSearchOperationsErrorContext() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> request =
                request(
                        List.of(new SearchRequest.SearchCriterion("score", SearchOperator.GT)),
                        Set.of("score"),
                        Set.of(),
                        Map.of("score", Set.of(SearchOperator.EQUALS)));

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () ->
                                SearchOperations.searchProjection(
                                        service,
                                        request,
                                        PageRequest.of(0, 5),
                                        TestProjection.class));

        assertTrue(exception.getMessage().contains("Unsupported search operator"));
        assertTrue(exception.getContext().containsKey("allowed"));
        verify(service, never()).findAll(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsMalformedCriteriaBeforeQueryExecution() {
        CrudQueryOperations<TestEntity, TestProjection, TestProjection> service =
                mock(CrudQueryOperations.class);
        SearchRequest<TestEntity> request =
                request(
                        Arrays.asList(
                                null,
                                new SearchRequest.SearchCriterion(null, SearchOperator.EQUALS),
                                new SearchRequest.SearchCriterion(" ", SearchOperator.EQUALS),
                                new SearchRequest.SearchCriterion("name", null),
                                new SearchRequest.SearchCriterion("tag", SearchOperator.GT),
                                new SearchRequest.SearchCriterion("state", SearchOperator.GT),
                                new SearchRequest.SearchCriterion("name", SearchOperator.EQUALS)),
                        Set.of("name", "tag", "state"),
                        Set.of(),
                        Map.of("name", Set.of(SearchOperator.EQUALS), "state", Set.of()));

        assertThrows(
                BadRequestException.class,
                () ->
                        SearchOperations.searchProjection(
                                service, request, PageRequest.of(0, 5), TestProjection.class));
        verify(service, never()).findAll(any(), any(), any());
    }

    private SearchRequest<TestEntity> request(
            List<SearchRequest.SearchCriterion> criteria,
            Set<String> allowedSearchPaths,
            Set<String> allowedSortPaths,
            Map<String, Set<SearchOperator>> allowedSearchOperators) {
        return new SearchRequest<>() {
            @Override
            public Specification<TestEntity> toSpecification() {
                return (root, query, cb) -> cb.conjunction();
            }

            @Override
            public Set<String> allowedSearchPaths() {
                return allowedSearchPaths;
            }

            @Override
            public Map<String, Set<SearchOperator>> allowedSearchOperators() {
                return allowedSearchOperators;
            }

            @Override
            public Set<String> allowedSortPaths() {
                return allowedSortPaths;
            }

            @Override
            public List<SearchCriterion> requestedSearchCriteria() {
                return criteria;
            }
        };
    }

    private SearchRequest<TestEntity> request(List<SearchRequest.SearchCriterion> criteria) {
        return request(
                criteria,
                Set.of("name"),
                Set.of("name"),
                Map.of("name", Set.of(SearchOperator.EQUALS)));
    }

    private static final class TestEntity {}

    private record TestProjection(String name) {}
}

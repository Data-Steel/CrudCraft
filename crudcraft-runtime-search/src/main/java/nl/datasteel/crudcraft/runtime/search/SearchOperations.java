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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import nl.datasteel.crudcraft.runtime.service.CrudQueryOperations;
import nl.datasteel.crudcraft.runtime.service.KeysetPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;


/** Runtime search operations for generated search-enabled APIs. */
public final class SearchOperations {

    private SearchOperations() {}

    /**
     * Runs a search returning the default response DTO.
     *
     * @param service CRUD query operations
     * @param request search request
     * @param pageable requested page and sort
     * @param projection target DTO type
     * @param <T> entity type
     * @param <R> full response DTO type
     * @param <F> reference response DTO type
     * @return paged full response DTOs
     */
    public static <T, R, F> Page<R> search(
            CrudQueryOperations<T, R, F> service,
            SearchRequest<T> request,
            Pageable pageable,
            Class<R> projection) {
        return searchProjection(service, request, pageable, projection);
    }

    /**
     * Runs a search returning reference DTOs.
     *
     * @param service CRUD query operations
     * @param request search request
     * @param pageable requested page and sort
     * @param projection target DTO type
     * @param <T> entity type
     * @param <R> full response DTO type
     * @param <F> reference response DTO type
     * @return paged reference response DTOs
     */
    public static <T, R, F> Page<F> searchRef(
            CrudQueryOperations<T, R, F> service,
            SearchRequest<T> request,
            Pageable pageable,
            Class<F> projection) {
        return searchProjection(service, request, pageable, projection);
    }

    /**
     * Runs a search returning an arbitrary projection.
     *
     * @param service CRUD query operations
     * @param request search request
     * @param pageable requested page and sort
     * @param projection target projection type
     * @param <T> entity type
     * @param <R> full response DTO type
     * @param <F> reference response DTO type
     * @param <P> projection type
     * @return paged projected results
     */
    public static <T, R, F, P> Page<P> searchProjection(
            CrudQueryOperations<T, R, F> service,
            SearchRequest<T> request,
            Pageable pageable,
            Class<P> projection) {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(projection, "projection");
        validate(request, pageable == null ? Sort.unsorted() : pageable.getSort());
        return service.findAll(toSpecification(request), pageable, projection);
    }

    /**
     * Runs the canonical keyset search flow for generated search controllers.
     *
     * <p>This overload validates requested criteria and sort paths against the generated {@link
     * SearchRequest} metadata before delegating to {@link
     * CrudQueryOperations#findAllKeyset(Specification, int, String, Sort, Class)}. Generated
     * controllers should prefer this method for keyset endpoints because it keeps validation,
     * specification creation, cursor handling, and projection selection in one contract.
     *
     * <p>The {@code cursor} value is an opaque token produced by the service. Clients must send it
     * back unchanged for the same query, sort, and projection; callers must not parse or construct
     * cursor values.
     *
     * @param service CRUD query operations
     * @param request search request
     * @param limit max rows
     * @param cursor cursor token
     * @param sort sort definition
     * @param projection target projection type
     * @param <T> entity type
     * @param <R> full response DTO type
     * @param <F> reference response DTO type
     * @param <P> projection type
     * @return keyset page result
     */
    public static <T, R, F, P> KeysetPage<P> searchKeyset(
            CrudQueryOperations<T, R, F> service,
            SearchRequest<T> request,
            int limit,
            String cursor,
            Sort sort,
            Class<P> projection) {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(projection, "projection");
        validate(request, sort);
        return service.findAllKeyset(toSpecification(request), limit, cursor, sort, projection);
    }

    private static <T> Specification<T> toSpecification(SearchRequest<T> request) {
        return request == null ? null : request.toSpecification();
    }

    private static <T> void validate(SearchRequest<T> request, Sort sort) {
        if (request == null) {
            return;
        }
        validateCriteria(request);
        request.validate();
        validateSort(request, sort);
    }

    private static <T> void validateCriteria(SearchRequest<T> request) {
        Set<String> allowedPaths = request.allowedSearchPaths();
        if (allowedPaths == null || allowedPaths.isEmpty()) {
            return;
        }
        List<SearchRequest.SearchCriterion> criteria = request.requestedSearchCriteria();
        if (criteria == null || criteria.isEmpty()) {
            return;
        }
        Map<String, Set<SearchOperator>> allowedOperatorsByPath =
                Objects.requireNonNullElseGet(request.allowedSearchOperators(), Map::of);
        for (SearchRequest.SearchCriterion criterion : criteria) {
            if (criterion == null || criterion.path() == null || criterion.path().isBlank()) {
                continue;
            }
            if (!allowedPaths.contains(criterion.path())) {
                throw new BadRequestException(
                        "Unsupported search field. Use one of the generated searchable paths.",
                        Map.of(
                                "requested",
                                criterion.path(),
                                "allowed",
                                allowedPaths.toString(),
                                "docs",
                                "docs/feature-guides/search/filtering.md"));
            }
            SearchOperator operator = criterion.operator();
            Set<SearchOperator> allowedOperators = allowedOperatorsByPath.get(criterion.path());
            if (operator != null
                    && allowedOperators != null
                    && !allowedOperators.isEmpty()
                    && !allowedOperators.contains(operator)) {
                throw new BadRequestException(
                        "Unsupported search operator for field.",
                        Map.of(
                                "field",
                                criterion.path(),
                                "operator",
                                operator.name(),
                                "allowed",
                                allowedOperators.toString(),
                                "docs",
                                "docs/feature-guides/search/operators.md"));
            }
        }
    }

    private static <T> void validateSort(SearchRequest<T> request, Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return;
        }
        Set<String> allowedSortPaths = request.allowedSortPaths();
        if (allowedSortPaths == null || allowedSortPaths.isEmpty()) {
            return;
        }
        for (Sort.Order order : sort) {
            if (!allowedSortPaths.contains(order.getProperty())) {
                throw new BadRequestException(
                        "Unsupported sort field. Use one of the generated sortable paths.",
                        Map.of(
                                "requested",
                                order.getProperty(),
                                "allowed",
                                allowedSortPaths.toString(),
                                "docs",
                                "docs/feature-guides/search/sorting.md"));
            }
        }
    }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import org.springframework.data.jpa.domain.Specification;


/**
 * Fluent runtime builder for programmatic search requests.
 *
 * @param <T> entity type
 */
public final class SearchBuilder<T> {

    private Specification<T> specification;
    private SearchLogic searchLogic = SearchLogic.OR;
    private final Set<String> allowedSortPaths = new LinkedHashSet<>();
    private final Set<String> allowedSearchPaths = new LinkedHashSet<>();
    private final Map<String, Set<SearchOperator>> allowedOperators = new LinkedHashMap<>();
    private final List<SearchRequest.SearchCriterion> criteria = new ArrayList<>();

    private SearchBuilder() {}

    /**
     * Creates an empty search builder.
     *
     * @param <T> entity type
     * @return builder
     */
    public static <T> SearchBuilder<T> create() {
        return new SearchBuilder<>();
    }

    /**
     * Creates a builder with an initial specification.
     *
     * @param specification JPA specification
     * @param <T> entity type
     * @return builder
     */
    public static <T> SearchBuilder<T> from(Specification<T> specification) {
        return SearchBuilder.<T>create().specification(specification);
    }

    /**
     * Sets the JPA specification returned by the built request.
     *
     * @param specification JPA specification
     * @return this builder
     */
    public SearchBuilder<T> specification(Specification<T> specification) {
        this.specification = specification;
        return this;
    }

    /**
     * Sets the criterion composition logic.
     *
     * @param searchLogic search logic
     * @return this builder
     */
    public SearchBuilder<T> searchLogic(SearchLogic searchLogic) {
        this.searchLogic = Objects.requireNonNull(searchLogic, "searchLogic");
        return this;
    }

    /**
     * Allows sorting by the given path.
     *
     * @param path generated or runtime search path
     * @return this builder
     */
    public SearchBuilder<T> sortable(String path) {
        allowedSortPaths.add(requirePath(path));
        return this;
    }

    /**
     * Allows searching the path with one or more operators.
     *
     * @param path generated or runtime search path
     * @param firstOperator first allowed operator
     * @param extraOperators additional allowed operators
     * @return this builder
     */
    public SearchBuilder<T> searchable(
            String path, SearchOperator firstOperator, SearchOperator... extraOperators) {
        String normalizedPath = requirePath(path);
        Set<SearchOperator> operators =
                allowedOperators.computeIfAbsent(normalizedPath, ignored -> new LinkedHashSet<>());
        operators.add(Objects.requireNonNull(firstOperator, "firstOperator"));
        for (SearchOperator operator : extraOperators) {
            operators.add(Objects.requireNonNull(operator, "operator"));
        }
        allowedSearchPaths.add(normalizedPath);
        return this;
    }

    /**
     * Adds an active search criterion.
     *
     * @param path generated or runtime search path
     * @param operator requested operator
     * @return this builder
     */
    public SearchBuilder<T> criterion(String path, SearchOperator operator) {
        criteria.add(
                new SearchRequest.SearchCriterion(
                        requirePath(path), Objects.requireNonNull(operator, "operator")));
        return this;
    }

    /**
     * Builds an immutable search request snapshot.
     *
     * @return search request
     */
    public SearchRequest<T> build() {
        Specification<T> resolvedSpecification =
                specification == null ? (root, query, cb) -> cb.conjunction() : specification;
        return new BuiltSearchRequest<>(
                resolvedSpecification,
                searchLogic,
                Set.copyOf(allowedSortPaths),
                Set.copyOf(allowedSearchPaths),
                copyAllowedOperators(),
                List.copyOf(criteria));
    }

    private Map<String, Set<SearchOperator>> copyAllowedOperators() {
        Map<String, Set<SearchOperator>> copy = new LinkedHashMap<>();
        allowedOperators.forEach((path, operators) -> copy.put(path, Set.copyOf(operators)));
        return Collections.unmodifiableMap(copy);
    }

    private static String requirePath(String path) {
        Objects.requireNonNull(path, "path");
        if (path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        return SearchPathGuard.requireWellFormed(path);
    }

    private record BuiltSearchRequest<T>(
            Specification<T> specification,
            SearchLogic searchLogic,
            Set<String> allowedSortPaths,
            Set<String> allowedSearchPaths,
            Map<String, Set<SearchOperator>> allowedSearchOperators,
            List<SearchRequest.SearchCriterion> requestedSearchCriteria)
            implements SearchRequest<T> {

        @Override
        public Specification<T> toSpecification() {
            return specification;
        }

        @Override
        public SearchLogic getSearchLogic() {
            return searchLogic;
        }
    }
}

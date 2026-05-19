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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import nl.datasteel.crudcraft.runtime.service.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;


/**
 * Marker interface for generated search request objects that can produce a JPA {@link
 * Specification}.
 *
 * @param <T> entity type
 */
public interface SearchRequest<T> extends SpecificationProvider<T> {
    /**
     * Returns a JPA specification representing the search criteria.
     *
     * @return search specification
     */
    @NonNull Specification<T> toSpecification();

    /**
     * Returns the logic operator used to combine multiple search criteria. Defaults to OR logic if
     * not specified.
     *
     * @return the search logic (OR or AND)
     */
    default @NonNull SearchLogic getSearchLogic() {
        return SearchLogic.OR;
    }

    /**
     * Returns the set of sortable paths defined by generated metadata. Empty means no generated
     * sort restrictions are available.
     *
     * @return allowed sort paths
     */
    default @NonNull Set<String> allowedSortPaths() {
        return Set.of();
    }

    /**
     * Returns the set of searchable paths defined by generated metadata. Empty means no generated
     * search-path metadata is available.
     *
     * @return allowed search paths
     */
    default @NonNull Set<String> allowedSearchPaths() {
        return Set.of();
    }

    /**
     * Returns allowed operators per generated search path.
     *
     * @return allowed operators keyed by path
     */
    default @NonNull Map<String, Set<SearchOperator>> allowedSearchOperators() {
        return Map.of();
    }

    /**
     * Returns active search criteria provided by the request.
     *
     * @return requested search criteria
     */
    default @NonNull List<SearchCriterion> requestedSearchCriteria() {
        return List.of();
    }

    /**
     * Returns the maximum allowed search path segment count. Generated request classes override
     * this with the configured generation depth observed in their searchable metadata.
     *
     * @return maximum path depth, or {@link Integer#MAX_VALUE} when no depth budget is available
     */
    default int maxSearchPathDepth() {
        Set<String> allowedPaths =
                Objects.requireNonNull(allowedSearchPaths(), "allowedSearchPaths must not be null");
        if (allowedPaths.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        int maxDepth = 0;
        for (String path : allowedPaths) {
            if (path != null && !path.isBlank()) {
                maxDepth = Math.max(maxDepth, pathDepth(path));
            }
        }
        return maxDepth == 0 ? Integer.MAX_VALUE : maxDepth;
    }

    /**
     * Validates the active search criteria against generated metadata.
     *
     * <p>Generated request classes call this hook before building a JPA specification, and custom
     * direct service callers can call it before invoking {@link #toSpecification()}. The validation
     * rejects blank paths, malformed dotted paths, missing operators, paths outside the generated
     * allow-list, and unsupported operators.
     *
     * @throws BadRequestException when active criteria violate generated search metadata
     */
    default void validate() {
        Set<String> allowedPaths =
                Objects.requireNonNull(allowedSearchPaths(), "allowedSearchPaths must not be null");
        Map<String, Set<SearchOperator>> allowedOperators =
                Objects.requireNonNull(
                        allowedSearchOperators(), "allowedSearchOperators must not be null");
        List<SearchCriterion> criteria =
                Objects.requireNonNull(
                        requestedSearchCriteria(), "requestedSearchCriteria must not be null");
        int maxDepth = maxSearchPathDepth();
        for (SearchCriterion criterion : criteria) {
            validateCriterion(criterion, allowedPaths, allowedOperators, maxDepth);
        }
    }

    private static void validateCriterion(
            SearchCriterion criterion,
            Set<String> allowedPaths,
            Map<String, Set<SearchOperator>> allowedOperators,
            int maxDepth) {
        if (criterion == null) {
            throw new BadRequestException("Invalid search criterion: criterion must not be null.");
        }
        String path = criterion.path();
        if (path == null || path.isBlank()) {
            throw new BadRequestException("Invalid search criterion: path must not be blank.");
        }
        SearchPathGuard.rejectCycles(path, allowedPaths);
        SearchPathGuard.enforceMaxDepth(path, maxDepth);
        if (!allowedPaths.isEmpty() && !allowedPaths.contains(path)) {
            throw new BadRequestException(
                    "Invalid search path '"
                            + path
                            + "'. It is not part of the generated searchable field allow-list.");
        }
        SearchOperator operator = criterion.operator();
        if (operator == null) {
            throw new BadRequestException(
                    "Invalid search criterion for path '" + path + "': operator must not be null.");
        }
        if (allowedOperators.containsKey(path)) {
            Set<SearchOperator> operators = allowedOperators.get(path);
            if (operators != null && !operators.isEmpty() && !operators.contains(operator)) {
                throw new BadRequestException(
                        "Invalid search operator "
                                + operator
                                + " for path '"
                                + path
                                + "'. Allowed operators: "
                                + operators
                                + ".");
            }
        }
    }

    private static int pathDepth(String path) {
        int depth = 1;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '.') {
                depth++;
            }
        }
        return depth;
    }

    /**
     * Immutable criterion value used for runtime validation.
     *
     * @param path flattened search path
     * @param operator selected operator
     */
    record SearchCriterion(@Nullable String path, @Nullable SearchOperator operator) {}
}

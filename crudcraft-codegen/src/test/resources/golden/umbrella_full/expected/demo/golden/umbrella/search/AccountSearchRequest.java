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
package demo.golden.umbrella.search;

import demo.golden.umbrella.Account;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.runtime.search.NotThreadSafe;
import nl.datasteel.crudcraft.runtime.search.SearchLogic;
import nl.datasteel.crudcraft.runtime.search.SearchPathGuard;
import nl.datasteel.crudcraft.runtime.search.SearchRequest;
import org.springframework.data.jpa.domain.Specification;

/**
 * Generated model file for Account; do not edit manually.
 * @CrudCraft:generated
 *
 * This search class reflects the @Searchable fields of your entity and should not be edited. It is a mutable, @NotThreadSafe command object intended for one request at a time.
 *
 * Included elements:
 * - Mutable search parameters with generated accessors
 * - Specification logic for filtering results
 * - Allowed path and operator metadata for request validation
 *
 * Example:
 * var request = new AccountSearchRequest();
 * request.setTitle("CrudCraft");
 * var spec = request.toSpecification();
 *
 * Generation context:
 * - Source model: Account
 * - Package: demo.golden.umbrella.search
 * - Generator: SearchGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * To make changes, edit the entity model class and rebuild the project.
 * Do not edit or rename this file manually.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 *
 * <p>Nested searchable paths are generated only up to the configured search generation depth. Treat dotted paths as an allow-list generated at compile time; requests outside this list are rejected before a JPA specification is built.
 *
 * @see nl.datasteel.crudcraft.runtime.search.SearchPathGuard
 */
@SuppressWarnings("serial")
@NotThreadSafe
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "Spring binds generated search request collections and maps through live accessors"
)
public class AccountSearchRequest implements SearchRequest<Account>, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Set<String> ALLOWED_SEARCH_PATHS = Set.of("name", "tenantId", "createdAt");

    private static final int MAX_SEARCH_PATH_DEPTH = 1;

    private static final Set<String> ALLOWED_SORT_PATHS = Set.of("name", "tenantId", "createdAt");

    private static final Map<String, Set<SearchOperator>> ALLOWED_SEARCH_OPERATORS = Map.ofEntries(Map.entry("name", Set.of(SearchOperator.EQUALS, SearchOperator.NOT_EQUALS, SearchOperator.CONTAINS, SearchOperator.STARTS_WITH, SearchOperator.ENDS_WITH, SearchOperator.REGEX, SearchOperator.IN, SearchOperator.NOT_IN)), Map.entry("tenantId", Set.of(SearchOperator.EQUALS, SearchOperator.NOT_EQUALS, SearchOperator.CONTAINS, SearchOperator.STARTS_WITH, SearchOperator.ENDS_WITH, SearchOperator.REGEX, SearchOperator.IN, SearchOperator.NOT_IN)), Map.entry("createdAt", Set.of(SearchOperator.AFTER)));

    private String name;

    private SearchOperator nameOp;

    private String tenantId;

    private SearchOperator tenantIdOp;

    private Instant createdAt;

    private SearchOperator createdAtOp;

    private SearchLogic searchLogic;

    public AccountSearchRequest() {
    }

    public AccountSearchRequest(AccountSearchRequest other) {
        if (other != null) {
            this.name = other.name;
            this.nameOp = other.nameOp;
            this.tenantId = other.tenantId;
            this.tenantIdOp = other.tenantIdOp;
            this.createdAt = other.createdAt;
            this.createdAtOp = other.createdAtOp;
            this.searchLogic = other.searchLogic;
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SearchOperator getNameOp() {
        return this.nameOp;
    }

    public void setNameOp(SearchOperator nameOp) {
        this.nameOp = nameOp;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public SearchOperator getTenantIdOp() {
        return this.tenantIdOp;
    }

    public void setTenantIdOp(SearchOperator tenantIdOp) {
        this.tenantIdOp = tenantIdOp;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public SearchOperator getCreatedAtOp() {
        return this.createdAtOp;
    }

    public void setCreatedAtOp(SearchOperator createdAtOp) {
        this.createdAtOp = createdAtOp;
    }

    @Override
    public SearchLogic getSearchLogic() {
        return searchLogic != null ? searchLogic : SearchLogic.OR;
    }

    public void setSearchLogic(SearchLogic searchLogic) {
        this.searchLogic = searchLogic;
    }

    @Override
    public Set<String> allowedSortPaths() {
        return ALLOWED_SORT_PATHS;
    }

    @Override
    public Set<String> allowedSearchPaths() {
        return ALLOWED_SEARCH_PATHS;
    }

    @Override
    public Map<String, Set<SearchOperator>> allowedSearchOperators() {
        return Map.copyOf(ALLOWED_SEARCH_OPERATORS);
    }

    @Override
    public List<SearchRequest.SearchCriterion> requestedSearchCriteria() {
        List<SearchRequest.SearchCriterion> criteria = new ArrayList<>();
        if (getName() != null) {
            criteria.add(new SearchRequest.SearchCriterion("name", getNameOp()));
        }
        if (getTenantId() != null) {
            criteria.add(new SearchRequest.SearchCriterion("tenantId", getTenantIdOp()));
        }
        if (getCreatedAt() != null) {
            criteria.add(new SearchRequest.SearchCriterion("createdAt", getCreatedAtOp()));
        }
        return criteria;
    }

    @Override
    public int maxSearchPathDepth() {
        return MAX_SEARCH_PATH_DEPTH;
    }

    @Override
    public void validate() {
        for (SearchRequest.SearchCriterion criterion : requestedSearchCriteria()) {
            if (criterion != null) {
                SearchPathGuard.enforceMaxDepth(criterion.path(), MAX_SEARCH_PATH_DEPTH);
            }
        }
        SearchRequest.super.validate();
    }

    @Override
    public Specification<Account> toSpecification() {
        validate();
        return new AccountSpecification(this);
    }
}

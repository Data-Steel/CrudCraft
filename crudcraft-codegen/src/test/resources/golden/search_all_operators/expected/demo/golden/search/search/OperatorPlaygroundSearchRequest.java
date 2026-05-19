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
package demo.golden.search.search;

import demo.golden.search.OperatorPlayground;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.math.BigDecimal;
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
 * Generated model file for OperatorPlayground; do not edit manually.
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
 * var request = new OperatorPlaygroundSearchRequest();
 * request.setTitle("CrudCraft");
 * var spec = request.toSpecification();
 *
 * Generation context:
 * - Source model: OperatorPlayground
 * - Package: demo.golden.search.search
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
public class OperatorPlaygroundSearchRequest implements SearchRequest<OperatorPlayground>, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Set<String> ALLOWED_SEARCH_PATHS = Set.of("title", "score", "publishedAt", "labels", "labelsSize", "attributes", "attributesSize");

    private static final int MAX_SEARCH_PATH_DEPTH = 1;

    private static final Set<String> ALLOWED_SORT_PATHS = Set.of("title", "score", "publishedAt");

    private static final Map<String, Set<SearchOperator>> ALLOWED_SEARCH_OPERATORS = Map.ofEntries(Map.entry("title", Set.of(SearchOperator.EQUALS, SearchOperator.NOT_EQUALS, SearchOperator.CONTAINS, SearchOperator.STARTS_WITH, SearchOperator.ENDS_WITH, SearchOperator.REGEX, SearchOperator.IN, SearchOperator.NOT_IN, SearchOperator.IS_EMPTY, SearchOperator.NOT_EMPTY)), Map.entry("score", Set.of(SearchOperator.GT, SearchOperator.GTE, SearchOperator.LT, SearchOperator.LTE, SearchOperator.RANGE, SearchOperator.BETWEEN)), Map.entry("publishedAt", Set.of(SearchOperator.BEFORE, SearchOperator.AFTER)), Map.entry("labels", Set.of(SearchOperator.IS_EMPTY, SearchOperator.NOT_EMPTY, SearchOperator.CONTAINS_ALL)), Map.entry("labelsSize", Set.of(SearchOperator.SIZE_EQUALS, SearchOperator.SIZE_GT, SearchOperator.SIZE_LT)), Map.entry("attributes", Set.of(SearchOperator.CONTAINS_KEY, SearchOperator.CONTAINS_VALUE)), Map.entry("attributesSize", Set.of(SearchOperator.SIZE_EQUALS)));

    private String title;

    private SearchOperator titleOp;

    private BigDecimal score;

    private BigDecimal scoreStart;

    private BigDecimal scoreEnd;

    private SearchOperator scoreOp;

    private Instant publishedAt;

    private SearchOperator publishedAtOp;

    private Set<String> labels;

    private SearchOperator labelsOp;

    private Integer labelsSize;

    private SearchOperator labelsSizeOp;

    private Map<String, String> attributes;

    private SearchOperator attributesOp;

    private Integer attributesSize;

    private SearchOperator attributesSizeOp;

    private SearchLogic searchLogic;

    public OperatorPlaygroundSearchRequest() {
    }

    public OperatorPlaygroundSearchRequest(OperatorPlaygroundSearchRequest other) {
        if (other != null) {
            this.title = other.title;
            this.titleOp = other.titleOp;
            this.score = other.score;
            this.scoreStart = other.scoreStart;
            this.scoreEnd = other.scoreEnd;
            this.scoreOp = other.scoreOp;
            this.publishedAt = other.publishedAt;
            this.publishedAtOp = other.publishedAtOp;
            this.labels = other.labels;
            this.labelsOp = other.labelsOp;
            this.labelsSize = other.labelsSize;
            this.labelsSizeOp = other.labelsSizeOp;
            this.attributes = other.attributes;
            this.attributesOp = other.attributesOp;
            this.attributesSize = other.attributesSize;
            this.attributesSizeOp = other.attributesSizeOp;
            this.searchLogic = other.searchLogic;
        }
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SearchOperator getTitleOp() {
        return this.titleOp;
    }

    public void setTitleOp(SearchOperator titleOp) {
        this.titleOp = titleOp;
    }

    public BigDecimal getScore() {
        return this.score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getScoreStart() {
        return this.scoreStart;
    }

    public void setScoreStart(BigDecimal scoreStart) {
        this.scoreStart = scoreStart;
    }

    public BigDecimal getScoreEnd() {
        return this.scoreEnd;
    }

    public void setScoreEnd(BigDecimal scoreEnd) {
        this.scoreEnd = scoreEnd;
    }

    public SearchOperator getScoreOp() {
        return this.scoreOp;
    }

    public void setScoreOp(SearchOperator scoreOp) {
        this.scoreOp = scoreOp;
    }

    public Instant getPublishedAt() {
        return this.publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public SearchOperator getPublishedAtOp() {
        return this.publishedAtOp;
    }

    public void setPublishedAtOp(SearchOperator publishedAtOp) {
        this.publishedAtOp = publishedAtOp;
    }

    public Set<String> getLabels() {
        return this.labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public SearchOperator getLabelsOp() {
        return this.labelsOp;
    }

    public void setLabelsOp(SearchOperator labelsOp) {
        this.labelsOp = labelsOp;
    }

    public Integer getLabelsSize() {
        return this.labelsSize;
    }

    public void setLabelsSize(Integer labelsSize) {
        this.labelsSize = labelsSize;
    }

    public SearchOperator getLabelsSizeOp() {
        return this.labelsSizeOp;
    }

    public void setLabelsSizeOp(SearchOperator labelsSizeOp) {
        this.labelsSizeOp = labelsSizeOp;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public SearchOperator getAttributesOp() {
        return this.attributesOp;
    }

    public void setAttributesOp(SearchOperator attributesOp) {
        this.attributesOp = attributesOp;
    }

    public Integer getAttributesSize() {
        return this.attributesSize;
    }

    public void setAttributesSize(Integer attributesSize) {
        this.attributesSize = attributesSize;
    }

    public SearchOperator getAttributesSizeOp() {
        return this.attributesSizeOp;
    }

    public void setAttributesSizeOp(SearchOperator attributesSizeOp) {
        this.attributesSizeOp = attributesSizeOp;
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
        if (getTitle() != null) {
            criteria.add(new SearchRequest.SearchCriterion("title", getTitleOp()));
        }
        if (getScore() != null || getScoreStart() != null || getScoreEnd() != null) {
            criteria.add(new SearchRequest.SearchCriterion("score", getScoreOp()));
        }
        if (getPublishedAt() != null) {
            criteria.add(new SearchRequest.SearchCriterion("publishedAt", getPublishedAtOp()));
        }
        if (getLabels() != null) {
            criteria.add(new SearchRequest.SearchCriterion("labels", getLabelsOp()));
        }
        if (getLabelsSize() != null) {
            criteria.add(new SearchRequest.SearchCriterion("labelsSize", getLabelsSizeOp()));
        }
        if (getAttributes() != null) {
            criteria.add(new SearchRequest.SearchCriterion("attributes", getAttributesOp()));
        }
        if (getAttributesSize() != null) {
            criteria.add(new SearchRequest.SearchCriterion("attributesSize", getAttributesSizeOp()));
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
    public Specification<OperatorPlayground> toSpecification() {
        validate();
        return new OperatorPlaygroundSpecification(this);
    }
}

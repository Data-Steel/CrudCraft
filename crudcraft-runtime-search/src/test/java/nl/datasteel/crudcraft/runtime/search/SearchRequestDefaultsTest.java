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

import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import nl.datasteel.crudcraft.runtime.search.SearchRequest.SearchCriterion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SearchRequestDefaultsTest {

    @Test
    void defaultMethodsExposeEmptyMetadataAndOrLogic() {
        SearchRequest<Object> request = () -> (root, query, cb) -> cb.conjunction();

        assertEquals(SearchLogic.OR, request.getSearchLogic());
        assertTrue(request.allowedSortPaths().isEmpty());
        assertTrue(request.allowedSearchPaths().isEmpty());
        assertTrue(request.allowedSearchOperators().isEmpty());
        assertTrue(request.requestedSearchCriteria().isEmpty());
        assertEquals(Integer.MAX_VALUE, request.maxSearchPathDepth());
        assertDoesNotThrow(request::validate);
    }

    @Test
    void searchCriterionRecordExposesPathAndOperator() {
        SearchRequest.SearchCriterion criterion =
                new SearchRequest.SearchCriterion("name", SearchOperator.EQUALS);

        assertEquals("name", criterion.path());
        assertEquals(SearchOperator.EQUALS, criterion.operator());
    }

    @Test
    void validateRejectsUnsupportedGeneratedMetadata() {
        SearchRequest<Object> request =
                request(
                        java.util.List.of(new SearchCriterion("name", SearchOperator.GT)),
                        java.util.Set.of("name"),
                        java.util.Map.of("name", java.util.Set.of(SearchOperator.EQUALS)));

        BadRequestException ex = assertThrows(BadRequestException.class, request::validate);
        assertTrue(ex.getMessage().contains("Invalid search operator"));
    }

    @Test
    void validateAllowsRepeatedSegmentNames() {
        SearchRequest<Object> request =
                request(
                        java.util.List.of(
                                new SearchCriterion("parent.parent", SearchOperator.EQUALS)),
                        java.util.Set.of("parent.parent"),
                        java.util.Map.of());

        assertDoesNotThrow(request::validate);
    }

    @Test
    void validateRejectsMalformedSearchPath() {
        SearchRequest<Object> request =
                request(
                        java.util.List.of(
                                new SearchCriterion("parent..parent", SearchOperator.EQUALS)),
                        java.util.Set.of("parent..parent"),
                        java.util.Map.of());

        BadRequestException ex = assertThrows(BadRequestException.class, request::validate);
        assertTrue(ex.getMessage().contains("Invalid searchable path rejected"));
    }

    @Test
    void validateRejectsSearchPathBeyondMetadataDepth() {
        SearchRequest<Object> request =
                request(
                        java.util.List.of(
                                new SearchCriterion("author.address.city", SearchOperator.EQUALS)),
                        java.util.Set.of("author.name"),
                        java.util.Map.of());

        BadRequestException ex = assertThrows(BadRequestException.class, request::validate);
        assertTrue(ex.getMessage().contains("exceeds the configured maximum depth"));
        assertTrue(ex.getMessage().contains("author.address.city"));
    }

    @Test
    void validateRejectsMalformedCriteria() {
        SearchRequest<Object> nullCriterion =
                request(
                        java.util.Collections.singletonList(null),
                        java.util.Set.of(),
                        java.util.Map.of());
        SearchRequest<Object> blankPath =
                request(
                        java.util.List.of(new SearchCriterion(" ", SearchOperator.EQUALS)),
                        java.util.Set.of(),
                        java.util.Map.of());
        SearchRequest<Object> missingOperator =
                request(
                        java.util.List.of(new SearchCriterion("name", null)),
                        java.util.Set.of("name"),
                        java.util.Map.of());

        assertTrue(
                assertThrows(BadRequestException.class, nullCriterion::validate)
                        .getMessage()
                        .contains("must not be null"));
        assertTrue(
                assertThrows(BadRequestException.class, blankPath::validate)
                        .getMessage()
                        .contains("must not be blank"));
        assertTrue(
                assertThrows(BadRequestException.class, missingOperator::validate)
                        .getMessage()
                        .contains("operator must not be null"));
    }

    @Test
    void validateRejectsPathOutsideAllowList() {
        SearchRequest<Object> request =
                request(
                        java.util.List.of(new SearchCriterion("email", SearchOperator.EQUALS)),
                        java.util.Set.of("name"),
                        java.util.Map.of());

        BadRequestException ex = assertThrows(BadRequestException.class, request::validate);
        assertTrue(ex.getMessage().contains("Invalid search path"));
    }

    @Test
    void validateAcceptsAllowedCriteriaAndOptionalMetadata() {
        SearchRequest<Object> allowed =
                request(
                        java.util.List.of(new SearchCriterion("name", SearchOperator.EQUALS)),
                        java.util.Set.of("name"),
                        java.util.Map.of("name", java.util.Set.of(SearchOperator.EQUALS)));
        SearchRequest<Object> emptyOperatorSet =
                request(
                        java.util.List.of(new SearchCriterion("name", SearchOperator.EQUALS)),
                        java.util.Set.of(),
                        java.util.Map.of("name", java.util.Set.of()));
        SearchRequest<Object> noOperatorEntry =
                request(
                        java.util.List.of(new SearchCriterion("name", SearchOperator.EQUALS)),
                        java.util.Set.of(),
                        java.util.Map.of("other", java.util.Set.of(SearchOperator.GT)));

        assertDoesNotThrow(allowed::validate);
        assertDoesNotThrow(emptyOperatorSet::validate);
        assertDoesNotThrow(noOperatorEntry::validate);
    }

    @Test
    void validateRejectsBrokenCustomNullnessContracts() {
        SearchRequest<Object> nullPaths =
                request(
                        java.util.List.of(new SearchCriterion("name", SearchOperator.EQUALS)),
                        null,
                        java.util.Map.of());
        SearchRequest<Object> nullOperators =
                request(
                        java.util.List.of(new SearchCriterion("name", SearchOperator.EQUALS)),
                        java.util.Set.of("name"),
                        null);
        SearchRequest<Object> nullCriteria =
                request(null, java.util.Set.of("name"), java.util.Map.of());

        assertTrue(
                assertThrows(NullPointerException.class, nullPaths::validate)
                        .getMessage()
                        .contains("allowedSearchPaths"));
        assertTrue(
                assertThrows(NullPointerException.class, nullOperators::validate)
                        .getMessage()
                        .contains("allowedSearchOperators"));
        assertTrue(
                assertThrows(NullPointerException.class, nullCriteria::validate)
                        .getMessage()
                        .contains("requestedSearchCriteria"));
    }

    private SearchRequest<Object> request(
            java.util.List<SearchCriterion> criteria,
            java.util.Set<String> paths,
            java.util.Map<String, java.util.Set<SearchOperator>> operators) {
        return new SearchRequest<>() {
                    @Override
                    public org.springframework.data.jpa.domain.Specification<Object>
                            toSpecification() {
                        return (root, query, cb) -> cb.conjunction();
                    }

                    @Override
                    public java.util.Set<String> allowedSearchPaths() {
                        return paths;
                    }

                    @Override
                    public java.util.Map<String, java.util.Set<SearchOperator>>
                            allowedSearchOperators() {
                        return operators;
                    }

                    @Override
                    public java.util.List<SearchCriterion> requestedSearchCriteria() {
                        return criteria;
                    }
                };
    }
}

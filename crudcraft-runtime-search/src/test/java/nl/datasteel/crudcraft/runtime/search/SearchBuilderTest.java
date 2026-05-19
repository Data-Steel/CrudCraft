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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SearchBuilderTest {

    @Test
    void constructorIsPrivate() throws Exception {
        Constructor<?> constructor = SearchBuilder.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }

    @Test
    void createBuildsDefaultRequest() {
        SearchRequest<TestEntity> request = SearchBuilder.<TestEntity>create().build();

        assertNotNull(request.toSpecification());
        assertEquals(SearchLogic.OR, request.getSearchLogic());
        assertTrue(request.allowedSortPaths().isEmpty());
        assertTrue(request.allowedSearchPaths().isEmpty());
        assertTrue(request.allowedSearchOperators().isEmpty());
        assertTrue(request.requestedSearchCriteria().isEmpty());
    }

    @Test
    void fromSeedsSpecification() {
        Specification<TestEntity> specification = (root, query, cb) -> cb.conjunction();

        SearchRequest<TestEntity> request = SearchBuilder.from(specification).build();

        assertSame(specification, request.toSpecification());
    }

    @Test
    void buildsRequestWithConfiguredMetadataAndCriteria() {
        Specification<TestEntity> specification = (root, query, cb) -> cb.disjunction();
        SearchRequest<TestEntity> request =
                SearchBuilder.<TestEntity>create()
                        .specification(specification)
                        .searchLogic(SearchLogic.AND)
                        .sortable("name")
                        .searchable("name", SearchOperator.EQUALS, SearchOperator.CONTAINS)
                        .criterion("name", SearchOperator.CONTAINS)
                        .build();

        assertSame(specification, request.toSpecification());
        assertEquals(SearchLogic.AND, request.getSearchLogic());
        assertEquals(Set.of("name"), request.allowedSortPaths());
        assertEquals(Set.of("name"), request.allowedSearchPaths());
        assertEquals(
                Set.of(SearchOperator.EQUALS, SearchOperator.CONTAINS),
                request.allowedSearchOperators().get("name"));
        assertEquals(
                new SearchRequest.SearchCriterion("name", SearchOperator.CONTAINS),
                request.requestedSearchCriteria().getFirst());
    }

    @Test
    void builderMethodsReturnSameBuilder() {
        SearchBuilder<TestEntity> builder = SearchBuilder.create();

        assertSame(builder, builder.specification(null));
        assertSame(builder, builder.searchLogic(SearchLogic.AND));
        assertSame(builder, builder.sortable("createdAt"));
        assertSame(builder, builder.searchable("createdAt", SearchOperator.BEFORE));
        assertSame(builder, builder.criterion("createdAt", SearchOperator.BEFORE));
        assertFalse(builder.build().requestedSearchCriteria().isEmpty());
    }

    @Test
    void builtRequestIsImmutableSnapshot() {
        SearchBuilder<TestEntity> builder =
                SearchBuilder.<TestEntity>create()
                        .sortable("name")
                        .searchable("name", SearchOperator.EQUALS)
                        .criterion("name", SearchOperator.EQUALS);

        SearchRequest<TestEntity> request = builder.build();
        builder.sortable("createdAt")
                .searchable("createdAt", SearchOperator.AFTER)
                .criterion("createdAt", SearchOperator.AFTER);

        assertEquals(Set.of("name"), request.allowedSortPaths());
        assertEquals(Set.of("name"), request.allowedSearchPaths());
        assertEquals(1, request.allowedSearchOperators().size());
        assertEquals(1, request.requestedSearchCriteria().size());
        assertThrows(UnsupportedOperationException.class, () -> request.allowedSortPaths().add("x"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> request.allowedSearchOperators().put("x", Set.of(SearchOperator.EQUALS)));
        assertThrows(
                UnsupportedOperationException.class,
                () -> request.allowedSearchOperators().get("name").add(SearchOperator.CONTAINS));
        assertThrows(
                UnsupportedOperationException.class,
                () ->
                        request.requestedSearchCriteria()
                                .add(new SearchRequest.SearchCriterion("x", SearchOperator.EQUALS)));
    }

    @Test
    void rejectsInvalidInputs() {
        SearchBuilder<TestEntity> builder = SearchBuilder.create();

        assertThrows(NullPointerException.class, () -> builder.searchLogic(null));
        assertThrows(NullPointerException.class, () -> builder.sortable(null));
        assertThrows(IllegalArgumentException.class, () -> builder.sortable(" "));
        assertThrows(
                NullPointerException.class,
                () -> builder.searchable(null, SearchOperator.EQUALS));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.searchable("", SearchOperator.EQUALS));
        assertThrows(NullPointerException.class, () -> builder.searchable("name", null));
        assertThrows(
                NullPointerException.class,
                () -> builder.searchable("name", SearchOperator.EQUALS, (SearchOperator) null));
        assertThrows(NullPointerException.class, () -> builder.criterion(null, SearchOperator.EQUALS));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.criterion("\t", SearchOperator.EQUALS));
        assertThrows(NullPointerException.class, () -> builder.criterion("name", null));
    }

    @Test
    void allowsRepeatedPathSegments() {
        SearchBuilder<TestEntity> builder = SearchBuilder.create();
        assertSame(
                builder,
                builder.searchable("posts.author.posts", SearchOperator.EQUALS));
    }

    @Test
    void rejectsMalformedPathSegments() {
        SearchBuilder<TestEntity> builder = SearchBuilder.create();

        BadRequestException thrown =
                assertThrows(
                        BadRequestException.class,
                        () -> builder.searchable("posts..author", SearchOperator.EQUALS));

        assertTrue(thrown.getMessage().contains("Invalid searchable path rejected"));
    }

    static class TestEntity {}
}

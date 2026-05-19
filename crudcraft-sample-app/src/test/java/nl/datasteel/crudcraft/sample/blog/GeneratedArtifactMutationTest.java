/*
 * Copyright (c) 2026 CrudCraft contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package nl.datasteel.crudcraft.sample.blog;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.lang.reflect.RecordComponent;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import nl.datasteel.crudcraft.runtime.search.SearchLogic;
import nl.datasteel.crudcraft.runtime.search.SearchRequest;
import nl.datasteel.crudcraft.sample.blog.controller.TagController;
import nl.datasteel.crudcraft.sample.blog.dto.request.TagRequestDto;
import nl.datasteel.crudcraft.sample.blog.dto.response.TagResponseDto;
import nl.datasteel.crudcraft.sample.blog.search.TagSearchRequest;
import nl.datasteel.crudcraft.sample.blog.service.TagService;
import nl.datasteel.crudcraft.sample.tck.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeneratedArtifactMutationTest extends PostgresIntegrationTestBase {

    @Test
    @Tag("tck:generated-artifact.dto-builders")
    @Tag("tck:pit.generated-roundtrip")
    void generatedDtoBuildersPreserveEveryGeneratedField() {
        UUID id = UUID.randomUUID();

        TagRequestDto request = TagRequestDto.builder().name("platform").build();
        TagResponseDto response = TagResponseDto.builder().id(id).name(request.name()).build();

        assertEquals("platform", request.name());
        assertEquals(id, response.id());
        assertEquals("platform", response.name());
        assertEquals("name", TagRequestDto.fieldSecurityMetadata().fields().getFirst().name());
        assertEquals("id", TagResponseDto.fieldSecurityMetadata().fields().getFirst().name());
    }

    @Test
    @Tag("tck:generated-artifact.validation")
    void generatedDtoValidationAnnotationsStayAttachedToRecordComponents() {
        RecordComponent requestName = TagRequestDto.class.getRecordComponents()[0];
        RecordComponent responseId = TagResponseDto.class.getRecordComponents()[0];
        RecordComponent responseName = TagResponseDto.class.getRecordComponents()[1];

        assertEquals("name", requestName.getName());
        assertNotNull(requestName.getAccessor().getAnnotation(NotBlank.class));
        Size requestSize = requestName.getAccessor().getAnnotation(Size.class);
        assertEquals(2, requestSize.min());
        assertEquals(30, requestSize.max());
        assertEquals("id", responseId.getName());
        assertNotNull(responseId.getAccessor().getAnnotation(NotNull.class));
        assertEquals("name", responseName.getName());
        assertNotNull(responseName.getAccessor().getAnnotation(NotBlank.class));
    }

    @Test
    @Tag("tck:generated-artifact.search-metadata")
    void generatedSearchRequestCopiesCriteriaAndExposesAllowedMetadata() {
        TagSearchRequest request = new TagSearchRequest();
        UUID id = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        OffsetDateTime publishedAt = OffsetDateTime.parse("2026-01-02T03:04:05Z");
        OffsetDateTime start = publishedAt.minusDays(1);
        OffsetDateTime end = publishedAt.plusDays(1);
        request.setId(id);
        request.setIdOp(SearchOperator.EQUALS);
        request.setName("platform");
        request.setNameOp(SearchOperator.EQUALS);
        request.setPostsId(postId);
        request.setPostsIdOp(SearchOperator.NOT_EQUALS);
        request.setPostsTitle("title");
        request.setPostsTitleOp(SearchOperator.CONTAINS);
        request.setPostsContent("content");
        request.setPostsContentOp(SearchOperator.STARTS_WITH);
        request.setPostsPublishedAt(publishedAt);
        request.setPostsPublishedAtStart(start);
        request.setPostsPublishedAtEnd(end);
        request.setPostsPublishedAtOp(SearchOperator.BETWEEN);
        request.setPostsStatus(PostStatus.PUBLISHED);
        request.setPostsStatusOp(SearchOperator.IN);
        request.setSearchLogic(SearchLogic.AND);

        TagSearchRequest copy = new TagSearchRequest(request);

        assertEquals(id, copy.getId());
        assertEquals(SearchOperator.EQUALS, copy.getIdOp());
        assertEquals("platform", copy.getName());
        assertEquals(SearchOperator.EQUALS, copy.getNameOp());
        assertEquals(postId, copy.getPostsId());
        assertEquals(SearchOperator.NOT_EQUALS, copy.getPostsIdOp());
        assertEquals("title", copy.getPostsTitle());
        assertEquals(SearchOperator.CONTAINS, copy.getPostsTitleOp());
        assertEquals("content", copy.getPostsContent());
        assertEquals(SearchOperator.STARTS_WITH, copy.getPostsContentOp());
        assertEquals(publishedAt, copy.getPostsPublishedAt());
        assertEquals(start, copy.getPostsPublishedAtStart());
        assertEquals(end, copy.getPostsPublishedAtEnd());
        assertEquals(SearchOperator.BETWEEN, copy.getPostsPublishedAtOp());
        assertEquals(PostStatus.PUBLISHED, copy.getPostsStatus());
        assertEquals(SearchOperator.IN, copy.getPostsStatusOp());
        assertEquals(SearchLogic.AND, copy.getSearchLogic());
        assertEquals(7, copy.requestedSearchCriteria().size());
        assertEquals(List.of("id", "name", "postsId", "postsTitle", "postsContent",
                        "postsPublishedAt", "postsStatus"),
                copy.requestedSearchCriteria().stream().map(criterion -> criterion.path()).toList());
        assertTrue(copy.allowedSearchPaths().contains("name"));
        assertTrue(copy.allowedSortPaths().contains("postsTitle"));
        assertTrue(copy.allowedSearchOperators().get("name").contains(SearchOperator.CONTAINS));
        assertNotNull(copy.toSpecification());
        assertEquals(SearchLogic.OR, new TagSearchRequest().getSearchLogic());
    }

    @Test
    void generatedSearchRequestTreatsEachRangeBoundAsCriteria() {
        OffsetDateTime publishedAt = OffsetDateTime.parse("2026-01-02T03:04:05Z");

        assertEquals(List.of("postsPublishedAt"), criteriaPaths(rangeRequest(publishedAt, null, null)));
        assertEquals(List.of("postsPublishedAt"), criteriaPaths(rangeRequest(null, publishedAt, null)));
        assertEquals(List.of("postsPublishedAt"), criteriaPaths(rangeRequest(null, null, publishedAt)));
        assertTrue(criteriaPaths(rangeRequest(null, null, null)).isEmpty());
    }

    @Test
    @Tag("tck:generated-artifact.search-metadata")
    @Tag("tck:pit.generated-roundtrip")
    void generatedSearchRequestValidatesDepthAndSpecificationCreation() {
        assertEquals(2, new TagSearchRequest().maxSearchPathDepth());

        TagSearchRequest deepRequest =
                new TagSearchRequest() {
                    @Override
                    public List<SearchRequest.SearchCriterion> requestedSearchCriteria() {
                        return List.of(
                                new SearchRequest.SearchCriterion(
                                        "posts.author.tags.name", SearchOperator.EQUALS));
                    }

                    @Override
                    public Set<String> allowedSearchPaths() {
                        return Set.of();
                    }

                    @Override
                    public int maxSearchPathDepth() {
                        return Integer.MAX_VALUE;
                    }
                };

        BadRequestException depth =
                assertThrows(BadRequestException.class, deepRequest::validate);
        assertTrue(depth.getMessage().contains("maximum depth of 2"));

        TagSearchRequest nullCriterionRequest =
                new TagSearchRequest() {
                    @Override
                    public List<SearchRequest.SearchCriterion> requestedSearchCriteria() {
                        return Arrays.asList((SearchRequest.SearchCriterion) null);
                    }
                };

        BadRequestException nullCriterion =
                assertThrows(BadRequestException.class, nullCriterionRequest::validate);
        assertTrue(nullCriterion.getMessage().contains("criterion must not be null"));

        TagSearchRequest throwingValidateRequest =
                new TagSearchRequest() {
                    @Override
                    public void validate() {
                        throw new IllegalStateException("validated");
                    }
                };

        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, throwingValidateRequest::toSpecification);
        assertEquals("validated", thrown.getMessage());
    }

    @Test
    @Tag("tck:generated-artifact.controller-metrics")
    void generatedControllerClampsPaginationReturnsTypedResponseAndRecordsMetrics() {
        TagService service = mock(TagService.class);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        TagController controller = controller(service, registry, 2);
        TagResponseDto dto = TagResponseDto.builder().id(UUID.randomUUID()).name("platform").build();
        when(service.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 2), 1));

        ResponseEntity<PaginatedResponse<TagResponseDto>> response = controller.getAll(PageRequest.of(0, 50));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(List.of(dto), response.getBody().content());
        assertEquals(2, response.getBody().size());
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAll(pageable.capture());
        assertEquals(2, pageable.getValue().getPageSize());
        assertTimer(registry, "GET_ALL", "success");
    }

    @Test
    @Tag("tck:generated-artifact.controller-metrics")
    void generatedControllerRecordsErrorOutcomeBeforeRethrowing() {
        TagService service = mock(TagService.class);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        TagController controller = controller(service, registry, 10);
        IllegalStateException failure = new IllegalStateException("boom");
        when(service.findById(any(UUID.class))).thenThrow(failure);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> controller.getOne(UUID.randomUUID()));

        assertSame(failure, thrown);
        assertTimer(registry, "GET_ONE", "error");
    }

    private static TagController controller(
            TagService service, SimpleMeterRegistry registry, int maxPageSize) {
        ObjectProvider<MeterRegistry> meterRegistry = new ObjectProvider<>() {
            @Override
            public MeterRegistry getObject(Object... args) {
                return registry;
            }

            @Override
            public MeterRegistry getIfAvailable() {
                return registry;
            }

            @Override
            public MeterRegistry getObject() {
                return registry;
            }
        };
        return new TagController(service, meterRegistry, maxPageSize, 100, 100, 100, 100, 5);
    }

    private static void assertTimer(SimpleMeterRegistry registry, String operation, String outcome) {
        Timer timer = registry.find("crudcraft.generated.operation")
                .tag("model", "Tag")
                .tag("operation", operation)
                .tag("outcome", outcome)
                .timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
    }

    private static TagSearchRequest rangeRequest(
            OffsetDateTime exact, OffsetDateTime start, OffsetDateTime end) {
        TagSearchRequest request = new TagSearchRequest();
        request.setPostsPublishedAt(exact);
        request.setPostsPublishedAtStart(start);
        request.setPostsPublishedAtEnd(end);
        request.setPostsPublishedAtOp(SearchOperator.BETWEEN);
        return request;
    }

    private static List<String> criteriaPaths(TagSearchRequest request) {
        return request.requestedSearchCriteria().stream()
                .map(criterion -> criterion.path())
                .toList();
    }
}

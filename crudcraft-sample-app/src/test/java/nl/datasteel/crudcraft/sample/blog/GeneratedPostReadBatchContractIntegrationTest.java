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

package nl.datasteel.crudcraft.sample.blog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import nl.datasteel.crudcraft.sample.tck.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class GeneratedPostReadBatchContractIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @Tag("tck:endpoint.get-all")
    @Tag("tck:endpoint.get-all-ref")
    @Tag("tck:endpoint.get-one")
    @Tag("tck:endpoint.exists")
    @Tag("tck:pagination.offset")
    @Tag("tck:pagination.max-size-clamp")
    void generatedReadEndpointsSupportPaginationClampReferencesAndHeadExists() throws Exception {
        String token = bearerToken();
        UUID postId = firstPostId();

        JsonNode posts =
                json(
                        mockMvc.perform(
                                        get("/posts")
                                                .param("page", "0")
                                                .param("size", "1000")
                                                .header(HttpHeaders.AUTHORIZATION, token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(100, posts.path("size").asInt());
        assertTrue(posts.path("content").isArray());

        JsonNode refs =
                json(
                        mockMvc.perform(
                                        get("/posts/ref")
                                                .param("page", "0")
                                                .param("size", "1")
                                                .header(HttpHeaders.AUTHORIZATION, token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(1, refs.path("size").asInt());
        assertFalse(refs.at("/content/0/id").asText().isBlank());
        assertFalse(refs.at("/content/0").has("content"));

        JsonNode one =
                json(
                        mockMvc.perform(
                                        get("/posts/{id}", postId)
                                                .header(HttpHeaders.AUTHORIZATION, token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(postId.toString(), one.path("id").asText());

        mockMvc.perform(head("/posts/exists/{id}", postId).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("tck:pagination.sort-asc")
    @Tag("tck:pagination.sort-desc")
    @Tag("tck:pagination.invalid-sort")
    void generatedReadEndpointsApplySortsAndRejectInvalidSortPaths() throws Exception {
        String token = bearerToken();

        JsonNode ascending =
                json(
                        mockMvc.perform(
                                        get("/posts")
                                                .param("page", "0")
                                                .param("size", "10")
                                                .param("sort", "title,asc")
                                                .header(HttpHeaders.AUTHORIZATION, token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertSorted(titles(ascending), true);

        JsonNode descending =
                json(
                        mockMvc.perform(
                                        get("/posts")
                                                .param("page", "0")
                                                .param("size", "10")
                                                .param("sort", "title,desc")
                                                .header(HttpHeaders.AUTHORIZATION, token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertSorted(titles(descending), false);

        mockMvc.perform(
                        get("/posts")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "doesNotExist,asc")
                                .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Tag("tck:projection.named-list-page")
    @Tag("tck:projection.named-list-get-one")
    void generatedNamedListProjectionEndpointsReturnOnlyListDtoFields() throws Exception {
        String token = bearerToken();
        JsonNode page =
                json(
                        mockMvc.perform(
                                        get("/posts/list")
                                                .param("page", "0")
                                                .param("size", "2")
                                                .param("sort", "title,asc")
                                                .header(HttpHeaders.AUTHORIZATION, token))
                                .andExpect(status().isOk())
                                .andReturn());

        assertEquals(2, page.path("content").size());
        JsonNode first = page.at("/content/0");
        assertFalse(first.path("id").asText().isBlank());
        assertFalse(first.path("title").asText().isBlank());
        assertTrue(first.has("summary"));
        assertFalse(first.has("content"));
        assertFalse(first.has("author"));
        assertFalse(first.has("status"));

        JsonNode one =
                json(
                        mockMvc.perform(
                                        get("/posts/list/{id}", first.path("id").asText())
                                                .header(HttpHeaders.AUTHORIZATION, token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(first.path("id").asText(), one.path("id").asText());
        assertFalse(one.has("content"));
        assertFalse(one.has("author"));
    }

    @Test
    void generatedReadEndpointsRejectUnauthenticatedMalformedAndMissingInputs() throws Exception {
        String token = bearerToken();

        mockMvc.perform(get("/posts").param("size", "1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/posts/not-a-uuid").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/posts/{id}", UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound());

        mockMvc.perform(head("/posts/exists/not-a-uuid").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Tag("tck:endpoint.bulk-upsert")
    void generatedBulkUpsertCreatesRowsAndReturnsDtos() throws Exception {
        String token = bearerToken();
        Map<String, Object> first = validPostPayload("Batch Upsert Generated One");
        Map<String, Object> second = validPostPayload("Batch Upsert Generated Two");

        JsonNode body =
                json(
                        mockMvc.perform(
                                        post("/posts/batch/upsert")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(List.of(first, second)))
                                                .header(HttpHeaders.AUTHORIZATION, token))
                                .andExpect(status().isCreated())
                                .andReturn());

        JsonNode succeeded = body.path("succeeded");
        assertEquals(2, succeeded.size());
        assertEquals(0, body.path("failed").size());
        assertEquals("Batch Upsert Generated One", succeeded.get(0).path("title").asText());
        assertFalse(succeeded.get(0).path("id").asText().isBlank());
    }

    @Test
    void generatedBulkUpsertRejectsUnauthenticatedMalformedMethodAndMediaType() throws Exception {
        String token = bearerToken();

        mockMvc.perform(
                        post("/posts/batch/upsert")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(List.of(validPostPayload("No Auth")))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(
                        post("/posts/batch/upsert")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[{broken]")
                                .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/posts/batch/upsert").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(
                        post("/posts/batch/upsert")
                                .contentType(MediaType.TEXT_PLAIN)
                                .content("not json")
                                .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnsupportedMediaType());
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private List<String> titles(JsonNode page) {
        List<String> titles = new ArrayList<>();
        page.path("content").forEach(node -> titles.add(node.path("title").asText()));
        return titles;
    }

    private void assertSorted(List<String> values, boolean ascending) {
        for (int i = 1; i < values.size(); i++) {
            int comparison = values.get(i - 1).compareTo(values.get(i));
            assertTrue(
                    ascending ? comparison <= 0 : comparison >= 0,
                    () -> "Expected sorted titles but got " + values);
        }
    }

    private Map<String, Object> validPostPayload(String title) {
        SeedIds seed = seedIds();
        return Map.of(
                "title", title,
                "content", "Long enough generated integration-test content.",
                "summary", "Generated integration-test summary",
                "authorId", seed.authorId(),
                "categoryId", seed.categoryId(),
                "tagIds", List.of(seed.tagId()));
    }

    private UUID firstPostId() {
        return jdbcTemplate.queryForObject("select id from posts limit 1", UUID.class);
    }

    private SeedIds seedIds() {
        UUID authorId = jdbcTemplate.queryForObject("select id from authors limit 1", UUID.class);
        UUID categoryId = jdbcTemplate.queryForObject("select id from categories limit 1", UUID.class);
        UUID tagId = jdbcTemplate.queryForObject("select id from tags limit 1", UUID.class);
        return new SeedIds(authorId, categoryId, tagId);
    }

    private String bearerToken() throws Exception {
        String payload =
                objectMapper.writeValueAsString(Map.of("username", "admin", "password", "password"));
        MvcResult result =
                mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(status().isOk())
                        .andReturn();
        return "Bearer " + objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
    }

    private record SeedIds(UUID authorId, UUID categoryId, UUID tagId) {}
}

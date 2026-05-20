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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class GeneratedPostCrudEndpointIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @Tag("tck:template.full")
    @Tag("tck:endpoint.post")
    @Tag("tck:endpoint.put")
    @Tag("tck:endpoint.patch")
    @Tag("tck:endpoint.delete")
    void generatedCrudEndpointsSupportHappyPathLifecycle() throws Exception {
        SeedIds seed = seedIds();
        Map<String, Object> createPayload = validPostPayload(seed, "Integration Generated Post");

        JsonNode created =
                json(
                        mockMvc.perform(
                                        post("/posts")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(createPayload))
                                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                                .andExpect(status().isCreated())
                                .andReturn());
        String id = created.path("id").asText();
        assertFalse(id.isBlank());
        assertEquals("Integration Generated Post", created.path("title").asText());

        mockMvc.perform(get("/posts/exists/{id}", id).header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk());

        Map<String, Object> updatePayload = validPostPayload(seed, "Integration Generated Post Updated");
        JsonNode updated =
                json(
                        mockMvc.perform(
                                        put("/posts/{id}", id)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(updatePayload))
                                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals("Integration Generated Post Updated", updated.path("title").asText());

        JsonNode patched =
                json(
                        mockMvc.perform(
                                        patch("/posts/{id}", id)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                        objectMapper.writeValueAsString(
                                                                Map.of("summary", "patched summary")))
                                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals("patched summary", patched.path("summary").asText());

        mockMvc.perform(delete("/posts/{id}", id).header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/posts/exists/{id}", id).header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Tag("tck:exception.unauthorized")
    @Tag("tck:exception.not-found")
    @Tag("tck:exception.bad-request")
    void generatedCrudEndpointsRejectUnauthenticatedInvalidAndMissingResources() throws Exception {
        SeedIds seed = seedIds();
        Map<String, Object> payload = validPostPayload(seed, "Unauthenticated Generated Post");

        mockMvc.perform(
                        post("/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());

        UUID missing = UUID.randomUUID();
        mockMvc.perform(
                        get("/posts/exists/{id}", missing)
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        get("/posts/exists/not-a-uuid")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(
                        post("/posts/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("title", "bad")))
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Tag("tck:endpoint.bulk-create")
    @Tag("tck:endpoint.bulk-update")
    @Tag("tck:endpoint.bulk-patch")
    @Tag("tck:endpoint.bulk-delete")
    @Tag("tck:endpoint.find-by-ids")
    void generatedBulkEndpointsSupportHappyPaths() throws Exception {
        SeedIds seed = seedIds();
        Map<String, Object> first = validPostPayload(seed, "Bulk Generated Post One");
        Map<String, Object> second = validPostPayload(seed, "Bulk Generated Post Two");

        JsonNode created =
                json(
                        mockMvc.perform(
                                        post("/posts/batch")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(List.of(first, second)))
                                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                                .andExpect(status().isCreated())
                                .andReturn());
        JsonNode createdItems = created.path("succeeded");
        assertEquals(2, createdItems.size());
        assertEquals(0, created.path("failed").size());

        String firstId = createdItems.get(0).path("id").asText();
        String secondId = createdItems.get(1).path("id").asText();
        Map<String, Object> updatedPayload = validPostPayload(seed, "Bulk Generated Post Updated");
        JsonNode updated =
                json(
                        mockMvc.perform(
                                        put("/posts/batch")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                        objectMapper.writeValueAsString(
                                                                List.of(identified(firstId, updatedPayload))))
                                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(
                "Bulk Generated Post Updated",
                updated.path("succeeded").get(0).path("title").asText());

        JsonNode patched =
                json(
                        mockMvc.perform(
                                        patch("/posts/batch")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                        objectMapper.writeValueAsString(
                                                                List.of(
                                                                        identified(
                                                                                firstId,
                                                                                validPostPayload(
                                                                                        seed,
                                                                                        "Bulk "
                                                                                                + "Generated Post "
                                                                                                + "Patched")))))
                                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(
                "Bulk Generated Post Patched",
                patched.path("succeeded").get(0).path("title").asText());

        JsonNode byIds =
                json(
                        mockMvc.perform(
                                        post("/posts/batch/ids")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(List.of(firstId, secondId)))
                                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(2, byIds.path("totalElements").asInt());

        mockMvc.perform(
                        delete("/posts/batch/delete")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(List.of(firstId, secondId)))
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.succeeded.length()").value(2))
                .andExpect(jsonPath("$.failed.length()").value(0));
    }

    @Test
    void generatedBulkEndpointsRejectUnauthenticatedMalformedAndMissingResources() throws Exception {
        SeedIds seed = seedIds();
        Map<String, Object> payload = validPostPayload(seed, "Bulk Unhappy Generated Post");

        mockMvc.perform(
                        post("/posts/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(List.of(payload))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(
                        post("/posts/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[{\"title\":\"bad\"}]")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(
                        put("/posts/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                List.of(identified(UUID.randomUUID().toString(), payload))))
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isMultiStatus())
                .andExpect(jsonPath("$.succeeded.length()").value(0))
                .andExpect(jsonPath("$.failed[0].index").value(0));

        mockMvc.perform(
                        post("/posts/batch/ids")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[\"not-a-uuid\"]")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Tag("tck:endpoint.race")
    @Tag("tck:endpoint.create-concurrent")
    void generatedCreateEndpointsHandleConcurrentRequests() throws Exception {
        String token = bearerToken();
        SeedIds seed = seedIds();

        int requestCount = 24;
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        List<Callable<String>> writers =
                new ArrayList<>();
        for (int i = 0; i < requestCount; i++) {
            final int index = i;
            writers.add(
                    () -> {
                        ready.countDown();
                        start.await();
                        MvcResult created =
                                mockMvc.perform(
                                                post("/posts")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                objectMapper.writeValueAsString(
                                                                        validPostPayload(
                                                                                seed,
                                                                                "Concurrent Generated Post "
                                                                                        + index
                                                                                        + "-"
                                                                                        + UUID
                                                                                                .randomUUID())))
                                                        .header(HttpHeaders.AUTHORIZATION, token))
                                        .andExpect(status().isCreated())
                                        .andReturn();
                        return objectMapper
                                .readTree(created.getResponse().getContentAsString())
                                .path("id")
                                .asText();
                    });
        }

        List<Future<String>> futures = writers.stream().map(executor::submit).toList();
        try {
            assertTrue(ready.await(10, TimeUnit.SECONDS), "all workers became ready");
            start.countDown();

            HashSet<String> ids = new HashSet<>();
            for (Future<String> future : futures) {
                ids.add(future.get(30, TimeUnit.SECONDS));
            }

            assertEquals(requestCount, ids.size());
            assertEquals(
                    requestCount,
                    ids.stream().filter(id -> id != null && !id.isBlank()).count());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @Tag("tck:endpoint.count")
    @Tag("tck:endpoint.validate")
    @Tag("tck:endpoint.export")
    @Tag("tck:export.limit-zero")
    void generatedSupportEndpointsSupportHappyPaths() throws Exception {
        JsonNode count = getJson("/posts/count");
        assertTrue(count.path("count").asLong() > 0);

        mockMvc.perform(
                        post("/posts/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                validPostPayload(seedIds(), "Validated Post")))
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk());

        MvcResult csvStart =
                mockMvc.perform(
                                get("/posts/export")
                                        .param("format", "csv")
                                        .param("limit", "0")
                                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                        .andReturn();
        if (csvStart.getRequest().isAsyncStarted()) {
            mockMvc.perform(asyncDispatch(csvStart))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv"));
        } else {
            assertEquals(200, csvStart.getResponse().getStatus());
            assertEquals("text/csv", csvStart.getResponse().getHeader(HttpHeaders.CONTENT_TYPE));
        }
    }

    @Test
    @Tag("tck:export.unknown-format")
    @Tag("tck:export.negative-limit")
    void generatedSupportEndpointsRejectBadInputs() throws Exception {
        mockMvc.perform(get("/posts/count"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(
                        post("/posts/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("title", "bad")))
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(
                        get("/posts/export")
                                .param("format", "pdf")
                                .param("limit", "2")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        get("/posts/export")
                                .param("format", "json")
                                .param("limit", "-1")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isBadRequest());
    }

    private JsonNode getJson(String path, String... params) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request =
                get(path).header(HttpHeaders.AUTHORIZATION, bearerToken());
        requireParamPairs(params);
        for (int i = 0; i + 1 < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        return json(mockMvc.perform(request).andExpect(status().isOk()).andReturn());
    }

    private static void requireParamPairs(String[] params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("params must contain key/value pairs");
        }
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private Map<String, Object> validPostPayload(SeedIds seed, String title) {
        return Map.of(
                "title", title,
                "content", "Long enough generated integration-test content.",
                "summary", "Generated integration-test summary",
                "authorId", seed.authorId(),
                "categoryId", seed.categoryId(),
                "tagIds", List.of(seed.tagId()));
    }

    private SeedIds seedIds() {
        UUID authorId = jdbcTemplate.queryForObject("select id from authors limit 1", UUID.class);
        UUID categoryId = jdbcTemplate.queryForObject("select id from categories limit 1", UUID.class);
        UUID tagId = jdbcTemplate.queryForObject("select id from tags limit 1", UUID.class);
        return new SeedIds(authorId, categoryId, tagId);
    }

    private Map<String, Object> identified(String id, Object data) {
        return Map.of("id", id, "data", data);
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

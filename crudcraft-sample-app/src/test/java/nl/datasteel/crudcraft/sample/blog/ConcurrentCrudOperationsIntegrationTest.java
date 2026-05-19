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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import nl.datasteel.crudcraft.sample.tck.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class ConcurrentCrudOperationsIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @Tag("tck:endpoint.create-concurrent")
    @Tag("tck:endpoint.race")
    void concurrentGeneratedCreatesCompleteWithoutRaceFailures() throws Exception {
        int requestCount = 12;
        SeedIds seed = seedIds();
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        String token = bearerToken();
        var executor = Executors.newFixedThreadPool(requestCount);
        List<Future<Integer>> futures = new ArrayList<>();

        try {
            for (int index = 0; index < requestCount; index++) {
                int captured = index;
                futures.add(
                        executor.submit(
                                () -> {
                                    ready.countDown();
                                    assertTrue(start.await(10, TimeUnit.SECONDS));
                                    return createPost(token, seed, captured);
                                }));
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS), "all workers became ready");
            start.countDown();

            for (Future<Integer> future : futures) {
                assertEquals(201, future.get(30, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private int createPost(String token, SeedIds seed, int index) throws Exception {
        String payload =
                objectMapper.writeValueAsString(
                        validPostPayload(
                                seed, "Concurrent create " + index + " " + UUID.randomUUID()));

        return mockMvc.perform(
                        post("/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", token)
                                .content(payload))
                .andReturn()
                .getResponse()
                .getStatus();
    }

    private String bearerToken() throws Exception {
        String payload =
                objectMapper.writeValueAsString(Map.of("username", "admin", "password", "password"));
        MvcResult result =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(payload))
                        .andExpect(status().isOk())
                        .andReturn();
        return "Bearer "
                + objectMapper.readTree(result.getResponse().getContentAsString())
                        .get("token")
                        .asText();
    }

    private Map<String, Object> validPostPayload(SeedIds seed, String title) {
        return Map.of(
                "title",
                title,
                "content",
                "Long enough generated integration-test content.",
                "summary",
                "Generated integration-test summary",
                "authorId",
                seed.authorId(),
                "categoryId",
                seed.categoryId(),
                "tagIds",
                List.of(seed.tagId()));
    }

    private SeedIds seedIds() {
        UUID authorId = jdbcTemplate.queryForObject("select id from authors limit 1", UUID.class);
        UUID categoryId =
                jdbcTemplate.queryForObject("select id from categories limit 1", UUID.class);
        UUID tagId = jdbcTemplate.queryForObject("select id from tags limit 1", UUID.class);
        return new SeedIds(authorId, categoryId, tagId);
    }

    private record SeedIds(UUID authorId, UUID categoryId, UUID tagId) {}
}

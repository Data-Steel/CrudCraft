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
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class GeneratedPostEndpointIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    void listProjectionReturnsGeneratedSummaryDto() throws Exception {
        JsonNode body = getJson("/posts/list", "page", "0", "size", "5");

        JsonNode first = body.at("/content/0");
        assertFalse(first.path("id").asText().isBlank());
        assertFalse(first.path("title").asText().isBlank());
        assertTrue(first.has("summary"));
        assertFalse(first.has("content"));
    }

    @Test
    void listProjectionCanReadSingleProjectedPost() throws Exception {
        String id = getJson("/posts/list", "page", "0", "size", "1")
                .at("/content/0/id")
                .asText();

        JsonNode body = getJson("/posts/list/" + id);

        assertEqualsText(id, body.path("id").asText());
        assertFalse(body.path("title").asText().isBlank());
        assertFalse(body.has("content"));
    }

    @Test
    void generatedSearchEndpointFindsSeededPosts() throws Exception {
        JsonNode body = getJson("/posts/search", "title", "Guide", "titleOp", "CONTAINS", "size", "10");

        assertTrue(body.path("totalElements").asLong() > 0);
        assertTrue(body.at("/content/0/title").asText().contains("Guide"));
    }

    @Test
    void generatedSearchEndpointUsesSpringPageableSize() throws Exception {
        JsonNode body = getJson("/posts/search", "title", "Guide", "titleOp", "CONTAINS", "size", "1");

        assertEqualsText("1", body.path("size").asText());
    }

    @Test
    void listProjectionRequiresAuthenticationAtFilterBoundary() throws Exception {
        mockMvc.perform(get("/posts/list").param("page", "0").param("size", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listProjectionReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/posts/list/{id}", UUID.randomUUID()).header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    private JsonNode getJson(String path, String... params) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request =
                get(path).header("Authorization", bearerToken());
        requireParamPairs(params);
        for (int i = 0; i + 1 < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private static void requireParamPairs(String[] params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("params must contain key/value pairs");
        }
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

    private void assertEqualsText(String expected, String actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}

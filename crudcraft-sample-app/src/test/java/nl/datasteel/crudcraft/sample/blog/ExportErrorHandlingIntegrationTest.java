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
import nl.datasteel.crudcraft.sample.tck.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class ExportErrorHandlingIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    @Tag("tck:export.depth-exceeded")
    void exportDepthExceededReturnsBadRequestWithContext() throws Exception {
        MvcResult result =
                mockMvc.perform(
                                get("/posts/export")
                                        .param("format", "json")
                                        .param("limit", "1")
                                        .param("maxDepth", "0")
                                        .param("includeFields", "author.name")
                                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                        .andExpect(status().isBadRequest())
                        .andReturn();

        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("Requested export field depth exceeds"));
        assertTrue(body.contains("requested_depth=1"));
        assertTrue(body.contains("max_depth=0"));
    }

    @Test
    @Tag("tck:export.unknown-format")
    @Tag("tck:export.negative-limit")
    void malformedExportRequestsReturnBadRequest() throws Exception {
        String token = bearerToken();

        mockMvc.perform(
                        get("/posts/export")
                                .param("format", "pdf")
                                .param("limit", "1")
                                .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        get("/posts/export")
                                .param("format", "json")
                                .param("limit", "-1")
                                .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest());
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
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return "Bearer " + json.get("token").asText();
    }
}

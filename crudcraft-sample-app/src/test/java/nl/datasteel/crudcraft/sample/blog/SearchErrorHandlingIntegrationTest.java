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


class SearchErrorHandlingIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    @Tag("tck:search.invalid-search-field")
    void invalidSearchPathReturnsBadRequestWithContext() throws Exception {
        String field = "nonSearchablePath";

        MvcResult result =
                mockMvc.perform(
                                get("/posts/search")
                                        .param(field, "bad")
                                        .param(field + "Op", "EQUALS")
                                        .param("size", "5")
                                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                        .andExpect(status().isBadRequest())
                        .andReturn();

        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("Unsupported search field"));
        assertTrue(body.contains("requested=" + field));
        assertTrue(body.contains("allowed=["));
    }

    @Test
    @Tag("tck:search.invalid-operator")
    void invalidSearchOperatorReturnsBadRequest() throws Exception {
        mockMvc.perform(
                        get("/posts/search")
                                .param("title", "Guide")
                                .param("titleOp", "NOT_A_REAL_OPERATOR")
                                .param("size", "5")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
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

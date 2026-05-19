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

package nl.datasteel.crudcraft.sample.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import nl.datasteel.crudcraft.sample.tck.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class FieldLevelSecurityE2eIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    @Tag("tck:field-security.redaction-e2e")
    void securedFieldsAreRedactedAcrossCreateListAndExport() throws Exception {
        String adminToken = token("admin");
        String username = "field-security-e2e-explicit-" + UUID.randomUUID();
        String payload =
                objectMapper.writeValueAsString(
                        Map.of(
                                "username",
                                username,
                                "passwordHash",
                                "secret-hash",
                                "internalMemo",
                                "server side value",
                                "roles",
                                new String[] {"VIEWER"}));

        MvcResult created =
                mockMvc.perform(
                                post("/users")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .header("Authorization", "Bearer " + adminToken)
                                        .content(payload))
                        .andExpect(status().isCreated())
                        .andReturn();
        JsonNode createdJson = json(created);
        String id = createdJson.path("id").asText();
        assertFalse(createdJson.has("passwordHash"));
        assertFalse(createdJson.has("internalMemo"));

        JsonNode listed =
                json(
                                mockMvc.perform(
                                                get("/users")
                                                        .param("size", "20")
                                                        .header(
                                                                "Authorization",
                                                                "Bearer " + adminToken))
                                        .andExpect(status().isOk())
                                        .andReturn())
                        .path("content");
        assertRedacted(matchById(listed, id), "users list");

        MvcResult exportStart =
                mockMvc.perform(
                                get("/users/export")
                                        .param("format", "json")
                                        .param("limit", "20")
                                        .header("Authorization", "Bearer " + adminToken))
                        .andExpect(status().isOk())
                        .andReturn();
        MvcResult exportResult =
                exportStart.getRequest().isAsyncStarted()
                        ? mockMvc.perform(asyncDispatch(exportStart))
                                .andExpect(status().isOk())
                                .andReturn()
                        : exportStart;
        assertRedacted(matchById(json(exportResult), id), "users export");
    }

    private JsonNode matchById(JsonNode rows, String id) {
        for (JsonNode row : rows) {
            if (id.equals(row.path("id").asText())) {
                return row;
            }
        }
        return null;
    }

    private void assertRedacted(JsonNode row, String source) {
        assertNotNull(row, "created user should be present in " + source);
        assertFalse(row.has("passwordHash"));
        assertFalse(row.has("internalMemo"));
    }

    private String token(String username) throws Exception {
        String payload =
                objectMapper.writeValueAsString(Map.of("username", username, "password", "password"));
        MvcResult result =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(payload))
                        .andExpect(status().isOk())
                        .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class SecuritySmokeTest extends PostgresIntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @Tag("tck:security.unauthenticated")
    void unauthenticatedRequestIsRejectedAtEndpointBoundary() throws Exception {
        mockMvc.perform(get("/users/export").param("format", "json").param("limit", "5"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("tck:security.unauthenticated")
    void unauthenticatedGeneratedEndpointIsRejected() throws Exception {
        mockMvc.perform(get("/posts")).andExpect(status().isUnauthorized());
    }

    @Test
    void viewerCannotAccessAdminOnlyUsersEndpoint() throws Exception {
        String viewerToken = login("viewer", "password");
        mockMvc.perform(
                        get("/users/export")
                                .param("format", "json")
                                .param("limit", "5")
                                .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportRespectsFieldSecurityForUsers() throws Exception {
        String adminToken = login("admin", "password");
        MvcResult result =
                mockMvc.perform(
                                get("/users/export")
                                        .param("format", "json")
                                        .param("limit", "5")
                                        .header("Authorization", "Bearer " + adminToken))
                        .andExpect(status().isOk())
                        .andReturn();
        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains("passwordHash"));
    }

    @Test
    @Tag("tck:field-security.read-denied")
    @Tag("tck:field-security.write-skip")
    @Tag("tck:field-security.write-policy.skip-on-denied")
    void generatedUserControllerRunsFieldSecurityReadAndWriteFilters() throws Exception {
        String adminToken = login("admin", "password");
        String username = "field-security-runtime";
        String payload =
                objectMapper.writeValueAsString(
                        Map.of(
                                "username",
                                username,
                                "passwordHash",
                                "secret-hash",
                                "internalMemo",
                                "client supplied",
                                "roles",
                                new String[] {"VIEWER"}));

        MvcResult result =
                mockMvc.perform(
                                post("/users")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .header("Authorization", "Bearer " + adminToken)
                                        .content(payload))
                        .andExpect(status().isCreated())
                        .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertFalse(json.has("passwordHash"));
        assertFalse(json.has("internalMemo"));
        assertEquals(
                "secret-hash",
                jdbcTemplate.queryForObject(
                        "select password_hash from app_users where username = ?",
                        String.class,
                        username));
        assertNull(
                jdbcTemplate.queryForObject(
                        "select internal_memo from app_users where username = ?",
                        String.class,
                        username));
    }

    @Test
    @Tag("tck:field-security.write-policy.fail-on-denied")
    void generatedUserControllerFailsDeniedWriteWhenPolicyRequiresIt() throws Exception {
        String viewerToken = login("viewer", "password");
        String payload =
                objectMapper.writeValueAsString(
                        Map.of(
                                "name",
                                "field-security-fail-runtime",
                                "guardedSecret",
                                "client supplied"));

        mockMvc.perform(
                        post("/writepolicyprobes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + viewerToken)
                                .content(payload))
                .andExpect(status().isForbidden());

        assertEquals(
                0,
                jdbcTemplate.queryForObject(
                        "select count(*) from write_policy_probes where name = ?",
                        Integer.class,
                        "field-security-fail-runtime"));
    }

    @Test
    void generatedUserControllerRedactsSecuredFieldsOnReadResponse() throws Exception {
        String adminToken = login("admin", "password");
        String username = "field-security-read-runtime";
        String payload =
                objectMapper.writeValueAsString(
                        Map.of(
                                "username",
                                username,
                                "passwordHash",
                                "secret-hash",
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

        JsonNode createdJson = objectMapper.readTree(created.getResponse().getContentAsString());
        String id = createdJson.get("id").asText();
        jdbcTemplate.update(
                "update app_users set internal_memo = ? where username = ?",
                "server-side note",
                username);

        MvcResult read =
                mockMvc.perform(get("/users/" + id).header("Authorization", "Bearer " + adminToken))
                        .andExpect(status().isOk())
                        .andReturn();

        JsonNode json = objectMapper.readTree(read.getResponse().getContentAsString());
        assertFalse(json.has("passwordHash"));
        assertEquals("server-side note", json.get("internalMemo").asText());
    }

    @Test
    @Tag("tck:field-security.redaction-e2e")
    void generatedUserControllerRedactsSecuredFieldsAcrossReadAndExport() throws Exception {
        String adminToken = login("admin", "password");
        String username = "field-security-e2e-" + UUID.randomUUID();
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

        JsonNode createdJson = objectMapper.readTree(created.getResponse().getContentAsString());
        String id = createdJson.path("id").asText();
        assertFalse(createdJson.has("passwordHash"));
        assertFalse(createdJson.has("internalMemo"));

        JsonNode list =
                json(
                        mockMvc.perform(
                                        get("/users")
                                                .param("size", "20")
                                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andReturn());
        JsonNode matched = null;
        for (JsonNode entry : list.path("content")) {
            if (id.equals(entry.path("id").asText())) {
                matched = entry;
                break;
            }
        }
        assertNotNull(matched, "created user should be in users list");
        assertFalse(matched.has("passwordHash"));
        assertFalse(matched.has("internalMemo"));

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
        JsonNode exported = json(exportResult);
        matched = null;
        for (JsonNode entry : exported) {
            if (id.equals(entry.path("id").asText())) {
                matched = entry;
                break;
            }
        }
        assertNotNull(matched, "created user should be in users export");
        assertFalse(matched.has("passwordHash"));
        assertFalse(matched.has("internalMemo"));
    }

    private String login(String username, String password) throws Exception {
        String payload =
                objectMapper.writeValueAsString(Map.of("username", username, "password", password));
        MvcResult result =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(payload))
                        .andExpect(status().isOk())
                        .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}

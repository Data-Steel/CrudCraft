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

package nl.datasteel.crudcraft.integration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import nl.datasteel.crudcraft.sample.CrudCraftSampleApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(
        classes = CrudCraftSampleApplication.class,
        properties = {
            "crudcraft.security.jwt.secret=test-only-jwt-secret-value-with-32-plus-chars"
        })
@AutoConfigureMockMvc
class CrossModuleSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Value("${crudcraft.security.jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void seedScopedRecords() {
        jdbcTemplate.update("delete from scope_records");
        insertScoped("visible-record", "tenant-a", "client-a", "owner-a");
        insertScoped("hidden-record", "tenant-b", "client-a", "owner-a");
    }

    @Test
    void rowSecurityAppliesToSearchAndReferenceEndpoints() throws Exception {
        String token = scopedToken("owner-a", "tenant-a", "client-a");

        JsonNode search =
                json(
                        mockMvc.perform(
                                        get("/scopedrecords/search")
                                                .param("name", "visible-record")
                                                .param("nameOp", "EQUALS")
                                                .param("limit", "10")
                                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(1, search.get("totalElements").asInt());
        assertEquals("visible-record", search.at("/content/0/name").asText());

        JsonNode refs =
                json(
                        mockMvc.perform(
                                        get("/scopedrecords/ref")
                                                .param("page", "0")
                                                .param("size", "20")
                                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(1, refs.get("totalElements").asInt());
        assertFalse(refs.at("/content/0/id").asText().isBlank());
    }

    @Test
    void userExportRedactsFieldSecurityProtectedColumns() throws Exception {
        String adminToken = login("admin", "password");
        MvcResult start =
                mockMvc.perform(
                                get("/users/export")
                                        .param("format", "json")
                                        .param("limit", "5")
                                        .header("Authorization", "Bearer " + adminToken))
                        .andReturn();

        MvcResult result =
                start.getRequest().isAsyncStarted()
                        ? mockMvc.perform(asyncDispatch(start)).andExpect(status().isOk()).andReturn()
                        : start;
        if (!start.getRequest().isAsyncStarted()) {
            assertEquals(200, result.getResponse().getStatus());
        }

        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains("passwordHash"));
        assertFalse(body.contains("password_hash"));
    }

    @Test
    void sampleShowsInheritanceEndpointsAndPolicyProtectedUserSurface() throws Exception {
        String adminToken = login("admin", "password");
        String viewerToken = login("viewer", "password");

        mockMvc.perform(
                        get("/articles")
                                .param("page", "0")
                                .param("size", "5")
                                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(
                        get("/tutorials")
                                .param("page", "0")
                                .param("size", "5")
                                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/users")
                                .param("page", "0")
                                .param("size", "5")
                                .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden());
    }

    private String login(String username, String password) throws Exception {
        MvcResult loginResult =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        Map.of(
                                                                "username",
                                                                username,
                                                                "password",
                                                                password))))
                        .andExpect(status().isOk())
                        .andReturn();
        return objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
    }

    private String scopedToken(String subject, String tenantId, String clientId) {
        return JWT.create()
                .withSubject(subject)
                .withClaim("roles", List.of("USER"))
                .withClaim("tenant_id", tenantId)
                .withClaim("client_id", clientId)
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    private void insertScoped(String name, String tenantId, String clientId, String ownerId) {
        jdbcTemplate.update(
                "insert into scope_records (id, name, tenant_id, client_id, owner_id) values (?, ?,"
                        + " ?, ?, ?)",
                UUID.randomUUID(),
                name,
                tenantId,
                clientId,
                ownerId);
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}

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

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import nl.datasteel.crudcraft.sample.tck.PostgresIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class ScopedRowSecurityIntegrationTest extends PostgresIntegrationTestBase {

    private static final String BASE_PATH = "/scopedrecords";
    private static final String VISIBLE_NAME = "visible-record";
    private static final String TENANT_MISMATCH_NAME = "tenant-mismatch-record";
    private static final String CLIENT_MISMATCH_NAME = "client-mismatch-record";
    private static final String OWNER_MISMATCH_NAME = "owner-mismatch-record";

    @Autowired private MockMvc mockMvc;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private ObjectMapper objectMapper;

    @Value("${crudcraft.security.jwt.secret}")
    private String jwtSecret;

    private UUID visibleId;
    private UUID tenantMismatchId;

    @BeforeEach
    void seedData() {
        jdbcTemplate.update("delete from scope_records");

        visibleId = UUID.randomUUID();
        tenantMismatchId = UUID.randomUUID();
        UUID clientMismatchId = UUID.randomUUID();
        UUID ownerMismatchId = UUID.randomUUID();

        insert(visibleId, VISIBLE_NAME, "tenant-a", "client-a", "owner-a");
        insert(tenantMismatchId, TENANT_MISMATCH_NAME, "tenant-b", "client-a", "owner-a");
        insert(clientMismatchId, CLIENT_MISMATCH_NAME, "tenant-a", "client-b", "owner-a");
        insert(ownerMismatchId, OWNER_MISMATCH_NAME, "tenant-a", "client-a", "owner-b");
    }

    @Test
    @Tag("tck:row-security.tenant-claim")
    @Tag("tck:row-security.client-claim")
    @Tag("tck:row-security.owner-claim")
    @Tag("tck:row-security.mismatch-hidden")
    @Tag("tck:row-security.search-count-export")
    void tenantClientOwnerScopesApplyToReadSearchCountAndExport() throws Exception {
        String token = scopedToken("owner-a", "tenant-a", "client-a");

        JsonNode all =
                json(
                        mockMvc.perform(
                                        get(BASE_PATH)
                                                .param("page", "0")
                                                .param("size", "20")
                                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(1L, all.get("totalElements").asLong());
        assertEquals(VISIBLE_NAME, all.at("/content/0/name").asText());

        mockMvc.perform(
                        get(BASE_PATH + "/{id}", visibleId)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(BASE_PATH + "/{id}", tenantMismatchId)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        JsonNode refs =
                json(
                        mockMvc.perform(
                                        get(BASE_PATH + "/ref")
                                                .param("page", "0")
                                                .param("size", "20")
                                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(1L, refs.get("totalElements").asLong());

        JsonNode byIds =
                json(
                        mockMvc.perform(
                                        post(BASE_PATH + "/batch/ids")
                                                .contentType("application/json")
                                                .content(
                                                        objectMapper.writeValueAsString(
                                                                List.of(
                                                                        visibleId,
                                                                        tenantMismatchId)))
                                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(1L, byIds.get("totalElements").asLong());
        assertEquals(VISIBLE_NAME, byIds.at("/content/0/name").asText());

        mockMvc.perform(
                        get(BASE_PATH + "/exists/{id}", visibleId)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(BASE_PATH + "/exists/{id}", tenantMismatchId)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        JsonNode matchingSearch =
                json(
                        mockMvc.perform(
                                        get(BASE_PATH + "/search")
                                                .param("name", VISIBLE_NAME)
                                                .param("nameOp", "EQUALS")
                                                .param("limit", "20")
                                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(1L, matchingSearch.get("totalElements").asLong());
        assertEquals(VISIBLE_NAME, matchingSearch.at("/content/0/name").asText());

        JsonNode outOfScopeSearch =
                json(
                        mockMvc.perform(
                                        get(BASE_PATH + "/search")
                                                .param("name", TENANT_MISMATCH_NAME)
                                                .param("nameOp", "EQUALS")
                                                .param("limit", "20")
                                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(0L, outOfScopeSearch.get("totalElements").asLong());

        JsonNode count =
                json(
                        mockMvc.perform(
                                        get(BASE_PATH + "/count")
                                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(1L, count.get("count").asLong());

        MvcResult exportStart =
                mockMvc.perform(
                                get(BASE_PATH + "/export")
                                        .param("format", "json")
                                        .param("limit", "20")
                                        .header("Authorization", "Bearer " + token))
                        .andExpect(request().asyncStarted())
                        .andReturn();
        JsonNode exported =
                json(
                        mockMvc.perform(asyncDispatch(exportStart))
                                .andExpect(status().isOk())
                                .andReturn());
        assertTrue(exported.isArray());
        assertEquals(1, exported.size());
        assertEquals(VISIBLE_NAME, exported.get(0).get("name").asText());
    }

    @Test
    @Tag("tck:export.entity-mode")
    @Tag("tck:export.mode.entity")
    @Tag("tck:export.empty-limit")
    void scopedExportHonorsFieldFilteringEmptyLimitAndBadRequests() throws Exception {
        String token = scopedToken("owner-a", "tenant-a", "client-a");

        JsonNode included =
                json(
                        export(
                                        token,
                                        "format",
                                        "json",
                                        "limit",
                                        "20",
                                        "includeFields",
                                        "name")
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(1, included.size());
        assertEquals(VISIBLE_NAME, included.get(0).get("name").asText());
        assertFalse(included.get(0).has("id"));

        JsonNode excluded =
                json(
                        export(
                                        token,
                                        "format",
                                        "json",
                                        "limit",
                                        "20",
                                        "includeFields",
                                        "id",
                                        "includeFields",
                                        "name",
                                        "excludeFields",
                                        "id")
                                .andExpect(status().isOk())
                                .andReturn());
        assertEquals(1, excluded.size());
        assertEquals(VISIBLE_NAME, excluded.get(0).get("name").asText());
        assertFalse(excluded.get(0).has("id"));

        JsonNode empty =
                json(export(token, "format", "json", "limit", "0")
                        .andExpect(status().isOk())
                        .andReturn());
        assertTrue(empty.isArray());
        assertEquals(0, empty.size());

        mockMvc.perform(get(BASE_PATH + "/export").param("format", "json").param("limit", "1"))
                .andExpect(status().isUnauthorized());

        export(token, "format", "xml", "limit", "1").andExpect(status().isBadRequest());
        export(token, "format", "json", "limit", "-1").andExpect(status().isBadRequest());
        export(token, "format", "json", "limit", "1", "exportMode", "NOPE")
                .andExpect(status().isBadRequest());
    }

    private void insert(UUID id, String name, String tenantId, String clientId, String ownerId) {
        jdbcTemplate.update(
                "insert into scope_records (id, name, tenant_id, client_id, owner_id) values (?, ?,"
                        + " ?, ?, ?)",
                id,
                name,
                tenantId,
                clientId,
                ownerId);
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

    private org.springframework.test.web.servlet.ResultActions export(String token, String... params)
            throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request =
                get(BASE_PATH + "/export").header("Authorization", "Bearer " + token);
        for (int i = 0; i < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        MvcResult start = mockMvc.perform(request).andReturn();
        if (start.getRequest().isAsyncStarted()) {
            return mockMvc.perform(asyncDispatch(start));
        }
        return new ImmediateResultActions(start);
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private record ImmediateResultActions(MvcResult result)
            implements org.springframework.test.web.servlet.ResultActions {
        @Override
        public org.springframework.test.web.servlet.ResultActions andExpect(
                org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
            matcher.match(result);
            return this;
        }

        @Override
        public org.springframework.test.web.servlet.ResultActions andDo(
                org.springframework.test.web.servlet.ResultHandler handler) throws Exception {
            handler.handle(result);
            return this;
        }

        @Override
        public MvcResult andReturn() {
            return result;
        }
    }
}

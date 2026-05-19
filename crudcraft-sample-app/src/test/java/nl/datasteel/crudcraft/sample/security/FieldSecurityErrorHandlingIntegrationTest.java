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
import nl.datasteel.crudcraft.sample.tck.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class FieldSecurityErrorHandlingIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @Tag("tck:field-security.authorization")
    void nonAdminCannotUseFieldSecuredUserExport() throws Exception {
        mockMvc.perform(
                        get("/users/export")
                                .param("format", "json")
                                .param("limit", "1")
                                .header("Authorization", "Bearer " + token("viewer")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Tag("tck:field-security.write-policy.fail-on-denied")
    void failOnDeniedWritePolicyRejectsRequestWithoutPersisting() throws Exception {
        String payload =
                objectMapper.writeValueAsString(
                        Map.of(
                                "name",
                                "field-security-error-runtime",
                                "guardedSecret",
                                "client supplied"));

        mockMvc.perform(
                        post("/writepolicyprobes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token("viewer"))
                                .content(payload))
                .andExpect(status().isForbidden());

        assertEquals(
                0,
                jdbcTemplate.queryForObject(
                        "select count(*) from write_policy_probes where name = ?",
                        Integer.class,
                        "field-security-error-runtime"));
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
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }
}

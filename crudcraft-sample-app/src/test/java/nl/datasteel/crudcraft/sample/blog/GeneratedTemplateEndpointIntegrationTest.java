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
import java.util.Map;
import java.util.UUID;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class GeneratedTemplateEndpointIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @Tag("tck:template.read-only")
    @Tag("tck:exception.method-not-allowed")
    void readOnlyTemplateAllowsReadsAndRejectsWrites() throws Exception {
        UUID categoryId = jdbcTemplate.queryForObject("select id from categories limit 1", UUID.class);

        mockMvc.perform(
                        get("/categories/{id}", categoryId)
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk());
        mockMvc.perform(
                        get("/categories")
                                .param("size", "1")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"integration category\"}")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(
                        patch("/categories/{id}", categoryId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"description\":\"nope\"}")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(
                        delete("/categories/{id}", categoryId)
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @Tag("tck:template.immutable-write")
    void immutableWriteTemplateAllowsCreateButRejectsMutationEndpoints() throws Exception {
        String uniqueName = "it-" + UUID.randomUUID().toString().substring(0, 8);
        MvcResult created =
                mockMvc.perform(
                                post("/tags")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(Map.of("name", uniqueName)))
                                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                        .andExpect(status().isCreated())
                        .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
        assertTrue(UUID.fromString(id).version() > 0);

        mockMvc.perform(
                        patch("/tags/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"changed\"}")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(
                        delete("/tags/{id}", id)
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get("/tags").header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("tck:template.patch-only")
    @Tag("tck:template.no-delete")
    void noDeleteAndPatchOnlyTemplatesExposeOnlyTheirContractedMutationSurface()
            throws Exception {
        UUID commentId = jdbcTemplate.queryForObject("select id from comments limit 1", UUID.class);
        UUID statsId = jdbcTemplate.queryForObject("select id from post_stats limit 1", UUID.class);

        mockMvc.perform(
                        delete("/comments/{id}", commentId)
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(
                        patch("/poststatses/{id}", statsId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"viewCount\":42}")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/poststatses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"viewCount\":1}")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(
                        delete("/poststatses/{id}", statsId)
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void generatedTemplateEndpointsRejectUnauthenticatedMalformedAndMissingInputs()
            throws Exception {
        mockMvc.perform(get("/categories").param("size", "1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(
                        get("/categories/not-a-uuid")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        get("/categories/{id}", UUID.randomUUID())
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        post("/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"\"}")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().is4xxClientError());
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
}

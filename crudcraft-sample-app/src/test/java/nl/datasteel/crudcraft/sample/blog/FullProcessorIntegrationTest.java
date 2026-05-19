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
import nl.datasteel.crudcraft.sample.tck.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * End-to-end processor contract: annotated entity, generated source compilation, HTTP request,
 * persistence, and generated response DTO shape.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FullProcessorIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    @Tag("tck:processor.full-roundtrip")
    @Tag("tck:generated-code.spring-context")
    void generatedTagEndpointRoundTripsThroughHttpPersistenceAndSearch() throws Exception {
        String token = bearerToken();
        String tagName = "apt-" + UUID.randomUUID().toString().substring(0, 8);

        MvcResult created =
                mockMvc.perform(
                                post("/tags")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(Map.of("name", tagName)))
                                        .header(HttpHeaders.AUTHORIZATION, token))
                        .andExpect(status().isCreated())
                        .andReturn();
        JsonNode createdBody = objectMapper.readTree(created.getResponse().getContentAsString());
        String id = createdBody.path("id").asText();

        assertTrue(UUID.fromString(id).version() > 0);
        assertEquals(tagName, createdBody.path("name").asText());

        MvcResult fetched =
                mockMvc.perform(get("/tags/{id}", id).header(HttpHeaders.AUTHORIZATION, token))
                        .andExpect(status().isOk())
                        .andReturn();
        JsonNode fetchedBody = objectMapper.readTree(fetched.getResponse().getContentAsString());
        assertEquals(id, fetchedBody.path("id").asText());
        assertEquals(tagName, fetchedBody.path("name").asText());

        MvcResult searched =
                mockMvc.perform(
                                get("/tags/search")
                                        .param("name", tagName)
                                        .param("nameOp", "EQUALS")
                                        .param("size", "5")
                                        .header(HttpHeaders.AUTHORIZATION, token))
                        .andExpect(status().isOk())
                        .andReturn();
        JsonNode results = objectMapper.readTree(searched.getResponse().getContentAsString());
        assertEquals(1, results.path("totalElements").asInt());
        assertEquals(tagName, results.at("/content/0/name").asText());
    }

    private String bearerToken() throws Exception {
        String payload =
                objectMapper.writeValueAsString(Map.of("username", "admin", "password", "password"));
        MvcResult result =
                mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(status().isOk())
                        .andReturn();
        return "Bearer " + objectMapper.readTree(result.getResponse().getContentAsString())
                .path("token")
                .asText();
    }
}

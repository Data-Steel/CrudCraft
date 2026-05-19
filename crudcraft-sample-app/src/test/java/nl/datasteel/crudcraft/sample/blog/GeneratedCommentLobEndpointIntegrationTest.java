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
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import nl.datasteel.crudcraft.sample.tck.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class GeneratedCommentLobEndpointIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    @Tag("tck:lob.single")
    @Tag("tck:lob.nullable")
    @Tag("tck:lob.multipart-create")
    @Tag("tck:lob.multipart-update")
    void multipartCreateStoresLobAndPutCanClearItWithEmptyFile() throws Exception {
        String token = bearerToken();
        UUID postId = firstPostId(token);

        JsonNode created =
                json(
                        mockMvc.perform(
                                        multipart("/comments")
                                                .file(dataPart(commentPayload(postId, "with file")))
                                                .file(
                                                        new MockMultipartFile(
                                                                "attachment",
                                                                "note.txt",
                                                                MediaType.TEXT_PLAIN_VALUE,
                                                                "hello file".getBytes(
                                                                        StandardCharsets.UTF_8)))
                                                .header("Authorization", token))
                                .andExpect(status().isCreated())
                                .andReturn());

        String id = created.get("id").asText();
        assertFalse(id.isBlank());
        assertFalse(created.get("attachment").asText().isBlank());

        JsonNode cleared =
                json(
                        mockMvc.perform(
                                        multipart("/comments/{id}", id)
                                                .file(dataPart(commentPayload(postId, "cleared")))
                                                .file(new MockMultipartFile("attachment", new byte[0]))
                                                .header("Authorization", token)
                                                .with(
                                                        request -> {
                                                            request.setMethod("PUT");
                                                            return request;
                                                        }))
                                .andExpect(status().isOk())
                                .andReturn());

        assertEquals(id, cleared.get("id").asText());
        assertFalse(cleared.hasNonNull("attachment"));
    }

    @Test
    @Tag("tck:lob.invalid-request")
    void multipartCreateRejectsUnauthenticatedMissingDataInvalidJsonAndInvalidPayload()
            throws Exception {
        String token = bearerToken();
        UUID postId = firstPostId(token);

        mockMvc.perform(
                        multipart("/comments")
                                .file(dataPart(commentPayload(postId, "unauthenticated"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(
                        multipart("/comments")
                                .file(
                                        new MockMultipartFile(
                                                "attachment",
                                                "note.txt",
                                                MediaType.TEXT_PLAIN_VALUE,
                                                "orphan".getBytes(StandardCharsets.UTF_8)))
                                .header("Authorization", token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        multipart("/comments")
                                .file(
                                        new MockMultipartFile(
                                                "data",
                                                "",
                                                MediaType.APPLICATION_JSON_VALUE,
                                                "{broken".getBytes(StandardCharsets.UTF_8)))
                                .header("Authorization", token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        multipart("/comments")
                                .file(dataPart(Map.of("postId", postId, "authorName", "x")))
                                .header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Tag("tck:lob.invalid-request")
    void multipartUpdateRejectsUnknownCommentWrongMethodAndPlainJsonCreate() throws Exception {
        String token = bearerToken();
        UUID postId = firstPostId(token);

        mockMvc.perform(
                        multipart("/comments/{id}", UUID.randomUUID())
                                .file(dataPart(commentPayload(postId, "missing comment")))
                                .header("Authorization", token)
                                .with(
                                        request -> {
                                            request.setMethod("PATCH");
                                            return request;
                                        }))
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        multipart("/comments/{id}", UUID.randomUUID())
                                .file(dataPart(commentPayload(postId, "delete not allowed")))
                                .header("Authorization", token)
                                .with(
                                        request -> {
                                            request.setMethod("DELETE");
                                            return request;
                                        }))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(
                        post("/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsBytes(
                                                commentPayload(postId, "json")))
                                .header("Authorization", token))
                .andExpect(status().isUnsupportedMediaType());
    }

    private Map<String, Object> commentPayload(UUID postId, String suffix) {
        return Map.of(
                "postId",
                postId,
                "authorName",
                "Integration Tester",
                "authorEmail",
                "integration-" + suffix.replace(' ', '-') + "@example.com",
                "content",
                "Generated multipart comment " + suffix);
    }

    private MockMultipartFile dataPart(Object payload) throws Exception {
        return new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(payload));
    }

    private UUID firstPostId(String token) throws Exception {
        JsonNode posts =
                json(
                        mockMvc.perform(
                                        get("/posts")
                                                .param("page", "0")
                                                .param("size", "1")
                                                .header("Authorization", token))
                                .andExpect(status().isOk())
                                .andReturn());
        return UUID.fromString(posts.at("/content/0/id").asText());
    }

    private String bearerToken() throws Exception {
        MvcResult result =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsBytes(
                                                        Map.of(
                                                                "username",
                                                                "admin",
                                                                "password",
                                                                "password"))))
                        .andExpect(status().isOk())
                        .andReturn();
        return "Bearer "
                + objectMapper.readTree(result.getResponse().getContentAsString())
                        .get("token")
                        .asText();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}

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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class GeneratedPostSearchExportIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @Tag("tck:endpoint.search")
    @Tag("tck:search.string")
    @Tag("tck:search.enum")
    @Tag("tck:search.relation")
    @Tag("tck:search.and-logic")
    @Tag("tck:search.logic.and")
    void generatedSearchSupportsStringEnumRelationAndAndLogic() throws Exception {
        SearchSeed seed = searchSeed();

        JsonNode titleMatch =
                getJson(
                        "/posts/search",
                        "title",
                        seed.titleFragment(),
                        "titleOp",
                        "CONTAINS",
                        "size",
                        "10");
        assertTrue(titleMatch.path("totalElements").asLong() > 0);
        assertTrue(titleMatch.at("/content/0/title").asText().contains(seed.titleFragment()));

        JsonNode relationMatch =
                getJson(
                        "/posts/search",
                        "authorName",
                        seed.authorName(),
                        "authorNameOp",
                        "EQUALS",
                        "tagsName",
                        seed.tagName(),
                        "tagsNameOp",
                        "EQUALS",
                        "searchLogic",
                        "AND",
                        "size",
                        "10");
        assertTrue(relationMatch.path("totalElements").asLong() > 0);

        JsonNode enumMatch =
                getJson(
                        "/posts/search",
                        "status",
                        seed.status(),
                        "statusOp",
                        "EQUALS",
                        "size",
                        "10");
        assertTrue(enumMatch.path("totalElements").asLong() > 0);
        assertEquals(seed.status(), enumMatch.at("/content/0/status").asText());
    }

    @Test
    @Tag("tck:search.string-prefix-suffix")
    @Tag("tck:search.string-regex")
    @Tag("tck:search.not-equals")
    @Tag("tck:search.in-not-in")
    @Tag("tck:search.temporal-before-after")
    @Tag("tck:search.temporal-between")
    @Tag("tck:search.sort")
    @Tag("tck:search.invalid-sort")
    void generatedSearchSupportsAdditionalOperatorsAndSortValidation() throws Exception {
        SearchSeed seed = searchSeed();
        String prefix = firstWord(seed.title());
        String suffix = lastWord(seed.title());

        JsonNode startsWith =
                getJson(
                        "/posts/search",
                        "title",
                        prefix,
                        "titleOp",
                        "STARTS_WITH",
                        "size",
                        "10");
        assertTrue(startsWith.path("totalElements").asLong() > 0);
        assertTrue(startsWith.at("/content/0/title").asText().startsWith(prefix));

        JsonNode endsWith =
                getJson(
                        "/posts/search",
                        "title",
                        suffix,
                        "titleOp",
                        "ENDS_WITH",
                        "size",
                        "10");
        assertTrue(endsWith.path("totalElements").asLong() > 0);
        assertTrue(endsWith.at("/content/0/title").asText().endsWith(suffix));

        JsonNode regexLike =
                getJson(
                        "/posts/search",
                        "title",
                        seed.title(),
                        "titleOp",
                        "REGEX",
                        "size",
                        "10");
        assertTrue(regexLike.path("totalElements").asLong() > 0);
        assertEquals(seed.title(), regexLike.at("/content/0/title").asText());

        JsonNode notEquals =
                getJson(
                        "/posts/search",
                        "title",
                        seed.title(),
                        "titleOp",
                        "NOT_EQUALS",
                        "size",
                        "10");
        assertTrue(notEquals.path("totalElements").asLong() > 0);
        assertFalse(titles(notEquals).contains(seed.title()));

        JsonNode statusIn =
                getJson(
                        "/posts/search",
                        "status",
                        seed.status(),
                        "statusOp",
                        "IN",
                        "size",
                        "10");
        assertTrue(statusIn.path("totalElements").asLong() > 0);
        assertEquals(seed.status(), statusIn.at("/content/0/status").asText());

        JsonNode statusNotIn =
                getJson(
                        "/posts/search",
                        "status",
                        seed.status(),
                        "statusOp",
                        "NOT_IN",
                        "size",
                        "10");
        assertTrue(statusNotIn.path("totalElements").asLong() > 0);
        assertFalse(statuses(statusNotIn).contains(seed.status()));

        JsonNode before =
                getJson(
                        "/posts/search",
                        "publishedAt",
                        OffsetDateTime.now().plusDays(1).toString(),
                        "publishedAtOp",
                        "BEFORE",
                        "size",
                        "10");
        assertTrue(before.path("totalElements").asLong() > 0);

        JsonNode after =
                getJson(
                        "/posts/search",
                        "publishedAt",
                        "2000-01-01T00:00:00Z",
                        "publishedAtOp",
                        "AFTER",
                        "size",
                        "10");
        assertTrue(after.path("totalElements").asLong() > 0);

        JsonNode between =
                getJson(
                        "/posts/search",
                        "publishedAtStart",
                        "2000-01-01T00:00:00Z",
                        "publishedAtEnd",
                        OffsetDateTime.now().plusDays(1).toString(),
                        "publishedAtOp",
                        "BETWEEN",
                        "size",
                        "10");
        assertTrue(between.path("totalElements").asLong() > 0);

        JsonNode sorted =
                getJson(
                        "/posts/search",
                        "title",
                        prefix,
                        "titleOp",
                        "STARTS_WITH",
                        "sort",
                        "title,desc",
                        "size",
                        "10");
        assertSorted(titles(sorted), false);

        MvcResult invalidSort =
                mockMvc.perform(
                                get("/posts/search")
                                        .param("title", prefix)
                                        .param("titleOp", "STARTS_WITH")
                                        .param("sort", "notSearchable,asc")
                                        .param("size", "10")
                                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                        .andExpect(status().isBadRequest())
                        .andReturn();
        String invalidSortBody = invalidSort.getResponse().getContentAsString();
        assertTrue(invalidSortBody.contains("Unsupported sort field"));
        assertTrue(invalidSortBody.contains("notSearchable"));
    }

    @Test
    @Tag("tck:search.or-logic")
    @Tag("tck:search.logic.or")
    void generatedSearchDefaultsToOrLogicWhenLogicParameterIsAbsent() throws Exception {
        SearchSeed seed = searchSeed();

        JsonNode result =
                getJson(
                        "/posts/search",
                        "title",
                        firstWord(seed.title()),
                        "titleOp",
                        "STARTS_WITH",
                        "status",
                        oppositeStatus(seed.status()),
                        "statusOp",
                        "EQUALS",
                        "size",
                        "10");

        assertTrue(result.path("totalElements").asLong() > 0);
    }

    @Test
    @Tag("tck:search.invalid-search-field")
    void generatedSearchRejectsUnknownSearchFieldWithContext() throws Exception {
        String invalidField = "nonSearchablePath";
        MvcResult invalid =
                mockMvc.perform(
                                get("/posts/search")
                                        .param(invalidField, "bad")
                                        .param(invalidField + "Op", "EQUALS")
                                        .param("size", "5")
                                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                        .andExpect(status().isBadRequest())
                        .andReturn();
        String body = invalid.getResponse().getContentAsString();
        assertTrue(body.contains("Unsupported search field"));
        assertTrue(body.contains("requested=" + invalidField));
        assertTrue(body.contains("allowed=["));
    }

    @Test
    @Tag("tck:search.invalid-operator")
    void generatedSearchRejectsUnauthenticatedMalformedAndInvalidRequests() throws Exception {
        mockMvc.perform(get("/posts/search").param("title", "Guide").param("size", "5"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(
                        get("/posts/search")
                                .param("title", "Guide")
                                .param("titleOp", "NOT_A_REAL_OPERATOR")
                                .param("size", "5")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        get("/posts/search")
                                .param("status", "NOT_A_STATUS")
                                .param("statusOp", "EQUALS")
                                .param("size", "5")
                                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Tag("tck:export.csv")
    @Tag("tck:export.json")
    @Tag("tck:export.xlsx")
    @Tag("tck:export.format-normalization")
    @Tag("tck:export.mode.dto")
    void generatedExportStreamsCsvJsonXlsxAndNormalizesFormat() throws Exception {
        MvcResult csv =
                export("/posts/export", Map.of("format", " CSV ", "limit", "2"))
                        .andExpect(status().isOk())
                        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv"))
                        .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                        .andReturn();
        assertTrue(csv.getResponse().getContentAsString().contains("title"));

        MvcResult json =
                export("/posts/export", Map.of("format", "json", "limit", "2"))
                        .andExpect(status().isOk())
                        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/json"))
                        .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                        .andReturn();
        JsonNode rows = objectMapper.readTree(json.getResponse().getContentAsString());
        assertTrue(rows.isArray());
        assertTrue(rows.size() <= 2);

        MvcResult xlsx =
                export("/posts/export", Map.of("format", "xlsx", "limit", "1"))
                        .andExpect(status().isOk())
                        .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                        .andReturn();
        assertTrue(
                xlsx.getResponse()
                        .getContentType()
                        .startsWith(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml"));
        assertEquals(
                "PK",
                new String(
                        xlsx.getResponse().getContentAsByteArray(),
                        0,
                        2,
                        StandardCharsets.US_ASCII));
    }

    @Test
    @Tag("tck:export.field-include")
    @Tag("tck:export.field-exclude")
    @Tag("tck:export.dot-notation")
    @Tag("tck:export.depth-limit")
    void generatedExportHonorsFieldSelectionDotNotationAndDepth() throws Exception {
        JsonNode included =
                exportedJson(
                        "format",
                        "json",
                        "limit",
                        "1",
                        "includeFields",
                        "title");
        assertEquals(1, included.size());
        assertTrue(included.get(0).has("title"));
        assertFalse(included.get(0).has("content"));
        assertFalse(included.get(0).has("author"));

        JsonNode excluded =
                exportedJson(
                        "format",
                        "json",
                        "limit",
                        "1",
                        "includeFields",
                        "title",
                        "includeFields",
                        "content",
                        "excludeFields",
                        "content");
        assertEquals(1, excluded.size());
        assertTrue(excluded.get(0).has("title"));
        assertFalse(excluded.get(0).has("content"));

        JsonNode nested =
                exportedJson(
                        "format",
                        "json",
                        "limit",
                        "1",
                        "includeFields",
                        "author.id");
        assertEquals(1, nested.size());
        assertTrue(nested.get(0).has("author"));
        assertTrue(nested.get(0).path("author").has("id"));
        assertFalse(nested.get(0).path("author").has("name"));

        JsonNode depthLimited =
                exportedJson("format", "json", "limit", "1", "maxDepth", "0");
        assertEquals(1, depthLimited.size());
        assertTrue(depthLimited.get(0).has("title"));
        assertFalse(depthLimited.get(0).has("author"));
        assertFalse(depthLimited.get(0).has("tags"));
    }

    @Test
    @Tag("tck:export.depth-exceeded")
    void generatedExportRejectsTooDeepRelationshipsWithContext() throws Exception {
        MvcResult exceeded =
                mockMvc.perform(
                                get("/posts/export")
                                        .param("format", "json")
                                        .param("limit", "1")
                                        .param("maxDepth", "0")
                                        .param("includeFields", "author.name")
                                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                        .andExpect(status().isBadRequest())
                        .andReturn();
        String body = exceeded.getResponse().getContentAsString();
        assertTrue(body.contains("Requested export field depth exceeds"));
        assertTrue(body.contains("requested_depth=1"));
        assertTrue(body.contains("max_depth=0"));
    }

    @Test
    void generatedExportRejectsUnauthenticatedMalformedAndInvalidRequests() throws Exception {
        mockMvc.perform(get("/posts/export").param("format", "json").param("limit", "1"))
                .andExpect(status().isUnauthorized());

        export("/posts/export", Map.of("format", "pdf", "limit", "1"))
                .andExpect(status().isBadRequest());

        export("/posts/export", Map.of("format", "json", "limit", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/posts/export").header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Tag("tck:field-security.export-redaction")
    void userExportAppliesFieldSecurityAndRbac() throws Exception {
        MvcResult adminExport =
                export("/users/export", Map.of("format", "json", "limit", "3"))
                        .andExpect(status().isOk())
                        .andReturn();
        String body = adminExport.getResponse().getContentAsString();
        assertTrue(objectMapper.readTree(body).isArray());
        assertFalse(body.contains("passwordHash"));
        assertFalse(body.contains("password"));

        String viewer = bearerToken("viewer", "password");
        mockMvc.perform(
                        get("/users")
                                .param("size", "1")
                                .header(HttpHeaders.AUTHORIZATION, viewer))
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        get("/users/export")
                                .param("format", "json")
                                .param("limit", "1")
                                .header(HttpHeaders.AUTHORIZATION, viewer))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/users").param("size", "1"))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions export(
            String path, Map<String, String> params) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request =
                get(path).header(HttpHeaders.AUTHORIZATION, bearerToken());
        params.forEach(request::param);
        MvcResult start = mockMvc.perform(request).andReturn();
        if (start.getRequest().isAsyncStarted()) {
            return mockMvc.perform(asyncDispatch(start));
        }
        return new ImmediateResultActions(start);
    }

    private org.springframework.test.web.servlet.ResultActions export(String path, String... params)
            throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request =
                get(path).header(HttpHeaders.AUTHORIZATION, bearerToken());
        for (int i = 0; i < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        MvcResult start = mockMvc.perform(request).andReturn();
        if (start.getRequest().isAsyncStarted()) {
            return mockMvc.perform(asyncDispatch(start));
        }
        return new ImmediateResultActions(start);
    }

    private JsonNode exportedJson(String... params) throws Exception {
        MvcResult result = export("/posts/export", params).andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode getJson(String path, String... params) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request =
                get(path).header(HttpHeaders.AUTHORIZATION, bearerToken());
        for (int i = 0; i < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        return objectMapper.readTree(
                mockMvc.perform(request)
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString());
    }

    private SearchSeed searchSeed() {
        return jdbcTemplate.queryForObject(
                """
                select p.title, p.status, a.name, t.name, p.published_at
                from posts p
                join authors a on a.id = p.author_id
                join post_tags pt on pt.post_id = p.id
                join tags t on t.id = pt.tag_id
                where p.title is not null and p.published_at is not null
                limit 1
                """,
                (rs, rowNum) ->
                        new SearchSeed(
                                rs.getString(1),
                                titleFragment(rs.getString(1)),
                                rs.getString(2),
                                rs.getString(3),
                                rs.getString(4),
                                rs.getObject(5, OffsetDateTime.class)));
    }

    private String titleFragment(String title) {
        String[] words = title.split("\\s+");
        return words.length == 0 ? title : words[0];
    }

    private String firstWord(String title) {
        return title.split("\\s+")[0];
    }

    private String lastWord(String title) {
        String[] words = title.split("\\s+");
        return words[words.length - 1];
    }

    private String oppositeStatus(String status) {
        return "PUBLISHED".equals(status) ? "DRAFT" : "PUBLISHED";
    }

    private List<String> titles(JsonNode page) {
        List<String> titles = new ArrayList<>();
        page.path("content").forEach(node -> titles.add(node.path("title").asText()));
        return titles;
    }

    private List<String> statuses(JsonNode page) {
        List<String> statuses = new ArrayList<>();
        page.path("content").forEach(node -> statuses.add(node.path("status").asText()));
        return statuses;
    }

    private void assertSorted(List<String> values, boolean ascending) {
        for (int i = 1; i < values.size(); i++) {
            int comparison = values.get(i - 1).compareTo(values.get(i));
            assertTrue(
                    ascending ? comparison <= 0 : comparison >= 0,
                    () -> "Expected sorted titles but got " + values);
        }
    }

    private String bearerToken() throws Exception {
        return bearerToken("admin", "password");
    }

    private String bearerToken(String username, String password) throws Exception {
        String payload =
                objectMapper.writeValueAsString(Map.of("username", username, "password", password));
        MvcResult result =
                mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(payload))
                        .andExpect(status().isOk())
                        .andReturn();
        return "Bearer " + objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
    }

    private record SearchSeed(
            String title,
            String titleFragment,
            String status,
            String authorName,
            String tagName,
            OffsetDateTime publishedAt) {}

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

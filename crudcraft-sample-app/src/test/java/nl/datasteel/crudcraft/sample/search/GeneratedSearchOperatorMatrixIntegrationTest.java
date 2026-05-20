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

package nl.datasteel.crudcraft.sample.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import nl.datasteel.crudcraft.sample.search.repository.OperatorMatrixProbeRepository;
import nl.datasteel.crudcraft.sample.tck.PostgresIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class GeneratedSearchOperatorMatrixIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private OperatorMatrixProbeRepository repository;

    @BeforeEach
    void seedOperatorMatrix() {
        repository.deleteAll();
        repository.saveAll(
                List.of(
                        probe(
                                "Alpha Craft",
                                "10.50",
                                "2026-01-10T10:00:00Z",
                                List.of("java", "crud"),
                                Map.of("tier", "gold", "lang", "java")),
                        probe(
                                "Beta Matrix",
                                "20.00",
                                "2026-02-10T10:00:00Z",
                                List.of("java"),
                                Map.of("tier", "silver", "lang", "kotlin")),
                        probe(
                                "Gamma Empty",
                                "30.00",
                                "2026-03-10T10:00:00Z",
                                List.of(),
                                Map.of())));
    }

    @Test
    @Tag("tck:search.operator.equals")
    @Tag("tck:search.operator.not-equals")
    @Tag("tck:search.operator.contains")
    @Tag("tck:search.operator.starts-with")
    @Tag("tck:search.operator.ends-with")
    @Tag("tck:search.operator.regex")
    @Tag("tck:search.operator.gt")
    @Tag("tck:search.operator.gte")
    @Tag("tck:search.operator.lt")
    @Tag("tck:search.operator.lte")
    @Tag("tck:search.operator.in")
    @Tag("tck:search.operator.not-in")
    @Tag("tck:search.operator.range")
    @Tag("tck:search.operator.before")
    @Tag("tck:search.operator.after")
    @Tag("tck:search.operator.between")
    void generatedSearchEndpointCoversScalarAndTemporalOperators() throws Exception {
        assertContainsTitle(search("title", "Alpha Craft", "titleOp", "EQUALS"), "Alpha Craft");
        assertExcludesTitle(search("title", "Alpha Craft", "titleOp", "NOT_EQUALS"), "Alpha Craft");
        assertContainsTitle(search("title", "Matrix", "titleOp", "CONTAINS"), "Beta Matrix");
        assertContainsTitle(search("title", "Alpha", "titleOp", "STARTS_WITH"), "Alpha Craft");
        assertContainsTitle(search("title", "Craft", "titleOp", "ENDS_WITH"), "Alpha Craft");
        assertContainsTitle(search("title", "Alpha Craft", "titleOp", "REGEX"), "Alpha Craft");
        assertContainsTitle(search("title", "Beta Matrix", "titleOp", "IN"), "Beta Matrix");
        assertExcludesTitle(search("title", "Beta Matrix", "titleOp", "NOT_IN"), "Beta Matrix");

        assertContainsTitle(search("score", "10.50", "scoreOp", "GT"), "Beta Matrix");
        assertContainsTitle(search("score", "20.00", "scoreOp", "GTE"), "Beta Matrix");
        assertContainsTitle(search("score", "20.00", "scoreOp", "LT"), "Alpha Craft");
        assertContainsTitle(search("score", "20.00", "scoreOp", "LTE"), "Beta Matrix");
        assertContainsTitle(
                search("scoreStart", "10.50", "scoreEnd", "20.00", "scoreOp", "RANGE"),
                "Alpha Craft");
        assertContainsTitle(
                search("scoreStart", "10.50", "scoreEnd", "20.00", "scoreOp", "BETWEEN"),
                "Beta Matrix");

        assertContainsTitle(
                search("publishedAt", "2026-02-01T00:00:00Z", "publishedAtOp", "BEFORE"),
                "Alpha Craft");
        assertContainsTitle(
                search("publishedAt", "2026-02-01T00:00:00Z", "publishedAtOp", "AFTER"),
                "Beta Matrix");
    }

    @Test
    @Tag("tck:search.operator.is-empty")
    @Tag("tck:search.operator.size-equals")
    @Tag("tck:search.operator.size-gt")
    @Tag("tck:search.operator.size-lt")
    @Tag("tck:search.operator.not-empty")
    @Tag("tck:search.operator.contains-all")
    @Tag("tck:search.operator.contains-key")
    @Tag("tck:search.operator.contains-value")
    void generatedSearchEndpointCoversCollectionAndMapOperators() throws Exception {
        assertContainsTitle(search("labelsOp", "IS_EMPTY"), "Gamma Empty");
        assertExcludesTitle(search("labelsOp", "NOT_EMPTY"), "Gamma Empty");
        assertContainsTitle(search("labelsSize", "2", "labelsSizeOp", "SIZE_EQUALS"), "Alpha Craft");
        assertContainsTitle(search("labelsSize", "1", "labelsSizeOp", "SIZE_GT"), "Alpha Craft");
        assertContainsTitle(search("labelsSize", "2", "labelsSizeOp", "SIZE_LT"), "Beta Matrix");
        assertContainsTitle(
                search("labels", "java", "labels", "crud", "labelsOp", "CONTAINS_ALL"),
                "Alpha Craft");

        assertContainsTitle(search("attributes[tier]", "ignored", "attributesOp", "CONTAINS_KEY"),
                "Alpha Craft");
        assertContainsTitle(
                search("attributes[probe]", "silver", "attributesOp", "CONTAINS_VALUE"),
                "Beta Matrix");
    }

    private JsonNode search(String... params) throws Exception {
        var request =
                get("/operatormatrixprobes/search")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .param("size", "10");
        requireParamPairs(params);
        for (int i = 0; i + 1 < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        return objectMapper.readTree(
                mockMvc.perform(request)
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString());
    }

    private static void requireParamPairs(String[] params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("params must contain key/value pairs");
        }
    }

    private void assertContainsTitle(JsonNode page, String title) {
        assertTrue(titles(page).contains(title), () -> "Expected " + title + " in " + page);
    }

    private void assertExcludesTitle(JsonNode page, String title) {
        assertFalse(titles(page).contains(title), () -> "Did not expect " + title + " in " + page);
    }

    private List<String> titles(JsonNode page) {
        return page.path("content").findValuesAsText("title");
    }

    private OperatorMatrixProbe probe(
            String title,
            String score,
            String publishedAt,
            List<String> labels,
            Map<String, String> attributes) {
        OperatorMatrixProbe probe = new OperatorMatrixProbe();
        probe.setTitle(title);
        probe.setScore(new BigDecimal(score));
        probe.setPublishedAt(Instant.parse(publishedAt));
        probe.setLabels(new LinkedHashSet<>(labels));
        probe.setAttributes(attributes);
        return probe;
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
        return "Bearer " + objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
    }
}

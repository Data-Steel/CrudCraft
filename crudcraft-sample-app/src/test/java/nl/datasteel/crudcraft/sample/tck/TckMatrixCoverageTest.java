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

package nl.datasteel.crudcraft.sample.tck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import nl.datasteel.crudcraft.runtime.search.SearchLogic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


class TckMatrixCoverageTest {

    private static final Pattern TAG_PATTERN = Pattern.compile("@Tag\\(\"(tck:[^\"]+)\"\\)");
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\bclass\\s+(\\w+)\\b");
    private static final Pattern METHOD_PATTERN =
            Pattern.compile("\\b(?:void|[\\w<>\\[\\], ?]+)\\s+(\\w+)\\s*\\(");

    @Test
    void everyTckMatrixRowHasJUnitTagEvidence() throws IOException {
        Path moduleRoot = moduleRoot();
        Map<String, MatrixRow> matrix =
                matrixRows(moduleRoot.resolve("src/test/resources/tck-matrix.md"));
        Map<String, List<TckEvidence>> evidence = tckTags(moduleRoot.resolve("src/test/java"));
        List<String> missing = matrix.keySet().stream()
                .filter(id -> !evidence.containsKey(id))
                .toList();

        writeCoverageReport(moduleRoot.resolve("target/tck-coverage.md"), matrix, evidence);

        assertTrue(
                missing.isEmpty(),
                () -> "TCK matrix rows without @Tag evidence: " + String.join(", ", missing));
    }

    @Test
    void everyTckTagIsDeclaredInMatrix() throws IOException {
        Path moduleRoot = moduleRoot();
        Map<String, MatrixRow> matrix =
                matrixRows(moduleRoot.resolve("src/test/resources/tck-matrix.md"));
        Map<String, List<TckEvidence>> evidence = tckTags(moduleRoot.resolve("src/test/java"));
        List<String> undocumented = evidence.keySet().stream()
                .filter(id -> !matrix.containsKey(id))
                .toList();

        assertTrue(
                undocumented.isEmpty(),
                () -> "TCK @Tag values missing from matrix: " + String.join(", ", undocumented));
    }

    @Test
    void matrixEvidenceColumnNamesTaggedTestMethods() throws IOException {
        Path moduleRoot = moduleRoot();
        Map<String, MatrixRow> matrix =
                matrixRows(moduleRoot.resolve("src/test/resources/tck-matrix.md"));
        Map<String, List<TckEvidence>> evidence = tckTags(moduleRoot.resolve("src/test/java"));

        List<String> staleEvidence = matrix.values().stream()
                .filter(row -> {
                    Set<String> actualMethods =
                            evidenceNames(evidence.getOrDefault(row.id(), List.of()));
                    return !actualMethods.containsAll(row.evidence());
                })
                .map(row -> row.id() + " -> " + String.join(", ", row.evidence()))
                .toList();

        assertTrue(
                staleEvidence.isEmpty(),
                () -> "TCK matrix evidence column does not match tagged test method: "
                        + String.join("; ", staleEvidence));
    }

    @Test
    void everyTckTagIsBackedByPostgresIntegrationTest() throws IOException {
        Path moduleRoot = moduleRoot();
        Path testSourceRoot = moduleRoot.resolve("src/test/java");
        Map<String, List<TckEvidence>> evidence = tckTags(testSourceRoot);

        List<String> nonPostgresEvidence =
                evidence.entrySet().stream()
                        .flatMap(entry -> entry.getValue().stream()
                                .filter(item -> !isPostgresIntegrationTest(testSourceRoot, item))
                                .map(item -> entry.getKey() + " -> " + item.reportPath()))
                        .toList();

        assertTrue(
                nonPostgresEvidence.isEmpty(),
                () -> "TCK tags must live on PostgresIntegrationTestBase tests: "
                        + String.join("; ", nonPostgresEvidence));
    }

    @Test
    void matrixCoversAllCrudCraftEnumVariants() throws IOException {
        Path moduleRoot = moduleRoot();
        Set<String> ids =
                matrixRows(moduleRoot.resolve("src/test/resources/tck-matrix.md")).keySet();
        List<String> missing = new ArrayList<>();

        Arrays.stream(CrudEndpoint.values())
                .map(endpoint -> "tck:endpoint." + kebab(endpoint.name()))
                .filter(id -> !ids.contains(id))
                .map(id -> "CrudEndpoint " + id)
                .forEach(missing::add);

        Arrays.stream(CrudTemplate.values())
                .map(template -> "tck:template." + kebab(template.name()))
                .filter(prefix -> ids.stream().noneMatch(id -> id.startsWith(prefix)))
                .map(id -> "CrudTemplate " + id)
                .forEach(missing::add);

        Arrays.stream(SearchOperator.values())
                .map(operator -> "tck:search.operator." + kebab(operator.name()))
                .filter(id -> !ids.contains(id))
                .map(id -> "SearchOperator " + id)
                .forEach(missing::add);

        Arrays.stream(SearchLogic.values())
                .map(logic -> "tck:search.logic." + kebab(logic.name()))
                .filter(id -> !ids.contains(id))
                .map(id -> "SearchLogic " + id)
                .forEach(missing::add);

        Arrays.stream(ExportRequest.ExportMode.values())
                .map(mode -> "tck:export.mode." + kebab(mode.name()))
                .filter(id -> !ids.contains(id))
                .map(id -> "ExportMode " + id)
                .forEach(missing::add);

        Arrays.stream(WritePolicy.values())
                .map(policy -> "tck:field-security.write-policy." + kebab(policy.name()))
                .filter(id -> !ids.contains(id))
                .map(id -> "WritePolicy " + id)
                .forEach(missing::add);

        assertTrue(
                missing.isEmpty(),
                () -> "CrudCraft enum variants missing from TCK matrix: "
                        + String.join(", ", missing));
    }

    private static Map<String, MatrixRow> matrixRows(Path matrixPath) throws IOException {
        Map<String, MatrixRow> rows = new LinkedHashMap<>();
        Set<String> duplicates = new LinkedHashSet<>();
        for (String line : Files.readAllLines(matrixPath)) {
            if (!line.startsWith("| tck:")) {
                continue;
            }
            String[] columns = line.split("\\|");
            if (columns.length < 5) {
                continue;
            }
            String id = columns[1].trim();
            MatrixRow previous =
                    rows.put(
                            id,
                            new MatrixRow(
                                    id,
                                    columns[2].trim() + " - " + columns[3].trim(),
                                    evidenceCells(columns[4].trim())));
            if (previous != null) {
                duplicates.add(id);
            }
        }
        assertTrue(
                duplicates.isEmpty(),
                () -> "Duplicate TCK matrix ids are not allowed: "
                        + String.join(", ", duplicates));
        return rows;
    }

    private static Map<String, List<TckEvidence>> tckTags(Path testSourceRoot) throws IOException {
        Map<String, List<TckEvidence>> tags = new LinkedHashMap<>();
        List<String> classLevelTags = new ArrayList<>();
        try (var files = Files.walk(testSourceRoot)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                readMethodLevelTags(testSourceRoot, file, tags, classLevelTags);
            }
        }
        assertTrue(
                classLevelTags.isEmpty(),
                () -> "TCK tags must be attached to concrete @Test methods, not classes: "
                        + String.join("; ", classLevelTags));
        return tags;
    }

    private static void readMethodLevelTags(
            Path testSourceRoot,
            Path file,
            Map<String, List<TckEvidence>> tags,
            List<String> classLevelTags)
            throws IOException {
        String relativePath = testSourceRoot.relativize(file).toString();
        String className = null;
        List<String> pendingTags = new ArrayList<>();
        for (String line : Files.readAllLines(file)) {
            Matcher tag = TAG_PATTERN.matcher(line);
            while (tag.find()) {
                pendingTags.add(tag.group(1));
            }
            Matcher classMatcher = CLASS_PATTERN.matcher(line);
            if (classMatcher.find()) {
                className = classMatcher.group(1);
                if (!pendingTags.isEmpty()) {
                    for (String pendingTag : pendingTags) {
                        classLevelTags.add(pendingTag + " -> " + relativePath);
                    }
                    pendingTags.clear();
                }
            }
            Matcher methodMatcher = METHOD_PATTERN.matcher(line);
            if (className != null && !pendingTags.isEmpty() && methodMatcher.find()) {
                String methodName = methodMatcher.group(1);
                TckEvidence evidence = new TckEvidence(relativePath, className, methodName);
                for (String pendingTag : pendingTags) {
                    tags.computeIfAbsent(pendingTag, ignored -> new ArrayList<>()).add(evidence);
                }
                pendingTags.clear();
            }
            if (!line.isBlank()
                    && !line.stripLeading().startsWith("@")
                    && !line.contains("class ")
                    && !line.contains("(")) {
                pendingTags.clear();
            }
        }
    }

    private static void writeCoverageReport(
            Path reportPath, Map<String, MatrixRow> matrix, Map<String, List<TckEvidence>> evidence)
            throws IOException {
        Files.createDirectories(reportPath.getParent());
        List<String> lines = new ArrayList<>();
        lines.add("# TCK Coverage");
        lines.add("");
        lines.add("| status | id | contract | evidence |");
        lines.add("|---|---|---|---|");
        matrix.forEach(
                (id, row) -> {
                    List<TckEvidence> items = evidence.getOrDefault(id, List.of());
                    String status = items.isEmpty() ? "missing" : "covered";
                    lines.add("| " + status + " | " + id + " | " + row.contract() + " | "
                            + String.join("<br>",
                                    items.stream().map(TckEvidence::reportPath).toList())
                            + " |");
                });
        Files.write(reportPath, lines);
    }

    private static List<String> evidenceCells(String rawEvidence) {
        if (rawEvidence == null || rawEvidence.isBlank()) {
            return List.of();
        }
        return Arrays.stream(rawEvidence.split("<br>|,"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private static Set<String> evidenceNames(List<TckEvidence> evidence) {
        Set<String> names = new LinkedHashSet<>();
        for (TckEvidence item : evidence) {
            names.add(item.target());
        }
        return names;
    }

    private static boolean isPostgresIntegrationTest(Path testSourceRoot, TckEvidence evidence) {
        try {
            return Files.readString(testSourceRoot.resolve(evidence.relativePath()))
                    .contains("extends PostgresIntegrationTestBase");
        } catch (IOException ex) {
            return false;
        }
    }

    private static String kebab(String enumName) {
        return enumName.toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private static Path moduleRoot() {
        Path currentModule = Path.of("src", "test", "resources", "tck-matrix.md");
        if (Files.exists(currentModule)) {
            return Path.of(".");
        }
        return Path.of("crudcraft-sample-app");
    }

    private record MatrixRow(String id, String contract, List<String> evidence) {}

    private record TckEvidence(String relativePath, String className, String methodName) {
        String target() {
            return className + "#" + methodName;
        }

        String reportPath() {
            return relativePath + "#" + methodName;
        }
    }
}

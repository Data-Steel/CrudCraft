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

package nl.datasteel.crudcraft.codegen.golden;

import com.google.testing.compile.Compilation;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.tools.JavaFileObject;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.CrudCraftProcessor;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataProcessor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/** Runs generated-source golden tests for representative CrudCraft input models. */
class GoldenTestRunner {

    private static final String INSOMNIA_OUTPUT_DIR =
            "-Acrudcraft.insomnia.outputDir=";
    private static final String UPDATE_PROPERTY = "crudcraft.golden.update";
    private static final String UPDATE_ENVIRONMENT = "CRUDCRAFT_GOLDEN_UPDATE";
    private static final String GENERATED_SOURCE_PREFIX = "/SOURCE_OUTPUT/";

    @ParameterizedTest(name = "{0}")
    @MethodSource("fixtures")
    void generatedOutputMatchesGolden(GoldenFixture fixture) throws IOException {
        Compilation compilation = compile(fixture);
        if (fixture.expectsFailure()) {
            assertFailure(fixture, compilation);
            return;
        }

        assertSucceededWithoutWarnings(compilation);
        Map<String, String> actual = generatedSources(compilation);
        if (goldenUpdateEnabled()) {
            updateGoldenFiles(fixture, actual);
        }
        assertMatchesGolden(fixture, actual);
        assertInsomniaMatchesGolden(fixture);
        assertStructuralContracts(fixture, actual);
        assertRoundTripCompiles(fixture, compilation);
    }

    static Stream<GoldenFixture> fixtures() {
        return Stream.of(
                GoldenFixture.success("basic", "demo/golden/basic/Book.java"),
                GoldenFixture.success(
                        "readonly_template", "demo/golden/readonly/Lookup.java"),
                GoldenFixture.success(
                        "immutable_write_template", "demo/golden/immutable/Invoice.java"),
                GoldenFixture.success(
                        "patch_only_template", "demo/golden/patchonly/Profile.java"),
                GoldenFixture.success("omit_endpoints", "demo/golden/omit/Article.java"),
                GoldenFixture.success("include_endpoints", "demo/golden/include/AuditLog.java"),
                GoldenFixture.success("secure_field", "demo/golden/securefield/Customer.java"),
                GoldenFixture.success("claim_scoped", "demo/golden/claimscoped/TenantNote.java"),
                GoldenFixture.success("lob_multipart", "demo/golden/lob/Document.java"),
                GoldenFixture.success("lob_collection", "demo/golden/lobcollection/Asset.java"),
                GoldenFixture.success(
                        "projection_field",
                        "demo/golden/projection/Customer.java",
                        "demo/golden/projection/Purchase.java"),
                GoldenFixture.failure(
                        "projection_field_invalid",
                        "@ProjectionField path 'customer..name'",
                        "demo/golden/projectioninvalid/Customer.java",
                        "demo/golden/projectioninvalid/BrokenPurchase.java"),
                GoldenFixture.success(
                        "search_all_operators",
                        "demo/golden/search/OperatorPlayground.java",
                        "demo/golden/search/SearchTag.java"),
                GoldenFixture.success(
                        "manytomany_relationship",
                        "demo/golden/relationship/AccessGroup.java",
                        "demo/golden/relationship/UserAccount.java",
                        "demo/golden/relationship/UserProfile.java"),
                GoldenFixture.success("editable_logic", "demo/golden/editable/Project.java"),
                GoldenFixture.success("dto_variants", "demo/golden/dtovariants/CatalogItem.java"),
                GoldenFixture.successWithOptions(
                        "withers_enabled",
                        List.of("-Acrudcraft.dto.generateWithers=true"),
                        "demo/golden/withers/Setting.java"),
                GoldenFixture.success(
                        "embeddable",
                        "demo/golden/embeddable/Address.java",
                        "demo/golden/embeddable/CustomerRecord.java"),
                GoldenFixture.successWithInsomnia(
                        "endpoint_policy_matrix",
                        "demo/golden/endpointmatrix/CreateOnlyTask.java",
                        "demo/golden/endpointmatrix/CustomPolicyReport.java",
                        "demo/golden/endpointmatrix/LightPublicPage.java",
                        "demo/golden/endpointmatrix/NoBatchTicket.java",
                        "demo/golden/endpointmatrix/NoDeleteRecord.java",
                        "demo/golden/endpointmatrix/SearchOnlyEvent.java",
                        "demo/golden/endpointmatrix/SecureInternalSecret.java",
                        "demo/golden/endpointmatrix/ValidationOnlyDraft.java"),
                GoldenFixture.success(
                        "umbrella_full",
                        "demo/golden/umbrella/Account.java",
                        "demo/golden/umbrella/AccountProfile.java",
                        "demo/golden/umbrella/AccountTag.java",
                        "demo/golden/umbrella/AccountType.java"));
    }

    private static Compilation compile(GoldenFixture fixture) {
        prepareInsomniaOutputDirectory(fixture);
        List<String> options = new ArrayList<>();
        options.add("-proc:only");
        options.add(INSOMNIA_OUTPUT_DIR + insomniaOutputDir(fixture));
        options.addAll(fixture.processorOptions());
        return CompilationTestUtils.javac(options.toArray(String[]::new))
                .withProcessors(new CrudCraftProcessor(), new ProjectionMetadataProcessor())
                .compile(inputSources(fixture));
    }

    private static Iterable<JavaFileObject> inputSources(GoldenFixture fixture) {
        return fixture.inputResources().stream()
                .map(CompilationTestUtils::sourceFromResource)
                .toList();
    }

    private static void assertFailure(GoldenFixture fixture, Compilation compilation) {
        assertEquals(Compilation.Status.FAILURE, compilation.status(), fixture.name());
        assertTrue(
                compilation.errors().stream()
                        .anyMatch(error -> error.getMessage(null).contains(fixture.expectedError())),
                () -> compilation.errors().toString());
    }

    private static void assertSucceededWithoutWarnings(Compilation compilation) {
        assertEquals(
                Compilation.Status.SUCCESS,
                compilation.status(),
                () -> compilation.diagnostics().toString());
        assertTrue(compilation.warnings().isEmpty(), () -> compilation.warnings().toString());
    }

    private static Map<String, String> generatedSources(Compilation compilation) {
        Map<String, String> sources = new LinkedHashMap<>();
        compilation.generatedSourceFiles().stream()
                .sorted(Comparator.comparing(GoldenTestRunner::className))
                .forEach(
                        source -> {
                            try {
                                sources.put(
                                        className(source),
                                        normalize(source.getCharContent(true).toString()));
                            } catch (IOException e) {
                                throw new UncheckedIOException("Failed to read generated source", e);
                            }
                        });
        return sources;
    }

    private static void assertMatchesGolden(GoldenFixture fixture, Map<String, String> actual)
            throws IOException {
        Map<String, String> expected = expectedSources(fixture);
        assertEquals(expected.keySet(), actual.keySet(), () -> changedSourcesMessage(expected, actual));
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            String actualContent = actual.get(entry.getKey());
            assertEquals(
                    entry.getValue(),
                    actualContent,
                    () -> diffMessage(fixture, entry.getKey(), entry.getValue(), actualContent));
        }
    }

    private static void assertInsomniaMatchesGolden(GoldenFixture fixture) throws IOException {
        if (!fixture.verifyInsomnia()) {
            return;
        }
        Path actualPath = actualInsomniaPath(fixture);
        assertTrue(
                Files.isRegularFile(actualPath),
                () -> "Missing generated Insomnia golden output " + actualPath);
        String actual = normalize(Files.readString(actualPath));
        if (goldenUpdateEnabled()) {
            Path expectedPath = expectedInsomniaPath(fixture);
            Files.createDirectories(expectedPath.getParent());
            Files.writeString(expectedPath, actual);
        }
        Path expectedPath = expectedInsomniaPath(fixture);
        assertTrue(
                Files.isRegularFile(expectedPath),
                () ->
                        "Missing Insomnia golden output for "
                                + fixture.name()
                                + ". Run mvn test -pl crudcraft-codegen -Pgolden-update "
                                + "-Dtest=GoldenTestRunner and review the generated diff.");
        assertEquals(
                normalize(Files.readString(expectedPath)),
                actual,
                () -> "Insomnia golden mismatch in " + fixture.name() + " / insomnia.json");
    }

    private static Map<String, String> expectedSources(GoldenFixture fixture) throws IOException {
        Path expectedRoot = expectedRoot(fixture);
        assertTrue(
                Files.isDirectory(expectedRoot),
                () ->
                        "Missing golden output for "
                                + fixture.name()
                                + ". Run mvn test -pl crudcraft-codegen -Pgolden-update "
                                + "-Dtest=GoldenTestRunner and review the generated diff.");
        Map<String, String> sources = new LinkedHashMap<>();
        try (Stream<Path> files = Files.walk(expectedRoot)) {
            files.filter(path -> path.getFileName().toString().endsWith(".java"))
                    .sorted()
                    .forEach(
                            path -> {
                                try {
                                    sources.put(
                                            className(expectedRoot, path),
                                            normalize(Files.readString(path)));
                                } catch (IOException e) {
                                    throw new UncheckedIOException(
                                            "Failed to read " + path, e);
                                }
                            });
        }
        assertFalse(sources.isEmpty(), () -> "No golden Java files under " + expectedRoot);
        return sources;
    }

    private static void assertStructuralContracts(
            GoldenFixture fixture, Map<String, String> actual) {
        switch (fixture.name()) {
            case "basic" -> {
                assertGenerated(actual, "demo.golden.basic.controller.BookController");
                assertContains(
                        actual, "demo.golden.basic.search.BookSearchRequest", "private String title");
                assertContains(
                        actual, "demo.golden.basic.controller.BookController", "@GetMapping");
                assertContains(
                        actual, "demo.golden.basic.meta.BookRelationshipMeta", "fix(Book entity)");
            }
            case "readonly_template" -> {
                String controller = source(actual, "demo.golden.readonly.controller.LookupController");
                assertContains(controller, "@GetMapping");
                assertNotContains(controller, "ResponseEntity<LookupResponseDto> post");
                assertNotContains(controller, "@PatchMapping");
                assertNotContains(controller, "@DeleteMapping");
            }
            case "secure_field" -> {
                assertContains(
                        actual,
                        "demo.golden.securefield.dto.request.CustomerRequestDto",
                        "FAIL_ON_DENIED");
                assertContains(
                        actual,
                        "demo.golden.securefield.dto.response.CustomerResponseDto",
                        "FIELD_SECURITY_METADATA");
            }
            case "claim_scoped" -> {
                assertContains(
                        actual,
                        "demo.golden.claimscoped.service.TenantNoteService",
                        "PrincipalScopeAccessor");
                assertContains(
                        actual,
                        "demo.golden.claimscoped.service.TenantNoteService",
                        "ClaimScopedRowSecurityHandler");
            }
            case "lob_multipart" -> {
                assertContains(
                        actual,
                        "demo.golden.lob.controller.DocumentController",
                        "RequestPart");
                assertContains(
                        actual,
                        "demo.golden.lob.dto.request.DocumentRequestDto",
                        "withContent");
            }
            case "lob_collection" ->
                    assertContains(
                            actual,
                            "demo.golden.lobcollection.dto.request.AssetRequestDto",
                            "Set<byte[]> attachments");
            case "projection_field" ->
                    assertContains(
                            actual,
                            "demo.golden.projection.dto.response.PurchaseResponseDtoProjectionMetadata",
                            "\"customer.name\"");
            case "search_all_operators" -> {
                String search = source(actual, "demo.golden.search.search.OperatorPlaygroundSearchRequest");
                assertContains(search, "private String title");
                assertContains(search, "private BigDecimal score");
                assertContains(search, "private Instant publishedAt");
                assertContains(search, "labels");
                assertContains(search, "private Map<String, String> attributes");
            }
            case "manytomany_relationship" ->
                    assertContains(
                            actual,
                            "demo.golden.relationship.meta.UserAccountRelationshipMeta",
                            "groups");
            case "editable_logic" -> {
                assertGenerated(actual, "demo.golden.editable.controller.ProjectController");
                assertGenerated(actual, "demo.golden.editable.service.ProjectService");
            }
            case "dto_variants" -> {
                assertGenerated(actual, "demo.golden.dtovariants.dto.response.CatalogItemListResponseDto");
                assertGenerated(actual, "demo.golden.dtovariants.dto.response.CatalogItemDetailResponseDto");
            }
            case "withers_enabled" ->
                    assertContains(
                            actual,
                            "demo.golden.withers.dto.response.SettingResponseDto",
                            "withName");
            case "endpoint_policy_matrix" -> {
                String noDelete =
                        source(actual, "demo.golden.endpointmatrix.controller.NoDeleteRecordController");
                assertContains(noDelete, "public ResponseEntity<NoDeleteRecordResponseDto> post");
                assertNotContains(noDelete, "public ResponseEntity<Void> delete(");
                assertNotContains(noDelete, "deleteAllByIds");
                assertContains(noDelete, "public ResponseEntity<StreamingResponseBody> export");
                assertContains(noDelete, "public ResponseEntity<PaginatedResponse<NoDeleteRecordResponseDto>> search");

                String noBatch =
                        source(actual, "demo.golden.endpointmatrix.controller.NoBatchTicketController");
                assertContains(noBatch, "public ResponseEntity<NoBatchTicketResponseDto> post");
                assertNotContains(noBatch, "bulkCreate");
                assertNotContains(noBatch, "bulkUpdate");
                assertNotContains(noBatch, "bulkPatch");
                assertNotContains(noBatch, "bulkUpsert");
                assertNotContains(noBatch, "deleteAllByIds");

                String createOnly =
                        source(actual, "demo.golden.endpointmatrix.controller.CreateOnlyTaskController");
                assertContains(createOnly, "public ResponseEntity<CreateOnlyTaskResponseDto> post");
                assertContains(createOnly, "public ResponseEntity<BulkResult<CreateOnlyTaskResponseDto>> createAll");
                assertContains(createOnly, "public ResponseEntity<BulkResult<CreateOnlyTaskResponseDto>> upsertAll");
                assertNotContains(createOnly, "public ResponseEntity<CreateOnlyTaskResponseDto> getOne");
                assertNotContains(createOnly, "public ResponseEntity<Map<String, Long>> count");

                String searchOnly =
                        source(actual, "demo.golden.endpointmatrix.controller.SearchOnlyEventController");
                assertContains(searchOnly, "public ResponseEntity<PaginatedResponse<SearchOnlyEventResponseDto>> search");
                assertNotContains(searchOnly, "public ResponseEntity<SearchOnlyEventResponseDto> getOne");
                assertNotContains(searchOnly, "public ResponseEntity<Void> validate");

                String lightPublic =
                        source(actual, "demo.golden.endpointmatrix.controller.LightPublicPageController");
                assertContains(lightPublic, "public ResponseEntity<LightPublicPageResponseDto> getOne");
                assertContains(lightPublic, "getAllRef");
                assertNotContains(lightPublic, "public ResponseEntity<PaginatedResponse<LightPublicPageResponseDto>> getAll");
                assertNotContains(lightPublic, "public ResponseEntity<LightPublicPageResponseDto> post");

                String validationOnly =
                        source(actual, "demo.golden.endpointmatrix.controller.ValidationOnlyDraftController");
                assertContains(validationOnly, "public ResponseEntity<Void> validate");
                assertNotContains(validationOnly, "public ResponseEntity<ValidationOnlyDraftResponseDto> post");

                String custom =
                        source(actual, "demo.golden.endpointmatrix.controller.CustomPolicyReportController");
                assertContains(custom, "public ResponseEntity<CustomPolicyReportResponseDto> getOne");
                assertContains(custom, "public ResponseEntity<Map<String, Long>> count");
                assertContains(custom, "public ResponseEntity<Void> exists");
                assertContains(custom, "public ResponseEntity<StreamingResponseBody> export");
                assertNotContains(custom, "public ResponseEntity<CustomPolicyReportResponseDto> post");

                String secure =
                        source(actual, "demo.golden.endpointmatrix.controller.SecureInternalSecretController");
                assertContains(secure, "@PreAuthorize(\"hasAuthority('ENDPOINT_GET_ONE')\")");
                assertContains(secure, "@PreAuthorize(\"hasAuthority('ENDPOINT_VALIDATE')\")");
            }
            default -> {
                assertTrue(actual.keySet().stream().anyMatch(Predicate.not(String::isBlank)));
            }
        }
    }

    private static void assertRoundTripCompiles(
            GoldenFixture fixture, Compilation generatedCompilation) {
        List<JavaFileObject> sources = new ArrayList<>();
        inputSources(fixture).forEach(sources::add);
        sources.addAll(generatedCompilation.generatedSourceFiles());
        Compilation roundTrip =
                CompilationTestUtils.javac("-proc:none", "-Xlint:all").compile(sources);

        assertSucceededWithoutWarnings(roundTrip);
    }

    private static void updateGoldenFiles(GoldenFixture fixture, Map<String, String> actual)
            throws IOException {
        Path expectedRoot = expectedRoot(fixture);
        Path resourcesRoot = resourcesRoot().toRealPath();
        Path absoluteExpectedRoot = expectedRoot.toAbsolutePath().normalize();
        assertTrue(
                absoluteExpectedRoot.startsWith(resourcesRoot),
                () -> "Refusing to update files outside " + resourcesRoot);
        if (Files.exists(expectedRoot)) {
            try (Stream<Path> files = Files.walk(expectedRoot)) {
                files.sorted(Comparator.reverseOrder())
                        .filter(path -> path.getFileName().toString().endsWith(".java"))
                        .forEach(GoldenTestRunner::delete);
            }
        }
        for (Map.Entry<String, String> entry : actual.entrySet()) {
            Path target = expectedRoot.resolve(entry.getKey().replace('.', '/') + ".java");
            Files.createDirectories(target.getParent());
            Files.writeString(target, entry.getValue());
        }
    }

    private static boolean goldenUpdateEnabled() {
        return Boolean.getBoolean(UPDATE_PROPERTY)
                || "true".equalsIgnoreCase(System.getenv(UPDATE_ENVIRONMENT));
    }

    private static Path expectedRoot(GoldenFixture fixture) {
        return resourcesRoot().resolve(fixture.name()).resolve("expected");
    }

    private static Path resourcesRoot() {
        return moduleRoot().resolve("src").resolve("test").resolve("resources").resolve("golden");
    }

    private static Path moduleRoot() {
        Path currentModuleRoot = Path.of(".");
        if (Files.isDirectory(
                currentModuleRoot
                        .resolve("src")
                        .resolve("test")
                        .resolve("resources")
                        .resolve("golden"))) {
            return currentModuleRoot;
        }
        return Path.of("crudcraft-codegen");
    }

    private static Path insomniaOutputDir(GoldenFixture fixture) {
        return moduleRoot().resolve("target").resolve("golden-insomnia").resolve(fixture.name());
    }

    private static Path actualInsomniaPath(GoldenFixture fixture) {
        return insomniaOutputDir(fixture).resolve("insomnia.json");
    }

    private static Path expectedInsomniaPath(GoldenFixture fixture) {
        return expectedRoot(fixture).resolve("insomnia.json");
    }

    private static void prepareInsomniaOutputDirectory(GoldenFixture fixture) {
        Path outputDir = insomniaOutputDir(fixture);
        Path targetRoot = moduleRoot().resolve("target").toAbsolutePath().normalize();
        Path absoluteOutputDir = outputDir.toAbsolutePath().normalize();
        assertTrue(
                absoluteOutputDir.startsWith(targetRoot),
                () -> "Refusing to clean files outside " + targetRoot);
        if (!Files.exists(outputDir)) {
            return;
        }
        try (Stream<Path> files = Files.walk(outputDir)) {
            files.sorted(Comparator.reverseOrder()).forEach(GoldenTestRunner::delete);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to clean " + outputDir, e);
        }
    }

    private static String className(JavaFileObject source) {
        String name = source.getName().replace('\\', '/');
        int generatedRoot = name.indexOf(GENERATED_SOURCE_PREFIX);
        if (generatedRoot >= 0) {
            name = name.substring(generatedRoot + GENERATED_SOURCE_PREFIX.length());
        }
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        if (!name.endsWith(".java")) {
            fail("Generated source is not a Java file: " + source.getName());
        }
        return name.substring(0, name.length() - ".java".length()).replace('/', '.');
    }

    private static String className(Path expectedRoot, Path file) {
        String relative = expectedRoot.relativize(file).toString().replace('\\', '/');
        return relative.substring(0, relative.length() - ".java".length()).replace('/', '.');
    }

    private static String changedSourcesMessage(
            Map<String, String> expected, Map<String, String> actual) {
        Set<String> expectedNames = expected.keySet();
        Set<String> actualNames = actual.keySet();
        return "Generated source set changed. Expected "
                + expectedNames
                + " but got "
                + actualNames
                + ". Run mvn test -pl crudcraft-codegen -Pgolden-update "
                + "-Dtest=GoldenTestRunner and review the expected/ diff.";
    }

    private static String diffMessage(
            GoldenFixture fixture, String className, String expected, String actual) {
        List<String> expectedLines = expected.lines().toList();
        List<String> actualLines = actual.lines().toList();
        int max = Math.max(expectedLines.size(), actualLines.size());
        for (int line = 0; line < max; line++) {
            String expectedLine = line < expectedLines.size() ? expectedLines.get(line) : "<eof>";
            String actualLine = line < actualLines.size() ? actualLines.get(line) : "<eof>";
            if (!expectedLine.equals(actualLine)) {
                return "Golden mismatch in "
                        + fixture.name()
                        + " / "
                        + className
                        + " at line "
                        + (line + 1)
                        + System.lineSeparator()
                        + "expected: "
                        + expectedLine
                        + System.lineSeparator()
                        + "actual:   "
                        + actualLine
                        + System.lineSeparator()
                        + "Run mvn test -pl crudcraft-codegen -Pgolden-update "
                        + "-Dtest=GoldenTestRunner and review the expected/ diff.";
            }
        }
        return "Golden mismatch in " + fixture.name() + " / " + className;
    }

    private static String normalize(String source) {
        return source.replace("\r\n", "\n").stripTrailing() + "\n";
    }

    private static String source(Map<String, String> actual, String className) {
        String source = actual.get(className);
        assertTrue(actual.containsKey(className), () -> "Missing generated source " + className);
        return source;
    }

    private static void assertGenerated(Map<String, String> actual, String className) {
        assertTrue(actual.containsKey(className), () -> "Missing generated source " + className);
    }

    private static void assertContains(
            Map<String, String> actual, String className, String expected) {
        assertContains(source(actual, className), expected);
    }

    private static void assertContains(String source, String expected) {
        assertTrue(source.contains(expected), () -> "Missing fragment: " + expected);
    }

    private static void assertNotContains(String source, String unexpected) {
        assertFalse(source.contains(unexpected), () -> "Unexpected fragment: " + unexpected);
    }

    private static void delete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete " + path, e);
        }
    }
}

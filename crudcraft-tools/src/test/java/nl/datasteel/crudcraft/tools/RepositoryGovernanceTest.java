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

package nl.datasteel.crudcraft.tools;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class RepositoryGovernanceTest {

    private static final String GROUP_ID = "nl.datasteel.crudcraft";
    private static final Path REPOSITORY_ROOT = Path.of("..").toAbsolutePath().normalize();
    private static final Set<String> IGNORED_DEPENDENCY_SCOPES = Set.of("test");
    private static final List<String> SPRING_BOOT_BOM_MANAGED_GROUP_PREFIXES =
            List.of(
                    "org.springframework",
                    "jakarta.",
                    "com.fasterxml.jackson.",
                    "com.h2database",
                    "org.slf4j",
                    "org.junit.");
    private static final Pattern MODULE_NAME =
            Pattern.compile("[a-zA-Z_$][\\w$]*(\\.[a-zA-Z_$][\\w$]*)*");

    @Test
    void rootReactorContainsOnlyCanonical2xModules() throws Exception {
        List<String> modules = childTextElements(readPom(REPOSITORY_ROOT.resolve("pom.xml")), "module");

        assertEquals(
                List.of(
                        "crudcraft-api",
                        "crudcraft-runtime-core",
                        "crudcraft-runtime-search",
                        "crudcraft-runtime-export",
                        "crudcraft-runtime-extensions",
                        "crudcraft-runtime-projection",
                        "crudcraft-runtime-security",
                        "crudcraft-runtime-observability",
                        "crudcraft-codegen",
                        "crudcraft-starter-core",
                        "crudcraft-starter-security",
                        "crudcraft-starter-search",
                        "crudcraft-starter-export",
                        "crudcraft-starter-projection",
                        "crudcraft-starter-extensions",
                        "crudcraft-starter-observability",
                        "crudcraft-starter",
                        "crudcraft-sample-app",
                        "crudcraft-integration-tests",
                        "crudcraft-tools"),
                modules);

        assertFalse(modules.contains("crudcraft-runtime"), "Legacy monolithic runtime is not a 2.x module");
        assertFalse(modules.contains("crudcraft-security"), "Legacy security module is not a 2.x module");
        assertFalse(
                modules.contains("crudcraft-projection"),
                "Legacy projection module is not a 2.x module");
    }

    @Test
    void internalMavenDependencyGraphMatchesArchitecture() throws Exception {
        Map<String, List<String>> actual = new LinkedHashMap<>();
        for (String module : childTextElements(readPom(REPOSITORY_ROOT.resolve("pom.xml")), "module")) {
            Element pom = readPom(REPOSITORY_ROOT.resolve(module).resolve("pom.xml"));
            actual.put(artifactId(pom), productionInternalDependencies(pom));
        }

        assertEquals(expectedProductionGraph(), actual);
    }

    @Test
    void publishedJarsDeclareStableAutomaticModuleNames() throws Exception {
        assertContains("pom.xml", "<Automatic-Module-Name>${crudcraft.automatic.module.name}</Automatic-Module-Name>");
        for (String module : childTextElements(readPom(REPOSITORY_ROOT.resolve("pom.xml")), "module")) {
            Element pom = readPom(REPOSITORY_ROOT.resolve(module).resolve("pom.xml"));
            String moduleName =
                    firstDirectChildText(directChild(pom, "properties"), "crudcraft.automatic.module.name");
            assertFalse(moduleName.isBlank(), module + " must declare crudcraft.automatic.module.name");
            assertTrue(
                    MODULE_NAME.matcher(moduleName).matches(),
                    () -> module + " has invalid Automatic-Module-Name: " + moduleName);
        }
    }

    @Test
    void mainJavaSourceRootsContainOnlyModuleDescriptorOrPackages() throws Exception {
        List<String> offenders = new ArrayList<>();
        for (String module : childTextElements(readPom(REPOSITORY_ROOT.resolve("pom.xml")), "module")) {
            Path mainJava = REPOSITORY_ROOT.resolve(module).resolve("src/main/java");
            if (!Files.isDirectory(mainJava)) {
                continue;
            }
            try (var directChildren = Files.list(mainJava)) {
                directChildren
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".java"))
                        .filter(path -> !"module-info.java".equals(path.getFileName().toString()))
                        .map(REPOSITORY_ROOT::relativize)
                        .map(Path::toString)
                        .forEach(offenders::add);
            }
        }
        assertTrue(
                offenders.isEmpty(),
                () -> "Top-level main Java sources must be module-info.java only: " + offenders);
    }

    @Test
    void codegenModuleDoesNotExportImplementationPackages() throws IOException {
        String moduleInfo =
                Files.readString(
                        REPOSITORY_ROOT.resolve("crudcraft-codegen/src/main/java/module-info.java"));
        List<String> forbiddenExports =
                List.of(
                        "nl.datasteel.crudcraft.codegen.reader",
                        "nl.datasteel.crudcraft.codegen.reader.field",
                        "nl.datasteel.crudcraft.codegen.reader.model",
                        "nl.datasteel.crudcraft.codegen.writer.controller",
                        "nl.datasteel.crudcraft.codegen.writer.controller.endpoints",
                        "nl.datasteel.crudcraft.codegen.writer.controller.method",
                        "nl.datasteel.crudcraft.codegen.writer.relationship",
                        "nl.datasteel.crudcraft.codegen.writer.stubs");
        List<String> exported = new ArrayList<>();
        for (String packageName : forbiddenExports) {
            if (moduleInfo.contains("exports " + packageName + ";")) {
                exported.add(packageName);
            }
        }
        assertTrue(exported.isEmpty(), () -> "Codegen implementation packages exported: " + exported);
    }

    @Test
    void runtimeCoreDoesNotOwnSpringSecurityOrSecurityRuntimeCode() throws IOException {
        List<String> forbiddenFragments =
                List.of("org.springframework.security", "nl.datasteel.crudcraft.runtime.security");
        List<String> offenders =
                javaFiles("crudcraft-runtime-core/src/main/java").stream()
                        .filter(
                                path -> {
                                    try {
                                        String content = Files.readString(path);
                                        return forbiddenFragments.stream().anyMatch(content::contains);
                                    } catch (IOException ex) {
                                        throw new AssertionError("Could not read " + path, ex);
                                    }
                                })
                        .map(REPOSITORY_ROOT::relativize)
                        .map(Path::toString)
                        .toList();

        assertTrue(offenders.isEmpty(), () -> "runtime-core owns security-specific imports: " + offenders);
    }

    @Test
    void generatedGoldenSourcesDoNotImportCodegenInternals() throws IOException {
        List<String> offenders =
                javaFiles("crudcraft-codegen/src/test/resources/golden").stream()
                        .filter(RepositoryGovernanceTest::isGoldenExpectedSource)
                        .filter(
                                path -> {
                                    try {
                                        return Files.readString(path)
                                                .contains("import nl.datasteel.crudcraft.codegen.");
                                    } catch (IOException ex) {
                                        throw new AssertionError("Could not read " + path, ex);
                                    }
                                })
                        .map(REPOSITORY_ROOT::relativize)
                        .map(Path::toString)
                        .toList();

        assertTrue(offenders.isEmpty(), () -> "generated golden sources import codegen internals: " + offenders);
    }

    @Test
    void configurationReferenceMatchesSearchDepthRuntimeContract() throws IOException {
        String properties =
                Files.readString(
                        REPOSITORY_ROOT.resolve(
                                "crudcraft-runtime-search/src/main/java/"
                                        + "nl/datasteel/crudcraft/runtime/search/config/"
                                        + "CrudCraftSearchProperties.java"));
        String configurationReference =
                Files.readString(REPOSITORY_ROOT.resolve("docs/configuration-reference.md"));

        assertTrue(properties.contains("private int depth = 1;"));
        assertTrue(properties.contains("depth < 1"));
        assertTrue(
                configurationReference.contains(
                        "| `crudcraft.search.depth` | integer | `1` |"));
        assertTrue(configurationReference.contains("Must be positive"));
        assertFalse(configurationReference.contains("`0` means only root fields"));
    }

    @Test
    void dependencyManagementCoversVersionlessChildDependencies() throws Exception {
        Element parentPom = readPom(REPOSITORY_ROOT.resolve("pom.xml"));
        Set<String> managedDependencies = managedDependencyKeys(parentPom);
        List<String> unmanaged = new ArrayList<>();
        for (String module : childTextElements(parentPom, "module")) {
            Element pom = readPom(REPOSITORY_ROOT.resolve(module).resolve("pom.xml"));
            for (Element dependency : directChildElements(directChild(pom, "dependencies"), "dependency")) {
                if (firstDirectChildText(dependency, "version").isBlank()) {
                    String key =
                            firstDirectChildText(dependency, "groupId")
                                    + ":"
                                    + firstDirectChildText(dependency, "artifactId");
                    if (!managedDependencies.contains(key) && !isSpringBootBomManaged(key)) {
                        unmanaged.add(module + " -> " + key);
                    }
                }
            }
        }
        assertTrue(unmanaged.isEmpty(), () -> "Unmanaged versionless dependencies: " + unmanaged);
    }

    @Test
    void releaseWorkflowVerifiesEveryPublishedArtifactOnMavenCentral() throws IOException {
        String releaseWorkflow =
                Files.readString(REPOSITORY_ROOT.resolve(".github/workflows/release.yml"));
        assertTrue(
                releaseWorkflow.contains("Verify Maven Central publication"),
                "release workflow must verify Maven Central artifacts");

        assertEquals(
                List.of(
                        "crudcraft-api",
                        "crudcraft-runtime-core",
                        "crudcraft-runtime-search",
                        "crudcraft-runtime-export",
                        "crudcraft-runtime-extensions",
                        "crudcraft-runtime-projection",
                        "crudcraft-runtime-security",
                        "crudcraft-runtime-observability",
                        "crudcraft-codegen",
                        "crudcraft-spring-boot-starter-core",
                        "crudcraft-spring-boot-starter-security",
                        "crudcraft-spring-boot-starter-search",
                        "crudcraft-spring-boot-starter-export",
                        "crudcraft-spring-boot-starter-projection",
                        "crudcraft-spring-boot-starter-extensions",
                        "crudcraft-spring-boot-starter-observability",
                        "crudcraft-spring-boot-starter",
                        "crudcraft-tools"),
                extractReleaseWorkflowArtifacts(releaseWorkflow));
    }

    @Test
    void releaseWorkflowRunsPullRequestProofSurfaceBeforeDeploy() throws IOException {
        String releaseWorkflow =
                Files.readString(REPOSITORY_ROOT.resolve(".github/workflows/release.yml"));
        int verify = releaseWorkflow.indexOf("Release preflight / verify Maven reactor");
        int deploy = releaseWorkflow.indexOf("Build, sign & deploy");

        assertTrue(verify >= 0, "release workflow must run the reactor verify preflight");
        assertTrue(deploy > verify, "release preflight must complete before deployment");
        assertTrue(releaseWorkflow.contains("-Dcrudcraft.tck.postgres.required=true"));
        assertTrue(releaseWorkflow.contains("Release preflight / golden generated-source check"));
        assertTrue(releaseWorkflow.contains("Release preflight / quality reports"));
        assertTrue(releaseWorkflow.contains("Release preflight / OWASP Dependency-Check"));
        assertTrue(releaseWorkflow.contains("Release preflight / Javadoc aggregate gate"));
    }

    @Test
    void exportDocumentationDoesNotOverclaimCsvAndXlsxStreaming() throws IOException {
        String features = Files.readString(REPOSITORY_ROOT.resolve("docs/features.md"));
        String exportGuide =
                Files.readString(REPOSITORY_ROOT.resolve("docs/feature-guides/export/README.md"));
        String exportService =
                Files.readString(
                        REPOSITORY_ROOT.resolve(
                                "crudcraft-runtime-export/src/main/java/"
                                        + "nl/datasteel/crudcraft/runtime/export/service/"
                                        + "ExportService.java"));

        assertFalse(features.contains("Streaming CSV, JSON, and XLSX export"));
        assertTrue(features.contains("bounded CSV/XLSX export"));
        assertTrue(exportGuide.contains("CSV and XLSX are row-streamed when `includeFields`"));
        assertTrue(exportGuide.contains("buffer flattened rows"));
        assertTrue(exportService.contains("CSV and XLSX are row-streamed when"));
        assertTrue(exportService.contains("buffer flattened rows"));
    }

    @Test
    void benchmarksInheritProjectVersionFromParent() throws Exception {
        Element benchmarkPom = readPom(REPOSITORY_ROOT.resolve("benchmarks/pom.xml"));
        Element parent = directChild(benchmarkPom, "parent");

        assertEquals("crudcraft-parent", firstDirectChildText(parent, "artifactId"));
        assertEquals("..", firstDirectChildText(parent, "relativePath"));
        assertTrue(firstDirectChildText(benchmarkPom, "version").isBlank());
    }

    @Test
    void editableSampleControllersDoNotCatchThrowableForOptionalFallbacks() throws IOException {
        List<String> offenders =
                javaFiles("crudcraft-sample-app/src/main/java").stream()
                        .filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                        .filter(
                                path -> {
                                    try {
                                        return Files.readString(path).contains("catch (Throwable");
                                    } catch (IOException ex) {
                                        throw new AssertionError("Could not read " + path, ex);
                                    }
                                })
                        .map(REPOSITORY_ROOT::relativize)
                        .map(Path::toString)
                        .toList();

        assertTrue(offenders.isEmpty(), () -> "Controllers must not catch Throwable: " + offenders);
    }

    @Test
    void releaseTrustAnd2xContractFilesArePresent() throws IOException {
        assertContains("CHANGELOG.md", "## [Unreleased]");
        assertContains("STABILITY.md", "Supported Platform Matrix");
        assertContains("STABILITY.md", "Release Trust");
        assertContains("STABILITY.md", "JPMS compatibility");
        assertContains("docs/architecture/contract-model.md", "Generated Code Contract");
        assertContains("docs/architecture/contract-model.md", "2.0 Planning Items");
        assertContains("docs/architecture/contract-model.md", "reproducible project build timestamp");
        assertContains("docs/architecture/contract-model.md", "jakarta.validation.Valid");
        assertContains("docs/architecture/module-boundaries.md", "Automatic-Module-Name");
        assertContains("docs/architecture/module-boundaries.md", "split packages");
        assertContains("docs/architecture/module-boundaries.md", "module-info.java");
        assertContains("docs/migration-guides/README.md", "Deferred Breaking Changes");
        assertContains(".github/workflows/dependency-scan.yml", "fail-on-vuln: true");
        assertContains(".github/workflows/reproducible-build.yml", "Compare two clean package builds");
        assertContains(".github/workflows/release.yml", "sigstore/cosign-installer");
        assertContains(".github/workflows/release.yml", "cyclonedx");
        assertContains("pom.xml", "cyclonedx-maven-plugin");
        assertContains("pom.xml", "project.build.outputTimestamp");
    }

    private static Map<String, List<String>> expectedProductionGraph() {
        Map<String, List<String>> expected = new LinkedHashMap<>();
        expected.put("crudcraft-api", List.of());
        expected.put("crudcraft-runtime-core", List.of("crudcraft-api"));
        expected.put("crudcraft-runtime-search", List.of("crudcraft-api", "crudcraft-runtime-core"));
        expected.put("crudcraft-runtime-export", List.of("crudcraft-api", "crudcraft-runtime-core"));
        expected.put("crudcraft-runtime-extensions", List.of("crudcraft-runtime-core"));
        expected.put("crudcraft-runtime-projection", List.of("crudcraft-api", "crudcraft-runtime-core"));
        expected.put("crudcraft-runtime-security", List.of("crudcraft-api", "crudcraft-runtime-core"));
        expected.put("crudcraft-runtime-observability", List.of("crudcraft-runtime-core"));
        expected.put("crudcraft-codegen", List.of("crudcraft-api"));
        expected.put("crudcraft-spring-boot-starter-core", List.of("crudcraft-api", "crudcraft-runtime-core"));
        expected.put(
                "crudcraft-spring-boot-starter-security",
                List.of("crudcraft-api", "crudcraft-runtime-security", "crudcraft-runtime-core"));
        expected.put("crudcraft-spring-boot-starter-search", List.of("crudcraft-runtime-search"));
        expected.put("crudcraft-spring-boot-starter-export", List.of("crudcraft-runtime-export"));
        expected.put("crudcraft-spring-boot-starter-projection", List.of("crudcraft-runtime-projection"));
        expected.put("crudcraft-spring-boot-starter-extensions", List.of("crudcraft-runtime-extensions"));
        expected.put(
                "crudcraft-spring-boot-starter-observability",
                List.of("crudcraft-runtime-observability"));
        expected.put(
                "crudcraft-spring-boot-starter",
                List.of(
                        "crudcraft-spring-boot-starter-core",
                        "crudcraft-spring-boot-starter-security",
                        "crudcraft-spring-boot-starter-search",
                        "crudcraft-spring-boot-starter-export",
                        "crudcraft-spring-boot-starter-projection",
                        "crudcraft-spring-boot-starter-extensions",
                        "crudcraft-spring-boot-starter-observability"));
        expected.put(
                "crudcraft-sample-app",
                List.of(
                        "crudcraft-spring-boot-starter",
                        "crudcraft-codegen",
                        "crudcraft-tools"));
        expected.put("crudcraft-integration-tests", List.of("crudcraft-sample-app"));
        expected.put("crudcraft-tools", List.of());
        return expected;
    }

    private static Element readPom(Path path) throws Exception {
        String xml = Files.readString(path);
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setNamespaceAware(false);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml))).getDocumentElement();
    }

    private static String artifactId(Element pom) {
        return firstDirectChildText(pom, "artifactId");
    }

    private static List<String> productionInternalDependencies(Element pom) {
        List<String> dependencies = new ArrayList<>();
        for (Element dependency : directChildElements(directChild(pom, "dependencies"), "dependency")) {
            if (GROUP_ID.equals(firstDirectChildText(dependency, "groupId"))
                    && !IGNORED_DEPENDENCY_SCOPES.contains(firstDirectChildText(dependency, "scope"))) {
                dependencies.add(firstDirectChildText(dependency, "artifactId"));
            }
        }
        return dependencies;
    }

    private static Set<String> managedDependencyKeys(Element parentPom) {
        Element dependencyManagement = directChild(parentPom, "dependencyManagement");
        Element dependencies = directChild(dependencyManagement, "dependencies");
        return directChildElements(dependencies, "dependency").stream()
                .map(
                        dependency ->
                                firstDirectChildText(dependency, "groupId")
                                        + ":"
                                        + firstDirectChildText(dependency, "artifactId"))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static boolean isSpringBootBomManaged(String dependencyKey) {
        return SPRING_BOOT_BOM_MANAGED_GROUP_PREFIXES.stream().anyMatch(dependencyKey::startsWith);
    }

    private static void assertContains(String relativePath, String expectedText) throws IOException {
        String content = Files.readString(REPOSITORY_ROOT.resolve(relativePath));
        assertTrue(
                content.contains(expectedText),
                () -> relativePath + " must contain '" + expectedText + "'");
    }

    private static List<Path> javaFiles(String relativeRoot) throws IOException {
        Path root = REPOSITORY_ROOT.resolve(relativeRoot);
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (var files = Files.walk(root)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .toList();
        }
    }

    private static boolean isGoldenExpectedSource(Path path) {
        return path.toString().replace('\\', '/').contains("/expected/");
    }

    private static List<String> extractReleaseWorkflowArtifacts(String releaseWorkflow) {
        String startToken = "ARTIFACTS=(";
        int start = releaseWorkflow.indexOf(startToken);
        if (start < 0) {
            return List.of();
        }
        int end = releaseWorkflow.indexOf(")", start);
        if (end < 0) {
            return List.of();
        }
        String block = releaseWorkflow.substring(start + startToken.length(), end);
        return Arrays.stream(block.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(line -> line.replace("\"", ""))
                .toList();
    }

    private static List<String> childTextElements(Element parent, String childName) {
        return directChildElements(directChild(parent, childName + "s"), childName).stream()
                .map(Element::getTextContent)
                .map(String::trim)
                .toList();
    }

    private static Element directChild(Element parent, String childName) {
        if (parent == null) {
            return null;
        }
        for (Element child : directChildElements(parent, childName)) {
            return child;
        }
        return null;
    }

    private static List<Element> directChildElements(Element parent, String childName) {
        List<Element> children = new ArrayList<>();
        if (parent == null) {
            return children;
        }
        var nodes = parent.getChildNodes();
        for (int index = 0; index < nodes.getLength(); index++) {
            if (nodes.item(index) instanceof Element element && childName.equals(element.getTagName())) {
                children.add(element);
            }
        }
        return children;
    }

    private static String firstDirectChildText(Element parent, String childName) {
        Element child = directChild(parent, childName);
        return child == null ? "" : child.getTextContent().trim();
    }
}

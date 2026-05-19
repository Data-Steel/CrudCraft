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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/** Tests for {@link GeneratedSpotBugsTool}. */
class GeneratedSpotBugsToolTest {

    private static final String DTO_SOURCE =
            "package nl.datasteel.demo.dto;\n"
                    + "\n"
                    + "import com.fasterxml.jackson.annotation.JsonInclude;\n"
                    + "import java.util.List;\n"
                    + "\n"
                    + "// @CrudCraft:generated\n"
                    + "@JsonInclude(JsonInclude.Include.NON_NULL)\n"
                    + "public class SampleDto {\n"
                    + "    private List<String> values;\n"
                    + "}\n";
    private static final String DTO_SOURCE_NO_IMPORTS =
            "package nl.datasteel.demo.dto;\n"
                    + "\n"
                    + "// @CrudCraft:generated\n"
                    + "public class MinimalDto {\n"
                    + "}\n";
    private static final String RECORD_DTO_SOURCE =
            "package nl.datasteel.demo.dto;\n"
                    + "\n"
                    + "import java.util.List;\n"
                    + "\n"
                    + "// @CrudCraft:generated\n"
                    + "public record SampleDto(List<String> values) {\n"
                    + "}\n";

    @TempDir Path tempDir;

    @Test
    void privateConstructorIsCoveredForUtilityClass() throws Exception {
        Constructor<GeneratedSpotBugsTool> constructor =
                GeneratedSpotBugsTool.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance());
    }

    @Test
    void mainDoesNothingWhenSourceMissing() throws Exception {
        Path missing = tempDir.resolve("missing");

        GeneratedSpotBugsTool.main(new String[] {missing.toString()});
        assertFalse(Files.exists(missing));
    }

    @Test
    void helpTextExplainsGeneratedSourceArgument() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            GeneratedSpotBugsTool.main(new String[] {"-h"});
        } finally {
            System.setOut(originalOut);
        }

        String help = out.toString(StandardCharsets.UTF_8);
        assertTrue(help.contains("Usage: GeneratedSpotBugsTool"));
        assertTrue(help.contains("generatedSourceDir"));
    }

    @Test
    void mainPatchesGeneratedDtoFile() throws Exception {
        Path sourceRoot = tempDir.resolve("generated");
        Path dtoDir = sourceRoot.resolve("nl/datasteel/demo/dto");
        Files.createDirectories(dtoDir);
        Path dtoFile = dtoDir.resolve("SampleDto.java");
        Files.writeString(dtoFile, DTO_SOURCE, StandardCharsets.UTF_8);

        GeneratedSpotBugsTool.main(new String[] {sourceRoot.toString()});

        String patched = Files.readString(dtoFile, StandardCharsets.UTF_8);
        assertTrue(patched.contains("import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;"));
        assertTrue(
                patched.contains(
                        "@SuppressFBWarnings(value = {\"EI_EXPOSE_REP\", \"EI_EXPOSE_REP2\"}"));
        assertTrue(
                patched.indexOf("import java.util.List;")
                        < patched.indexOf(
                                "import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;"));
    }

    @Test
    void mainPatchesGeneratedDtoFileWithoutImports() throws Exception {
        Path sourceRoot = tempDir.resolve("generated");
        Path dtoDir = sourceRoot.resolve("nl/datasteel/demo/dto");
        Files.createDirectories(dtoDir);
        Path dtoFile = dtoDir.resolve("MinimalDto.java");
        Files.writeString(dtoFile, DTO_SOURCE_NO_IMPORTS, StandardCharsets.UTF_8);

        GeneratedSpotBugsTool.main(new String[] {sourceRoot.toString()});

        String patched = Files.readString(dtoFile, StandardCharsets.UTF_8);
        assertTrue(patched.contains("package nl.datasteel.demo.dto;"));
        assertTrue(patched.contains("import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;"));
        assertTrue(patched.contains("@SuppressFBWarnings"));
        assertTrue(patched.contains("public class MinimalDto"));
    }

    @Test
    void mainLeavesGeneratedRecordDtoWithoutSpotBugsImport() throws Exception {
        Path sourceRoot = tempDir.resolve("generated");
        Path dtoDir = sourceRoot.resolve("nl/datasteel/demo/dto");
        Files.createDirectories(dtoDir);
        Path dtoFile = dtoDir.resolve("SampleDto.java");
        Files.writeString(dtoFile, RECORD_DTO_SOURCE, StandardCharsets.UTF_8);

        GeneratedSpotBugsTool.main(new String[] {sourceRoot.toString()});

        String patched = Files.readString(dtoFile, StandardCharsets.UTF_8);
        assertEquals(RECORD_DTO_SOURCE, patched);
        assertFalse(patched.contains("SuppressFBWarnings"));
    }

    @Test
    void mainSkipsNonDtoOrNonGeneratedFiles() throws Exception {
        Path sourceRoot = tempDir.resolve("generated");
        Path dtoDir = sourceRoot.resolve("nl/datasteel/demo/dto");
        Path modelDir = sourceRoot.resolve("nl/datasteel/demo/model");
        Files.createDirectories(dtoDir);
        Files.createDirectories(modelDir);

        Path dtoNoMarker = dtoDir.resolve("NoMarkerDto.java");
        String dtoNoMarkerContent = "public class NoMarkerDto {}\n";
        Files.writeString(dtoNoMarker, dtoNoMarkerContent, StandardCharsets.UTF_8);

        Path modelWithMarker = modelDir.resolve("Model.java");
        String modelWithMarkerContent = "// @CrudCraft:generated\npublic class Model {}\n";
        Files.writeString(modelWithMarker, modelWithMarkerContent, StandardCharsets.UTF_8);
        Path nonJavaFile = sourceRoot.resolve("nl/datasteel/demo/dto/notes.txt");
        String nonJavaContent = "@CrudCraft:generated";
        Files.writeString(nonJavaFile, nonJavaContent, StandardCharsets.UTF_8);

        GeneratedSpotBugsTool.main(new String[] {sourceRoot.toString()});

        assertEquals(dtoNoMarkerContent, Files.readString(dtoNoMarker, StandardCharsets.UTF_8));
        assertEquals(
                modelWithMarkerContent, Files.readString(modelWithMarker, StandardCharsets.UTF_8));
        assertEquals(nonJavaContent, Files.readString(nonJavaFile, StandardCharsets.UTF_8));
    }

    @Test
    void mainUsesDefaultSourceDirectoryWhenNoArgsGiven() throws Exception {
        Path defaultRoot = Path.of("target/generated-sources/annotations");
        Path scopedRoot = defaultRoot.resolve("crudcraft-tools-coverage-test");
        Path dtoFile = scopedRoot.resolve("nl/datasteel/demo/dto/DefaultDto.java");
        Files.createDirectories(dtoFile.getParent());
        Files.writeString(dtoFile, DTO_SOURCE, StandardCharsets.UTF_8);

        try {
            GeneratedSpotBugsTool.main(new String[0]);
            String patched = Files.readString(dtoFile, StandardCharsets.UTF_8);
            assertTrue(patched.contains("@SuppressFBWarnings"));
        } finally {
            deleteRecursively(scopedRoot);
        }
    }

    @Test
    void isGeneratedDtoReturnsTrueOnlyForGeneratedDtoFiles() throws Exception {
        Path sourceRoot = tempDir.resolve("generated");
        Path dtoDir = sourceRoot.resolve("nl/datasteel/demo/dto");
        Files.createDirectories(dtoDir);
        Path dtoFile = dtoDir.resolve("GeneratedDto.java");
        Files.writeString(dtoFile, DTO_SOURCE, StandardCharsets.UTF_8);
        assertTrue((boolean) invokePrivate("isGeneratedDto", dtoFile));

        Path notDto = sourceRoot.resolve("nl/datasteel/demo/model/Model.java");
        Files.createDirectories(notDto.getParent());
        Files.writeString(notDto, DTO_SOURCE, StandardCharsets.UTF_8);
        assertFalse((boolean) invokePrivate("isGeneratedDto", notDto));
    }

    @Test
    void isGeneratedDtoThrowsIllegalStateExceptionWhenReadFails() throws Exception {
        Path sourceRoot = tempDir.resolve("generated");
        Path dtoDir = sourceRoot.resolve("nl/datasteel/demo/dto/BrokenDto.java");
        Files.createDirectories(dtoDir);
        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class, () -> invokePrivate("isGeneratedDto", dtoDir));
        assertTrue(exception.getMessage().contains("Failed to read generated DTO"));
    }

    @Test
    void patchFileLeavesAlreadyPatchedFileUntouched() throws Exception {
        Path file = tempDir.resolve("Sample.java");
        String alreadyPatched =
                "import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;\n"
                        + "@SuppressFBWarnings(\"EI_EXPOSE_REP\")\n"
                        + "public class Sample {}";
        Files.writeString(file, alreadyPatched, StandardCharsets.UTF_8);

        invokePrivate("patchFile", file);

        assertEquals(alreadyPatched, Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void patchFilePatchesGeneratedDto() throws Exception {
        Path file = tempDir.resolve("SampleDto.java");
        Files.writeString(file, DTO_SOURCE, StandardCharsets.UTF_8);
        invokePrivate("patchFile", file);
        assertTrue(Files.readString(file, StandardCharsets.UTF_8).contains("@SuppressFBWarnings"));
    }

    @Test
    void patchFileRemovesStaleImportWhenNoAnnotationTargetExists() throws Exception {
        Path file = tempDir.resolve("SampleDto.java");
        String source =
                RECORD_DTO_SOURCE.replace(
                        "import java.util.List;\n",
                        "import java.util.List;\n"
                                + "import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;\n");
        Files.writeString(file, source, StandardCharsets.UTF_8);

        invokePrivate("patchFile", file);

        String patched = Files.readString(file, StandardCharsets.UTF_8);
        assertFalse(patched.contains("SuppressFBWarnings"));
        assertTrue(patched.contains("public record SampleDto"));
    }

    @Test
    void patchFileThrowsIllegalStateExceptionWhenReadFails() throws Exception {
        Path directory = tempDir.resolve("sample");
        Files.createDirectories(directory);
        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class, () -> invokePrivate("patchFile", directory));
        assertTrue(exception.getMessage().contains("Failed to patch generated DTO"));
    }

    @Test
    void addImportInsertsAfterPackageWhenNoImportExists() throws Exception {
        String source = "package demo;\npublic class Sample {}\n";
        String patched = (String) invokePrivate("addImport", source);
        assertEquals(
                "package demo;\n\n"
                        + "import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;\n"
                        + "public class Sample {}\n",
                patched);
    }

    @Test
    void addImportDoesNotDuplicateExistingImport() throws Exception {
        String source =
                "import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;\n"
                        + "public class Sample {}\n";

        assertEquals(source, invokePrivate("addImport", source));
    }

    @Test
    void addImportAppendsWhenImportHasNoTrailingNewline() throws Exception {
        String source = "package demo;\nimport java.util.List;";
        String patched = (String) invokePrivate("addImport", source);
        assertTrue(
                patched.endsWith("import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;\n"));
    }

    @Test
    void addImportPrependsWhenNoPackageAndNoImportExists() throws Exception {
        String source = "public class Sample {}";
        String patched = (String) invokePrivate("addImport", source);
        assertTrue(
                patched.startsWith(
                        "import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;\n\n"));
        assertTrue(patched.endsWith("public class Sample {}"));
    }

    @Test
    void addImportAppendsWhenPackageHasNoTrailingNewline() throws Exception {
        String source = "package demo";
        String patched = (String) invokePrivate("addImport", source);
        assertEquals(
                "package demo\n\nimport edu.umd.cs.findbugs.annotations.SuppressFBWarnings;\n",
                patched);
    }

    @Test
    void addImportInsertsAfterImportAtStart() throws Exception {
        String source = "import java.util.List;\npublic class Sample {}\n";
        String patched = (String) invokePrivate("addImport", source);
        assertEquals(
                "import java.util.List;\n"
                        + "import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;\n"
                        + "public class Sample {}\n",
                patched);
    }

    @Test
    void addImportInsertsAfterLastImport() throws Exception {
        String source = "package demo;\nimport java.util.List;\npublic class Sample {}\n";
        String patched = (String) invokePrivate("addImport", source);
        assertEquals(
                "package demo;\n"
                        + "import java.util.List;\n"
                        + "import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;\n"
                        + "public class Sample {}\n",
                patched);
    }

    @Test
    void addAnnotationHandlesJsonIncludeAndFallbackMarkers() throws Exception {
        String withJsonInclude = "@JsonInclude(JsonInclude.Include.NON_NULL)\npublic class A {}";
        String patchedWithJsonInclude = (String) invokePrivate("addAnnotation", withJsonInclude);
        assertTrue(patchedWithJsonInclude.contains("@SuppressFBWarnings"));
        assertTrue(
                patchedWithJsonInclude.contains(
                        "@JsonInclude(JsonInclude.Include.NON_NULL)\n@SuppressFBWarnings"));

        String withWindowsLineEndings =
                "@JsonInclude(JsonInclude.Include.NON_NULL)\r\npublic class B {}";
        String patchedWithWindowsLineEndings =
                (String) invokePrivate("addAnnotation", withWindowsLineEndings);
        assertTrue(patchedWithWindowsLineEndings.contains("@SuppressFBWarnings"));

        String withPublicClass = "public class C {}";
        assertTrue(
                ((String) invokePrivate("addAnnotation", withPublicClass))
                        .startsWith("@SuppressFBWarnings"));

        String withAbstractClass = "public abstract class D {}";
        assertTrue(
                ((String) invokePrivate("addAnnotation", withAbstractClass))
                        .startsWith("@SuppressFBWarnings"));

        String withFinalClass = "public final class E {}";
        assertTrue(
                ((String) invokePrivate("addAnnotation", withFinalClass))
                        .startsWith("@SuppressFBWarnings"));

        String withoutClass = "interface F {}";
        assertEquals(withoutClass, invokePrivate("addAnnotation", withoutClass));
    }

    private Object invokePrivate(String methodName, Object argument) throws Exception {
        Class<?> parameterType = argument instanceof String ? String.class : Path.class;
        Method method = GeneratedSpotBugsTool.class.getDeclaredMethod(methodName, parameterType);
        method.setAccessible(true);
        try {
            return method.invoke(null, argument);
        } catch (ReflectiveOperationException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw exception;
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(
                            path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException exception) {
                                    throw new IllegalStateException(
                                            "Failed to delete test path: " + path, exception);
                                }
                            });
        }
    }
}

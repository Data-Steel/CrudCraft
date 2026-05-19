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
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/** Tests for {@link EditableFileTool} covering both happy and error paths. */
class EditableFileToolTest {

    @TempDir Path tempDir;
    private static final List<Path> LEAK_PRONE_SOURCE_FILES =
            List.of(Path.of("src/main/java/Sample.java"), Path.of("src/main/java/SingleArg.java"));

    @BeforeEach
    @AfterEach
    void cleanupPotentialRepoWrites() throws Exception {
        for (Path file : LEAK_PRONE_SOURCE_FILES) {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void privateConstructorIsCoveredForUtilityClass() throws Exception {
        Constructor<EditableFileTool> constructor = EditableFileTool.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance());
    }

    @Test
    void mainDoesNothingWhenSourceMissing() throws Exception {
        Path source = tempDir.resolve("missing");
        Path dest = tempDir.resolve("dest");
        Files.createDirectories(dest);

        EditableFileTool.main(new String[] {source.toString(), dest.toString()});

        assertTrue(Files.list(dest).findAny().isEmpty());
    }

    @Test
    void helpTextExplainsArgumentsAndProperties() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            EditableFileTool.main(new String[] {"--help"});
        } finally {
            System.setOut(originalOut);
        }

        String help = out.toString(StandardCharsets.UTF_8);
        assertTrue(help.contains("Usage: EditableFileTool"));
        assertTrue(help.contains("crudcraft.generatedDir"));
        assertTrue(help.contains("crudcraft.sourceDir"));
    }

    @Test
    void copiesAndDeletesEditableFiles() throws Exception {
        Path sourceRoot = tempDir.resolve("gen");
        Path destRoot = tempDir.resolve("src");
        Files.createDirectories(sourceRoot);
        Files.createDirectories(destRoot);

        Path sourceFile = sourceRoot.resolve("Sample.java");
        Files.writeString(
                sourceFile,
                "// @CrudCraft:editable\npackage t;\nclass Sample {}",
                StandardCharsets.UTF_8);

        EditableFileTool.main(new String[] {sourceRoot.toString(), destRoot.toString()});

        Path destFile = destRoot.resolve("Sample.java");
        assertTrue(Files.exists(destFile));
        assertFalse(Files.exists(sourceFile));
        assertEquals("// @CrudCraft:editable\npackage t;\nclass Sample {}", Files.readString(destFile));
    }

    @Test
    void skipsExistingEditableFiles() throws Exception {
        Path sourceRoot = tempDir.resolve("gen");
        Path destRoot = tempDir.resolve("src");
        Files.createDirectories(sourceRoot);
        Files.createDirectories(destRoot);

        Path sourceFile = sourceRoot.resolve("Sample.java");
        Files.writeString(
                sourceFile,
                "// @CrudCraft:editable\npackage t;\nclass Sample {}",
                StandardCharsets.UTF_8);

        Path destFile = destRoot.resolve("Sample.java");
        Files.writeString(destFile, "existing", StandardCharsets.UTF_8);

        EditableFileTool.main(new String[] {sourceRoot.toString(), destRoot.toString()});

        assertTrue(Files.exists(destFile));
        assertEquals("existing", Files.readString(destFile));
        assertFalse(Files.exists(sourceFile));
    }

    @Test
    void nonEditableFilesIgnored() throws Exception {
        Path sourceRoot = tempDir.resolve("gen");
        Path destRoot = tempDir.resolve("src");
        Files.createDirectories(sourceRoot);
        Files.createDirectories(destRoot);

        Path sourceFile = sourceRoot.resolve("Sample.java");
        Files.writeString(sourceFile, "class Sample {}", StandardCharsets.UTF_8);

        EditableFileTool.main(new String[] {sourceRoot.toString(), destRoot.toString()});

        assertTrue(Files.exists(sourceFile));
        assertFalse(Files.exists(destRoot.resolve("Sample.java")));
    }

    @Test
    void throwsUncheckedIOExceptionWhenCopyFails() throws Exception {
        Path sourceRoot = tempDir.resolve("gen");
        Files.createDirectories(sourceRoot);

        Path sourceFile = sourceRoot.resolve("Sample.java");
        Files.writeString(
                sourceFile,
                "// @CrudCraft:editable\npackage t;\nclass Sample {}",
                StandardCharsets.UTF_8);

        // Make the destination "root" a FILE, not a directory → directory creation fails
        Path destRoot = tempDir.resolve("src");
        Files.writeString(destRoot, "not a directory", StandardCharsets.UTF_8);
        assertTrue(Files.isRegularFile(destRoot));

        assertThrows(
                UncheckedIOException.class,
                () ->
                        EditableFileTool.main(
                                new String[] {sourceRoot.toString(), destRoot.toString()}));
    }

    private boolean invokeIsEditable(Path file) throws Exception {
        Method m = EditableFileTool.class.getDeclaredMethod("isEditableCrudCraftFile", Path.class);
        m.setAccessible(true);
        return (boolean) m.invoke(null, file);
    }

    @Test
    void isEditableCrudCraftFileDetectsMarker() throws Exception {
        Path file = tempDir.resolve("Editable.java");
        Files.writeString(file, "// @CrudCraft:editable\npackage t;", StandardCharsets.UTF_8);
        assertTrue(invokeIsEditable(file));
    }

    @Test
    void isEditableCrudCraftFileReturnsFalseWithoutMarker() throws Exception {
        Path file = tempDir.resolve("NotEditable.java");
        Files.writeString(file, "class X {}", StandardCharsets.UTF_8);
        assertFalse(invokeIsEditable(file));
    }

    @Test
    void isEditableCrudCraftFileReturnsFalseWithoutPackageDeclaration() throws Exception {
        Path file = tempDir.resolve("NoPackage.java");
        Files.writeString(file, "// @CrudCraft:editable\nclass X {}", StandardCharsets.UTF_8);
        assertFalse(invokeIsEditable(file));
    }

    @Test
    void isEditableCrudCraftFileReturnsFalseOnIOException() throws Exception {
        Path dir = tempDir.resolve("someDir");
        Files.createDirectories(dir);
        assertFalse(invokeIsEditable(dir));
    }

    @Test
    void isEditableCrudCraftFileReturnsFalseForNullPath() throws Exception {
        assertFalse(invokeIsEditable(null));
    }

    @Test
    void isEditableCrudCraftFileReturnsFalseOnMalformedUtf8() throws Exception {
        Path file = tempDir.resolve("BrokenEncoding.java");
        Files.write(file, new byte[] {(byte) 0xC3, (byte) 0x28});
        assertFalse(invokeIsEditable(file));
    }

    @Test
    void usesSystemPropertiesWhenArgsMissing() throws Exception {
        Path sourceRoot = tempDir.resolve("gen");
        Path destRoot = tempDir.resolve("src");
        Files.createDirectories(sourceRoot);
        Files.writeString(
                sourceRoot.resolve("Sample.java"),
                "// @CrudCraft:editable\npackage t;\nclass Sample {}",
                StandardCharsets.UTF_8);
        System.setProperty("crudcraft.generatedDir", sourceRoot.toString());
        System.setProperty("crudcraft.sourceDir", destRoot.toString());
        try {
            EditableFileTool.main(new String[0]);
        } finally {
            System.clearProperty("crudcraft.generatedDir");
            System.clearProperty("crudcraft.sourceDir");
        }
        assertTrue(Files.exists(destRoot.resolve("Sample.java")));
        assertFalse(Files.exists(sourceRoot.resolve("Sample.java")));
    }

    @Test
    void createsMissingDestinationDirectories() throws Exception {
        Path sourceRoot = tempDir.resolve("gen");
        Path destRoot = tempDir.resolve("src");
        Path sourceFile = sourceRoot.resolve("a/b/Sample.java");
        Files.createDirectories(sourceFile.getParent());
        Files.writeString(
                sourceFile,
                "// @CrudCraft:editable\npackage t;\nclass Sample {}",
                StandardCharsets.UTF_8);
        EditableFileTool.main(new String[] {sourceRoot.toString(), destRoot.toString()});
        assertTrue(Files.exists(destRoot.resolve("a/b/Sample.java")));
    }

    @Test
    void ignoresNonJavaFiles() throws Exception {
        Path sourceRoot = tempDir.resolve("gen");
        Path destRoot = tempDir.resolve("src");
        Files.createDirectories(sourceRoot);
        Files.writeString(
                sourceRoot.resolve("README.md"), "// @CrudCraft:editable", StandardCharsets.UTF_8);
        EditableFileTool.main(new String[] {sourceRoot.toString(), destRoot.toString()});
        assertTrue(Files.exists(sourceRoot.resolve("README.md")));
        assertFalse(Files.exists(destRoot.resolve("README.md")));
    }

    @Test
    void usesDefaultDestinationWhenOnlySourceArgProvided() throws Exception {
        Path sourceRoot = tempDir.resolve("gen");
        Path sourceFile = sourceRoot.resolve("SingleArg.java");
        Path destRoot = tempDir.resolve("src-main");
        Files.createDirectories(sourceFile.getParent());
        Files.writeString(
                sourceFile,
                "// @CrudCraft:editable\npackage t;\nclass SingleArg {}",
                StandardCharsets.UTF_8);
        System.setProperty("crudcraft.sourceDir", destRoot.toString());
        try {
            EditableFileTool.main(new String[] {sourceRoot.toString()});
        } finally {
            System.clearProperty("crudcraft.sourceDir");
        }
        assertTrue(Files.exists(destRoot.resolve("SingleArg.java")));
        assertFalse(Files.exists(sourceFile));
    }
}

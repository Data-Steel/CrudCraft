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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class GeneratedSpotBugsToolIntegrationTest {

    private static final String GENERATED_DTO =
            "package demo.dto;\n"
                    + "\n"
                    + "import com.fasterxml.jackson.annotation.JsonInclude;\n"
                    + "import java.util.List;\n"
                    + "\n"
                    + "// @CrudCraft:generated\n"
                    + "@JsonInclude(JsonInclude.Include.NON_NULL)\n"
                    + "public class DemoDto {\n"
                    + "    private List<String> values;\n"
                    + "}\n";

    @TempDir Path tempDir;

    @Test
    void patchesGeneratedDtoTreeFromCliArgument() throws Exception {
        Path sourceRoot = tempDir.resolve("generated-sources");
        Path dtoFile = sourceRoot.resolve("demo/dto/DemoDto.java");
        Files.createDirectories(dtoFile.getParent());
        Files.writeString(dtoFile, GENERATED_DTO, StandardCharsets.UTF_8);

        GeneratedSpotBugsTool.main(new String[] {sourceRoot.toString()});

        String patched = Files.readString(dtoFile, StandardCharsets.UTF_8);
        assertTrue(patched.contains("import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;"));
        assertTrue(patched.contains("@SuppressFBWarnings"));
        assertTrue(patched.contains("Generated DTO with mutable reference fields"));
    }

    @Test
    void leavesMissingSourceRootUntouched() throws Exception {
        Path missingRoot = tempDir.resolve("does-not-exist");

        GeneratedSpotBugsTool.main(new String[] {missingRoot.toString()});

        assertFalse(Files.exists(missingRoot));
    }

    @Test
    void skipsAlreadyPatchedDtoOnRepeatedRun() throws Exception {
        Path sourceRoot = tempDir.resolve("generated-sources");
        Path dtoFile = sourceRoot.resolve("demo/dto/DemoDto.java");
        Files.createDirectories(dtoFile.getParent());
        Files.writeString(dtoFile, GENERATED_DTO, StandardCharsets.UTF_8);

        GeneratedSpotBugsTool.main(new String[] {sourceRoot.toString()});
        String once = Files.readString(dtoFile, StandardCharsets.UTF_8);
        GeneratedSpotBugsTool.main(new String[] {sourceRoot.toString()});

        assertEquals(once, Files.readString(dtoFile, StandardCharsets.UTF_8));
    }
}

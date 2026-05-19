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

package nl.datasteel.crudcraft.codegen.architecture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


class CodegenBoundaryTest {

    @Test
    void codegenDoesNotDirectlyImportRuntimeModules() throws IOException {
        Path sourceRoot = Path.of("src/main/java");
        List<Path> offenders;
        try (var files = Files.walk(sourceRoot)) {
            offenders =
                    files.filter(path -> path.toString().endsWith(".java"))
                            .filter(this::containsRuntimeImport)
                            .toList();
        }

        assertTrue(
                offenders.isEmpty(),
                () -> "Codegen must not directly import runtime modules: " + offenders);
    }

    private boolean containsRuntimeImport(Path path) {
        try {
            return Files.readString(path).contains("import nl.datasteel.crudcraft.runtime.");
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read " + path, ex);
        }
    }
}

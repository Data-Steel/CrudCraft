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

package nl.datasteel.crudcraft.runtime.architecture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


class CoreBoundaryTest {

    @Test
    void coreDoesNotImportOptionalRuntimeCapabilities() throws IOException {
        Path sourceRoot = Path.of("src/main/java");
        List<Path> offenders;
        try (var files = Files.walk(sourceRoot)) {
            offenders =
                    files.filter(path -> path.toString().endsWith(".java"))
                            .filter(this::importsOptionalCapability)
                            .toList();
        }
        assertTrue(
                offenders.isEmpty(),
                () -> "runtime-core must not directly import optional capabilities: " + offenders);
    }

    @Test
    void corePomDoesNotDependOnOptionalSearchModule() throws IOException {
        String pom = Files.readString(Path.of("pom.xml"));
        assertTrue(
                !pom.contains("<artifactId>crudcraft-runtime-search</artifactId>"),
                "runtime-core must not depend on crudcraft-runtime-search");
    }

    private boolean importsOptionalCapability(Path source) {
        try {
            String code = Files.readString(source);
            return code.contains("import nl.datasteel.crudcraft.runtime.security.")
                    || code.contains("import nl.datasteel.crudcraft.runtime.projection.")
                    || code.contains("import nl.datasteel.crudcraft.runtime.search.")
                    || code.contains("import nl.datasteel.crudcraft.runtime.export.")
                    || code.contains("import nl.datasteel.crudcraft.runtime.export.util.ExportUtil");
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read " + source, ex);
        }
    }
}

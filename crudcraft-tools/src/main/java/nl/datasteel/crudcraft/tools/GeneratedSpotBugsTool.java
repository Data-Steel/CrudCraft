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
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Adds SpotBugs suppressions to generated DTOs so generated sources do not fail verification. */
public final class GeneratedSpotBugsTool {

    private static final Logger log = LoggerFactory.getLogger(GeneratedSpotBugsTool.class);

    private static final String IMPORT_LINE =
            "import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;\n";
    private static final String PACKAGE_PREFIX = "package ";
    private static final String JSON_INCLUDE = "@JsonInclude(JsonInclude.Include.NON_NULL)";
    private static final String ANNOTATION_LINE =
            "@SuppressFBWarnings(value = {\"EI_EXPOSE_REP\", \"EI_EXPOSE_REP2\"}, "
                    + "justification = \"Generated DTO with mutable reference fields\")\n";
    private static final String HELP =
            """
            Usage: GeneratedSpotBugsTool [generatedSourceDir]

            Adds SpotBugs suppressions to generated DTO classes under the generated source tree.
            Use this after annotation processing and before static analysis when generated DTOs
            intentionally expose mutable request/response collection properties.

            Default generatedSourceDir: target/generated-sources/annotations
            """;

    private GeneratedSpotBugsTool() {}

    /**
     * Patches generated DTO classes with SpotBugs suppression annotations.
     *
     * @param args optional source directory; defaults to {@code
     *     target/generated-sources/annotations}
     * @throws IOException when walking the source tree fails
     */
    public static void main(String[] args) throws IOException {
        if (isHelp(args)) {
            System.out.print(HELP);
            return;
        }
        Path sourceRoot =
                args.length > 0
                        ? Paths.get(args[0])
                        : Paths.get("target/generated-sources/annotations");

        if (!Files.exists(sourceRoot)) {
            log.info("No generated annotation sources found. Skipping SpotBugs patch.");
            return;
        }

        log.info(
                "[CrudCraft] Patching generated DTOs for SpotBugs in: {}",
                sourceRoot.toAbsolutePath());

        Files.walkFileTree(
                sourceRoot,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (file.toString().endsWith(".java") && isGeneratedDto(file)) {
                            patchFile(file);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    private static boolean isGeneratedDto(Path file) {
        String normalized = file.toString().replace('\\', '/');
        if (!normalized.contains("/dto/")) {
            return false;
        }
        try {
            return Files.readString(file).contains("@CrudCraft:generated");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read generated DTO: " + file, e);
        }
    }

    private static void patchFile(Path file) {
        try {
            String source = Files.readString(file, StandardCharsets.UTF_8);
            if (source.contains("@SuppressFBWarnings(")) {
                return;
            }

            String annotated = addAnnotation(source);
            if (annotated.equals(source)) {
                String cleaned = removeImport(source);
                if (!cleaned.equals(source)) {
                    Files.writeString(file, cleaned, StandardCharsets.UTF_8);
                    log.info(
                            "[CrudCraft] Removed stale SpotBugs import from generated DTO: {}",
                            file);
                }
                return;
            }

            String patched = addImport(annotated);
            Files.writeString(file, patched, StandardCharsets.UTF_8);
            log.info("[CrudCraft] Patched generated DTO: {}", file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to patch generated DTO: " + file, e);
        }
    }

    private static String addImport(String source) {
        if (source.contains(IMPORT_LINE)) {
            return source;
        }

        int lastImport = source.lastIndexOf("import ");
        if (lastImport != -1) {
            int lineEnd = source.indexOf('\n', lastImport);
            if (lineEnd == -1) {
                return source + "\n" + IMPORT_LINE;
            }
            return source.substring(0, lineEnd + 1) + IMPORT_LINE + source.substring(lineEnd + 1);
        }

        int packageIndex = source.indexOf(PACKAGE_PREFIX);
        if (packageIndex != -1) {
            int lineEnd = source.indexOf('\n', packageIndex);
            if (lineEnd == -1) {
                return source + "\n\n" + IMPORT_LINE;
            }
            return source.substring(0, lineEnd + 1)
                    + "\n"
                    + IMPORT_LINE
                    + source.substring(lineEnd + 1);
        }
        return IMPORT_LINE + "\n" + source;
    }

    private static String removeImport(String source) {
        return source.replace(IMPORT_LINE, "");
    }

    private static String addAnnotation(String source) {
        String[] markers = {
            "@JsonInclude(JsonInclude.Include.NON_NULL)\npublic class ",
            "@JsonInclude(JsonInclude.Include.NON_NULL)\r\npublic class "
        };
        for (String marker : markers) {
            int index = source.indexOf(marker);
            if (index != -1) {
                String lineSeparator = marker.contains("\r\n") ? "\r\n" : "\n";
                return source.substring(0, index)
                        + JSON_INCLUDE
                        + lineSeparator
                        + ANNOTATION_LINE
                        + source.substring(index + marker.length() - "public class ".length());
            }
        }

        for (String marker :
                new String[] {"public class ", "public abstract class ", "public final class "}) {
            int index = source.indexOf(marker);
            if (index != -1) {
                return source.substring(0, index) + ANNOTATION_LINE + source.substring(index);
            }
        }
        return source;
    }

    private static boolean isHelp(String[] args) {
        return args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]));
    }
}

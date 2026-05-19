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
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility that post-processes generated sources to deal with editable stubs.
 *
 * <p>It copies files marked with the "@CrudCraft:editable" tag from the annotation processor's
 * output directory into the main source tree if they do not yet exist there. Regardless of whether
 * they are copied, the generated versions are removed so subsequent compilations do not pick them
 * up again.
 *
 * <p>The source and destination directories can be overridden either via command line arguments (
 * <code>sourceDir destDir</code>) or the system properties <code>crudcraft.generatedDir</code> and
 * <code>crudcraft.sourceDir</code>.
 */
public class EditableFileTool {

    private static final Logger log = LoggerFactory.getLogger(EditableFileTool.class);
    private static final Pattern PACKAGE_DECLARATION =
            Pattern.compile("(?m)^\\s*package\\s+[A-Za-z0-9_.]+\\s*;");
    private static final String HELP =
            """
            Usage: EditableFileTool [generatedSourceDir] [destinationSourceDir]

            Copies generated Java files marked with @CrudCraft:editable into the source tree
            when no destination file exists, then removes the generated copy so javac does not
            compile duplicate editable stubs.

            Defaults:
              generatedSourceDir    target/generated-sources/annotations
              destinationSourceDir  src/main/java

            System properties:
              crudcraft.generatedDir  overrides generatedSourceDir
              crudcraft.sourceDir     overrides destinationSourceDir
            """;

    /** Prevents instantiation. */
    private EditableFileTool() {}

    /**
     * Copies editable generated files into the main source tree.
     *
     * @param args optional source and destination directories
     * @throws IOException if walking the generated source tree fails
     */
    public static void main(String[] args) throws IOException {
        if (isHelp(args)) {
            System.out.print(HELP);
            return;
        }
        Path sourceRoot =
                args.length > 0
                        ? Paths.get(args[0])
                        : Paths.get(
                                System.getProperty(
                                        "crudcraft.generatedDir",
                                        "target/generated-sources/annotations"));
        Path destinationRoot =
                args.length > 1
                        ? Paths.get(args[1])
                        : Paths.get(System.getProperty("crudcraft.sourceDir", "src/main/java"));

        if (!Files.exists(sourceRoot)) {
            log.info("No generated annotation sources found. Skipping editable file copy.");
            return;
        }

        log.info("[CrudCraft] Scanning for editable files in: {}", sourceRoot.toAbsolutePath());

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                    .filter(EditableFileTool::isEditableCrudCraftFile)
                    .forEach(
                            source -> {
                                Path relative = sourceRoot.relativize(source);
                                Path destination = destinationRoot.resolve(relative);

                                try {
                                    if (!Files.exists(destination)) {
                                        Files.createDirectories(destination.getParent());
                                        Files.copy(source, destination);
                                        Files.delete(source);
                                        log.info(
                                                "[CrudCraft] Copied editable file: {}",
                                                destination);
                                    } else {
                                        Files.deleteIfExists(source);
                                        log.info(
                                                "[CrudCraft] Skipped (already exists): {}",
                                                destination);
                                    }
                                } catch (IOException e) {
                                    log.error(
                                            "[CrudCraft] Failed to copy file: {} -> {}",
                                            source,
                                            destination,
                                            e);
                                    throw new UncheckedIOException(e);
                                }
                            });
        }
    }

    /**
     * Returns true if the path is a regular file that contains the CrudCraft editable marker.
     * Robust against directories or unreadable files on different platforms.
     */
    private static boolean isEditableCrudCraftFile(Path filePath) {
        // Short-circuit: only regular files can be editable sources
        if (filePath == null || !Files.isRegularFile(filePath)) {
            return false;
        }
        try {
            String source = Files.readString(filePath, StandardCharsets.UTF_8);
            return source.contains("@CrudCraft:editable")
                    && PACKAGE_DECLARATION.matcher(source).find();
        } catch (UncheckedIOException | IOException e) {
            log.warn("[CrudCraft] Failed to read file: {}", filePath, e);
            return false;
        }
    }

    private static boolean isHelp(String[] args) {
        return args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]));
    }
}

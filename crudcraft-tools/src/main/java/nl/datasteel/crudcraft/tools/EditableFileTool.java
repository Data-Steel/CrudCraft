/*
 * Copyright (c) 2025 CrudCraft contributors
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Utility that post-processes generated sources to deal with editable stubs.
 * <p>
 * It copies files marked with the "@CrudCraft:editable" tag from the annotation
 * processor's output directory into the main source tree if they do not yet
 * exist there. Regardless of whether they are copied, the generated versions are
 * removed so subsequent compilations do not pick them up again.
 * <p>
 * The source and destination directories can be overridden either via command
 * line arguments (<code>sourceDir destDir</code>) or the system properties
 * <code>crudcraft.generatedDir</code> and <code>crudcraft.sourceDir</code>.
 */
public class EditableFileTool {

    /**
     * The main method that executes the file copying process.
     * It scans the generated sources directory for editable files,
     * copies them to the main source directory, and deletes the original files.
     *
     * @param args command line arguments (not used)
     * @throws IOException if an I/O error occurs during file operations
     */
    public static void main(String[] args) throws IOException {
        Path sourceRoot = args.length > 0
                ? Paths.get(args[0])
                : Paths.get(System.getProperty("crudcraft.generatedDir",
                        "target/generated-sources/annotations"));
        Path destinationRoot = args.length > 1
                ? Paths.get(args[1])
                : Paths.get(System.getProperty("crudcraft.sourceDir",
                        "src/main/java"));

        if (!Files.exists(sourceRoot)) {
            System.out.println("No generated annotation sources found. "
                    + "Skipping editable file copy.");
            return;
        }

        System.out.println("[CrudCraft] Scanning for editable files in: "
                + sourceRoot.toAbsolutePath());

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(EditableFileTool::isEditableCrudCraftFile)
                    .forEach(source -> {
                        Path relative = sourceRoot.relativize(source);
                        Path destination = destinationRoot.resolve(relative);

                        try {
                            if (!Files.exists(destination)) {
                                Files.createDirectories(destination.getParent());
                                Files.copy(source, destination);
                                Files.delete(source);
                                System.out.println("[CrudCraft] Copied editable file: "
                                        + destination);
                            } else {
                                Files.deleteIfExists(source);
                                System.out.println("[CrudCraft] Skipped (already exists): "
                                        + destination);
                            }
                        } catch (IOException e) {
                            System.err.println("[CrudCraft] Failed to copy file: " + source
                                    + " → " + destination + ": " + e.getMessage());
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }

    /**
     * Returns true if the file contains the CrudCraft editable marker.
     */
    private static boolean isEditableCrudCraftFile(Path filePath) {
        try (Stream<String> lines = Files.lines(filePath)) {
            return lines.anyMatch(line -> line.contains("@CrudCraft:editable"));
        } catch (IOException e) {
            System.err.println("[CrudCraft] Failed to read file: " + filePath
                    + ": " + e.getMessage());
            return false;
        }
    }
}

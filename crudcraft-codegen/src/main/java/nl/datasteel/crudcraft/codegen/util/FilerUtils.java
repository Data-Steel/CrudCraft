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
package nl.datasteel.crudcraft.codegen.util;

import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.io.Writer;
import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Utilities for dealing with {@link Filer} instances.
 */
public final class FilerUtils {

    /**
     * Private constructor to prevent instantiation.
     * This class provides static utility methods only.
     */
    private FilerUtils() {
    }

    /**
     * Writes the given {@link JavaFile} using the supplied {@link Filer}.
     * Any IO error will be reported through the provided {@link Messager}.
     *
     * @param javaFile the file to write
     * @param filer the filer to use
     * @param messager optional messager for error reporting
     */
    public static void writeJavaFile(JavaFile javaFile, Filer filer, Messager messager) {
        try {
            JavaFileObject file = filer.createSourceFile(
                    javaFile.packageName + "." + javaFile.typeSpec.name);
            try (Writer writer = file.openWriter()) {
                writer.write(javaFile.toString());
            }
        } catch (FilerException e) {
            // The file already exists, so skip it.
            if (messager != null) {
                messager.printMessage(Diagnostic.Kind.NOTE,
                        "Skipping generation of existing type "
                                + javaFile.packageName + "." + javaFile.typeSpec.name);
            }
        } catch (IOException e) {
            if (messager != null) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Failed to write " + javaFile.packageName + "."
                                + javaFile.typeSpec.name + ": " + e.getMessage());
            }
        }
    }
}

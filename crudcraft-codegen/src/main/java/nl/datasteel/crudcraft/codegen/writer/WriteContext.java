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
package nl.datasteel.crudcraft.codegen.writer;

import com.squareup.javapoet.JavaFile;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import nl.datasteel.crudcraft.codegen.util.FilerUtils;

/**
 * Shared context for writer components. Holds the processing environment and
 * provides a helper to write files with consistent error handling.
 */
public class WriteContext {

    /**
     * The processing environment provided by the annotation processing framework.
     * It contains information about the current processing round and allows
     * access to the file system, messager, and other utilities.
     */
    private final ProcessingEnvironment env;

    /**
     * Constructs a WriteContext with the given processing environment.
     *
     * @param env the ProcessingEnvironment instance
     */
    public WriteContext(ProcessingEnvironment env) {
        this.env = env;
    }

    /**
     * Creates a deep/shallow copy of the context.
     */
    public WriteContext(WriteContext other) {
        this.env = other.env;
    }

    /**
     * Creates a snapshot of the current context.
     * Useful for passing around a consistent state without modifying the original.
     */
    public static WriteContext snapshotOf(WriteContext other) {
        return new WriteContext(other);
    }

    /**
     * Returns the processing environment for this context.
     * This is used to access the file system, messager, and other utilities.
     *
     * @return the ProcessingEnvironment instance
     */
    public ProcessingEnvironment env() {
        return env;
    }

    /**
     * Finds a {@link TypeElement} by its fully qualified class name.
     *
     * @param fqcn fully qualified class name
     * @return the {@link TypeElement} or {@code null} if not found
     */
    public TypeElement findTypeElement(String fqcn) {
        return env.getElementUtils().getTypeElement(fqcn);
    }

    /**
     * Writes a JavaFile to the file system, handling any IO exceptions.
     * If an error occurs, it logs an error message using the processing environment's messager.
     *
     * @param jf the JavaFile to write
     */
    public void write(JavaFile jf) {
        FilerUtils.writeJavaFile(jf, env.getFiler(), env.getMessager());
    }
}

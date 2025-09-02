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

package nl.datasteel.crudcraft.codegen.writer.stubs;

import com.squareup.javapoet.JavaFile;
import java.util.List;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.Generator;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;

/**
 * Specialized generator for editable/non-editable stubs.
 */
public interface StubGenerator extends Generator {

    /**
     * Build the JavaFile representing the stub.
     */
    JavaFile build(ModelDescriptor modelDescriptor, WriteContext ctx);

    /**
     * Generates the JavaFile for the given model descriptor.
     * This method is called by the write method to produce the files.
     *
     * @param modelDescriptor the model descriptor to generate code for
     * @param ctx shared write context
     * @return a list of generated JavaFiles; never {@code null}
     */
    @Override
    default List<JavaFile> generate(ModelDescriptor modelDescriptor, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(modelDescriptor, ctx)) {
            ctx.env().getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Skipping stub generation: invalid model descriptor");

            return List.of();
        }
        return List.of(build(modelDescriptor, ctx));
    }

    /**
     * Writes the generated stub files to the context.
     * Editable stubs are written just like non-editable ones; a subsequent
     * tooling step will decide whether to keep or copy them into the user's
     * source tree.
     *
     * @param modelDescriptor the model descriptor to generate code for
     * @param ctx shared write context
     */
    @Override
    default void write(ModelDescriptor modelDescriptor, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(modelDescriptor, ctx)) {
            return;
        }
        for (JavaFile javaFile : generate(modelDescriptor, ctx)) {
            ctx.write(javaFile);
        }
    }
}

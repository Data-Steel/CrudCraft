/*
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.codegen.writer;

import com.squareup.javapoet.JavaFile;
import java.util.List;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;

/**
 * Base contract for all writer components that turn a {@link ModelDescriptor}
 * into one or more {@link JavaFile}s.
 */
public interface Generator {

    /**
     * Builds the java files for the given model without writing them.
     *
     * @param model the model descriptor to generate code for
     * @param ctx   shared write context
     * @return generated java files; never {@code null}
     */
    List<JavaFile> generate(ModelDescriptor model, WriteContext ctx);

    /**
     * Indicates whether this generator should only be applied to models that
     * are marked as CrudCraft entities. Generators returning {@code true} are
     * executed only when the model descriptor reports {@link
     * ModelDescriptor#isCrudCraftEntity()} as {@code true}.
     *
     * @return {@code true} if the generator requires a CrudCraft entity
     */
    default boolean requiresCrudEntity() {
        return false;
    }

    /**
     * Determines the execution order within the generator group
     * (basic or CRUD). Lower values run first.
     *
     * @return the order value of this generator
     */
    default int order() {
        return 0;
    }

    /**
     * Writes the generated files using the provided context.
     */
    default void write(ModelDescriptor model, WriteContext ctx) {
        if (!isValidModelDescriptor(model, ctx)) {
            return;
        }
        for (JavaFile javaFile : generate(model, ctx)) {
            ctx.write(javaFile);
        }
    }

    /**
     * Performs basic sanity checks on the descriptor before code generation.
     * Logs a diagnostic message when invalid.
     *
     * @return {@code true} if the descriptor seems valid
     */
    static boolean isValidModelDescriptor(ModelDescriptor model, WriteContext ctx) {
        if (model == null) {
            ctx.env().getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Model descriptor is null; skipping generation");
            return false;
        }
        if (model.getName() == null || model.getName().isBlank()
                || model.getPackageName() == null || model.getPackageName().isBlank()) {
            ctx.env().getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Model descriptor has missing name or package");
            return false;
        }
        if (model.getFields() == null) {
            ctx.env().getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Model descriptor has null fields for " + model.getName());
            return false;
        }
        return true;
    }
}

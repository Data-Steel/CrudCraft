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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;

/**
 * Central registry for writer components. Discovers {@link Generator}
 * implementations via {@link ServiceLoader} to allow external modules to plug
 * in additional generators without modifying CrudCraft itself.
 */
public final class WriterRegistry {

    private static List<Generator> basicGenerators;
    private static List<Generator> crudGenerators;

    private WriterRegistry() {
    }

    private static void init(WriteContext ctx) {
        List<Generator> all = new ArrayList<>();
        ServiceLoader<Generator> loader = ServiceLoader.load(
                Generator.class, WriterRegistry.class.getClassLoader());
        loader.forEach(all::add);
        basicGenerators = all.stream()
                .filter(g -> !g.requiresCrudEntity())
                .sorted(Comparator.comparingInt(Generator::order))
                .toList();
        crudGenerators = all.stream()
                .filter(Generator::requiresCrudEntity)
                .sorted(Comparator.comparingInt(Generator::order))
                .toList();

        ctx.env().getMessager().printMessage(Diagnostic.Kind.NOTE, "Found "
                + basicGenerators.size() + " basic generators and "
                + crudGenerators.size() + " CRUD generators");
    }

    /**
     * Dispatches generation for the given model descriptor.
     */
    public static void writeAll(ModelDescriptor modelDescriptor, WriteContext ctx) {
        if (modelDescriptor == null) {
            ctx.env().getMessager().printMessage(
                    Diagnostic.Kind.WARNING,
                    "ModelDescriptor was null, skipping generation");
            return;
        }
        if (basicGenerators == null || crudGenerators == null) {
            init(ctx);
        }
        basicGenerators.forEach(g -> g.write(modelDescriptor, ctx));
        if (modelDescriptor.isCrudCraftEntity()) {
            crudGenerators.forEach(g -> g.write(modelDescriptor, ctx));
        }
    }
}

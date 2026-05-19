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

package nl.datasteel.crudcraft.codegen.writer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.stubs.ControllerGenerator;
import nl.datasteel.crudcraft.codegen.writer.stubs.MapperGenerator;
import nl.datasteel.crudcraft.codegen.writer.stubs.RepositoryGenerator;
import nl.datasteel.crudcraft.codegen.writer.stubs.ServiceGenerator;


/**
 * Central registry for writer components. Discovers {@link Generator} implementations via {@link
 * ServiceLoader} to allow external modules to plug in additional generators without modifying
 * CrudCraft itself.
 */
public final class WriterRegistry {

    private WriterRegistry() {}

    private static volatile List<Generator> generatorOverridesForTests;

    private static GeneratorSet resolveGenerators(WriteContext ctx) {
        Map<String, Generator> discovered = new LinkedHashMap<>();
        discoveredGenerators()
                .forEach(generator -> discovered.put(generator.getClass().getName(), generator));
        List<Generator> all = new ArrayList<>(discovered.values());
        List<Generator> basicGenerators =
                all.stream()
                        .filter(g -> !g.requiresCrudEntity())
                        .sorted(Comparator.comparingInt(Generator::order))
                        .toList();
        List<Generator> crudGenerators =
                all.stream()
                        .filter(Generator::requiresCrudEntity)
                        .sorted(Comparator.comparingInt(Generator::order))
                        .toList();

        ctx.env()
                .getMessager()
                .printMessage(
                        Diagnostic.Kind.NOTE,
                        "Found "
                                + basicGenerators.size()
                                + " basic generators and "
                                + crudGenerators.size()
                                + " CRUD generators");
        return new GeneratorSet(basicGenerators, crudGenerators);
    }

    private static List<Generator> discoveredGenerators() {
        List<Generator> overrides = generatorOverridesForTests;
        if (overrides != null) {
            return overrides;
        }

        List<Generator> discovered = new ArrayList<>(builtInGenerators());
        ServiceLoader<Generator> loader =
                ServiceLoader.load(Generator.class, WriterRegistry.class.getClassLoader());
        loader.forEach(discovered::add);
        return discovered;
    }

    private static List<Generator> builtInGenerators() {
        return List.of(
                new DtoGenerator(),
                new SearchGenerator(),
                new RelationshipMetaGenerator(),
                new RepositoryGenerator(),
                new MapperGenerator(),
                new ServiceGenerator(),
                new ControllerGenerator(),
                new InsomniaGenerator());
    }

    static void setGeneratorOverridesForTests(List<Generator> generators) {
        generatorOverridesForTests = generators == null ? null : List.copyOf(generators);
    }

    static void clearGeneratorOverridesForTests() {
        generatorOverridesForTests = null;
    }

    /**
     * Dispatches generation for the given model descriptor.
     *
     * @param modelDescriptor model to process
     * @param ctx write context
     */
    public static void writeAll(ModelDescriptor modelDescriptor, WriteContext ctx) {
        if (modelDescriptor == null) {
            ctx.env()
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.WARNING,
                            "ModelDescriptor was null, skipping generation");
            return;
        }
        GeneratorSet generators = resolveGenerators(ctx);
        for (Generator generator : generators.basicGenerators()) {
            generator.write(modelDescriptor, ctx);
        }
        if (modelDescriptor.isCrudCraftEntity()) {
            for (Generator generator : generators.crudGenerators()) {
                generator.write(modelDescriptor, ctx);
            }
        }
    }

    private record GeneratorSet(
            List<Generator> basicGenerators, List<Generator> crudGenerators) {}
}

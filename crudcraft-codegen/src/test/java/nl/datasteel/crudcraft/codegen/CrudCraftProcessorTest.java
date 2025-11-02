/**
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
package nl.datasteel.crudcraft.codegen;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import com.squareup.javapoet.JavaFile;
import java.lang.reflect.Field;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.Generator;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import nl.datasteel.crudcraft.codegen.writer.WriterRegistry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/** Tests for CrudCraftProcessor. */
class CrudCraftProcessorTest {

    @Test
    void reportsErrorsWhenGeneratorThrows() throws Exception {
        Field basic = WriterRegistry.class.getDeclaredField("basicGenerators");
        Field crud = WriterRegistry.class.getDeclaredField("crudGenerators");
        basic.setAccessible(true);
        crud.setAccessible(true);
        Object oldBasic = basic.get(null);
        Object oldCrud = crud.get(null);
        try {
            Generator failing = new Generator() {
                @Override
                public List<JavaFile> generate(ModelDescriptor model, WriteContext ctx) {
                    throw new RuntimeException("boom");
                }
            };
            basic.set(null, List.of(failing));
            crud.set(null, List.of());

            JavaFileObject crafted = JavaFileObjects.forSourceLines(
                    "com.example.Book",
                    "package com.example;",
                    "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                    "@CrudCrafted",
                    "public class Book { String title; }");

            Compilation compilation = Compiler.javac()
                    .withProcessors(new CrudCraftProcessor())
                    .compile(crafted);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(compilation.diagnostics().stream()
                    .anyMatch(d -> d.getKind() == Diagnostic.Kind.ERROR
                            && d.getMessage(null).contains("CrudCraftProcessor failed for Book")));
        } finally {
            basic.set(null, oldBasic);
            crud.set(null, oldCrud);
        }
    }
}

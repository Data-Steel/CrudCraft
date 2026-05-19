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

package nl.datasteel.crudcraft.codegen.writer.stubs;

import com.sun.source.util.JavacTask;
import java.net.URI;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;


/** Utility to create {@link TypeMirror} instances for tests using the system compiler. */
class TypeFactory {
    private static final Elements ELEMENTS;
    private static final Types TYPES;

    static {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileObject dummy =
                new SimpleJavaFileObject(
                        URI.create("string:///Dummy.java"), JavaFileObject.Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                        return "class Dummy {}";
                    }
                };
        JavacTask task =
                (JavacTask)
                        compiler.getTask(
                                null,
                                null,
                                null,
                                CompilationTestUtils.compilerOptions(),
                                null,
                                List.of(dummy));
        ELEMENTS = task.getElements();
        TYPES = task.getTypes();
    }

    TypeMirror type(Class<?> clazz) {
        TypeElement te = ELEMENTS.getTypeElement(clazz.getCanonicalName());
        return te.asType();
    }

    DeclaredType listOf(Class<?> clazz) {
        return TYPES.getDeclaredType(ELEMENTS.getTypeElement("java.util.List"), type(clazz));
    }

    DeclaredType setOf(Class<?> clazz) {
        return TYPES.getDeclaredType(ELEMENTS.getTypeElement("java.util.Set"), type(clazz));
    }
}

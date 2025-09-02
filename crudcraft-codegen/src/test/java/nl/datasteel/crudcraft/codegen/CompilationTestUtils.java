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

package nl.datasteel.crudcraft.codegen;

import com.google.testing.compile.JavaFileObjects;
import com.sun.source.util.JavacTask;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/** Utility methods for compiling source snippets in tests. */
public final class CompilationTestUtils {

    private CompilationTestUtils() {}

    /**
     * Compile the provided sources and return the {@link Elements} utility from the processing environment.
     *
     * @param sources Java sources to compile
     * @return {@link Elements} from the compilation environment
     */
    public static Elements elements(JavaFileObject... sources) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Run tests with a JDK (not a JRE)");
        }
        StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null);
        List<String> opts = List.of(
                "-proc:none",
                "-g", "-nowarn"
        );
        JavacTask task = (JavacTask) compiler.getTask(
                /* out */ null, fm, /* diag */ null, opts, /* classes */ null,
                Arrays.asList(sources)
        );
        try {
            task.parse();
            task.analyze();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize javac task", e);
        }
        return task.getElements();
    }

    public static Elements elements(String className, String code) {
        return elements(JavaFileObjects.forSourceString(className, code));
    }

    private static class CapturingProcessor extends AbstractProcessor {
        Elements elements;
        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            this.elements = processingEnv.getElementUtils();
            return false;
        }
        @Override
        public Set<String> getSupportedAnnotationTypes() {
            return Set.of("*");
        }
    }
}

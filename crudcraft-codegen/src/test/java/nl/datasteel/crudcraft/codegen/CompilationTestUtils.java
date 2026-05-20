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

package nl.datasteel.crudcraft.codegen;

import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import com.sun.source.util.JavacTask;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.lang.model.util.Elements;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import nl.datasteel.crudcraft.codegen.writer.search.SearchRequestGenerator;


/** Utility methods for compiling source snippets in tests. */
public final class CompilationTestUtils {

    private static final Map<String, Elements> ELEMENTS_CACHE = new ConcurrentHashMap<>();
    private static final String[] COMPILATION_CLASSPATH_TYPES = {
        "com.fasterxml.jackson.annotation.JsonInclude",
        "com.palantir.javapoet.JavaFile",
        "edu.umd.cs.findbugs.annotations.SuppressFBWarnings",
        "io.swagger.v3.oas.annotations.media.Schema",
        "jakarta.annotation.Nullable",
        "jakarta.persistence.Entity",
        "jakarta.persistence.Id",
        "jakarta.persistence.criteria.CriteriaBuilder",
        "jakarta.validation.constraints.NotBlank",
        "jakarta.validation.constraints.Size",
        "io.micrometer.core.instrument.MeterRegistry",
        "io.micrometer.core.instrument.Timer",
        "nl.datasteel.crudcraft.annotations.CrudEndpoint",
        "nl.datasteel.crudcraft.annotations.CrudTemplate",
        "nl.datasteel.crudcraft.annotations.classes.CrudCrafted",
        "nl.datasteel.crudcraft.annotations.fields.Dto",
        "nl.datasteel.crudcraft.annotations.fields.ProjectionField",
        "nl.datasteel.crudcraft.annotations.fields.Request",
        "nl.datasteel.crudcraft.annotations.fields.Searchable",
        "nl.datasteel.crudcraft.codegen.CrudCraftProcessor",
        "nl.datasteel.crudcraft.codegen.reader.TestEndpointPolicy",
        "nl.datasteel.crudcraft.runtime.controller.AbstractCrudController",
        "nl.datasteel.crudcraft.runtime.export.service.ExportService",
        "nl.datasteel.crudcraft.runtime.extensions.util.RelationshipUtils",
        "nl.datasteel.crudcraft.runtime.projection.mapping.ProjectionMetadataRegistry",
        "nl.datasteel.crudcraft.runtime.security.FieldSecurityUtil",
        "nl.datasteel.crudcraft.runtime.security.row.ClaimScopedRowSecurityHandler",
        "nl.datasteel.crudcraft.runtime.security.scope.PrincipalScopeAccessor",
        "nl.datasteel.crudcraft.runtime.search.SearchRequest",
        "nl.datasteel.crudcraft.runtime.service.AbstractCrudService",
        "org.mapstruct.Mapper",
        "org.slf4j.Logger",
        "org.springframework.data.domain.Pageable",
        "org.springframework.data.jpa.domain.Specification",
        "org.springframework.data.jpa.repository.JpaRepository",
        "org.springframework.data.repository.ListCrudRepository",
        "org.springframework.http.ResponseEntity",
        "org.springframework.security.access.prepost.PreAuthorize",
        "org.springframework.stereotype.Component",
        "org.springframework.stereotype.Service",
        "org.springframework.transaction.annotation.Transactional",
        "org.springframework.web.bind.annotation.GetMapping"
    };

    private CompilationTestUtils() {}

    /**
     * Compile the provided sources and return the {@link Elements} utility from the processing
     * environment.
     *
     * @param sources Java sources to compile
     * @return {@link Elements} from the compilation environment
     */
    public static Elements elements(JavaFileObject... sources) {
        String key = cacheKey(sources);
        return ELEMENTS_CACHE.computeIfAbsent(
                key,
                cacheKey -> {
                    cacheKey.length();
                    return compileElements(sources);
                });
    }

    /**
     * Create a compile-testing compiler with the classpath needed for non-modular test snippets.
     *
     * @param baseOptions options to pass to javac before classpath options
     * @return configured compile-testing compiler
     */
    public static Compiler javac(String... baseOptions) {
        return Compiler.javac().withOptions(compilerOptions(baseOptions));
    }

    private static Elements compileElements(JavaFileObject... sources) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Run tests with a JDK (not a JRE)");
        }
        StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null);
        List<String> opts = compilerOptions("-proc:none", "-g", "-nowarn");
        JavacTask task =
                (JavacTask)
                        compiler.getTask(
                                /* out */ null,
                                fm, /* diag */
                                null,
                                opts, /* classes */
                                null,
                                Arrays.asList(sources));
        try {
            task.parse();
            task.analyze();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize javac task", e);
        }
        return task.getElements();
    }

    /**
     * Build javac options for snippet compilation.
     *
     * @param baseOptions options to pass to javac before classpath options
     * @return javac options with an explicit test classpath
     */
    public static List<String> compilerOptions(String... baseOptions) {
        List<String> options = new ArrayList<>();
        options.add("-Xlint:-options");
        options.addAll(List.of(baseOptions));
        String classPath = snippetClasspath();
        if (!classPath.isBlank()) {
            options.add("--class-path");
            options.add(classPath);
        }
        return options;
    }

    private static String snippetClasspath() {
        LinkedHashSet<String> entries = new LinkedHashSet<>();
        addCodeSource(entries, CompilationTestUtils.class);
        addCodeSource(entries, CrudCraftProcessor.class);
        addCodeSource(entries, SearchRequestGenerator.class);
        addPathEntries(entries, System.getProperty("surefire.test.class.path"));
        addPathEntries(entries, System.getProperty("java.class.path"));
        for (String typeName : COMPILATION_CLASSPATH_TYPES) {
            addTypeCodeSource(entries, typeName);
        }
        return String.join(System.getProperty("path.separator"), entries);
    }

    private static void addPathEntries(Set<String> entries, String classPath) {
        if (classPath == null || classPath.isBlank()) {
            return;
        }
        Arrays.stream(classPath.split(System.getProperty("path.separator")))
                .filter(path -> !isInstalledCodegenJar(path))
                .forEach(entries::add);
    }

    private static boolean isInstalledCodegenJar(String path) {
        return path.replace('\\', '/').matches(".*/crudcraft-codegen-[^/]+\\.jar");
    }

    private static void addTypeCodeSource(Set<String> entries, String typeName) {
        try {
            Class<?> type =
                    Class.forName(
                            typeName, false, Thread.currentThread().getContextClassLoader());
            addCodeSource(entries, type);
        } catch (ClassNotFoundException ignored) {
            // Optional dependencies are only added when the current test resolved them.
        }
    }

    private static void addCodeSource(Set<String> entries, Class<?> type) {
        if (type.getProtectionDomain().getCodeSource() == null) {
            return;
        }
        URL location = type.getProtectionDomain().getCodeSource().getLocation();
        try {
            entries.add(Path.of(location.toURI()).toString());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid classpath location for " + type.getName(), e);
        }
    }

    private static String cacheKey(JavaFileObject... sources) {
        StringBuilder key = new StringBuilder();
        for (JavaFileObject source : sources) {
            key.append(source.getName()).append('\n');
            try {
                key.append(source.getCharContent(true)).append('\n');
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read test source", e);
            }
        }
        return key.toString();
    }

    public static Elements elements(String className, String code) {
        return elements(JavaFileObjects.forSourceString(className, code));
    }

    /**
     * Loads a source file from the test classpath.
     *
     * @param resourcePath classpath-relative source path
     * @return source file object for compile-testing
     */
    public static JavaFileObject sourceFromResource(String resourcePath) {
        return JavaFileObjects.forResource(resourcePath);
    }
}

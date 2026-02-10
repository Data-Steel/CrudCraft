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

import com.google.testing.compile.JavaFileObjects;
import com.squareup.javapoet.JavaFile;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.reader.AnnotationModelReader;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests for SearchGenerator. */
class SearchGeneratorTest {

    private WriteContext ctx;

    // Helper ProcessingEnvironment stub for tests that need real compilation
    private static class EnvStub implements ProcessingEnvironment {
        private final Elements elements;
        EnvStub(Elements elements) { this.elements = elements; }
        @Override public Map<String, String> getOptions() { return Map.of(); }
        @Override public Messager getMessager() { return new Messager() {
            @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg) {}
            @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {}
            @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, javax.lang.model.element.AnnotationMirror a) {}
            @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, javax.lang.model.element.AnnotationMirror a, javax.lang.model.element.AnnotationValue v) {}
        }; }
        @Override public Filer getFiler() { return null; }
        @Override public Elements getElementUtils() { return elements; }
        @Override public Types getTypeUtils() { return null; }
        @Override public SourceVersion getSourceVersion() { return SourceVersion.latest(); }
        @Override public Locale getLocale() { return Locale.getDefault(); }
    }

    @BeforeEach
    void setup() {
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        Messager messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);
        ctx = new WriteContext(env);
    }

    @Test
    void returnsEmptyForInvalidModel() {
        SearchGenerator gen = new SearchGenerator();
        assertTrue(gen.generate(null, ctx).isEmpty());
    }

    @Test
    void generatesRequestAndSpecification() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields()).thenReturn(List.of());

        SearchGenerator gen = new SearchGenerator();
        List<JavaFile> files = gen.generate(md, ctx);
        assertEquals(2, files.size());
        assertEquals("BookSearchRequest", files.get(0).typeSpec.name);
        assertEquals("BookSpecification", files.get(1).typeSpec.name);
    }

    @Test
    void specificationIncludesDistinctForManyToManyRelationships() {
        // Create a model with a ManyToMany relationship field
        String entitySrc = "package test;" +
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;" +
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;" +
                "import jakarta.persistence.ManyToMany;" +
                "@CrudCrafted class SystemEntity {" +
                "  @Searchable @ManyToMany java.util.Set<SystemEntityVariant> systemEntityVariants;" +
                "}";
        String variantSrc = "package test;" +
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;" +
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;" +
                "import jakarta.persistence.Entity;" +
                "@CrudCrafted @Entity class SystemEntityVariant {" +
                "  @Searchable Long id;" +
                "}";

        Elements elements = CompilationTestUtils.elements(
                JavaFileObjects.forSourceString("test.SystemEntity", entitySrc),
                JavaFileObjects.forSourceString("test.SystemEntityVariant", variantSrc)
        );
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext testCtx = new WriteContext(env);

        var entityEl = elements.getTypeElement("test.SystemEntity");
        ModelDescriptor md = AnnotationModelReader.parse(entityEl, env);

        SearchGenerator gen = new SearchGenerator();
        List<JavaFile> files = gen.generate(md, testCtx);

        // Verify that both files are generated
        assertEquals(2, files.size(), "Should generate SearchRequest and Specification");

        // Get the Specification file (second one)
        JavaFile specFile = files.get(1);
        assertEquals("SystemEntitySpecification", specFile.typeSpec.name);

        // Convert to string and verify it contains the distinct call
        String specCode = specFile.toString();
        assertTrue(specCode.contains("query.distinct(true)"),
                "Specification should call query.distinct(true) when ManyToMany relationships are searched");

        // Verify it has the nested field for filtering (systemEntityVariantsId)
        assertTrue(specCode.contains("systemEntityVariantsId") || specCode.contains("join(\"systemEntityVariants\")"),
                "Specification should include the nested field path with join");
    }

    @Test
    void specificationDoesNotIncludeDistinctForSimpleFields() {
        // Create a model with only simple fields (no relationships)
        String entitySrc = "package test;" +
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;" +
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;" +
                "@CrudCrafted class SimpleEntity {" +
                "  @Searchable String name;" +
                "  @Searchable Long id;" +
                "}";

        Elements elements = CompilationTestUtils.elements(
                JavaFileObjects.forSourceString("test.SimpleEntity", entitySrc)
        );
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext testCtx = new WriteContext(env);

        var entityEl = elements.getTypeElement("test.SimpleEntity");
        ModelDescriptor md = AnnotationModelReader.parse(entityEl, env);

        SearchGenerator gen = new SearchGenerator();
        List<JavaFile> files = gen.generate(md, testCtx);

        assertEquals(2, files.size());

        // Get the Specification file
        JavaFile specFile = files.get(1);
        String specCode = specFile.toString();

        // Should NOT contain distinct call for simple fields
        assertFalse(specCode.contains("query.distinct(true)"),
                "Specification should NOT call query.distinct(true) when only simple fields are searched");
    }

    @Test
    void generatedRequestHasSearchLogicField() {
        // Create a simple model
        String entitySrc = "package test;" +
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;" +
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;" +
                "@CrudCrafted class SimpleEntity {" +
                "  @Searchable String name;" +
                "}";

        Elements elements = CompilationTestUtils.elements(
                JavaFileObjects.forSourceString("test.SimpleEntity", entitySrc)
        );
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext testCtx = new WriteContext(env);

        var entityEl = elements.getTypeElement("test.SimpleEntity");
        ModelDescriptor md = AnnotationModelReader.parse(entityEl, env);

        SearchGenerator gen = new SearchGenerator();
        List<JavaFile> files = gen.generate(md, testCtx);

        // Get the SearchRequest file
        JavaFile requestFile = files.get(0);
        String requestCode = requestFile.toString();

        // Should contain searchLogic field
        assertTrue(requestCode.contains("private SearchLogic searchLogic"),
                "SearchRequest should have a searchLogic field");
        assertTrue(requestCode.contains("public SearchLogic getSearchLogic()"),
                "SearchRequest should have a getSearchLogic() method");
        assertTrue(requestCode.contains("public void setSearchLogic(SearchLogic searchLogic)"),
                "SearchRequest should have a setSearchLogic() method");
        assertTrue(requestCode.contains("searchLogic != null ? searchLogic : SearchLogic.OR"),
                "getSearchLogic should default to OR");
    }

    @Test
    void generatedSpecificationUsesSearchLogic() {
        // Create a simple model
        String entitySrc = "package test;" +
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;" +
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;" +
                "@CrudCrafted class SimpleEntity {" +
                "  @Searchable String name;" +
                "}";

        Elements elements = CompilationTestUtils.elements(
                JavaFileObjects.forSourceString("test.SimpleEntity", entitySrc)
        );
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext testCtx = new WriteContext(env);

        var entityEl = elements.getTypeElement("test.SimpleEntity");
        ModelDescriptor md = AnnotationModelReader.parse(entityEl, env);

        SearchGenerator gen = new SearchGenerator();
        List<JavaFile> files = gen.generate(md, testCtx);

        // Get the Specification file
        JavaFile specFile = files.get(1);
        String specCode = specFile.toString();

        // Should contain logic variable initialization
        assertTrue(specCode.contains("SearchLogic logic = request.getSearchLogic()"),
                "Specification should get logic from request");
        assertTrue(specCode.contains("logic == SearchLogic.AND ? cb.conjunction() : cb.disjunction()"),
                "Specification should initialize predicate based on logic");
        assertTrue(specCode.contains("logic == SearchLogic.AND ? cb.and(p,") ||
                        specCode.contains("logic == SearchLogic.AND ? cb.and"),
                "Specification should use logic to combine predicates");
    }
}

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
package nl.datasteel.crudcraft.codegen.writer.search;

import com.google.testing.compile.JavaFileObjects;
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
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.reader.AnnotationModelReader;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SearchFieldCollectorTest {

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

    private static record Fixture(ModelDescriptor root, SearchFieldCollector collector) {}

    private Fixture fixture() {
        String rootSrc = "package t;"+
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"+
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;"+
                "import nl.datasteel.crudcraft.annotations.SearchOperator;"+
                "import jakarta.persistence.ManyToOne;"+
                "@CrudCrafted class Root {"+
                "  @Searchable(depth=2) @ManyToOne Child child;"+
                "  @Searchable(operators={SearchOperator.SIZE_GT}) java.util.List<String> tags;"+
                "  int ignored;"+
                "}";
        String childSrc = "package t;"+
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"+
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;"+
                "@CrudCrafted class Child {"+
                "  @Searchable String value;"+
                "}";
        Elements elements = CompilationTestUtils.elements(
                JavaFileObjects.forSourceString("t.Root", rootSrc),
                JavaFileObjects.forSourceString("t.Child", childSrc)
        );
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext ctx = new WriteContext(env);
        var rootEl = elements.getTypeElement("t.Root");
        ModelDescriptor root = AnnotationModelReader.parse(rootEl, env);
        return new Fixture(root, new SearchFieldCollector(ctx));
    }

    @Test
    void collectsNestedFieldsUpToDepth() {
        Fixture fx = fixture();
        List<SearchField> fields = fx.collector.collect(fx.root, 2);
        // Should have 2 fields: childValue (flattened from child.value) and tagsSize
        // The "child" field itself should NOT be included since it's a relationship
        // that will recurse into nested searchable fields
        assertEquals(2, fields.size());
        assertTrue(fields.stream().anyMatch(f -> f.property().equals("childValue") && f.path().equals("root.join(\"child\").get(\"value\")")));
        assertTrue(fields.stream().anyMatch(f -> f.property().equals("tagsSize") && f.operator() == SearchOperator.SIZE_GT));
        // Verify that "child" relationship field itself is NOT included
        assertFalse(fields.stream().anyMatch(f -> f.property().equals("child")));
    }

    @Test
    void relationshipFieldWithoutNestedSearchableFieldsIsIncluded() {
        // Test case where a relationship field has no nested searchable fields
        // In this case, the relationship field itself should be included
        String rootSrc = "package t;"+
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"+
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;"+
                "import jakarta.persistence.ManyToOne;"+
                "@CrudCrafted class Parent {"+
                "  @Searchable @ManyToOne ChildWithoutSearchable child;"+
                "}";
        String childSrc = "package t;"+
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"+
                "@CrudCrafted class ChildWithoutSearchable {"+
                "  String value;"+
                "}";
        Elements elements = CompilationTestUtils.elements(
                JavaFileObjects.forSourceString("t.Parent", rootSrc),
                JavaFileObjects.forSourceString("t.ChildWithoutSearchable", childSrc)
        );
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext ctx = new WriteContext(env);
        var rootEl = elements.getTypeElement("t.Parent");
        ModelDescriptor root = AnnotationModelReader.parse(rootEl, env);
        SearchFieldCollector collector = new SearchFieldCollector(ctx);

        List<SearchField> fields = collector.collect(root, 2);
        // The child field should be included since it has no nested searchable fields
        assertEquals(1, fields.size());
        assertTrue(fields.stream().anyMatch(f -> f.property().equals("child")));
    }

    @Test
    void postAuthorScenarioFlattenedCorrectly() {
        // Mimic the exact Post/Author scenario from the sample app
        String postSrc = "package t;"+
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"+
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;"+
                "import jakarta.persistence.ManyToOne;"+
                "@CrudCrafted class BlogPost {"+
                "  @Searchable String title;"+
                "  @Searchable(depth=2) @ManyToOne BlogAuthor author;"+
                "}";
        String authorSrc = "package t;"+
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"+
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;"+
                "@CrudCrafted class BlogAuthor {"+
                "  @Searchable String name;"+
                "  @Searchable String email;"+
                "}";
        Elements elements = CompilationTestUtils.elements(
                JavaFileObjects.forSourceString("t.BlogPost", postSrc),
                JavaFileObjects.forSourceString("t.BlogAuthor", authorSrc)
        );
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext ctx = new WriteContext(env);
        var rootEl = elements.getTypeElement("t.BlogPost");
        ModelDescriptor root = AnnotationModelReader.parse(rootEl, env);
        SearchFieldCollector collector = new SearchFieldCollector(ctx);

        List<SearchField> fields = collector.collect(root, 2);
        // Should have: title, authorName, authorEmail (NOT the author field itself)
        assertEquals(3, fields.size());
        assertTrue(fields.stream().anyMatch(f -> f.property().equals("title")));
        assertTrue(fields.stream().anyMatch(f -> f.property().equals("authorName")));
        assertTrue(fields.stream().anyMatch(f -> f.property().equals("authorEmail")));
        // The "author" relationship field itself should NOT be included
        assertFalse(fields.stream().anyMatch(f -> f.property().equals("author")));
    }

    @Test
    void depthZeroReturnsEmpty() {
        Fixture fx = fixture();
        List<SearchField> fields = fx.collector.collect(fx.root, 0);
        assertTrue(fields.isEmpty());
    }

    @Test
    void nullModelThrowsNpe() {
        Fixture fx = fixture();
        assertThrows(NullPointerException.class, () -> fx.collector.collect(null, 1));
    }
}

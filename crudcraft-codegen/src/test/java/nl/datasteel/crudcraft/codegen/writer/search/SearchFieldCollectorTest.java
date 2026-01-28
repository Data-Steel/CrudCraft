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
                "@CrudCrafted class Root {"+
                "  @Searchable(depth=2) Child child;"+
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
        
        // With the fix, we now correctly collect both:
        // 1. tagsSize - from the List<String> tags field with SIZE_GT operator
        // 2. childValue - from the nested Child.value field (depth=2 allows recursion)
        assertEquals(2, fields.size());
        assertTrue(fields.stream().anyMatch(f -> f.property().equals("tagsSize") && f.operator() == SearchOperator.SIZE_GT));
        assertTrue(fields.stream().anyMatch(f -> f.property().equals("childValue") && f.operator() == SearchOperator.EQUALS));
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

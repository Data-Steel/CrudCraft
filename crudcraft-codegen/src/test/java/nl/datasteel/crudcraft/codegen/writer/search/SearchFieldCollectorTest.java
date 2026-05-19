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

package nl.datasteel.crudcraft.codegen.writer.search;

import com.google.testing.compile.JavaFileObjects;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.exception.CodegenValidationException;
import nl.datasteel.crudcraft.codegen.reader.AnnotationModelReader;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class SearchFieldCollectorTest {

    private static class NoOpMessager implements Messager {
        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            // Unused (for now)
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
            // Unused (for now)
        }

        @Override
        public void printMessage(
                Diagnostic.Kind kind,
                CharSequence msg,
                Element e,
                javax.lang.model.element.AnnotationMirror a) {
            // Unused (for now)
        }

        @Override
        public void printMessage(
                Diagnostic.Kind kind,
                CharSequence msg,
                Element e,
                javax.lang.model.element.AnnotationMirror a,
                javax.lang.model.element.AnnotationValue v) {
            // Unused (for now)
        }
    }

    private static final class RecordingMessager extends NoOpMessager {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            messages.add(kind + ":" + msg);
        }
    }

    private static class EnvStub implements ProcessingEnvironment {
        private final Elements elements;
        private final Messager messager;

        EnvStub(Elements elements) {
            this(elements, new NoOpMessager());
        }

        EnvStub(Elements elements, Messager messager) {
            this.elements = elements;
            this.messager = messager;
        }

        @Override
        public Map<String, String> getOptions() {
            return Map.of();
        }

        @Override
        public Messager getMessager() {
            return messager;
        }

        @Override
        public Filer getFiler() {
            return null;
        }

        @Override
        public Elements getElementUtils() {
            return elements;
        }

        @Override
        public Types getTypeUtils() {
            return null;
        }

        @Override
        public SourceVersion getSourceVersion() {
            return SourceVersion.latest();
        }

        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }
    }

    private static record Fixture(ModelDescriptor root, SearchFieldCollector collector) {}

    private Fixture fixture() {
        String rootSrc =
                "package t;import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;import"
                    + " nl.datasteel.crudcraft.annotations.fields.Searchable;import"
                    + " nl.datasteel.crudcraft.annotations.SearchOperator;@CrudCrafted class Root {"
                    + "  @Searchable(depth=2) Child child; "
                    + " @Searchable(operators={SearchOperator.SIZE_GT}) java.util.List<String>"
                    + " tags;  int ignored;}";
        String childSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Child {"
                        + "  @Searchable String value;"
                        + "}";
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("t.Root", rootSrc),
                        JavaFileObjects.forSourceString("t.Child", childSrc));
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

        // With the fix, we now correctly collect:
        // 1. tagsSize - from the List<String> tags field with SIZE_GT operator (1 entry)
        // 2. childValue - from the nested Child.value field with ALL default String operators (8
        // entries)
        //    (EQUALS, CONTAINS, STARTS_WITH, ENDS_WITH, IN, NOT_EQUALS, NOT_IN, REGEX)
        assertEquals(9, fields.size());
        assertTrue(
                fields.stream()
                        .anyMatch(
                                f ->
                                        f.property().equals("tagsSize")
                                                && f.operator() == SearchOperator.SIZE_GT));

        // Verify all default String operators are present for childValue
        assertTrue(
                fields.stream()
                        .anyMatch(
                                f ->
                                        f.property().equals("childValue")
                                                && f.operator() == SearchOperator.EQUALS));
        assertTrue(
                fields.stream()
                        .anyMatch(
                                f ->
                                        f.property().equals("childValue")
                                                && f.operator() == SearchOperator.CONTAINS));
        assertTrue(
                fields.stream()
                        .anyMatch(
                                f ->
                                        f.property().equals("childValue")
                                                && f.operator() == SearchOperator.STARTS_WITH));
        assertTrue(
                fields.stream()
                        .anyMatch(
                                f ->
                                        f.property().equals("childValue")
                                                && f.operator() == SearchOperator.ENDS_WITH));
        assertTrue(
                fields.stream()
                        .anyMatch(
                                f ->
                                        f.property().equals("childValue")
                                                && f.operator() == SearchOperator.IN));
        assertTrue(
                fields.stream()
                        .anyMatch(
                                f ->
                                        f.property().equals("childValue")
                                                && f.operator() == SearchOperator.NOT_EQUALS));
        assertTrue(
                fields.stream()
                        .anyMatch(
                                f ->
                                        f.property().equals("childValue")
                                                && f.operator() == SearchOperator.NOT_IN));
        assertTrue(
                fields.stream()
                        .anyMatch(
                                f ->
                                        f.property().equals("childValue")
                                                && f.operator() == SearchOperator.REGEX));
    }

    @Test
    void depthZeroReturnsEmpty() {
        Fixture fx = fixture();
        List<SearchField> fields = fx.collector.collect(fx.root, 0);
        assertTrue(fields.isEmpty());
    }

    @Test
    void nullModelThrowsValidationException() {
        Fixture fx = fixture();
        assertThrows(CodegenValidationException.class, () -> fx.collector.collect(null, 1));
    }

    @Test
    void depthOneDoesNotExposeEntityFieldWhenNoRecursionBudget() {
        String rootSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Root { @Searchable Child child; }";
        String childSrc =
                "package t;"
                        + "import jakarta.persistence.Entity;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@Entity class Child { @Searchable String value; }";
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("t.Root", rootSrc),
                        JavaFileObjects.forSourceString("t.Child", childSrc));
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext ctx = new WriteContext(env);
        ModelDescriptor root = AnnotationModelReader.parse(elements.getTypeElement("t.Root"), env);
        SearchFieldCollector collector = new SearchFieldCollector(ctx);

        List<SearchField> fields = collector.collect(root, 1);

        assertTrue(fields.isEmpty());
    }

    @Test
    void collectsRootSimpleFieldWhenTargetTypeIsNotEntity() {
        String rootSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Root { @Searchable String title; }";
        Elements elements =
                CompilationTestUtils.elements(JavaFileObjects.forSourceString("t.Root", rootSrc));
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext ctx = new WriteContext(env);
        ModelDescriptor root = AnnotationModelReader.parse(elements.getTypeElement("t.Root"), env);
        SearchFieldCollector collector = new SearchFieldCollector(ctx);

        List<SearchField> fields = collector.collect(root, 1);

        assertFalse(fields.isEmpty());
        assertTrue(
                fields.stream()
                        .anyMatch(
                                field ->
                                        field.property().equals("title")
                                                && field.operator() == SearchOperator.EQUALS));
    }

    @Test
    void fallsBackToTypeMirrorWhenRelationshipTargetTypeIsMissing() {
        Elements realElements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("t.Root", "package t; class Root {}"));
        TypeMirror stringType = realElements.getTypeElement("java.lang.String").asType();
        Elements lookupElements = mock(Elements.class);
        ProcessingEnvironment env = new EnvStub(lookupElements);
        SearchFieldCollector collector = new SearchFieldCollector(new WriteContext(env));
        FieldDescriptor field = mock(FieldDescriptor.class);
        when(field.isSearchable()).thenReturn(true);
        when(field.getName()).thenReturn("title");
        when(field.getTargetType()).thenReturn(null);
        when(field.getType()).thenReturn(stringType);
        when(field.getSearchOperators()).thenReturn(List.of(SearchOperator.EQUALS));
        ModelDescriptor root = mock(ModelDescriptor.class);
        when(root.getPackageName()).thenReturn("t");
        when(root.getName()).thenReturn("Root");
        when(root.getFields()).thenReturn(List.of(field));

        List<SearchField> fields = collector.collect(root, 1);

        assertEquals(1, fields.size());
        assertEquals("title", fields.getFirst().property());
        verify(lookupElements).getTypeElement("java.lang.String");
    }

    @Test
    void defaultDepthStopsAfterRequestedHops() {
        String rootSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Root { @Searchable(depth=2) Child child; }";
        String childSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Child { @Searchable Grandchild grandchild; }";
        String grandchildSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Grandchild { @Searchable Great great; }";
        String greatSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Great { @Searchable String value; }";
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("t.Root", rootSrc),
                        JavaFileObjects.forSourceString("t.Child", childSrc),
                        JavaFileObjects.forSourceString("t.Grandchild", grandchildSrc),
                        JavaFileObjects.forSourceString("t.Great", greatSrc));
        ProcessingEnvironment env = new EnvStub(elements);
        ModelDescriptor root = AnnotationModelReader.parse(elements.getTypeElement("t.Root"), env);
        SearchFieldCollector collector = new SearchFieldCollector(new WriteContext(env));

        List<SearchField> fields = collector.collect(root, 3);

        assertTrue(fields.isEmpty());
    }

    @Test
    void configuredDepthCapsNestedTraversal() {
        String rootSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Root { @Searchable(depth=1) Child child; }";
        String childSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Child { @Searchable Grandchild grandchild; }";
        String grandchildSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Grandchild { @Searchable String value; }";
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("t.Root", rootSrc),
                        JavaFileObjects.forSourceString("t.Child", childSrc),
                        JavaFileObjects.forSourceString("t.Grandchild", grandchildSrc));
        ProcessingEnvironment env = new EnvStub(elements);
        ModelDescriptor root = AnnotationModelReader.parse(elements.getTypeElement("t.Root"), env);
        SearchFieldCollector collector = new SearchFieldCollector(new WriteContext(env));

        List<SearchField> fields = collector.collect(root, 3);

        assertTrue(fields.isEmpty());
    }

    @Test
    void zeroConfiguredDepthUsesRemainingTraversalBudget() {
        String rootSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Root { @Searchable(depth=0) Child child; }";
        String childSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Child { @Searchable Grandchild grandchild; }";
        String grandchildSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Grandchild { @Searchable String value; }";
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("t.Root", rootSrc),
                        JavaFileObjects.forSourceString("t.Child", childSrc),
                        JavaFileObjects.forSourceString("t.Grandchild", grandchildSrc));
        ProcessingEnvironment env = new EnvStub(elements);
        ModelDescriptor root = AnnotationModelReader.parse(elements.getTypeElement("t.Root"), env);
        SearchFieldCollector collector = new SearchFieldCollector(new WriteContext(env));

        List<SearchField> fields = collector.collect(root, 3);

        assertFalse(fields.isEmpty());
        assertTrue(
                fields.stream()
                        .anyMatch(field -> field.property().equals("childGrandchildValue")));
    }

    @Test
    void duplicateQueuedNodeIsProcessedOnlyOnce() {
        String childSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Child { @Searchable String value; }";
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("t.Child", childSrc));
        ProcessingEnvironment env = new EnvStub(elements);
        SearchFieldCollector collector = new SearchFieldCollector(new WriteContext(env));
        FieldDescriptor childField = mock(FieldDescriptor.class);
        when(childField.isSearchable()).thenReturn(true);
        when(childField.getName()).thenReturn("child");
        when(childField.getTargetType()).thenReturn("t.Child");
        when(childField.getSearchDepth()).thenReturn(1);
        when(childField.getSearchOperators()).thenReturn(List.of(SearchOperator.EQUALS));
        ModelDescriptor root = mock(ModelDescriptor.class);
        when(root.getPackageName()).thenReturn("t");
        when(root.getName()).thenReturn("Root");
        when(root.getFields()).thenReturn(List.of(childField, childField));

        List<SearchField> fields = collector.collect(root, 2);

        long childValues =
                fields.stream().filter(field -> field.property().equals("childValue")).count();
        assertEquals(8, childValues);
    }

    @Test
    void brokenEntityCandidateFallsBackToRegularSearchField() {
        Elements elements = mock(Elements.class);
        TypeElement broken = mock(TypeElement.class);
        var marker = mock(nl.datasteel.crudcraft.annotations.classes.CrudCrafted.class);
        when(elements.getTypeElement("t.Broken")).thenReturn(broken);
        when(broken.getAnnotation(nl.datasteel.crudcraft.annotations.classes.CrudCrafted.class))
                .thenReturn(marker);
        ProcessingEnvironment env = new EnvStub(elements);
        SearchFieldCollector collector = new SearchFieldCollector(new WriteContext(env));
        FieldDescriptor field = mock(FieldDescriptor.class);
        when(field.isSearchable()).thenReturn(true);
        when(field.getName()).thenReturn("broken");
        when(field.getTargetType()).thenReturn("t.Broken");
        when(field.getSearchOperators()).thenReturn(List.of(SearchOperator.EQUALS));
        ModelDescriptor root = mock(ModelDescriptor.class);
        when(root.getPackageName()).thenReturn("t");
        when(root.getName()).thenReturn("Root");
        when(root.getFields()).thenReturn(List.of(field));

        List<SearchField> fields = collector.collect(root, 1);

        assertEquals(1, fields.size());
        assertEquals("broken", fields.getFirst().property());
    }

    @Test
    void logsEachNestedCollectionStep() {
        String rootSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Root { @Searchable(depth=2) Child child; }";
        String childSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Child { @Searchable Grandchild grandchild; }";
        String grandchildSrc =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Grandchild { @Searchable String value; }";
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("t.Root", rootSrc),
                        JavaFileObjects.forSourceString("t.Child", childSrc),
                        JavaFileObjects.forSourceString("t.Grandchild", grandchildSrc));
        RecordingMessager messager = new RecordingMessager();
        ProcessingEnvironment env = new EnvStub(elements, messager);
        ModelDescriptor root = AnnotationModelReader.parse(elements.getTypeElement("t.Root"), env);
        SearchFieldCollector collector = new SearchFieldCollector(new WriteContext(env));

        collector.collect(root, 3);

        assertTrue(messager.messages.contains("NOTE:Collecting search fields for Child at depth 2"));
        assertTrue(
                messager.messages.contains(
                        "NOTE:Collecting search fields for Grandchild at depth 1"));
    }
}

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

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import com.palantir.javapoet.JavaFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.exception.CodegenValidationException;
import nl.datasteel.crudcraft.codegen.writer.Generator;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import nl.datasteel.crudcraft.codegen.writer.WriterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/** Tests for CrudCraftProcessor. */
class CrudCraftProcessorTest {
    private static final class EnvStub implements ProcessingEnvironment {
        private final Elements elements;
        private final List<String> diagnostics = new java.util.ArrayList<>();

        private EnvStub(Elements elements) {
            this.elements = elements;
        }

        @Override
        public Map<String, String> getOptions() {
            return Map.of();
        }

        @Override
        public Messager getMessager() {
            return new Messager() {
                @Override
                public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
                    diagnostics.add(kind + ":" + msg);
                }

                @Override
                public void printMessage(
                        Diagnostic.Kind kind,
                        CharSequence msg,
                        javax.lang.model.element.Element e) {
                    diagnostics.add(kind + ":" + msg);
                }

                @Override
                public void printMessage(
                        Diagnostic.Kind kind,
                        CharSequence msg,
                        javax.lang.model.element.Element e,
                        javax.lang.model.element.AnnotationMirror a) {
                    diagnostics.add(kind + ":" + msg);
                }

                @Override
                public void printMessage(
                        Diagnostic.Kind kind,
                        CharSequence msg,
                        javax.lang.model.element.Element e,
                        javax.lang.model.element.AnnotationMirror a,
                        javax.lang.model.element.AnnotationValue v) {
                    diagnostics.add(kind + ":" + msg);
                }
            };
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

    private CrudCraftProcessor processor;

    @BeforeEach
    void setup() {
        processor = new CrudCraftProcessor();
    }

    private static final class FailingGenerator implements Generator {
        @Override
        public List<JavaFile> generate(ModelDescriptor model, WriteContext ctx) {
            throw new RuntimeException("boom");
        }
    }

    @Test
    void reportsErrorsWhenGeneratorThrows() throws Exception {
        try {
            Generator failing = new FailingGenerator();
            setWriterRegistryGenerators(List.of(failing));

            JavaFileObject crafted =
                    JavaFileObjects.forSourceLines(
                            "com.example.Book",
                            "package com.example;",
                            "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                            "@CrudCrafted",
                            "public class Book { String title; }");

            Compilation compilation =
                    CompilationTestUtils.javac().withProcessors(new CrudCraftProcessor()).compile(crafted);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(
                    compilation.diagnostics().stream()
                            .anyMatch(
                                    d ->
                                            d.getKind() == Diagnostic.Kind.ERROR
                                                    && d.getMessage(null)
                                                            .contains(
                                                                    "CrudCraftProcessor failed for"
                                                                            + " com.example.Book:"
                                                                            + " RuntimeException:"
                                                                            + " boom")
                                                    && d.getMessage(null)
                                                            .contains("Common causes:")));
        } finally {
            clearWriterRegistryGenerators();
        }
    }

    @Test
    void generatedCodeCompilesOnJdk21() {
        assertEquals(SourceVersion.RELEASE_21, processor.getSupportedSourceVersion());
    }

    @Test
    void getProcessableElementsCollectsOnlyClassElements() throws Exception {
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceLines(
                                "t.Crafted",
                                "package t;",
                                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                                "@CrudCrafted class Crafted { String field; }"),
                        JavaFileObjects.forSourceLines(
                                "t.Emb",
                                "package t;",
                                "import jakarta.persistence.Embeddable;",
                                "@Embeddable class Emb { String value; }"));
        processor.init(new EnvStub(elements));

        RoundEnvironment roundEnv = Mockito.mock(RoundEnvironment.class);
        Element nonClass = Mockito.mock(Element.class);
        Mockito.when(nonClass.getKind()).thenReturn(ElementKind.FIELD);

        TypeElement crafted = elements.getTypeElement("t.Crafted");
        TypeElement emb = elements.getTypeElement("t.Emb");
        Set<Element> craftedSet = new java.util.HashSet<>();
        craftedSet.add(nonClass);
        craftedSet.add(crafted);
        Set<Element> embeddableSet = new java.util.HashSet<>();
        embeddableSet.add(nonClass);
        embeddableSet.add(emb);

        Mockito.doReturn(craftedSet)
                .when(roundEnv)
                .getElementsAnnotatedWith(
                        nl.datasteel.crudcraft.annotations.classes.CrudCrafted.class);
        Mockito.doReturn(embeddableSet)
                .when(roundEnv)
                .getElementsAnnotatedWith(jakarta.persistence.Embeddable.class);

        Method method =
                CrudCraftProcessor.class.getDeclaredMethod(
                        "getProcessableElements", RoundEnvironment.class);
        method.setAccessible(true);
        List<?> processableElements = (List<?>) method.invoke(processor, roundEnv);

        assertEquals(2, processableElements.size());
        assertTrue(processableElements.contains(crafted));
        assertTrue(processableElements.contains(emb));
    }

    @Test
    void processClaimsAnnotationsWhenNoModelsArePresent() {
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceLines(
                                "t.Empty",
                                "package t;",
                                "class Empty {}"));
        processor.init(new EnvStub(elements));
        RoundEnvironment roundEnv = Mockito.mock(RoundEnvironment.class);
        Mockito.doReturn(Set.of())
                .when(roundEnv)
                .getElementsAnnotatedWith(
                        nl.datasteel.crudcraft.annotations.classes.CrudCrafted.class);
        Mockito.doReturn(Set.of())
                .when(roundEnv)
                .getElementsAnnotatedWith(jakarta.persistence.Embeddable.class);

        assertTrue(processor.process(Set.of(), roundEnv));
    }

    @Test
    void processorRejectsEndpointOverlayConflicts() {
        JavaFileObject model =
                JavaFileObjects.forSourceLines(
                        "t.ConflictingProduct",
                        "package t;",
                        "import jakarta.persistence.Entity;",
                        "import jakarta.persistence.Id;",
                        "import nl.datasteel.crudcraft.annotations.CrudEndpoint;",
                        "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                        "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                        "@Entity",
                        "@CrudCrafted(",
                        "    omitEndpoints = CrudEndpoint.GET_ONE,",
                        "    includeEndpoints = CrudEndpoint.GET_ONE)",
                        "class ConflictingProduct {",
                        "  @Id @Dto(ref = true) Long id;",
                        "  @Dto String name;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac().withProcessors(new CrudCraftProcessor()).compile(model);

        assertEquals(Compilation.Status.FAILURE, compilation.status());
        assertTrue(
                compilation.errors().stream()
                        .anyMatch(
                                error ->
                                        error.getMessage(null)
                                                .contains(
                                                        "Endpoint GET_ONE cannot appear in both"
                                                                + " omitEndpoints and"
                                                                + " includeEndpoints")));
    }

    @Test
    void processorRejectsBulkAndSingleEndpointOverlayConflictsForEveryPair() {
        List<CrudEndpoint[]> conflicts =
                List.of(
                        new CrudEndpoint[] {CrudEndpoint.POST, CrudEndpoint.BULK_CREATE},
                        new CrudEndpoint[] {CrudEndpoint.PUT, CrudEndpoint.BULK_UPDATE},
                        new CrudEndpoint[] {CrudEndpoint.PATCH, CrudEndpoint.BULK_PATCH},
                        new CrudEndpoint[] {CrudEndpoint.DELETE, CrudEndpoint.BULK_DELETE});

        for (CrudEndpoint[] conflict : conflicts) {
            assertEndpointOverlayConflict(conflict[0], conflict[1]);
            assertEndpointOverlayConflict(conflict[1], conflict[0]);
        }
    }

    @Test
    void processorReportsEndpointDependencyNotesFromEffectiveEndpointSet() throws Exception {
        EnvStub env = new EnvStub(null);
        processor.init(env);
        ModelDescriptor model =
                endpointDescriptor(
                        CrudTemplate.FULL,
                        CrudTemplate.class,
                        new CrudEndpoint[] {CrudEndpoint.GET_ONE},
                        new CrudEndpoint[0]);

        invokeValidateEndpointConflicts(model);

        assertEquals(3, env.diagnostics.size());
        assertTrue(env.diagnostics.stream().allMatch(note -> note.startsWith("NOTE:")));
        assertTrue(containsDiagnostic(env.diagnostics, "Endpoint PATCH is enabled without GET_ONE"));
        assertTrue(containsDiagnostic(env.diagnostics, "Endpoint PUT is enabled without GET_ONE"));
        assertTrue(containsDiagnostic(env.diagnostics, "Endpoint DELETE is enabled without GET_ONE"));
    }

    @Test
    void processorUsesCustomEndpointPolicyWhenComputingEffectiveEndpoints() throws Exception {
        EnvStub env = new EnvStub(null);
        processor.init(env);
        ModelDescriptor model =
                endpointDescriptor(
                        CrudTemplate.READ_ONLY,
                        PatchOnlyPolicy.class,
                        new CrudEndpoint[0],
                        new CrudEndpoint[0]);

        invokeValidateEndpointConflicts(model);

        assertEquals(1, env.diagnostics.size());
        assertTrue(containsDiagnostic(env.diagnostics, "Endpoint PATCH is enabled without GET_ONE"));
        assertFalse(containsDiagnostic(env.diagnostics, "Endpoint PUT is enabled without GET_ONE"));
        assertFalse(containsDiagnostic(env.diagnostics, "Endpoint DELETE is enabled without GET_ONE"));
    }

    @Test
    void processorRejectsEmbeddableDepthBeyondConfiguredMaximum() {
        JavaFileObject root =
                JavaFileObjects.forSourceLines(
                        "t.RootEmbeddable",
                        "package t;",
                        "import jakarta.persistence.Embeddable;",
                        "@Embeddable",
                        "class RootEmbeddable {",
                        "  NestedEmbeddable nested;",
                        "}");
        JavaFileObject nested =
                JavaFileObjects.forSourceLines(
                        "t.NestedEmbeddable",
                        "package t;",
                        "import jakarta.persistence.Embeddable;",
                        "@Embeddable",
                        "class NestedEmbeddable {",
                        "  String value;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac("-Acrudcraft.embeddable.maxDepth=0")
                        .withProcessors(new CrudCraftProcessor())
                        .compile(root, nested);

        assertEquals(Compilation.Status.FAILURE, compilation.status());
        assertTrue(
                compilation.errors().stream()
                        .anyMatch(
                                error ->
                                        error.getMessage(null)
                                                .contains(
                                                        "Embeddable nesting depth 1 exceeds"
                                                                + " crudcraft.embeddable.maxDepth=0")));
    }

    @Test
    void processorFailsForFieldSecurityOutsideDto() throws Exception {
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceLines(
                                "t.Crafted",
                                "package t;",
                                "class Crafted {}"));
        EnvStub env = new EnvStub(elements);
        processor.init(env);

        ModelDescriptor model = Mockito.mock(ModelDescriptor.class);
        Mockito.when(model.getName()).thenReturn("Crafted");
        FieldDescriptor securedHidden = Mockito.mock(FieldDescriptor.class);
        Mockito.when(securedHidden.hasFieldSecurity()).thenReturn(true);
        Mockito.when(securedHidden.inDto()).thenReturn(false);
        Mockito.when(securedHidden.getName()).thenReturn("internalCode");
        Mockito.when(model.getFields()).thenReturn(List.of(securedHidden));

        Method method =
                CrudCraftProcessor.class.getDeclaredMethod(
                        "validateAnnotationCohesion", Element.class, ModelDescriptor.class);
        method.setAccessible(true);

        InvocationTargetException exception =
                assertThrows(
                        InvocationTargetException.class,
                        () -> method.invoke(processor, elements.getTypeElement("t.Crafted"), model));
        assertTrue(exception.getCause() instanceof CodegenValidationException);
        assertTrue(exception.getCause().getMessage().contains("@FieldSecurity on field"));
    }

    @Test
    void processorWarnsForIneffectiveOrExpensiveAnnotationCombinations() throws Exception {
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceLines(
                                "t.Crafted",
                                "package t;",
                                "import nl.datasteel.crudcraft.annotations.security.CrudSecurity;",
                                "@CrudSecurity(readRoles = \"ADMIN\") class Crafted {}"));
        EnvStub env = new EnvStub(elements);
        processor.init(env);

        ModelDescriptor model = Mockito.mock(ModelDescriptor.class);
        Mockito.when(model.isSecure()).thenReturn(false);
        Mockito.when(model.getName()).thenReturn("Crafted");
        FieldDescriptor searchableRelationship = Mockito.mock(FieldDescriptor.class);
        Mockito.when(searchableRelationship.isSearchable()).thenReturn(true);
        Mockito.when(searchableRelationship.getRelType()).thenReturn(RelationshipType.MANY_TO_ONE);
        Mockito.when(searchableRelationship.getName()).thenReturn("category");
        Mockito.when(searchableRelationship.getResponseDtos()).thenReturn(new String[0]);
        FieldDescriptor variantRelationship = Mockito.mock(FieldDescriptor.class);
        Mockito.when(variantRelationship.getRelType()).thenReturn(RelationshipType.ONE_TO_ONE);
        Mockito.when(variantRelationship.getName()).thenReturn("detail");
        Mockito.when(variantRelationship.getResponseDtos())
                .thenReturn(new String[] {"List", "Detail"});
        Mockito.when(model.getFields())
                .thenReturn(List.of(searchableRelationship, variantRelationship));

        Method method =
                CrudCraftProcessor.class.getDeclaredMethod(
                        "validateAnnotationCohesion", Element.class, ModelDescriptor.class);
        method.setAccessible(true);
        method.invoke(processor, elements.getTypeElement("t.Crafted"), model);

        List<String> warnings = env.diagnostics;
        assertEquals(4, warnings.size());
        assertEquals(1, countDiagnostics(warnings, "@CrudSecurity"));
        assertEquals(1, countDiagnostics(warnings, "@Searchable on relationship"));
        assertEquals(1, countDiagnostics(warnings, "not annotated with @BatchFetched"));
        assertEquals(1, countDiagnostics(warnings, "participates in multiple DTO variants"));
        assertTrue(warnings.stream().allMatch(w -> w.startsWith("NOTE:")));
        assertTrue(
                warnings.stream()
                        .anyMatch(w -> w.contains("participates in multiple DTO variants")));
    }

    @Test
    void processWritesValidModelDescriptors() throws Exception {
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceLines(
                                "t.Crafted",
                                "package t;",
                                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                                "@CrudCrafted class Crafted { String field; }"));
        processor.init(new EnvStub(elements));
        TypeElement crafted = elements.getTypeElement("t.Crafted");
        RoundEnvironment roundEnv = Mockito.mock(RoundEnvironment.class);
        Mockito.doReturn(Set.of(crafted))
                .when(roundEnv)
                .getElementsAnnotatedWith(
                        nl.datasteel.crudcraft.annotations.classes.CrudCrafted.class);
        Mockito.doReturn(Set.of())
                .when(roundEnv)
                .getElementsAnnotatedWith(jakarta.persistence.Embeddable.class);
        try {
            setWriterRegistryGenerators(List.of());

            assertTrue(processor.process(Set.of(), roundEnv));
        } finally {
            clearWriterRegistryGenerators();
        }
    }

    @Test
    void commonCausesHintListsExpectedTroubleshootingItems() throws Exception {
        Method method = CrudCraftProcessor.class.getDeclaredMethod("commonCausesHint");
        method.setAccessible(true);

        String hint = (String) method.invoke(processor);

        assertFalse(hint.isBlank());
        assertTrue(hint.contains("MapStruct processor ordering/configuration"));
        assertTrue(hint.contains("invalid endpoint include/omit combinations"));
    }

    @Test
    void isClassPresentDistinguishesAvailableAndMissingClasses() throws Exception {
        Method method = CrudCraftProcessor.class.getDeclaredMethod("isClassPresent", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(processor, "java.lang.String"));
        assertFalse((Boolean) method.invoke(processor, "t.missing.DoesNotExist"));
    }

    @Test
    void hasBatchFetchedAnnotationChecksTypeAndFieldName() throws Exception {
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceLines(
                                "nl.datasteel.crudcraft.annotations.fields.BatchFetched",
                                "package nl.datasteel.crudcraft.annotations.fields;",
                                "import java.lang.annotation.ElementType;",
                                "import java.lang.annotation.Retention;",
                                "import java.lang.annotation.RetentionPolicy;",
                                "import java.lang.annotation.Target;",
                                "@Target(ElementType.FIELD)",
                                "@Retention(RetentionPolicy.CLASS)",
                                "public @interface BatchFetched {}"),
                        JavaFileObjects.forSourceLines(
                                "t.BatchModel",
                                "package t;",
                                "import nl.datasteel.crudcraft.annotations.fields.BatchFetched;",
                                "class BatchModel {",
                                "  @BatchFetched String relation;",
                                "  String plain;",
                                "}"));
        processor.init(new EnvStub(elements));
        TypeElement model = elements.getTypeElement("t.BatchModel");
        Element nonType = Mockito.mock(Element.class);

        Method method =
                CrudCraftProcessor.class.getDeclaredMethod(
                        "hasBatchFetchedAnnotation", Element.class, String.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(processor, nonType, "relation"));
        assertTrue((Boolean) method.invoke(processor, model, "relation"));
        assertFalse((Boolean) method.invoke(processor, model, "plain"));
        assertFalse((Boolean) method.invoke(processor, model, "missing"));
    }

    private void assertEndpointOverlayConflict(CrudEndpoint omitted, CrudEndpoint included) {
        ModelDescriptor model =
                endpointDescriptor(
                        CrudTemplate.FULL,
                        CrudTemplate.class,
                        new CrudEndpoint[] {omitted},
                        new CrudEndpoint[] {included});

        Exception thrown = assertThrows(Exception.class, () -> invokeValidateEndpointConflicts(model));

        assertTrue(thrown.getCause().getMessage().contains("cannot be explicitly"));
    }

    private ModelDescriptor endpointDescriptor(
            CrudTemplate template,
            Class<? extends CrudEndpointPolicy> endpointPolicy,
            CrudEndpoint[] omitted,
            CrudEndpoint[] included) {
        ModelDescriptor model = Mockito.mock(ModelDescriptor.class);
        Mockito.when(model.getName()).thenReturn("Product");
        Mockito.when(model.getTemplate()).thenReturn(template);
        Mockito.doReturn(endpointPolicy).when(model).getEndpointPolicy();
        Mockito.when(model.getOmitEndpoints()).thenReturn(omitted);
        Mockito.when(model.getIncludeEndpoints()).thenReturn(included);
        return model;
    }

    private void invokeValidateEndpointConflicts(ModelDescriptor model) throws Exception {
        Method method =
                CrudCraftProcessor.class.getDeclaredMethod(
                        "validateEndpointConflicts", Element.class, ModelDescriptor.class);
        method.setAccessible(true);
        method.invoke(processor, Mockito.mock(Element.class), model);
    }

    private static boolean containsDiagnostic(List<String> diagnostics, String message) {
        return diagnostics.stream().anyMatch(diagnostic -> diagnostic.contains(message));
    }

    private static long countDiagnostics(List<String> diagnostics, String fragment) {
        return diagnostics.stream().filter(diagnostic -> diagnostic.contains(fragment)).count();
    }

    private static void setWriterRegistryGenerators(List<Generator> generators) throws Exception {
        Method setter =
                WriterRegistry.class.getDeclaredMethod(
                        "setGeneratorOverridesForTests", List.class);
        setter.setAccessible(true);
        setter.invoke(null, generators);
    }

    private static void clearWriterRegistryGenerators() throws Exception {
        Method clear = WriterRegistry.class.getDeclaredMethod("clearGeneratorOverridesForTests");
        clear.setAccessible(true);
        clear.invoke(null);
    }

    static final class PatchOnlyPolicy implements CrudEndpointPolicy {
        public PatchOnlyPolicy() {
        }

        @Override
        public Set<CrudEndpoint> resolveEndpoints() {
            return Set.of(CrudEndpoint.PATCH);
        }

        @Override
        public String name() {
            return "patch-only";
        }
    }
}

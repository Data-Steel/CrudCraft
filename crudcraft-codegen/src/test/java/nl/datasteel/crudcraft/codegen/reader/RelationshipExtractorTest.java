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

package nl.datasteel.crudcraft.codegen.reader;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Relationship;
import nl.datasteel.crudcraft.codegen.reader.field.RelationshipExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class RelationshipExtractorTest {
    private Elements elements;

    private static final class RecordingMessager implements Messager {
        private final List<String> notes = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            record(kind, msg);
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
            record(kind, msg);
        }

        @Override
        public void printMessage(
                Diagnostic.Kind kind,
                CharSequence msg,
                Element e,
                javax.lang.model.element.AnnotationMirror a) {
            record(kind, msg);
        }

        @Override
        public void printMessage(
                Diagnostic.Kind kind,
                CharSequence msg,
                Element e,
                javax.lang.model.element.AnnotationMirror a,
                javax.lang.model.element.AnnotationValue v) {
            record(kind, msg);
        }

        private void record(Diagnostic.Kind kind, CharSequence msg) {
            if (kind == Diagnostic.Kind.NOTE) {
                notes.add(msg.toString());
            } else if (kind == Diagnostic.Kind.WARNING) {
                warnings.add(msg.toString());
            } else if (kind == Diagnostic.Kind.ERROR) {
                errors.add(msg.toString());
            }
        }
    }

    private static final class RecordingEnv implements ProcessingEnvironment {
        private final Elements elements;
        private final RecordingMessager messager;

        private RecordingEnv(Elements elements, RecordingMessager messager) {
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

    @BeforeEach
    void compile() {
        String src =
                "package t;import jakarta.persistence.*;import"
                    + " nl.datasteel.crudcraft.annotations.classes.CrudCrafted;@CrudCrafted class"
                    + " Other {} @CrudCrafted abstract class AbstractOther {} class C"
                    + " {@OneToMany(mappedBy=\"c\") java.util.List<Other>"
                    + " otm;@ManyToMany(mappedBy=\"c\", targetEntity=Other.class)"
                    + " java.util.List<Other> mtm;@ManyToMany(targetEntity=Other.class)"
                    + " java.util.List<String> mtmTargetOnly;@ManyToMany java.util.List<Other>"
                    + " mtmNoTarget;@OneToOne(mappedBy=\"c\") Other oto;@ManyToOne"
                    + " Other mto;@OneToOne(mappedBy=\"c\") AbstractOther"
                    + " otoAbs;@OneToMany(mappedBy=\"x\") java.util.List<String> nonCrud;@Embedded"
                    + " Other emb;String none;}";
        elements = CompilationTestUtils.elements("t.C", src);
    }

    private VariableElement field(String name) {
        var type = elements.getTypeElement("t.C");
        return (VariableElement)
                type.getEnclosedElements().stream()
                        .filter(e -> e.getSimpleName().contentEquals(name))
                        .findFirst()
                        .orElseThrow();
    }

    @Test
    void extractsDifferentRelationshipTypes() {
        Relationship otm =
                RelationshipExtractor.INSTANCE.extract(
                        field("otm"), new TestUtils.ProcessingEnvStub(elements));
        assertEquals(RelationshipType.ONE_TO_MANY, otm.getRelationshipType());
        Relationship mtm =
                RelationshipExtractor.INSTANCE.extract(
                        field("mtm"), new TestUtils.ProcessingEnvStub(elements));
        assertEquals(RelationshipType.MANY_TO_MANY, mtm.getRelationshipType());
        assertTrue(mtm.isTargetCrud());
        Relationship oto =
                RelationshipExtractor.INSTANCE.extract(
                        field("oto"), new TestUtils.ProcessingEnvStub(elements));
        assertEquals(RelationshipType.ONE_TO_ONE, oto.getRelationshipType());
        Relationship mto =
                RelationshipExtractor.INSTANCE.extract(
                        field("mto"), new TestUtils.ProcessingEnvStub(elements));
        assertEquals(RelationshipType.MANY_TO_ONE, mto.getRelationshipType());
        Relationship none =
                RelationshipExtractor.INSTANCE.extract(
                        field("none"), new TestUtils.ProcessingEnvStub(elements));
        assertEquals(RelationshipType.NONE, none.getRelationshipType());
        Relationship emb =
                RelationshipExtractor.INSTANCE.extract(
                        field("emb"), new TestUtils.ProcessingEnvStub(elements));
        assertTrue(emb.isEmbedded());
        Relationship otoAbs =
                RelationshipExtractor.INSTANCE.extract(
                        field("otoAbs"), new TestUtils.ProcessingEnvStub(elements));
        assertTrue(otoAbs.isTargetAbstract());
        Relationship nonCrud =
                RelationshipExtractor.INSTANCE.extract(
                        field("nonCrud"), new TestUtils.ProcessingEnvStub(elements));
        assertFalse(nonCrud.isTargetCrud());
    }

    @Test
    void extractsExplicitManyToManyTargetEntityWhenGenericDiffers() {
        Relationship mtm =
                RelationshipExtractor.INSTANCE.extract(
                        field("mtmTargetOnly"), new TestUtils.ProcessingEnvStub(elements));

        assertEquals(RelationshipType.MANY_TO_MANY, mtm.getRelationshipType());
        assertEquals("t.Other", mtm.getTargetType());
        assertTrue(mtm.isTargetCrud());
    }

    @Test
    void manyToManyWithoutTargetEntityFallsBackToGenericType() {
        Relationship mtm =
                RelationshipExtractor.INSTANCE.extract(
                        field("mtmNoTarget"), new TestUtils.ProcessingEnvStub(elements));

        assertEquals(RelationshipType.MANY_TO_MANY, mtm.getRelationshipType());
        assertEquals("t.Other", mtm.getTargetType());
        assertTrue(mtm.isTargetCrud());
    }

    @Test
    void logsDetectedRelationshipBranchesAndDefaultBranch() {
        RecordingMessager messager = new RecordingMessager();
        RecordingEnv env = new RecordingEnv(elements, messager);

        RelationshipExtractor.INSTANCE.extract(field("otm"), env);
        RelationshipExtractor.INSTANCE.extract(field("mtm"), env);
        RelationshipExtractor.INSTANCE.extract(field("oto"), env);
        RelationshipExtractor.INSTANCE.extract(field("mto"), env);
        RelationshipExtractor.INSTANCE.extract(field("none"), env);

        assertTrue(messager.notes.stream().anyMatch(msg -> msg.contains("Detected @OneToMany")));
        assertTrue(messager.notes.stream().anyMatch(msg -> msg.contains("Detected @ManyToMany")));
        assertTrue(messager.notes.stream().anyMatch(msg -> msg.contains("Detected @OneToOne")));
        assertTrue(messager.notes.stream().anyMatch(msg -> msg.contains("Detected @ManyToOne")));
        assertTrue(
                messager.notes.stream()
                        .anyMatch(msg -> msg.contains("No relationship annotations found")));
    }

    @Test
    void handlesNullFieldTypeGracefully() {
        RecordingMessager messager = new RecordingMessager();
        VariableElement ve =
                (VariableElement)
                        Proxy.newProxyInstance(
                                getClass().getClassLoader(),
                                new Class[] {VariableElement.class},
                                (p, m, a) ->
                                        switch (m.getName()) {
                                            case "asType" -> null;
                                            case "getAnnotation" -> null;
                                            case "getAnnotationMirrors" -> List.of();
                                            case "getKind" -> ElementKind.FIELD;
                                            default -> null;
                                        });
        Relationship rel =
                RelationshipExtractor.INSTANCE.extract(
                        ve, new RecordingEnv(elements, messager));
        assertEquals(RelationshipType.NONE, rel.getRelationshipType());
        assertEquals("java.lang.Object", rel.getTargetType());
        assertTrue(messager.errors.stream().anyMatch(msg -> msg.contains("Has null type")));
    }

    @Test
    void extractionExceptionReturnsSafeRelationshipAndLogsError() {
        RecordingMessager messager = new RecordingMessager();
        TypeMirror type = field("none").asType();
        VariableElement ve =
                (VariableElement)
                        Proxy.newProxyInstance(
                                getClass().getClassLoader(),
                                new Class[] {VariableElement.class},
                                (p, m, a) ->
                                        switch (m.getName()) {
                                            case "asType" -> type;
                                            case "getAnnotation" -> {
                                                if (a != null
                                                        && a.length == 1
                                                        && a[0] == jakarta.persistence.Embedded.class) {
                                                    yield null;
                                                }
                                                throw new IllegalStateException("boom");
                                            }
                                            case "getKind" -> ElementKind.FIELD;
                                            default -> null;
                                        });

        Relationship rel =
                RelationshipExtractor.INSTANCE.extract(
                        ve, new RecordingEnv(elements, messager));

        assertEquals(RelationshipType.NONE, rel.getRelationshipType());
        assertEquals("java.lang.Object", rel.getTargetType());
        assertTrue(
                messager.errors.stream()
                        .anyMatch(msg -> msg.contains("Error extracting relationship")));
    }

    @Test
    void extractTargetFallsBackAndLogsWarningWhenAnnotationMirrorsFail() throws Exception {
        RecordingMessager messager = new RecordingMessager();
        VariableElement ve =
                (VariableElement)
                        Proxy.newProxyInstance(
                                getClass().getClassLoader(),
                                new Class[] {VariableElement.class},
                                (p, m, a) ->
                                        switch (m.getName()) {
                                            case "getAnnotationMirrors" ->
                                                    throw new IllegalStateException("mirror boom");
                                            default -> null;
                                        });
        Method extractTarget =
                RelationshipExtractor.class.getDeclaredMethod(
                        "extractTarget", VariableElement.class, TypeMirror.class, Messager.class);
        extractTarget.setAccessible(true);

        Object target = extractTarget.invoke(null, ve, field("mtm").asType(), messager);

        assertEquals("t.Other", target);
        assertTrue(
                messager.warnings.stream()
                        .anyMatch(msg -> msg.contains("Failed to extract targetEntity")));
    }

    @Test
    void manyToManyAnnotationPredicateDistinguishesAnnotationTypes() throws Exception {
        Method predicate =
                RelationshipExtractor.class.getDeclaredMethod(
                        "isManyToManyAnnotation", AnnotationMirror.class);
        predicate.setAccessible(true);
        String src =
                "package t; import jakarta.persistence.*; class C {"
                        + " @Deprecated String deprecated;"
                        + " @ManyToMany java.util.List<String> mtm;"
                        + "}";
        Elements localElements = CompilationTestUtils.elements("t.C", src);
        var type = localElements.getTypeElement("t.C");
        AnnotationMirror deprecated =
                ((VariableElement)
                                type.getEnclosedElements().stream()
                                        .filter(e -> e.getSimpleName().contentEquals("deprecated"))
                                        .findFirst()
                                        .orElseThrow())
                        .getAnnotationMirrors()
                        .getFirst();
        AnnotationMirror manyToMany =
                ((VariableElement)
                                type.getEnclosedElements().stream()
                                        .filter(e -> e.getSimpleName().contentEquals("mtm"))
                                        .findFirst()
                                        .orElseThrow())
                        .getAnnotationMirrors()
                        .getFirst();

        assertFalse((boolean) predicate.invoke(null, deprecated));
        assertTrue((boolean) predicate.invoke(null, manyToMany));
    }
}

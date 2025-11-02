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
package nl.datasteel.crudcraft.codegen.util;

import com.google.testing.compile.Compilation;
import static com.google.testing.compile.Compilation.Status.SUCCESS;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.tools.Diagnostic;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TypeUtilsTest {

    @Test
    void typeUtilityMethodsHandleVariousScenarios() {
        TypeProcessor processor = new TypeProcessor();
        Compilation compilation = Compiler.javac()
                .withProcessors(processor)
                .compile(JavaFileObjects.forSourceLines("test.Sample",
                        "package test;", 
                        "import jakarta.persistence.ManyToMany;", 
                        "import java.util.List;", 
                        "import java.util.Set;", 
                        "class MyEntity {}", 
                        "enum E {A}", 
                        "class Sample {", 
                        "  @ManyToMany(targetEntity = MyEntity.class) Set<MyEntity> rel;", 
                        "  @ManyToMany Set<E> defaults;", 
                        "  List<String> strings;", 
                        "  String str;", 
                        "  int primitiveField;", 
                        "  E enumField;", 
                        "}"));
        assertEquals(SUCCESS, compilation.status());
        assertEquals("test.MyEntity", TypeProcessor.targetEntity);
        assertNull(TypeProcessor.defaultTargetEntity);
        assertEquals("java.lang.String", TypeProcessor.unwrappedGeneric);
        assertEquals("java.lang.String", TypeProcessor.unwrappedNonGeneric);
        assertEquals("Broken", TypeProcessor.unwrappedBroken);
        assertFalse(TypeProcessor.enumPrimitive);
        assertFalse(TypeProcessor.enumString);
        assertTrue(TypeProcessor.enumEnum);
        assertTrue(compilation.diagnostics().stream()
                .anyMatch(d -> d.getKind() == Diagnostic.Kind.WARNING
                        && d.getMessage(null).contains("Failed to unwrap generic from type: Broken")));
    }

    static class TypeProcessor extends AbstractProcessor {
        static String targetEntity;
        static String defaultTargetEntity;
        static String unwrappedGeneric;
        static String unwrappedNonGeneric;
        static String unwrappedBroken;
        static boolean enumPrimitive;
        static boolean enumString;
        static boolean enumEnum;

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            try {
                TypeElement sample = processingEnv.getElementUtils().getTypeElement("test.Sample");
                Element rel = sample.getEnclosedElements().stream()
                        .filter(e -> e.getSimpleName().contentEquals("rel")).findFirst().orElseThrow();
                Element defaults = sample.getEnclosedElements().stream()
                        .filter(e -> e.getSimpleName().contentEquals("defaults")).findFirst().orElseThrow();
                AnnotationMirror relAnn = rel.getAnnotationMirrors().getFirst();
                AnnotationMirror defAnn = defaults.getAnnotationMirrors().getFirst();
                targetEntity = TypeUtils.extractTargetEntityValue(relAnn);
                defaultTargetEntity = TypeUtils.extractTargetEntityValue(defAnn);
                Element strings = sample.getEnclosedElements().stream()
                        .filter(e -> e.getSimpleName().contentEquals("strings")).findFirst().orElseThrow();
                Element str = sample.getEnclosedElements().stream()
                        .filter(e -> e.getSimpleName().contentEquals("str")).findFirst().orElseThrow();
                unwrappedGeneric = TypeUtils.unwrapGeneric(strings.asType(), processingEnv.getMessager());
                unwrappedNonGeneric = TypeUtils.unwrapGeneric(str.asType(), processingEnv.getMessager());
                unwrappedBroken = TypeUtils.unwrapGeneric(new BrokenDeclaredType(), processingEnv.getMessager());
                Element prim = sample.getEnclosedElements().stream()
                        .filter(e -> e.getSimpleName().contentEquals("primitiveField")).findFirst().orElseThrow();
                Element enumField = sample.getEnclosedElements().stream()
                        .filter(e -> e.getSimpleName().contentEquals("enumField")).findFirst().orElseThrow();
                enumPrimitive = TypeUtils.isEnum(prim.asType());
                enumString = TypeUtils.isEnum(str.asType());
                enumEnum = TypeUtils.isEnum(enumField.asType());
            } catch (Exception e) {
                throw new AssertionError(e);
            }
            return false;
        }

        @Override public Set<String> getSupportedAnnotationTypes() { return Set.of("*"); }
        @Override public SourceVersion getSupportedSourceVersion() { return SourceVersion.latestSupported(); }
    }

    static class BrokenDeclaredType implements DeclaredType {
        @Override public TypeMirror getEnclosingType() { return null; }
        @Override public List<? extends TypeMirror> getTypeArguments() { throw new RuntimeException("fail"); }
        @Override public Element asElement() { return null; }
        @Override public TypeKind getKind() { return TypeKind.DECLARED; }
        @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) { return v.visitDeclared(this, p); }
        @Override public List<? extends AnnotationMirror> getAnnotationMirrors() { return List.of(); }
        @Override public <A extends Annotation> A getAnnotation(Class<A> annotationType) { return null; }
        @Override public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) { return null; }
        @Override public String toString() { return "Broken"; }
    }
}

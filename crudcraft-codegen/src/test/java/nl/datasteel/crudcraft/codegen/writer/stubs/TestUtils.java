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
package nl.datasteel.crudcraft.codegen.writer.stubs;

import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;

/** Test utility classes for processing environment stubs. */
class TestUtils {

    /** Messager that records notes and errors for verification. */
    static class RecordingMessager implements Messager {
        final List<String> notes = new ArrayList<>();
        final List<String> errors = new ArrayList<>();
        @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            switch (kind) {
                case ERROR -> errors.add(msg.toString());
                default -> notes.add(msg.toString());
            }
        }
        @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) { printMessage(kind, msg); }
        @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) { printMessage(kind, msg); }
        @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) { printMessage(kind, msg); }
    }

    /** JavaFileObject capturing written content. */
    static class RecordingJavaFileObject extends SimpleJavaFileObject {
        final boolean failOnWrite;
        String written = "";
        RecordingJavaFileObject(boolean failOnWrite) {
            super(URI.create("string:///Generated.java"), JavaFileObject.Kind.SOURCE);
            this.failOnWrite = failOnWrite;
        }
        @Override public Writer openWriter() throws IOException {
            if (failOnWrite) {
                throw new IOException("write fail");
            }
            StringWriter sw = new StringWriter() {
                @Override public void close() throws IOException {
                    written = toString();
                    super.close();
                }
            };
            return sw;
        }
    }

    /** Filer that records the last created file and optionally fails. */
    static class RecordingFiler implements Filer {
        final boolean failOnCreate;
        final RecordingJavaFileObject jfo;
        RecordingFiler(boolean failOnCreate, boolean failOnWrite) {
            this.failOnCreate = failOnCreate;
            this.jfo = new RecordingJavaFileObject(failOnWrite);
        }
        @Override public JavaFileObject createSourceFile(CharSequence name, Element... originatingElements) throws IOException {
            if (failOnCreate) {
                throw new FilerException("exists");
            }
            jfo.written = ""; // reset
            return jfo;
        }
        @Override public JavaFileObject createClassFile(CharSequence name, Element... originatingElements) {
            throw new UnsupportedOperationException();
        }
        @Override public FileObject createResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName, Element... originatingElements) {
            throw new UnsupportedOperationException();
        }
        @Override public FileObject getResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName) {
            throw new UnsupportedOperationException();
        }
    }

    /** Minimal Elements implementation. */
    static class ElementsStub implements Elements {
        /** Simple {@link Name} implementation backed by a {@link String}. */
        static class StringName implements Name {
            final String value;
            StringName(String value) { this.value = value; }
            @Override public boolean contentEquals(CharSequence cs) { return value.contentEquals(cs); }
            @Override public int length() { return value.length(); }
            @Override public char charAt(int index) { return value.charAt(index); }
            @Override public CharSequence subSequence(int start, int end) { return value.subSequence(start, end); }
            @Override public String toString() { return value; }
        }

        @Override public PackageElement getPackageElement(CharSequence name) { return null; }
        @Override public TypeElement getTypeElement(CharSequence name) { return null; }
        @Override public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror a) { return Map.of(); }
        @Override public String getDocComment(Element e) { return null; }
        @Override public boolean isDeprecated(Element e) { return false; }
        @Override public Name getBinaryName(TypeElement type) { return new StringName(""); }
        @Override public PackageElement getPackageOf(Element type) { return null; }
        @Override public List<? extends Element> getAllMembers(TypeElement type) { return List.of(); }
        @Override public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) { return List.of(); }
        @Override public boolean hides(Element hider, Element hidden) { return false; }
        @Override public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type) { return false; }
        @Override public String getConstantExpression(Object value) { return null; }
        @Override public void printElements(Writer w, Element... elements) {}
        @Override public Name getName(CharSequence cs) { return new StringName(cs.toString()); }
        @Override public boolean isFunctionalInterface(TypeElement type) { return false; }
    }

    /** Minimal Types implementation. */
    static class TypesStub implements Types {
        @Override public Element asElement(TypeMirror t) { return null; }
        @Override public boolean isSameType(TypeMirror t1, TypeMirror t2) { return false; }
        @Override public boolean isSubtype(TypeMirror t1, TypeMirror t2) { return false; }
        @Override public boolean isAssignable(TypeMirror t1, TypeMirror t2) { return false; }
        @Override public boolean contains(TypeMirror t1, TypeMirror t2) { return false; }
        @Override public boolean isSubsignature(ExecutableType m1, ExecutableType m2) { return false; }
        @Override public List<? extends TypeMirror> directSupertypes(TypeMirror t) { return List.of(); }
        @Override public TypeMirror erasure(TypeMirror t) { return t; }
        @Override public TypeElement boxedClass(PrimitiveType p) { return null; }
        @Override public PrimitiveType unboxedType(TypeMirror t) { return null; }
        @Override public TypeMirror capture(TypeMirror t) { return t; }
        @Override public PrimitiveType getPrimitiveType(TypeKind kind) { return null; }
        @Override public NullType getNullType() { return null; }
        @Override public NoType getNoType(TypeKind kind) { return null; }
        @Override public ArrayType getArrayType(TypeMirror componentType) { return null; }
        @Override public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) { return null; }
        @Override public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) { return null; }
        @Override public DeclaredType getDeclaredType(DeclaredType containing, TypeElement typeElem, TypeMirror... typeArgs) { return null; }
        @Override public TypeMirror asMemberOf(DeclaredType containing, Element element) { return null; }
    }

    /** Processing environment stub using recording filer and messager. */
    static class ProcessingEnvStub implements ProcessingEnvironment {
        final RecordingMessager messager;
        final Filer filer;
        ProcessingEnvStub(Filer filer) {
            this.filer = filer;
            this.messager = new RecordingMessager();
        }
        @Override public Map<String, String> getOptions() { return Map.of(); }
        @Override public Messager getMessager() { return messager; }
        @Override public Filer getFiler() { return filer; }
        @Override public Elements getElementUtils() { return new ElementsStub(); }
        @Override public Types getTypeUtils() { return new TypesStub(); }
        @Override public SourceVersion getSourceVersion() { return SourceVersion.latest(); }
        @Override public Locale getLocale() { return Locale.getDefault(); }
    }

    /** WriteContext that records generated files instead of writing. */
    static class RecordingWriteContext extends WriteContext {
        final List<JavaFile> files = new ArrayList<>();
        RecordingWriteContext(ProcessingEnvironment env) { super(env); }
        @Override public void write(JavaFile jf) { files.add(jf); }
    }
}

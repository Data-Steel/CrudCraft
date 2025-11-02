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
package nl.datasteel.crudcraft.codegen.util;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class FilerUtilsTest {

    @Test
    void writeJavaFileHandlesIOException() {
        CollectingMessager messager = new CollectingMessager();
        JavaFile file = JavaFile.builder("p", TypeSpec.classBuilder("Err").build()).build();
        FilerUtils.writeJavaFile(file, new FailingFiler(), messager);
        assertTrue(messager.messages.get(Diagnostic.Kind.ERROR).stream()
                .anyMatch(m -> m.contains("Failed to write p.Err")));
    }

    static class CollectingMessager implements Messager {
        final Map<Diagnostic.Kind, List<String>> messages = new EnumMap<>(Diagnostic.Kind.class);
        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            messages.computeIfAbsent(kind, k -> new ArrayList<>()).add(msg.toString());
        }
        @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) { printMessage(kind, msg); }
        @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) { printMessage(kind, msg); }
        @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) { printMessage(kind, msg); }
    }

    static class FailingFiler implements Filer {
        @Override
        public JavaFileObject createSourceFile(CharSequence name, Element... originatingElements) throws IOException {
            return new FailingJavaFileObject();
        }
        @Override public JavaFileObject createClassFile(CharSequence name, Element... originatingElements) { throw new UnsupportedOperationException(); }
        @Override public FileObject createResource(javax.tools.JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName, Element... originatingElements) { throw new UnsupportedOperationException(); }
        @Override public FileObject getResource(javax.tools.JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName) { throw new UnsupportedOperationException(); }
    }

    static class FailingJavaFileObject extends SimpleJavaFileObject {
        FailingJavaFileObject() { super(URI.create("string:///Fail.java"), JavaFileObject.Kind.SOURCE); }
        @Override public Writer openWriter() throws IOException { throw new IOException("boom"); }
    }
}

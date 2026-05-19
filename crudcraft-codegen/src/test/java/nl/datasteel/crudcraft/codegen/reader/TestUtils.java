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

import java.util.ArrayList;
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


/** Utility test classes for stub processing environment. */
class TestUtils {
    record Message(Diagnostic.Kind kind, String text, Element element) {}

    static class MessagerStub implements Messager {
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

    static final class RecordingMessager implements Messager {
        private final List<Message> messages = new ArrayList<>();

        List<Message> messages() {
            return List.copyOf(messages);
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            messages.add(new Message(kind, msg.toString(), null));
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
            messages.add(new Message(kind, msg.toString(), e));
        }

        @Override
        public void printMessage(
                Diagnostic.Kind kind,
                CharSequence msg,
                Element e,
                javax.lang.model.element.AnnotationMirror a) {
            messages.add(new Message(kind, msg.toString(), e));
        }

        @Override
        public void printMessage(
                Diagnostic.Kind kind,
                CharSequence msg,
                Element e,
                javax.lang.model.element.AnnotationMirror a,
                javax.lang.model.element.AnnotationValue v) {
            messages.add(new Message(kind, msg.toString(), e));
        }
    }

    static class ProcessingEnvStub implements ProcessingEnvironment {
        private final Elements elements;
        private final Messager messager;

        ProcessingEnvStub(Elements elements) {
            this(elements, new MessagerStub());
        }

        ProcessingEnvStub(Elements elements, Messager messager) {
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
}

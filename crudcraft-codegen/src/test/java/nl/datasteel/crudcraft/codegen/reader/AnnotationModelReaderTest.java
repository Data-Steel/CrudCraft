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

import java.lang.reflect.Constructor;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AnnotationModelReaderTest {

    @Test
    void privateConstructorIsCoveredForUtilityClass() throws Exception {
        Constructor<AnnotationModelReader> constructor =
                AnnotationModelReader.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance());
    }

    @Test
    void parsesModelUsingRegistry() {
        String src =
                "package t; import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "@CrudCrafted class M { String f; }";
        Elements elements = CompilationTestUtils.elements("t.M", src);
        TypeElement te = elements.getTypeElement("t.M");
        TestUtils.RecordingMessager messager = new TestUtils.RecordingMessager();
        ModelDescriptor md =
                AnnotationModelReader.parse(te, new TestUtils.ProcessingEnvStub(elements, messager));
        assertEquals("M", md.getName());
        assertTrue(md.isCrudCraftEntity());
        assertEquals(1, md.getFields().size());
        assertTrue(
                messager.messages().stream()
                        .anyMatch(
                                message ->
                                        message.kind() == Diagnostic.Kind.NOTE
                                                && message.text()
                                                        .contains("Parsing model: t.M")));
    }

    @Test
    void defaultsWhenCrudCraftAnnotationMissing() {
        String src = "package t; class M { String f; }";
        Elements elements = CompilationTestUtils.elements("t.M", src);
        TypeElement te = elements.getTypeElement("t.M");
        ModelDescriptor md =
                AnnotationModelReader.parse(te, new TestUtils.ProcessingEnvStub(elements));
        assertFalse(md.isCrudCraftEntity());
        assertEquals(1, md.getFields().size());
    }

    @Test
    void parseRequiresTypeElement() {
        String src = "package t; class M { String f; }";
        Elements elements = CompilationTestUtils.elements("t.M", src);
        VariableElement field =
                (VariableElement)
                        elements.getTypeElement("t.M").getEnclosedElements().stream()
                                .filter(e -> e.getKind() == ElementKind.FIELD)
                                .findFirst()
                                .orElseThrow();

        assertThrows(
                ClassCastException.class,
                () ->
                        AnnotationModelReader.parse(
                                field, new TestUtils.ProcessingEnvStub(elements)));
    }

    @Test
    void parseNullElementThrows() {
        Elements elements = CompilationTestUtils.elements("t.M", "package t; class M {}");
        assertThrows(
                NullPointerException.class,
                () -> AnnotationModelReader.parse(null, new TestUtils.ProcessingEnvStub(elements)));
    }
}

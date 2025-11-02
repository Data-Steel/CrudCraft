/*
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
package nl.datasteel.crudcraft.codegen.reader;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AnnotationModelReaderTest {

    @Test
    void parsesModelUsingRegistry() {
        String src = "package t; import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;" +
                "@CrudCrafted class M { String f; }";
        Elements elements = CompilationTestUtils.elements("t.M", src);
        TypeElement te = elements.getTypeElement("t.M");
        ModelDescriptor md = AnnotationModelReader.parse(te, new TestUtils.ProcessingEnvStub(elements));
        assertEquals("M", md.getName());
        assertTrue(md.isCrudCraftEntity());
        assertEquals(1, md.getFields().size());
    }

    @Test
    void defaultsWhenCrudCraftAnnotationMissing() {
        String src = "package t; class M { String f; }";
        Elements elements = CompilationTestUtils.elements("t.M", src);
        TypeElement te = elements.getTypeElement("t.M");
        ModelDescriptor md = AnnotationModelReader.parse(te, new TestUtils.ProcessingEnvStub(elements));
        assertFalse(md.isCrudCraftEntity());
        assertEquals(1, md.getFields().size());
    }

    @Test
    void parseRequiresTypeElement() {
        String src = "package t; class M { String f; }";
        Elements elements = CompilationTestUtils.elements("t.M", src);
        VariableElement field = (VariableElement) elements.getTypeElement("t.M")
                .getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .findFirst()
                .orElseThrow();

        assertThrows(ClassCastException.class,
                () -> AnnotationModelReader.parse(field, new TestUtils.ProcessingEnvStub(elements)));
    }

    @Test
    void parseNullElementThrows() {
        Elements elements = CompilationTestUtils.elements("t.M", "package t; class M {}");
        assertThrows(NullPointerException.class,
                () -> AnnotationModelReader.parse(null, new TestUtils.ProcessingEnvStub(elements)));
    }
}

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
package nl.datasteel.crudcraft.codegen.reader;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;
import nl.datasteel.crudcraft.codegen.reader.field.DtoOptionsExtractor;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DtoOptionsExtractorTest {

    private Elements elements;
    private VariableElement field(String name) {
        String src = "package t; import nl.datasteel.crudcraft.annotations.fields.*;" +
                "class C { @Dto(value=\"X\", ref=true) @Request String " + name + "; String other; }";
        elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        return (VariableElement) type.getEnclosedElements().stream()
                .filter(e -> e.getSimpleName().contentEquals(name)).findFirst().orElseThrow();
    }

    @Test
    void extractsValuesFromAnnotations() {
        VariableElement ve = field("f");
        DtoOptions opts = DtoOptionsExtractor.INSTANCE.extract(ve,
                new TestUtils.ProcessingEnvStub(elements));
        assertTrue(opts.isInDto());
        assertTrue(opts.isInRequest());
        assertTrue(opts.isInRef());
        assertArrayEquals(new String[]{"X"}, opts.getResponseDtos());
    }

    @Test
    void defaultsWhenAnnotationsMissing() {
        String src = "package t; class C { String f; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement ve = (VariableElement) type.getEnclosedElements().stream()
                .filter(e -> e.getSimpleName().contentEquals("f"))
                .findFirst().orElseThrow();
        DtoOptions opts = DtoOptionsExtractor.INSTANCE.extract(ve, new TestUtils.ProcessingEnvStub(elements));
        assertFalse(opts.isInDto());
        assertFalse(opts.isInRequest());
        assertFalse(opts.isInRef());
        assertArrayEquals(new String[0], opts.getResponseDtos());
    }
}

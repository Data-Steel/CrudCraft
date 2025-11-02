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

import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.EnumOptions;
import nl.datasteel.crudcraft.codegen.reader.field.EnumOptionsExtractor;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EnumOptionsExtractorTest {

    @Test
    void extractsEnumValues() {
        String src = "package t; import nl.datasteel.crudcraft.annotations.fields.EnumString;" +
                "class C { @EnumString(values=\"A\") String f; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement ve = ElementFilter.fieldsIn(type.getEnclosedElements()).getFirst();
        EnumOptions opts = EnumOptionsExtractor.INSTANCE.extract(ve, new TestUtils.ProcessingEnvStub(elements));
        assertTrue(opts.isEnum());
        assertEquals(List.of("A"), opts.getValues());
    }

    @Test
    void defaultsWhenAbsent() {
        String src = "package t; class C { String f; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement ve = ElementFilter.fieldsIn(type.getEnclosedElements()).getFirst();
        EnumOptions opts = EnumOptionsExtractor.INSTANCE.extract(ve, new TestUtils.ProcessingEnvStub(elements));
        assertFalse(opts.isEnum());
        assertTrue(opts.getValues().isEmpty());
    }
}

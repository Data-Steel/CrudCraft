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

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Security;
import nl.datasteel.crudcraft.codegen.reader.field.SecurityExtractor;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SecurityExtractorTest {

    private static VariableElement firstField(TypeElement type) {
        return (VariableElement) type.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .map(VariableElement.class::cast)
                .findFirst().orElseThrow();
    }

    @Test
    void readsFieldSecurityAnnotation() {
        String src = "package t; import nl.datasteel.crudcraft.annotations.security.FieldSecurity;" +
                "class C { @FieldSecurity(readRoles=\"R\", writeRoles=\"W\") String f; String g; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement f = firstField(type);
        Security sec = SecurityExtractor.INSTANCE.extract(f, new TestUtils.ProcessingEnvStub(elements));
        assertTrue(sec.hasFieldSecurity());
        assertArrayEquals(new String[]{"R"}, sec.getReadRoles());
        assertArrayEquals(new String[]{"W"}, sec.getWriteRoles());
        VariableElement g = (VariableElement) type.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .map(VariableElement.class::cast)
                .skip(1).findFirst().orElseThrow();
        Security none = SecurityExtractor.INSTANCE.extract(g, new TestUtils.ProcessingEnvStub(elements));
        assertFalse(none.hasFieldSecurity());
    }
}

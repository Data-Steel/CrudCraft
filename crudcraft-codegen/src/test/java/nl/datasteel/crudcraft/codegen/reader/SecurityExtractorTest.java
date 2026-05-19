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

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Security;
import nl.datasteel.crudcraft.codegen.reader.field.SecurityExtractor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SecurityExtractorTest {

    @Test
    void constructorCreatesExtractorInstance() {
        assertNotNull(new SecurityExtractor());
    }

    private static VariableElement firstField(TypeElement type) {
        return (VariableElement)
                type.getEnclosedElements().stream()
                        .filter(e -> e.getKind() == ElementKind.FIELD)
                        .map(VariableElement.class::cast)
                        .findFirst()
                        .orElseThrow();
    }

    @Test
    void readsFieldSecurityAnnotation() {
        String src =
                "package t; import nl.datasteel.crudcraft.annotations.security.FieldSecurity;class"
                    + " C { @FieldSecurity(readRoles=\"R\", writeRoles=\"W\") String f; String g;"
                    + " }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement f = firstField(type);
        TestUtils.RecordingMessager messager = new TestUtils.RecordingMessager();
        Security sec =
                SecurityExtractor.INSTANCE.extract(
                        f, new TestUtils.ProcessingEnvStub(elements, messager));
        assertTrue(sec.hasFieldSecurity());
        assertArrayEquals(new String[] {"R"}, sec.getReadRoles());
        assertArrayEquals(new String[] {"W"}, sec.getWriteRoles());
        assertEquals(WritePolicy.SKIP_ON_DENIED, sec.getWritePolicy());
        assertEquals(1, messager.messages().size());
        TestUtils.Message message = messager.messages().get(0);
        assertEquals(Diagnostic.Kind.NOTE, message.kind());
        assertTrue(message.text().contains("defined=true"));
        assertTrue(message.text().contains("read=[R]"));
        assertTrue(message.text().contains("write=[W]"));
        assertTrue(message.text().contains("Field: f"));
        VariableElement g =
                (VariableElement)
                        type.getEnclosedElements().stream()
                                .filter(e -> e.getKind() == ElementKind.FIELD)
                                .map(VariableElement.class::cast)
                                .skip(1)
                                .findFirst()
                                .orElseThrow();
        Security none =
                SecurityExtractor.INSTANCE.extract(g, new TestUtils.ProcessingEnvStub(elements));
        assertFalse(none.hasFieldSecurity());
        assertEquals(WritePolicy.SKIP_ON_DENIED, none.getWritePolicy());
    }

    @Test
    void readsExplicitWritePolicy() {
        String src =
                "package t; import nl.datasteel.crudcraft.annotations.security.*;class C {"
                        + " @FieldSecurity(writePolicy = WritePolicy.FAIL_ON_DENIED) String f; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement f = firstField(type);
        Security sec =
                SecurityExtractor.INSTANCE.extract(f, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(WritePolicy.FAIL_ON_DENIED, sec.getWritePolicy());
    }
}

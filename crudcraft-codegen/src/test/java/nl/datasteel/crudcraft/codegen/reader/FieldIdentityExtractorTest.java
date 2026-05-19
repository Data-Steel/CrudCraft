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

import java.lang.reflect.Method;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SchemaMetadata;
import nl.datasteel.crudcraft.codegen.reader.field.IdentityExtractor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class FieldIdentityExtractorTest {

    private static VariableElement firstField(TypeElement type) {
        return (VariableElement)
                type.getEnclosedElements().stream()
                        .filter(e -> e.getKind() == ElementKind.FIELD)
                        .map(VariableElement.class::cast)
                        .findFirst()
                        .orElseThrow();
    }

    @Test
    void constructorCreatesExtractorInstance() {
        assertNotNull(new IdentityExtractor());
    }

    @Test
    void readsNameAndType() {
        String src = "package t; class C { String f; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement f = firstField(type);
        TestUtils.RecordingMessager messager = new TestUtils.RecordingMessager();
        Identity id =
                IdentityExtractor.INSTANCE.extract(
                        f, new TestUtils.ProcessingEnvStub(elements, messager));
        assertEquals("f", id.getName());
        assertEquals(f.asType(), id.getType());
        assertNotNull(id.getSchemaMetadata());
        assertTrue(id.getSchemaMetadata().isEmpty());
        assertEquals(1, messager.messages().size());
        TestUtils.Message message = messager.messages().get(0);
        assertEquals(Diagnostic.Kind.NOTE, message.kind());
        assertTrue(message.text().contains("Extracting Identity field part"));
        assertTrue(message.text().contains("Field: f"));
    }

    @Test
    void readsSchemaMetadataFields() {
        String src =
                "package t;import io.swagger.v3.oas.annotations.media.Schema;class C { "
                    + " @Schema(description=\"desc\", example=\"42\", format=\"uuid\","
                    + " defaultValue=\"x\", pattern=\"[a-z]+\", minimum=\"1\", maximum=\"10\","
                    + " minLength=1, maxLength=10, deprecated=true, hidden=false, nullable=true,"
                    + " accessMode=Schema.AccessMode.READ_ONLY, allowableValues={\"A\",\"B\"},"
                    + " requiredMode=Schema.RequiredMode.REQUIRED)  String f;}";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement f = firstField(type);

        Identity id =
                IdentityExtractor.INSTANCE.extract(f, new TestUtils.ProcessingEnvStub(elements));
        assertEquals("desc", id.getSchemaMetadata().description());
        assertEquals("42", id.getSchemaMetadata().example());
        assertFalse(id.getSchemaMetadata().additionalProperties().isEmpty());
        assertTrue(id.getSchemaMetadata().additionalProperties().containsKey("format"));
        assertTrue(id.getSchemaMetadata().additionalProperties().containsKey("requiredMode"));
    }

    @Test
    void schemaMetadataExtractorReturnsEmptyMetadataWhenSchemaIsAbsent() throws Exception {
        String src = "package t; class C { @Deprecated String f; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement f = firstField(type);
        Method method =
                IdentityExtractor.class.getDeclaredMethod(
                        "extractSchemaMetadata", VariableElement.class);
        method.setAccessible(true);

        SchemaMetadata metadata =
                (SchemaMetadata) method.invoke(IdentityExtractor.INSTANCE, f);

        assertNotNull(metadata);
        assertTrue(metadata.isEmpty());
    }
}

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

package nl.datasteel.crudcraft.codegen.descriptor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SchemaMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;


class IdentityPartTest {

    private TypeMirror stringType() {
        String src = "package t; class C { String name; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement field =
                (VariableElement)
                        type.getEnclosedElements().stream()
                                .filter(e -> e.getSimpleName().contentEquals("name"))
                                .findFirst()
                                .orElseThrow();
        return field.asType();
    }

    @Test
    void gettersReturnNameAndType() {
        TypeMirror tm = stringType();
        SchemaMetadata metadata =
                new SchemaMetadata("description", "example", java.util.Map.of("x", "y"));
        Identity id = new Identity("name", tm, "javadoc", metadata);
        assertEquals("name", id.getName());
        assertEquals("name", id.name());
        assertEquals(tm, id.getType());
        assertEquals(tm, id.type());
        assertEquals("javadoc", id.getJavadoc());
        assertEquals("javadoc", id.javadoc());
        assertMetadata(metadata, id.getSchemaMetadata());
        assertMetadata(metadata, id.schemaMetadata());
        assertNotSame(metadata, id.getSchemaMetadata());
    }

    @Test
    void nullSchemaMetadataReturnsEmptyMetadata() {
        Identity id = new Identity("name", stringType(), null, null);

        assertMetadata(SchemaMetadata.empty(), id.getSchemaMetadata());
        assertMetadata(SchemaMetadata.empty(), id.schemaMetadata());
    }

    private static void assertMetadata(SchemaMetadata expected, SchemaMetadata actual) {
        assertEquals(expected.description(), actual.description());
        assertEquals(expected.example(), actual.example());
        assertEquals(expected.additionalProperties(), actual.additionalProperties());
    }
}

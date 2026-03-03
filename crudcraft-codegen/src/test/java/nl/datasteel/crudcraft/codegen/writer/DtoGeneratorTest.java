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
package nl.datasteel.crudcraft.codegen.writer;

import com.squareup.javapoet.JavaFile;
import java.util.List;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.EnumOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Relationship;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SchemaMetadata;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SearchOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Security;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Validation;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests for DtoGenerator. */
class DtoGeneratorTest {

    private WriteContext ctx;

    @BeforeEach
    void setup() {
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        Messager messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);
        ctx = new WriteContext(env);
    }

    @Test
    void returnsEmptyListForInvalidModel() {
        DtoGenerator gen = new DtoGenerator();
        assertTrue(gen.generate(null, ctx).isEmpty());
    }

    @Test
    void generatesThreeBasicDtos() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields()).thenReturn(List.of());

        DtoGenerator gen = new DtoGenerator();
        List<JavaFile> files = gen.generate(md, ctx);
        assertEquals(3, files.size());
        assertEquals("BookRequestDto", files.get(0).typeSpec.name);
        assertEquals("BookResponseDto", files.get(1).typeSpec.name);
        assertEquals("BookRef", files.get(2).typeSpec.name);
    }

    @Test
    void lobFieldInRequestDtoAllowsJsonDeserialization() {
        // Use real TypeMirrors from compiler
        javax.lang.model.util.Elements elems = nl.datasteel.crudcraft.codegen.CompilationTestUtils.elements(
                "t.C", "package t; class C { byte[] attachment; String title; }");
        javax.lang.model.element.TypeElement type = elems.getTypeElement("t.C");
        javax.lang.model.type.TypeMirror byteArrayType = type.getEnclosedElements().stream()
                .filter(e -> e.getSimpleName().contentEquals("attachment"))
                .findFirst().orElseThrow().asType();
        javax.lang.model.type.TypeMirror stringType = type.getEnclosedElements().stream()
                .filter(e -> e.getSimpleName().contentEquals("title"))
                .findFirst().orElseThrow().asType();

        FieldDescriptor lobField = new FieldDescriptor(
                new Identity("attachment", byteArrayType, null, SchemaMetadata.empty()),
                new DtoOptions(true, true, false, new String[0], true),
                new EnumOptions(false, List.of()),
                new Relationship(RelationshipType.NONE, "", null, false, false, false),
                new Validation(List.of()),
                new SearchOptions(false, List.of(), 0),
                new Security(false, null, null)
        );

        FieldDescriptor normalField = new FieldDescriptor(
                new Identity("title", stringType, null, SchemaMetadata.empty()),
                new DtoOptions(true, true, false, new String[0], false),
                new EnumOptions(false, List.of()),
                new Relationship(RelationshipType.NONE, "", null, false, false, false),
                new Validation(List.of()),
                new SearchOptions(false, List.of(), 0),
                new Security(false, null, null)
        );

        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Document");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields()).thenReturn(List.of(normalField, lobField));

        DtoGenerator gen = new DtoGenerator();
        List<JavaFile> files = gen.generate(md, ctx);

        // Request DTO should NOT have @JsonIgnore on LOB fields
        // because bulk/validate endpoints still use @RequestBody and need JSON LOB data
        String requestCode = files.get(0).toString();
        assertFalse(requestCode.contains("@JsonIgnore"),
                "LOB field in request DTO should not have @JsonIgnore");

        // Response DTO should also not have @JsonIgnore
        String responseCode = files.get(1).toString();
        assertFalse(responseCode.contains("@JsonIgnore"),
                "Response DTO should not have @JsonIgnore on LOB fields");
    }
}

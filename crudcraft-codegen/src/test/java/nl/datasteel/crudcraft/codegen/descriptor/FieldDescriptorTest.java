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
package nl.datasteel.crudcraft.codegen.descriptor;

import com.squareup.javapoet.AnnotationSpec;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.EnumOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Relationship;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SchemaMetadata;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SearchOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Security;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Validation;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class FieldDescriptorTest {

    private static VariableElement firstField(TypeElement type) {
        return (VariableElement) type.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .map(VariableElement.class::cast)
                .findFirst().orElseThrow();
    }

    private TypeMirror stringType() {
        String src = "package t; class C { String field; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement field = firstField(type);
        return field.asType();
    }

    private FieldDescriptor sample() {
        TypeMirror tm = stringType();
        Identity id = new Identity("field", tm, null, SchemaMetadata.empty());
        DtoOptions dto = new DtoOptions(true, true, false, new String[]{"X"});
        EnumOptions eo = new EnumOptions(false, List.of());
        Relationship rel = new Relationship(RelationshipType.NONE, "", "java.lang.String", false, false, false);
        Validation val = new Validation(List.of(AnnotationSpec.builder(Deprecated.class).build()));
        SearchOptions so = new SearchOptions(true, List.of(SearchOperator.EQUALS), 2);
        Security sec = new Security(false, new String[0], new String[0]);
        return new FieldDescriptor(id, dto, eo, rel, val, so, sec);
    }

    @Test
    void delegatesToParts() {
        FieldDescriptor fd = sample();
        assertEquals("field", fd.getName());
        assertTrue(fd.inDto());
        assertTrue(fd.inRequest());
        assertFalse(fd.inRef());
        assertArrayEquals(new String[]{"X"}, fd.getResponseDtos());
        assertFalse(fd.isEnumString());
        assertEquals(RelationshipType.NONE, fd.getRelType());
        assertEquals("", fd.getMappedBy());
        assertEquals("java.lang.String", fd.getTargetType());
        assertFalse(fd.isTargetCrud());
        assertFalse(fd.isEmbedded());
        assertEquals(1, fd.getValidations().size());
        assertTrue(fd.isSearchable());
        assertEquals(List.of(SearchOperator.EQUALS), fd.getSearchOperators());
        assertEquals(2, fd.getSearchDepth());
        assertFalse(fd.hasFieldSecurity());
        assertEquals(0, fd.getReadRoles().length);
        assertEquals(0, fd.getWriteRoles().length);
    }

    @Test
    void equalsAndHashCode() {
        FieldDescriptor a = sample();
        FieldDescriptor b = sample();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.toString().contains("field"));
    }

    @Test
    void notEqualWhenIdentityDiffers() {
        FieldDescriptor a = sample();
        TypeMirror tm = stringType();
        Identity otherId = new Identity("other", tm, null, SchemaMetadata.empty());
        DtoOptions dto = new DtoOptions(true, true, false, new String[]{"X"});
        EnumOptions eo = new EnumOptions(false, List.of());
        Relationship rel = new Relationship(RelationshipType.NONE, "", "java.lang.String", false, false, false);
        Validation val = new Validation(List.of(AnnotationSpec.builder(Deprecated.class).build()));
        SearchOptions so = new SearchOptions(true, List.of(SearchOperator.EQUALS), 2);
        Security sec = new Security(false, new String[0], new String[0]);
        FieldDescriptor b = new FieldDescriptor(otherId, dto, eo, rel, val, so, sec);
        assertNotEquals(a, b);
    }
}

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

import com.palantir.javapoet.AnnotationSpec;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;
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
import nl.datasteel.crudcraft.codegen.exception.CodegenValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class FieldDescriptorTest {

    private static VariableElement firstField(TypeElement type) {
        return (VariableElement)
                type.getEnclosedElements().stream()
                        .filter(e -> e.getKind() == ElementKind.FIELD)
                        .map(VariableElement.class::cast)
                        .findFirst()
                        .orElseThrow();
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
        DtoOptions dto = new DtoOptions(true, true, false, new String[] {"X"}, false);
        EnumOptions eo = new EnumOptions(false, List.of());
        Relationship rel =
                new Relationship(
                        RelationshipType.NONE, "", "java.lang.String", false, false, false);
        Validation val = new Validation(List.of(AnnotationSpec.builder(Deprecated.class).build()));
        SearchOptions so = new SearchOptions(true, List.of(SearchOperator.EQUALS), 2);
        Security sec = new Security(false, new String[0], new String[0]);
        return new FieldDescriptor(id, dto, eo, rel, val, so, sec);
    }

    @Test
    void delegatesToParts() {
        FieldDescriptor fd = sample();
        assertEquals("field", fd.getName());
        assertNotNull(fd.getType());
        assertTrue(fd.inDto());
        assertTrue(fd.inRequest());
        assertFalse(fd.inRef());
        assertArrayEquals(new String[] {"X"}, fd.getResponseDtos());
        assertFalse(fd.isEnumString());
        assertEquals(RelationshipType.NONE, fd.getRelType());
        assertEquals("", fd.getMappedBy());
        assertEquals("java.lang.String", fd.getTargetType());
        assertFalse(fd.isTargetCrud());
        assertFalse(fd.isEmbedded());
        assertFalse(fd.isTargetAbstract());
        assertFalse(fd.isLob());
        assertEquals(1, fd.getValidations().size());
        assertTrue(fd.isSearchable());
        assertEquals(List.of(SearchOperator.EQUALS), fd.getSearchOperators());
        assertEquals(2, fd.getSearchDepth());
        assertFalse(fd.hasFieldSecurity());
        assertEquals(0, fd.getReadRoles().length);
        assertEquals(0, fd.getWriteRoles().length);
        assertEquals(WritePolicy.SKIP_ON_DENIED, fd.getWritePolicy());
        assertTrue(fd.getSchemaMetadata().isEmpty());
        assertNull(fd.getJavadoc());
    }

    @Test
    void constructorRejectsMissingIdentityWithContext() {
        CodegenValidationException ex =
                assertThrows(
                        CodegenValidationException.class,
                        () -> new FieldDescriptor(null, null, null, null, null, null, null));

        assertTrue(ex.getMessage().contains("FieldDescriptor"));
        assertTrue(ex.getMessage().contains("identity must not be null"));
    }

    @Test
    void constructorDefaultsOptionalDescriptorPartsToSafeValues() {
        FieldDescriptor fd =
                new FieldDescriptor(
                        new Identity("field", null, null, SchemaMetadata.empty()),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        assertFalse(fd.inDto());
        assertEquals(RelationshipType.NONE, fd.getRelType());
        assertTrue(fd.getValidations().isEmpty());
        assertTrue(fd.getSearchOperators().isEmpty());
        assertFalse(fd.hasFieldSecurity());
    }

    @Test
    void relationshipAndEnumDelegatesExposePositiveValues() {
        TypeMirror tm = stringType();
        FieldDescriptor fd =
                new FieldDescriptor(
                        new Identity("owner", tm, "Owner docs", SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[] {"List"}, true),
                        new EnumOptions(true, List.of("A", "B")),
                        new Relationship(
                                RelationshipType.MANY_TO_ONE,
                                "children",
                                "com.example.Owner",
                                true,
                                true,
                                true),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(
                                true,
                                new String[] {"USER"},
                                new String[] {"ADMIN"},
                                WritePolicy.FAIL_ON_DENIED));

        assertTrue(fd.inRef());
        assertTrue(fd.isLob());
        assertTrue(fd.isEnumString());
        assertEquals(List.of("A", "B"), fd.getEnumValues());
        assertEquals(RelationshipType.MANY_TO_ONE, fd.getRelType());
        assertEquals("children", fd.getMappedBy());
        assertEquals("com.example.Owner", fd.getTargetType());
        assertTrue(fd.isTargetCrud());
        assertTrue(fd.isEmbedded());
        assertTrue(fd.isTargetAbstract());
        assertTrue(fd.hasFieldSecurity());
        assertFalse(fd.isSearchable());
        assertArrayEquals(new String[] {"USER"}, fd.getReadRoles());
        assertArrayEquals(new String[] {"ADMIN"}, fd.getWriteRoles());
        assertEquals(WritePolicy.FAIL_ON_DENIED, fd.getWritePolicy());
        assertEquals("Owner docs", fd.getJavadoc());
    }

    @Test
    void equalsAndHashCode() {
        FieldDescriptor a = sample();
        FieldDescriptor b = sample();
        assertEquals(a, b);
        assertEquals(a, a);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(0, a.hashCode());
        assertTrue(a.toString().contains("field"));
    }

    @Test
    void notEqualWhenIdentityDiffers() {
        FieldDescriptor a = sample();
        TypeMirror tm = stringType();
        Identity otherId = new Identity("other", tm, null, SchemaMetadata.empty());
        DtoOptions dto = new DtoOptions(true, true, false, new String[] {"X"}, false);
        EnumOptions eo = new EnumOptions(false, List.of());
        Relationship rel =
                new Relationship(
                        RelationshipType.NONE, "", "java.lang.String", false, false, false);
        Validation val = new Validation(List.of(AnnotationSpec.builder(Deprecated.class).build()));
        SearchOptions so = new SearchOptions(true, List.of(SearchOperator.EQUALS), 2);
        Security sec = new Security(false, new String[0], new String[0]);
        FieldDescriptor b = new FieldDescriptor(otherId, dto, eo, rel, val, so, sec);
        assertNotEquals(a, b);
    }

    @Test
    void equalsCoversTypeAndPartDifferences() {
        FieldDescriptor base = sample();
        TypeMirror tm = stringType();

        FieldDescriptor differentType =
                new FieldDescriptor(
                        new Identity("field", null, null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, false, new String[] {"X"}, false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.NONE, "", "java.lang.String", false, false, false),
                        new Validation(List.of(AnnotationSpec.builder(Deprecated.class).build())),
                        new SearchOptions(true, List.of(SearchOperator.EQUALS), 2),
                        new Security(false, new String[0], new String[0]));
        FieldDescriptor differentDto =
                new FieldDescriptor(
                        new Identity("field", tm, null, SchemaMetadata.empty()),
                        new DtoOptions(false, false, false, new String[] {"X"}, false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.NONE, "", "java.lang.String", false, false, false),
                        new Validation(List.of(AnnotationSpec.builder(Deprecated.class).build())),
                        new SearchOptions(true, List.of(SearchOperator.EQUALS), 2),
                        new Security(false, new String[0], new String[0]));

        assertNotEquals(base, null);
        assertNotEquals(base, "x");
        assertNotEquals(base, differentType);
        assertNotEquals(base, differentDto);
        assertFalse(differentDto.inDto());
        assertFalse(differentDto.inRequest());
        assertDoesNotThrow(differentType::hashCode);
        assertNotEquals(base.hashCode(), differentType.hashCode());
        assertNotEquals(base.hashCode(), differentDto.hashCode());
    }

    @Test
    void equalsDetectsDifferencesAcrossAllParts() {
        FieldDescriptor base = sample();
        TypeMirror tm = stringType();

        FieldDescriptor differentEnum =
                new FieldDescriptor(
                        new Identity("field", tm, null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, false, new String[] {"X"}, false),
                        new EnumOptions(true, List.of("A")),
                        new Relationship(
                                RelationshipType.NONE, "", "java.lang.String", false, false, false),
                        new Validation(List.of(AnnotationSpec.builder(Deprecated.class).build())),
                        new SearchOptions(true, List.of(SearchOperator.EQUALS), 2),
                        new Security(false, new String[0], new String[0]));
        FieldDescriptor differentRelationship =
                new FieldDescriptor(
                        new Identity("field", tm, null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, false, new String[] {"X"}, false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.ONE_TO_ONE,
                                "owner",
                                "java.lang.String",
                                false,
                                false,
                                false),
                        new Validation(List.of(AnnotationSpec.builder(Deprecated.class).build())),
                        new SearchOptions(true, List.of(SearchOperator.EQUALS), 2),
                        new Security(false, new String[0], new String[0]));
        FieldDescriptor differentValidation =
                new FieldDescriptor(
                        new Identity("field", tm, null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, false, new String[] {"X"}, false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.NONE, "", "java.lang.String", false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(true, List.of(SearchOperator.EQUALS), 2),
                        new Security(false, new String[0], new String[0]));
        FieldDescriptor differentSearch =
                new FieldDescriptor(
                        new Identity("field", tm, null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, false, new String[] {"X"}, false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.NONE, "", "java.lang.String", false, false, false),
                        new Validation(List.of(AnnotationSpec.builder(Deprecated.class).build())),
                        new SearchOptions(true, List.of(SearchOperator.CONTAINS), 2),
                        new Security(false, new String[0], new String[0]));
        FieldDescriptor differentSecurity =
                new FieldDescriptor(
                        new Identity("field", tm, null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, false, new String[] {"X"}, false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.NONE, "", "java.lang.String", false, false, false),
                        new Validation(List.of(AnnotationSpec.builder(Deprecated.class).build())),
                        new SearchOptions(true, List.of(SearchOperator.EQUALS), 2),
                        new Security(true, new String[] {"USER"}, new String[0]));

        assertNotEquals(base, differentEnum);
        assertNotEquals(base, differentRelationship);
        assertNotEquals(base, differentValidation);
        assertNotEquals(base, differentSearch);
        assertNotEquals(base, differentSecurity);
    }
}

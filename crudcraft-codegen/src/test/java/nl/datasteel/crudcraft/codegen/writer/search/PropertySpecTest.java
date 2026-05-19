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

package nl.datasteel.crudcraft.codegen.writer.search;

import com.google.testing.compile.JavaFileObjects;
import com.palantir.javapoet.TypeSpec;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PropertySpecTest {

    private static TypeMirror stringType;
    private static TypeMirror intType;
    private static TypeMirror listType;

    @BeforeAll
    static void compileTypes() {
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString(
                                "test.SearchTypes",
                                "package test; class SearchTypes { String name; int age; "
                                        + "java.util.List<String> tags; }"));
        TypeElement type = elements.getTypeElement("test.SearchTypes");
        stringType = fieldType(type, "name");
        intType = fieldType(type, "age");
        listType = fieldType(type, "tags");
    }

    @Test
    void valueOperatorAddsScalarFieldAndCopyNames() {
        PropertySpec spec =
                new PropertySpec(
                        descriptor(stringType), "name", Set.of(SearchOperator.CONTAINS));
        TypeSpec.Builder builder = TypeSpec.classBuilder("Request");

        spec.addMembers(builder);
        String code = builder.build().toString();

        assertTrue(code.contains("private java.lang.String name;"));
        assertTrue(code.contains("public java.lang.String getName()"));
        assertEquals(List.of("name", "nameOp"), List.copyOf(spec.copyFieldNames()));
    }

    @Test
    void rangeOperatorBoxesPrimitiveAndCopiesStartEndFields() {
        PropertySpec spec =
                new PropertySpec(descriptor(intType), "age", Set.of(SearchOperator.RANGE));
        TypeSpec.Builder builder = TypeSpec.classBuilder("Request");

        spec.addMembers(builder);
        String code = builder.build().toString();

        assertTrue(code.contains("private java.lang.Integer ageStart;"));
        assertTrue(code.contains("private java.lang.Integer ageEnd;"));
        assertEquals(List.of("ageStart", "ageEnd", "ageOp"), List.copyOf(spec.copyFieldNames()));
    }

    @Test
    void sizeOperatorAddsIntegerFieldAndCopiesIt() {
        PropertySpec spec =
                new PropertySpec(descriptor(listType), "tagsSize", Set.of(SearchOperator.SIZE_GT));
        TypeSpec.Builder builder = TypeSpec.classBuilder("Request");

        spec.addMembers(builder);
        String code = builder.build().toString();

        assertTrue(code.contains("private java.lang.Integer tagsSize;"));
        assertTrue(code.contains("public java.lang.Integer getTagsSize()"));
        assertTrue(
                code.contains(
                        "private nl.datasteel.crudcraft.annotations.SearchOperator tagsSizeOp;"));
        assertEquals(List.of("tagsSize", "tagsSizeOp"), List.copyOf(spec.copyFieldNames()));
    }

    @Test
    void nullOperatorsOnlyCreateOperatorField() {
        PropertySpec spec = new PropertySpec(descriptor(stringType), "flag", null);
        TypeSpec.Builder builder = TypeSpec.classBuilder("Request");

        spec.addMembers(builder);
        String code = builder.build().toString();

        assertTrue(
                code.contains(
                        "private nl.datasteel.crudcraft.annotations.SearchOperator flagOp;"));
        assertEquals(List.of("flagOp"), List.copyOf(spec.copyFieldNames()));
    }

    @Test
    void accessorsExposeConstructorValuesAndImmutableOperators() {
        FieldDescriptor descriptor = descriptor(stringType);
        PropertySpec spec =
                new PropertySpec(
                        descriptor,
                        "name",
                        Set.of(SearchOperator.CONTAINS, SearchOperator.EQUALS));

        assertEquals(descriptor, spec.descriptor());
        assertEquals("name", spec.name());
        assertEquals(Set.of(SearchOperator.CONTAINS, SearchOperator.EQUALS), spec.operators());
    }

    private static FieldDescriptor descriptor(TypeMirror type) {
        FieldDescriptor descriptor = mock(FieldDescriptor.class);
        when(descriptor.getType()).thenReturn(type);
        return descriptor;
    }

    private static TypeMirror fieldType(TypeElement type, String name) {
        return type.getEnclosedElements().stream()
                .filter(element -> element.getKind() == ElementKind.FIELD)
                .filter(element -> element.getSimpleName().contentEquals(name))
                .map(Element::asType)
                .findFirst()
                .orElseThrow();
    }
}

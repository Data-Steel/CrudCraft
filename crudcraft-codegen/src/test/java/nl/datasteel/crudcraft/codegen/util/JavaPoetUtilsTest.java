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
package nl.datasteel.crudcraft.codegen.util;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class JavaPoetUtilsTest {

    @Test
    void classNameAndListOfWork() {
        ClassName cn = JavaPoetUtils.getClassName("a.b", "C");
        assertEquals("a.b.C", cn.canonicalName());
        ParameterizedTypeName list = JavaPoetUtils.getListOf(TypeName.get(String.class));
        assertEquals("java.util.List<java.lang.String>", list.toString());
    }

    @Test
    void addFieldWithAccessorsHandlesCollectionsAndAnnotations() {
        AnnotationSpec val = AnnotationSpec.builder(Deprecated.class).build();
        AnnotationSpec schema = AnnotationSpec.builder(ClassName.get("io.swagger.v3.oas.annotations.media", "Schema"))
                .addMember("allowableValues", "$S", "A,B").build();
        AnnotationSpec sec = AnnotationSpec.builder(ClassName.get("test", "Sec")).build();
        TypeSpec.Builder builder = TypeSpec.classBuilder("T");
        ParameterizedTypeName setType = ParameterizedTypeName.get(ClassName.get(Set.class), TypeName.get(String.class));
        JavaPoetUtils.addFieldWithAccessors(builder, "values", setType, List.of(val), schema, sec);
        TypeSpec type = builder.build();
        assertEquals(1, type.fieldSpecs.size());
        String getter = type.methodSpecs.get(0).toString();
        assertTrue(getter.contains("Set.copyOf(this.values)"));
        String setter = type.methodSpecs.get(1).toString();
        assertTrue(setter.contains("new java.util.HashSet<>(values)"));
        assertTrue(type.fieldSpecs.getFirst().annotations.contains(schema));
        assertTrue(type.fieldSpecs.getFirst().annotations.contains(sec));
    }

    @Test
    void booleanGetterUsesIsPrefix() {
        TypeSpec.Builder builder = TypeSpec.classBuilder("B");
        JavaPoetUtils.addFieldWithAccessors(builder, "active", TypeName.BOOLEAN, List.of(), null, null);
        TypeSpec type = builder.build();
        assertEquals("isActive", type.methodSpecs.get(0).name);
    }

    @Test
    void addFieldWithGetterForListCopies() {
        TypeSpec.Builder builder = TypeSpec.classBuilder("L");
        ParameterizedTypeName listType = ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(String.class));
        JavaPoetUtils.addFieldWithGetter(builder, "names", listType, List.of(), null, null);
        TypeSpec type = builder.build();
        String getter = type.methodSpecs.get(0).toString();
        assertTrue(getter.contains("List.copyOf(this.names)"));
        assertEquals(1, type.methodSpecs.size());
        assertEquals(1, type.fieldSpecs.size());
        assertTrue(type.fieldSpecs.get(0).hasModifier(javax.lang.model.element.Modifier.FINAL));
    }

    @Test
    void addFieldWithAccessorsForListCopies() {
        TypeSpec.Builder builder = TypeSpec.classBuilder("ListHolder");
        ParameterizedTypeName listType = ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(String.class));
        JavaPoetUtils.addFieldWithAccessors(builder, "items", listType, List.of(), null, null);
        TypeSpec type = builder.build();
        String getter = type.methodSpecs.get(0).toString();
        assertTrue(getter.contains("List.copyOf(this.items)"));
        String setter = type.methodSpecs.get(1).toString();
        assertTrue(setter.contains("new java.util.ArrayList<>(items)"));
    }
}

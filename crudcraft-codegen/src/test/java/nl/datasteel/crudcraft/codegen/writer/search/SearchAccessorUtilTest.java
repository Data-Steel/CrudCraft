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
package nl.datasteel.crudcraft.codegen.writer.search;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class SearchAccessorUtilTest {

    @Test
    void getterReturnsFieldDirectlyForSimpleType() {
        MethodSpec getter = SearchAccessorUtil.getter("age", TypeName.INT.box());
        String code = toString(getter);
        assertTrue(code.contains("public Integer getAge()"));
        assertTrue(code.contains("return this.age"));
    }

    @Test
    void getterDefensivelyCopiesSet() {
        TypeName setType = ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class));
        MethodSpec getter = SearchAccessorUtil.getter("tags", setType);
        String code = toString(getter);
        assertTrue(code.contains("public Set<String> getTags()"));
        assertTrue(code.contains("return this.tags == null ? null : Set.copyOf(this.tags)"));
    }

    @Test
    void getterDefensivelyCopiesList() {
        TypeName listType = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class));
        MethodSpec getter = SearchAccessorUtil.getter("names", listType);
        String code = toString(getter);
        assertTrue(code.contains("public List<String> getNames()"));
        assertTrue(code.contains("return this.names == null ? null : List.copyOf(this.names)"));
    }

    @Test
    void setterAssignsSimpleType() {
        MethodSpec setter = SearchAccessorUtil.setter("age", TypeName.INT.box());
        String code = toString(setter);
        assertTrue(code.contains("public void setAge(Integer age)"));
        assertTrue(code.contains("this.age = age"));
    }

    @Test
    void setterDefensivelyCopiesSet() {
        TypeName setType = ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class));
        MethodSpec setter = SearchAccessorUtil.setter("tags", setType);
        String code = toString(setter);
        assertTrue(code.contains("public void setTags(Set<String> tags)"));
        assertTrue(code.contains("this.tags = tags == null ? null : new HashSet<>(tags)"));
    }

    @Test
    void setterDefensivelyCopiesList() {
        TypeName listType = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class));
        MethodSpec setter = SearchAccessorUtil.setter("names", listType);
        String code = toString(setter);
        assertTrue(code.contains("public void setNames(List<String> names)"));
        assertTrue(code.contains("this.names = names == null ? null : new ArrayList<>(names)"));
    }

    /**
     * Utility to convert a MethodSpec to string representation of a full class,
     * so we can inspect the generated method code in context.
     *
     * @param methodSpec the method specification to convert.
     * @return the string representation of the class containing the method.
     */
    private String toString(MethodSpec methodSpec) {
        TypeSpec type = TypeSpec.classBuilder("Dummy").addMethod(methodSpec).build();
        return JavaFile.builder("test", type).build().toString();
    }
}


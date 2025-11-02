/**
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.codegen.writer.search;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class OperatorSpecImplTest {

    @Test
    void valueOperatorAddsFieldAndAccessors() {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Test");
        TypeName type = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class));
        new ValueOperatorSpec().addFields(builder, "prop", type);
        String code = builder.build().toString();
        assertTrue(code.contains("private List<String> prop;"));
        assertTrue(code.contains("public List<String> getProp()"));
        assertTrue(code.contains("public void setProp(List<String> prop)"));
    }

    @Test
    void sizeOperatorAddsFieldAndAccessors() {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Test");
        new SizeOperatorSpec().addFields(builder, "count", TypeName.INT.box());
        String code = builder.build().toString();
        assertTrue(code.contains("private Integer count;"));
        assertTrue(code.contains("public Integer getCount()"));
        assertTrue(code.contains("public void setCount(Integer count)"));
    }

    @Test
    void rangeOperatorAddsStartAndEndFields() {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Test");
        new RangeOperatorSpec().addFields(builder, "age", TypeName.INT.box());
        String code = builder.build().toString();
        assertTrue(code.contains("private Integer ageStart;"));
        assertTrue(code.contains("private Integer ageEnd;"));
        assertTrue(code.contains("public Integer getAgeStart()"));
        assertTrue(code.contains("public void setAgeStart(Integer ageStart)"));
        assertTrue(code.contains("public Integer getAgeEnd()"));
        assertTrue(code.contains("public void setAgeEnd(Integer ageEnd)"));
    }
}


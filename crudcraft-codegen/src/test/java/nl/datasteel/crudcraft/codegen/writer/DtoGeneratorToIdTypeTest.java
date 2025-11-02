/*
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
package nl.datasteel.crudcraft.codegen.writer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/** Tests for DtoGenerator#toIdType. */
class DtoGeneratorToIdTypeTest {

    private TypeName invoke(TypeName original) throws Exception {
        Method m = DtoGenerator.class.getDeclaredMethod("toIdType", TypeName.class);
        m.setAccessible(true);
        return (TypeName) m.invoke(new DtoGenerator(), original);
    }

    @Test
    void convertsNonCollectionToUuid() throws Exception {
        TypeName result = invoke(ClassName.get(String.class));
        assertEquals(ClassName.get(UUID.class), result);
    }

    @Test
    void convertsListToUuidList() throws Exception {
        TypeName original = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class));
        TypeName result = invoke(original);
        assertEquals(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(UUID.class)), result);
    }

    @Test
    void convertsSetToUuidSet() throws Exception {
        TypeName original = ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class));
        TypeName result = invoke(original);
        assertEquals(ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(UUID.class)), result);
    }
}

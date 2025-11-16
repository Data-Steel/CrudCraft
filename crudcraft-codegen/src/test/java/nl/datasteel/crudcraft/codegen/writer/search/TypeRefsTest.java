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
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class TypeRefsTest {

    @Test
    void constantsAreCorrectClassNames() {
        assertEquals("java.util.Set", TypeRefs.SET().canonicalName());
        assertEquals("java.util.List", TypeRefs.LIST().canonicalName());
        assertEquals("java.time.Instant", TypeRefs.INSTANT().canonicalName());
    }

    @Test
    void setOfBuildsParameterizedType() {
        TypeName t = TypeRefs.setOf(ClassName.get(Instant.class));
        assertTrue(t instanceof ParameterizedTypeName);
        ParameterizedTypeName p = (ParameterizedTypeName) t;
        assertEquals("java.util.Set", ((ClassName) p.rawType).canonicalName());
        assertEquals(1, p.typeArguments.size());
        assertEquals("java.time.Instant", ((ClassName) p.typeArguments.get(0)).canonicalName());
    }

    @Test
    void listOfBuildsParameterizedType() {
        TypeName t = TypeRefs.listOf(ClassName.get(String.class));
        assertTrue(t instanceof ParameterizedTypeName);
        ParameterizedTypeName p = (ParameterizedTypeName) t;
        assertEquals("java.util.List", ((ClassName) p.rawType).canonicalName());
        assertEquals(1, p.typeArguments.size());
        assertEquals("java.lang.String", ((ClassName) p.typeArguments.get(0)).canonicalName());
    }
}

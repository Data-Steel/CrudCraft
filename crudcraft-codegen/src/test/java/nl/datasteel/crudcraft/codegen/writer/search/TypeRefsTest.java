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

import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TypeRefsTest {

    @Test
    void privateConstructorsAreCoveredForStrictMutationLineCoverage() throws Exception {
        var typeRefsConstructor = TypeRefs.class.getDeclaredConstructor();
        typeRefsConstructor.setAccessible(true);
        var typeNamesConstructor = TypeNames.class.getDeclaredConstructor();
        typeNamesConstructor.setAccessible(true);

        assertNotNull(typeRefsConstructor.newInstance());
        assertNotNull(typeNamesConstructor.newInstance());
    }

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
        assertEquals("java.util.Set", ((ClassName) p.rawType()).canonicalName());
        assertEquals(1, p.typeArguments().size());
        assertEquals("java.time.Instant", ((ClassName) p.typeArguments().get(0)).canonicalName());
    }

    @Test
    void listOfBuildsParameterizedType() {
        TypeName t = TypeRefs.listOf(ClassName.get(String.class));
        assertTrue(t instanceof ParameterizedTypeName);
        ParameterizedTypeName p = (ParameterizedTypeName) t;
        assertEquals("java.util.List", ((ClassName) p.rawType()).canonicalName());
        assertEquals(1, p.typeArguments().size());
        assertEquals("java.lang.String", ((ClassName) p.typeArguments().get(0)).canonicalName());
    }

    @Test
    void typeNamesSimplifiesClassParameterizedArrayAndPrimitiveTypes() {
        TypeName nested =
                ParameterizedTypeName.get(
                        ClassName.get("java.util", "Map"),
                        ClassName.get("java.lang", "String"),
                        ArrayTypeName.of(ClassName.get("com.example", "Thing")));

        assertEquals("Map<String, Thing[]>", TypeNames.simple(nested).toString());
        assertEquals("int", TypeNames.simple(TypeName.INT).toString());
    }
}

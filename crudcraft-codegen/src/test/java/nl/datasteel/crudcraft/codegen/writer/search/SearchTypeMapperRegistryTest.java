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
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SearchTypeMapperRegistryTest {

    @Test
    void mapsJavaTimeToClassName() {
        TypeName t1 = SearchTypeMapperRegistry.map(ClassName.get(LocalDate.class));
        TypeName t2 = SearchTypeMapperRegistry.map(ClassName.get(Instant.class));

        assertTrue(t1 instanceof ClassName);
        assertEquals("java.time.LocalDate", ((ClassName) t1).canonicalName());

        assertTrue(t2 instanceof ClassName);
        assertEquals("java.time.Instant", ((ClassName) t2).canonicalName());
    }

    @Test
    void mapsCollectionsRecursively() {
        ParameterizedTypeName original = ParameterizedTypeName.get(
                ClassName.get(Set.class), ClassName.get(Instant.class)
        );
        TypeName mapped = SearchTypeMapperRegistry.map(original);

        assertTrue(mapped instanceof ParameterizedTypeName);
        ParameterizedTypeName p = (ParameterizedTypeName) mapped;
        assertEquals("java.util.Set", ((ClassName) p.rawType).canonicalName());
        assertEquals(1, p.typeArguments.size());
        assertEquals("java.time.Instant", ((ClassName) p.typeArguments.get(0)).canonicalName());
    }

    @Test
    void mapsNestedCollectionsRecursively() {
        ParameterizedTypeName inner = ParameterizedTypeName.get(
                ClassName.get(Set.class), ClassName.get(Instant.class)
        );
        ParameterizedTypeName outer = ParameterizedTypeName.get(
                ClassName.get(List.class), inner
        );
        TypeName mapped = SearchTypeMapperRegistry.map(outer);

        assertTrue(mapped instanceof ParameterizedTypeName);
        ParameterizedTypeName pOuter = (ParameterizedTypeName) mapped;
        assertEquals("java.util.List", ((ClassName) pOuter.rawType).canonicalName());

        assertTrue(pOuter.typeArguments.get(0) instanceof ParameterizedTypeName);
        ParameterizedTypeName pInner = (ParameterizedTypeName) pOuter.typeArguments.get(0);
        assertEquals("java.util.Set", ((ClassName) pInner.rawType).canonicalName());
        assertEquals("java.time.Instant", ((ClassName) pInner.typeArguments.get(0)).canonicalName());
    }

    @Test
    void customMapperHasPriority() {
        ClassName custom = ClassName.get("com.example", "CustomDate");
        try {
            SearchTypeMapperRegistry.register(new SearchTypeMapperRegistry.Mapper() {
                @Override public boolean supports(TypeName original) {
                    return (original instanceof ClassName c) && c.canonicalName().equals(custom.canonicalName());
                }
                @Override public TypeName toSearchType(TypeName original) {
                    return ClassName.get(Instant.class);
                }
            });
            TypeName mapped = SearchTypeMapperRegistry.map(custom);
            assertTrue(mapped instanceof ClassName);
            assertEquals("java.time.Instant", ((ClassName) mapped).canonicalName());
        } finally {
            // no explicit reset API; registration is additive.
            // We registered a unique type, so it won't affect other tests.
        }
    }
}

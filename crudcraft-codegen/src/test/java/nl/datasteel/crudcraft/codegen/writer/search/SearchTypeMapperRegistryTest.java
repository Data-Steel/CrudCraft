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

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import java.lang.reflect.Constructor;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SearchTypeMapperRegistryTest {

    @AfterEach
    void resetCustomMappers() {
        SearchTypeMapperRegistry.resetForTesting();
    }

    private static final class CustomDateMapper implements SearchTypeMapperRegistry.Mapper {
        private final ClassName custom;

        private CustomDateMapper(ClassName custom) {
            this.custom = custom;
        }

        @Override
        public boolean supports(TypeName original) {
            return (original instanceof ClassName c)
                    && c.canonicalName().equals(custom.canonicalName());
        }

        @Override
        public TypeName toSearchType(TypeName original) {
            return ClassName.get(Instant.class);
        }
    }

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
    void mapsNullToObjectAndUnknownTypeToItself() {
        assertSame(ClassName.OBJECT, SearchTypeMapperRegistry.map(null));

        ClassName unknown = ClassName.get("com.example", "UnknownType");
        assertSame(unknown, SearchTypeMapperRegistry.map(unknown));
    }

    @Test
    void mapsCollectionsRecursively() {
        ParameterizedTypeName original =
                ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(Instant.class));
        TypeName mapped = SearchTypeMapperRegistry.map(original);

        assertTrue(mapped instanceof ParameterizedTypeName);
        ParameterizedTypeName p = (ParameterizedTypeName) mapped;
        assertEquals("java.util.Set", ((ClassName) p.rawType()).canonicalName());
        assertEquals(1, p.typeArguments().size());
        assertEquals("java.time.Instant", ((ClassName) p.typeArguments().get(0)).canonicalName());
    }

    @Test
    void mapsNestedCollectionsRecursively() {
        ParameterizedTypeName inner =
                ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(Instant.class));
        ParameterizedTypeName outer = ParameterizedTypeName.get(ClassName.get(List.class), inner);
        TypeName mapped = SearchTypeMapperRegistry.map(outer);

        assertTrue(mapped instanceof ParameterizedTypeName);
        ParameterizedTypeName pOuter = (ParameterizedTypeName) mapped;
        assertEquals("java.util.List", ((ClassName) pOuter.rawType()).canonicalName());

        assertTrue(pOuter.typeArguments().get(0) instanceof ParameterizedTypeName);
        ParameterizedTypeName pInner = (ParameterizedTypeName) pOuter.typeArguments().get(0);
        assertEquals("java.util.Set", ((ClassName) pInner.rawType()).canonicalName());
        assertEquals(
                "java.time.Instant", ((ClassName) pInner.typeArguments().get(0)).canonicalName());
    }

    @Test
    void leavesRawAndUnsupportedParameterizedCollectionsUntouched() {
        ClassName rawSet = ClassName.get(Set.class);
        assertSame(rawSet, SearchTypeMapperRegistry.map(rawSet));

        ParameterizedTypeName map =
                ParameterizedTypeName.get(
                        ClassName.get(Map.class), ClassName.get(String.class),
                        ClassName.get(Instant.class));
        assertSame(map, SearchTypeMapperRegistry.map(map));
    }

    @Test
    void collectionMapperRejectsRawTypesAndReturnsThemUnchanged() throws Exception {
        SearchTypeMapperRegistry.Mapper mapper = collectionMapper();
        ClassName rawSet = ClassName.get(Set.class);
        ParameterizedTypeName supported =
                ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(Instant.class));
        ParameterizedTypeName unsupported =
                ParameterizedTypeName.get(
                        ClassName.get(Map.class), ClassName.get(String.class),
                        ClassName.get(Instant.class));

        assertTrue(mapper.supports(supported));
        assertFalse(mapper.supports(rawSet));
        assertFalse(mapper.supports(unsupported));
        assertSame(rawSet, mapper.toSearchType(rawSet));
    }

    @Test
    void customMapperHasPriority() {
        ClassName custom = ClassName.get("com.example", "CustomDate" + System.nanoTime());
        assertSame(custom, SearchTypeMapperRegistry.map(custom));

        SearchTypeMapperRegistry.register(new CustomDateMapper(custom));
        TypeName mapped = SearchTypeMapperRegistry.map(custom);

        assertTrue(mapped instanceof ClassName);
        assertEquals("java.time.Instant", ((ClassName) mapped).canonicalName());
    }

    @Test
    void customMapperRegistrationIsSafeWhileMappingConcurrently() throws Exception {
        int workers = 8;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(workers);
        List<Throwable> failures = java.util.Collections.synchronizedList(new ArrayList<>());
        try (var executor = Executors.newFixedThreadPool(workers)) {
            for (int index = 0; index < workers; index++) {
                int worker = index;
                executor.submit(
                        () -> {
                            try {
                                start.await(5, TimeUnit.SECONDS);
                                ClassName custom =
                                        ClassName.get("com.example", "Concurrent" + worker);
                                SearchTypeMapperRegistry.register(new CustomDateMapper(custom));
                                SearchTypeMapperRegistry.map(custom);
                            } catch (Throwable ex) {
                                failures.add(ex);
                            } finally {
                                done.countDown();
                            }
                        });
            }

            start.countDown();

            assertTrue(done.await(5, TimeUnit.SECONDS));
            assertTrue(failures.isEmpty(), () -> "Concurrent registry failures: " + failures);
        }
    }

    @Test
    void registerIgnoresNullMapper() {
        ClassName custom = ClassName.get("com.example", "NullMapperType" + System.nanoTime());

        SearchTypeMapperRegistry.register(null);

        assertSame(custom, SearchTypeMapperRegistry.map(custom));
    }

    @Test
    void privateConstructorIsCoveredForUtilityClass() throws Exception {
        Constructor<SearchTypeMapperRegistry> constructor =
                SearchTypeMapperRegistry.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance());
    }

    private static SearchTypeMapperRegistry.Mapper collectionMapper() throws Exception {
        Class<?> mapperClass =
                Class.forName(
                        "nl.datasteel.crudcraft.codegen.writer.search.SearchTypeMapperRegistry"
                                + "$CollectionMapper");
        Constructor<?> constructor = mapperClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return (SearchTypeMapperRegistry.Mapper) constructor.newInstance();
    }
}

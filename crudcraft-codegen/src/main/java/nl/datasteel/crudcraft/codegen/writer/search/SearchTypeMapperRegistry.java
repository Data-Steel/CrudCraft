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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import nl.datasteel.crudcraft.codegen.util.CollectionTypes;


/**
 * Central place to decide which type the SearchRequest should expose for a given model type. -
 * Always return JavaPoet TypeNames (never raw strings) so imports are generated. - Extensible via
 * register(...) to support custom library types.
 */
public final class SearchTypeMapperRegistry {

    /** A mapper turns a model field type into a SearchRequest field type. */
    public interface Mapper {
        /**
         * Checks whether the mapper can handle the supplied type.
         *
         * @param original original field type
         * @return {@code true} when the mapper can convert the type
         */
        boolean supports(TypeName original);

        /**
         * Converts the original type into the search-request type.
         *
         * @param original original field type
         * @return mapped search-request type
         */
        TypeName toSearchType(TypeName original);
    }

    private static final List<Mapper> DEFAULT_MAPPERS =
            List.of(
                    new SimpleMapper(LocalDate.class),
                    new SimpleMapper(LocalDateTime.class),
                    new SimpleMapper(OffsetDateTime.class),
                    new SimpleMapper(ZonedDateTime.class),
                    new SimpleMapper(Instant.class),
                    new SimpleMapper(UUID.class),
                    new SimpleMapper(BigDecimal.class),
                    new SimpleMapper(BigInteger.class),
                    new CollectionMapper());

    private static final AtomicReference<List<Mapper>> CUSTOM =
            new AtomicReference<>(List.of());

    private SearchTypeMapperRegistry() {}

    /**
     * Registers a custom mapper with higher priority than the defaults.
     *
     * @param mapper mapper to add
     */
    public static void register(Mapper mapper) {
        if (mapper != null) {
            CUSTOM.updateAndGet(existing -> prepend(mapper, existing));
        }
    }

    static void resetForTesting() {
        CUSTOM.set(List.of());
    }

    private static List<Mapper> prepend(Mapper mapper, List<Mapper> existing) {
        java.util.ArrayList<Mapper> updated = new java.util.ArrayList<>(existing.size() + 1);
        updated.add(mapper);
        updated.addAll(existing);
        return List.copyOf(updated);
    }

    /**
     * Map a model-field TypeName to its SearchRequest field TypeName. Keeps parameterized raw types
     * (e.g. Set/List) and maps their args recursively.
     *
     * @param original field type to map
     * @return mapped search-request type
     */
    public static TypeName map(TypeName original) {
        if (original == null) {
            return ClassName.OBJECT;
        }

        for (Mapper m : CUSTOM.get()) {
            if (m.supports(original)) {
                return m.toSearchType(original);
            }
        }
        for (Mapper m : DEFAULT_MAPPERS) {
            if (m.supports(original)) {
                return m.toSearchType(original);
            }
        }
        return original;
    }

    // Helpers.

    /** Maps a single concrete class to itself (ensuring ClassName import). */
    private static final class SimpleMapper implements Mapper {
        private final ClassName cn;

        private SimpleMapper(Class<?> cls) {
            this.cn = ClassName.get(cls);
        }

        @Override
        public boolean supports(TypeName original) {
            return original instanceof ClassName c && c.canonicalName().equals(cn.canonicalName());
        }

        @Override
        public TypeName toSearchType(TypeName original) {
            return cn;
        }
    }

    /**
     * * Maps parameterized collections preserving raw type and mapping type arguments recursively.
     * .
     */
    private static final class CollectionMapper implements Mapper {
        private static final Set<String> SUPPORTED_RAW =
                Set.of(
                        CollectionTypes.JAVA_UTIL_SET,
                        CollectionTypes.JAVA_UTIL_LIST,
                        CollectionTypes.JAVA_UTIL_COLLECTION);

        @Override
        public boolean supports(TypeName original) {
            if (original instanceof ParameterizedTypeName p) {
                return SUPPORTED_RAW.contains(((ClassName) p.rawType()).canonicalName());
            }
            return false;
        }

        @Override
        public TypeName toSearchType(TypeName original) {
            if (!(original instanceof ParameterizedTypeName p)) {
                return original;
            }
            ClassName raw = p.rawType();
            TypeName[] args =
                    p.typeArguments().stream()
                            .map(SearchTypeMapperRegistry::map)
                            .toArray(TypeName[]::new);
            return ParameterizedTypeName.get(raw, args);
        }
    }
}

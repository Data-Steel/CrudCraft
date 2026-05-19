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

package nl.datasteel.crudcraft.codegen.util;

import java.util.Set;


/** Shared Java collection type names used by code generators and extractors. */
public final class CollectionTypes {

    /** Simple {@code Collection} type name. */
    public static final String COLLECTION = "Collection";

    /** Simple {@code List} type name. */
    public static final String LIST = "List";

    /** Simple {@code Map} type name. */
    public static final String MAP = "Map";

    /** Simple {@code Optional} type name. */
    public static final String OPTIONAL = "Optional";

    /** Simple {@code Set} type name. */
    public static final String SET = "Set";

    /** Fully qualified {@code Collection} type name. */
    public static final String JAVA_UTIL_COLLECTION = "java.util.Collection";

    /** Fully qualified {@code List} type name. */
    public static final String JAVA_UTIL_LIST = "java.util.List";

    /** Fully qualified {@code Map} type name. */
    public static final String JAVA_UTIL_MAP = "java.util.Map";

    /** Fully qualified {@code Optional} type name. */
    public static final String JAVA_UTIL_OPTIONAL = "java.util.Optional";

    /** Fully qualified {@code Set} type name. */
    public static final String JAVA_UTIL_SET = "java.util.Set";

    private static final Set<String> QUALIFIED_COLLECTION_TYPES =
            Set.of(JAVA_UTIL_COLLECTION, JAVA_UTIL_LIST, JAVA_UTIL_SET);

    private CollectionTypes() {}

    /**
     * Checks whether a fully qualified type name is one of the supported collection interfaces.
     *
     * @param qualifiedName the fully qualified type name to inspect
     * @return {@code true} for {@code Collection}, {@code List}, or {@code Set}
     */
    public static boolean isQualifiedCollection(String qualifiedName) {
        return QUALIFIED_COLLECTION_TYPES.contains(qualifiedName);
    }

    /**
     * Checks whether a rendered type includes a simple collection type name.
     *
     * @param typeName the rendered type name to inspect
     * @return {@code true} when it contains a recognized collection type
     */
    public static boolean containsCollectionType(String typeName) {
        return typeName.contains(COLLECTION) || typeName.contains(LIST) || typeName.contains(SET);
    }
}

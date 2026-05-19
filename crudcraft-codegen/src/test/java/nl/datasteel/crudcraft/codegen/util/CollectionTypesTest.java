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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class CollectionTypesTest {

    @Test
    void qualifiedCollectionDetectionAcceptsSupportedInterfacesOnly() {
        assertTrue(CollectionTypes.isQualifiedCollection(CollectionTypes.JAVA_UTIL_COLLECTION));
        assertTrue(CollectionTypes.isQualifiedCollection(CollectionTypes.JAVA_UTIL_LIST));
        assertTrue(CollectionTypes.isQualifiedCollection(CollectionTypes.JAVA_UTIL_SET));

        assertFalse(CollectionTypes.isQualifiedCollection(CollectionTypes.JAVA_UTIL_MAP));
        assertFalse(CollectionTypes.isQualifiedCollection(CollectionTypes.JAVA_UTIL_OPTIONAL));
        assertFalse(CollectionTypes.isQualifiedCollection("java.lang.String"));
    }

    @Test
    void renderedTypeDetectionFindsSimpleCollectionNamesOnly() {
        assertTrue(CollectionTypes.containsCollectionType("java.util.List<java.lang.String>"));
        assertTrue(CollectionTypes.containsCollectionType("java.util.Set<java.util.UUID>"));
        assertTrue(CollectionTypes.containsCollectionType("java.util.Collection<byte[]>"));

        assertFalse(CollectionTypes.containsCollectionType("java.util.Map<java.lang.String, X>"));
        assertFalse(CollectionTypes.containsCollectionType("java.util.Optional<X>"));
    }
}

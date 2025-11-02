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
package nl.datasteel.crudcraft.codegen.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class PluralizerTest {

    @Test
    void pluralizeHandlesNullAndBlank() {
        assertNull(Pluralizer.pluralize(null));
        assertEquals("", Pluralizer.pluralize(""));
    }

    @Test
    void pluralizeAppliesEsAndIesRules() {
        assertEquals("boxes", Pluralizer.pluralize("box"));
        assertEquals("parties", Pluralizer.pluralize("party"));
    }

    @Test
    void pluralizeDefaultAddsS() {
        assertEquals("cars", Pluralizer.pluralize("car"));
    }

    @Test
    void singularizeHandlesIrregularAndCase() {
        assertEquals("index", Pluralizer.singularize("indices"));
        assertEquals("Index", Pluralizer.singularize("Indices"));
    }

    @Test
    void singularizeRevertsIesAndEs() {
        assertEquals("baby", Pluralizer.singularize("babies"));
        assertEquals("box", Pluralizer.singularize("boxes"));
        assertEquals("bus", Pluralizer.singularize("buses"));
    }
}

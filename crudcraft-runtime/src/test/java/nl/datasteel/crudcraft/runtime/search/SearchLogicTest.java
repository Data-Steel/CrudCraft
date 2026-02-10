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
package nl.datasteel.crudcraft.runtime.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 * Tests for the SearchLogic enum.
 */
class SearchLogicTest {

    @Test
    void enumHasOrValue() {
        SearchLogic logic = SearchLogic.OR;
        assertNotNull(logic);
        assertEquals("OR", logic.name());
    }

    @Test
    void enumHasAndValue() {
        SearchLogic logic = SearchLogic.AND;
        assertNotNull(logic);
        assertEquals("AND", logic.name());
    }

    @Test
    void enumHasTwoValues() {
        SearchLogic[] values = SearchLogic.values();
        assertEquals(2, values.length);
    }

    @Test
    void enumValueOf() {
        assertEquals(SearchLogic.OR, SearchLogic.valueOf("OR"));
        assertEquals(SearchLogic.AND, SearchLogic.valueOf("AND"));
    }
}

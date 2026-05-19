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

package nl.datasteel.crudcraft.codegen.descriptor;

import java.util.ArrayList;
import java.util.List;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SearchOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SearchOptionsTest {

    @Test
    void defensiveCopyOfOperators() {
        List<SearchOperator> ops = new ArrayList<>();
        ops.add(SearchOperator.EQUALS);
        SearchOptions so = new SearchOptions(true, ops, 3);
        ops.add(SearchOperator.CONTAINS);
        assertEquals(List.of(SearchOperator.EQUALS), so.getOperators());
        assertThrows(
                UnsupportedOperationException.class,
                () -> so.getOperators().add(SearchOperator.AFTER));
    }

    @Test
    void nullOperatorsCreatesEmptyList() {
        SearchOptions so = new SearchOptions(false, null, 0);
        assertTrue(so.getOperators().isEmpty());
        assertFalse(so.isSearchable());
        assertEquals(0, so.getDepth());
    }

    @Test
    void equalsAndHashCodeCoverAllBranches() {
        SearchOptions base = new SearchOptions(true, List.of(SearchOperator.EQUALS), 1);
        SearchOptions same = new SearchOptions(true, List.of(SearchOperator.EQUALS), 1);
        SearchOptions differentSearchable =
                new SearchOptions(false, List.of(SearchOperator.EQUALS), 1);
        SearchOptions differentDepth = new SearchOptions(true, List.of(SearchOperator.EQUALS), 2);
        SearchOptions differentOperators =
                new SearchOptions(true, List.of(SearchOperator.CONTAINS), 1);
        int expectedHash = Boolean.hashCode(true);
        expectedHash = 31 * expectedHash + List.of(SearchOperator.EQUALS).hashCode();
        expectedHash = 31 * expectedHash + Integer.hashCode(1);

        assertEquals(base, same);
        assertEquals(expectedHash, base.hashCode());
        assertEquals(base, base);
        assertNotEquals(base, null);
        assertNotEquals(base, "x");
        assertNotEquals(base, differentSearchable);
        assertNotEquals(base, differentDepth);
        assertNotEquals(base, differentOperators);
        assertTrue(base.searchable());
        assertFalse(differentSearchable.searchable());
        assertEquals(List.of(SearchOperator.EQUALS), base.operators());
        assertEquals(1, base.depth());
        assertEquals(2, differentDepth.depth());
        assertEquals(
                "SearchOptions{searchable=true, operators=[EQUALS], depth=1}", base.toString());
    }
}

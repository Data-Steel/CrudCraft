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
package nl.datasteel.crudcraft.codegen.descriptor;

import java.util.ArrayList;
import java.util.List;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SearchOptions;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SearchOptionsTest {

    @Test
    void defensiveCopyOfOperators() {
        List<SearchOperator> ops = new ArrayList<>();
        ops.add(SearchOperator.EQUALS);
        SearchOptions so = new SearchOptions(true, ops, 3);
        ops.add(SearchOperator.CONTAINS);
        assertEquals(List.of(SearchOperator.EQUALS), so.getOperators());
        assertThrows(UnsupportedOperationException.class, () -> so.getOperators().add(SearchOperator.AFTER));
    }

    @Test
    void nullOperatorsCreatesEmptyList() {
        SearchOptions so = new SearchOptions(false, null, 0);
        assertTrue(so.getOperators().isEmpty());
        assertFalse(so.isSearchable());
        assertEquals(0, so.getDepth());
    }
}

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

package nl.datasteel.crudcraft.annotations;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SearchOperatorTest {

    @Test
    void enumContainsAllSearchOperators() {
        assertEquals(24, SearchOperator.values().length);
        assertTrue(EnumSet.allOf(SearchOperator.class).contains(SearchOperator.CONTAINS));
        assertTrue(EnumSet.allOf(SearchOperator.class).contains(SearchOperator.CONTAINS_VALUE));
    }
}

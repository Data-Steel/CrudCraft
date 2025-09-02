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

package nl.datasteel.crudcraft.annotations;

import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SearchOperatorTest {

    @Test
    void containsAllOperators() {
        Set<SearchOperator> expected = EnumSet.of(
                SearchOperator.EQUALS,
                SearchOperator.NOT_EQUALS,
                SearchOperator.CONTAINS,
                SearchOperator.STARTS_WITH,
                SearchOperator.ENDS_WITH,
                SearchOperator.REGEX,
                SearchOperator.GT,
                SearchOperator.GTE,
                SearchOperator.LT,
                SearchOperator.LTE,
                SearchOperator.IN,
                SearchOperator.NOT_IN,
                SearchOperator.RANGE,
                SearchOperator.BEFORE,
                SearchOperator.AFTER,
                SearchOperator.BETWEEN,
                SearchOperator.IS_EMPTY,
                SearchOperator.SIZE_EQUALS,
                SearchOperator.SIZE_GT,
                SearchOperator.SIZE_LT,
                SearchOperator.NOT_EMPTY,
                SearchOperator.CONTAINS_ALL,
                SearchOperator.CONTAINS_KEY,
                SearchOperator.CONTAINS_VALUE
        );
        assertEquals(expected, EnumSet.allOf(SearchOperator.class));
    }

    @Test
    void expectedNumberOfOperators() {
        assertEquals(24, SearchOperator.values().length);
    }

    @Test
    void valueOfRejectsUnknownOperator() {
        assertThrows(IllegalArgumentException.class,
                () -> SearchOperator.valueOf("UNKNOWN"));
    }
}


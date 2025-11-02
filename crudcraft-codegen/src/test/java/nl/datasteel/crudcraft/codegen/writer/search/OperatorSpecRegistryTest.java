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
package nl.datasteel.crudcraft.codegen.writer.search;

import nl.datasteel.crudcraft.annotations.SearchOperator;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class OperatorSpecRegistryTest {

    @Test
    void factoryMethodsReturnFreshInstances() {
        assertNotSame(OperatorSpecRegistry.value(), OperatorSpecRegistry.value());
        assertNotSame(OperatorSpecRegistry.range(), OperatorSpecRegistry.range());
        assertNotSame(OperatorSpecRegistry.size(), OperatorSpecRegistry.size());
    }

    @Test
    void identifiesRangeOperators() {
        assertTrue(OperatorSpecRegistry.isRangeOperator(SearchOperator.RANGE));
        assertTrue(OperatorSpecRegistry.isRangeOperator(SearchOperator.BETWEEN));
        assertFalse(OperatorSpecRegistry.isRangeOperator(SearchOperator.EQUALS));
    }

    @Test
    void identifiesSizeOperators() {
        assertTrue(OperatorSpecRegistry.isSizeOperator(SearchOperator.SIZE_EQUALS));
        assertTrue(OperatorSpecRegistry.isSizeOperator(SearchOperator.SIZE_GT));
        assertTrue(OperatorSpecRegistry.isSizeOperator(SearchOperator.SIZE_LT));
        assertFalse(OperatorSpecRegistry.isSizeOperator(SearchOperator.EQUALS));
    }

    @Test
    void identifiesValueOperators() {
        assertTrue(OperatorSpecRegistry.isValueOperator(SearchOperator.EQUALS));
        assertFalse(OperatorSpecRegistry.isValueOperator(SearchOperator.RANGE));
        assertFalse(OperatorSpecRegistry.isValueOperator(SearchOperator.SIZE_GT));
        assertFalse(OperatorSpecRegistry.isValueOperator(SearchOperator.IS_EMPTY));
        assertFalse(OperatorSpecRegistry.isValueOperator(SearchOperator.NOT_EMPTY));
    }
}


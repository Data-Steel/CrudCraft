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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class PredicateGeneratorRegistryTest {

    @Test
    void eachOperatorGeneratesCode() {
        for (SearchOperator op : SearchOperator.values()) {
            PredicateGenerator generator = PredicateGeneratorRegistry.of(op);
            assertNotNull(generator, "Missing generator for " + op);
            SearchField field = new SearchField(null, "name", "root.name", op);
            assertNotNull(generator.generate(field));
        }
    }

    @Test
    void unknownOperatorReturnsNull() {
        assertNull(PredicateGeneratorRegistry.of(null));
    }
}

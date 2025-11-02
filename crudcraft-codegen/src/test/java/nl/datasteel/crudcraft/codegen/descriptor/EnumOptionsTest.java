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

package nl.datasteel.crudcraft.codegen.descriptor;

import java.util.ArrayList;
import java.util.List;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.EnumOptions;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EnumOptionsTest {

    @Test
    void valuesAreDefensivelyCopied() {
        List<String> src = new ArrayList<>();
        src.add("A");
        EnumOptions opts = new EnumOptions(true, src);
        src.add("B");
        assertEquals(List.of("A"), opts.getValues());
        assertThrows(UnsupportedOperationException.class, () -> opts.getValues().add("C"));
    }

    @Test
    void nullListResultsInEmptyValues() {
        EnumOptions opts = new EnumOptions(false, null);
        assertTrue(opts.getValues().isEmpty());
    }
}

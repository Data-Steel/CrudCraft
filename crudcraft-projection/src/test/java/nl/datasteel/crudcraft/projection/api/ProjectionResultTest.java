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
package nl.datasteel.crudcraft.projection.api;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ProjectionResultTest {

    @Test
    void constructorDefensivelyCopiesList() {
        List<String> original = new ArrayList<>();
        original.add("one");
        ProjectionResult<String> result = new ProjectionResult<>(original, 1);
        original.add("two");
        assertEquals(List.of("one"), result.content());
    }

    @Test
    void contentAccessorReturnsCopy() {
        List<String> original = List.of("a");
        ProjectionResult<String> result = new ProjectionResult<>(original, 1);
        List<String> copy = result.content();
        assertThrows(UnsupportedOperationException.class, () -> copy.add("b"));
        assertEquals(List.of("a"), result.content());
    }

    @Test
    void constructorRejectsNullContent() {
        assertThrows(NullPointerException.class, () -> new ProjectionResult<String>(null, 0));
    }
}

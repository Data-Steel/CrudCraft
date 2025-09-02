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

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RelationshipTypeTest {

    @Test
    void bidirectionalCheck() {
        assertTrue(RelationshipType.ONE_TO_ONE.isBidirectional());
        assertTrue(RelationshipType.ONE_TO_MANY.isBidirectional());
        assertTrue(RelationshipType.MANY_TO_MANY.isBidirectional());
        assertFalse(RelationshipType.MANY_TO_ONE.isBidirectional());
        assertFalse(RelationshipType.NONE.isBidirectional());
    }

    @Test
    void fromStringParsesAllValuesCaseInsensitive() {
        assertEquals(RelationshipType.NONE, RelationshipType.fromString("none"));
        assertEquals(RelationshipType.ONE_TO_ONE, RelationshipType.fromString("one_to_one"));
        assertEquals(RelationshipType.ONE_TO_MANY, RelationshipType.fromString("One_To_Many"));
        assertEquals(RelationshipType.MANY_TO_ONE, RelationshipType.fromString("MANY_TO_ONE"));
        assertEquals(RelationshipType.MANY_TO_MANY, RelationshipType.fromString("many_to_many"));
    }

    @Test
    void fromStringRejectsUnknownValue() {
        assertThrows(IllegalArgumentException.class, () -> RelationshipType.fromString("invalid"));
    }
}

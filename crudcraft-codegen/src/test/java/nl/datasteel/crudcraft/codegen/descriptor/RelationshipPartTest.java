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

import nl.datasteel.crudcraft.codegen.descriptor.field.part.Relationship;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RelationshipPartTest {

    @Test
    void gettersWork() {
        Relationship rel = new Relationship(RelationshipType.MANY_TO_ONE, "mapped", "Target", true, false);
        assertEquals(RelationshipType.MANY_TO_ONE, rel.getRelationshipType());
        assertEquals("mapped", rel.getMappedBy());
        assertEquals("Target", rel.getTargetType());
        assertTrue(rel.isTargetCrud());
        assertFalse(rel.isEmbedded());
    }

    @Test
    void embeddedRelationship() {
        Relationship rel = new Relationship(RelationshipType.NONE, "", "T", false, true);
        assertTrue(rel.isEmbedded());
        assertEquals(RelationshipType.NONE, rel.getRelationshipType());
    }
}

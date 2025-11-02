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
package nl.datasteel.crudcraft.runtime.util;

import nl.datasteel.crudcraft.runtime.exception.RelationshipException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RelationshipUtilsTest {

    public static class PlainEntity {}

    @Test
    void fixBidirectionalInvokesMetaWhenPresent() {
        WithMeta entity = new WithMeta();
        RelationshipUtils.fixBidirectional(entity);
        assertTrue(entity.fixed);
    }

    @Test
    void fixBidirectionalSkipsWhenMetaMissing() {
        PlainEntity entity = new PlainEntity();
        RelationshipUtils.fixBidirectional(entity);
    }

    @Test
    void fixBidirectionalThrowsWhenMetaFails() {
        BrokenMeta entity = new BrokenMeta();
        assertThrows(RelationshipException.class, () -> RelationshipUtils.fixBidirectional(entity));
    }

    @Test
    void clearBidirectionalInvokesMetaWhenPresent() {
        WithMeta entity = new WithMeta();
        RelationshipUtils.clearBidirectional(entity);
        assertTrue(entity.cleared);
    }

    @Test
    void clearBidirectionalSkipsWhenMetaMissing() {
        PlainEntity entity = new PlainEntity();
        RelationshipUtils.clearBidirectional(entity);
    }

    @Test
    void clearBidirectionalThrowsWhenMetaFails() {
        BrokenMeta entity = new BrokenMeta();
        assertThrows(RelationshipException.class, () -> RelationshipUtils.clearBidirectional(entity));
    }
}

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
package nl.datasteel.crudcraft.runtime.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrudCraftSearchPropertiesTest {

    @BeforeEach
    void resetStaticDepth() {
        // ensure static depth is reset before each test
        CrudCraftSearchProperties props = new CrudCraftSearchProperties();
        props.setDepth(1);
    }

    @Test
    void defaultDepthIsOne() {
        CrudCraftSearchProperties props = new CrudCraftSearchProperties();
        assertEquals(1, props.getDepth());
        assertEquals(1, CrudCraftSearchProperties.getStaticDepth());
    }

    @Test
    void setDepthUpdatesStaticDepth() {
        CrudCraftSearchProperties props = new CrudCraftSearchProperties();
        props.setDepth(5);
        assertEquals(5, props.getDepth());
        assertEquals(5, CrudCraftSearchProperties.getStaticDepth());
    }

    @Test
    void staticDepthSharedAcrossInstances() {
        CrudCraftSearchProperties first = new CrudCraftSearchProperties();
        first.setDepth(3);
        CrudCraftSearchProperties second = new CrudCraftSearchProperties();
        assertEquals(1, second.getDepth());
        assertEquals(3, CrudCraftSearchProperties.getStaticDepth());
    }

    @Test
    void negativeDepthIsAccepted() {
        CrudCraftSearchProperties props = new CrudCraftSearchProperties();
        props.setDepth(-2);
        assertEquals(-2, props.getDepth());
        assertEquals(-2, CrudCraftSearchProperties.getStaticDepth());
    }
}

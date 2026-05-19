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

package nl.datasteel.crudcraft.starter.projection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import nl.datasteel.crudcraft.runtime.projection.config.ProjectionAutoConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class CrudCraftProjectionStarterTest {

    @Test
    void markerTypeIsFinalWithPrivateConstructor() throws Exception {
        Constructor<CrudCraftProjectionStarter> constructor =
                CrudCraftProjectionStarter.class.getDeclaredConstructor();

        assertTrue(Modifier.isFinal(CrudCraftProjectionStarter.class.getModifiers()));
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }

    @Test
    void starterArtifactExposesRuntimeProjectionAutoConfiguration() {
        assertEquals(
                "nl.datasteel.crudcraft.runtime.projection.config",
                ProjectionAutoConfiguration.class.getPackageName());
    }
}

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
package nl.datasteel.crudcraft.projection.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ProjectionPropertiesTest {

    @Test
    void defaultEngineIsCriteria() {
        ProjectionProperties props = new ProjectionProperties();
        assertEquals(ProjectionProperties.Engine.CRITERIA, props.getEngine());
    }

    @Test
    void engineCanBeChanged() {
        ProjectionProperties props = new ProjectionProperties();
        props.setEngine(ProjectionProperties.Engine.QUERYDSL);
        assertEquals(ProjectionProperties.Engine.QUERYDSL, props.getEngine());
    }
}

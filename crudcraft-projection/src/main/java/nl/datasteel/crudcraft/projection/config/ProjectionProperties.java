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
package nl.datasteel.crudcraft.projection.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for projection engine selection.
 */
@ConfigurationProperties(prefix = "crudcraft.projection")
public class ProjectionProperties {

    /**
     * Enum representing the available projection engines.
     * CRITERIA uses JPA Criteria API, QUERYDSL uses QueryDSL.
     */
    public enum Engine { CRITERIA, QUERYDSL }

    /**
     * The projection engine to use for executing projections.
     * Default is CRITERIA, which uses JPA Criteria API.
     */
    private Engine engine = Engine.CRITERIA;

    /**
     * Gets the projection engine to use.
     *
     * @return the current projection engine
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * Sets the projection engine to use.
     *
     * @param engine the projection engine to set
     */
    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}

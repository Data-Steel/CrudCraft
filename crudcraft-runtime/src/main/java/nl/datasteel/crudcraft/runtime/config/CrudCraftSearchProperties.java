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
package nl.datasteel.crudcraft.runtime.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for CrudCraft search operations.
 * This class allows customization of the search depth used in CRUD operations.
 * The depth determines how deep the search will traverse relationships.
 */
@Component
@ConfigurationProperties(prefix = "crudcraft.search")
public class CrudCraftSearchProperties {

    /**
     * The default depth for search operations.
     * This value can be overridden in the application properties.
     */
    private int depth = 1;

    /**
     * Static depth value for search operations.
     * This is used to provide a static access point for the depth
     * without requiring dependency injection.
     */
    private static int staticDepth = 1;

    /**
     * Gets the current depth for search operations.
     * This value determines how deep the search will traverse relationships.
     *
     * @return the current depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Sets the depth for search operations.
     * This value determines how deep the search will traverse relationships.
     *
     * @param depth the depth to set
     */
    public void setDepth(int depth) {
        this.depth = depth;
        setStaticDepth(depth);
    }

    /**
     * Sets the static depth value, allowing it to be accessed without dependency injection.
     * This is useful for scenarios where the properties are needed in static contexts.
     *
     * @param depth the depth to set
     */
    private static void setStaticDepth(int depth) {
        staticDepth = depth;
    }

    /**
     * Provides access to the configured depth without requiring dependency injection.
     */
    public static int getStaticDepth() {
        return staticDepth;
    }
}

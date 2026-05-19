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

package nl.datasteel.crudcraft.runtime.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * Configuration properties for CrudCraft search operations. This class allows customization of the
 * search depth used in CRUD operations. The depth determines how deep the search will traverse
 * relationships.
 */
@Component
@ConfigurationProperties(prefix = "crudcraft.search")
public class CrudCraftSearchProperties {
    /** Creates properties with default depth. */
    public CrudCraftSearchProperties() {
        // Constructor without any parameters stays empty
    }

    /**
     * The default depth for search operations. This value can be overridden in the application
     * properties.
     */
    private int depth = 1;

    /**
     * Gets the current depth for search operations. This value determines how deep the search will
     * traverse relationships.
     *
     * @return the current depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Sets the depth for search operations. This value determines how deep the search will traverse
     * relationships.
     *
     * @param depth the depth to set
     */
    public void setDepth(int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("crudcraft.search.depth must be positive");
        }
        this.depth = depth;
    }
}

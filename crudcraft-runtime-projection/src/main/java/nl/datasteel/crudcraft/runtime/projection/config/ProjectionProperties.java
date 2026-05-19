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

package nl.datasteel.crudcraft.runtime.projection.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Configuration properties for projection support.
 *
 * <p>Generated applications normally register their projection metadata registry as an
 * application-local Spring component. This fallback property is used only when no registry bean is
 * present.
 */
@ConfigurationProperties(prefix = "crudcraft.projection")
public class ProjectionProperties {

    private static final int DEFAULT_MAX_DEPTH = 5;

    private String registryFqcn =
            "nl.datasteel.crudcraft.runtime.projection.mapping.GeneratedProjectionMetadataRegistry";
    private int maxDepth = DEFAULT_MAX_DEPTH;
    private boolean warnOnCollectionHydration = true;

    /** Creates projection properties with default values. */
    public ProjectionProperties() {}

    /**
     * Returns the fully qualified class name of the projection metadata registry implementation.
     *
     * @return registry class name
     */
    public String getRegistryFqcn() {
        return registryFqcn;
    }

    /**
     * Sets the fully qualified class name of the projection metadata registry implementation.
     *
     * @param registryFqcn registry class name
     */
    public void setRegistryFqcn(String registryFqcn) {
        this.registryFqcn = registryFqcn;
    }

    /**
     * Returns the maximum nested projection metadata depth accepted by the JPA executor.
     *
     * @return maximum projection depth, never negative
     */
    public int getMaxDepth() {
        return Math.max(0, maxDepth);
    }

    /**
     * Sets the maximum nested projection metadata depth accepted by the JPA executor.
     *
     * @param maxDepth maximum depth; negative values are treated as {@code 0}
     */
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * Returns whether collection hydration should emit performance warnings.
     *
     * @return {@code true} when collection hydration warnings are enabled
     */
    public boolean isWarnOnCollectionHydration() {
        return warnOnCollectionHydration;
    }

    /**
     * Sets whether collection hydration should emit performance warnings.
     *
     * @param warnOnCollectionHydration {@code true} to log collection hydration warnings
     */
    public void setWarnOnCollectionHydration(boolean warnOnCollectionHydration) {
        this.warnOnCollectionHydration = warnOnCollectionHydration;
    }
}

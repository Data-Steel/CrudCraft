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

package nl.datasteel.crudcraft.runtime.export.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


/** Spring configuration properties for entity export safeguards. */
@ConfigurationProperties(prefix = "crudcraft.export")
public class ExportProperties {

    /** Default maximum relationship depth for export when the request does not override it. */
    public static final int DEFAULT_MAX_DEPTH = 5;
    /** Default global row cap; non-positive means format-specific caps decide. */
    public static final int DEFAULT_MAX_ROWS = -1;

    private int maxRows = DEFAULT_MAX_ROWS;
    private int maxDepth = DEFAULT_MAX_DEPTH;
    private boolean mustFetch;
    private boolean allowEntityMode;

    /** Creates export properties with production-safe defaults. */
    public ExportProperties() {}

    /**
     * Returns the global maximum row count allowed for every export format.
     *
     * @return global row cap, or non-positive when disabled
     */
    public int getMaxRows() {
        return maxRows;
    }

    /**
     * Sets the global maximum row count allowed for every export format.
     *
     * @param maxRows global row cap, or non-positive to disable the global cap
     */
    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    /**
     * Returns the default maximum relationship depth for export requests.
     *
     * @return maximum relationship depth, never negative
     */
    public int getMaxDepth() {
        return Math.max(0, maxDepth);
    }

    /**
     * Sets the default maximum relationship depth for export requests.
     *
     * @param maxDepth maximum relationship depth; negative values are treated as {@code 0}
     */
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * Returns whether failed relationship prefetching should fail the export.
     *
     * @return {@code true} to throw when prefetching fails
     */
    public boolean isMustFetch() {
        return mustFetch;
    }

    /**
     * Sets whether failed relationship prefetching should fail the export.
     *
     * @param mustFetch {@code true} to throw when prefetching fails
     */
    public void setMustFetch(boolean mustFetch) {
        this.mustFetch = mustFetch;
    }

    /**
     * Returns whether entity export mode is enabled.
     *
     * @return {@code true} when trusted entity exports are enabled
     */
    public boolean isAllowEntityMode() {
        return allowEntityMode;
    }

    /**
     * Sets whether entity export mode is enabled.
     *
     * @param allowEntityMode {@code true} to allow entity exports
     */
    public void setAllowEntityMode(boolean allowEntityMode) {
        this.allowEntityMode = allowEntityMode;
    }
}

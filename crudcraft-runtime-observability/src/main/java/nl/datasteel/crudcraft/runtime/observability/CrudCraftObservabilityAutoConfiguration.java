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

package nl.datasteel.crudcraft.runtime.observability;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;


/** Auto-configuration for CrudCraft observability support. */
@AutoConfiguration
public class CrudCraftObservabilityAutoConfiguration {

    /** Creates the auto-configuration. */
    public CrudCraftObservabilityAutoConfiguration() {
        // Constructor without any parameters stays empty.
    }

    /**
     * Provides a registry fallback for applications that have not configured Micrometer yet.
     *
     * @return observation registry
     */
    @Bean
    @ConditionalOnMissingBean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }

    /**
     * Provides CrudCraft span/observation helpers.
     *
     * @param registry observation registry
     * @return observation support
     */
    @Bean
    @ConditionalOnMissingBean
    public CrudCraftObservationSupport crudCraftObservationSupport(ObservationRegistry registry) {
        return new CrudCraftObservationSupport(registry);
    }
}

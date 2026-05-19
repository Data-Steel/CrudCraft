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

package nl.datasteel.crudcraft.starter.search;

import nl.datasteel.crudcraft.runtime.search.config.CrudCraftSearchProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


/**
 * Auto-configuration for CrudCraft search properties.
 *
 * <p>Activates property binding for generated search endpoints. Applications can override the
 * defaults with regular Spring Boot configuration:
 *
 * <pre>{@code
 * crudcraft.search.depth=2
 * }</pre>
 */
@AutoConfiguration
@EnableConfigurationProperties
public class CrudCraftSearchAutoConfiguration {

    /** Creates the search auto-configuration. */
    public CrudCraftSearchAutoConfiguration() {
        // Constructor without any parameters stays empty
    }

    /**
     * Registers search configuration properties unless the application provides its own bean.
     *
     * @return search properties
     */
    @Bean
    @ConditionalOnMissingBean(CrudCraftSearchProperties.class)
    @ConfigurationProperties(prefix = "crudcraft.search")
    public CrudCraftSearchProperties crudCraftSearchProperties() {
        return new CrudCraftSearchProperties();
    }
}

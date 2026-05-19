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

package nl.datasteel.crudcraft.starter;

import nl.datasteel.crudcraft.runtime.controller.CrudCraftExceptionHandler;
import nl.datasteel.crudcraft.runtime.service.CrudCraftStartupValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;


/**
 * Autoconfiguration for CrudCraft core web support. This configuration is only active when the
 * application is a web application.
 *
 * <p>Registers the default {@link CrudCraftExceptionHandler} for servlet applications unless the
 * application provides its own bean. Typical usage:
 *
 * <pre>{@code
 * # application.properties
 * crudcraft.api.max-page-size=100
 * }</pre>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CrudCraftAutoConfiguration {

    /** Creates the auto-configuration. */
    public CrudCraftAutoConfiguration() {
        // Constructor without any parameters stays empty
    }

    /**
     * Registers the default exception handler when none is provided by the application.
     *
     * @return default CrudCraft exception handler
     */
    @Bean
    @ConditionalOnMissingBean(CrudCraftExceptionHandler.class)
    public CrudCraftExceptionHandler crudCraftExceptionHandler() {
        return new CrudCraftExceptionHandler();
    }

    /**
     * Registers fail-fast validation for generated service collaborators.
     *
     * @param applicationContext Spring context used to discover CrudCraft services
     * @return startup validator for generated services
     */
    @Bean
    @ConditionalOnMissingBean(CrudCraftStartupValidator.class)
    public CrudCraftStartupValidator crudCraftStartupValidator(
            ApplicationContext applicationContext) {
        return new CrudCraftStartupValidator(applicationContext);
    }
}

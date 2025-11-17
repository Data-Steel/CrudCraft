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
package nl.datasteel.crudcraft.starter;

import nl.datasteel.crudcraft.runtime.config.CrudCraftSearchProperties;
import nl.datasteel.crudcraft.runtime.config.CrudCraftTomcatConfiguration;
import nl.datasteel.crudcraft.runtime.controller.CrudCraftExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Autoconfiguration for CrudCraft, enabling the exception handler, search properties,
 * and Tomcat configuration for relaxed URL validation.
 * This configuration is only active when the application is a web application (servlet type).
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({CrudCraftExceptionHandler.class, CrudCraftTomcatConfiguration.class})
@EnableConfigurationProperties(CrudCraftSearchProperties.class)
public class CrudCraftAutoConfiguration {
    // No beans needed here — importing the advice is enough
}

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
package nl.datasteel.crudcraft.starter;

import nl.datasteel.crudcraft.runtime.config.CrudCraftSearchProperties;
import nl.datasteel.crudcraft.runtime.controller.CrudCraftExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class CrudCraftAutoConfigurationTest {

    private final ApplicationContextRunner nonWebRunner =
            new ApplicationContextRunner().withUserConfiguration(CrudCraftAutoConfiguration.class);

    private final WebApplicationContextRunner webRunner =
            new WebApplicationContextRunner().withUserConfiguration(CrudCraftAutoConfiguration.class);

    private final ReactiveWebApplicationContextRunner reactiveRunner =
            new ReactiveWebApplicationContextRunner().withUserConfiguration(CrudCraftAutoConfiguration.class);

    @Test
    void autoConfigNotLoadedOutsideWebContext() {
        nonWebRunner.run(context -> {
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> context.getBean(CrudCraftExceptionHandler.class));
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> context.getBean(CrudCraftSearchProperties.class));
        });
    }

    @Test
    void autoConfigRegistersBeansInServletWebContext() {
        webRunner.run(context -> {
            assertNotNull(context.getBean(CrudCraftExceptionHandler.class));
            assertNotNull(context.getBean(CrudCraftSearchProperties.class));
        });
    }

    @Test
    void autoConfigNotLoadedInReactiveWebContext() {
        reactiveRunner.run(context -> {
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> context.getBean(CrudCraftExceptionHandler.class));
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> context.getBean(CrudCraftSearchProperties.class));
        });
    }
}

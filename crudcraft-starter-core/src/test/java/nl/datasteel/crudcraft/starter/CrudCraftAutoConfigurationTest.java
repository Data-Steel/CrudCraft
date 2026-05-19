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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class CrudCraftAutoConfigurationTest {

    private final ApplicationContextRunner nonWebRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(CrudCraftAutoConfiguration.class));

    private final WebApplicationContextRunner webRunner =
            new WebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(CrudCraftAutoConfiguration.class));

    private final ReactiveWebApplicationContextRunner reactiveRunner =
            new ReactiveWebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(CrudCraftAutoConfiguration.class));

    @Test
    void autoConfigNotLoadedOutsideWebContext() {
        nonWebRunner.run(
                context ->
                        assertThrows(
                                NoSuchBeanDefinitionException.class,
                                () -> context.getBean(CrudCraftExceptionHandler.class)));
    }

    @Test
    void autoConfigRegistersBeansInServletWebContext() {
        webRunner.run(
                context -> {
                    assertNotNull(context.getBean(CrudCraftExceptionHandler.class));
                    assertEquals(1, context.getBeansOfType(CrudCraftExceptionHandler.class).size());
                    assertNotNull(context.getBean(CrudCraftStartupValidator.class));
                    assertEquals(
                            1,
                            context.getBeansOfType(CrudCraftStartupValidator.class).size());
                });
    }

    @Test
    void autoConfigNotLoadedInReactiveWebContext() {
        reactiveRunner.run(
                context ->
                        assertThrows(
                                NoSuchBeanDefinitionException.class,
                                () -> context.getBean(CrudCraftExceptionHandler.class)));
    }

    @Test
    void keepsUserProvidedExceptionHandler() {
        webRunner
                .withUserConfiguration(CustomExceptionHandlerConfiguration.class)
                .run(
                        context -> {
                            assertInstanceOf(
                                    CustomCrudCraftExceptionHandler.class,
                                    context.getBean(CrudCraftExceptionHandler.class));
                            assertEquals(
                                    1,
                                    context.getBeansOfType(CrudCraftExceptionHandler.class).size());
                        });
    }

    @Configuration(proxyBeanMethods = false)
    public static class CustomExceptionHandlerConfiguration {

        @Bean
        public CrudCraftExceptionHandler customCrudCraftExceptionHandler() {
            return new CustomCrudCraftExceptionHandler();
        }
    }

    public static class CustomCrudCraftExceptionHandler extends CrudCraftExceptionHandler {}
}

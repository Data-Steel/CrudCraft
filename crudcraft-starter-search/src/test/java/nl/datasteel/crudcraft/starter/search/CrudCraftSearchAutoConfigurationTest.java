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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class CrudCraftSearchAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(CrudCraftSearchAutoConfiguration.class));

    @Test
    void searchAutoConfigRegistersSearchProperties() {
        contextRunner.run(
                context -> assertNotNull(context.getBean(CrudCraftSearchProperties.class)));
    }

    @Test
    void searchPropertiesBeanMissingWhenAutoConfigNotRegistered() {
        new ApplicationContextRunner()
                .run(
                        context ->
                                assertThrows(
                                        NoSuchBeanDefinitionException.class,
                                        () -> context.getBean(CrudCraftSearchProperties.class)));
    }

    @Test
    void bindsConfiguredDepthProperty() {
        contextRunner
                .withPropertyValues("crudcraft.search.depth=7")
                .run(
                        context ->
                                assertEquals(
                                        7,
                                        context.getBean(CrudCraftSearchProperties.class)
                                                .getDepth()));
    }

    @Test
    void failsOnInvalidDepthValue() {
        contextRunner
                .withPropertyValues("crudcraft.search.depth=not-a-number")
                .run(context -> assertNotNull(context.getStartupFailure()));
    }

    @Test
    void keepsUserProvidedSearchPropertiesBeanAsReplacement() {
        contextRunner
                .withUserConfiguration(CustomSearchPropertiesConfiguration.class)
                .run(
                        context -> {
                            assertEquals(
                                    CustomCrudCraftSearchProperties.class,
                                    context.getBean(CrudCraftSearchProperties.class).getClass());
                            assertEquals(
                                    1,
                                    context.getBeansOfType(CrudCraftSearchProperties.class).size());
                        });
    }

    @Configuration(proxyBeanMethods = false)
    public static class CustomSearchPropertiesConfiguration {

        @Bean
        public CrudCraftSearchProperties customCrudCraftSearchProperties() {
            return new CustomCrudCraftSearchProperties();
        }
    }

    public static class CustomCrudCraftSearchProperties extends CrudCraftSearchProperties {}
}

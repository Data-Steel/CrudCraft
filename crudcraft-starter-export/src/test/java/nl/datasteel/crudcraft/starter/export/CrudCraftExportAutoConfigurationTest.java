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

package nl.datasteel.crudcraft.starter.export;

import jakarta.persistence.EntityManager;
import nl.datasteel.crudcraft.runtime.export.EntityExportAdapter;
import nl.datasteel.crudcraft.runtime.export.EntityExportService;
import nl.datasteel.crudcraft.runtime.export.EntitySerializer;
import nl.datasteel.crudcraft.runtime.export.service.EnhancedExportServiceFactory;
import nl.datasteel.crudcraft.runtime.metadata.EntityMetadataRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;


class CrudCraftExportAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(CrudCraftExportAutoConfiguration.class));

    @Test
    void doesNotRegisterExportBeansWithoutEntityManager() {
        contextRunner.run(
                context -> {
                    assertEquals(0, context.getBeansOfType(EntityMetadataRegistry.class).size());
                    assertEquals(0, context.getBeansOfType(EntitySerializer.class).size());
                    assertEquals(0, context.getBeansOfType(EntityExportService.class).size());
                    assertEquals(0, context.getBeansOfType(EntityExportAdapter.class).size());
                    assertEquals(
                            0, context.getBeansOfType(EnhancedExportServiceFactory.class).size());
                });
    }

    @Test
    void registersExportBeansWhenEntityManagerIsPresent() {
        contextRunner
                .withUserConfiguration(EntityManagerConfiguration.class)
                .run(
                        context -> {
                            assertNotNull(context.getBean(EntityMetadataRegistry.class));
                            assertNotNull(context.getBean(EntitySerializer.class));
                            assertNotNull(context.getBean(EntityExportService.class));
                            assertNotNull(context.getBean(EntityExportAdapter.class));
                            assertNotNull(context.getBean(EnhancedExportServiceFactory.class));
                        });
    }

    @Test
    void keepsUserProvidedMetadataRegistry() {
        contextRunner
                .withUserConfiguration(
                        EntityManagerConfiguration.class, CustomMetadataRegistryConfiguration.class)
                .run(
                        context -> {
                            assertInstanceOf(
                                    CustomEntityMetadataRegistry.class,
                                    context.getBean(EntityMetadataRegistry.class));
                            assertEquals(
                                    1, context.getBeansOfType(EntityMetadataRegistry.class).size());
                            assertNotNull(context.getBean(EntitySerializer.class));
                        });
    }

    @Test
    void keepsUserProvidedEntityExportAdapter() {
        contextRunner
                .withUserConfiguration(
                        EntityManagerConfiguration.class,
                        CustomEntityExportAdapterConfiguration.class)
                .run(
                        context -> {
                            assertInstanceOf(
                                    CustomEntityExportAdapter.class,
                                    context.getBean(EntityExportAdapter.class));
                            assertEquals(
                                    1, context.getBeansOfType(EntityExportAdapter.class).size());
                        });
    }

    @Configuration(proxyBeanMethods = false)
    public static class EntityManagerConfiguration {

        @Bean
        public EntityManager entityManager() {
            return mock(EntityManager.class);
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class CustomMetadataRegistryConfiguration {

        @Bean
        public EntityMetadataRegistry entityMetadataRegistry() {
            return new CustomEntityMetadataRegistry();
        }
    }

    public static class CustomEntityMetadataRegistry extends EntityMetadataRegistry {}

    @Configuration(proxyBeanMethods = false)
    public static class CustomEntityExportAdapterConfiguration {

        @Bean
        public EntityExportAdapter entityExportAdapter(
                EntityExportService entityExportService, EntitySerializer entitySerializer) {
            return new CustomEntityExportAdapter(entityExportService, entitySerializer);
        }
    }

    public static class CustomEntityExportAdapter extends EntityExportAdapter {

        public CustomEntityExportAdapter(
                EntityExportService entityExportService, EntitySerializer entitySerializer) {
            super(entityExportService, entitySerializer);
        }
    }
}

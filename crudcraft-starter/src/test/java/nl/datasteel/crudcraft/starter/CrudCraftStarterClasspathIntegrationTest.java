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

package nl.datasteel.crudcraft.starter.bundle;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import nl.datasteel.crudcraft.starter.CrudCraftAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class CrudCraftStarterClasspathIntegrationTest {

    private static final String AUTO_CONFIGURATION_IMPORTS =
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    @Test
    void umbrellaStarterExposesAllRuntimeAutoConfigurationImports() throws Exception {
        List<String> imports = autoConfigurationImports();

        assertTrue(imports.contains("nl.datasteel.crudcraft.starter.CrudCraftAutoConfiguration"));
        assertTrue(
                imports.contains(
                        "nl.datasteel.crudcraft.starter.security."
                                + "CrudCraftSecurityAutoConfiguration"));
        assertTrue(
                imports.contains(
                        "nl.datasteel.crudcraft.starter.search."
                                + "CrudCraftSearchAutoConfiguration"));
        assertTrue(
                imports.contains(
                        "nl.datasteel.crudcraft.starter.export."
                                + "CrudCraftExportAutoConfiguration"));
        assertTrue(
                imports.contains(
                        "nl.datasteel.crudcraft.runtime.projection.config."
                                + "ProjectionAutoConfiguration"));
        assertTrue(
                imports.contains(
                        "nl.datasteel.crudcraft.starter.extensions."
                                + "CrudCraftExtensionsAutoConfiguration"));
    }

    @Test
    void umbrellaStarterDoesNotPullAnnotationProcessorsOntoRuntimeClasspath() throws Exception {
        List<URL> processorServices =
                Collections.list(
                        Thread.currentThread()
                                .getContextClassLoader()
                                .getResources("META-INF/services/javax.annotation.processing.Processor"));

        assertTrue(processorServices.isEmpty());
    }

    @Test
    void starterAutoConfigurationsStartTogetherWithoutDuplicateBeanFailures() {
        new WebApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(
                                CrudCraftAutoConfiguration.class,
                                nl.datasteel.crudcraft.starter.security
                                        .CrudCraftSecurityAutoConfiguration.class,
                                nl.datasteel.crudcraft.starter.search
                                        .CrudCraftSearchAutoConfiguration.class,
                                nl.datasteel.crudcraft.starter.export
                                        .CrudCraftExportAutoConfiguration.class,
                                nl.datasteel.crudcraft.runtime.projection.config
                                        .ProjectionAutoConfiguration.class,
                                nl.datasteel.crudcraft.starter.extensions
                                        .CrudCraftExtensionsAutoConfiguration.class))
                .run(context -> assertFalse(context.getStartupFailure() != null));
    }

    private List<String> autoConfigurationImports() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return Collections.list(classLoader.getResources(AUTO_CONFIGURATION_IMPORTS)).stream()
                .flatMap(
                        url -> {
                            try (var stream = url.openStream()) {
                                return new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                                        .lines()
                                        .map(String::trim)
                                        .filter(line -> !line.isBlank());
                            } catch (Exception e) {
                                throw new IllegalStateException(e);
                            }
                        })
                .toList();
    }
}

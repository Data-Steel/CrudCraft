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

import edu.umd.cs.findbugs.annotations.NonNull;
import jakarta.persistence.EntityManager;
import nl.datasteel.crudcraft.runtime.export.EntityExportAdapter;
import nl.datasteel.crudcraft.runtime.export.EntityExportService;
import nl.datasteel.crudcraft.runtime.export.EntitySerializer;
import nl.datasteel.crudcraft.runtime.export.service.EnhancedExportServiceFactory;
import nl.datasteel.crudcraft.runtime.metadata.EntityMetadataRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration for CrudCraft entity export functionality. This configuration must be explicitly
 * imported or component-scanned to be activated.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EntityManager.class)
@ConditionalOnBean(EntityManager.class)
@EnableConfigurationProperties(ExportProperties.class)
public class EntityExportConfiguration {
    /** Creates the configuration. */
    public EntityExportConfiguration() {
        // Constructor without any parameters stays empty
    }

    /**
     * Creates the entity metadata registry bean.
     *
     * @return the entity metadata registry
     */
    @Bean
    @ConditionalOnMissingBean(EntityMetadataRegistry.class)
    public @NonNull EntityMetadataRegistry entityMetadataRegistry() {
        return new EntityMetadataRegistry();
    }

    /**
     * Creates the entity serializer bean.
     *
     * @param metadataRegistry the metadata registry
     * @return the entity serializer
     */
    @Bean
    @ConditionalOnMissingBean(EntitySerializer.class)
    public @NonNull EntitySerializer entitySerializer(
            @NonNull EntityMetadataRegistry metadataRegistry) {
        return new EntitySerializer(metadataRegistry);
    }

    /**
     * Creates the entity export service bean.
     *
     * @param entityManager the entity manager
     * @param metadataRegistry the metadata registry
     * @param exportProperties export configuration properties
     * @return the entity export service
     */
    @Bean
    @ConditionalOnMissingBean(EntityExportService.class)
    public @NonNull EntityExportService entityExportService(
            @NonNull EntityManager entityManager,
            @NonNull EntityMetadataRegistry metadataRegistry,
            @NonNull ExportProperties exportProperties) {
        return new EntityExportService(entityManager, metadataRegistry, exportProperties);
    }

    /**
     * Creates the entity export adapter bean.
     *
     * @param entityExportService the entity export service
     * @param entitySerializer the entity serializer
     * @return the entity export adapter
     */
    @Bean
    @ConditionalOnMissingBean(EntityExportAdapter.class)
    public @NonNull EntityExportAdapter entityExportAdapter(
            @NonNull EntityExportService entityExportService,
            @NonNull EntitySerializer entitySerializer) {
        return new EntityExportAdapter(entityExportService, entitySerializer);
    }

    /**
     * Creates the generated-controller export service factory bean.
     *
     * @param entityExportAdapterProvider optional adapter provider
     * @param exportProperties export configuration properties
     * @return export service factory
     */
    @Bean
    @ConditionalOnMissingBean(EnhancedExportServiceFactory.class)
    public @NonNull EnhancedExportServiceFactory enhancedExportServiceFactory(
            @NonNull ObjectProvider<EntityExportAdapter> entityExportAdapterProvider,
            @NonNull ExportProperties exportProperties) {
        return new EnhancedExportServiceFactory(
                entityExportAdapterProvider,
                exportProperties.isAllowEntityMode(),
                exportProperties.getMaxDepth());
    }
}

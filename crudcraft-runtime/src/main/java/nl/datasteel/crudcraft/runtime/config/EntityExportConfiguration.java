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
package nl.datasteel.crudcraft.runtime.config;

import jakarta.persistence.EntityManager;
import nl.datasteel.crudcraft.runtime.export.EntityExportAdapter;
import nl.datasteel.crudcraft.runtime.export.EntityExportService;
import nl.datasteel.crudcraft.runtime.export.EntityMetadataRegistry;
import nl.datasteel.crudcraft.runtime.export.EntitySerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for CrudCraft entity export functionality.
 * This configuration is only enabled when JPA is present on the classpath.
 */
@Configuration
@ConditionalOnClass(EntityManager.class)
public class EntityExportConfiguration {
    
    /**
     * Creates the entity metadata registry bean.
     *
     * @return the entity metadata registry
     */
    @Bean
    public EntityMetadataRegistry entityMetadataRegistry() {
        return new EntityMetadataRegistry();
    }
    
    /**
     * Creates the entity serializer bean.
     *
     * @param metadataRegistry the metadata registry
     * @return the entity serializer
     */
    @Bean
    public EntitySerializer entitySerializer(EntityMetadataRegistry metadataRegistry) {
        return new EntitySerializer(metadataRegistry);
    }
    
    /**
     * Creates the entity export service bean.
     *
     * @param entityManager the entity manager
     * @param metadataRegistry the metadata registry
     * @return the entity export service
     */
    @Bean
    public EntityExportService entityExportService(EntityManager entityManager, 
                                                     EntityMetadataRegistry metadataRegistry) {
        return new EntityExportService(entityManager, metadataRegistry);
    }
    
    /**
     * Creates the entity export adapter bean.
     *
     * @param entityExportService the entity export service
     * @param entitySerializer the entity serializer
     * @return the entity export adapter
     */
    @Bean
    public EntityExportAdapter entityExportAdapter(EntityExportService entityExportService,
                                                     EntitySerializer entitySerializer) {
        return new EntityExportAdapter(entityExportService, entitySerializer);
    }
}

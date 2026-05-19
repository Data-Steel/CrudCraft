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

import jakarta.persistence.EntityManager;
import nl.datasteel.crudcraft.runtime.export.EntityExportAdapter;
import nl.datasteel.crudcraft.runtime.export.EntityExportService;
import nl.datasteel.crudcraft.runtime.export.EntitySerializer;
import nl.datasteel.crudcraft.runtime.export.service.EnhancedExportServiceFactory;
import nl.datasteel.crudcraft.runtime.metadata.EntityMetadataRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;


class EntityExportConfigurationTest {

    @Test
    void createsAllBeans() {
        EntityExportConfiguration configuration = new EntityExportConfiguration();
        EntityMetadataRegistry metadataRegistry = configuration.entityMetadataRegistry();
        EntitySerializer serializer = configuration.entitySerializer(metadataRegistry);
        EntityExportService service =
                configuration.entityExportService(
                        mock(EntityManager.class), metadataRegistry, new ExportProperties());
        EntityExportAdapter adapter = configuration.entityExportAdapter(service, serializer);
        @SuppressWarnings("unchecked")
        ObjectProvider<EntityExportAdapter> provider = mock(ObjectProvider.class);
        ExportProperties exportProperties = new ExportProperties();
        EnhancedExportServiceFactory factory =
                configuration.enhancedExportServiceFactory(provider, exportProperties);

        assertNotNull(metadataRegistry);
        assertNotNull(serializer);
        assertNotNull(service);
        assertNotNull(adapter);
        assertNotNull(factory);
    }
}

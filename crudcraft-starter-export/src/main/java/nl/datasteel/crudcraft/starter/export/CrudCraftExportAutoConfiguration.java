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
import nl.datasteel.crudcraft.runtime.export.config.EntityExportConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;


/**
 * Auto-configuration bridge that enables CrudCraft export runtime beans in Spring Boot apps.
 *
 * <p>The bridge is active only when JPA is on the classpath and an {@link EntityManager} bean is
 * present. It imports {@link EntityExportConfiguration}, which contributes metadata registry,
 * entity serializer, and enhanced export service beans. Example limits:
 *
 * <pre>{@code
 * crudcraft.export.max-csv-rows=100000
 * crudcraft.export.max-json-rows=50000
 * crudcraft.export.max-xlsx-rows=25000
 * crudcraft.export.max-depth=5
 * }</pre>
 */
@AutoConfiguration
@ConditionalOnClass(EntityManager.class)
@ConditionalOnBean(EntityManager.class)
@Import(EntityExportConfiguration.class)
public class CrudCraftExportAutoConfiguration {

    /** Creates the export auto-configuration bridge. */
    public CrudCraftExportAutoConfiguration() {
        // Constructor without any parameters stays empty
    }
}

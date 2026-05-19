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

package nl.datasteel.crudcraft.starter.extensions;

import nl.datasteel.crudcraft.runtime.extensions.AuditingRuntimeExtension;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;


/** Auto-configuration for CrudCraft runtime extension hooks. */
@AutoConfiguration
@ConditionalOnClass(AuditingRuntimeExtension.class)
public class CrudCraftExtensionsAutoConfiguration {

    /** Creates the extensions auto-configuration. */
    public CrudCraftExtensionsAutoConfiguration() {
        // Constructor without any parameters stays empty
    }

    /**
     * Registers the supported audit timestamp runtime hook.
     *
     * @return audit runtime extension
     */
    @Bean
    @ConditionalOnMissingBean(AuditingRuntimeExtension.class)
    public AuditingRuntimeExtension auditingRuntimeExtension() {
        return new AuditingRuntimeExtension();
    }
}

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

package nl.datasteel.crudcraft.starter.security;

import nl.datasteel.crudcraft.annotations.security.FieldSecurityAdapter;
import nl.datasteel.crudcraft.runtime.security.DefaultFieldSecurityAdapter;
import nl.datasteel.crudcraft.runtime.security.FieldSecurityRuntimeExtension;
import nl.datasteel.crudcraft.runtime.security.scope.PrincipalScopeAccessor;
import nl.datasteel.crudcraft.runtime.security.scope.SpringSecurityPrincipalScopeAccessor;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;


/**
 * Auto-configuration for CrudCraft security integration.
 *
 * <p>Registers the default field-security adapter, principal-scope accessor, and runtime extension
 * when the application has not supplied replacements. Applications with custom field-security
 * rules should expose exactly one {@link FieldSecurityAdapter} bean; Spring Boot will then skip the
 * default adapter and wire the custom bean into the runtime extension.
 *
 * <pre>{@code
 * @Bean
 * FieldSecurityAdapter fieldSecurityAdapter() {
 *     return new MyFieldSecurityAdapter();
 * }
 * }</pre>
 */
@AutoConfiguration
public class CrudCraftSecurityAutoConfiguration {

    /** Creates the security auto-configuration. */
    public CrudCraftSecurityAutoConfiguration() {
        // Constructor without any parameters stays empty
    }

    /**
     * Provides the default field security adapter when no custom bean exists.
     *
     * @return a field security adapter
     */
    @Bean
    @ConditionalOnMissingBean(FieldSecurityAdapter.class)
    public FieldSecurityAdapter fieldSecurityAdapter() {
        return new DefaultFieldSecurityAdapter();
    }

    /**
     * Provides the principal scope accessor when no custom bean exists.
     *
     * @return a principal scope accessor
     */
    @Bean
    @ConditionalOnMissingBean(PrincipalScopeAccessor.class)
    public PrincipalScopeAccessor principalScopeAccessor() {
        return new SpringSecurityPrincipalScopeAccessor();
    }

    /**
     * Provides the Spring Security exception mapper when no custom mapper exists.
     *
     * @return a security exception handler
     */
    @Bean
    @ConditionalOnMissingBean(CrudCraftSecurityExceptionHandler.class)
    public CrudCraftSecurityExceptionHandler crudCraftSecurityExceptionHandler() {
        return new CrudCraftSecurityExceptionHandler();
    }

    /**
     * Exposes the security runtime extension for field-level security.
     *
     * @param fieldSecurityAdapter adapter used by the extension
     * @return the runtime extension bean
     */
    @Bean
    @ConditionalOnMissingBean(name = "crudCraftFieldSecurityRuntimeExtension")
    public CrudRuntimeExtension<?, ?> crudCraftFieldSecurityRuntimeExtension(
            FieldSecurityAdapter fieldSecurityAdapter) {
        if (fieldSecurityAdapter == null) {
            throw new IllegalArgumentException("fieldSecurityAdapter must not be null");
        }
        return new FieldSecurityRuntimeExtension<>(fieldSecurityAdapter);
    }
}

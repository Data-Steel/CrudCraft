/*
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.annotations.classes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.annotations.security.policy.PermitAllSecurityPolicy;

/**
 * Annotation to mark a class as a CrudCrafted entity. When applied,
 * CrudCraft will generate a complete CRUD implementation for the entity.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CrudCrafted {

    /**
     * When true, we generate a concrete stub
     * extending the abstract base (once).
     */
    boolean editable() default false;

    /**
     * If set, all the generated code (dtos, repos, services, controllers, etc.) will
     * be rooted at this package instead of the entity’s own package.
     */
    String basePackage() default "";

    /**
     * The CrudTemplate to use for generating endpoints.
     * Defaults to FULL, which generates all CRUD operations.
     */
    CrudTemplate template() default CrudTemplate.FULL;

    /**
     * Used for omitting endpoints that are not needed, but are included in the specified template.
     */
    CrudEndpoint[] omitEndpoints() default {};

    /**
     * Used for generating additional code.
     * This can be used to add endpoints that are not part of the specified template.
     */
    CrudEndpoint[] includeEndpoints() default {};

    /**
     * Used for user-defined policies that can be used to control which endpoints are generated.
     */
    Class<? extends CrudEndpointPolicy> endpointPolicy() default CrudTemplate.class;

    /**
     * Toggle table/endpoint security. When {@code false}, no @PreAuthorize wrappers are generated.
     */
    boolean secure() default true;

    /**
     * The table-level security policy to use for generating @PreAuthorize expressions.
     */
    Class<? extends CrudSecurityPolicy> securityPolicy() default PermitAllSecurityPolicy.class;
}

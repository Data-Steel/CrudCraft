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

package nl.datasteel.crudcraft.annotations.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/** Annotation-first RBAC configuration for generated CRUD endpoints. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CrudSecurity {

    /**
     * Roles used for read-like endpoints.
     *
     * @return roles allowed for read endpoints
     */
    String[] readRoles() default {};

    /**
     * Roles used for write-like endpoints.
     *
     * @return roles allowed for write endpoints
     */
    String[] writeRoles() default {};

    /**
     * Roles used for delete-like endpoints.
     *
     * @return roles allowed for delete endpoints
     */
    String[] deleteRoles() default {};

    /**
     * Endpoint-specific role overrides.
     *
     * @return endpoint-specific RBAC overrides
     */
    EndpointRbac[] endpoints() default {};
}

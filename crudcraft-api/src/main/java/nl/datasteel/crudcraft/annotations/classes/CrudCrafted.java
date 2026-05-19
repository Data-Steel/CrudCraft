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

package nl.datasteel.crudcraft.annotations.classes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;


/**
 * Marks an entity class for CrudCraft source generation.
 *
 * <p>This annotation is source-retained because it controls code generation in the current
 * compilation round and is not meant to be inspected by runtime modules. It targets entity classes
 * only; field-level generation options use the annotations in {@code
 * nl.datasteel.crudcraft.annotations.fields}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CrudCrafted {

    /**
     * Controls whether editable concrete stubs are generated once.
     *
     * @return {@code true} when editable stubs are generated
     */
    boolean editable() default false;

    /**
     * Overrides the base package for generated artifacts.
     *
     * @return root package for generated classes
     */
    String basePackage() default "";

    /**
     * Selects the standard CRUD template.
     *
     * @return selected template
     */
    CrudTemplate template() default CrudTemplate.FULL;

    /**
     * Excludes endpoints that are otherwise part of the selected template. An endpoint must not
     * also appear in {@link #includeEndpoints()}; the annotation processor rejects contradictory
     * overlays at compile time.
     *
     * @return endpoints to omit
     */
    CrudEndpoint[] omitEndpoints() default {};

    /**
     * Includes additional endpoints beyond the selected template. An endpoint must not also appear
     * in {@link #omitEndpoints()}; the annotation processor rejects contradictory overlays at
     * compile time.
     *
     * @return additional endpoints to include
     */
    CrudEndpoint[] includeEndpoints() default {};

    /**
     * Applies a custom endpoint policy for endpoint generation decisions.
     *
     * @return endpoint policy type
     */
    Class<? extends CrudEndpointPolicy> endpointPolicy() default CrudTemplate.class;

    /**
     * Enables generated endpoint and table security wrappers.
     *
     * @return {@code true} when security wrappers are generated
     */
    boolean secure() default false;

    /**
     * Defines the table-level security policy for generated expressions.
     *
     * @return security policy type
     */
    Class<? extends CrudSecurityPolicy> securityPolicy() default CrudSecurityPolicy.class;
}

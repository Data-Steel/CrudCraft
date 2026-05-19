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

package nl.datasteel.crudcraft.annotations;

import java.util.Set;


/**
 * Compile-time policy for deciding which CRUD endpoints are generated.
 *
 * <p>A policy returns the complete endpoint set for a template or custom endpoint profile. The
 * annotation processor evaluates the selected template or policy first, then applies per-model
 * include and omit options from {@code @CrudCrafted}. Endpoints present after that resolution are
 * generated; endpoints absent from the final set are not emitted.
 *
 * <p>This is not a runtime authorization mechanism. Runtime access control belongs in CrudCraft
 * security annotations and {@link nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy}.
 * Omitting an endpoint prevents the controller method from existing at all, while security
 * expressions protect generated methods that do exist.
 *
 * <p>Implementations should be stateless and deterministic. Return a defensive or immutable
 * {@link Set}; callers may retain the returned set while generating source. Do not return {@code
 * null}, and do not mutate the returned set after exposing it.
 *
 * @see CrudTemplate
 * @see CrudEndpoint
 */
public interface CrudEndpointPolicy {

    /**
     * Resolves the endpoints enabled by this policy before model-level include/omit options are
     * applied.
     *
     * @return a non-null set of {@link CrudEndpoint} instances enabled by the policy
     */
    Set<CrudEndpoint> resolveEndpoints();

    /**
     * Returns the policy name used in generated source and diagnostics.
     *
     * @return stable, non-blank policy name
     */
    String name();
}

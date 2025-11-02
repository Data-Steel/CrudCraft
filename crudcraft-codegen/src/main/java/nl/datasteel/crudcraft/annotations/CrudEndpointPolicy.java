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
package nl.datasteel.crudcraft.annotations;

import java.util.Set;

/**
 * Interface for defining policies that resolve CRUD endpoints.
 * Implementations of this interface should provide a way to resolve
 * a set of CRUD endpoints based on specific criteria or configurations.
 */
public interface CrudEndpointPolicy {

    /**
     * Resolves a set of CRUD endpoints based on the policy's criteria.
     *
     * @return a set of {@link CrudEndpoint} instances that match the policy.
     */
    Set<CrudEndpoint> resolveEndpoints();

    /**
     * Returns the name of the policy.
     * This name is used to identify the policy in generated code.
     *
     * @return the name of the policy.
     */
    String name();
}
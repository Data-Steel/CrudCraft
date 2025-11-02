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
package nl.datasteel.crudcraft.annotations.security.policy;

import java.util.EnumMap;
import java.util.Map;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;

/**
 * Allows configuring roles per CRUD endpoint.
 */
public class RoleBasedCrudSecurityPolicy implements CrudSecurityPolicy {

    /**
     * Maps each CRUD endpoint to a role required for access.
     */
    private final Map<CrudEndpoint, String> endpointRoles = new EnumMap<>(CrudEndpoint.class);

    /**
     * Default constructor for serialization frameworks.
     */
    public RoleBasedCrudSecurityPolicy() {
    }

    /**
     * Constructs a policy with specific roles for each CRUD endpoint.
     *
     * @param roles a map of CRUD endpoints to their required roles
     */
    public RoleBasedCrudSecurityPolicy(Map<CrudEndpoint, String> roles) {
        this.endpointRoles.putAll(roles);
    }

    /**
     * Retrieves the role required for a specific CRUD endpoint.
     */
    @Override
    public String getSecurityExpression(CrudEndpoint endpoint) {
        String role = endpointRoles.get(endpoint);
        return role == null ? "denyAll()" : "hasRole('" + role + "')";
    }
}

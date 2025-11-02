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
package nl.datasteel.crudcraft.annotations.security;

import nl.datasteel.crudcraft.annotations.CrudEndpoint;

/**
 * Determines the Spring Security expression to apply for a specific CRUD endpoint.
 * Implementations may return SpEL expressions like {@code "hasRole('ADMIN')"}.
 */
public interface CrudSecurityPolicy {

    /**
     * Resolve the security expression for the given endpoint.
     *
     * @param endpoint the CRUD endpoint being secured
     * @return a Spring Security expression, e.g. {@code "permitAll()"}
     */
    String getSecurityExpression(CrudEndpoint endpoint);
}

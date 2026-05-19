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

import nl.datasteel.crudcraft.annotations.CrudEndpoint;


/**
 * Resolves the Spring Security expression used to protect generated CRUD endpoints.
 *
 * <p>This is a code-generation extension point. The annotation processor instantiates the policy
 * while generating controllers and calls {@link #getSecurityExpression(CrudEndpoint)} once for
 * each generated endpoint. The returned expression is written into the generated controller
 * security annotation and evaluated later by Spring Security for each HTTP request.
 *
 * <p>Implementations should be stateless and thread-safe. Annotation processors may reuse policy
 * instances while compiling multiple annotated models, and generated applications may compile in
 * parallel. Store configuration in immutable fields only, and do not depend on request-scoped
 * state here. Runtime state belongs in Spring Security beans referenced by the returned expression.
 *
 * <p>The expression must be a valid Spring Security SpEL expression such as {@code "permitAll()"},
 * {@code "isAuthenticated()"}, or {@code "hasRole('ADMIN')"}. Return a non-null, non-blank value;
 * invalid expressions fail when the generated endpoint is invoked.
 *
 * @see EndpointRbac
 * @see CrudSecurity
 */
@FunctionalInterface
public interface CrudSecurityPolicy {

    /**
     * Resolve the security expression for the given endpoint.
     *
     * <p>The {@code endpoint} argument is never null for framework calls. Implementations should
     * throw {@link IllegalArgumentException} for unsupported endpoints rather than silently
     * returning a permissive expression.
     *
     * @param endpoint the CRUD endpoint being secured
     * @return a Spring Security expression, e.g. {@code "permitAll()"}
     */
    String getSecurityExpression(CrudEndpoint endpoint);
}

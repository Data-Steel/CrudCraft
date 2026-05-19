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

package nl.datasteel.crudcraft.codegen.reader.model;

import java.util.EnumMap;
import java.util.Map;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;


/** Immutable policy backed by endpoint expression map resolved during code generation. */
public class ResolvedCrudSecurityPolicy implements CrudSecurityPolicy {

    private final Map<CrudEndpoint, String> expressions;

    /**
     * Creates a resolved policy from endpoint expressions.
     *
     * @param expressions endpoint-to-expression mapping
     */
    public ResolvedCrudSecurityPolicy(Map<CrudEndpoint, String> expressions) {
        this.expressions = new EnumMap<>(expressions);
    }

    /**
     * Returns the security expression for the requested endpoint.
     *
     * @param endpoint the CRUD endpoint to resolve
     * @return the configured security expression, or {@code denyAll()}
     */
    @Override
    public String getSecurityExpression(CrudEndpoint endpoint) {
        return expressions.getOrDefault(endpoint, "denyAll()");
    }
}

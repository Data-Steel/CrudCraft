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

package nl.datasteel.crudcraft.codegen.descriptor.model.part;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;


/** Security-related configuration for a model. */
public final class ModelSecurity {
    private final boolean secure;
    private final Class<? extends CrudSecurityPolicy> securityPolicy;
    private final List<String> rowSecurityHandlers;
    private final List<RowScope> rowScopes;
    private final Map<CrudEndpoint, String> endpointExpressions;

    /**
     * Backward-compatible constructor.
     *
     * @param secure whether the model endpoints require security
     * @param securityPolicy security policy class
     * @param rowSecurityHandlers row security handler class names
     */
    public ModelSecurity(
            boolean secure,
            Class<? extends CrudSecurityPolicy> securityPolicy,
            List<String> rowSecurityHandlers) {
        this(secure, securityPolicy, rowSecurityHandlers, List.of(), Map.of());
    }

    /**
     * Creates security configuration for a model.
     *
     * @param secure whether the model endpoints require security
     * @param securityPolicy the class handling security policies
     * @param rowSecurityHandlers optional row-level security handler class names
     * @param rowScopes optional built-in scope declarations
     * @param endpointExpressions optional pre-resolved per-endpoint expressions
     */
    public ModelSecurity(
            boolean secure,
            Class<? extends CrudSecurityPolicy> securityPolicy,
            List<String> rowSecurityHandlers,
            List<RowScope> rowScopes,
            Map<CrudEndpoint, String> endpointExpressions) {
        this.secure = secure;
        this.securityPolicy = securityPolicy;
        this.rowSecurityHandlers =
                rowSecurityHandlers == null ? List.of() : List.copyOf(rowSecurityHandlers);
        this.rowScopes = rowScopes == null ? List.of() : List.copyOf(rowScopes);
        if (endpointExpressions == null || endpointExpressions.isEmpty()) {
            this.endpointExpressions = Map.of();
        } else {
            this.endpointExpressions =
                    Collections.unmodifiableMap(new EnumMap<>(endpointExpressions));
        }
    }

    /**
     * Returns true if the model endpoints require security.
     *
     * @return true if secure
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Returns whether the model endpoints require security.
     *
     * @return {@code true} if security is enabled
     */
    public boolean secure() {
        return secure;
    }

    /**
     * Returns the class that handles security policies for this model.
     *
     * @return the security policy class
     */
    public Class<? extends CrudSecurityPolicy> getSecurityPolicy() {
        return securityPolicy;
    }

    /**
     * Returns the configured security policy type.
     *
     * @return the security policy class
     */
    public Class<? extends CrudSecurityPolicy> securityPolicy() {
        return securityPolicy;
    }

    /**
     * Returns the class names that handle row-level security for this model, if any.
     *
     * @return list of fully qualified row security handler class names
     */
    public List<String> getRowSecurityHandlers() {
        return List.copyOf(rowSecurityHandlers);
    }

    /**
     * Returns the configured row security handlers.
     *
     * @return row security handler class names
     */
    public List<String> rowSecurityHandlers() {
        return List.copyOf(rowSecurityHandlers);
    }

    /**
     * Returns built-in row scope declarations.
     *
     * @return row scope declarations
     */
    public List<RowScope> getRowScopes() {
        return List.copyOf(rowScopes);
    }

    /**
     * Returns the configured row scopes.
     *
     * @return row scope declarations
     */
    public List<RowScope> rowScopes() {
        return List.copyOf(rowScopes);
    }

    /**
     * Returns endpoint expressions resolved during model extraction.
     *
     * @return endpoint expression map
     */
    public Map<CrudEndpoint, String> getEndpointExpressions() {
        return Map.copyOf(endpointExpressions);
    }

    /**
     * Returns the configured endpoint expressions.
     *
     * @return endpoint expression map
     */
    public Map<CrudEndpoint, String> endpointExpressions() {
        return Map.copyOf(endpointExpressions);
    }

    /**
     * Returns true when endpoint expressions were resolved from @CrudSecurity.
     *
     * @return {@code true} when endpoint expressions exist
     */
    public boolean hasEndpointExpressions() {
        return !endpointExpressions.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModelSecurity that)) {
            return false;
        }
        return secure == that.secure
                && Objects.equals(securityPolicy, that.securityPolicy)
                && Objects.equals(rowSecurityHandlers, that.rowSecurityHandlers)
                && Objects.equals(rowScopes, that.rowScopes)
                && Objects.equals(endpointExpressions, that.endpointExpressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                secure, securityPolicy, rowSecurityHandlers, rowScopes, endpointExpressions);
    }

    @Override
    public String toString() {
        return "ModelSecurity{"
                + "secure="
                + secure
                + ", securityPolicy="
                + securityPolicy
                + ", rowSecurityHandlers="
                + rowSecurityHandlers
                + ", rowScopes="
                + rowScopes
                + ", endpointExpressions="
                + endpointExpressions
                + '}';
    }
}

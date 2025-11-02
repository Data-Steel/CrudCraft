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
package nl.datasteel.crudcraft.codegen.descriptor.model;

import java.util.List;
import java.util.Objects;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;

/**
 * Metadata for a model composed of distinct descriptor parts.
 */
public class ModelDescriptor {

    /**
     * Represents the identity of the model, including its name, package, and fields.
     */
    private final ModelIdentity identity;

    /**
     * Flags indicating various properties of the model,
     * such as whether it is editable or embeddable.
     */
    private final ModelFlags flags;

    /**
     * Options for endpoints associated with the model, including templates and policies.
     */
    private final EndpointOptions endpoints;

    /**
     * Security options for the model, defining access control and security policies.
     */
    private final ModelSecurity security;

    /**
     * Constructs a ModelDescriptor with the provided identity, flags,
     * endpoints, and security options.
     *
     * @param identity the core identity of the model
     * @param flags the flags indicating properties of the model
     * @param endpoints the options for endpoints associated with the model
     * @param security the security options for the model
     */
    public ModelDescriptor(ModelIdentity identity,
                           ModelFlags flags,
                           EndpointOptions endpoints,
                           ModelSecurity security) {
        this.identity = Objects.requireNonNull(identity);
        this.flags = Objects.requireNonNull(flags);
        this.endpoints = Objects.requireNonNull(endpoints);
        this.security = Objects.requireNonNull(security);
    }

    // ────────────────────── Convenience getters ──────────────────────

    /**
     * Returns the name of the model, which is used for generating code and identifying the model.
     *
     * @return the name of the model as a String
     */
    public String getName() {
        return identity.getName();
    }

    /**
     * Returns the package name of the model,
     * which is used for organizing code and avoiding naming conflicts.
     */
    public String getPackageName() {
        return identity.getPackageName();
    }

    /**
     * Returns the base package of the model, which is used for generating code.
     * This is typically the package where the model's classes are located.
     *
     * @return the base package as a String
     */
    public String getBasePackage() {
        return identity.getBasePackage();
    }

    /**
     * Returns the fields of the model, which are defined in the ModelIdentity.
     *
     * @return a list of FieldDescriptor objects representing the fields of the model
     */
    public List<FieldDescriptor> getFields() {
        return identity.getFields();
    }

    /**
     * Returns the identity of the model, which includes its name, package, and fields.
     *
     * @return the ModelIdentity object representing the model's identity
     */
    public boolean isEditable() {
        return flags.isEditable();
    }

    /**
     * Returns whether the model is a CrudCraft entity,
     * meaning it is part of the CrudCraft framework.
     *
     * @return true if the model is a CrudCraft entity, false otherwise
     */
    public boolean isCrudCraftEntity() {
        return flags.isCrudCraftEntity();
    }

    /**
     * Returns whether the model is embeddable, meaning it can be used as a part of another entity.
     *
     * @return true if the model is embeddable, false otherwise
     */
    public boolean isEmbeddable() {
        return flags.isEmbeddable();
    }

    /**
     * Returns whether the model is an abstract class.
     *
     * @return true if the model is abstract, false otherwise
     */
    public boolean isAbstract() {
        return flags.isAbstract();
    }

    /**
     * Returns the template used for generating endpoints for this model.
     *
     * @return the CrudTemplate object representing the template
     */
    public CrudTemplate getTemplate() {
        return endpoints.getTemplate();
    }

    /**
     * Returns the endpoints that should be omitted during controller generation.
     *
     * @return an array of omitted CrudEndpoint objects
     */
    public CrudEndpoint[] getOmitEndpoints() {
        return endpoints.getOmitEndpoints();
    }

    /**
     * Returns the endpoints that should be included during controller generation.
     *
     * @return an array of included CrudEndpoint objects
     */
    public CrudEndpoint[] getIncludeEndpoints() {
        return endpoints.getIncludeEndpoints();
    }

    /**
     * Returns the custom endpoint policy, if defined.
     */
    public Class<? extends CrudEndpointPolicy> getEndpointPolicy() {
        return endpoints.getEndpointPolicy();
    }

    /**
     * Returns whether the model is secure, meaning it has a defined security policy.
     *
     * @return true if the model is secure, false otherwise
     */
    public boolean isSecure() {
        return security.isSecure();
    }

    /**
     * Returns the class that implements the security policy for this model.
     *
     * @return the class implementing CrudSecurityPolicy for this model
     */
    public Class<? extends CrudSecurityPolicy> getSecurityPolicy() {
        return security.getSecurityPolicy();
    }

    /**
     * Returns the class names that handle row-level security for this model.
     *
     * @return list of fully qualified row security handler class names
     */
    public List<String> getRowSecurityHandlers() {
        return security.getRowSecurityHandlers();
    }

    /**
     * Returns the identity of the model, which includes its name, package, and fields.
     *
     * @return the ModelIdentity object representing the model's identity
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModelDescriptor other)) {
            return false;
        }
        return Objects.equals(identity, other.identity)
                && Objects.equals(flags, other.flags)
                && Objects.equals(endpoints, other.endpoints)
                && Objects.equals(security, other.security);
    }


    /**
     * Computes a hash code for the ModelDescriptor based on its identity, flags,
     * endpoints, and security options.
     *
     * @return an integer hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(identity, flags, endpoints, security);
    }


    /**
     * Returns a string representation of the ModelDescriptor.
     *
     * @return a string containing the model's identity, flags, endpoints, and security options
     */
    @Override
    public String toString() {
        return "ModelDescriptor{"
                + "identity=" + identity
                + ", flags=" + flags
                + ", endpoints=" + endpoints
                + ", security=" + security
                + '}';
    }
}

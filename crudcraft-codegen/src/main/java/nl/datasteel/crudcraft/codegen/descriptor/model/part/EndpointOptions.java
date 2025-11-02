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

package nl.datasteel.crudcraft.codegen.descriptor.model.part;

import java.util.Arrays;
import java.util.Objects;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;

/**
 * Options related to endpoint generation for a model.
 *
 * @param template the base endpoint template
 * @param omitEndpoints endpoints to omit
 * @param includeEndpoints endpoints to explicitly include
 * @param endpointPolicy custom policy class resolving endpoints
 */
public record EndpointOptions(CrudTemplate template,
                              CrudEndpoint[] omitEndpoints, CrudEndpoint[] includeEndpoints,
                              Class<? extends CrudEndpointPolicy> endpointPolicy) {

    /**
     * Immutable constructor for EndpointOptions.
     */
    public EndpointOptions {
        omitEndpoints = omitEndpoints == null ? new CrudEndpoint[0] :
                Arrays.copyOf(omitEndpoints, omitEndpoints.length);
        includeEndpoints = includeEndpoints == null ? new CrudEndpoint[0] :
                Arrays.copyOf(includeEndpoints, includeEndpoints.length);
    }

    /**
     * Returns the base endpoint template to use for this model.
     *
     * @return the CrudTemplate to use
     */
    public CrudTemplate getTemplate() {
        return template;
    }

    /**
     * Safe, defensive accessor for omitEndpoints.
     */
    @Override
    public CrudEndpoint[] omitEndpoints() {
        return Arrays.copyOf(omitEndpoints, omitEndpoints.length);
    }

    /**
     * Returns the endpoints that should be omitted for this model.
     *
     * @return array of CrudEndpoint to omit
     */
    public CrudEndpoint[] getOmitEndpoints() {
        return Arrays.copyOf(omitEndpoints, omitEndpoints.length);
    }

    /**
     * Safe, defensive accessor for includeEndpoints.
     */
    @Override
    public CrudEndpoint[] includeEndpoints() {
        return Arrays.copyOf(includeEndpoints, includeEndpoints.length);
    }

    /**
     * Returns the endpoints that should be explicitly included for this model.
     *
     * @return array of CrudEndpoint to include
     */
    public CrudEndpoint[] getIncludeEndpoints() {
        return Arrays.copyOf(includeEndpoints, includeEndpoints.length);
    }

    /**
     * Returns the custom endpoint policy class for this model.
     *
     * @return the CrudEndpointPolicy class
     */
    public Class<? extends CrudEndpointPolicy> getEndpointPolicy() {
        return endpointPolicy;
    }

    /**
     * Checks if the EndpointOptions is equal to another object.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof EndpointOptions(
                    CrudTemplate otherTemplate,
                    CrudEndpoint[] otherOmit,
                    CrudEndpoint[] otherInclude,
                    Class<? extends CrudEndpointPolicy> otherPolicy
                )
                && template == otherTemplate
                && Arrays.equals(omitEndpoints, otherOmit)
                && Arrays.equals(includeEndpoints, otherInclude)
                && Objects.equals(endpointPolicy, otherPolicy);
    }

    /**
     * Computes the hash code for the EndpointOptions.
     *
     * @return the hash code based on template, omitEndpoints, includeEndpoints, and endpointPolicy
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(template, endpointPolicy);
        result = 31 * result + Arrays.hashCode(omitEndpoints);
        result = 31 * result + Arrays.hashCode(includeEndpoints);
        return result;
    }

    /**
     * Returns a string representation of the EndpointOptions.
     *
     * @return a string describing the EndpointOptions
     */
    @Override
    public String toString() {
        return "EndpointOptions{"
                + "template=" + template
                + ", omitEndpoints=" + Arrays.toString(omitEndpoints)
                + ", includeEndpoints=" + Arrays.toString(includeEndpoints)
                + ", endpointPolicy=" + endpointPolicy
                + '}';
    }
}

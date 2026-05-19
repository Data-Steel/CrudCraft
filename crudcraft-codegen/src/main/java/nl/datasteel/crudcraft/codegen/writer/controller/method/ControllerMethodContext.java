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

package nl.datasteel.crudcraft.codegen.writer.controller.method;

import com.palantir.javapoet.MethodSpec;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;


/**
 * Context object passed to individual method components during generation.
 *
 * @param builder method builder under construction
 * @param model model descriptor
 * @param spec endpoint specification
 * @param securityPolicy resolved security policy
 */
public record ControllerMethodContext(
        MethodSpec.Builder builder,
        ModelDescriptor model,
        EndpointSpec spec,
        CrudSecurityPolicy securityPolicy) {
    /**
     * Returns the endpoint that is currently being generated.
     *
     * @return endpoint identifier
     */
    public CrudEndpoint endpoint() {
        return spec.endpoint();
    }
}

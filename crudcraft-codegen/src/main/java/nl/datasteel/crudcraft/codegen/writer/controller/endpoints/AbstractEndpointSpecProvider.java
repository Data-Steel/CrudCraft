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

package nl.datasteel.crudcraft.codegen.writer.controller.endpoints;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import java.util.Objects;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;


/**
 * Base class for endpoint specification providers.
 *
 * <p>Endpoint classes differ in HTTP mapping, parameters, and body generation, but they share
 * model validation and DTO type naming conventions. Centralising those helpers keeps new endpoint
 * providers consistent without hiding the generated method body from reviewers.
 */
abstract class AbstractEndpointSpecProvider implements EndpointSpecProvider {

    /**
     * Validates a model descriptor before endpoint generation.
     *
     * @param modelDescriptor descriptor to validate
     * @return the same descriptor for fluent use
     */
    protected final ModelDescriptor requireModel(ModelDescriptor modelDescriptor) {
        return Objects.requireNonNull(modelDescriptor, "modelDescriptor must not be null");
    }

    /**
     * Returns the generated full response DTO type for the model.
     *
     * @param modelDescriptor model descriptor
     * @return response DTO class name
     */
    protected final ClassName responseDto(ModelDescriptor modelDescriptor) {
        ModelDescriptor md = requireModel(modelDescriptor);
        return ClassName.get(md.getPackageName() + ".dto.response", md.getName() + "ResponseDto");
    }

    /**
     * Returns the generated request DTO type for the model.
     *
     * @param modelDescriptor model descriptor
     * @return request DTO class name
     */
    protected final ClassName requestDto(ModelDescriptor modelDescriptor) {
        ModelDescriptor md = requireModel(modelDescriptor);
        return ClassName.get(md.getPackageName() + ".dto.request", md.getName() + "RequestDto");
    }

    /**
     * Wraps a type in Spring's {@code ResponseEntity}.
     *
     * @param type response body type
     * @return response entity parameterized type
     */
    protected final ParameterizedTypeName responseEntity(com.palantir.javapoet.TypeName type) {
        return ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, type);
    }
}

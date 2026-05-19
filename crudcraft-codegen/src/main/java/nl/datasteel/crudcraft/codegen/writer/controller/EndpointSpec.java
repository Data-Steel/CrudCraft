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

package nl.datasteel.crudcraft.codegen.writer.controller;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeName;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;


/** Describes how to generate a single controller endpoint. */
public final class EndpointSpec {
    private final CrudEndpoint endpoint;
    private final String methodName;
    private final Function<ModelDescriptor, AnnotationSpec> mapping;
    private final Function<ModelDescriptor, TypeName> returnType;
    private final List<Function<ModelDescriptor, ParameterSpec>> params;
    private final BiConsumer<com.palantir.javapoet.MethodSpec.Builder, ModelDescriptor> body;

    /**
     * Creates an endpoint specification.
     *
     * @param endpoint endpoint identifier
     * @param methodName generated method name
     * @param mapping mapping annotation factory
     * @param returnType return type factory
     * @param params parameter factory list
     * @param body method body writer
     */
    public EndpointSpec(
            CrudEndpoint endpoint,
            String methodName,
            Function<ModelDescriptor, AnnotationSpec> mapping,
            Function<ModelDescriptor, TypeName> returnType,
            List<Function<ModelDescriptor, ParameterSpec>> params,
            BiConsumer<com.palantir.javapoet.MethodSpec.Builder, ModelDescriptor> body) {
        this.endpoint = endpoint;
        this.methodName = methodName;
        this.mapping = mapping;
        this.returnType = returnType;
        this.params = params == null ? List.of() : List.copyOf(params);
        this.body = body;
    }

    /**
     * Returns the endpoint identifier.
     *
     * @return endpoint identifier
     */
    public CrudEndpoint endpoint() {
        return endpoint;
    }

    /**
     * Returns the generated method name.
     *
     * @return method name
     */
    public String methodName() {
        return methodName;
    }

    /**
     * Returns the mapping annotation factory.
     *
     * @return mapping annotation factory
     */
    public Function<ModelDescriptor, AnnotationSpec> mapping() {
        return mapping;
    }

    /**
     * Returns the return type factory.
     *
     * @return return type factory
     */
    public Function<ModelDescriptor, TypeName> returnType() {
        return returnType;
    }

    /**
     * Returns the parameter factory list.
     *
     * @return parameter factory list
     */
    public List<Function<ModelDescriptor, ParameterSpec>> params() {
        return List.copyOf(params);
    }

    /**
     * Returns the method body writer.
     *
     * @return method body writer
     */
    public BiConsumer<com.palantir.javapoet.MethodSpec.Builder, ModelDescriptor> body() {
        return body;
    }
}

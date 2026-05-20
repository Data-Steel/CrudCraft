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

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import java.util.function.Function;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;


/** Reusable parameter shapes shared by generated endpoint methods. */
enum EndpointParameterTemplates {
    PATH_ID {
        @Override
        Function<ModelDescriptor, ParameterSpec> create(ClassName requestDto) {
            return modelDescriptor ->
                    ParameterSpec.builder(
                                    EndpointSupport.resolveModelIdType(modelDescriptor), "id")
                            .addAnnotation(
                                    AnnotationSpec.builder(EndpointSupport.PATH_VAR)
                                            .addMember("value", "$S", "id")
                                            .build())
                            .build();
        }
    },

    REQUEST_BODY {
        @Override
        Function<ModelDescriptor, ParameterSpec> create(ClassName requestDto) {
            return modelDescriptor ->
                    EndpointSupport.withModel(
                            modelDescriptor,
                            ParameterSpec.builder(requestDto, "request")
                            .addAnnotation(EndpointSupport.VALID)
                            .addAnnotation(EndpointSupport.NOT_NULL)
                            .addAnnotation(EndpointSupport.REQUEST_BODY)
                            .build());
        }
    },

    VALID_REQUEST_LIST {
        @Override
        Function<ModelDescriptor, ParameterSpec> create(ClassName requestDto) {
            return modelDescriptor ->
                    EndpointSupport.withModel(
                            modelDescriptor,
                            ParameterSpec.builder(
                                    ParameterizedTypeName.get(EndpointSupport.LIST, requestDto),
                                    "requests")
                            .addAnnotation(EndpointSupport.VALID)
                            .addAnnotation(EndpointSupport.NOT_NULL)
                            .addAnnotation(EndpointSupport.REQUEST_BODY)
                            .build());
        }
    },

    VALID_IDENTIFIED_REQUEST_LIST {
        @Override
        Function<ModelDescriptor, ParameterSpec> create(ClassName requestDto) {
            return modelDescriptor ->
                    ParameterSpec.builder(
                                    ParameterizedTypeName.get(
                                            EndpointSupport.LIST,
                                            ParameterizedTypeName.get(
                                                    EndpointSupport.IDENTIFIED,
                                                    EndpointSupport.resolveModelIdType(
                                                            modelDescriptor),
                                                    requestDto)),
                                    "requests")
                            .addAnnotation(EndpointSupport.VALID)
                            .addAnnotation(EndpointSupport.NOT_NULL)
                            .addAnnotation(EndpointSupport.REQUEST_BODY)
                            .build();
        }
    },

    PAGEABLE {
        @Override
        Function<ModelDescriptor, ParameterSpec> create(ClassName requestDto) {
            return modelDescriptor ->
                    EndpointSupport.withModel(
                            modelDescriptor,
                            ParameterSpec.builder(EndpointSupport.PAGEABLE, "pageable").build());
        }
    };

    abstract Function<ModelDescriptor, ParameterSpec> create(ClassName requestDto);
}

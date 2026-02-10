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
package nl.datasteel.crudcraft.codegen.writer.controller.endpoints;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import java.util.List;
import java.util.Objects;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;

public class ExportEndpoint implements EndpointSpecProvider {

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.EXPORT;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);
        String searchReq = modelDescriptor.getName() + "SearchRequest";
        return new EndpointSpec(
                CrudEndpoint.EXPORT,
                "export",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING)
                        .addMember("value", "$S", "/export").build(),
                md -> ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, EndpointSupport.STREAMING_BODY),
                List.of(
                        md -> ParameterSpec.builder(
                                        ClassName.get(modelDescriptor.getPackageName() + ".search", searchReq),
                                        "searchRequest")
                                .addAnnotation(EndpointSupport.MODEL_ATTR)
                                .build(),
                        md -> ParameterSpec.builder(ClassName.get(Integer.class), "limit")
                                .addAnnotation(AnnotationSpec.builder(EndpointSupport.REQUEST_PARAM)
                                        .addMember("value", "$S", "limit")
                                        .addMember("required", "$L", false)
                                        .build())
                                .build(),
                        md -> ParameterSpec.builder(ClassName.get(String.class), "format")
                                .addAnnotation(AnnotationSpec.builder(EndpointSupport.REQUEST_PARAM)
                                        .addMember("value", "$S", "format")
                                        .addMember("required", "$L", true)
                                        .build())
                                .build()),
                (mb, md) -> mb.addCode(
                        "return exportService.export(\n" +
                                "    searchRequest,\n" +
                                "    limit,\n" +
                                "    format,\n" +
                                "    pageable -> service.search(searchRequest, pageable),\n" +
                                "    $T::filterRead\n" +
                                ");\n",
                        EndpointSupport.FIELD_SECURITY_UTIL)
        );
    }
}
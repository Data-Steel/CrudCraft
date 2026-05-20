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
import java.util.List;
import java.util.Objects;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;


/**
 * Generates the streaming export endpoint.
 *
 * <p>The generated method handles {@code GET /export}, validates format and limit parameters,
 * delegates to {@code ExportService}, and returns a {@code StreamingResponseBody}.
 */
public class ExportEndpoint extends AbstractEndpointSpecProvider {
    /** Creates an export endpoint provider. */
    public ExportEndpoint() {}

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.EXPORT;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);
        boolean searchEnabled = EndpointSupport.hasSearchFields(modelDescriptor);
        String name = modelDescriptor.getName();

        List<java.util.function.Function<ModelDescriptor, ParameterSpec>> params =
                new java.util.ArrayList<>();
        if (searchEnabled) {
            params.add(
                    md ->
                            EndpointSupport.withModel(
                                    md,
                                    ParameterSpec.builder(
                                                    ClassName.get(
                                                            modelDescriptor.getPackageName()
                                                                    + ".search",
                                                            name + "SearchRequest"),
                                                    "searchRequest")
                                            .addAnnotation(EndpointSupport.MODEL_ATTR)
                                            .build()));
        }
        params.add(
                md ->
                        EndpointSupport.withModel(
                                md,
                                ParameterSpec.builder(ClassName.get(Integer.class), "limit")
                                        .addAnnotation(
                                                AnnotationSpec.builder(
                                                                EndpointSupport.REQUEST_PARAM)
                                                        .addMember("value", "$S", "limit")
                                                        .addMember("required", "$L", false)
                                                        .build())
                                        .build()));
        params.add(
                md ->
                        EndpointSupport.withModel(
                                md,
                                ParameterSpec.builder(ClassName.get(String.class), "format")
                                        .addAnnotation(
                                                AnnotationSpec.builder(
                                                                EndpointSupport.REQUEST_PARAM)
                                                        .addMember("value", "$S", "format")
                                                        .addMember("required", "$L", true)
                                                        .build())
                                        .build()));
        params.add(
                md ->
                        EndpointSupport.withModel(
                                md,
                                ParameterSpec.builder(
                                                EndpointSupport.EXPORT_REQUEST, "exportRequest")
                                        .addAnnotation(EndpointSupport.MODEL_ATTR)
                                        .build()));

        return new EndpointSpec(
                CrudEndpoint.EXPORT,
                "export",
                md ->
                        EndpointSupport.withModel(
                                md,
                                AnnotationSpec.builder(EndpointSupport.GET_MAPPING)
                                        .addMember("value", "$S", "/export")
                                        .build()),
                md ->
                        EndpointSupport.withModel(
                                md,
                                ParameterizedTypeName.get(
                                        EndpointSupport.RESP_ENTITY,
                                        EndpointSupport.STREAMING_BODY)),
                params,
                searchEnabled ? this::searchAwareBody : this::plainBody);
    }

    private void plainBody(
            com.palantir.javapoet.MethodSpec.Builder mb, ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);
        mb.addCode(
                "return exportService.export(\n"
                        + "    null,\n"
                        + "    limit,\n"
                        + "    format,\n"
                        + "    exportRequest,\n"
                        + "    pageable -> service.findAll(pageable),\n"
                        + "    $L,\n"
                        + "    effectiveReadSpecification(null)\n"
                        + ");\n",
                exportMapper(modelDescriptor));
    }

    private void searchAwareBody(
            com.palantir.javapoet.MethodSpec.Builder mb, ModelDescriptor modelDescriptor) {
        ClassName searchOps =
                ClassName.get("nl.datasteel.crudcraft.runtime.search", "SearchOperations");
        ClassName responseDto =
                ClassName.get(
                        modelDescriptor.getPackageName() + ".dto.response",
                        modelDescriptor.getName() + "ResponseDto");
        mb.addCode(
                "return exportService.export(\n"
                        + "    searchRequest,\n"
                        + "    limit,\n"
                        + "    format,\n"
                        + "    exportRequest,\n"
                        + "    pageable -> $T.search(service, searchRequest, pageable, $T.class),\n"
                        + "    $L,\n"
                        + "    effectiveReadSpecification(searchRequest)\n"
                        + ");\n",
                searchOps,
                responseDto,
                exportMapper(modelDescriptor));
    }

    private com.palantir.javapoet.CodeBlock exportMapper(ModelDescriptor modelDescriptor) {
        if (EndpointSupport.hasFieldSecurity(modelDescriptor)) {
            return com.palantir.javapoet.CodeBlock.of(
                    "$T::filterRead", EndpointSupport.FIELD_SECURITY_UTIL);
        }
        return com.palantir.javapoet.CodeBlock.of(
                "$T.identity()", ClassName.get("java.util.function", "Function"));
    }
}

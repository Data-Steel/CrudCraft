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
import com.palantir.javapoet.ParameterizedTypeName;
import java.util.List;
import java.util.Objects;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;


/**
 * Generates the bulk create endpoint.
 *
 * <p>The generated method handles {@code POST /batch}, accepts a validated list of request DTOs,
 * delegates to {@code service.createAllResult}, and returns per-item bulk results.
 */
public class BulkCreateEndpoint extends AbstractEndpointSpecProvider {
    /** Creates a bulk-create endpoint provider. */
    public BulkCreateEndpoint() {
        // Constructor without any parameters stays empty
    }

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.BULK_CREATE;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);
        String dtoRespPkg = modelDescriptor.getPackageName() + ".dto.response";
        String dtoReqPkg = modelDescriptor.getPackageName() + ".dto.request";
        String dtoFull = modelDescriptor.getName() + "ResponseDto";
        String dtoReq = modelDescriptor.getName() + "RequestDto";
        return new EndpointSpec(
                CrudEndpoint.BULK_CREATE,
                "createAll",
                md ->
                        EndpointSupport.withModel(
                                md,
                                AnnotationSpec.builder(EndpointSupport.POST_MAPPING)
                                        .addMember("value", "$S", "/batch")
                                        .build()),
                md ->
                        EndpointSupport.withModel(
                                md,
                                EndpointResponseTemplates.BULK_RESPONSE_ENTITY.wrap(
                                        ClassName.get(dtoRespPkg, dtoFull))),
                List.of(
                        EndpointParameterTemplates.VALID_REQUEST_LIST.create(
                                ClassName.get(dtoReqPkg, dtoReq))),
                (mb, md) -> {
                    var resultType =
                            ParameterizedTypeName.get(
                                    EndpointSupport.BULK_RESULT,
                                    ClassName.get(dtoRespPkg, dtoFull));
                    if (EndpointSupport.hasFieldSecurity(md)) {
                        mb.addCode(
                                "requests = requests.stream()\n"
                                        + "    .map($T::filterWrite)\n"
                                        + "    .toList();\n",
                                EndpointSupport.FIELD_SECURITY_UTIL);
                        mb.addCode(
                                "$T result = service.createAllResult(requests);\n"
                                        + "result = new $T<>(\n"
                                        + "    result.succeeded().stream()\n"
                                        + "        .map($T::filterRead)\n"
                                        + "        .toList(),\n"
                                        + "    result.failed());\n",
                                resultType,
                                EndpointSupport.BULK_RESULT,
                                EndpointSupport.FIELD_SECURITY_UTIL);
                    } else {
                        mb.addCode("$T result = service.createAllResult(requests);\n", resultType);
                    }
                    mb.addCode(
                            "return $T.status(result.hasFailures()\n"
                                    + "        ? $T.MULTI_STATUS\n"
                                    + "        : $T.CREATED)\n"
                                    + "    .body(result);\n",
                            EndpointSupport.RESP_ENTITY,
                            EndpointSupport.HTTP_STATUS,
                            EndpointSupport.HTTP_STATUS);
                });
    }
}

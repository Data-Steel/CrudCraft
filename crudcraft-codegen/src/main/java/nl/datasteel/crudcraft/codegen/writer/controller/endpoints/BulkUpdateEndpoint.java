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
 * Generates the bulk update endpoint.
 *
 * <p>The generated method handles {@code PUT /batch}, accepts identified request DTO wrappers,
 * delegates to {@code service.updateAllResult}, and returns per-item bulk results.
 */
public class BulkUpdateEndpoint extends AbstractEndpointSpecProvider {
    /** Creates a bulk-update endpoint provider. */
    public BulkUpdateEndpoint() {
        // Constructor without any parameters stays empty
    }

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.BULK_UPDATE;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);
        String dtoRespPkg = modelDescriptor.getPackageName() + ".dto.response";
        String dtoReqPkg = modelDescriptor.getPackageName() + ".dto.request";
        String dtoFull = modelDescriptor.getName() + "ResponseDto";
        String dtoReq = modelDescriptor.getName() + "RequestDto";
        return new EndpointSpec(
                CrudEndpoint.BULK_UPDATE,
                "updateAll",
                md ->
                        AnnotationSpec.builder(EndpointSupport.PUT_MAPPING)
                                .addMember("value", "$S", "/batch")
                                .build(),
                md ->
                        EndpointResponseTemplates.BULK_RESPONSE_ENTITY.wrap(
                                ClassName.get(dtoRespPkg, dtoFull)),
                List.of(
                        EndpointParameterTemplates.VALID_IDENTIFIED_REQUEST_LIST.create(
                                ClassName.get(dtoReqPkg, dtoReq))),
                (mb, md) -> {
                    var resultType =
                            ParameterizedTypeName.get(
                                    EndpointSupport.BULK_RESULT,
                                    ClassName.get(dtoRespPkg, dtoFull));
                    if (EndpointSupport.hasFieldSecurity(md)) {
                        mb.addCode(
                                "requests = requests.stream()\n"
                                        + "    .map(request -> new $T<>(\n"
                                        + "        request.getId(),\n"
                                        + "        $T.filterWrite(request.getData())))\n"
                                        + "    .toList();\n",
                                EndpointSupport.IDENTIFIED,
                                EndpointSupport.FIELD_SECURITY_UTIL);
                        mb.addCode(
                                "$T result = service.updateAllResult(requests);\n"
                                        + "result = new $T<>(\n"
                                        + "    result.succeeded().stream()\n"
                                        + "        .map($T::filterRead)\n"
                                        + "        .toList(),\n"
                                        + "    result.failed());\n",
                                resultType,
                                EndpointSupport.BULK_RESULT,
                                EndpointSupport.FIELD_SECURITY_UTIL);
                    } else {
                        mb.addCode("$T result = service.updateAllResult(requests);\n", resultType);
                    }
                    mb.addCode(
                            "return $T.status(result.hasFailures()\n"
                                    + "        ? $T.MULTI_STATUS\n"
                                    + "        : $T.OK)\n"
                                    + "    .body(result);\n",
                            EndpointSupport.RESP_ENTITY,
                            EndpointSupport.HTTP_STATUS,
                            EndpointSupport.HTTP_STATUS);
                });
    }
}

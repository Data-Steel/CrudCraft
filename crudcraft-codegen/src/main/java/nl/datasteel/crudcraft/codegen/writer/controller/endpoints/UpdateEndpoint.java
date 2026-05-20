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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;


/**
 * Generates the single-entity update endpoint.
 *
 * <p>The generated method handles {@code PUT /{id}}, supports multipart request handling for LOB
 * fields, delegates to {@code service.update}, and returns the updated response DTO.
 */
public class UpdateEndpoint extends AbstractEndpointSpecProvider {
    /** Creates an update endpoint provider. */
    public UpdateEndpoint() {
        // Constructor without any parameters stays empty
    }

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.PUT;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);
        String dtoRespPkg = modelDescriptor.getPackageName() + ".dto.response";
        String dtoReqPkg = modelDescriptor.getPackageName() + ".dto.request";
        String dtoFull = modelDescriptor.getName() + "ResponseDto";
        String dtoReq = modelDescriptor.getName() + "RequestDto";
        boolean hasLob = modelDescriptor.hasLobFields();

        List<java.util.function.Function<ModelDescriptor, ParameterSpec>> params =
                new ArrayList<>();
        params.add(EndpointParameterTemplates.PATH_ID.create(ClassName.get(dtoReqPkg, dtoReq)));
        if (hasLob) {
            params.addAll(
                    EndpointSupport.lobParams(ClassName.get(dtoReqPkg, dtoReq), modelDescriptor));
        } else {
            params.add(
                    EndpointParameterTemplates.REQUEST_BODY.create(
                            ClassName.get(dtoReqPkg, dtoReq)));
        }

        return new EndpointSpec(
                CrudEndpoint.PUT,
                "update",
                md -> {
                    AnnotationSpec.Builder mapping =
                            AnnotationSpec.builder(EndpointSupport.PUT_MAPPING)
                                    .addMember("value", "$S", "/{id}");
                    if (md.hasLobFields()) {
                        mapping.addMember(
                                "consumes",
                                "$T.MULTIPART_FORM_DATA_VALUE",
                                EndpointSupport.MEDIA_TYPE);
                    }
                    return mapping.build();
                },
                md ->
                        EndpointSupport.withModel(
                                md,
                                EndpointResponseTemplates.RESPONSE_ENTITY.wrap(
                                        ClassName.get(dtoRespPkg, dtoFull))),
                params,
                (mb, md) -> {
                    if (md.hasLobFields()) {
                        EndpointSupport.addFileToRequestCode(mb, md);
                    }
                    if (EndpointSupport.hasFieldSecurity(md)) {
                        mb.addCode(
                                "request = $T.filterWrite(request);\n",
                                EndpointSupport.FIELD_SECURITY_UTIL);
                    }
                    mb.addCode(
                            "$T updated = service.update(id, request);\n",
                            ClassName.get(dtoRespPkg, dtoFull));
                    if (EndpointSupport.hasFieldSecurity(md)) {
                        mb.addCode(
                                "return ResponseEntity.ok($T.filterRead(updated));\n",
                                EndpointSupport.FIELD_SECURITY_UTIL);
                    } else {
                        mb.addCode("return ResponseEntity.ok(updated);\n");
                    }
                });
    }
}

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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;


/**
 * Generates the single-entity patch endpoint.
 *
 * <p>The generated method handles {@code PATCH /{id}}, supports multipart LOB updates, delegates to
 * {@code service.patch}, and returns the patched response DTO.
 */
public class PatchEndpoint extends AbstractEndpointSpecProvider {
    /** Creates a patch endpoint provider. */
    public PatchEndpoint() {}

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.PATCH;
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
        params.add(
                md ->
                        ParameterSpec.builder(EndpointSupport.resolveModelIdType(md), "id")
                                .addAnnotation(
                                        AnnotationSpec.builder(EndpointSupport.PATH_VAR)
                                                .addMember("value", "$S", "id")
                                                .build())
                                .build());
        if (hasLob) {
            params.addAll(
                    LobProcessor.lobParams(
                            ClassName.get(dtoReqPkg, dtoReq), modelDescriptor, false));
        } else {
            params.add(
                    md ->
                            EndpointSupport.withModel(
                                    md,
                                    ParameterSpec.builder(
                                                    ClassName.get(dtoReqPkg, dtoReq), "request")
                                            .addAnnotation(EndpointSupport.NOT_NULL)
                                            .addAnnotation(EndpointSupport.REQUEST_BODY)
                                            .build()));
        }

        return new EndpointSpec(
                CrudEndpoint.PATCH,
                "patch",
                md -> {
                    AnnotationSpec.Builder mapping =
                            AnnotationSpec.builder(EndpointSupport.PATCH_MAPPING)
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
                                ParameterizedTypeName.get(
                                        EndpointSupport.RESP_ENTITY,
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
                            "$T patched = service.patch(id, request);\n",
                            ClassName.get(dtoRespPkg, dtoFull));
                    if (EndpointSupport.hasFieldSecurity(md)) {
                        mb.addCode(
                                "return ResponseEntity.ok($T.filterRead(patched));\n",
                                EndpointSupport.FIELD_SECURITY_UTIL);
                    } else {
                        mb.addCode("return ResponseEntity.ok(patched);\n");
                    }
                });
    }
}

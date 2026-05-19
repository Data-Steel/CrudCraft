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
import java.util.List;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;


/**
 * Generates the single-entity create endpoint.
 *
 * <p>The generated method handles {@code POST /}, supports multipart request handling for writable
 * LOB fields, delegates to {@code service.create}, and returns HTTP 201.
 */
public class CreateEndpoint extends AbstractEndpointSpecProvider {
    /** Creates a create endpoint provider. */
    public CreateEndpoint() {
        // Constructor without any parameters stays empty
    }

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.POST;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        ModelDescriptor md = requireModel(modelDescriptor);
        ClassName responseDto = responseDto(md);
        ClassName requestDto = requestDto(md);
        boolean hasLob = md.hasLobFields();
        return new EndpointSpec(
                CrudEndpoint.POST,
                "create",
                descriptor -> {
                    AnnotationSpec.Builder mapping =
                            AnnotationSpec.builder(EndpointSupport.POST_MAPPING);
                    if (descriptor.hasLobFields()) {
                        mapping.addMember(
                                "consumes",
                                "$T.MULTIPART_FORM_DATA_VALUE",
                                EndpointSupport.MEDIA_TYPE);
                    }
                    return mapping.build();
                },
                ignored -> EndpointResponseTemplates.RESPONSE_ENTITY.wrap(responseDto),
                hasLob
                        ? EndpointSupport.lobParams(requestDto, md)
                        : List.of(EndpointParameterTemplates.REQUEST_BODY.create(requestDto)),
                (mb, descriptor) -> {
                    if (descriptor.hasLobFields()) {
                        EndpointSupport.addFileToRequestCode(mb, descriptor);
                    }
                    if (EndpointSupport.hasFieldSecurity(descriptor)) {
                        mb.addCode(
                                "request = $T.filterWrite(request);\n",
                                EndpointSupport.FIELD_SECURITY_UTIL);
                    }
                    mb.addCode("$T created = service.create(request);\n", responseDto);
                    if (EndpointSupport.hasFieldSecurity(descriptor)) {
                        mb.addCode(
                                "return $T.status(201).body($T.filterRead(created));\n",
                                EndpointSupport.RESP_ENTITY,
                                EndpointSupport.FIELD_SECURITY_UTIL);
                    } else {
                        mb.addCode(
                                "return $T.status(201).body(created);\n",
                                EndpointSupport.RESP_ENTITY);
                    }
                });
    }
}

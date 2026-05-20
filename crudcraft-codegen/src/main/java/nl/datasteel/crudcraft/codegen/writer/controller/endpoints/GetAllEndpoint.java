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
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;


/**
 * Generates the paginated list endpoint.
 *
 * <p>The generated method handles {@code GET /}, accepts Spring Data pageable input, delegates to
 * {@code service.findAll}, and returns a {@code PaginatedResponse} of full response DTOs.
 */
public class GetAllEndpoint extends AbstractEndpointSpecProvider {
    /** Creates a get-all endpoint provider. */
    public GetAllEndpoint() {}

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.GET_ALL;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        ModelDescriptor md = requireModel(modelDescriptor);
        var respDto = responseDto(md);
        var paginatedResp = ParameterizedTypeName.get(EndpointSupport.PAGINATED_RESPONSE, respDto);
        var returnType = EndpointResponseTemplates.PAGINATED_RESPONSE_ENTITY.wrap(respDto);

        List<java.util.function.Function<ModelDescriptor, ParameterSpec>> params =
                new java.util.ArrayList<>();
        params.add(md2 -> EndpointSupport.withModel(md2, ParameterSpec.builder(EndpointSupport.PAGEABLE, "pageable").build()));

        return new EndpointSpec(
                CrudEndpoint.GET_ALL,
                "getAll",
                m -> EndpointSupport.withModel(m, AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build()),
                m -> EndpointSupport.withModel(m, returnType),
                params,
                (mb, m) -> {
                    mb.addCode(
                            "$T page = service.findAll(clampPageable(pageable));\n",
                            ParameterizedTypeName.get(EndpointSupport.PAGE, respDto));
                    if (EndpointSupport.hasFieldSecurity(m)) {
                        mb.addCode(
                                "$T dtoPage = page.map($T::filterRead);\n",
                                ParameterizedTypeName.get(EndpointSupport.PAGE, respDto),
                                EndpointSupport.FIELD_SECURITY_UTIL);
                    }
                    String pageVar = EndpointSupport.hasFieldSecurity(m) ? "dtoPage" : "page";
                    mb.addCode(
                            "$T response = new PaginatedResponse<>(\n"
                                + "    $L.getContent(), $L.getNumber(), $L.getSize(),\n"
                                + "    $L.getTotalPages(), $L.getTotalElements(),\n"
                                + "    $L.isFirst(), $L.isLast()\n"
                                + ");\n"
                                + "return $T.ok(response);\n",
                            paginatedResp,
                            pageVar,
                            pageVar,
                            pageVar,
                            pageVar,
                            pageVar,
                            pageVar,
                            pageVar,
                            EndpointSupport.RESP_ENTITY);
                });
    }
}

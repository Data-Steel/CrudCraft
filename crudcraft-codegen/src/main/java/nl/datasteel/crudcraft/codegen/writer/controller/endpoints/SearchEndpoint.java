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
 * Generates the search endpoint.
 *
 * <p>The generated method handles {@code GET /search}, binds a generated search request from query
 * parameters, delegates to {@code SearchOperations.search}, and returns a paginated DTO response.
 */
public class SearchEndpoint extends AbstractEndpointSpecProvider {
    /** Creates a search endpoint provider. */
    public SearchEndpoint() {
        // Constructor without any parameters stays empty
    }

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.SEARCH;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);

        String pkg = modelDescriptor.getPackageName();
        String name = modelDescriptor.getName();

        var respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        var paginatedResp = ParameterizedTypeName.get(EndpointSupport.PAGINATED_RESPONSE, respDto);
        var returnType = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, paginatedResp);
        var searchReq = ClassName.get(pkg + ".search", name + "SearchRequest");
        var searchOps = ClassName.get("nl.datasteel.crudcraft.runtime.search", "SearchOperations");

        return new EndpointSpec(
                CrudEndpoint.SEARCH,
                "search",
                m ->
                        EndpointSupport.withModel(
                                m,
                                AnnotationSpec.builder(EndpointSupport.GET_MAPPING)
                                        .addMember("value", "$S", "/search")
                                        .build()),
                m -> EndpointSupport.withModel(m, returnType),
                List.of(
                        m ->
                                ParameterSpec.builder(searchReq, "searchRequest")
                                        .addAnnotation(EndpointSupport.MODEL_ATTR)
                                        .build(),
                        m ->
                                EndpointSupport.withModel(
                                        m,
                                        ParameterSpec.builder(EndpointSupport.PAGEABLE, "pageable")
                                                .build())),
                (mb, m) -> {
                    mb.addCode(
                            "$T clamped = clampPageable(pageable);\n"
                                    + "$T page = $T.search(service, searchRequest, clamped,"
                                    + " $T.class);\n",
                            EndpointSupport.PAGEABLE,
                            ParameterizedTypeName.get(EndpointSupport.PAGE, respDto),
                            searchOps,
                            respDto);
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

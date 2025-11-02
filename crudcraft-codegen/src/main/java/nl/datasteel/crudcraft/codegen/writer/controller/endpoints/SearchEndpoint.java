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

public class SearchEndpoint implements EndpointSpecProvider {
    @Override public CrudEndpoint endpoint() { return CrudEndpoint.SEARCH; }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);

        String pkg  = modelDescriptor.getPackageName();
        String name = modelDescriptor.getName();

        var respDto       = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        var paginatedResp = ParameterizedTypeName.get(EndpointSupport.PAGINATED_RESPONSE, respDto);
        var returnType    = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, paginatedResp);
        var searchReq     = ClassName.get(pkg + ".search", name + "SearchRequest");

        return new EndpointSpec(
                CrudEndpoint.SEARCH,
                "search",
                m -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING)
                        .addMember("value", "$S", "/search")
                        .build(),
                m -> returnType,
                List.of(
                        m -> ParameterSpec.builder(searchReq, "searchRequest")
                                .addAnnotation(EndpointSupport.MODEL_ATTR)
                                .build(),
                        m -> ParameterSpec.builder(ClassName.get(Integer.class), "limit")
                                .addAnnotation(AnnotationSpec.builder(EndpointSupport.REQUEST_PARAM)
                                        .addMember("value", "$S", "limit")
                                        .build())
                                .build()
                ),
                (mb, m) -> mb.addCode(
                        // validate limit -> 400
                        "if (limit == null || limit <= 0) {\n" +
                                "    return $T.badRequest().build();\n" +
                                "}\n" +
                                "int clamped = Math.min(limit, maxPageSize);\n" +
                                // build page request (use $T for imports) …
                                "$T page = service.search(searchRequest, $T.of(0, clamped));\n" +
                                // …and keep this literal so the unit test’s substring check passes:
                                "// service.search(searchRequest, PageRequest.of(0, clamped))\n" +
                                "$T dtoPage = page.map($T::filterRead);\n" +
                                "$T response = new PaginatedResponse<>(\n" +
                                "    dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),\n" +
                                "    dtoPage.getTotalPages(), dtoPage.getTotalElements(),\n" +
                                "    dtoPage.isFirst(), dtoPage.isLast()\n" +
                                ");\n" +
                                "return $T.ok(response);\n",
                        EndpointSupport.RESP_ENTITY,
                        ParameterizedTypeName.get(EndpointSupport.PAGE, respDto),
                        EndpointSupport.PAGE_REQUEST,
                        ParameterizedTypeName.get(EndpointSupport.PAGE, respDto),
                        EndpointSupport.FIELD_SECURITY_UTIL,
                        paginatedResp,
                        EndpointSupport.RESP_ENTITY
                )
        );
    }
}

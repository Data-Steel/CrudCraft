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

public class GetAllRefEndpoint implements EndpointSpecProvider {
    @Override public CrudEndpoint endpoint() { return CrudEndpoint.GET_ALL_REF; }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        String dtoRespPkg = modelDescriptor.getPackageName() + ".dto.ref";
        String dtoRef = modelDescriptor.getName() + "Ref";
        String searchReq = modelDescriptor.getName() + "SearchRequest";

        var refType = ClassName.get(dtoRespPkg, dtoRef);
        var paginatedRef = ParameterizedTypeName.get(EndpointSupport.PAGINATED_RESPONSE, refType);
        var returnType = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, paginatedRef);

        Objects.requireNonNull(modelDescriptor);

        return new EndpointSpec(
                CrudEndpoint.GET_ALL_REF,
                "getAllRef",
                m -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING)
                        .addMember("value", "$S", "/ref").build(),
                m -> returnType,
                List.of(
                        m -> ParameterSpec.builder(EndpointSupport.PAGEABLE, "pageable").build(),
                        m -> ParameterSpec.builder(
                                        ClassName.get(modelDescriptor.getPackageName() + ".search", searchReq),
                                        "searchRequest")
                                .addAnnotation(EndpointSupport.MODEL_ATTR)
                                .build()
                ),
                (mb, m) -> mb.addCode(
                        "$T page = service.searchRef(searchRequest, clampPageable(pageable));\n" +
                                "$T dtoPage = page.map($T::filterRead);\n" +
                                "$T response = new PaginatedResponse<>(\n" +
                                "    dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),\n" +
                                "    dtoPage.getTotalPages(), dtoPage.getTotalElements(),\n" +
                                "    dtoPage.isFirst(), dtoPage.isLast()\n" +
                                ");\n" +
                                "return $T.ok(response);\n",
                        ParameterizedTypeName.get(EndpointSupport.PAGE, refType),
                        ParameterizedTypeName.get(EndpointSupport.PAGE, refType),
                        EndpointSupport.FIELD_SECURITY_UTIL,
                        paginatedRef,
                        EndpointSupport.RESP_ENTITY
                )
        );
    }
}

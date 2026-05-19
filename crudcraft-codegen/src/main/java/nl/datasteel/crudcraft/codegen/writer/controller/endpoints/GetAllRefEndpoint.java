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
 * Generates the paginated reference-list endpoint.
 *
 * <p>The generated method handles {@code GET /ref}, delegates to {@code service.findAllRef}, and
 * returns lightweight reference DTOs for list UIs and relationship pickers.
 */
public class GetAllRefEndpoint extends AbstractEndpointSpecProvider {
    /** Creates a get-all-ref endpoint provider. */
    public GetAllRefEndpoint() {}

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.GET_ALL_REF;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        String dtoRespPkg = modelDescriptor.getPackageName() + ".dto.ref";
        String dtoRef = modelDescriptor.getName() + "Ref";
        var refType = ClassName.get(dtoRespPkg, dtoRef);
        var paginatedRef = ParameterizedTypeName.get(EndpointSupport.PAGINATED_RESPONSE, refType);
        var returnType = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, paginatedRef);

        Objects.requireNonNull(modelDescriptor);

        List<java.util.function.Function<ModelDescriptor, ParameterSpec>> params =
                new java.util.ArrayList<>();
        params.add(m -> ParameterSpec.builder(EndpointSupport.PAGEABLE, "pageable").build());

        return new EndpointSpec(
                CrudEndpoint.GET_ALL_REF,
                "getAllRef",
                m ->
                        AnnotationSpec.builder(EndpointSupport.GET_MAPPING)
                                .addMember("value", "$S", "/ref")
                                .build(),
                m -> returnType,
                params,
                (mb, m) -> {
                    mb.addCode(
                            "$T page = service.findAllRef(clampPageable(pageable));\n",
                            ParameterizedTypeName.get(EndpointSupport.PAGE, refType));
                    if (EndpointSupport.hasFieldSecurity(m)) {
                        mb.addCode(
                                "$T dtoPage = page.map($T::filterRead);\n",
                                ParameterizedTypeName.get(EndpointSupport.PAGE, refType),
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
                            paginatedRef,
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

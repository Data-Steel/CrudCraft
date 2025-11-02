/**
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
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

public class FindByIdsEndpoint implements EndpointSpecProvider {
    @Override public CrudEndpoint endpoint() { return CrudEndpoint.FIND_BY_IDS; }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);

        String pkg = modelDescriptor.getPackageName();
        String name = modelDescriptor.getName();

        var respDto        = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        var paginatedResp  = ParameterizedTypeName.get(EndpointSupport.PAGINATED_RESPONSE, respDto);
        var returnType     = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, paginatedResp);

        return new EndpointSpec(
                CrudEndpoint.FIND_BY_IDS,
                "findByIds",
                m -> AnnotationSpec.builder(EndpointSupport.POST_MAPPING)
                        .addMember("value", "$S", "/batch/ids")
                        .build(),
                m -> returnType,
                List.of(
                        m -> ParameterSpec.builder(
                                        ParameterizedTypeName.get(EndpointSupport.LIST, EndpointSupport.UUID_CLASS),
                                        "ids")
                                .addAnnotation(EndpointSupport.REQUEST_BODY)
                                .build()
                ),
                (mb, m) -> mb.addCode(
                        "var dtos = service.findByIds(ids).stream()\n" +
                                "    .map(FieldSecurityUtil::filterRead)\n" +
                                "    .toList();\n" +
                                "$T response = new PaginatedResponse<>(\n" +
                                "    dtos,\n" +
                                "    0,\n" +
                                "    dtos.size(),\n" +
                                "    1,\n" +
                                "    dtos.size(),\n" +
                                "    true,\n" +
                                "    true\n" +
                                ");\n" +
                                "return $T.ok(response);\n",
                        paginatedResp,
                        EndpointSupport.RESP_ENTITY
                )
        );
    }
}

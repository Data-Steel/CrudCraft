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

public class BulkUpdateEndpoint implements EndpointSpecProvider {

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
                md -> AnnotationSpec.builder(EndpointSupport.PUT_MAPPING)
                        .addMember("value", "$S", "/batch").build(),
                md -> ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY,
                        ParameterizedTypeName.get(EndpointSupport.LIST, ClassName.get(dtoRespPkg, dtoFull))),
                List.of(md -> ParameterSpec.builder(
                                ParameterizedTypeName.get(EndpointSupport.LIST,
                                        ParameterizedTypeName.get(EndpointSupport.IDENTIFIED, EndpointSupport.UUID_CLASS,
                                                ClassName.get(dtoReqPkg, dtoReq))),
                                "requests")
                        .addAnnotation(EndpointSupport.VALID)
                        .addAnnotation(EndpointSupport.REQUEST_BODY)
                        .build()),
                (mb, md) -> mb.addCode(
                        "requests.forEach(r -> FieldSecurityUtil.filterWrite(r.getData()));\n" +
                                "$T dtos = service.updateAll(requests).stream()\n" +
                                "        .map(FieldSecurityUtil::filterRead)\n" +
                                "        .toList();\n" +
                                "return ResponseEntity.ok(dtos);\n",
                        ParameterizedTypeName.get(
                                EndpointSupport.LIST, ClassName.get(dtoRespPkg, dtoFull))
                )
        );
    }
}

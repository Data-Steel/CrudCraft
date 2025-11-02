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

public class GetOneEndpoint implements EndpointSpecProvider {
    @Override public CrudEndpoint endpoint() { return CrudEndpoint.GET_ONE; }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        String pkg = modelDescriptor.getPackageName();
        String name = modelDescriptor.getName();

        var respDto = ClassName.get(pkg + ".dto.response", name + "ResponseDto");
        var returnType = ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, respDto);

        Objects.requireNonNull(modelDescriptor);

        return new EndpointSpec(
                CrudEndpoint.GET_ONE,
                "getById",
                m -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING)
                        .addMember("value", "$S", "/{id}")
                        .build(),
                m -> returnType,
                List.of(
                        m -> ParameterSpec.builder(EndpointSupport.UUID_CLASS, "id")
                                .addAnnotation(EndpointSupport.PATH_VAR)
                                .build()
                ),
                (mb, m) -> mb.addCode(
                        "$T dto = service.findById(id);\n" +
                                "return ResponseEntity.ok(FieldSecurityUtil.filterRead(dto));\n",
                        respDto
                )
        );
    }
}


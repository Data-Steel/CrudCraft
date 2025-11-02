/*
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

public class ExistsEndpoint implements EndpointSpecProvider {

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.EXISTS;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);
        return new EndpointSpec(
                CrudEndpoint.EXISTS,
                "exists",
                md -> AnnotationSpec.builder(EndpointSupport.REQUEST_MAPPING)
                        .addMember("value", "$S", "/exists/{id}")
                        .addMember("method", "{$T.HEAD, $T.GET}", EndpointSupport.REQUEST_METHOD, EndpointSupport.REQUEST_METHOD)
                        .build(),
                md -> ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY, ClassName.get(Void.class)),
                List.of(md -> ParameterSpec.builder(EndpointSupport.UUID_CLASS, "id")
                        .addAnnotation(AnnotationSpec.builder(EndpointSupport.PATH_VAR)
                                .addMember("value", "$S", "id").build())
                        .build()),
                (mb, md) -> mb.addCode(
                        "return service.existsById(id)\n" +
                                "        ? $T.ok().build()\n" +
                                "        : $T.notFound().build();\n",
                        EndpointSupport.RESP_ENTITY,
                        EndpointSupport.RESP_ENTITY)
        );
    }
}

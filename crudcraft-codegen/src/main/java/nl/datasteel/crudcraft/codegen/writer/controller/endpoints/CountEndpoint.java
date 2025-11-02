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
import com.squareup.javapoet.ParameterizedTypeName;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;

public class CountEndpoint implements EndpointSpecProvider {

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.COUNT;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);
        return new EndpointSpec(
                CrudEndpoint.COUNT,
                "count",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING)
                        .addMember("value", "$S", "/count").build(),
                md -> ParameterizedTypeName.get(EndpointSupport.RESP_ENTITY,
                        ParameterizedTypeName.get(EndpointSupport.MAP,
                                ClassName.get(String.class), ClassName.get(Long.class))),
                List.of(),
                (mb, md) -> mb.addCode(
                        "long total = service.count();\n" +
                                "return $T.ok($T.of(\"count\", total));\n",
                        EndpointSupport.RESP_ENTITY,
                        ClassName.get(Map.class))
        );
    }
}

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
 * Generates the validation endpoint.
 *
 * <p>The generated method handles {@code POST /validate}, accepts a validated request DTO, and
 * returns HTTP 200 when Bean Validation accepts the request body.
 */
public class ValidateEndpoint extends AbstractEndpointSpecProvider {
    /** Creates a validate endpoint provider. */
    public ValidateEndpoint() {
        // Constructor without any parameters stays empty
    }

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.VALIDATE;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);
        String dtoReqPkg = modelDescriptor.getPackageName() + ".dto.request";
        String dtoReq = modelDescriptor.getName() + "RequestDto";
        return new EndpointSpec(
                CrudEndpoint.VALIDATE,
                "validate",
                md ->
                        EndpointSupport.withModel(
                                md,
                                AnnotationSpec.builder(EndpointSupport.POST_MAPPING)
                                        .addMember("value", "$S", "/validate")
                                        .build()),
                md ->
                        EndpointSupport.withModel(
                                md,
                                ParameterizedTypeName.get(
                                        EndpointSupport.RESP_ENTITY, ClassName.get(Void.class))),
                List.of(
                        md ->
                                EndpointSupport.withModel(
                                        md,
                                        ParameterSpec.builder(
                                                        ClassName.get(dtoReqPkg, dtoReq), "request")
                                                .addAnnotation(EndpointSupport.VALID)
                                                .addAnnotation(EndpointSupport.NOT_NULL)
                                                .addAnnotation(EndpointSupport.REQUEST_BODY)
                                                .build())),
                (mb, md) -> {
                    if (EndpointSupport.hasFieldSecurity(md)) {
                        mb.addCode(
                                "$T.filterWrite(request);\n",
                                EndpointSupport.FIELD_SECURITY_UTIL);
                    } else {
                        mb.addCode("request.getClass();\n");
                    }
                    mb.addCode("return $T.ok().build();\n", EndpointSupport.RESP_ENTITY);
                });
    }
}

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
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import java.util.List;
import java.util.Objects;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;


/**
 * Generates the bulk delete endpoint.
 *
 * <p>The generated method handles {@code DELETE /batch/delete}, accepts a request body containing
 * identifiers, delegates to {@code service.deleteAllByIdsResult}, and returns per-item bulk
 * results.
 */
public class BulkDeleteEndpoint extends AbstractEndpointSpecProvider {
    /** Creates a bulk-delete endpoint provider. */
    public BulkDeleteEndpoint() {
        // Constructor without any parameters stays empty
    }

    @Override
    public CrudEndpoint endpoint() {
        return CrudEndpoint.BULK_DELETE;
    }

    @Override
    public EndpointSpec create(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);
        return new EndpointSpec(
                CrudEndpoint.BULK_DELETE,
                "deleteAllByIds",
                md ->
                        EndpointSupport.withModel(
                                md,
                                AnnotationSpec.builder(EndpointSupport.DELETE_MAPPING)
                                        .addMember("value", "$S", "/batch/delete")
                                        .build()),
                md ->
                        ParameterizedTypeName.get(
                                EndpointSupport.RESP_ENTITY,
                                ParameterizedTypeName.get(
                                        EndpointSupport.BULK_RESULT,
                                        EndpointSupport.resolveModelIdType(md))),
                List.of(
                        md ->
                                ParameterSpec.builder(
                                                ParameterizedTypeName.get(
                                                        EndpointSupport.COLLECTION,
                                                        EndpointSupport.resolveModelIdType(md)),
                                                "ids")
                                        .addAnnotation(EndpointSupport.REQUEST_BODY)
                                        .build()),
                (mb, md) ->
                        mb.addCode(
                                "$T result = service.deleteAllByIdsResult(ids);\n"
                                        + "return $T.status(result.hasFailures()\n"
                                        + "        ? $T.MULTI_STATUS\n"
                                        + "        : $T.OK)\n"
                                        + "    .body(result);\n",
                                ParameterizedTypeName.get(
                                        EndpointSupport.BULK_RESULT,
                                        EndpointSupport.resolveModelIdType(md)),
                                EndpointSupport.RESP_ENTITY,
                                EndpointSupport.HTTP_STATUS,
                                EndpointSupport.HTTP_STATUS));
    }
}

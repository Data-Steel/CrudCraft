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
package nl.datasteel.crudcraft.codegen.writer.controller.method;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;
import nl.datasteel.crudcraft.codegen.writer.controller.TestModelDescriptorFactory;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.EndpointSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiDocComponentTest {

    private final ModelDescriptor model = TestModelDescriptorFactory.create();
    private final OpenApiDocComponent component = new OpenApiDocComponent();

    @Test
    void addsOperationIdToAnnotation() {
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.EXISTS,
                "exists",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> ClassName.get(Void.class),
                List.of(),
                (b, md) -> {}
        );

        MethodSpec.Builder builder = MethodSpec.methodBuilder("exists")
                .returns(ClassName.get(Void.class));

        ControllerMethodContext ctx = new ControllerMethodContext(builder, model, spec, null);
        component.apply(ctx);

        MethodSpec method = builder.build();
        String annotations = method.annotations.toString();

        // Check that operationId is present in the @Operation annotation
        assertTrue(annotations.contains("operationId"), 
                "Should contain operationId attribute");
    }

    @Test
    void generatesUniqueOperationIdForDifferentEntities() {
        // Test for Post entity
        ModelDescriptor postModel = TestModelDescriptorFactory.createWithName("Post");
        EndpointSpec postSpec = new EndpointSpec(
                CrudEndpoint.EXISTS,
                "exists",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> ClassName.get(Void.class),
                List.of(),
                (b, md) -> {}
        );

        MethodSpec.Builder postBuilder = MethodSpec.methodBuilder("exists")
                .returns(ClassName.get(Void.class));

        ControllerMethodContext postCtx = new ControllerMethodContext(postBuilder, postModel, postSpec, null);
        component.apply(postCtx);

        MethodSpec postMethod = postBuilder.build();
        String postAnnotations = postMethod.annotations.toString();

        // Test for Author entity
        ModelDescriptor authorModel = TestModelDescriptorFactory.createWithName("Author");
        MethodSpec.Builder authorBuilder = MethodSpec.methodBuilder("exists")
                .returns(ClassName.get(Void.class));

        ControllerMethodContext authorCtx = new ControllerMethodContext(authorBuilder, authorModel, postSpec, null);
        component.apply(authorCtx);

        MethodSpec authorMethod = authorBuilder.build();
        String authorAnnotations = authorMethod.annotations.toString();

        // Verify that Post has "postExists" and Author has "authorExists"
        assertTrue(postAnnotations.contains("postExists"), 
                "Post entity should have operationId 'postExists'");
        assertTrue(authorAnnotations.contains("authorExists"), 
                "Author entity should have operationId 'authorExists'");
    }

    @Test
    void generatesCorrectOperationIdForAllEndpointTypes() {
        CrudEndpoint[] endpoints = {
            CrudEndpoint.POST, CrudEndpoint.BULK_CREATE, CrudEndpoint.PUT,
            CrudEndpoint.PATCH, CrudEndpoint.BULK_UPDATE, CrudEndpoint.BULK_PATCH,
            CrudEndpoint.BULK_UPSERT, CrudEndpoint.DELETE, CrudEndpoint.BULK_DELETE,
            CrudEndpoint.GET_ONE, CrudEndpoint.GET_ALL, CrudEndpoint.GET_ALL_REF,
            CrudEndpoint.FIND_BY_IDS, CrudEndpoint.EXISTS, CrudEndpoint.COUNT,
            CrudEndpoint.SEARCH, CrudEndpoint.EXPORT, CrudEndpoint.VALIDATE
        };

        String[] expectedIds = {
            "postCreate", "postBulkCreate", "postUpdate",
            "postPatch", "postBulkUpdate", "postBulkPatch",
            "postBulkUpsert", "postDelete", "postBulkDelete",
            "postGetOne", "postGetAll", "postGetAllRef",
            "postFindByIds", "postExists", "postCount",
            "postSearch", "postExport", "postValidate"
        };

        ModelDescriptor postModel = TestModelDescriptorFactory.createWithName("Post");

        for (int i = 0; i < endpoints.length; i++) {
            EndpointSpec spec = new EndpointSpec(
                    endpoints[i],
                    "method",
                    md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                    md -> ClassName.get(Void.class),
                    List.of(),
                    (b, md) -> {}
            );

            MethodSpec.Builder builder = MethodSpec.methodBuilder("method")
                    .returns(ClassName.get(Void.class));

            ControllerMethodContext ctx = new ControllerMethodContext(builder, postModel, spec, null);
            component.apply(ctx);

            MethodSpec method = builder.build();
            String annotations = method.annotations.toString();

            assertTrue(annotations.contains(expectedIds[i]), 
                    String.format("Endpoint %s should have operationId '%s'", 
                            endpoints[i], expectedIds[i]));
        }
    }
}

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

package nl.datasteel.crudcraft.codegen.writer.controller.method;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;
import nl.datasteel.crudcraft.codegen.writer.controller.TestModelDescriptorFactory;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.EndpointSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class OpenApiDocComponentTest {

    private final ModelDescriptor model = TestModelDescriptorFactory.create();
    private final OpenApiDocComponent component = new OpenApiDocComponent();
    private static final Set<CrudEndpoint> ID_ENDPOINTS =
            EnumSet.of(
                    CrudEndpoint.GET_ONE,
                    CrudEndpoint.PUT,
                    CrudEndpoint.PATCH,
                    CrudEndpoint.DELETE,
                    CrudEndpoint.EXISTS);
    private static final Set<CrudEndpoint> MODIFYING_ENDPOINTS =
            EnumSet.of(
                    CrudEndpoint.POST,
                    CrudEndpoint.BULK_CREATE,
                    CrudEndpoint.PUT,
                    CrudEndpoint.PATCH,
                    CrudEndpoint.BULK_UPDATE,
                    CrudEndpoint.BULK_PATCH,
                    CrudEndpoint.BULK_UPSERT,
                    CrudEndpoint.BULK_DELETE);

    @Test
    void addsOperationIdToAnnotation() {
        EndpointSpec spec =
                new EndpointSpec(
                        CrudEndpoint.EXISTS,
                        "exists",
                        md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                        md -> ClassName.get(Void.class),
                        List.of(),
                        (b, md) -> { if (md != null) { md.getName(); } });

        MethodSpec.Builder builder =
                MethodSpec.methodBuilder("exists").returns(ClassName.get(Void.class));

        ControllerMethodContext ctx = new ControllerMethodContext(builder, model, spec, null);
        component.apply(ctx);

        MethodSpec method = builder.build();
        String annotations = method.annotations().toString();

        // Check that operationId is present in the @Operation annotation
        assertTrue(annotations.contains("operationId"), "Should contain operationId attribute");
    }

    @Test
    void generatesUniqueOperationIdForDifferentEntities() {
        // Test for Post entity
        ModelDescriptor postModel = TestModelDescriptorFactory.createWithName("Post");
        EndpointSpec postSpec =
                new EndpointSpec(
                        CrudEndpoint.EXISTS,
                        "exists",
                        md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                        md -> ClassName.get(Void.class),
                        List.of(),
                        (b, md) -> { if (md != null) { md.getName(); } });

        MethodSpec.Builder postBuilder =
                MethodSpec.methodBuilder("exists").returns(ClassName.get(Void.class));

        ControllerMethodContext postCtx =
                new ControllerMethodContext(postBuilder, postModel, postSpec, null);
        component.apply(postCtx);

        MethodSpec postMethod = postBuilder.build();
        String postAnnotations = postMethod.annotations().toString();

        // Test for Author entity
        ModelDescriptor authorModel = TestModelDescriptorFactory.createWithName("Author");
        MethodSpec.Builder authorBuilder =
                MethodSpec.methodBuilder("exists").returns(ClassName.get(Void.class));

        ControllerMethodContext authorCtx =
                new ControllerMethodContext(authorBuilder, authorModel, postSpec, null);
        component.apply(authorCtx);

        MethodSpec authorMethod = authorBuilder.build();
        String authorAnnotations = authorMethod.annotations().toString();

        // Verify that Post has "postExists" and Author has "authorExists"
        assertTrue(
                postAnnotations.contains("postExists"),
                "Post entity should have operationId 'postExists'");
        assertTrue(
                authorAnnotations.contains("authorExists"),
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
            EndpointSpec spec =
                    new EndpointSpec(
                            endpoints[i],
                            "method",
                            md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                            md -> ClassName.get(Void.class),
                            List.of(),
                            (b, md) -> { if (md != null) { md.getName(); } });

            MethodSpec.Builder builder =
                    MethodSpec.methodBuilder("method").returns(ClassName.get(Void.class));

            ControllerMethodContext ctx =
                    new ControllerMethodContext(builder, postModel, spec, null);
            component.apply(ctx);

            MethodSpec method = builder.build();
            String annotations = method.annotations().toString();

            assertTrue(
                    annotations.contains(expectedIds[i]),
                    String.format(
                            "Endpoint %s should have operationId '%s'",
                            endpoints[i], expectedIds[i]));
        }
    }

    @Test
    void documentsEveryEndpointWithExpectedOperationTextAndSuccessResponse() {
        ModelDescriptor postModel = TestModelDescriptorFactory.createWithName("Post");

        for (CrudEndpoint endpoint : CrudEndpoint.values()) {
            String annotations = annotationsFor(endpoint, postModel);

            assertTrue(
                    annotations.contains("operationId = \"" + expectedOperationId(endpoint) + "\""),
                    endpoint + " should expose the expected operation id");
            assertTrue(
                    annotations.contains("summary = \"" + expectedSummary(endpoint) + "\""),
                    endpoint + " should expose the expected summary");
            assertTrue(
                    annotations.contains("description = \"" + expectedDescription(endpoint) + "\""),
                    endpoint + " should expose the expected description");
            assertTrue(
                    annotations.contains(
                            "responseCode = \"" + expectedSuccessCode(endpoint) + "\""),
                    endpoint + " should expose the expected success response code");
            assertTrue(
                    annotations.contains(
                            "description = \""
                                    + expectedSuccessDescription(endpoint)
                                    + "\""),
                    endpoint + " should expose the expected success response description");
        }
    }

    @Test
    void addsNotFoundOnlyForIdEndpoints() {
        ModelDescriptor postModel = TestModelDescriptorFactory.createWithName("Post");

        for (CrudEndpoint endpoint : CrudEndpoint.values()) {
            String annotations = annotationsFor(endpoint, postModel);

            if (ID_ENDPOINTS.contains(endpoint)) {
                assertTrue(
                        annotations.contains("responseCode = \"404\""),
                        endpoint + " should document not-found responses");
                assertTrue(
                        annotations.contains("Post with the specified ID was not found"),
                        endpoint + " should document the not-found model");
            } else {
                assertFalse(
                        annotations.contains("responseCode = \"404\""),
                        endpoint + " should not document not-found responses");
                assertFalse(
                        annotations.contains("Post with the specified ID was not found"),
                        endpoint + " should not document a not-found model");
            }
        }
    }

    @Test
    void addsBadRequestOnlyForModifyingEndpoints() {
        for (CrudEndpoint endpoint : CrudEndpoint.values()) {
            String annotations = annotationsFor(endpoint, model);

            if (MODIFYING_ENDPOINTS.contains(endpoint)) {
                assertTrue(
                        annotations.contains("responseCode = \"400\""),
                        endpoint + " should document bad-request responses");
                assertTrue(
                        annotations.contains("Invalid request data"),
                        endpoint + " should document invalid request data");
            } else {
                assertFalse(
                        annotations.contains("responseCode = \"400\""),
                        endpoint + " should not document bad-request responses");
                assertFalse(
                        annotations.contains("Invalid request data"),
                        endpoint + " should not document invalid request data");
            }
        }
    }

    private String annotationsFor(CrudEndpoint endpoint, ModelDescriptor descriptor) {
        EndpointSpec spec =
                new EndpointSpec(
                        endpoint,
                        "method",
                        md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                        md -> ClassName.get(Void.class),
                        List.of(),
                        (b, md) -> { if (md != null) { md.getName(); } });
        MethodSpec.Builder builder =
                MethodSpec.methodBuilder("method").returns(ClassName.get(Void.class));

        component.apply(new ControllerMethodContext(builder, descriptor, spec, null));

        return builder.build().annotations().toString();
    }

    private String expectedOperationId(CrudEndpoint endpoint) {
        return switch (endpoint) {
            case POST -> "postCreate";
            case BULK_CREATE -> "postBulkCreate";
            case PUT -> "postUpdate";
            case PATCH -> "postPatch";
            case BULK_UPDATE -> "postBulkUpdate";
            case BULK_PATCH -> "postBulkPatch";
            case BULK_UPSERT -> "postBulkUpsert";
            case DELETE -> "postDelete";
            case BULK_DELETE -> "postBulkDelete";
            case GET_ONE -> "postGetOne";
            case GET_ALL -> "postGetAll";
            case GET_ALL_REF -> "postGetAllRef";
            case FIND_BY_IDS -> "postFindByIds";
            case EXISTS -> "postExists";
            case COUNT -> "postCount";
            case SEARCH -> "postSearch";
            case EXPORT -> "postExport";
            case VALIDATE -> "postValidate";
        };
    }

    private String expectedSummary(CrudEndpoint endpoint) {
        return switch (endpoint) {
            case POST -> "Create a new Post";
            case BULK_CREATE -> "Create multiple Post entities";
            case PUT -> "Update an existing Post";
            case PATCH -> "Partially update an existing Post";
            case BULK_UPDATE -> "Update multiple Post entities";
            case BULK_PATCH -> "Partially update multiple Post entities";
            case BULK_UPSERT -> "Create or update multiple Post entities";
            case DELETE -> "Delete a Post";
            case BULK_DELETE -> "Delete multiple Post entities";
            case GET_ONE -> "Get a single Post by ID";
            case GET_ALL -> "Get all Post entities with pagination";
            case GET_ALL_REF -> "Get all Post references with pagination";
            case FIND_BY_IDS -> "Find Post entities by IDs";
            case EXISTS -> "Check if a Post exists";
            case COUNT -> "Count Post entities";
            case SEARCH -> "Search Post entities";
            case EXPORT -> "Export Post entities";
            case VALIDATE -> "Validate Post data";
        };
    }

    private String expectedDescription(CrudEndpoint endpoint) {
        return switch (endpoint) {
            case POST ->
                    "Creates a new Post entity with the provided data. Returns the created entity with generated ID.";
            case BULK_CREATE ->
                    "Creates multiple Post entities in a single request. Returns per-item success and failure details.";
            case PUT -> "Updates an existing Post entity identified by ID. Returns the updated entity.";
            case PATCH ->
                    "Partially updates an existing Post entity identified by ID. Returns the updated entity.";
            case BULK_UPDATE ->
                    "Updates multiple Post entities in a single request. Each entity must include its ID. Returns per-item success and failure details.";
            case BULK_PATCH ->
                    "Partially updates multiple Post entities in a single request. Each entity must include its ID. Returns per-item success and failure details.";
            case BULK_UPSERT ->
                    "Creates or updates multiple Post entities in a single request. Returns per-item success and failure details.";
            case DELETE -> "Permanently deletes a Post entity identified by ID.";
            case BULK_DELETE ->
                    "Permanently deletes multiple Post entities by their IDs. Returns per-item success and failure details.";
            case GET_ONE -> "Retrieves a single Post entity by its unique identifier.";
            case GET_ALL ->
                    "Retrieves all Post entities with support for pagination, sorting, and filtering via search parameters.";
            case GET_ALL_REF ->
                    "Retrieves lightweight references to Post entities with support for pagination and filtering.";
            case FIND_BY_IDS -> "Retrieves multiple Post entities by their IDs in a single request.";
            case EXISTS -> "Checks whether a Post entity with the given ID exists in the system.";
            case COUNT -> "Counts the total number of Post entities matching the search criteria.";
            case SEARCH -> "Searches for Post entities based on the provided search criteria.";
            case EXPORT -> "Exports Post entities in the requested format.";
            case VALIDATE ->
                    "Validates Post data without persisting it. Returns validation errors if any.";
        };
    }

    private String expectedSuccessCode(CrudEndpoint endpoint) {
        return switch (endpoint) {
            case POST, BULK_CREATE, BULK_UPSERT -> "201";
            case DELETE -> "204";
            default -> "200";
        };
    }

    private String expectedSuccessDescription(CrudEndpoint endpoint) {
        return switch (endpoint) {
            case POST -> "Post created successfully";
            case BULK_CREATE -> "Post entities created successfully";
            case PUT -> "Post updated successfully";
            case PATCH -> "Post partially updated successfully";
            case BULK_UPDATE -> "Post entities updated successfully";
            case BULK_PATCH -> "Post entities partially updated successfully";
            case BULK_UPSERT -> "Post entities created or updated successfully";
            case DELETE -> "Post deleted successfully";
            case BULK_DELETE -> "Post entities deleted successfully";
            case GET_ONE -> "Post retrieved successfully";
            case GET_ALL -> "Paginated list of Post entities";
            case GET_ALL_REF -> "Paginated list of Post references";
            case FIND_BY_IDS -> "Post entities retrieved successfully";
            case EXISTS -> "Post existence check result";
            case COUNT -> "Count of Post entities";
            case SEARCH -> "Search results for Post entities";
            case EXPORT -> "Post data exported";
            case VALIDATE -> "Validation results";
        };
    }
}

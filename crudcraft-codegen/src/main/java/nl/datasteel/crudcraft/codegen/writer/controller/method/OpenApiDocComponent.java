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
import com.squareup.javapoet.CodeBlock;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;

/**
 * Adds OpenAPI documentation annotations (@Operation, @ApiResponse, @Parameter) to controller methods.
 */
public class OpenApiDocComponent implements ControllerMethodComponent {
    
    @Override
    public void apply(ControllerMethodContext ctx) {
        CrudEndpoint endpoint = ctx.spec().endpoint();
        String modelName = ctx.model().getName();
        
        // Add @Operation annotation with summary and description
        AnnotationSpec operation = createOperationAnnotation(endpoint, modelName);
        ctx.builder().addAnnotation(operation);
        
        // Add @ApiResponse annotations for common HTTP responses
        addApiResponses(ctx, endpoint, modelName);
    }
    
    /**
     * Creates an @Operation annotation with summary and description based on the endpoint type.
     */
    private AnnotationSpec createOperationAnnotation(CrudEndpoint endpoint, String modelName) {
        String summary = getOperationSummary(endpoint, modelName);
        String description = getOperationDescription(endpoint, modelName);
        
        AnnotationSpec.Builder builder = AnnotationSpec.builder(
                ClassName.get("io.swagger.v3.oas.annotations", "Operation"));
        
        builder.addMember("summary", "$S", summary);
        
        if (description != null && !description.isEmpty()) {
            builder.addMember("description", "$S", description);
        }
        
        return builder.build();
    }
    
    /**
     * Adds @ApiResponse annotations to document possible HTTP responses.
     */
    private void addApiResponses(ControllerMethodContext ctx, CrudEndpoint endpoint, String modelName) {
        ClassName apiResponse = ClassName.get("io.swagger.v3.oas.annotations.responses", "ApiResponse");
        ClassName apiResponses = ClassName.get("io.swagger.v3.oas.annotations.responses", "ApiResponses");
        
        AnnotationSpec.Builder responsesBuilder = AnnotationSpec.builder(apiResponses);
        
        // Add success response
        responsesBuilder.addMember("value", "$L", createSuccessResponse(endpoint, modelName, apiResponse));
        
        // Add common error responses
        if (requiresIdParameter(endpoint)) {
            responsesBuilder.addMember("value", "$L", 
                    createErrorResponse("404", modelName + " not found", apiResponse));
        }
        
        if (isModifyingEndpoint(endpoint)) {
            responsesBuilder.addMember("value", "$L", 
                    createErrorResponse("400", "Invalid request data", apiResponse));
        }
        
        ctx.builder().addAnnotation(responsesBuilder.build());
    }
    
    /**
     * Creates a success response annotation.
     */
    private CodeBlock createSuccessResponse(CrudEndpoint endpoint, String modelName, ClassName apiResponse) {
        String responseCode = getSuccessResponseCode(endpoint);
        String description = getSuccessResponseDescription(endpoint, modelName);
        
        return CodeBlock.builder()
                .add("@$T(responseCode = $S, description = $S)", 
                        apiResponse, responseCode, description)
                .build();
    }
    
    /**
     * Creates an error response annotation.
     */
    private CodeBlock createErrorResponse(String code, String description, 
                                          ClassName apiResponse) {
        return CodeBlock.builder()
                .add("@$T(responseCode = $S, description = $S)", 
                        apiResponse, code, description)
                .build();
    }
    
    /**
     * Gets the operation summary based on the endpoint type.
     */
    private String getOperationSummary(CrudEndpoint endpoint, String modelName) {
        return switch (endpoint) {
            case POST -> "Create a new " + modelName;
            case BULK_CREATE -> "Create multiple " + modelName + " entities";
            case PUT -> "Update an existing " + modelName;
            case PATCH -> "Partially update an existing " + modelName;
            case BULK_UPDATE -> "Update multiple " + modelName + " entities";
            case BULK_PATCH -> "Partially update multiple " + modelName + " entities";
            case BULK_UPSERT -> "Create or update multiple " + modelName + " entities";
            case DELETE -> "Delete a " + modelName;
            case BULK_DELETE -> "Delete multiple " + modelName + " entities";
            case GET_ONE -> "Get a single " + modelName + " by ID";
            case GET_ALL -> "Get all " + modelName + " entities with pagination";
            case GET_ALL_REF -> "Get all " + modelName + " references with pagination";
            case FIND_BY_IDS -> "Find " + modelName + " entities by IDs";
            case EXISTS -> "Check if a " + modelName + " exists";
            case COUNT -> "Count " + modelName + " entities";
            case SEARCH -> "Search " + modelName + " entities";
            case EXPORT -> "Export " + modelName + " entities";
            case VALIDATE -> "Validate " + modelName + " data";
        };
    }
    
    /**
     * Gets the operation description with more details.
     */
    private String getOperationDescription(CrudEndpoint endpoint, String modelName) {
        return switch (endpoint) {
            case POST -> "Creates a new " + modelName + " entity with the provided data. " +
                          "Returns the created entity with generated ID.";
            case BULK_CREATE -> "Creates multiple " + modelName + " entities in a single request. " +
                                "Returns all created entities with generated IDs.";
            case PUT -> "Updates an existing " + modelName + " entity identified by ID. " +
                          "Returns the updated entity.";
            case PATCH -> "Partially updates an existing " + modelName + " entity identified by ID. " +
                          "Returns the updated entity.";
            case BULK_UPDATE -> "Updates multiple " + modelName + " entities in a single request. " +
                                "Each entity must include its ID. Returns all updated entities.";
            case BULK_PATCH -> "Partially updates multiple " + modelName + " entities in a single request. " +
                                "Each entity must include its ID. Returns all updated entities.";
            case BULK_UPSERT -> "Creates or updates multiple " + modelName + " entities in a single request. " +
                                "Returns all created or updated entities.";
            case DELETE -> "Permanently deletes a " + modelName + " entity identified by ID.";
            case BULK_DELETE -> "Permanently deletes multiple " + modelName + " entities by their IDs.";
            case GET_ONE -> "Retrieves a single " + modelName + " entity by its unique identifier.";
            case GET_ALL -> "Retrieves all " + modelName + " entities with support for pagination, " +
                           "sorting, and filtering via search parameters.";
            case GET_ALL_REF -> "Retrieves lightweight references to " + modelName + " entities " +
                               "with support for pagination and filtering.";
            case FIND_BY_IDS -> "Retrieves multiple " + modelName + " entities by their IDs in a single request.";
            case EXISTS -> "Checks whether a " + modelName + " entity with the given ID exists in the system.";
            case COUNT -> "Counts the total number of " + modelName + " entities matching the search criteria.";
            case SEARCH -> "Searches for " + modelName + " entities based on the provided search criteria.";
            case EXPORT -> "Exports " + modelName + " entities in the requested format.";
            case VALIDATE -> "Validates " + modelName + " data without persisting it. " +
                            "Returns validation errors if any.";
        };
    }
    
    /**
     * Gets the success response code for the endpoint.
     */
    private String getSuccessResponseCode(CrudEndpoint endpoint) {
        return switch (endpoint) {
            case POST, BULK_CREATE, BULK_UPSERT -> "201";
            case DELETE, BULK_DELETE -> "204";
            default -> "200";
        };
    }
    
    /**
     * Gets the success response description.
     */
    private String getSuccessResponseDescription(CrudEndpoint endpoint, String modelName) {
        return switch (endpoint) {
            case POST -> modelName + " created successfully";
            case BULK_CREATE -> modelName + " entities created successfully";
            case PUT -> modelName + " updated successfully";
            case PATCH -> modelName + " partially updated successfully";
            case BULK_UPDATE -> modelName + " entities updated successfully";
            case BULK_PATCH -> modelName + " entities partially updated successfully";
            case BULK_UPSERT -> modelName + " entities created or updated successfully";
            case DELETE -> modelName + " deleted successfully";
            case BULK_DELETE -> modelName + " entities deleted successfully";
            case GET_ONE -> modelName + " retrieved successfully";
            case GET_ALL -> "Paginated list of " + modelName + " entities";
            case GET_ALL_REF -> "Paginated list of " + modelName + " references";
            case FIND_BY_IDS -> modelName + " entities retrieved successfully";
            case EXISTS -> modelName + " existence check result";
            case COUNT -> "Count of " + modelName + " entities";
            case SEARCH -> "Search results for " + modelName + " entities";
            case EXPORT -> modelName + " data exported";
            case VALIDATE -> "Validation results";
        };
    }
    
    /**
     * Checks if the endpoint requires an ID parameter.
     */
    private boolean requiresIdParameter(CrudEndpoint endpoint) {
        return endpoint == CrudEndpoint.GET_ONE || 
               endpoint == CrudEndpoint.PUT ||
               endpoint == CrudEndpoint.PATCH || 
               endpoint == CrudEndpoint.DELETE ||
               endpoint == CrudEndpoint.EXISTS;
    }
    
    /**
     * Checks if the endpoint modifies data.
     */
    private boolean isModifyingEndpoint(CrudEndpoint endpoint) {
        return endpoint == CrudEndpoint.POST || 
               endpoint == CrudEndpoint.BULK_CREATE ||
               endpoint == CrudEndpoint.PUT ||
               endpoint == CrudEndpoint.PATCH || 
               endpoint == CrudEndpoint.BULK_UPDATE ||
               endpoint == CrudEndpoint.BULK_PATCH ||
               endpoint == CrudEndpoint.BULK_UPSERT;
    }
}

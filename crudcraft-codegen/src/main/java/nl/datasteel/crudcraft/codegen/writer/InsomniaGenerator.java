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
package nl.datasteel.crudcraft.codegen.writer;

import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.util.Pluralizer;
import nl.datasteel.crudcraft.codegen.util.StringCase;

/**
 * Generates an insomnia.json export file containing all CRUD endpoints
 * for entities annotated with @CrudCrafted.
 * The generated file can be imported into Insomnia for API testing.
 */
public class InsomniaGenerator implements Generator {

    private static final String INSOMNIA_FILE = "insomnia.json";
    private static final Set<ModelDescriptor> allModels = new HashSet<>();

    @Override
    public List<JavaFile> generate(ModelDescriptor model, WriteContext ctx) {
        // Collect all models
        allModels.add(model);
        
        // Don't generate files for each model - we'll generate once at the end
        return List.of();
    }

    @Override
    public boolean requiresCrudEntity() {
        return true;
    }

    @Override
    public int order() {
        return 1000; // Run after all other generators
    }

    @Override
    public void write(ModelDescriptor model, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(model, ctx)) {
            return;
        }
        
        allModels.add(model);
        
        // Generate the Insomnia file with all models collected so far
        // This will be called multiple times, but the final call will have all models
        generateInsomniaFile(ctx);
    }

    private void generateInsomniaFile(WriteContext ctx) {
        try {
            // Try to write to the project root directory
            Path projectRoot = Paths.get("").toAbsolutePath();
            Path insomniaPath = projectRoot.resolve(INSOMNIA_FILE);
            
            String json = buildInsomniaJson();
            
            Files.writeString(insomniaPath, json, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.TRUNCATE_EXISTING);
            
            ctx.env().getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Generated Insomnia collection at: " + insomniaPath
            );
        } catch (IOException e) {
            ctx.env().getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "Failed to generate Insomnia file: " + e.getMessage()
            );
        }
    }

    private String buildInsomniaJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"_type\": \"export\",\n");
        json.append("  \"__export_format\": 4,\n");
        json.append("  \"__export_date\": \"").append(Instant.now().toString()).append("\",\n");
        json.append("  \"__export_source\": \"crudcraft-codegen\",\n");
        json.append("  \"resources\": [\n");

        List<String> resources = new ArrayList<>();

        // Add workspace
        resources.add(generateWorkspace());

        // Add base environment
        resources.add(generateBaseEnvironment());

        // Add resources for each model
        for (ModelDescriptor model : allModels) {
            resources.addAll(generateModelResources(model));
        }

        json.append(String.join(",\n", resources));
        json.append("\n  ]\n");
        json.append("}\n");

        return json.toString();
    }

    private String generateWorkspace() {
        return """
            {
              "_id": "wrk_crudcraft",
              "parentId": null,
              "_type": "workspace",
              "name": "CrudCraft Generated API",
              "scope": "collection"
            }""";
    }

    private String generateBaseEnvironment() {
        StringBuilder env = new StringBuilder();
        env.append("    {\n");
        env.append("      \"_id\": \"env_base\",\n");
        env.append("      \"parentId\": \"wrk_crudcraft\",\n");
        env.append("      \"_type\": \"environment\",\n");
        env.append("      \"name\": \"Base Environment\",\n");
        env.append("      \"data\": {\n");
        env.append("        \"base_url\": \"http://localhost:8080\",\n");
        env.append("        \"jwt_token\": \"\"");

        // Add ID variables for each entity
        for (ModelDescriptor model : allModels) {
            String varName = StringCase.SNAKE.apply(model.getName()) + "_id";
            env.append(",\n        \"").append(varName).append("\": \"\"");
        }

        env.append("\n      }\n");
        env.append("    }");
        return env.toString();
    }

    private List<String> generateModelResources(ModelDescriptor model) {
        List<String> resources = new ArrayList<>();
        String entityName = model.getName();
        String pluralName = Pluralizer.pluralize(entityName);
        String endpoint = StringCase.KEBAB.apply(pluralName);
        String folderId = "fld_" + StringCase.SNAKE.apply(pluralName);
        String idVar = StringCase.SNAKE.apply(entityName) + "_id";

        // Add folder
        resources.add(generateFolder(folderId, formatDisplayName(pluralName)));

        // Get effective endpoints
        Set<CrudEndpoint> endpoints = resolveEndpoints(model);

        // Generate requests for each endpoint
        if (endpoints.contains(CrudEndpoint.GET_ALL)) {
            resources.add(generateListRequest(model, folderId, endpoint, idVar));
        }
        if (endpoints.contains(CrudEndpoint.GET_ONE)) {
            resources.add(generateGetRequest(model, folderId, endpoint, idVar));
        }
        if (endpoints.contains(CrudEndpoint.POST)) {
            resources.add(generateCreateRequest(model, folderId, endpoint, idVar));
        }
        if (endpoints.contains(CrudEndpoint.PUT)) {
            resources.add(generateUpdateRequest(model, folderId, endpoint, idVar));
        }
        if (endpoints.contains(CrudEndpoint.PATCH)) {
            resources.add(generatePatchRequest(model, folderId, endpoint, idVar));
        }
        if (endpoints.contains(CrudEndpoint.DELETE)) {
            resources.add(generateDeleteRequest(model, folderId, endpoint, idVar));
        }
        if (endpoints.contains(CrudEndpoint.BULK_CREATE)) {
            resources.add(generateBulkCreateRequest(model, folderId, endpoint));
        }
        if (endpoints.contains(CrudEndpoint.BULK_UPDATE)) {
            resources.add(generateBulkUpdateRequest(model, folderId, endpoint, idVar));
        }
        if (endpoints.contains(CrudEndpoint.BULK_PATCH)) {
            resources.add(generateBulkPatchRequest(model, folderId, endpoint, idVar));
        }
        if (endpoints.contains(CrudEndpoint.BULK_UPSERT)) {
            resources.add(generateBulkUpsertRequest(model, folderId, endpoint));
        }
        if (endpoints.contains(CrudEndpoint.FIND_BY_IDS)) {
            resources.add(generateFindByIdsRequest(model, folderId, endpoint, idVar));
        }
        if (endpoints.contains(CrudEndpoint.BULK_DELETE)) {
            resources.add(generateBulkDeleteRequest(model, folderId, endpoint, idVar));
        }

        return resources;
    }

    private Set<CrudEndpoint> resolveEndpoints(ModelDescriptor model) {
        Set<CrudEndpoint> allowed;
        if (model.getEndpointPolicy() == CrudTemplate.class) {
            allowed = new HashSet<>(model.getTemplate().resolveEndpoints());
        } else {
            try {
                CrudEndpointPolicy policy = model.getEndpointPolicy()
                        .getDeclaredConstructor().newInstance();
                allowed = new HashSet<>(policy.resolveEndpoints());
            } catch (Exception e) {
                // Fall back to template if policy instantiation fails
                allowed = new HashSet<>(model.getTemplate().resolveEndpoints());
            }
        }
        Arrays.asList(model.getOmitEndpoints()).forEach(allowed::remove);
        allowed.addAll(Arrays.asList(model.getIncludeEndpoints()));
        return allowed;
    }

    private String generateFolder(String folderId, String name) {
        return String.format("""
            {
              "_id": "%s",
              "parentId": "wrk_crudcraft",
              "_type": "request_group",
              "name": "%s"
            }""", folderId, name);
    }

    private String generateListRequest(ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_list";
        String testScript = String.format(
            "const resp = JSON.parse(response.body); if(Array.isArray(resp) && resp.length){insomnia.environment.set('%s', resp[0].id);}",
            idVar
        );
        
        return String.format("""
            {
              "_id": "%s",
              "parentId": "%s",
              "_type": "request",
              "name": "List",
              "method": "GET",
              "url": "{{ base_url }}/%s",
              "authentication": {
                "type": "bearer",
                "token": "{{ jwt_token }}"
              },
              "settingTestScript": "%s"
            }""", reqId, folderId, endpoint, escapeJson(testScript));
    }

    private String generateGetRequest(ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_get";
        return String.format("""
            {
              "_id": "%s",
              "parentId": "%s",
              "_type": "request",
              "name": "Get",
              "method": "GET",
              "url": "{{ base_url }}/%s/{{ %s }}",
              "authentication": {
                "type": "bearer",
                "token": "{{ jwt_token }}"
              }
            }""", reqId, folderId, endpoint, idVar);
    }

    private String generateCreateRequest(ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_create";
        String sampleBody = generateSampleRequestBody(model);
        String testScript = String.format(
            "const resp = JSON.parse(response.body); if(resp.id){insomnia.environment.set('%s', resp.id);}",
            idVar
        );
        
        return String.format("""
            {
              "_id": "%s",
              "parentId": "%s",
              "_type": "request",
              "name": "Create",
              "method": "POST",
              "url": "{{ base_url }}/%s",
              "authentication": {
                "type": "bearer",
                "token": "{{ jwt_token }}"
              },
              "body": {
                "mimeType": "application/json",
                "text": "%s"
              },
              "headers": [
                {
                  "name": "Content-Type",
                  "value": "application/json"
                }
              ],
              "settingTestScript": "%s"
            }""", reqId, folderId, endpoint, escapeJson(sampleBody), escapeJson(testScript));
    }

    private String generateUpdateRequest(ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_update";
        String sampleBody = generateSampleRequestBody(model);
        
        return String.format("""
            {
              "_id": "%s",
              "parentId": "%s",
              "_type": "request",
              "name": "Update",
              "method": "PUT",
              "url": "{{ base_url }}/%s/{{ %s }}",
              "authentication": {
                "type": "bearer",
                "token": "{{ jwt_token }}"
              },
              "body": {
                "mimeType": "application/json",
                "text": "%s"
              },
              "headers": [
                {
                  "name": "Content-Type",
                  "value": "application/json"
                }
              ]
            }""", reqId, folderId, endpoint, idVar, escapeJson(sampleBody));
    }

    private String generatePatchRequest(ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_patch";
        String sampleBody = generateSampleRequestBody(model);
        
        return String.format("""
            {
              "_id": "%s",
              "parentId": "%s",
              "_type": "request",
              "name": "Patch",
              "method": "PATCH",
              "url": "{{ base_url }}/%s/{{ %s }}",
              "authentication": {
                "type": "bearer",
                "token": "{{ jwt_token }}"
              },
              "body": {
                "mimeType": "application/json",
                "text": "%s"
              },
              "headers": [
                {
                  "name": "Content-Type",
                  "value": "application/json"
                }
              ]
            }""", reqId, folderId, endpoint, idVar, escapeJson(sampleBody));
    }

    private String generateDeleteRequest(ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_delete";
        
        return String.format("""
            {
              "_id": "%s",
              "parentId": "%s",
              "_type": "request",
              "name": "Delete",
              "method": "DELETE",
              "url": "{{ base_url }}/%s/{{ %s }}",
              "authentication": {
                "type": "bearer",
                "token": "{{ jwt_token }}"
              }
            }""", reqId, folderId, endpoint, idVar);
    }

    private String generateBulkCreateRequest(ModelDescriptor model, String folderId, String endpoint) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_bulk_create";
        String sampleBody = "[" + generateSampleRequestBody(model) + "]";
        
        return String.format("""
            {
              "_id": "%s",
              "parentId": "%s",
              "_type": "request",
              "name": "Bulk Create",
              "method": "POST",
              "url": "{{ base_url }}/%s/batch",
              "authentication": {
                "type": "bearer",
                "token": "{{ jwt_token }}"
              },
              "body": {
                "mimeType": "application/json",
                "text": "%s"
              },
              "headers": [
                {
                  "name": "Content-Type",
                  "value": "application/json"
                }
              ]
            }""", reqId, folderId, endpoint, escapeJson(sampleBody));
    }

    private String generateBulkUpdateRequest(ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_bulk_update";
        String sampleBody = String.format("[{\\\"id\\\": \\\"{{ %s }}\\\", \\\"data\\\": %s}]", 
            idVar, generateSampleRequestBody(model));
        
        return String.format("""
            {
              "_id": "%s",
              "parentId": "%s",
              "_type": "request",
              "name": "Bulk Update",
              "method": "PUT",
              "url": "{{ base_url }}/%s/batch",
              "authentication": {
                "type": "bearer",
                "token": "{{ jwt_token }}"
              },
              "body": {
                "mimeType": "application/json",
                "text": "%s"
              },
              "headers": [
                {
                  "name": "Content-Type",
                  "value": "application/json"
                }
              ]
            }""", reqId, folderId, endpoint, escapeJson(sampleBody));
    }

    private String generateBulkPatchRequest(ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_bulk_patch";
        String sampleBody = String.format("[{\\\"id\\\": \\\"{{ %s }}\\\", \\\"data\\\": %s}]", 
            idVar, generateSampleRequestBody(model));
        
        return String.format("""
            {
              "_id": "%s",
              "parentId": "%s",
              "_type": "request",
              "name": "Bulk Patch",
              "method": "PATCH",
              "url": "{{ base_url }}/%s/batch",
              "authentication": {
                "type": "bearer",
                "token": "{{ jwt_token }}"
              },
              "body": {
                "mimeType": "application/json",
                "text": "%s"
              },
              "headers": [
                {
                  "name": "Content-Type",
                  "value": "application/json"
                }
              ]
            }""", reqId, folderId, endpoint, escapeJson(sampleBody));
    }

    private String generateBulkUpsertRequest(ModelDescriptor model, String folderId, String endpoint) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_bulk_upsert";
        String sampleBody = "[" + generateSampleRequestBody(model) + "]";
        
        return String.format("""
            {
              "_id": "%s",
              "parentId": "%s",
              "_type": "request",
              "name": "Bulk Upsert",
              "method": "POST",
              "url": "{{ base_url }}/%s/batch",
              "authentication": {
                "type": "bearer",
                "token": "{{ jwt_token }}"
              },
              "body": {
                "mimeType": "application/json",
                "text": "%s"
              },
              "headers": [
                {
                  "name": "Content-Type",
                  "value": "application/json"
                }
              ]
            }""", reqId, folderId, endpoint, escapeJson(sampleBody));
    }

    private String generateFindByIdsRequest(ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_bulk_ids";
        String sampleBody = String.format("[\\\"{{ %s }}\\\"]", idVar);
        
        return String.format("""
            {
              "_id": "%s",
              "parentId": "%s",
              "_type": "request",
              "name": "Bulk Ids",
              "method": "POST",
              "url": "{{ base_url }}/%s/batch/ids",
              "authentication": {
                "type": "bearer",
                "token": "{{ jwt_token }}"
              },
              "body": {
                "mimeType": "application/json",
                "text": "%s"
              },
              "headers": [
                {
                  "name": "Content-Type",
                  "value": "application/json"
                }
              ]
            }""", reqId, folderId, endpoint, escapeJson(sampleBody));
    }

    private String generateBulkDeleteRequest(ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_bulk_delete";
        String sampleBody = String.format("[\\\"{{ %s }}\\\"]", idVar);
        
        return String.format("""
            {
              "_id": "%s",
              "parentId": "%s",
              "_type": "request",
              "name": "Bulk Delete",
              "method": "DELETE",
              "url": "{{ base_url }}/%s/batch",
              "authentication": {
                "type": "bearer",
                "token": "{{ jwt_token }}"
              },
              "body": {
                "mimeType": "application/json",
                "text": "%s"
              }
            }""", reqId, folderId, endpoint, escapeJson(sampleBody));
    }

    private String generateSampleRequestBody(ModelDescriptor model) {
        StringBuilder body = new StringBuilder("{");
        
        // Use same logic as DtoGenerator for request fields
        List<FieldDescriptor> requestFields = model.getFields().stream()
            .filter(fd -> fd.inRequest()
                    || (fd.inDto() && fd.getRelType() != RelationshipType.NONE
                    && !fd.isEmbedded()))
            .toList();
        
        for (int i = 0; i < requestFields.size(); i++) {
            FieldDescriptor field = requestFields.get(i);
            if (i > 0) {
                body.append(", ");
            }
            body.append("\\\"").append(field.getName()).append("\\\": ");
            body.append(getSampleValue(field));
        }
        
        body.append("}");
        return body.toString();
    }

    private String getSampleValue(FieldDescriptor field) {
        String typeName = field.getType().toString();
        
        // Handle relationships - use environment variable
        if (field.getRelType() != null && field.getRelType() == RelationshipType.MANY_TO_ONE) {
            String relatedType = field.getType().toString();
            if (relatedType.contains(".")) {
                relatedType = relatedType.substring(relatedType.lastIndexOf('.') + 1);
            }
            String varName = StringCase.SNAKE.apply(relatedType) + "_id";
            return "\\\"{{ " + varName + " }}\\\"";
        }
        
        // Handle primitive types and common objects
        if (typeName.contains("String")) {
            return "\\\"\\\"";
        } else if (typeName.contains("Integer") || typeName.contains("int") || 
                   typeName.contains("Long") || typeName.contains("long")) {
            return "0";
        } else if (typeName.contains("Double") || typeName.contains("double") || 
                   typeName.contains("Float") || typeName.contains("float") ||
                   typeName.contains("BigDecimal")) {
            return "0.0";
        } else if (typeName.contains("Boolean") || typeName.contains("boolean")) {
            return "false";
        } else if (typeName.contains("Instant") || typeName.contains("LocalDate") || 
                   typeName.contains("OffsetDateTime") || typeName.contains("ZonedDateTime")) {
            return "\\\"2024-01-01T00:00:00Z\\\"";
        } else if (typeName.contains("Set") || typeName.contains("List")) {
            return "[]";
        }
        
        return "null";
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private String formatDisplayName(String name) {
        // Convert camelCase or PascalCase to Title Case with spaces
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                result.append(" ");
            }
            result.append(c);
        }
        return result.toString();
    }
}

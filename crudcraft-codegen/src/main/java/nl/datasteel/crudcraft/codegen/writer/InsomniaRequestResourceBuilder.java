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

package nl.datasteel.crudcraft.codegen.writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.util.CollectionTypes;
import nl.datasteel.crudcraft.codegen.util.Pluralizer;
import nl.datasteel.crudcraft.codegen.util.StringCase;


/** Builds Insomnia request/folder resources for one generated model. */
final class InsomniaRequestResourceBuilder {

    private InsomniaRequestResourceBuilder() {}

    static List<String> generateModelResources(ModelDescriptor model) {
        List<String> resources = new ArrayList<>();
        String entityName = model.getName();
        String pluralName = Pluralizer.pluralize(entityName);
        String endpoint = StringCase.KEBAB.apply(pluralName);
        String folderId = "fld_" + StringCase.SNAKE.apply(pluralName);
        String idVar = StringCase.SNAKE.apply(entityName) + "_id";

        resources.add(generateFolder(folderId, formatDisplayName(pluralName)));

        Set<CrudEndpoint> endpoints = resolveEndpoints(model);
        if (endpoints.contains(CrudEndpoint.GET_ALL)) {
            resources.add(generateListRequest(model, folderId, endpoint, idVar));
        }
        if (endpoints.contains(CrudEndpoint.GET_ALL_REF)) {
            resources.add(generateListRefRequest(model, folderId, endpoint));
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
        if (endpoints.contains(CrudEndpoint.EXISTS)) {
            resources.add(generateExistsRequest(model, folderId, endpoint, idVar));
        }
        if (endpoints.contains(CrudEndpoint.COUNT)) {
            resources.add(generateCountRequest(model, folderId, endpoint));
        }
        if (endpoints.contains(CrudEndpoint.SEARCH)) {
            resources.add(generateSearchRequest(model, folderId, endpoint));
        }
        if (endpoints.contains(CrudEndpoint.VALIDATE)) {
            resources.add(generateValidateRequest(model, folderId, endpoint));
        }
        if (endpoints.contains(CrudEndpoint.EXPORT)) {
            resources.add(generateExportRequest(model, folderId, endpoint));
        }

        return resources;
    }

    static Set<CrudEndpoint> resolveEndpoints(ModelDescriptor model) {
        Set<CrudEndpoint> allowed;
        if (model.getEndpointPolicy() == CrudTemplate.class) {
            allowed = new HashSet<>(model.getTemplate().resolveEndpoints());
        } else {
            try {
                CrudEndpointPolicy policy =
                        model.getEndpointPolicy().getDeclaredConstructor().newInstance();
                allowed = new HashSet<>(policy.resolveEndpoints());
            } catch (ReflectiveOperationException e) {
                allowed = new HashSet<>(model.getTemplate().resolveEndpoints());
            }
        }
        Arrays.asList(model.getOmitEndpoints()).forEach(allowed::remove);
        allowed.addAll(Arrays.asList(model.getIncludeEndpoints()));
        if (hasSearchFields(model)) {
            allowed.add(CrudEndpoint.SEARCH);
        } else {
            allowed.remove(CrudEndpoint.SEARCH);
        }
        return allowed;
    }

    static boolean hasSearchFields(ModelDescriptor model) {
        return model.getFields().stream().anyMatch(FieldDescriptor::isSearchable);
    }

    static String generateFolder(String folderId, String name) {
        return jsonFormat(
                """
                {
                        "_id": "%s",
                        "parentId": "wrk_crudcraft",
                        "_type": "request_group",
                        "name": "%s"
                }\
                """,
                folderId, name);
    }

    static String generateListRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_list";
        String testScript =
                jsonFormat(
                        "const resp = JSON.parse(response.body); if(Array.isArray(resp) &&"
                                + " resp.length){insomnia.environment.set('%s', resp[0].id);}",
                        idVar);

        return jsonFormat(
                """
                {
                        "_id": "%s",
                        "parentId": "%s",
                        "_type": "request",
                        "name": "%s",
                        "method": "GET",
                        "url": "{{ base_url }}/%s",
                        "authentication": {
                                "type": "bearer",
                                "token": "{{ jwt_token }}"
                        },
                        "settingTestScript": "%s"
                }\
                """,
                reqId, folderId, CollectionTypes.LIST, endpoint, escapeJson(testScript));
    }

    static String generateGetRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_get";
        return jsonFormat(
                """
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
                }\
                """,
                reqId, folderId, endpoint, idVar);
    }

    static String generateListRefRequest(
            ModelDescriptor model, String folderId, String endpoint) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_list_ref";
        return jsonFormat(
                """
                {
                        "_id": "%s",
                        "parentId": "%s",
                        "_type": "request",
                        "name": "List Ref",
                        "method": "GET",
                        "url": "{{ base_url }}/%s/ref",
                        "authentication": {
                                "type": "bearer",
                                "token": "{{ jwt_token }}"
                        }
                }\
                """,
                reqId, folderId, endpoint);
    }

    static String generateCreateRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_create";
        String sampleBody = generateSampleRequestBody(model);
        String testScript =
                jsonFormat(
                        "const resp = JSON.parse(response.body);"
                                + " if(resp.id){insomnia.environment.set('%s', resp.id);}",
                        idVar);

        return jsonFormat(
                """
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
                }\
                """,
                reqId, folderId, endpoint, escapeJson(sampleBody), escapeJson(testScript));
    }

    static String generateUpdateRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_update";
        String sampleBody = generateSampleRequestBody(model);

        return jsonFormat(
                """
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
                }\
                """,
                reqId, folderId, endpoint, idVar, escapeJson(sampleBody));
    }

    static String generatePatchRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_patch";
        String sampleBody = generateSampleRequestBody(model);

        return jsonFormat(
                """
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
                }\
                """,
                reqId, folderId, endpoint, idVar, escapeJson(sampleBody));
    }

    static String generateDeleteRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_delete";

        return jsonFormat(
                """
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
                }\
                """,
                reqId, folderId, endpoint, idVar);
    }

    static String generateBulkCreateRequest(
            ModelDescriptor model, String folderId, String endpoint) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_bulk_create";
        String sampleBody = "[" + generateSampleRequestBody(model) + "]";

        return jsonFormat(
                """
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
                }\
                """,
                reqId, folderId, endpoint, escapeJson(sampleBody));
    }

    static String generateBulkUpdateRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_bulk_update";
        String sampleBody =
                jsonFormat(
                        "[{\\\"id\\\": \\\"{{ %s }}\\\", \\\"data\\\": %s}]",
                        idVar, generateSampleRequestBody(model));

        return jsonFormat(
                """
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
                }\
                """,
                reqId, folderId, endpoint, escapeJson(sampleBody));
    }

    static String generateBulkPatchRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_bulk_patch";
        String sampleBody =
                jsonFormat(
                        "[{\\\"id\\\": \\\"{{ %s }}\\\", \\\"data\\\": %s}]",
                        idVar, generateSampleRequestBody(model));

        return jsonFormat(
                """
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
                }\
                """,
                reqId, folderId, endpoint, escapeJson(sampleBody));
    }

    static String generateBulkUpsertRequest(
            ModelDescriptor model, String folderId, String endpoint) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_bulk_upsert";
        String sampleBody = "[" + generateSampleRequestBody(model) + "]";

        return jsonFormat(
                """
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
                }\
                """,
                reqId, folderId, endpoint, escapeJson(sampleBody));
    }

    static String generateFindByIdsRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_bulk_ids";
        String sampleBody = jsonFormat("[\\\"{{ %s }}\\\"]", idVar);

        return jsonFormat(
                """
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
                }\
                """,
                reqId, folderId, endpoint, escapeJson(sampleBody));
    }

    static String generateBulkDeleteRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_bulk_delete";
        String sampleBody = jsonFormat("[\\\"{{ %s }}\\\"]", idVar);

        return jsonFormat(
                """
                {
                        "_id": "%s",
                        "parentId": "%s",
                        "_type": "request",
                        "name": "Bulk Delete",
                        "method": "DELETE",
                        "url": "{{ base_url }}/%s/batch/delete",
                        "authentication": {
                                "type": "bearer",
                                "token": "{{ jwt_token }}"
                        },
                        "body": {
                                "mimeType": "application/json",
                                "text": "%s"
                        }
                }\
                """,
                reqId, folderId, endpoint, escapeJson(sampleBody));
    }

    static String generateExistsRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_exists";
        return jsonFormat(
                """
                {
                        "_id": "%s",
                        "parentId": "%s",
                        "_type": "request",
                        "name": "Exists",
                        "method": "GET",
                        "url": "{{ base_url }}/%s/exists/{{ %s }}",
                        "authentication": {
                                "type": "bearer",
                                "token": "{{ jwt_token }}"
                        }
                }\
                """,
                reqId, folderId, endpoint, idVar);
    }

    static String generateCountRequest(
            ModelDescriptor model, String folderId, String endpoint) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_count";
        return jsonFormat(
                """
                {
                        "_id": "%s",
                        "parentId": "%s",
                        "_type": "request",
                        "name": "Count",
                        "method": "GET",
                        "url": "{{ base_url }}/%s/count",
                        "authentication": {
                                "type": "bearer",
                                "token": "{{ jwt_token }}"
                        }
                }\
                """,
                reqId, folderId, endpoint);
    }

    static String generateSearchRequest(
            ModelDescriptor model, String folderId, String endpoint) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_search";
        return jsonFormat(
                """
                {
                        "_id": "%s",
                        "parentId": "%s",
                        "_type": "request",
                        "name": "Search",
                        "method": "GET",
                        "url": "{{ base_url }}/%s/search",
                        "authentication": {
                                "type": "bearer",
                                "token": "{{ jwt_token }}"
                        }
                }\
                """,
                reqId, folderId, endpoint);
    }

    static String generateValidateRequest(
            ModelDescriptor model, String folderId, String endpoint) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_validate";
        String sampleBody = generateSampleRequestBody(model);
        return jsonFormat(
                """
                {
                        "_id": "%s",
                        "parentId": "%s",
                        "_type": "request",
                        "name": "Validate",
                        "method": "POST",
                        "url": "{{ base_url }}/%s/validate",
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
                }\
                """,
                reqId, folderId, endpoint, escapeJson(sampleBody));
    }

    static String generateExportRequest(
            ModelDescriptor model, String folderId, String endpoint) {
        String pluralName = Pluralizer.pluralize(model.getName());
        String reqId = "req_" + StringCase.SNAKE.apply(pluralName) + "_export";
        return jsonFormat(
                """
                {
                        "_id": "%s",
                        "parentId": "%s",
                        "_type": "request",
                        "name": "Export",
                        "method": "GET",
                        "url": "{{ base_url }}/%s/export?format=csv&limit=100",
                        "authentication": {
                                "type": "bearer",
                                "token": "{{ jwt_token }}"
                        }
                }\
                """,
                reqId, folderId, endpoint);
    }

    static String generateSampleRequestBody(ModelDescriptor model) {
        return InsomniaPayloadHelper.generateSampleRequestBody(model);
    }

    static String escapeJson(String str) {
        return InsomniaPayloadHelper.escapeJson(str);
    }

    static String jsonFormat(String template, Object... args) {
        return InsomniaPayloadHelper.jsonFormat(template, args);
    }

    static String formatDisplayName(String name) {
        return InsomniaPayloadHelper.formatDisplayName(name);
    }
}


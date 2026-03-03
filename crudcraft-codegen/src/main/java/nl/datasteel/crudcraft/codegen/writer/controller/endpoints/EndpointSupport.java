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
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;

/**
 * Common {@link ClassName} constants used by endpoint generators.
 */
public final class EndpointSupport {
    private EndpointSupport() {}

    public static final ClassName RESP_ENTITY = ClassName.get("org.springframework.http", "ResponseEntity");
    public static final ClassName UUID_CLASS = ClassName.get(UUID.class);
    public  static final ClassName REQUEST_BODY = ClassName.get("org.springframework.web.bind.annotation", "RequestBody");
    public static final ClassName PATH_VAR = ClassName.get("org.springframework.web.bind.annotation", "PathVariable");
    public static final ClassName MODEL_ATTR = ClassName.get("org.springframework.web.bind.annotation", "ModelAttribute");
    public static final ClassName REQUEST_PARAM = ClassName.get("org.springframework.web.bind.annotation", "RequestParam");
    public static final ClassName VALID = ClassName.get("jakarta.validation", "Valid");
    public static final ClassName IDENTIFIED = ClassName.get("nl.datasteel.crudcraft.runtime", "Identified");
    public static final ClassName GET_MAPPING = ClassName.get("org.springframework.web.bind.annotation", "GetMapping");
    public static final ClassName POST_MAPPING = ClassName.get("org.springframework.web.bind.annotation", "PostMapping");
    public static final ClassName PUT_MAPPING = ClassName.get("org.springframework.web.bind.annotation", "PutMapping");
    public static final ClassName PATCH_MAPPING = ClassName.get("org.springframework.web.bind.annotation", "PatchMapping");
    public static final ClassName DELETE_MAPPING = ClassName.get("org.springframework.web.bind.annotation", "DeleteMapping");
    public static final ClassName REQUEST_MAPPING = ClassName.get("org.springframework.web.bind.annotation", "RequestMapping");
    public static final ClassName REQUEST_METHOD = ClassName.get("org.springframework.web.bind.annotation", "RequestMethod");
    public static final ClassName STREAMING_BODY = ClassName.get(
            "org.springframework.web.servlet.mvc.method.annotation", "StreamingResponseBody");
    public static final ClassName LIST = ClassName.get(List.class);
    public static final ClassName COLLECTION = ClassName.get(Collection.class);
    public static final ClassName MAP = ClassName.get(Map.class);
    public static final ClassName PAGINATED_RESPONSE = ClassName.get(
            "nl.datasteel.crudcraft.runtime.controller.response", "PaginatedResponse");
    public static final ClassName FIELD_SECURITY_UTIL = ClassName.get(
            "nl.datasteel.crudcraft.runtime.security", "FieldSecurityUtil");
    public static final ClassName PAGE = ClassName.get("org.springframework.data.domain", "Page");
    public static final ClassName PAGEABLE = ClassName.get("org.springframework.data.domain", "Pageable");
    public static final ClassName PAGE_REQUEST = ClassName.get("org.springframework.data.domain", "PageRequest");
    public static final ClassName EXPORT_UTIL = ClassName.get("nl.datasteel.crudcraft.runtime.util", "ExportUtil");
    public static final ClassName EXPORT_SERVICE = ClassName.get("nl.datasteel.crudcraft.runtime.service", "ExportService");
    public static final ClassName EXPORT_REQUEST = ClassName.get("nl.datasteel.crudcraft.runtime.export", "ExportRequest");
    public static final ClassName NO_SUCH_ELEMENT = ClassName.get("java.util", "NoSuchElementException");
    public static final ClassName ITERATOR = ClassName.get("java.util", "Iterator");
    public static final ClassName COLLECTIONS = ClassName.get("java.util", "Collections");
    public static final ClassName OUTPUT_STREAM = ClassName.get("java.io", "OutputStream");
    public static final ClassName BI_CONSUMER = ClassName.get("java.util.function", "BiConsumer");
    public static final ClassName HTTP_HEADERS = ClassName.get("org.springframework.http", "HttpHeaders");
    public static final ClassName REQUEST_PART = ClassName.get("org.springframework.web.bind.annotation", "RequestPart");
    public static final ClassName MULTIPART_FILE = ClassName.get("org.springframework.web.multipart", "MultipartFile");
    public static final ClassName MEDIA_TYPE = ClassName.get("org.springframework.http", "MediaType");
    public static final ClassName IO_EXCEPTION = ClassName.get("java.io", "IOException");

    /**
     * Generates code that reads bytes from MultipartFile parameters and sets them
     * on the request DTO for each writable {@code @Lob} field. Each LOB field has its own
     * MultipartFile parameter named after the field. An empty file upload explicitly
     * sets the field to {@code null}, allowing LOB clearing via PATCH.
     */
    public static void addFileToRequestCode(MethodSpec.Builder mb, ModelDescriptor md) {
        for (FieldDescriptor lf : md.getRequestLobFields()) {
            String fieldName = lf.getName();
            String setter = "set" + Character.toUpperCase(fieldName.charAt(0))
                    + fieldName.substring(1);
            mb.beginControlFlow("if ($L != null)", fieldName);
            mb.beginControlFlow("if ($L.isEmpty())", fieldName);
            mb.addStatement("request.$L(null)", setter);
            mb.nextControlFlow("else");
            mb.beginControlFlow("try");
            mb.addStatement("request.$L($L.getBytes())", setter, fieldName);
            mb.nextControlFlow("catch ($T e)", IO_EXCEPTION);
            mb.addStatement("throw new $T($T.BAD_REQUEST, $S + $S, e)",
                    ClassName.get("org.springframework.web.server", "ResponseStatusException"),
                    ClassName.get("org.springframework.http", "HttpStatus"),
                    "Failed to read uploaded file for field: ",
                    fieldName);
            mb.endControlFlow();
            mb.endControlFlow();
            mb.endControlFlow();
        }
    }

    /**
     * Builds the list of parameter functions for multipart LOB endpoints.
     * Includes the request DTO part and a MultipartFile part for each writable LOB field.
     */
    public static List<java.util.function.Function<ModelDescriptor, ParameterSpec>> lobParams(
            ClassName requestDtoClass, ModelDescriptor modelDescriptor) {
        List<java.util.function.Function<ModelDescriptor, ParameterSpec>> params = new java.util.ArrayList<>();
        params.add(md -> ParameterSpec.builder(requestDtoClass, "request")
                .addAnnotation(AnnotationSpec.builder(REQUEST_PART)
                        .addMember("value", "$S", "data").build())
                .build());
        for (FieldDescriptor lf : modelDescriptor.getRequestLobFields()) {
            String fieldName = lf.getName();
            params.add(md -> ParameterSpec.builder(MULTIPART_FILE, fieldName)
                    .addAnnotation(AnnotationSpec.builder(REQUEST_PART)
                            .addMember("value", "$S", fieldName)
                            .addMember("required", "$L", false).build())
                    .build());
        }
        return params;
    }
}

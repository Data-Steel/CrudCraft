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
import com.squareup.javapoet.ParameterizedTypeName;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
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
    public static final ClassName ARRAY_LIST = ClassName.get("java.util", "ArrayList");
    public static final ClassName HASH_SET = ClassName.get("java.util", "HashSet");
    public static final ClassName SET = ClassName.get("java.util", "Set");

    private static final String JAKARTA_NOT_NULL = "jakarta.validation.constraints.NotNull";
    private static final String JAVAX_NOT_NULL = "javax.validation.constraints.NotNull";

    /**
     * Returns {@code true} if the LOB field's type is a {@link java.util.List},
     * {@link java.util.Collection}, or {@link java.util.Set}, meaning it holds
     * multiple files rather than a single byte array.
     */
    static boolean isCollectionLobField(FieldDescriptor fd) {
        if (!(fd.getType() instanceof DeclaredType dt)) {
            return false;
        }
        String qualifiedName = ((TypeElement) dt.asElement()).getQualifiedName().toString();
        return qualifiedName.equals("java.util.List")
                || qualifiedName.equals("java.util.Collection")
                || qualifiedName.equals("java.util.Set");
    }

    /**
     * Returns {@code true} if the LOB field's collection type is {@link java.util.Set}.
     * Used to select the right concrete collection type when generating bytes conversion code.
     */
    private static boolean isSetLobField(FieldDescriptor fd) {
        if (!(fd.getType() instanceof DeclaredType dt)) {
            return false;
        }
        return ((TypeElement) dt.asElement()).getQualifiedName().toString()
                .equals("java.util.Set");
    }

    /**
     * Returns {@code true} if the LOB field carries a {@code @NotNull} validation annotation,
     * indicating the file part is mandatory and the generated {@code @RequestPart} should use
     * {@code required = true}.
     */
    static boolean isRequiredLobField(FieldDescriptor fd) {
        return fd.getValidations().stream()
                .anyMatch(as -> as.type.toString().equals(JAKARTA_NOT_NULL)
                        || as.type.toString().equals(JAVAX_NOT_NULL));
    }

    /**
     * Generates code that reads bytes from MultipartFile parameters and sets them
     * on the request DTO for each writable {@code @Lob} field. Each LOB field has its own
     * MultipartFile (or {@code List<MultipartFile>} for collection-typed fields) parameter
     * named after the field.
     *
     * <p>Behavior by parameter presence:
     * <ul>
     *   <li>Part absent (parameter is {@code null}): no change to the DTO field.</li>
     *   <li>Part present but file(s) empty: sets the DTO field to {@code null}, allowing
     *       LOB clearing via PATCH.</li>
     *   <li>Part present with file data: sets the DTO field to the uploaded byte(s).</li>
     * </ul>
     */
    public static void addFileToRequestCode(MethodSpec.Builder mb, ModelDescriptor md) {
        for (FieldDescriptor lf : md.getRequestLobFields()) {
            String fieldName = lf.getName();
            String setter = "set" + Character.toUpperCase(fieldName.charAt(0))
                    + fieldName.substring(1);
            if (isCollectionLobField(lf)) {
                boolean isSet = isSetLobField(lf);
                ClassName iface = isSet ? SET : LIST;
                ClassName impl = isSet ? HASH_SET : ARRAY_LIST;
                // Only act when the part was actually submitted (non-null)
                mb.beginControlFlow("if ($L != null)", fieldName);
                mb.addStatement("$T<byte[]> $LBytes = new $T<>()", iface, fieldName, impl);
                mb.beginControlFlow("for ($T _file : $L)", MULTIPART_FILE, fieldName);
                mb.beginControlFlow("if (!_file.isEmpty())");
                mb.beginControlFlow("try");
                mb.addStatement("$LBytes.add(_file.getBytes())", fieldName);
                mb.nextControlFlow("catch ($T e)", IO_EXCEPTION);
                mb.addStatement("throw new $T($T.BAD_REQUEST, $S + $S, e)",
                        ClassName.get("org.springframework.web.server", "ResponseStatusException"),
                        ClassName.get("org.springframework.http", "HttpStatus"),
                        "Failed to read uploaded file for field: ",
                        fieldName);
                mb.endControlFlow();
                mb.endControlFlow();
                mb.endControlFlow();
                // Empty collection (all files empty) clears the field; non-empty sets it
                mb.addStatement("request.$L($LBytes.isEmpty() ? null : $LBytes)",
                        setter, fieldName, fieldName);
                mb.endControlFlow();
            } else {
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
    }

    /**
     * Builds the list of parameter functions for multipart LOB endpoints.
     * Includes the request DTO part and a MultipartFile (or {@code List<MultipartFile>}
     * for collection-typed fields) part for each writable LOB field.
     *
     * <p>The {@code required} attribute on each {@code @RequestPart} is derived from
     * the field's validation annotations: a {@code @NotNull} constraint causes
     * {@code required = true}; otherwise {@code required = false} is used.
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
            boolean required = isRequiredLobField(lf);
            if (isCollectionLobField(lf)) {
                params.add(md -> ParameterSpec.builder(
                                ParameterizedTypeName.get(LIST, MULTIPART_FILE), fieldName)
                        .addAnnotation(AnnotationSpec.builder(REQUEST_PART)
                                .addMember("value", "$S", fieldName)
                                .addMember("required", "$L", required).build())
                        .build());
            } else {
                params.add(md -> ParameterSpec.builder(MULTIPART_FILE, fieldName)
                        .addAnnotation(AnnotationSpec.builder(REQUEST_PART)
                                .addMember("value", "$S", fieldName)
                                .addMember("required", "$L", required).build())
                        .build());
            }
        }
        return params;
    }
}

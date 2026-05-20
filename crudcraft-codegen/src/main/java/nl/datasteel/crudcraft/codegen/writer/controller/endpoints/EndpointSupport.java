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

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeName;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.util.ModelIdTypeResolver;


/** Common {@link ClassName} constants used by endpoint generators. */
public final class EndpointSupport {
    private EndpointSupport() {}

    /** Spring {@code ResponseEntity}. */
    public static final ClassName RESP_ENTITY =
            ClassName.get("org.springframework.http", "ResponseEntity");

    /** Spring {@code HttpStatus}. */
    public static final ClassName HTTP_STATUS =
            ClassName.get("org.springframework.http", "HttpStatus");

    /** Java {@link UUID} class reference. */
    public static final ClassName UUID_CLASS = ClassName.get(UUID.class);

    /** Spring {@code @RequestBody}. */
    public static final ClassName REQUEST_BODY =
            ClassName.get("org.springframework.web.bind.annotation", "RequestBody");

    /** Spring {@code @PathVariable}. */
    public static final ClassName PATH_VAR =
            ClassName.get("org.springframework.web.bind.annotation", "PathVariable");

    /** Spring {@code @ModelAttribute}. */
    public static final ClassName MODEL_ATTR =
            ClassName.get("org.springframework.web.bind.annotation", "ModelAttribute");

    /** Spring {@code @RequestParam}. */
    public static final ClassName REQUEST_PARAM =
            ClassName.get("org.springframework.web.bind.annotation", "RequestParam");

    /** Jakarta {@code @Valid}. */
    public static final ClassName VALID = ClassName.get("jakarta.validation", "Valid");

    /** Jakarta {@code @NotNull}. */
    public static final ClassName NOT_NULL =
            ClassName.get("jakarta.validation.constraints", "NotNull");

    /** Runtime identified request wrapper class reference. */
    public static final ClassName IDENTIFIED =
            ClassName.get("nl.datasteel.crudcraft.runtime", "Identified");

    /** Spring {@code @GetMapping}. */
    public static final ClassName GET_MAPPING =
            ClassName.get("org.springframework.web.bind.annotation", "GetMapping");

    /** Spring {@code @PostMapping}. */
    public static final ClassName POST_MAPPING =
            ClassName.get("org.springframework.web.bind.annotation", "PostMapping");

    /** Spring {@code @PutMapping}. */
    public static final ClassName PUT_MAPPING =
            ClassName.get("org.springframework.web.bind.annotation", "PutMapping");

    /** Spring {@code @PatchMapping}. */
    public static final ClassName PATCH_MAPPING =
            ClassName.get("org.springframework.web.bind.annotation", "PatchMapping");

    /** Spring {@code @DeleteMapping}. */
    public static final ClassName DELETE_MAPPING =
            ClassName.get("org.springframework.web.bind.annotation", "DeleteMapping");

    /** Spring {@code @RequestMapping}. */
    public static final ClassName REQUEST_MAPPING =
            ClassName.get("org.springframework.web.bind.annotation", "RequestMapping");

    /** Spring {@code RequestMethod}. */
    public static final ClassName REQUEST_METHOD =
            ClassName.get("org.springframework.web.bind.annotation", "RequestMethod");

    /** Spring streaming response body class reference. */
    public static final ClassName STREAMING_BODY =
            ClassName.get(
                    "org.springframework.web.servlet.mvc.method.annotation",
                    "StreamingResponseBody");

    /** Java {@link List} class reference. */
    public static final ClassName LIST = ClassName.get(List.class);

    /** Java {@link Collection} class reference. */
    public static final ClassName COLLECTION = ClassName.get(Collection.class);

    /** Java {@link Map} class reference. */
    public static final ClassName MAP = ClassName.get(Map.class);

    /** Runtime paginated response wrapper class reference. */
    public static final ClassName PAGINATED_RESPONSE =
            ClassName.get(
                    "nl.datasteel.crudcraft.runtime.controller.response", "PaginatedResponse");

    /** Runtime bulk response wrapper class reference. */
    public static final ClassName BULK_RESULT =
            ClassName.get("nl.datasteel.crudcraft.runtime.service", "BulkResult");

    /** Spring Data {@code Page}. */
    public static final ClassName PAGE = ClassName.get("org.springframework.data.domain", "Page");

    /** Spring Data {@code Pageable}. */
    public static final ClassName PAGEABLE =
            ClassName.get("org.springframework.data.domain", "Pageable");

    /** Spring Data {@code PageRequest}. */
    public static final ClassName PAGE_REQUEST =
            ClassName.get("org.springframework.data.domain", "PageRequest");

    /** Runtime export service class reference. */
    public static final ClassName EXPORT_SERVICE =
            ClassName.get("nl.datasteel.crudcraft.runtime.export.service", "ExportService");

    /** Runtime field security utility class reference. */
    public static final ClassName FIELD_SECURITY_UTIL =
            ClassName.get("nl.datasteel.crudcraft.runtime.security", "FieldSecurityUtil");

    /** Runtime export request class reference. */
    public static final ClassName EXPORT_REQUEST =
            ClassName.get("nl.datasteel.crudcraft.runtime.export", "ExportRequest");

    /** Java {@code NoSuchElementException} class reference. */
    public static final ClassName NO_SUCH_ELEMENT =
            ClassName.get("java.util", "NoSuchElementException");

    /** Java {@link java.util.Iterator} class reference. */
    public static final ClassName ITERATOR = ClassName.get("java.util", "Iterator");

    /** Java {@code Collections} utility class reference. */
    public static final ClassName COLLECTIONS = ClassName.get("java.util", "Collections");

    /** Java {@code OutputStream} class reference. */
    public static final ClassName OUTPUT_STREAM = ClassName.get("java.io", "OutputStream");

    /** Java {@code BiConsumer} class reference. */
    public static final ClassName BI_CONSUMER = ClassName.get("java.util.function", "BiConsumer");

    /** Spring {@code HttpHeaders}. */
    public static final ClassName HTTP_HEADERS =
            ClassName.get("org.springframework.http", "HttpHeaders");

    /** Spring {@code @RequestPart}. */
    public static final ClassName REQUEST_PART =
            ClassName.get("org.springframework.web.bind.annotation", "RequestPart");

    /** Spring {@code MultipartFile}. */
    public static final ClassName MULTIPART_FILE =
            ClassName.get("org.springframework.web.multipart", "MultipartFile");

    /** Spring {@code MediaType}. */
    public static final ClassName MEDIA_TYPE =
            ClassName.get("org.springframework.http", "MediaType");

    /** Java {@code IOException} class reference. */
    public static final ClassName IO_EXCEPTION = ClassName.get("java.io", "IOException");

    /** Java {@code ArrayList} class reference. */
    public static final ClassName ARRAY_LIST = ClassName.get("java.util", "ArrayList");

    /** Java {@code HashSet} class reference. */
    public static final ClassName HASH_SET = ClassName.get("java.util", "HashSet");

    /** Java {@code Set} class reference. */
    public static final ClassName SET = ClassName.get("java.util", "Set");

    /**
     * Resolves the model identifier type from descriptor metadata.
     *
     * @param modelDescriptor model descriptor
     * @return boxed model identifier type
     */
    public static TypeName resolveModelIdType(ModelDescriptor modelDescriptor) {
        if (modelDescriptor == null) {
            return UUID_CLASS;
        }
        return ModelIdTypeResolver.resolveModelIdType(modelDescriptor).box();
    }

    /**
     * Returns {@code true} when the model has at least one searchable field.
     *
     * @param modelDescriptor model descriptor
     * @return {@code true} when at least one field is searchable
     */
    public static boolean hasSearchFields(ModelDescriptor modelDescriptor) {
        if (modelDescriptor == null) {
            return false;
        }
        return FieldProcessor.hasSearchFields(modelDescriptor);
    }

    /**
     * Returns {@code true} when the model contains at least one field-level security rule.
     *
     * @param modelDescriptor model descriptor
     * @return {@code true} when generated controller methods must apply field filtering
     */
    public static boolean hasFieldSecurity(ModelDescriptor modelDescriptor) {
        if (modelDescriptor == null) {
            return false;
        }
        return SecurityFieldFilter.hasFieldSecurity(modelDescriptor);
    }

    /**
     * Returns {@code true} if the LOB field's type is a {@link java.util.List}, {@link
     * java.util.Collection}, or {@link java.util.Set}, meaning it holds multiple files rather than
     * a single byte array.
     */
    static boolean isCollectionLobField(FieldDescriptor fd) {
        return LobProcessor.isCollectionLobField(fd);
    }

    /**
     * Returns {@code true} if the LOB field carries a {@code @NotNull} validation annotation,
     * indicating the file part is mandatory and the generated {@code @RequestPart} should use
     * {@code required = true}.
     */
    static boolean isRequiredLobField(FieldDescriptor fd) {
        return LobProcessor.isRequiredLobField(fd);
    }

    /**
     * Generates code that reads bytes from MultipartFile parameters and sets them on the request
     * DTO for each writable {@code @Lob} field. Each LOB field has its own MultipartFile (or {@code
     * List<MultipartFile>} for collection-typed fields) parameter named after the field.
     *
     * <p>Behavior by parameter presence:
     *
     * <ul>
     *   <li>Part absent (parameter is {@code null}): no change to the DTO field.
     *   <li>Part present but file(s) empty: sets the DTO field to {@code null}, allowing LOB
     *       clearing via PATCH.
     *   <li>Part present with file data: sets the DTO field to the uploaded byte(s).
     * </ul>
     *
     * @param mb method builder receiving generated statements
     * @param md model descriptor
     */
    public static void addFileToRequestCode(MethodSpec.Builder mb, ModelDescriptor md) {
        LobProcessor.addFileToRequestCode(mb, md);
    }

    /**
     * Builds the list of parameter functions for multipart LOB endpoints. Includes the request DTO
     * part and a MultipartFile (or {@code List<MultipartFile>} for collection-typed fields) part
     * for each writable LOB field.
     *
     * <p>The {@code required} attribute on each {@code @RequestPart} is derived from the field's
     * validation annotations: a {@code @NotNull} constraint causes {@code required = true};
     * otherwise {@code required = false} is used.
     *
     * @param requestDtoClass request DTO class
     * @param modelDescriptor model descriptor
     * @return parameter factory list for multipart endpoints
     */
    public static List<java.util.function.Function<ModelDescriptor, ParameterSpec>> lobParams(
            ClassName requestDtoClass, ModelDescriptor modelDescriptor) {
        return LobProcessor.lobParams(requestDtoClass, modelDescriptor);
    }

    /**
     * Marks a model descriptor parameter as intentionally consumed in constant endpoint templates.
     *
     * @param modelDescriptor descriptor supplied by endpoint generation pipeline
     */
    public static void touch(ModelDescriptor modelDescriptor) {
        if (modelDescriptor == null) {
            return;
        }
        modelDescriptor.getName();
    }

    /**
     * Marks a descriptor as consumed and returns a constant value.
     *
     * @param modelDescriptor descriptor supplied by endpoint generation pipeline
     * @param value value to return
     * @param <T> value type
     * @return provided value
     */
    public static <T> T withModel(ModelDescriptor modelDescriptor, T value) {
        touch(modelDescriptor);
        return value;
    }
}

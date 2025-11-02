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

/**
 * Configuration for the different DTO flavours.
 */
public enum DtoType {
    REQUEST(".dto.request", "RequestDto", "Create/update DTO for %s",
            true),
    RESPONSE(".dto.response", "ResponseDto", "Response DTO for %s",
            false),
    REF(".dto.ref", "Ref", "Reference DTO for %s", false);

    /**
     * Package suffix for this DTO type.
     * Used to generate package names like "com.example.dto.request", "com.example.dto.response".
     */
    private final String pkgSuffix;

    /**
     * Class suffix for this DTO type.
     * Used to generate class names like "UserRequestDto", "UserResponseDto".
     */
    private final String classSuffix;

    /**
     * Schema description for this DTO type.
     * Used to generate OpenAPI documentation.
     */
    private final String schema;

    /**
     * Indicates whether this DTO type is used for requests (create/update).
     */
    private final boolean request;

    /**
     * Constructs a DTO type with the specified package suffix, class suffix,
     * schema description, and whether it is a request type.
     *
     * @param pkgSuffix the package suffix for this DTO type
     * @param classSuffix the class suffix for this DTO type
     * @param schema the schema description for this DTO type
     * @param request true if this DTO type is for requests, false otherwise
     */
    DtoType(String pkgSuffix, String classSuffix, String schema, boolean request) {
        this.pkgSuffix = pkgSuffix;
        this.classSuffix = classSuffix;
        this.schema = schema;
        this.request = request;
    }

    /**
     * Returns the package suffix for this DTO type.
     *
     * @return the package suffix, e.g., ".dto.request", ".dto.response"
     */
    public String packageSuffix() {
        return pkgSuffix;
    }

    /**
     * Returns the class suffix for this DTO type.
     *
     * @return the class suffix, e.g., "RequestDto", "ResponseDto"
     */
    public String classSuffix() {
        return classSuffix;
    }

    /**
     * Returns the schema description for the given model.
     *
     * @param model the model name to format into the schema description
     * @return the formatted schema description
     */
    public String schemaDescription(String model) {
        return String.format(schema, model);
    }

    /**
     * Indicates whether this DTO type is used for requests (create/update).
     *
     * @return true if this DTO type is for requests, false otherwise
     */
    public boolean isRequest() {
        return request;
    }
}


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
package nl.datasteel.crudcraft.codegen.descriptor.field.part;

import java.util.Map;

/**
 * Holds OpenAPI Schema annotation metadata extracted from entity fields.
 *
 * @param description the schema description
 * @param example the example value
 * @param additionalProperties map of additional @Schema properties (format, defaultValue, etc.)
 */
public record SchemaMetadata(String description, String example, 
                              Map<String, Object> additionalProperties) {
    
    /**
     * Creates an empty SchemaMetadata.
     *
     * @return an empty SchemaMetadata instance
     */
    public static SchemaMetadata empty() {
        return new SchemaMetadata(null, null, Map.of());
    }
    
    /**
     * Checks if this SchemaMetadata is empty (has no metadata).
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return description == null && example == null && additionalProperties.isEmpty();
    }
}

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

package nl.datasteel.crudcraft.codegen.descriptor.field.part;

import java.util.LinkedHashMap;
import java.util.Map;


/** Holds OpenAPI Schema annotation metadata extracted from entity fields. */
public final class SchemaMetadata {
    private final String description;
    private final String example;
    private final Map<String, Object> additionalProperties;

    /**
     * Creates schema metadata.
     *
     * @param description the schema description
     * @param example the example value
     * @param additionalProperties map of additional {@code @Schema} properties
     */
    public SchemaMetadata(
            String description, String example, Map<String, Object> additionalProperties) {
        this.description = description;
        this.example = example;
        this.additionalProperties =
                additionalProperties == null
                        ? Map.of()
                        : Map.copyOf(new LinkedHashMap<>(additionalProperties));
    }

    /**
     * Creates an empty SchemaMetadata.
     *
     * @return an empty SchemaMetadata instance
     */
    public static SchemaMetadata empty() {
        return new SchemaMetadata(null, null, Map.of());
    }

    /**
     * Returns the schema description.
     *
     * @return the schema description, or {@code null}
     */
    public String description() {
        return description;
    }

    /**
     * Returns the schema example.
     *
     * @return the example value, or {@code null}
     */
    public String example() {
        return example;
    }

    /**
     * Returns additional OpenAPI schema properties.
     *
     * @return additional schema properties
     */
    public Map<String, Object> additionalProperties() {
        return Map.copyOf(additionalProperties);
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

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

import javax.lang.model.type.TypeMirror;

/**
 * Represents the core identity of a field in a model.
 *
 * @param name the field name
 * @param type the field type
 * @param javadoc the JavaDoc comment for the field, if available
 * @param schemaMetadata the @Schema annotation metadata from the entity field, if available
 */
public record Identity(String name, TypeMirror type, String javadoc, SchemaMetadata schemaMetadata) {

    /**
     * Returns the name of the identity field.
     *
     * @return the name of the identity field
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the identity field.
     *
     * @return the type of the identity field
     */
    public TypeMirror getType() {
        return type;
    }

    /**
     * Returns the JavaDoc comment for the field.
     *
     * @return the JavaDoc comment, or null if none is available
     */
    public String getJavadoc() {
        return javadoc;
    }
    
    /**
     * Returns the @Schema annotation metadata from the entity field.
     *
     * @return the SchemaMetadata, or empty if none is available
     */
    public SchemaMetadata getSchemaMetadata() {
        return schemaMetadata != null ? schemaMetadata : SchemaMetadata.empty();
    }
}

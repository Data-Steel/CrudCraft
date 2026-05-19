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

import javax.lang.model.type.TypeMirror;


/** Represents the core identity of a field in a model. */
public final class Identity {
    private final String name;
    private final TypeMirror type;
    private final String javadoc;
    private final SchemaMetadata schemaMetadata;
    private final String projectionPath;

    /**
     * Creates a field identity.
     *
     * @param name the field name
     * @param type the field type
     * @param javadoc the JavaDoc comment for the field, if available
     * @param schemaMetadata the {@code @Schema} annotation metadata from the entity field
     */
    public Identity(String name, TypeMirror type, String javadoc, SchemaMetadata schemaMetadata) {
        this(name, type, javadoc, schemaMetadata, "");
    }

    /**
     * Creates a field identity.
     *
     * @param name the field name
     * @param type the field type
     * @param javadoc the JavaDoc comment for the field, if available
     * @param schemaMetadata the {@code @Schema} annotation metadata from the entity field
     * @param projectionPath optional {@code @ProjectionField} path from the entity field
     */
    public Identity(
            String name,
            TypeMirror type,
            String javadoc,
            SchemaMetadata schemaMetadata,
            String projectionPath) {
        this.name = name;
        this.type = type;
        this.javadoc = javadoc;
        this.schemaMetadata = schemaMetadata == null ? null : copySchemaMetadata(schemaMetadata);
        this.projectionPath = projectionPath == null ? "" : projectionPath;
    }

    /**
     * Returns the name of the identity field.
     *
     * @return the name of the identity field
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the field name.
     *
     * @return the field name
     */
    public String name() {
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
     * Returns the field type.
     *
     * @return the field type
     */
    public TypeMirror type() {
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
     * Returns the field JavaDoc comment.
     *
     * @return the field JavaDoc comment, or {@code null}
     */
    public String javadoc() {
        return javadoc;
    }

    /**
     * Returns the @Schema annotation metadata from the entity field.
     *
     * @return the SchemaMetadata, or empty if none is available
     */
    public SchemaMetadata getSchemaMetadata() {
        return schemaMetadata != null ? copySchemaMetadata(schemaMetadata) : SchemaMetadata.empty();
    }

    /**
     * Returns the schema metadata.
     *
     * @return the schema metadata, or an empty instance
     */
    public SchemaMetadata schemaMetadata() {
        return getSchemaMetadata();
    }

    /**
     * Returns the optional projection path declared on the entity field.
     *
     * @return projection path, or an empty string when none is declared
     */
    public String projectionPath() {
        return projectionPath;
    }

    private static SchemaMetadata copySchemaMetadata(SchemaMetadata schemaMetadata) {
        return new SchemaMetadata(
                schemaMetadata.description(),
                schemaMetadata.example(),
                schemaMetadata.additionalProperties());
    }
}

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

package nl.datasteel.crudcraft.codegen.descriptor.field;

import com.palantir.javapoet.AnnotationSpec;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;
import java.util.Objects;
import javax.lang.model.type.TypeMirror;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.EnumOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Relationship;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SchemaMetadata;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SearchOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Security;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Validation;
import nl.datasteel.crudcraft.codegen.exception.CodegenValidationException;


/** Represents a field in a model, encapsulating various field properties. */
public final class FieldDescriptor {

    /** The field's identity, including its name and type. */
    private final Identity identity;

    /** Options for DTO generation, such as whether to include the field in DTOs. */
    private final DtoOptions dtoOptions;

    /** Options for enum fields. */
    private final EnumOptions enumOptions;

    /**
     * Represents the relationship of the field to other entities, such as foreign keys or
     * references.
     */
    private final Relationship relationship;

    /** Validation rules for the field, such as constraints or annotations. */
    private final Validation validation;

    /** Search options for the field, defining how it can be used in search queries. */
    private final SearchOptions searchOptions;

    /** Security options for the field, defining access control and visibility. */
    private final Security security;

    /**
     * Creates a FieldDescriptor instance its various properties.
     *
     * @param identity the core identity of the field, including its name and type
     * @param dtoOptions options for DTO generation, such as whether to include the field in DTOs
     * @param enumOptions options for enum fields, such as whether the field is an enum and its
     *     possible values
     * @param relationship the relationship of the field to other entities, such as foreign keys or
     *     references
     * @param validation validation rules for the field, such as constraints or annotations
     * @param searchOptions search options for the field, defining how it can be used in search
     *     queries
     * @param security security options for the field, defining access control and visibility
     */
    public FieldDescriptor(
            @NonNull Identity identity,
            @Nullable DtoOptions dtoOptions,
            @Nullable EnumOptions enumOptions,
            @Nullable Relationship relationship,
            @Nullable Validation validation,
            @Nullable SearchOptions searchOptions,
            @Nullable Security security) {
        this.identity = requirePart("identity", identity);
        this.dtoOptions =
                dtoOptions == null
                        ? new DtoOptions(false, false, false, new String[0], false)
                        : dtoOptions;
        this.enumOptions = enumOptions == null ? new EnumOptions(false, List.of()) : enumOptions;
        this.relationship =
                relationship == null
                        ? new Relationship(RelationshipType.NONE, "", null, false, false, false)
                        : relationship;
        this.validation = validation == null ? new Validation(List.of()) : validation;
        this.searchOptions =
                searchOptions == null ? new SearchOptions(false, List.of(), 0) : searchOptions;
        this.security =
                security == null ? new Security(false, new String[0], new String[0]) : security;
    }

    private static <T> T requirePart(String partName, T value) {
        if (value == null) {
            throw new CodegenValidationException(
                    "FieldDescriptor", partName + " must not be null");
        }
        return value;
    }

    // Convenience getters.

    /**
     * Returns the simple name of the field.
     *
     * @return the simple name of the field
     */
    public @NonNull String getName() {
        return identity.getName();
    }

    /**
     * Returns the {@link TypeMirror} of the field.
     *
     * @return the {@link TypeMirror} of the field
     */
    public @Nullable TypeMirror getType() {
        return identity.getType();
    }

    /**
     * Returns the JavaDoc comment for the field.
     *
     * @return the JavaDoc comment, or null if none is available
     */
    public @Nullable String getJavadoc() {
        return identity.getJavadoc();
    }

    /**
     * Returns whether the field is included in the DTO. This is used to determine if the field
     * should be part of a Data Transfer Object.
     *
     * @return true if the field is included in the DTO, false otherwise
     */
    public boolean inDto() {
        return dtoOptions.isInDto();
    }

    /**
     * Returns whether the field is included in the request DTO. This is used to determine if the
     * field should be part of a request object.
     *
     * @return true if the field is included in the request DTO, false otherwise
     */
    public boolean inRequest() {
        return dtoOptions.isInRequest();
    }

    /**
     * Returns whether the field is included in the reference DTO. This is used to determine if the
     * field should be part of a reference response object.
     *
     * @return true if the field is included in the reference DTO, false otherwise
     */
    public boolean inRef() {
        return dtoOptions.isInRef();
    }

    /**
     * Returns whether the field is a large object (Jakarta @Lob). LOB fields should be lazy loaded
     * and excluded from request DTOs.
     *
     * @return true if the field is a LOB, false otherwise
     */
    public boolean isLob() {
        return dtoOptions.isLob();
    }

    /**
     * Returns the names of additional response DTO variants this field participates in.
     *
     * @return array of custom DTO identifiers
     */
    public @NonNull String[] getResponseDtos() {
        return dtoOptions.getResponseDtos();
    }

    /**
     * Returns whether the field is an enum. This is determined by the presence of enum options.
     *
     * @return true if the field is an enum, false otherwise
     */
    public boolean isEnumString() {
        return enumOptions.isEnum();
    }

    /**
     * Returns the list of enum values if the field is an enum. This is used to provide options for
     * enum fields in DTOs or forms.
     *
     * @return a list of enum values as Strings, or an empty list if the field is not an enum
     */
    public @NonNull List<String> getEnumValues() {
        return enumOptions.getValues();
    }

    /**
     * Returns the relationship type of the field. This indicates how the field relates to other
     * entities, such as one-to-one, one-to-many, etc.
     *
     * @return relationship type
     */
    public @NonNull RelationshipType getRelType() {
        return relationship.getRelationshipType();
    }

    /**
     * Returns the mappedBy attribute of the relationship. This is used in bidirectional
     * relationships to indicate which side owns the relationship.
     *
     * @return the mappedBy attribute as a String
     */
    public @NonNull String getMappedBy() {
        return relationship.getMappedBy();
    }

    /**
     * Returns the target type of the relationship. This is the type of the entity that this field
     * relates to.
     *
     * @return the target type as a String
     */
    public @Nullable String getTargetType() {
        return relationship.getTargetType();
    }

    /**
     * Returns whether the field is a target in a CRUD operation. This indicates if the field is
     * part of a relationship that can be created, read, updated, or deleted.
     *
     * @return true if the field is a target in CRUD operations, false otherwise
     */
    public boolean isTargetCrud() {
        return relationship.isTargetCrud();
    }

    /**
     * Returns whether the field is embedded. An embedded field is one that is part of the same
     * entity and does not have a separate table.
     *
     * @return true if the field is embedded, false otherwise
     */
    public boolean isEmbedded() {
        return relationship.isEmbedded();
    }

    /**
     * Returns whether the target type is abstract. An abstract target cannot be instantiated
     * directly in mappers.
     *
     * @return true if the target type is abstract, false otherwise
     */
    public boolean isTargetAbstract() {
        return relationship.isTargetAbstract();
    }

    /**
     * Returns the validation rules for the field. This includes constraints and annotations that
     * apply to the field.
     *
     * @return a Validation object containing the field's validation rules
     */
    public @NonNull List<AnnotationSpec> getValidations() {
        return validation.getValidations();
    }

    /**
     * Returns whether the field is searchable. This indicates if the field can be used in search
     * queries.
     *
     * @return true if the field is searchable, false otherwise
     */
    public boolean isSearchable() {
        return searchOptions.isSearchable();
    }

    /**
     * Returns the list of search operators that can be used with this field. These operators define
     * how the field can be queried in search operations.
     *
     * @return a list of SearchOperator objects
     */
    public @NonNull List<SearchOperator> getSearchOperators() {
        return searchOptions.getOperators();
    }

    /**
     * Returns the maximum depth for search queries on this field. This is used to limit the depth
     * of nested searches.
     *
     * @return the maximum search depth
     */
    public int getSearchDepth() {
        return searchOptions.getDepth();
    }

    /**
     * Check if the field has security settings defined.
     *
     * @return {@code true} when field security metadata is present
     */
    public boolean hasFieldSecurity() {
        return security.hasFieldSecurity();
    }

    /**
     * Returns roles that are allowed to read this field.
     *
     * @return an array of role names that can read this field
     */
    public @NonNull String[] getReadRoles() {
        return security.getReadRoles();
    }

    /**
     * Returns the roles that are allowed to write to this field.
     *
     * @return an array of role names that can write to this field
     */
    public @NonNull String[] getWriteRoles() {
        return security.getWriteRoles();
    }

    /**
     * Returns the write policy that applies when write access is denied.
     *
     * @return write policy
     */
    public @NonNull WritePolicy getWritePolicy() {
        return security.getWritePolicy();
    }

    /**
     * Returns the @Schema annotation metadata from the entity field.
     *
     * @return the SchemaMetadata, or empty if none is available
     */
    public @NonNull SchemaMetadata getSchemaMetadata() {
        return identity.getSchemaMetadata();
    }

    /**
     * Returns the projection path copied from {@code @ProjectionField}, if present.
     *
     * @return projection path, or an empty string
     */
    public @NonNull String getProjectionPath() {
        return identity.projectionPath();
    }

    /**
     * Compares this FieldDescriptor to another object for equality.
     *
     * @param obj the object to compare with
     * @return true if the other object is a FieldDescriptor with the same properties, false
     *     otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FieldDescriptor other)) {
            return false;
        }

        boolean sameName = Objects.equals(identity.getName(), other.identity.getName());
        boolean sameType =
                Objects.equals(
                        identity.getType() == null ? null : identity.getType().toString(),
                        other.identity.getType() == null
                                ? null
                                : other.identity.getType().toString());

        return sameName
                && sameType
                && Objects.equals(dtoOptions, other.dtoOptions)
                && Objects.equals(enumOptions, other.enumOptions)
                && Objects.equals(relationship, other.relationship)
                && Objects.equals(validation, other.validation)
                && Objects.equals(searchOptions, other.searchOptions)
                && Objects.equals(security, other.security);
    }

    /**
     * Computes a hash code for the FieldDescriptor based on its properties.
     *
     * @return an integer hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                identity.getName(),
                identity.getType() == null ? null : identity.getType().toString(),
                dtoOptions,
                enumOptions,
                relationship,
                validation,
                searchOptions,
                security);
    }

    /**
     * Returns a string representation of the FieldDescriptor.
     *
     * @return a string containing the field's name, type, and various options
     */
    @Override
    public String toString() {
        return "FieldDescriptor{"
                + "name="
                + identity.getName()
                + ", type="
                + identity.getType()
                + ", dtoOptions="
                + dtoOptions
                + ", enumOptions="
                + enumOptions
                + ", relationship="
                + relationship
                + ", validation="
                + validation
                + ", searchOptions="
                + searchOptions
                + ", security="
                + security
                + ", projectionPath="
                + identity.projectionPath()
                + '}';
    }
}

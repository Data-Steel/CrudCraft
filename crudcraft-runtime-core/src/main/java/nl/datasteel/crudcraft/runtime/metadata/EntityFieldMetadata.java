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

package nl.datasteel.crudcraft.runtime.metadata;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Field;


/** Metadata about a field in an entity, shared by runtime modules. */
public final class EntityFieldMetadata {

    /** Type of the field. */
    public enum FieldType {
        /** Scalar/primitive field (String, Integer, etc.). */
        SCALAR,
        /** Embedded object. */
        EMBEDDED,
        /** ManyToOne relationship. */
        MANY_TO_ONE,
        /** OneToOne relationship. */
        ONE_TO_ONE,
        /** OneToMany relationship (collection). */
        ONE_TO_MANY,
        /** ManyToMany relationship (collection). */
        MANY_TO_MANY
    }

    private final String name;
    private final Field field;
    private final FieldType type;
    private final Class<?> targetType;
    private final boolean exportable;

    /**
     * Creates field metadata.
     *
     * @param name the field name
     * @param field the Java field
     * @param type the field type
     * @param targetType the target class for relationships
     * @param exportable whether the field can be exported
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Reflection Field metadata is intentionally retained for runtime consumers.")
    public EntityFieldMetadata(
            String name, Field field, FieldType type, Class<?> targetType, boolean exportable) {
        this.name = name;
        this.field = field;
        this.type = type;
        this.targetType = targetType;
        this.exportable = exportable;
    }

    /**
     * Returns field name.
     *
     * @return field name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns reflected Java field.
     *
     * @return reflected field
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "Callers need direct access to the reflective Field.")
    public Field getField() {
        return field;
    }

    /**
     * Returns classified field type.
     *
     * @return field type
     */
    public FieldType getType() {
        return type;
    }

    /**
     * Returns target type for this field.
     *
     * @return target type
     */
    public Class<?> getTargetType() {
        return targetType;
    }

    /**
     * Indicates whether this field is exportable.
     *
     * @return {@code true} when exportable
     */
    public boolean isExportable() {
        return exportable;
    }

    /**
     * Indicates whether this field is a collection relationship.
     *
     * @return {@code true} when collection-based
     */
    public boolean isCollection() {
        return type == FieldType.ONE_TO_MANY || type == FieldType.MANY_TO_MANY;
    }

    /**
     * Indicates whether this field is any relationship type.
     *
     * @return {@code true} when relationship
     */
    public boolean isRelationship() {
        return type != FieldType.SCALAR && type != FieldType.EMBEDDED;
    }
}

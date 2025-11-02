/**
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.codegen.descriptor;

/**
 * Enum representing the type of relationship between entities in a data model.
 * This is used to define how entities are related to each other, such as one-to-one,
 * one-to-many, many-to-one, or many-to-many relationships.
 */
public enum RelationshipType {
    /**
     * No relationship between entities.
     */
    NONE,

    /**
     * One-to-one relationship between entities.
     */
    ONE_TO_ONE,

    /**
     * One-to-many relationship between entities.
     */
    ONE_TO_MANY,

    /**
     * Many-to-one relationship between entities.
     */
    MANY_TO_ONE,

    /**
     * Many-to-many relationship between entities.
     */
    MANY_TO_MANY;

    /**
     * Returns true for the relations that have a mappedBy side
     * and thus need bidirectional fix/clear.
     */
    public boolean isBidirectional() {
        return this == ONE_TO_MANY
                || this == MANY_TO_MANY
                || this == ONE_TO_ONE;
    }

    /**
     * Returns the Enum constant for the given string.
     */
    public static RelationshipType fromString(String type) {
        return switch (type.toUpperCase()) {
            case "NONE" -> NONE;
            case "ONE_TO_ONE" -> ONE_TO_ONE;
            case "ONE_TO_MANY" -> ONE_TO_MANY;
            case "MANY_TO_ONE" -> MANY_TO_ONE;
            case "MANY_TO_MANY" -> MANY_TO_MANY;
            default -> throw new IllegalArgumentException("Unknown relationship type: " + type);
        };
    }
}

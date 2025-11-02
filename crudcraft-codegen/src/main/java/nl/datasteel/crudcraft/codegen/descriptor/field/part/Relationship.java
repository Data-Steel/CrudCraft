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

import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;

/**
 * Represents a relationship between entities in a model.
 * This includes the type of relationship, the mappedBy field,
 * the target type, and whether the target is a CRUD entity.
 *
 * @param relationshipType the type of the relationship
 * @param mappedBy the mappedBy field for the relationship
 * @param targetType the target type of the relationship
 * @param targetCrud true if the target is a CRUD entity
 */
public record Relationship(RelationshipType relationshipType, String mappedBy, String targetType,
                           boolean targetCrud, boolean embedded) {

    /**
     * Returns the type of the relationship.
     *
     * @return the type of the relationship
     */
    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    /**
     * Returns the mappedBy field for the relationship.
     *
     * @return the mappedBy field
     */
    public String getMappedBy() {
        return mappedBy;
    }

    /**
     * Returns the target type of the relationship.
     *
     * @return the target type
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * Returns true if the target is a CRUD entity.
     *
     * @return true if target is a CRUD entity
     */
    public boolean isTargetCrud() {
        return targetCrud;
    }

    /**
     * Returns true if the field is embedded.
     *
     * @return true if the field is embedded
     */
    public boolean isEmbedded() {
        return embedded;
    }
}

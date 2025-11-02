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
package nl.datasteel.crudcraft.codegen.writer.relationship;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;

/**
 * Strategy for adding relationship fix and clear logic.
 */
public interface RelationshipHandler {

    String RELATIONSHIP_FIELD_SUFFIX = "Field";

    /**
     * Adds the fix logic for a relationship field.
     *
     * @param md the model descriptor
     * @param fd the field descriptor
     * @param fix the method spec builder to add fix logic to
     * @param entityType the class name of the entity type
     */
    void addFix(ModelDescriptor md, FieldDescriptor fd,
                MethodSpec.Builder fix, ClassName entityType);

    /**
     * Adds the clear logic for a relationship field.
     *
     * @param md the model descriptor
     * @param fd the field descriptor
     * @param clear the method spec builder to add clear logic to
     * @param entityType the class name of the entity type
     */
    void addClear(ModelDescriptor md, FieldDescriptor fd,
                  MethodSpec.Builder clear, ClassName entityType);

    default String getRelationshipFieldName(FieldDescriptor fd) {
        return fd.getName() + RELATIONSHIP_FIELD_SUFFIX;
    }

    default String getInverseRelationshipFieldName(FieldDescriptor fd, ModelDescriptor md) {
        // If the field has a mappedBy property, use that; otherwise, derive it from the model name
        String invProp = fd.getMappedBy() != null && !fd.getMappedBy().isBlank()
                ? fd.getMappedBy()
                : Character.toLowerCase(md.getName().charAt(0))
                    + md.getName().substring(1) + "s";
        return fd.getName() + "_" + invProp + RELATIONSHIP_FIELD_SUFFIX;
    }
}


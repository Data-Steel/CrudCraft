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

package nl.datasteel.crudcraft.codegen.writer.relationship;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.util.Collection;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;

/**
 * Handles ONE_TO_MANY and MANY_TO_MANY relationships.
 */
public class OneToManyHandler implements RelationshipHandler {

    /**
     * Adds the fix logic for a one-to-many or many-to-many relationship field.
     * This method updates the inverse relationship collection of the entity.
     *
     * @param modelDescriptor the model descriptor
     * @param fieldDescriptor the field descriptor
     * @param fix             the method spec builder to add fix logic to
     * @param entityType      the class name of the entity type
     */
    @Override
    public void addFix(ModelDescriptor modelDescriptor, FieldDescriptor fieldDescriptor,
                       MethodSpec.Builder fix, ClassName entityType) {
        addUpdate(modelDescriptor, fieldDescriptor, fix, entityType, "add");
    }

    /**
     * Adds the clear logic for a one-to-many or many-to-many relationship field.
     * This method removes the entity from the inverse relationship collection.
     *
     * @param modelDescriptor the model descriptor
     * @param fieldDescriptor the field descriptor
     * @param clear           the method spec builder to add clear logic to
     * @param entityType      the class name of the entity type
     */
    @Override
    public void addClear(ModelDescriptor modelDescriptor, FieldDescriptor fieldDescriptor,
                         MethodSpec.Builder clear, ClassName entityType) {
        addUpdate(modelDescriptor, fieldDescriptor, clear, entityType, "remove");
    }

    /**
     * Writes the shared loop & inverse-maintenance logic.
     *
     * @param operationOnInverse either "add" or "remove"
     */
    private void addUpdate(ModelDescriptor modelDescriptor, FieldDescriptor fieldDescriptor,
                           MethodSpec.Builder method, ClassName entityType,
                           String operationOnInverse) {

        // Resolve field handles
        String fieldName = getRelationshipFieldName(fieldDescriptor);
        String inverseRelationshipFieldName =
                getInverseRelationshipFieldName(fieldDescriptor, modelDescriptor);

        // Types: child and collections
        ClassName child = ClassName.bestGuess(fieldDescriptor.getTargetType());
        TypeName childCollection = ParameterizedTypeName.get(
                ClassName.get(Collection.class),
                child
        );
        TypeName inverseCollection = ParameterizedTypeName.get(
                ClassName.get(Collection.class),
                entityType
        );

        // Add the method statement to retrieve the child collection
        method.addStatement("@SuppressWarnings(\"unchecked\") $T children = ($T)$L.get(entity)",
                childCollection, childCollection, fieldName);

        // Add the loop to iterate over children and update the inverse relationship
        method.beginControlFlow("if (children != null)")
                .beginControlFlow("for ($T c : children)", child)
                .addStatement("@SuppressWarnings(\"unchecked\") $T inv = ($T)$L.get(c)",
                        inverseCollection, inverseCollection, inverseRelationshipFieldName)
                .beginControlFlow("if (inv != null)")
                .addStatement("inv.$L(entity)", operationOnInverse)
                .endControlFlow()
                .endControlFlow()
                .endControlFlow();
    }
}

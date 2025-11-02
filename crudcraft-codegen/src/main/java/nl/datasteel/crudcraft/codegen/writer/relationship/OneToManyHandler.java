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
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.util.Collection;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;

/**
 * Handles ONE_TO_MANY and MANY_TO_MANY relationships.
 */
public class OneToManyHandler implements RelationshipHandler {

    /**
     * Adds the fix logic for a one-to-many or many-to-many relationship field.
     * This method updates the inverse relationship (either a single field or collection).
     *
     * @param modelDescriptor the model descriptor
     * @param fieldDescriptor the field descriptor
     * @param fix             the method spec builder to add fix logic to
     * @param entityType      the class name of the entity type
     */
    @Override
    public void addFix(ModelDescriptor modelDescriptor, FieldDescriptor fieldDescriptor,
                       MethodSpec.Builder fix, ClassName entityType) {
        if (fieldDescriptor.getRelType() == RelationshipType.ONE_TO_MANY) {
            addUpdateWithSingleInverse(modelDescriptor, fieldDescriptor, fix, "entity");
        } else {
            addUpdateWithCollectionInverse(modelDescriptor, fieldDescriptor, fix, entityType, "add");
        }
    }

    /**
     * Adds the clear logic for a one-to-many or many-to-many relationship field.
     * This method clears the inverse relationship (either sets to null or removes from collection).
     *
     * @param modelDescriptor the model descriptor
     * @param fieldDescriptor the field descriptor
     * @param clear           the method spec builder to add clear logic to
     * @param entityType      the class name of the entity type
     */
    @Override
    public void addClear(ModelDescriptor modelDescriptor, FieldDescriptor fieldDescriptor,
                         MethodSpec.Builder clear, ClassName entityType) {
        if (fieldDescriptor.getRelType() == RelationshipType.ONE_TO_MANY) {
            addUpdateWithSingleInverse(modelDescriptor, fieldDescriptor, clear, "null");
        } else {
            addUpdateWithCollectionInverse(modelDescriptor, fieldDescriptor, clear, entityType, "remove");
        }
    }

    /**
     * Writes the loop & inverse-maintenance logic for ONE_TO_MANY relationships
     * where the inverse side is a single reference (ManyToOne).
     *
     * @param rhsExpr either "entity" for fix or "null" for clear
     */
    private void addUpdateWithSingleInverse(ModelDescriptor modelDescriptor,
                                           FieldDescriptor fieldDescriptor,
                                           MethodSpec.Builder method,
                                           String rhsExpr) {
        // Resolve field handles
        String fieldName = getRelationshipFieldName(fieldDescriptor);
        String inverseRelationshipFieldName =
                getInverseRelationshipFieldName(fieldDescriptor, modelDescriptor);

        // Types: child and collection
        ClassName child = ClassName.bestGuess(fieldDescriptor.getTargetType());
        TypeName childCollection = ParameterizedTypeName.get(
                ClassName.get(Collection.class),
                child
        );

        // Use field-specific variable name to avoid conflicts
        String childrenVar = fieldDescriptor.getName() + "Children";
        String childVar = fieldDescriptor.getName() + "Child";

        // Add the method statement to retrieve the child collection
        method.addStatement("@SuppressWarnings(\"unchecked\") $T $L = ($T)$L.get(entity)",
                childCollection, childrenVar, childCollection, fieldName);

        // Add the loop to iterate over children and update the inverse single reference
        method.beginControlFlow("if ($L != null)", childrenVar)
                .beginControlFlow("for ($T $L : $L)", child, childVar, childrenVar)
                .addStatement("$L.set($L, $L)", inverseRelationshipFieldName, childVar, rhsExpr)
                .endControlFlow()
                .endControlFlow();
    }

    /**
     * Writes the loop & inverse-maintenance logic for MANY_TO_MANY relationships
     * where the inverse side is also a collection.
     *
     * @param operationOnInverse either "add" or "remove"
     */
    private void addUpdateWithCollectionInverse(ModelDescriptor modelDescriptor,
                                               FieldDescriptor fieldDescriptor,
                                               MethodSpec.Builder method,
                                               ClassName entityType,
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

        // Use field-specific variable names to avoid conflicts
        String childrenVar = fieldDescriptor.getName() + "Children";
        String childVar = fieldDescriptor.getName() + "Child";
        String invVar = fieldDescriptor.getName() + "Inv";

        // Add the method statement to retrieve the child collection
        method.addStatement("@SuppressWarnings(\"unchecked\") $T $L = ($T)$L.get(entity)",
                childCollection, childrenVar, childCollection, fieldName);

        // Add the loop to iterate over children and update the inverse relationship collection
        method.beginControlFlow("if ($L != null)", childrenVar)
                .beginControlFlow("for ($T $L : $L)", child, childVar, childrenVar)
                .addStatement("@SuppressWarnings(\"unchecked\") $T $L = ($T)$L.get($L)",
                        inverseCollection, invVar, inverseCollection, inverseRelationshipFieldName, childVar)
                .beginControlFlow("if ($L != null)", invVar)
                .addStatement("$L.$L(entity)", invVar, operationOnInverse)
                .endControlFlow()
                .endControlFlow()
                .endControlFlow();
    }
}

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
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;

/**
 * Handles ONE_TO_ONE relationships.
 */
public class OneToOneHandler implements RelationshipHandler {

    /**
     * Adds the fix logic for a one-to-one relationship field.
     * This method updates the inverse relationship field of the entity.
     *
     * @param modelDescriptor the model descriptor
     * @param fieldDescriptor the field descriptor
     * @param fix             the method spec builder to add fix logic to
     * @param entityType      the class name of the entity type
     */
    @Override
    public void addFix(ModelDescriptor modelDescriptor, FieldDescriptor fieldDescriptor,
                       MethodSpec.Builder fix, ClassName entityType) {
        writeInverseSet(modelDescriptor, fieldDescriptor, fix, "entity");
    }

    /**
     * Adds the clear logic for a one-to-one relationship field.
     * This method sets the inverse relationship field of the entity to null.
     *
     * @param modelDescriptor the model descriptor
     * @param fieldDescriptor the field descriptor
     * @param clear           the method spec builder to add clear logic to
     * @param entityType      the class name of the entity type
     */
    @Override
    public void addClear(ModelDescriptor modelDescriptor, FieldDescriptor fieldDescriptor,
                         MethodSpec.Builder clear, ClassName entityType) {
        writeInverseSet(modelDescriptor, fieldDescriptor, clear, "null");
    }

    /**
     * Writes the logic to set the inverse relationship field of the entity.
     *
     * @param md       the model descriptor
     * @param fd       the field descriptor
     * @param method   the method spec builder to add logic to
     * @param rhsExpr  the right-hand side expression to set (either "entity" or "null")
     */
    private void writeInverseSet(ModelDescriptor md, FieldDescriptor fd,
                                 MethodSpec.Builder method, String rhsExpr) {
        // Resolve field handles
        String fieldName = getRelationshipFieldName(fd);
        String inverseRelationshipFieldName = getInverseRelationshipFieldName(fd, md);
        ClassName child = ClassName.bestGuess(fd.getTargetType());

        // Write the logic to set the inverse relationship field
        method.addStatement("@SuppressWarnings(\"unchecked\") $T child = ($T)$L.get(entity)",
                child, child, fieldName);
        method.beginControlFlow("if (child != null)")
                .addStatement("$L.set(child, $L)", inverseRelationshipFieldName, rhsExpr)
                .endControlFlow();
    }
}

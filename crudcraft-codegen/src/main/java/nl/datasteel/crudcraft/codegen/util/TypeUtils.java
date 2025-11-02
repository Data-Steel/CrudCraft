/*
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
package nl.datasteel.crudcraft.codegen.util;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Utility class for handling type-related operations in the code generation process.
 */
public final class TypeUtils {

    /**
     * Private constructor to prevent instantiation.
     * This class provides static utility methods only.
     */
    private TypeUtils() {
    }

    /**
     * Extracts the targetEntity value from a ManyToMany annotation mirror.
     *
     * @param mirror the AnnotationMirror to extract from
     * @return the targetEntity value as a String, or null if not present
     */
    public static String extractTargetEntityValue(AnnotationMirror mirror) {
        for (var entry : mirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals("targetEntity")) {
                String value = entry.getValue().getValue().toString();
                return !"void".equals(value) ? value : null;
            }
        }
        return null;
    }

    /**
     * Unwraps the generic type from a TypeMirror if applicable,
     * if not returns string name of the fieldType.
     *
     * @param fieldType the TypeMirror to unwrap
     * @return the unwrapped type as a String
     */
    public static String unwrapGeneric(TypeMirror fieldType, Messager messager) {
        try {
            if (fieldType instanceof DeclaredType dt && !dt.getTypeArguments().isEmpty()) {
                TypeMirror typeArg = dt.getTypeArguments().getFirst();
                return typeArg.toString();
            }
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.WARNING,
                    "Failed to unwrap generic from type: " + fieldType);
        }
        return fieldType.toString();
    }

    /**
     * Determines whether the provided type represents an enum.
     *
     * @param type the type to inspect
     * @return {@code true} if the type is an enum
     */
    public static boolean isEnum(TypeMirror type) {
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        Element el = ((DeclaredType) type).asElement();
        return el.getKind() == ElementKind.ENUM;
    }
}

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

package nl.datasteel.crudcraft.codegen.writer;

import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import jakarta.persistence.Id;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;


/** Shared DTO type and ID mapping utilities used by DTO generation. */
final class DtoTypeResolver {

    private DtoTypeResolver() {}

    static TypeName resolveDtoType(
            TypeName original, String targetFqn, DtoType parentType, boolean embedded) {
        String simple = targetFqn.substring(targetFqn.lastIndexOf('.') + 1);
        String pkgBase = targetFqn.substring(0, targetFqn.lastIndexOf('.'));
        String subPkg;
        String suffix;
        if (embedded) {
            subPkg = parentType.isRequest() ? ".dto.request" : ".dto.response";
            suffix = parentType.isRequest() ? "RequestDto" : "ResponseDto";
        } else {
            subPkg = parentType.isRequest() ? ".dto.request" : ".dto.ref";
            suffix = parentType.isRequest() ? "RequestDto" : "Ref";
        }

        ClassName dtoClass = ClassName.get(pkgBase + subPkg, simple + suffix);
        if (original instanceof ParameterizedTypeName ptn) {
            return ParameterizedTypeName.get(ptn.rawType(), dtoClass);
        }
        return dtoClass;
    }

    static TypeName toIdType(TypeName original, TypeName idType) {
        if (isSet(original)) {
            return ParameterizedTypeName.get(ClassName.get(Set.class), idType);
        }
        if (isList(original)) {
            return ParameterizedTypeName.get(ClassName.get(List.class), idType);
        }
        return idType;
    }

    static TypeName resolveModelIdType(ModelDescriptor modelDescriptor) {
        return modelDescriptor.getFields().stream()
                .filter(field -> "id".equalsIgnoreCase(field.getName()))
                .findFirst()
                .map(FieldDescriptor::getType)
                .map(TypeName::get)
                .orElse(ClassName.get(UUID.class));
    }

    static TypeName relationshipIdType(FieldDescriptor fieldDescriptor, TypeName modelIdType) {
        String targetType = fieldDescriptor.getTargetType();
        if (targetType == null || targetType.isBlank()) {
            return modelIdType;
        }
        try {
            Class<?> targetClass = Class.forName(targetType);
            Field idField = findIdField(targetClass);
            if (idField != null) {
                return TypeName.get(idField.getType()).box();
            }
        } catch (ClassNotFoundException ignored) {
            // Falls back to model ID type when the target is unavailable during processing.
        }
        return modelIdType;
    }

    static String idFieldName(FieldDescriptor fieldDescriptor, TypeName typeName) {
        if (fieldDescriptor.getRelType() == RelationshipType.NONE
                || fieldDescriptor.isEmbedded()) {
            return fieldDescriptor.getName();
        }
        if (isSet(typeName) || isList(typeName)) {
            return nl.datasteel.crudcraft.codegen.util.Pluralizer
                            .singularize(fieldDescriptor.getName())
                    + "Ids";
        }
        return fieldDescriptor.getName() + "Id";
    }

    static boolean isSet(TypeName type) {
        return type instanceof ParameterizedTypeName pt
                && pt.rawType().equals(ClassName.get(Set.class));
    }

    static boolean isList(TypeName type) {
        return type instanceof ParameterizedTypeName pt
                && pt.rawType().equals(ClassName.get(List.class));
    }

    static boolean isMap(TypeName type) {
        return type instanceof ParameterizedTypeName pt
                && pt.rawType().equals(ClassName.get(Map.class));
    }

    static boolean isArray(TypeName typeName) {
        return typeName instanceof ArrayTypeName;
    }

    static Class<?> collectionCopyType(TypeName typeName) {
        if (isSet(typeName)) {
            return Set.class;
        }
        if (isList(typeName)) {
            return List.class;
        }
        if (isMap(typeName)) {
            return Map.class;
        }
        return null;
    }

    private static Field findIdField(Class<?> targetClass) {
        Class<?> current = targetClass;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class) || "id".equalsIgnoreCase(field.getName())) {
                    return field;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
}

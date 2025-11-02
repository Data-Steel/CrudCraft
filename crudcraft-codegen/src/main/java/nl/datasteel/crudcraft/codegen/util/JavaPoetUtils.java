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
package nl.datasteel.crudcraft.codegen.util;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;

/**
 * Helper methods around JavaPoet.
 */
public final class JavaPoetUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private JavaPoetUtils() {
    }

    /**
     * Creates a {@link ClassName} from a package and simple name.
     *
     * @param pkg package name
     * @param simple simple class name
     * @return a ClassName instance
     */
    public static ClassName getClassName(String pkg, String simple) {
        return ClassName.get(pkg, simple);
    }

    /**
     * Returns a {@link ParameterizedTypeName} representing a {@link List} of the given type.
     *
     * @param type the contained type
     * @return a parameterized List type
     */
    public static ParameterizedTypeName getListOf(TypeName type) {
        return ParameterizedTypeName.get(ClassName.get(List.class), type);
    }

    /**
     * Adds a private field with standard getter and setter methods.
     * Optionally applies validation, enum schema and security annotations.
     *
     * @param builder         the {@link TypeSpec.Builder} to add to
     * @param name            field name
     * @param type            field type
     * @param validations     annotations to add directly on the field
     * @param enumSchema      optional {@code @Schema(allowableValues="...")}
     * @param fieldSecurity   optional field security annotation
     */
    public static void addFieldWithAccessors(
            TypeSpec.Builder builder,
            String name,
            TypeName type,
            List<AnnotationSpec> validations,
            AnnotationSpec enumSchema,
            AnnotationSpec fieldSecurity
    ) {
        FieldSpec.Builder fs = FieldSpec.builder(type, name, Modifier.PRIVATE);
        if (enumSchema != null) {
            fs.addAnnotation(enumSchema);
        }
        validations.forEach(fs::addAnnotation);
        if (fieldSecurity != null) {
            fs.addAnnotation(fieldSecurity);
        }
        builder.addField(fs.build())
               .addMethod(getter(name, type))
               .addMethod(setter(name, type));
    }

    /**
     * Adds a private final field with a standard getter method.
     * Optionally applies validation, enum schema and security annotations.
     *
     * @param builder       the {@link TypeSpec.Builder} to add to
     * @param name          field name
     * @param type          field type
     * @param validations   annotations to add directly on the field
     * @param enumSchema    optional {@code @Schema(allowableValues="...")}
     * @param fieldSecurity optional field security annotation
     */
    public static void addFieldWithGetter(
            TypeSpec.Builder builder,
            String name,
            TypeName type,
            List<AnnotationSpec> validations,
            AnnotationSpec enumSchema,
            AnnotationSpec fieldSecurity
    ) {
        FieldSpec.Builder fs = FieldSpec.builder(type, name, Modifier.PRIVATE, Modifier.FINAL);
        if (enumSchema != null) {
            fs.addAnnotation(enumSchema);
        }
        validations.forEach(fs::addAnnotation);
        if (fieldSecurity != null) {
            fs.addAnnotation(fieldSecurity);
        }
        builder.addField(fs.build())
               .addMethod(getter(name, type));
    }

    private static MethodSpec getter(String name, TypeName type) {
        String up = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        String pre = type.equals(TypeName.BOOLEAN) ? "is" : "get";
        MethodSpec.Builder mb = MethodSpec.methodBuilder(pre + up)
                .addModifiers(Modifier.PUBLIC)
                .returns(type);
        if (isSet(type)) {
            mb.addStatement("return this.$N == null ? null : $T.copyOf(this.$N)",
                    name, ClassName.get(Set.class), name);
        } else if (isList(type)) {
            mb.addStatement("return this.$N == null ? null : $T.copyOf(this.$N)",
                    name, ClassName.get(List.class), name);
        } else {
            mb.addStatement("return this.$N", name);
        }
        return mb.build();
    }

    private static MethodSpec setter(String name, TypeName type) {
        String up = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        MethodSpec.Builder mb = MethodSpec.methodBuilder("set" + up)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(type, name);
        if (isSet(type)) {
            mb.addStatement("this.$N = $N == null ? null : new $T<>($N)",
                    name, name, ClassName.get(HashSet.class), name);
        } else if (isList(type)) {
            mb.addStatement("this.$N = $N == null ? null : new $T<>($N)",
                    name, name, ClassName.get(ArrayList.class), name);
        } else {
            mb.addStatement("this.$N = $N", name, name);
        }
        return mb.build();
    }

    private static boolean isSet(TypeName type) {
        return type instanceof ParameterizedTypeName pt
                && pt.rawType.equals(ClassName.get(Set.class));
    }

    private static boolean isList(TypeName type) {
        return type instanceof ParameterizedTypeName pt
                && pt.rawType.equals(ClassName.get(List.class));
    }
}

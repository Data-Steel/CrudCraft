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
package nl.datasteel.crudcraft.codegen.writer.search;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.Modifier;

/**
 * Utility class for generating getter and setter methods for fields in search
 * request DTOs. This class provides methods to create method specifications
 * for accessing and modifying private fields in generated classes.
 */
public final class SearchAccessorUtil {

    /**
     * Private constructor to prevent instantiation.
     * This class provides static utility methods only.
     */
    private SearchAccessorUtil() {
    }

    /**
     * Generates a getter method for a given field name and type.
     *
     * @param name the name of the field
     * @param type the type of the field
     * @return a MethodSpec representing the getter method
     */
    public static MethodSpec getter(String name, TypeName type) {
        String up = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        MethodSpec.Builder mb = MethodSpec.methodBuilder("get" + up)
                .addModifiers(Modifier.PUBLIC)
                .returns(type);
        if (isSet(type)) {
            mb.addStatement("return this.$N == null ? null : $T.copyOf(this.$N)",
                    name, ClassName.get(java.util.Set.class), name);
        } else if (isList(type)) {
            mb.addStatement("return this.$N == null ? null : $T.copyOf(this.$N)",
                    name, ClassName.get(java.util.List.class), name);
        } else {
            mb.addStatement("return this.$N", name);
        }
        return mb.build();
    }

    /**
     * Generates a setter method for a given field name and type.
     *
     * @param name the name of the field
     * @param type the type of the field
     * @return a MethodSpec representing the setter method
     */
    public static MethodSpec setter(String name, TypeName type) {
        String up = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        MethodSpec.Builder mb = MethodSpec.methodBuilder("set" + up)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(type, name);
        if (isSet(type)) {
            mb.addStatement("this.$N = $N == null ? null : new $T<>($N)",
                    name, name, ClassName.get(java.util.HashSet.class), name);
        } else if (isList(type)) {
            mb.addStatement("this.$N = $N == null ? null : new $T<>($N)",
                    name, name, ClassName.get(java.util.ArrayList.class), name);
        } else {
            mb.addStatement("this.$N = $N", name, name);
        }
        return mb.build();
    }

    private static boolean isSet(TypeName type) {
        return type instanceof ParameterizedTypeName pt
                && pt.rawType.equals(ClassName.get(java.util.Set.class));
    }

    private static boolean isList(TypeName type) {
        return type instanceof ParameterizedTypeName pt
                && pt.rawType.equals(ClassName.get(java.util.List.class));
    }
}


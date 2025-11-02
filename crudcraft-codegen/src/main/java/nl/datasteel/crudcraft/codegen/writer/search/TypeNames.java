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
package nl.datasteel.crudcraft.codegen.writer.search;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

final class TypeNames {
    private TypeNames() {}

    static TypeName simple(TypeName t) {
        if (t instanceof ParameterizedTypeName p) {
            ClassName raw = (ClassName) simple(p.rawType);
            TypeName[] args = p.typeArguments.stream()
                    .map(TypeNames::simple)
                    .toArray(TypeName[]::new);
            return ParameterizedTypeName.get(raw, args);
        }
        if (t instanceof ClassName cn) {
            // keep only simple names (handles nested types too)
            String simple = String.join(".", cn.simpleNames());
            return ClassName.bestGuess(simple);
        }
        if (t instanceof ArrayTypeName at) {
            return ArrayTypeName.of(simple(at.componentType));
        }
        return t; // primitives etc.
    }
}

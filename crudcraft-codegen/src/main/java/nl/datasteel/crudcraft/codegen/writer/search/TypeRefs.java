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

package nl.datasteel.crudcraft.codegen.writer.search;

// CHECKSTYLE.SUPPRESS: MethodName for +1000 lines
// CHECKSTYLE.SUPPRESS: AbbreviationAsWordInName for +1000 lines

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import java.time.Instant;
import java.util.List;
import java.util.Set;


/** Common type references used by search code generation. */
public final class TypeRefs {
    private TypeRefs() {}

    /**
     * Returns the {@link Set} type reference.
     *
     * @return set class name
     */
    public static ClassName SET() {
        return ClassName.get(Set.class);
    }

    /**
     * Returns the {@link List} type reference.
     *
     * @return list class name
     */
    public static ClassName LIST() {
        return ClassName.get(List.class);
    }

    /**
     * Returns the {@link Instant} type reference.
     *
     * @return instant class name
     */
    public static ClassName INSTANT() {
        return ClassName.get(Instant.class);
    }

    /**
     * Returns a parameterized {@link Set} type.
     *
     * @param element set element type
     * @return parameterized set type
     */
    public static TypeName setOf(TypeName element) {
        return ParameterizedTypeName.get(SET(), element);
    }

    /**
     * Returns a parameterized {@link List} type.
     *
     * @param element list element type
     * @return parameterized list type
     */
    public static TypeName listOf(TypeName element) {
        return ParameterizedTypeName.get(LIST(), element);
    }
}

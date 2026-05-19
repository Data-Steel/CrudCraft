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

import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;


/** Describes how an operator contributes fields and accessors to the request DTO. */
public interface OperatorSpec {

    /**
     * Adds fields to the specified TypeSpec builder for the operator.
     *
     * @param cls type builder
     * @param prop property name
     * @param type property type
     */
    void addFields(TypeSpec.Builder cls, String prop, TypeName type);
}

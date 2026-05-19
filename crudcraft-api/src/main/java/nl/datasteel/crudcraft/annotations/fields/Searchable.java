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

package nl.datasteel.crudcraft.annotations.fields;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import nl.datasteel.crudcraft.annotations.SearchOperator;


/**
 * Marks a field or entity class as searchable and optionally restricts the allowed {@link
 * SearchOperator operators}. The annotation is processed at compile time by the CrudCraft
 * annotation processor to generate type safe search helpers. It is retained in class files so
 * processors can inspect compiled dependencies, but it is not a runtime API contract.
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Searchable {

    /**
     * The set of allowed operators for this field. When empty a default set based on the field type
     * is used by the code generator.
     *
     * @return allowed operators for this field
     */
    SearchOperator[] operators() default {};

    /**
     * Optional search depth when the annotation is applied on an entity class. When omitted the
     * global configuration is used.
     *
     * @return search depth for nested fields
     */
    int depth() default 1;
}

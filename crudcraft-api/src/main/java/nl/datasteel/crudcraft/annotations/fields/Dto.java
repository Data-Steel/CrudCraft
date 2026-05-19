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


/**
 * Annotation to mark a field for inclusion in generated Data Transfer Objects (DTOs). CrudCraft
 * reads this annotation at compile time from class files and generated source, so it is retained
 * with {@link RetentionPolicy#CLASS} and targets entity fields only.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Dto {
    /**
     * When set to {@code true}, the field will be included in a reference DTO.
     *
     * @return whether field is included in reference DTOs
     */
    boolean ref() default false;

    /**
     * Names of additional response DTO variants this field should appear on. For each name
     * provided, CrudCraft will generate an {@code EntityNameResponseDto} containing all fields that
     * specify the same name. Example: {@code @Dto(value = {"List"})} on fields of {@code User} will
     * produce a {@code UserListResponseDto}. Named variants use the same mutable JavaBean contract
     * as the default generated DTOs; callers should treat collection-valued properties as DTO state
     * and copy them before mutating when sharing across threads.
     *
     * @return additional response DTO variant names
     */
    String[] value() default {};
}

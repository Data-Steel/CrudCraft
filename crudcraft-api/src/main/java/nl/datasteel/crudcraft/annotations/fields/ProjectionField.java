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
 * Specifies an alternative projection path for a DTO field. When present the path will be used
 * instead of the raw field name when generating projection metadata.
 *
 * <p>Paths use dot notation relative to the entity that owns the generated DTO, for example
 * {@code author.name} or {@code category.id}. Each path segment must match a readable field, record
 * component, or JavaBean getter on the current entity or nested object. Segments are Java
 * identifier-style property names; blank segments such as {@code author..name} are invalid.
 *
 * <p>The annotation processor performs best-effort compile-time validation when the entity type can
 * be resolved from the DTO package and name. Unresolvable invalid paths are reported later by the
 * runtime projection engine when the query is executed.
 */
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProjectionField {

    /**
     * The projection path to use for the annotated field.
     *
     * @return projection path
     */
    String value();
}

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

package nl.datasteel.crudcraft.annotations.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/** Declares ownership-based row security. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface OwnedBy {

    /**
     * Entity field that stores owner ID.
     *
     * @return entity field name containing owner ID
     */
    String field() default "ownerId";

    /**
     * Principal claim used as owner ID.
     *
     * @return claim name containing owner ID
     */
    String claim() default "sub";
}

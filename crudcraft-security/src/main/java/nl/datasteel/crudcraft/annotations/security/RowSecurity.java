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
package nl.datasteel.crudcraft.annotations.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity with a {@link RowSecurityHandler} that filters accessible rows.
 */
@SuppressWarnings("java:S1452") // Suppress warning for an unbounded generic wildcard type
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RowSecurity {

    /**
     * One or more {@link RowSecurityHandler} types providing row-level filters.
     */
    Class<? extends RowSecurityHandler<?>>[] handlers();
}

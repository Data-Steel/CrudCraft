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

package nl.datasteel.crudcraft.runtime.search;

/**
 * Defines the logical operator used to combine multiple search criteria.
 *
 * <p>Generated specifications combine predicates left-to-right using the selected operator. For
 * example, three searched fields {@code F1}, {@code F2}, and {@code F3} with {@link #OR} are
 * evaluated as {@code (F1 = ?) OR (F2 = ?) OR (F3 = ?)}. With {@link #AND}, all predicates must
 * match.
 */
public enum SearchLogic {
    /** Combines search criteria using logical OR (any criteria matches). */
    OR,

    /** Combines search criteria using logical AND (all criteria must match). */
    AND
}

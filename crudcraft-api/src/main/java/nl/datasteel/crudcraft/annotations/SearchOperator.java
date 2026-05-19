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

package nl.datasteel.crudcraft.annotations;

/**
 * Search operators available to generated search request classes.
 *
 * <p>Operators are translated by code generation into Spring Data JPA {@code Specification}
 * predicates. Scalar operators apply to comparable or string-like fields. Collection and map
 * operators are only generated when the target field type supports the operation.
 *
 * <p>Null request values do not produce predicates for value-based operators. Operator-only
 * predicates such as {@link #IS_EMPTY} and {@link #NOT_EMPTY} are active when their operator flag
 * is selected. String matching uses the generated JPA criteria predicate for the configured
 * database; case sensitivity and regular-expression support are therefore database and collation
 * dependent.
 */
public enum SearchOperator {
    /** Matches scalar fields using equality semantics for the generated criteria path. */
    EQUALS,
    /** Matches scalar fields that are not equal to the supplied value. */
    NOT_EQUALS,
    /** Matches string fields containing the supplied value, implemented as a SQL {@code LIKE}. */
    CONTAINS,
    /** Matches string fields that start with the supplied prefix. */
    STARTS_WITH,
    /** Matches string fields that end with the supplied suffix. */
    ENDS_WITH,
    /** Matches string fields using the generated database-specific pattern predicate. */
    REGEX,
    /** Matches comparable fields greater than the supplied lower bound. */
    GT,
    /** Matches comparable fields greater than or equal to the supplied lower bound. */
    GTE,
    /** Matches comparable fields less than the supplied upper bound. */
    LT,
    /** Matches comparable fields less than or equal to the supplied upper bound. */
    LTE,
    /** Matches scalar fields whose value is present in the supplied collection. */
    IN,
    /** Matches scalar fields whose value is absent from the supplied collection. */
    NOT_IN,
    /** Matches comparable fields between the supplied start and end values, inclusively. */
    RANGE,
    /** Matches temporal or comparable fields before the supplied value. */
    BEFORE,
    /** Matches temporal or comparable fields after the supplied value. */
    AFTER,
    /** Matches comparable fields between the supplied start and end values, inclusively. */
    BETWEEN,
    /** Matches empty collections, maps, or strings where supported by the criteria path. */
    IS_EMPTY,
    /** Matches collection or map fields with exactly the supplied size. */
    SIZE_EQUALS,
    /**
     * Matches collection or map fields with size greater than the supplied value.
     */
    SIZE_GT,
    /** Matches collection or map fields with size less than the supplied value. */
    SIZE_LT,
    /** Matches non-empty collections, maps, or strings where supported by the criteria path. */
    NOT_EMPTY,
    /** Matches collection fields containing every supplied value. */
    CONTAINS_ALL,
    /** Matches map fields containing the supplied key. */
    CONTAINS_KEY,
    /** Matches map fields containing the supplied value. */
    CONTAINS_VALUE
}

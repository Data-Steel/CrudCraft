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
package nl.datasteel.crudcraft.annotations;

/**
 * Enum representing various search operators that can be used in query
 * specifications. These operators can be used to define how fields should be compared or
 * filtered in search queries.
 */
public enum SearchOperator {
    /**
     * Represents an equality check.
     */
    EQUALS,
    /**
     * Represents an inequality check.
     */
    NOT_EQUALS,
    /**
     * Represents a check for null values.
     */
    CONTAINS,
    /**
     * Represents a check for values that start with a specified prefix.
     */
    STARTS_WITH,
    /**
     * Represents a check for values that end with a specified prefix.
     */
    ENDS_WITH,
    /**
     * Represents a check for values that match a specified pattern.
     */
    REGEX,
    /**
     * Represents a check for values greater than a specified value.
     */
    GT,
    /**
     * Represents a check for values greater than or equal to a specified value.
     */
    GTE,
    /**
     * Represents a check for values lower than a specified value.
     */
    LT,
    /**
     * Represents a check for values lower than or equal to specified value.
     */
    LTE,
    /**
     * Represents a check for values that are in a specified collection.
     */
    IN,
    /**
     * Represents a check for values that are not in a specified collection.
     */
    NOT_IN,
    /**
     * Represents a check for values within a specified range.
     */
    RANGE,
    /**
     * Represents a check for values that are before a specified value.
     */
    BEFORE,
    /**
     * Represents a check for values that are after a specified value.
     */
    AFTER,
    /**
     * Represents a check for values that are between two specified values.
     */
    BETWEEN,
    /**
     * Represents a check for empty values.
     */
    IS_EMPTY,
    /**
     * Represents a check for size equality with a specified value.
     */
    SIZE_EQUALS,
    /**
     * Represents a check for size greater than a specified value.
     */
    SIZE_GT,
    /**
     * Represents a check for a size lower than a specified value.
     */
    SIZE_LT,
    /**
     * Represents a check for the presence of elements in a collection.
     */
    NOT_EMPTY,
    /**
     * Represents a check for the presence of all elements in a collection.
     */
    CONTAINS_ALL,
    /**
     * Represents a check for the presence of a key in a collection.
     */
    CONTAINS_KEY,
    /**
     * Represents a check for the presence of a value in a collection.
     */
    CONTAINS_VALUE
}

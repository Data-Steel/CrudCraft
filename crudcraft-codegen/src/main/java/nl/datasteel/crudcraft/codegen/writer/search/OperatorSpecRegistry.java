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

package nl.datasteel.crudcraft.codegen.writer.search;

import nl.datasteel.crudcraft.annotations.SearchOperator;

/**
 * Utility class that provides static methods to retrieve operator specifications
 * based on the SearchOperator enum. This class is used to encapsulate the logic
 * for determining the type of operator and returning the corresponding OperatorSpec.
 */
public final class OperatorSpecRegistry {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * This class is designed to provide static methods for operator specifications.
     */
    private OperatorSpecRegistry() {
    }

    /**
     * Returns ValueOperatorSpec for operators that involve value checks.
     */
    public static OperatorSpec value() {
        return new ValueOperatorSpec();
    }

    /**
     * Returns ContainsOperatorSpec for operators that involve containment checks.
     */
    public static OperatorSpec range() {
        return new RangeOperatorSpec();
    }

    /**
     * Returns ContainsOperatorSpec for operators that involve size checks.
     */
    public static OperatorSpec size() {
        return new SizeOperatorSpec();
    }

    /**
     * Checks if the given operator is a range operator.
     */
    public static boolean isRangeOperator(SearchOperator op) {
        return op == SearchOperator.RANGE || op == SearchOperator.BETWEEN;
    }

    /**
     * Checks if the given operator is a size operator.
     */
    public static boolean isSizeOperator(SearchOperator op) {
        return op == SearchOperator.SIZE_EQUALS
                || op == SearchOperator.SIZE_GT
                || op == SearchOperator.SIZE_LT;
    }

    /**
     * Checks if the given operator is a value operator.
     * Value operators are those that do not involve range or size checks,
     * and are not related to emptiness checks.
     */
    public static boolean isValueOperator(SearchOperator op) {
        return !isRangeOperator(op) && !isSizeOperator(op)
                && op != SearchOperator.IS_EMPTY
                && op != SearchOperator.NOT_EMPTY;
    }
}


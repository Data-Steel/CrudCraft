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

package nl.datasteel.crudcraft.codegen.descriptor.field.part;

import java.util.List;
import nl.datasteel.crudcraft.annotations.SearchOperator;


/** Search configuration for a field. */
public final class SearchOptions {
    private final boolean searchable;
    private final List<SearchOperator> operators;
    private final int depth;

    /**
     * Creates immutable search options.
     *
     * @param searchable {@code true} if the field can be searched
     * @param operators the supported search operators
     * @param depth traversal depth for nested search
     */
    public SearchOptions(boolean searchable, List<SearchOperator> operators, int depth) {
        this.searchable = searchable;
        this.operators = operators == null ? List.of() : List.copyOf(operators);
        this.depth = depth;
    }

    /**
     * Indicates whether the field is searchable.
     *
     * @return {@code true} when the field is searchable
     */
    public boolean isSearchable() {
        return searchable;
    }

    /**
     * Indicates whether the field is searchable.
     *
     * @return {@code true} when the field is searchable
     */
    public boolean searchable() {
        return searchable;
    }

    /**
     * Safe, defensive accessor for operators.
     *
     * @return the configured search operators
     */
    public List<SearchOperator> operators() {
        return List.copyOf(operators);
    }

    /**
     * Returns the supported search operators for the field.
     *
     * @return the configured search operators
     */
    public List<SearchOperator> getOperators() {
        return List.copyOf(operators);
    }

    /**
     * Returns the search depth used when traversing relationships.
     *
     * @return the configured search depth
     */
    public int depth() {
        return depth;
    }

    /**
     * Returns the search depth used when traversing relationships.
     *
     * @return the configured search depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchOptions that)) {
            return false;
        }

        if (searchable != that.searchable) {
            return false;
        }
        if (depth != that.depth) {
            return false;
        }
        return operators.equals(that.operators);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int result = Boolean.hashCode(searchable);
        result = 31 * result + operators.hashCode();
        result = 31 * result + Integer.hashCode(depth);
        return result;
    }

    /**
     * Returns a string representation of the SearchOptions.
     *
     * @return a string representation of the SearchOptions
     */
    @Override
    public String toString() {
        return "SearchOptions{"
                + "searchable="
                + searchable
                + ", operators="
                + operators
                + ", depth="
                + depth
                + '}';
    }
}

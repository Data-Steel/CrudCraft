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
package nl.datasteel.crudcraft.codegen.descriptor.field.part;

import java.util.List;

/**
 * Represents options for an enum field in a model.
 *
 * @param isEnum true if the field is an enum
 * @param values the list of enum values
 */
public record EnumOptions(boolean isEnum, List<String> values) {

    /**
     * Unmodifiable constructor for EnumOptions.
     */
    public EnumOptions {
        values = values == null ? List.of() : List.copyOf(values);
    }

    /**
     * Safe, defensive accessor for values.
     */
    @Override
    public List<String> values() {
        return List.copyOf(values);
    }

    /**
     * Returns the list of enum values.
     *
     * @return the list of enum values
     */
    public List<String> getValues() {
        return List.copyOf(values);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnumOptions that)) return false;

        if (isEnum != that.isEnum) return false;
        return values.equals(that.values);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int result = Boolean.hashCode(isEnum);
        result = 31 * result + values.hashCode();
        return result;
    }

    /**
     * Returns a string representation of the EnumOptions.
     *
     * @return a string representation of the EnumOptions
     */
    @Override
    public String toString() {
        return "EnumOptions{" +
                "isEnum=" + isEnum +
                ", values=" + values +
                '}';
    }
}

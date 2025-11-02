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

import java.util.Arrays;

/**
 * Field level security configuration.
 *
 * @param defined    whether a {@code FieldSecurity} annotation is present
 * @param readRoles  roles allowed to read the field
 * @param writeRoles roles allowed to write the field
 */
public record Security(boolean defined, String[] readRoles, String[] writeRoles) {

    /**
     * Immutable constructor for Security.
     * Initializes read and write roles, ensuring they are not null.
     *
     * @param readRoles  roles allowed to read the field
     * @param writeRoles roles allowed to write the field
     */
    public Security {
        readRoles = readRoles == null ? new String[0]
                : Arrays.copyOf(readRoles, readRoles.length);
        writeRoles = writeRoles == null ? new String[0]
                : Arrays.copyOf(writeRoles, writeRoles.length);
    }

    /**
     * Safe, defensive accessor for read roles.
     */
    @Override
    public String[] readRoles() {
        return Arrays.copyOf(readRoles, readRoles.length);
    }

    /**
     * Returns the roles allowed to read the field.
     */
    public String[] getReadRoles() {
        return Arrays.copyOf(readRoles, readRoles.length);
    }

    /**
     * Safe, defensive accessor for write roles.
     */
    @Override
    public String[] writeRoles() {
        return Arrays.copyOf(writeRoles, writeRoles.length);
    }

    /**
     * Returns the roles allowed to write the field.
     */
    public String[] getWriteRoles() {
        return Arrays.copyOf(writeRoles, writeRoles.length);
    }

    /**
     * Indicates whether any field security is configured.
     */
    public boolean hasFieldSecurity() {
        return defined;
    }

    /**
     * Checks if the current Security object is equal to another object.
     * Two Security objects are considered equal if they have the same read and write roles.
     *
     * @param o the object to compare with
     * @return true if both objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Security(
                    boolean otherDefined,
                    String[] otherReadRoles,
                    String[] otherWriteRoles
                )
                && defined == otherDefined
                && Arrays.equals(readRoles, otherReadRoles)
                && Arrays.equals(writeRoles, otherWriteRoles);
    }

    /**
     * Computes the hash code for the Security object based on read and write roles.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        int result = Boolean.hashCode(defined);
        result = 31 * result + Arrays.hashCode(readRoles);
        result = 31 * result + Arrays.hashCode(writeRoles);
        return result;
    }

    /**
     * Returns a string representation of the Security object.
     *
     * @return a string describing the security configuration
     */
    @Override
    public String toString() {
        return "Security{"
                + "defined=" + defined
                + ", readRoles=" + Arrays.toString(readRoles)
                + ", writeRoles=" + Arrays.toString(writeRoles)
                + '}';
    }
}

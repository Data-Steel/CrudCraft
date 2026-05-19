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

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;


/**
 * Generated metadata contract for field-level security.
 *
 * @param <T> dto type
 */
@FunctionalInterface
public interface FieldSecurityMetadata<T> {

    /**
     * Returns immutable field rules for the DTO type.
     *
     * @return immutable field rule list
     */
    List<FieldRule<T>> fields();

    /**
     * Creates metadata from an immutable list of rules.
     *
     * @param fields source field rules
     * @param <T> dto type
     * @return metadata wrapper around an immutable rule copy
     */
    static <T> FieldSecurityMetadata<T> of(List<FieldRule<T>> fields) {
        List<FieldRule<T>> copy = fields == null ? List.of() : List.copyOf(fields);
        return () -> copy;
    }

    /**
     * Immutable field-level read/write rule.
     *
     * <p>Generated implementations use this record as a pure data contract. Custom code may create
     * additional rules, but the accessors and mutators supplied to the rule must be thread-safe and
     * side-effect free except for the documented write mutator. The contract may be tightened in a
     * future major version if the metadata model becomes sealed.
     *
     * @param <T> dto type
     * @param name dto field name
     * @param reader field read accessor
     * @param writer field write mutator
     * @param secured whether this field is security constrained
     * @param readRoles roles allowed to read the field
     * @param writeRoles roles allowed to write the field
     * @param writePolicy policy to apply when write is denied
     */
    record FieldRule<T>(
            String name,
            Function<T, Object> reader,
            BiConsumer<T, Object> writer,
            boolean secured,
            List<String> readRoles,
            List<String> writeRoles,
            WritePolicy writePolicy) {

        /** Canonical constructor with immutable role lists. */
        public FieldRule {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(reader, "reader must not be null");
            readRoles = readRoles == null ? List.of() : List.copyOf(readRoles);
            writeRoles = writeRoles == null ? List.of() : List.copyOf(writeRoles);
            writePolicy = writePolicy == null ? WritePolicy.SKIP_ON_DENIED : writePolicy;
        }
    }
}

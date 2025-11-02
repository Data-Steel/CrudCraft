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
package nl.datasteel.crudcraft.codegen.projection;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Describes how a DTO maps to entity attributes. Implementations are expected
 * to be generated at compile time.
 *
 * @param <D> DTO type
 */
public interface ProjectionMetadata<D> {

    /**
     * Returns the DTO type this metadata represents.
     */
    Class<D> dtoType();

    /**
     * Return attributes in the order of the DTO constructor parameters.
     */
    List<Attribute> attributes();

    /**
     * Attribute binding between DTO constructor arguments and entity paths.
     */
    interface Attribute {
        /**
         * Entity attribute path relative to the current entity or join.
         */
        String path();

        /**
         * Metadata for nested DTO projections, or {@code null} for simple fields.
         */
        ProjectionMetadata<?> nested();

        /**
         * Whether the attribute represents a collection association.
         */
        boolean collection();

        /**
         * Mutator used to assign collection values on the DTO. Implementations
         * are expected to be generated at compile time so that collection
         * properties can be populated without reflection.
         *
         * @return a mutator accepting the DTO instance and the values to set
         */
        BiConsumer<Object, List<?>> mutator();
    }
}


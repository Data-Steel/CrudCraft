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

package nl.datasteel.crudcraft.runtime.projection.api;

import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;


/**
 * Abstraction over different filtering mechanisms.
 *
 * @param <T> entity type used by the filter
 */
public interface FilterCriteria<T> {
    /**
     * Returns optional JPA specification representation.
     *
     * @return optional specification for the current filter
     */
    default Optional<Specification<T>> asSpecification() {
        return Optional.empty();
    }

    /**
     * Creates a FilterCriteria from a JPA Specification.
     *
     * @param spec the JPA specification to wrap
     * @param <T> entity type used by the specification
     * @return a FilterCriteria that uses the provided specification
     */
    static <T> FilterCriteria<T> ofSpecification(Specification<T> spec) {
        if (spec == null) {
            return new FilterCriteria<>() {};
        }
        return new FilterCriteria<>() {
            @Override
            public Optional<Specification<T>> asSpecification() {
                return Optional.of(spec);
            }
        };
    }
}

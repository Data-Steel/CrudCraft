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

package nl.datasteel.crudcraft.projection.api;

import com.querydsl.core.types.Predicate;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;

/**
 * Abstraction over different filtering mechanisms.
 */
public interface FilterCriteria<T> {
    /**
     * Returns optional JPA specification representation.
     */
    default Optional<Specification<T>> asSpecification() {
        return Optional.empty();
    }

    /**
     * Returns optional QueryDSL predicate representation.
     */
    default Optional<Predicate> asPredicate() {
        return Optional.empty();
    }

    /**
     * Creates a FilterCriteria from a JPA Specification.
     *
     * @param spec the JPA specification to wrap
     * @return a FilterCriteria that uses the provided specification
     */
    static <T> FilterCriteria<T> ofSpecification(Specification<T> spec) {
        return new FilterCriteria<>() {
            @Override
            public Optional<Specification<T>> asSpecification() {
                return Optional.of(spec);
            }
        };
    }

    /**
     * Creates a FilterCriteria from a QueryDSL Predicate.
     *
     * @param predicate the QueryDSL predicate to wrap
     * @return a FilterCriteria that uses the provided predicate
     */
    static <T> FilterCriteria<T> ofPredicate(Predicate predicate) {
        return new FilterCriteria<>() {
            @Override
            public Optional<Predicate> asPredicate() {
                return Optional.of(predicate);
            }
        };
    }
}

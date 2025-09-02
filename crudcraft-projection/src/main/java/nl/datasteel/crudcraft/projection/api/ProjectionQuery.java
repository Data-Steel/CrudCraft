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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Encapsulates filtering and paging information for a projection query.
 */
public record ProjectionQuery<T>(FilterCriteria<T> filter, Pageable pageable) {

    /**
     * Creates a new ProjectionQuery with the specified filter criteria and pageable.
     *
     * @param filter   the filter criteria to apply to the query
     * @param pageable the pagination information for the query
     * @param <T>      the type of the entity being queried
     * @return a new ProjectionQuery instance
     */
    public static <T> ProjectionQuery<T> of(FilterCriteria<T> filter, Pageable pageable) {
        return new ProjectionQuery<>(filter, pageable);
    }

    /**
     * Converts the filter criteria into a JPA Specification.
     *
     * @return an Optional containing the Specification if it can be created, otherwise empty.
     */
    public Optional<Specification<T>> asSpecification() {
        return filter.asSpecification();
    }

    /**
     * Converts the filter criteria into a QueryDSL Predicate.
     *
     * @return an Optional containing the Predicate if it can be created, otherwise empty.
     */
    public Optional<Predicate> asPredicate() {
        return filter.asPredicate();
    }
}

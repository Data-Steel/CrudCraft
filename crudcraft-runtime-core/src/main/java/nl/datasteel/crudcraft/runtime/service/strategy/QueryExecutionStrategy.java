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

package nl.datasteel.crudcraft.runtime.service.strategy;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


/**
 * Strategy abstraction for executing JPA Specification queries.
 *
 * @param <T> entity type
 */
public interface QueryExecutionStrategy<T> {

    /**
     * Finds all entities matching the given specification, returning a paginated result.
     *
     * @param spec the JPA specification to apply
     * @param pageable the pagination information
     * @return a page of entities matching the criteria
     */
    Page<T> findAll(Specification<T> spec, Pageable pageable);

    /**
     * Finds all entities matching the given specification.
     *
     * @param spec the JPA specification to apply
     * @return a list of entities matching the criteria
     */
    List<T> findAll(Specification<T> spec);

    /**
     * Finds all entities matching the given specification, returning a paginated result.
     *
     * @param spec the JPA specification to apply
     * @param pageable the pagination information
     * @param projection the class type for the projection
     * @param <R> projection type
     * @return a page of entities matching given criteria, projected to the specified type.
     */
    <R> Page<R> findAll(Specification<T> spec, Pageable pageable, Class<R> projection);

    /**
     * Finds all entities matching the given specification, returning a paginated result.
     *
     * @param spec the JPA specification to apply
     * @param projection the class type for the projection
     * @param <R> projection type
     * @return a page of entities matching the criteria, projected to the specified type
     */
    <R> List<R> findAll(Specification<T> spec, Class<R> projection);

    /**
     * Finds a single entity matching the given specification.
     *
     * @param spec the JPA specification to apply
     * @return an Optional containing the found entity, or empty if none found
     */
    Optional<T> findOne(Specification<T> spec);

    /**
     * Finds a single entity matching the given specification.
     *
     * @param spec the JPA specification to apply
     * @param projection the class type for the projection
     * @param <R> projection type
     * @return an Optional containing the found entity, or empty if none found
     */
    <R> Optional<R> findOne(Specification<T> spec, Class<R> projection);

    /**
     * Checks if any entity matches the given predicate and specification.
     *
     * @param spec the JPA specification to apply
     * @return true if at least one entity matches the criteria, false otherwise
     */
    boolean exists(Specification<T> spec);

    /**
     * Counts the number of entities matching the given specification.
     *
     * @param spec the JPA specification to apply
     * @return the count of entities matching the criteria
     */
    long count(Specification<T> spec);
}

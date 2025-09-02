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

package nl.datasteel.crudcraft.runtime.service.strategy;

import com.querydsl.core.types.Predicate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Strategy abstraction for executing queries using either QueryDSL or JPA Specifications.
 */
public interface QueryExecutionStrategy<T> {

    /**
     * Finds all entities matching the given predicate and specification,
     * returning a paginated result.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @param pageable the pagination information
     * @return a page of entities matching the criteria
     */
    Page<T> findAll(Predicate predicate, Specification<T> spec, Pageable pageable);

    /**
     * Finds all entities matching the given predicate and specification.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @return a list of entities matching the criteria
     */
    List<T> findAll(Predicate predicate, Specification<T> spec);

    /**
     * Finds all entities matching the given predicate and specification,
     * returning a paginated result.
     *
     * @param predicate  the Querydsl predicate to filter results
     * @param spec       the JPA specification to apply additional filters
     * @param pageable   the pagination information
     * @param projection the class type for the projection
     * @return a page of entities matching given criteria,
     *     projected to the specified type.
     */
    <R> Page<R> findAll(Predicate predicate, Specification<T> spec,
                        Pageable pageable, Class<R> projection);

    /**
     * Finds all entities matching the given predicate and specification,
     * returning a paginated result.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @param projection the class type for the projection
     * @return a page of entities matching the criteria, projected to the specified type
     */
    <R> List<R> findAll(Predicate predicate, Specification<T> spec, Class<R> projection);

    /**
     * Finds a single entity matching the given predicate and specification.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @return an Optional containing the found entity, or empty if none found
     */
    Optional<T> findOne(Predicate predicate, Specification<T> spec);

    /**
     * Finds a single entity matching the given predicate and specification.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @return an Optional containing the found entity, or empty if none found
     */
    <R> Optional<R> findOne(Predicate predicate, Specification<T> spec, Class<R> projection);

    /**
     * Checks if any entity matches the given predicate and specification.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @return true if at least one entity matches the criteria, false otherwise
     */
    boolean exists(Predicate predicate, Specification<T> spec);

    /**
     * Counts the number of entities matching the given predicate and specification.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @return the count of entities matching the criteria
     */
    long count(Predicate predicate, Specification<T> spec);
}


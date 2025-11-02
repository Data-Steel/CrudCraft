/*
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.runtime.service.strategy;

import com.querydsl.core.types.Predicate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Executes queries using a {@link JpaSpecificationExecutor}.
 */
public class JpaSpecificationExecutionStrategy<T> implements QueryExecutionStrategy<T> {

    /**
     * The repository to execute queries against.
     * This is a stateless, thread-safe proxy provided by Spring Data.
     */
    private final JpaSpecificationExecutor<T> repository;

    /**
     * Constructs a new execution strategy using the provided repository.
     *
     * @param repository the JPA specification executor to use for query execution
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring Data repository is a stateless, "
                    + "thread-safe proxy; storing the reference is intended and safe")
    public JpaSpecificationExecutionStrategy(JpaSpecificationExecutor<T> repository) {
        this.repository = repository;
    }

    /**
     * Executes a paginated query with the given predicate and specification.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec      the JPA specification to apply additional filters
     * @param pageable  the pagination information
     * @return a page of entities matching the criteria
     */
    @Override
    public Page<T> findAll(Predicate predicate, Specification<T> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    /**
     * Executes a query with the given predicate and specification,
     * returning all matching entities.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec      the JPA specification to apply additional filters
     * @return a list of entities matching the criteria
     */
    @Override
    public List<T> findAll(Predicate predicate, Specification<T> spec) {
        return repository.findAll(spec);
    }

    /**
     * Finds all entities matching the given predicate and specification,
     * paginated and projected to the specified type.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @param pageable the pagination information
     * @param projection the class of the projection type
     * @return a page of projected entities
     */
    @Override
    public <R> Page<R> findAll(Predicate predicate, Specification<T> spec,
                               Pageable pageable, Class<R> projection) {
        return repository.findBy(spec, q -> q.as(projection).page(pageable));
    }

    /**
     * Finds all entities matching the given predicate and specification,
     * projecting them to the specified type.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @param projection the class of the projection type
     * @return a list of projected entities
     */
    @Override
    public <R> List<R> findAll(Predicate predicate, Specification<T> spec, Class<R> projection) {
        return repository.findBy(spec, q -> q.as(projection).all());
    }

    /**
     * Finds a single entity matching the given predicate and specification.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec      the JPA specification to apply additional filters
     * @return an Optional containing the entity if found, or empty if not
     */
    @Override
    public Optional<T> findOne(Predicate predicate, Specification<T> spec) {
        return repository.findOne(spec);
    }

    /**
     * Finds a single entity matching the given predicate and specification,
     * projecting it to the specified type.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @param projection the class of the projection type
     * @return an Optional containing the projected entity, or empty if no match is found
     */
    @Override
    public <R> Optional<R> findOne(Predicate predicate,
                                   Specification<T> spec, Class<R> projection) {
        return repository.findBy(spec, q -> q.as(projection).first());
    }

    /**
     * Checks if any entities match the given predicate and specification.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @return true if at least one entity matches the criteria, false otherwise
     */
    @Override
    public boolean exists(Predicate predicate, Specification<T> spec) {
        return repository.count(spec) > 0;
    }

    /**
     * Counts the number of entities matching the given predicate and specification.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @return the count of entities matching the criteria
     */
    @Override
    public long count(Predicate predicate, Specification<T> spec) {
        return repository.count(spec);
    }
}


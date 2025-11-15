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
import com.querydsl.core.types.dsl.Expressions;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * Executes queries using a {@link QuerydslPredicateExecutor}.
 */
public class QuerydslExecutionStrategy<T, R, F> implements QueryExecutionStrategy<T> {

    /**
     * The repository to execute queries against.
     * This is a stateless, thread-safe proxy provided by Spring Data.
     */
    private final QuerydslPredicateExecutor<T> repository;

    /**
     * Optional JPA specification executor when the repository also implements
     * {@link JpaSpecificationExecutor}. Allows falling back to specifications
     * when no QueryDSL predicate is provided.
     */
    private final JpaSpecificationExecutor<T> specRepository;

    /**
     * Mapper used for converting entities into DTO projections.
     */
    private final EntityMapper<T, ?, R, F, ?> mapper;

    /**
     * Full response DTO class.
     */
    private final Class<R> responseClass;

    /**
     * Reference DTO class.
     */
    private final Class<F> refClass;

    /**
     * Cache of mapper methods by return type for specialized DTOs.
     * Key: projection class, Value: mapper method
     */
    private final Map<Class<?>, Method> mapperMethodCache = new ConcurrentHashMap<>();

    /**
     * Constructs a new execution strategy using the provided repository.
     *
     * @param repository the QueryDSL predicate executor to use for query execution
     * @param mapper the mapper for converting entities to DTOs
     * @param responseClass the response DTO class
     * @param refClass the reference DTO class
     */
    public QuerydslExecutionStrategy(QuerydslPredicateExecutor<T> repository,
                                     EntityMapper<T, ?, R, F, ?> mapper,
                                     Class<R> responseClass,
                                     Class<F> refClass,
                                     JpaSpecificationExecutor<T> specRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.responseClass = responseClass;
        this.refClass = refClass;
        this.specRepository = specRepository;
    }

    /**
     * Ensures the predicate passed to the repository is never {@code null}.
     * Spring Data's QuerydslPredicateExecutor rejects {@code null} predicates,
     * so when no filtering is required we supply a predicate that always
     * evaluates to {@code true}.
     *
     * @param predicate the predicate to use, may be {@code null}
     * @return the provided predicate or one that always evaluates to {@code true}
     */
    private Predicate nonNullPredicate(Predicate predicate) {
        return predicate == null ? Expressions.asBoolean(true).isTrue() : predicate;
    }

    /**
     * Finds a mapper method that returns the specified projection type.
     * This method uses reflection to find mapper methods for specialized DTOs.
     * Results are cached for performance.
     *
     * @param projectionClass the projection class to find a mapper for
     * @return a Function that maps entities to the projection type
     * @throws UnsupportedOperationException if no suitable mapper method is found
     */
    @SuppressWarnings("unchecked")
    private <P> Function<T, P> findMapperMethod(Class<P> projectionClass) {
        // Check standard mappings first
        if (projectionClass.equals(responseClass)) {
            return entity -> (P) mapper.toResponse(entity);
        }
        if (projectionClass.equals(refClass)) {
            return entity -> (P) mapper.toRef(entity);
        }

        // Try to find cached method
        Method cachedMethod = mapperMethodCache.get(projectionClass);
        if (cachedMethod != null) {
            return entity -> {
                try {
                    return (P) cachedMethod.invoke(mapper, entity);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke mapper method: " + cachedMethod.getName(), e);
                }
            };
        }

        // Search for a method that returns the projection type
        Method foundMethod = null;
        for (Method method : mapper.getClass().getMethods()) {
            // Look for public methods with single entity parameter that return projection type
            if (method.getReturnType().equals(projectionClass)
                    && method.getParameterCount() == 1
                    && !method.getName().equals("getIdFromRequest")) {
                foundMethod = method;
                mapperMethodCache.put(projectionClass, method);
                break;
            }
        }

        if (foundMethod != null) {
            Method finalFoundMethod = foundMethod;
            return entity -> {
                try {
                    return (P) finalFoundMethod.invoke(mapper, entity);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke mapper method: " + finalFoundMethod.getName(), e);
                }
            };
        }

        throw new UnsupportedOperationException(
                "Projection type not supported: " + projectionClass.getName());
    }

    /**
     * Finds all entities matching the given predicate and specification,
     * returning a paginated result.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @param pageable the pagination information
     * @return a page of entities matching the criteria
     */
    @Override
    public Page<T> findAll(Predicate predicate, Specification<T> spec, Pageable pageable) {
        if (predicate == null && specRepository != null) {
            return specRepository.findAll(spec, pageable);
        }
        return repository.findAll(nonNullPredicate(predicate), pageable);
    }

    /**
     * Finds all entities matching the given predicate and specification.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @return a list of entities matching the criteria
     */
    @Override
    public List<T> findAll(Predicate predicate, Specification<T> spec) {
        if (predicate == null && specRepository != null) {
            return specRepository.findAll(spec);
        }
        Iterable<T> iterable = repository.findAll(nonNullPredicate(predicate));
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    /**
     * Finds all entities matching the given predicate and specification,
     * returning a paginated result.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @param pageable the pagination information
     * @return a page of entities matching the criteria
     */
    @Override
    public <P> Page<P> findAll(Predicate predicate, Specification<T> spec,
                               Pageable pageable, Class<P> projection) {
        if (predicate == null && specRepository != null) {
            if (projection.isInterface()) {
                return specRepository.findBy(spec, q -> q.as(projection).page(pageable));
            }
            Page<T> entities = specRepository.findAll(spec, pageable);
            Function<T, P> mapperFn = findMapperMethod(projection);
            return entities.map(mapperFn::apply);
        }
        Predicate nonNullPredicate = nonNullPredicate(predicate);
        if (projection.isInterface()) {
            return repository.findBy(nonNullPredicate, q -> q.as(projection).page(pageable));
        }
        Page<T> entities = repository.findAll(nonNullPredicate, pageable);
        Function<T, P> mapperFn = findMapperMethod(projection);
        return entities.map(mapperFn::apply);
    }

    /**
     * Finds all entities matching the given predicate and specification,
     * returning them as the specified projection type.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @param projection the class of the projection type
     * @return a list of projected entities matching the criteria
     */
    @Override
    public <P> List<P> findAll(Predicate predicate, Specification<T> spec, Class<P> projection) {
        if (predicate == null && specRepository != null) {
            if (projection.isInterface()) {
                return specRepository.findBy(spec, q -> q.as(projection).all());
            }
            List<T> entities = specRepository.findAll(spec);
            Function<T, P> mapperFn = findMapperMethod(projection);
            return entities.stream().map(mapperFn).toList();
        }
        Predicate nonNullPredicate = nonNullPredicate(predicate);
        if (projection.isInterface()) {
            return repository.findBy(nonNullPredicate, q -> q.as(projection).all());
        }
        Iterable<T> iterable = repository.findAll(nonNullPredicate);
        List<P> result = new ArrayList<>();
        Function<T, P> mapperFn = findMapperMethod(projection);
        iterable.forEach(entity -> result.add(mapperFn.apply(entity)));
        return result;
    }

    /**
     * Finds a single entity matching the given predicate and specification.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @return an Optional containing the entity if found, or empty if not found
     */
    @Override
    public Optional<T> findOne(Predicate predicate, Specification<T> spec) {
        if (predicate == null && specRepository != null) {
            return specRepository.findOne(spec);
        }
        return repository.findOne(nonNullPredicate(predicate));
    }

    /**
     * Finds a single entity matching the given predicate and specification,
     * returning it as the specified projection type.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @param projection the class of the projection type
     * @return an Optional containing the projected entity, or empty if no match is found
     */
    @Override
    public <P> Optional<P> findOne(Predicate predicate, Specification<T> spec,
                                   Class<P> projection) {
        if (predicate == null && specRepository != null) {
            if (projection.isInterface()) {
                return specRepository.findBy(spec, q -> q.as(projection).first());
            }
            Optional<T> entity = specRepository.findOne(spec);
            if (entity.isEmpty()) {
                return Optional.empty();
            }
            Function<T, P> mapperFn = findMapperMethod(projection);
            return Optional.of(mapperFn.apply(entity.get()));
        }
        Predicate nonNullPredicate = nonNullPredicate(predicate);
        if (projection.isInterface()) {
            return repository.findBy(nonNullPredicate, q -> q.as(projection).first());
        }
        Optional<T> entity = repository.findOne(nonNullPredicate);
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        Function<T, P> mapperFn = findMapperMethod(projection);
        return Optional.of(mapperFn.apply(entity.get()));
    }

    /**
     * Checks if any entity matches the given predicate and specification.
     *
     * @param predicate the QueryDSL predicate to filter results
     * @param spec the JPA specification to apply additional filters
     * @return true if at least one entity matches the criteria, false otherwise
     */
    @Override
    public boolean exists(Predicate predicate, Specification<T> spec) {
        if (predicate == null && specRepository != null) {
            return specRepository.count(spec) > 0;
        }
        return repository.exists(nonNullPredicate(predicate));
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
        if (predicate == null && specRepository != null) {
            return specRepository.count(spec);
        }
        return repository.count(nonNullPredicate(predicate));
    }
}


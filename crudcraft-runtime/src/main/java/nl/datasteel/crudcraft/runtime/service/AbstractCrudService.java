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

package nl.datasteel.crudcraft.runtime.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import jakarta.transaction.Transactional;
import java.beans.Introspector;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.Identified;
import nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import nl.datasteel.crudcraft.runtime.search.SearchRequest;
import nl.datasteel.crudcraft.runtime.security.FieldSecurityUtil;
import nl.datasteel.crudcraft.runtime.service.strategy.JpaSpecificationExecutionStrategy;
import nl.datasteel.crudcraft.runtime.service.strategy.QueryExecutionStrategy;
import nl.datasteel.crudcraft.runtime.service.strategy.QuerydslExecutionStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * Generic abstract base for CRUD operations, with hooks for custom business logic,
 * bulk operations, upsert, and partial updates (patch).
 *
 * @param <T>  the JPA entity type
 * @param <U>  the upsert/patch request DTO type
 * @param <R>  the full response DTO type
 * @param <F>  the reference DTO type
 * @param <ID> the identifier type (e.g., UUID, Long)
 */
public abstract class AbstractCrudService<T, U, R, F, ID> implements CrudService<T, U, R, F, ID> {

    /**
     * The JPA repository used for CRUD operations.
     * This is the primary interface for interacting with the database.
     */
    protected final JpaRepository<T, ID> repository;

    /**
     * The mapper used to convert between entity and DTOs.
     * This handles the conversion logic for create, update, and response DTOs.
     */
    protected final EntityMapper<T, U, R, F, ID> mapper;
    protected final Class<T> entityClass;

    /**
     * The full response class type, used for detailed entity projections.
     * This is typically used for API responses that require all entity details.
     */
    protected final Class<R> responseClass;

    /**
     * The reference class type, used for lightweight projections.
     * This is useful for scenarios where full entity details are not required.
     */
    protected final Class<F> refClass;

    /**
     * The strategy used to execute queries, either via QueryDSL or JPA Specifications.
     * This allows for flexible query execution based on the repository capabilities.
     */
    protected final QueryExecutionStrategy<T> queryExecutor;

    /**
     * The attribute name for the ID field in the entity.
     * This is used to construct predicates.
     */
    protected static final String ID_ATTRIBUTE = "id";

    /**
     * Constructor to initialize the service with repository, mapper, and entity classes.
     *
     * @param repository the JPA repository for CRUD operations
     * @param mapper the entity mapper for converting between entity and DTOs
     * @param entityClass the JPA entity class type
     * @param responseClass the full response DTO class type
     * @param refClass the reference DTO class type
     */
    @SuppressWarnings({"unchecked", "PatternVariableName"})
    protected AbstractCrudService(JpaRepository<T, ID> repository,
                                  EntityMapper<T, U, R, F, ID> mapper,
                                  Class<T> entityClass,
                                  Class<R> responseClass,
                                  Class<F> refClass) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityClass = entityClass;
        this.responseClass = responseClass;
        this.refClass = refClass;

        if (repository instanceof QuerydslPredicateExecutor<?> queryDslRepo) {
            JpaSpecificationExecutor<T> specRepo = repository instanceof JpaSpecificationExecutor
                    ? (JpaSpecificationExecutor<T>) repository
                    : null;
            this.queryExecutor = new QuerydslExecutionStrategy<>(
                    (QuerydslPredicateExecutor<T>) queryDslRepo,
                    mapper,
                    responseClass,
                    refClass,
                    specRepo);
        } else {
            this.queryExecutor = new JpaSpecificationExecutionStrategy<>(
                (JpaSpecificationExecutor<T>) repository);
        }

        // TODO: ADD A DYNAMIC WAY TO RETRIEVE ID ATTRIBUTE NAME
    }

    /**
     * Override to provide row-level security filter using QueryDSL.
     */
    protected Predicate rowSecurityPredicate() {
        List<RowSecurityHandler<?>> handlers = rowSecurityHandlers();
        if (handlers == null || handlers.isEmpty()) {
            return null;
        }
        List<Predicate> predicates = new java.util.ArrayList<>();
        for (RowSecurityHandler<?> h : handlers) {
            Predicate p = h.rowFilterPredicate();
            if (p != null) {
                predicates.add(p);
            }
        }

        if (predicates.isEmpty()) {
            return null;
        }

        return predicates.size() == 1
                ? predicates.getFirst()
                : com.querydsl.core.types.ExpressionUtils.allOf(predicates);
    }

    /**
     * Retrieve a paginated list of DTO projections,
     * optionally filtered by a search query.
     *
     * @param pageable    pagination information
     * @param searchQuery optional search string
     * @return page of DTOs matching criteria
     */
    @Override
    @Transactional
    public Page<R> findAll(Pageable pageable, String searchQuery) {
        Predicate predicate = rowSecurityPredicate();
        Specification<T> spec = rowSecurityFilter();
        return queryExecutor.findAll(predicate, spec, pageable, responseClass);
    }

    /**
     * Retrieve a paginated list of DTO projections,
     * optionally filtered by a search request.
     *
     * @param request  the search request containing criteria
     * @param pageable pagination information
     * @return page of DTOs matching criteria
     */
    @Override
    @Transactional
    public Page<R> search(SearchRequest<T> request, Pageable pageable) {
        Predicate searchPredicate = request == null ? null : request.toPredicate();
        Predicate rowPred = rowSecurityPredicate();
        Predicate finalPred;

        if (searchPredicate != null && rowPred != null) {
            finalPred = com.querydsl.core.types.ExpressionUtils.allOf(searchPredicate, rowPred);
        } else {
            finalPred = searchPredicate != null ? searchPredicate : rowPred;
        }

        Specification<T> searchSpec = request == null ? null : request.toSpecification();
        Specification<T> rowSpec = rowSecurityFilter();
        Specification<T> spec;
        if (searchSpec != null && rowSpec != null) {
            spec = searchSpec.and(rowSpec);
        } else {
            spec = searchSpec != null ? searchSpec : rowSpec;
        }

        return queryExecutor.findAll(finalPred, spec, pageable, responseClass);
    }

    /**
     * Retrieve a paginated list of reference DTO projections,
     * optionally filtered by a search request.
     *
     * @param request  the search request containing criteria
     * @param pageable pagination information
     * @return page of reference DTOs matching criteria
     */
    @Override
    @Transactional
    public Page<F> searchRef(SearchRequest<T> request, Pageable pageable) {
        Predicate searchPredicate = request == null ? null : request.toPredicate();
        Predicate rowPred = rowSecurityPredicate();

        Predicate finalPred;
        if (searchPredicate != null && rowPred != null) {
            finalPred = com.querydsl.core.types.ExpressionUtils.allOf(searchPredicate, rowPred);
        } else {
            finalPred = searchPredicate != null ? searchPredicate : rowPred;
        }
        Specification<T> searchSpec = request == null ? null : request.toSpecification();
        Specification<T> rowSpec = rowSecurityFilter();
        Specification<T> spec;
        if (searchSpec != null && rowSpec != null) {
            spec = searchSpec.and(rowSpec);
        } else {
            spec = searchSpec != null ? searchSpec : rowSpec;
        }
        return queryExecutor.findAll(finalPred, spec, pageable, refClass);
    }

    /**
     * Override to provide row-level security filter.
     */
    @SuppressWarnings("unchecked")
    protected Specification<T> rowSecurityFilter() {
        List<RowSecurityHandler<?>> handlers = rowSecurityHandlers();
        Specification<T> spec = null;
        if (handlers != null) {
            for (RowSecurityHandler<?> h : handlers) {
                Specification<?> s = h.rowFilter();
                if (s != null) {
                    spec = spec == null ? (Specification<T>) s : spec.and((Specification<T>) s);
                }
            }
        }
        return spec;
    }

    /**
     * Override to provide a {@link RowSecurityHandler} used for both filtering and
     * write-time validation.
     */
    protected List<RowSecurityHandler<?>> rowSecurityHandlers() {
        return null;
    }

    /**
     * Apply row-security validation/mutation to the given entity prior to persistence.
     */
    @SuppressWarnings("unchecked")
    protected void applyRowSecurity(T entity) {
        List<RowSecurityHandler<?>> handlers = rowSecurityHandlers();
        if (handlers != null) {
            for (RowSecurityHandler<?> h : handlers) {
                ((RowSecurityHandler<T>) h).apply(entity);
            }
        }
    }

    /**
     * Return a QueryDSL predicate for a single ID.
     *
     * @param id identifier
     * @return predicate matching the given ID
     */
    @SuppressWarnings("unchecked")
    private Predicate idPredicate(ID id) {
        String alias = Introspector.decapitalize(entityClass.getSimpleName());
        PathBuilder<T> entityPath = new PathBuilder<>(entityClass, alias);
        return entityPath.get(ID_ATTRIBUTE, (Class<ID>) id.getClass()).eq(id);
    }

    /**
     * Return a QueryDSL predicate for a collection of IDs.
     */
    @SuppressWarnings("unchecked")
    private Predicate idsPredicate(Collection<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        ID first = ids.iterator().next();
        String alias = Introspector.decapitalize(entityClass.getSimpleName());
        PathBuilder<T> entityPath = new PathBuilder<>(entityClass, alias);
        return entityPath.get(ID_ATTRIBUTE, (Class<ID>) first.getClass()).in(ids);
    }

    /**
     * Retrieve entities by a collection of IDs.
     *
     * @param ids collection of identifiers
     * @return list of entities with matching IDs
     */
    @Override
    @Transactional
    public List<R> findByIds(Collection<ID> ids) {
        Predicate idPredicate = idsPredicate(ids);
        BooleanBuilder builder = new BooleanBuilder();
        if (idPredicate != null) {
            builder.and(idPredicate);
        }
        Predicate rowPred = rowSecurityPredicate();
        if (rowPred != null) {
            builder.and(rowPred);
        }
        Predicate finalPred = builder.hasValue() ? builder : null;

        Specification<T> idSpec = (root, query, cb) -> root.get(ID_ATTRIBUTE).in(ids);
        Specification<T> spec = idSpec.and(rowSecurityFilter());
        return queryExecutor.findAll(finalPred, spec, responseClass);
    }

    /**
     * Find an entity by ID, returning Optional.
     *
     * @param id identifier
     * @return Optional containing entity if found
     */
    @Override
    @Transactional
    public Optional<R> findByIdOptional(ID id) {
        Predicate idPred = idPredicate(id);
        BooleanBuilder builder = new BooleanBuilder().and(idPred);
        Predicate rowPred = rowSecurityPredicate();
        if (rowPred != null) {
            builder.and(rowPred);
        }
        Predicate finalPred = builder.hasValue() ? builder : null;
        Specification<T> spec = byId(id).and(rowSecurityFilter());

        return queryExecutor.findOne(finalPred, spec, responseClass);
    }

    /**
     * Find an entity by ID or throw ResourceNotFoundException.
     *
     * @param id identifier
     * @return found entity
     * @throws ResourceNotFoundException if not found
     */
    @Override
    @Transactional
    public R findById(ID id) {
        return findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("%s with ID '%s' could not be found",
                                entityClass.getSimpleName(), id)));
    }

    /**
     * Return a reference proxy to the entity without hitting the database immediately.
     *
     * @param id identifier
     * @return entity reference proxy
     */
    @Override
    @Transactional
    public T findReferenceById(ID id) {
        Predicate idPred = idPredicate(id);
        BooleanBuilder builder = new BooleanBuilder().and(idPred);
        Predicate rowPred = rowSecurityPredicate();
        if (rowPred != null) {
            builder.and(rowPred);
        }
        Predicate finalPred = builder.hasValue() ? builder : null;
        Specification<T> spec = byId(id).and(rowSecurityFilter());

        if (!queryExecutor.exists(finalPred, spec)) {
            throw new ResourceNotFoundException(
                    String.format("%s with ID '%s' could not be found",
                            entityClass.getSimpleName(), id));
        }
        return repository.getReferenceById(id);
    }

    /**
     * Load the entity by ID applying row-security; throws if not found.
     */
    protected T loadEntity(ID id) {
        Predicate idPred = idPredicate(id);
        BooleanBuilder builder = new BooleanBuilder().and(idPred);
        Predicate rowPred = rowSecurityPredicate();
        if (rowPred != null) {
            builder.and(rowPred);
        }
        Predicate finalPred = builder.hasValue() ? builder : null;
        Specification<T> spec = byId(id).and(rowSecurityFilter());

        return queryExecutor.findOne(finalPred, spec)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("%s with ID '%s' could not be found",
                                entityClass.getSimpleName(), id)));
    }

    /**
     * Create a new entity from the request DTO, invoking pre-/post-save hooks.
     *
     * @param request DTO containing creation data
     * @return created DTO
     */
    @Override
    @Transactional
    public R create(U request) {
        T entity = mapper.fromRequest(request);
        applyRowSecurity(entity);
        preSave(entity, request);

        T saved = repository.save(entity);
        postSave(saved);

        return mapper.toResponse(saved);
    }

    /**
     * Update an existing entity by ID, invoking pre-/post-update hooks.
     *
     * @param id identifier of the entity to update
     * @param request DTO containing updated data
     * @return updated DTO
     */
    @Override
    @Transactional
    public R update(ID id, U request) {
        T entity = loadEntity(id);
        FieldSecurityUtil.filterWrite(request, entity);
        mapper.update(entity, request);
        applyRowSecurity(entity);
        preSave(entity, request);

        T saved = repository.save(entity);
        postSave(saved);

        return mapper.toResponse(saved);
    }

    /**
     * Partially update an existing entity (PATCH semantics).
     * Override prePatch/postPatch for custom logic.
     *
     * @param id identifier of the entity to patch
     * @param request DTO containing only fields to update
     * @return patched DTO
     */
    @Override
    @Transactional
    public R patch(ID id, U request) {
        T entity = loadEntity(id);
        FieldSecurityUtil.filterWrite(request, entity);
        mapper.patch(entity, request);
        applyRowSecurity(entity);
        preSave(entity, request);

        T saved = repository.save(entity);
        postSave(saved);

        return mapper.toResponse(saved);
    }

    /**
     * Create or update an entity based on presence/existence of ID in request (UPSERT).
     *
     * @param request DTO containing upsert data (must carry ID when updating)
     * @return created or updated DTO
     */
    @Override
    @Transactional
    public R upsert(U request) {
        ID id = mapper.getIdFromRequest(request);
        boolean exists = (id != null) && existsById(id);

        return exists ? update(id, request) : create(request);
    }

    /**
     * Bulk create entities from a collection of request DTOs.
     *
     * @param requests collection of creation DTOs
     * @return list of created DTOs
     */
    @Override
    @Transactional
    public List<R> createAll(Collection<U> requests) {
        List<T> entities = requests.stream()
                .map(mapper::fromRequest)
                .toList();
        entities.forEach(e -> {
            applyRowSecurity(e);
            preSave(e, null);
        });

        List<T> saved = repository.saveAll(entities);
        saved.forEach(this::postSave);

        return saved.stream().map(mapper::toResponse).toList();
    }

    /**
     * Bulk upsert: create or update for each request in the batch.
     *
     * @param requests collection of upsert DTOs
     * @return list of created or updated DTOs
     */
    @Override
    @Transactional
    public List<R> upsertAll(Collection<U> requests) {
        return requests.stream()
                .map(this::upsert)
                .toList();
    }

    /**
     * Bulk update entities by their IDs.
     *
     * @param requests collection of update DTOs
     * @return list of updated DTOs
     */
    @Override
    @Transactional
    public List<R> updateAll(List<Identified<ID, U>> requests) {
        return requests.stream()
                .map(r -> update(r.getId(), r.getData()))
                .toList();
    }

    /**
     * Bulk patch entities by their IDs.
     *
     * @param requests collection of patch DTOs
     * @return list of patched DTOs
     */
    @Override
    @Transactional
    public List<R> patchAll(List<Identified<ID, U>> requests) {
        return requests.stream()
                .map(r -> patch(r.getId(), r.getData()))
                .toList();
    }

    /**
     * Delete an entity by ID, invoking pre/post-delete hooks.
     *
     * @param id identifier of entity to delete
     */
    @Override
    @Transactional
    public void delete(ID id) {
        T entity = loadEntity(id);
        applyRowSecurity(entity);
        preDelete(entity);

        repository.delete(entity);
        postDelete(entity);
    }

    /**
     * Bulk delete entities by their IDs.
     *
     * @param ids collection of identifiers to delete
     */
    @Override
    @Transactional
    public void deleteAllByIds(Collection<ID> ids) {
        ids.forEach(this::delete);
    }

    /**
     * Check if an entity exists by ID.
     *
     * @param id identifier
     * @return true if exists
     */
    @Override
    @Transactional
    public boolean existsById(ID id) {
        Predicate idPred = idPredicate(id);
        BooleanBuilder builder = new BooleanBuilder().and(idPred);
        Predicate rowPred = rowSecurityPredicate();
        if (rowPred != null) {
            builder.and(rowPred);
        }
        Predicate finalPred = builder.hasValue() ? builder : null;
        Specification<T> spec = byId(id).and(rowSecurityFilter());

        return queryExecutor.exists(finalPred, spec);
    }

    /**
     * Count total number of entities.
     *
     * @return count of entities
     */
    @Override
    @Transactional
    public long count() {
        Predicate predicate = rowSecurityPredicate();
        Specification<T> spec = rowSecurityFilter();
        return queryExecutor.count(predicate, spec);
    }

    // -------------------------------------------------------------------------
    // Helper methods for internal use
    // -------------------------------------------------------------------------

    /**
     * Create a Specification to filter by ID.
     *
     * @param id identifier
     * @return Specification for the given ID
     */
    protected Specification<T> byId(ID id) {
        return (root, query, cb) -> cb.equal(root.get(ID_ATTRIBUTE), id);
    }

    // -------------------------------------------------------------------------
    // Hook methods for custom business logic. Default no-ops.
    // -------------------------------------------------------------------------

    /**
     * Pre-save hook for custom logic before an entity is saved.
     * This is called before the entity is persisted to the repository.
     *
     * @param entity  the entity to be saved
     * @param request the request DTO containing creation or update data
     */
    protected void preSave(T entity, U request) {
        // no-op by default
    }

    /**
     * Post-save hook for custom logic after an entity is saved.
     * This is called after the entity has been persisted to the repository.
     *
     * @param entity the entity that was saved
     */
    protected void postSave(T entity) {
        // no-op by default
    }

    /**
     * Pre-delete hook for custom logic before an entity is deleted.
     * This is called before the entity is removed from the repository.
     *
     * @param entity the entity that will be deleted
     */
    protected void preDelete(T entity) {
        // no-op by default
    }

    /**
     * Post-delete hook for custom logic after an entity is deleted.
     * This is called after the entity has been removed from the repository.
     *
     * @param entity the entity that was deleted
     */
    protected void postDelete(T entity) {
        // no-op by default
    }
}

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

package nl.datasteel.crudcraft.runtime.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import nl.datasteel.crudcraft.runtime.InternalApi;
import nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import nl.datasteel.crudcraft.runtime.service.projection.ProjectionAdapter;
import nl.datasteel.crudcraft.runtime.service.strategy.QueryExecutionStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


/**
 * Read-side layer for projection adaptation, reference responses, search dispatch, and visible
 * entity lookup.
 *
 * @param <T> entity type
 * @param <U> request DTO type
 * @param <R> full response DTO type
 * @param <F> reference DTO type
 * @param <ID> identifier type
 */
@InternalApi
abstract class ProjectionService<T, U, R, F, ID> {

    /**
     * Retrieves all visible entities as default response DTOs.
     *
     * @param pageable pagination request
     * @return page of default response DTOs after runtime read filters
     */
    @Transactional(readOnly = true)
    public Page<R> findAll(Pageable pageable) {
        return findAll(pageable, responseClass());
    }

    /**
     * Retrieves all visible entities using the supplied projection DTO.
     *
     * @param pageable pagination request
     * @param projection projection DTO type
     * @param <P> projection response type
     * @return page of projected responses after runtime read filters
     */
    @Transactional(readOnly = true)
    public <P> Page<P> findAll(Pageable pageable, Class<P> projection) {
        return findAll(null, pageable, projection);
    }

    /**
     * Retrieves visible entities matching the supplied specification using the given projection.
     *
     * @param specification optional caller specification
     * @param pageable pagination request
     * @param projection projection DTO type
     * @param <P> projection response type
     * @return page of projected responses after runtime read filters
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <P> Page<P> findAll(
            Specification<T> specification, Pageable pageable, Class<P> projection) {
        Class<P> effectiveProjection = projection != null ? projection : (Class<P>) responseClass();
        Specification<T> spec = combine(specification, runtimeReadFilter());
        if (isDefaultProjection(effectiveProjection)) {
            return mapDefaultProjectionPage(spec, pageable, effectiveProjection)
                    .map(this::afterRead);
        }
        Page<P> projectedPage = projectPage(spec, pageable, effectiveProjection);
        if (projectedPage != null) {
            return projectedPage.map(this::afterRead);
        }
        return queryExecutor().findAll(spec, pageable, effectiveProjection).map(this::afterRead);
    }

    /**
     * Retrieves all visible entities as reference DTOs.
     *
     * @param pageable pagination request
     * @return page of reference DTOs
     */
    @Transactional(readOnly = true)
    public Page<F> findAllRef(Pageable pageable) {
        return findAll(pageable, refClass());
    }

    /**
     * Searches visible entities using a generated search request and default response DTOs.
     *
     * @param searchRequest generated search request object
     * @param pageable pagination request
     * @return page of matching default response DTOs
     */
    @Transactional(readOnly = true)
    public Page<R> search(Object searchRequest, Pageable pageable) {
        return findAll(effectiveReadSpecification(searchRequest), pageable, responseClass());
    }

    /**
     * Searches visible entities using a generated search request and projection DTO.
     *
     * @param searchRequest generated search request object
     * @param pageable pagination request
     * @param projection projection DTO type
     * @param <P> projection response type
     * @return page of matching projected responses
     */
    @Transactional(readOnly = true)
    public <P> Page<P> search(Object searchRequest, Pageable pageable, Class<P> projection) {
        return findAll(effectiveReadSpecification(searchRequest), pageable, projection);
    }

    /**
     * Searches visible entities using a generated search request and reference DTOs.
     *
     * @param searchRequest generated search request object
     * @param pageable pagination request
     * @return page of matching reference DTOs
     */
    @Transactional(readOnly = true)
    public Page<F> searchRef(Object searchRequest, Pageable pageable) {
        return findAll(effectiveReadSpecification(searchRequest), pageable, refClass());
    }

    /**
     * Builds the effective read specification by combining search criteria with runtime visibility
     * constraints.
     *
     * @param searchRequest generated search request object, may be {@code null}
     * @return merged specification used by read/search flows
     */
    public Specification<T> effectiveReadSpecification(Object searchRequest) {
        return combine(searchSpecification(searchRequest), runtimeReadFilter());
    }

    /**
     * Retrieves visible entities by identifiers.
     *
     * @param ids identifiers to retrieve
     * @return response DTOs for visible matching entities
     */
    @Transactional(readOnly = true)
    public List<R> findByIds(Collection<ID> ids) {
        Specification<T> spec = combine(byIds(ids), runtimeReadFilter());
        return queryExecutor().findAll(spec).stream()
                .map(entity -> customizeMappedResponse(entity, mapper().toResponse(entity)))
                .map(this::afterRead)
                .toList();
    }

    /**
     * Retrieves one visible entity by identifier when present.
     *
     * @param id identifier to retrieve
     * @return optional response DTO for the visible entity
     */
    @Transactional(readOnly = true)
    public Optional<R> findByIdOptional(ID id) {
        Specification<T> spec = combine(byId(id), runtimeReadFilter());
        return queryExecutor()
                .findOne(spec)
                .map(entity -> customizeMappedResponse(entity, mapper().toResponse(entity)))
                .map(this::afterRead);
    }

    /**
     * Retrieves one visible entity by identifier using the supplied projection DTO.
     *
     * @param id identifier to retrieve
     * @param projection projection DTO type
     * @param <P> projection response type
     * @return projected response for the visible entity
     * @throws ResourceNotFoundException when no visible entity exists for the identifier
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <P> P findById(ID id, Class<P> projection) {
        Specification<T> spec = combine(byId(id), runtimeReadFilter());
        Class<P> effectiveProjection = projection != null ? projection : (Class<P>) responseClass();
        if (isDefaultProjection(effectiveProjection)) {
            return mapDefaultProjectionOne(spec, effectiveProjection)
                    .map(this::afterRead)
                    .orElseThrow(() -> notFound(id, "findById"));
        }
        List<P> projected = projectList(spec, effectiveProjection);
        if (projected != null) {
            return projected.stream()
                    .map(this::afterRead)
                    .findFirst()
                    .orElseThrow(() -> notFound(id, "findById"));
        }
        return queryExecutor()
                .findOne(spec, effectiveProjection)
                .map(this::afterRead)
                .orElseThrow(() -> notFound(id, "findById"));
    }

    /**
     * Retrieves one visible entity by identifier using the default response DTO.
     *
     * @param id identifier to retrieve
     * @return response DTO for the visible entity
     * @throws ResourceNotFoundException when no visible entity exists for the identifier
     */
    @Transactional(readOnly = true)
    public R findById(ID id) {
        return findById(id, responseClass());
    }

    /**
     * Retrieves a JPA reference for one visible entity by identifier.
     *
     * @param id identifier to reference
     * @return visible entity reference
     * @throws ResourceNotFoundException when no visible entity exists for the identifier
     */
    @Transactional(readOnly = true)
    public T findReferenceById(ID id) {
        if (!queryExecutor().exists(combine(byId(id), runtimeReadFilter()))) {
            throw notFound(id, "findReferenceById");
        }
        return repository().getReferenceById(id);
    }

    /**
     * Checks whether an entity is visible by identifier.
     *
     * @param id identifier to check
     * @return {@code true} when a visible entity exists
     */
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        return queryExecutor().exists(combine(byId(id), runtimeReadFilter()));
    }

    /**
     * Counts all entities visible under runtime read filters.
     *
     * @return visible entity count
     */
    @Transactional(readOnly = true)
    public long count() {
        return queryExecutor().count(runtimeReadFilter());
    }

    protected T loadEntity(ID id) {
        return queryExecutor()
                .findOne(combine(byId(id), runtimeReadFilter()))
                .orElseThrow(() -> notFound(id, "loadEntity"));
    }

    protected ResourceNotFoundException notFound(ID id) {
        return notFound(id, "read");
    }

    protected ResourceNotFoundException notFound(ID id, String operation) {
        auditDeniedReadIfHidden(id, operation);
        return new ResourceNotFoundException(
                String.format(
                        "%s with ID '%s' could not be found", entityClass().getSimpleName(), id));
    }

    private void auditDeniedReadIfHidden(ID id, String operation) {
        if (id == null) {
            return;
        }
        Specification<T> readFilter = runtimeReadFilter();
        if (readFilter == null) {
            return;
        }
        if (!queryExecutor().exists(byId(id))) {
            return;
        }
        if (queryExecutor().exists(combine(byId(id), readFilter))) {
            return;
        }
        for (ReadDeniedAuditHook hook : collaborators().readDeniedAuditHooks()) {
            hook.onReadDenied(entityClass(), id, operation);
        }
    }

    protected <P> Page<P> projectPage(
            Specification<T> spec, Pageable pageable, Class<P> projection) {
        ProjectionAdapter adapter = collaborators().projectionAdapter();
        if (adapter == null || !collaborators().supportsProjection(projection)) {
            return null;
        }
        return adapter.projectPage(entityClass(), projection, spec, pageable);
    }

    protected <P> List<P> projectList(Specification<T> spec, Class<P> projection) {
        ProjectionAdapter adapter = collaborators().projectionAdapter();
        if (adapter == null || !collaborators().supportsProjection(projection)) {
            return null;
        }
        return adapter.projectList(entityClass(), projection, spec);
    }

    @SuppressWarnings("unchecked")
    protected <P> Page<P> mapDefaultProjectionPage(
            Specification<T> spec, Pageable pageable, Class<P> projection) {
        if (projection.equals(responseClass())) {
            return queryExecutor()
                    .findAll(spec, pageable)
                    .map(
                            entity ->
                                    customizeMappedResponse(
                                            entity, (P) mapper().toResponse(entity)));
        }
        return queryExecutor()
                .findAll(spec, pageable)
                .map(entity -> customizeMappedResponse(entity, (P) mapper().toRef(entity)));
    }

    @SuppressWarnings("unchecked")
    protected <P> Optional<P> mapDefaultProjectionOne(
            Specification<T> spec, Class<P> projection) {
        if (projection.equals(responseClass())) {
            return queryExecutor()
                    .findOne(spec)
                    .map(
                            entity ->
                                    customizeMappedResponse(
                                            entity, (P) mapper().toResponse(entity)));
        }
        return queryExecutor()
                .findOne(spec)
                .map(entity -> customizeMappedResponse(entity, (P) mapper().toRef(entity)));
    }

    protected <P> boolean isDefaultProjection(Class<P> projection) {
        return projection.equals(responseClass()) || projection.equals(refClass());
    }

    protected Specification<T> byId(ID id) {
        return (root, query, cb) -> cb.equal(root.get(resolveIdAttributeName()), id);
    }

    protected Specification<T> byIds(Collection<ID> ids) {
        return (root, query, cb) -> root.get(resolveIdAttributeName()).in(ids);
    }

    protected Specification<T> combine(Specification<T> first, Specification<T> second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.and(second);
    }

    protected abstract JpaRepository<T, ID> repository();

    protected abstract EntityMapper<T, U, R, F, ID> mapper();

    protected abstract Class<T> entityClass();

    protected abstract Class<R> responseClass();

    protected abstract Class<F> refClass();

    protected abstract QueryExecutionStrategy<T> queryExecutor();

    protected abstract ServiceCollaborators<T, U> collaborators();

    protected abstract String resolveIdAttributeName();

    protected abstract Specification<T> runtimeReadFilter();

    protected abstract Specification<T> searchSpecification(Object searchRequest);

    protected abstract <P> P afterRead(P dto);

    protected abstract <P> P customizeMappedResponse(T entity, P dto);
}

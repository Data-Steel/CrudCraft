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

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import nl.datasteel.crudcraft.runtime.Identified;
import nl.datasteel.crudcraft.runtime.InternalApi;
import nl.datasteel.crudcraft.runtime.exception.CrudCraftConfigurationException;
import nl.datasteel.crudcraft.runtime.exception.ForbiddenException;
import nl.datasteel.crudcraft.runtime.exception.MapperException;
import nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapperCustomizer;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
import nl.datasteel.crudcraft.runtime.service.strategy.JpaSpecificationExecutionStrategy;
import nl.datasteel.crudcraft.runtime.service.strategy.QueryExecutionStrategy;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * Generic abstract base for CRUD operations, bulk operations, upsert, patching, projection, and
 * neutral runtime extensions.
 *
 * <p>Threading assumptions: generated services are Spring singleton beans and may be called by
 * multiple request threads concurrently. This base class keeps per-service collaborators immutable
 * after construction, caches the combined runtime-extension snapshot after first resolution, and
 * resolves identifier metadata with volatile publication. Subclasses must keep hook methods
 * stateless or protect any mutable state they introduce.
 *
 * <p>Keyset cursors returned by this service are opaque transport tokens. Clients must store and
 * replay the cursor value exactly as returned, and must not parse, construct, or depend on the
 * token's current encoding.
 *
 * @param <T> entity type
 * @param <U> request DTO type
 * @param <R> full response DTO type
 * @param <F> reference DTO type
 * @param <ID> identifier type
 */
@ThreadSafe
@InternalApi
abstract class CoreCrudOperations<T, U, R, F, ID>
        extends KeysetPaginationService<T, U, R, F, ID>
        implements CrudService<T, U, R, F, ID>, ApplicationContextAware {

    private static final String DEFAULT_ID_ATTRIBUTE = "id";
    protected final JpaRepository<T, ID> repository;
    protected final EntityMapper<T, U, R, F, ID> mapper;
    protected final Class<T> entityClass;
    protected final Class<R> responseClass;
    protected final Class<F> refClass;
    private volatile QueryExecutionStrategy<T> queryExecutor;

    /*
     * Published by Spring through @PersistenceContext before request handling.
     * Spring performs this injection during bean initialization, before
     * generated services are used by request threads. Some unit tests exercise
     * fallback behavior without an EntityManager; in that case ID resolution
     * returns the conventional "id" name without caching it, so later injection
     * can still publish real metamodel metadata.
     */
    @Nullable protected volatile Metamodel metamodel;

    private final ServiceCollaborators<T, U> collaborators = new ServiceCollaborators<>();
    private volatile List<CrudRuntimeExtension<T, U>> allExtensions;
    /*
     * Lazily resolved from the JPA metamodel and safely published through volatile assignment.
     * Double-checked initialization synchronizes on the service instance, so no separate lock
     * object is needed. This value is never cached from the fallback path used when the
     * metamodel is unavailable, and it is not invalidated for runtime entity redefinition. Entity
     * metadata is expected to be stable after the Spring persistence context is initialized.
     */
    private volatile String idAttributeName;

    /**
     * Creates a CRUD service backed by the provided repository and mapper.
     *
     * @param repository JPA repository used for persistence
     * @param mapper mapper between entities and DTOs
     * @param entityClass entity type handled by this service
     * @param responseClass full response DTO type
     * @param refClass reference DTO type
     */
    @SuppressWarnings("unchecked")
    @SuppressFBWarnings(
            value = "CT_CONSTRUCTOR_THROW",
            justification =
                    "Fail-fast validation is intentional for misconfigured repositories; "
                            + "services are Spring-managed and not exposed to "
                            + "attacker-controlled finalizers")
    protected CoreCrudOperations(
            JpaRepository<T, ID> repository,
            EntityMapper<T, U, R, F, ID> mapper,
            Class<T> entityClass,
            Class<R> responseClass,
            Class<F> refClass) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityClass = entityClass;
        this.responseClass = responseClass;
        this.refClass = refClass;
        if (repository == null) {
            this.queryExecutor = null;
            return;
        }
        if (!(repository instanceof JpaSpecificationExecutor<?> specificationRepository)) {
            throw new CrudCraftConfigurationException(
                    "CrudCraft repository must implement JpaSpecificationExecutor.",
                    Map.of(
                            "repository",
                            repository.getClass().getName(),
                            "entity",
                            entityClass.getName(),
                            "required_interface",
                            JpaSpecificationExecutor.class.getName()));
        }
        this.queryExecutor =
                new JpaSpecificationExecutionStrategy<>(
                        (JpaSpecificationExecutor<T>) specificationRepository);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        collaborators.setApplicationContext(applicationContext);
        allExtensions = null;
    }

    /**
     * Supplies the JPA entity manager used to validate sort paths and resolve identifier metadata.
     *
     * @param entityManager JPA entity manager for the current persistence context
     */
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        Metamodel nextMetamodel = entityManager.getMetamodel();
        if (metamodel != null && metamodel != nextMetamodel) {
            throw new IllegalStateException(
                    "EntityManager cannot be reset after CrudCraft service initialization");
        }
        metamodel = nextMetamodel;
        idAttributeName = null;
    }

    @Override
    protected JpaRepository<T, ID> repository() {
        return repository;
    }

    @Override
    protected EntityMapper<T, U, R, F, ID> mapper() {
        return mapper;
    }

    @Override
    protected Class<T> entityClass() {
        return entityClass;
    }

    @Override
    protected Class<R> responseClass() {
        return responseClass;
    }

    @Override
    protected Class<F> refClass() {
        return refClass;
    }

    @Override
    protected QueryExecutionStrategy<T> queryExecutor() {
        return queryExecutor;
    }

    void setQueryExecutorForTests(QueryExecutionStrategy<T> queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    @Override
    protected ServiceCollaborators<T, U> collaborators() {
        return collaborators;
    }

    @Override
    protected @Nullable Metamodel metamodel() {
        return metamodel;
    }

    /**
     * Creates an entity from a request DTO.
     *
     * @param request request DTO to persist
     * @return created response DTO after runtime read filters are applied
     */
    @Override
    @Transactional
    public R create(U request) {
        U prepared = beforeCreate(request);
        T entity =
                afterFromRequest(
                        mapWithContext(
                                "create.fromRequest",
                                prepared,
                                () -> mapper.fromRequest(prepared)),
                        prepared);
        beforeSave(entity);
        preSave(entity, prepared);
        T saved = repository.save(entity);
        postSave(saved);
        return afterRead(
                afterToResponse(
                        saved,
                        mapWithContext(
                                "create.toResponse", prepared, () -> mapper.toResponse(saved))));
    }

    /**
     * Replaces an existing visible entity from a request DTO.
     *
     * @param id identifier to update
     * @param request replacement request DTO
     * @return updated response DTO after runtime read filters are applied
     * @throws ResourceNotFoundException when no visible entity exists for the id
     */
    @Override
    @Transactional
    public R update(ID id, U request) {
        T entity = loadEntity(id);
        T mappingTarget = entity;
        U prepared = beforeUpdate(request, entity);
        mapWithContext("update.mapper", prepared, () -> {
            mapper.update(mappingTarget, prepared);
            return null;
        });
        entity = afterUpdate(mappingTarget, prepared);
        beforeSave(entity);
        preSave(entity, prepared);
        T saved = repository.save(entity);
        postSave(saved);
        return afterRead(
                afterToResponse(
                        saved,
                        mapWithContext(
                                "update.toResponse", prepared, () -> mapper.toResponse(saved))));
    }

    /**
     * Partially updates an existing visible entity from a request DTO.
     *
     * @param id identifier to patch
     * @param request patch request DTO
     * @return patched response DTO after runtime read filters are applied
     * @throws ResourceNotFoundException when no visible entity exists for the id
     */
    @Override
    @Transactional
    public R patch(ID id, U request) {
        T entity = loadEntity(id);
        T mappingTarget = entity;
        U prepared = beforeUpdate(request, entity);
        mapWithContext("patch.mapper", prepared, () -> {
            mapper.patch(mappingTarget, prepared);
            return null;
        });
        entity = afterPatch(mappingTarget, prepared);
        beforeSave(entity);
        preSave(entity, prepared);
        T saved = repository.save(entity);
        postSave(saved);
        return afterRead(
                afterToResponse(
                        saved,
                        mapWithContext(
                                "patch.toResponse", prepared, () -> mapper.toResponse(saved))));
    }

    /**
     * Creates or updates an entity depending on whether the request carries a visible id.
     *
     * @param request request DTO to create or update
     * @return created or updated response DTO
     */
    @Override
    @Transactional
    public R upsert(U request) {
        ID id = idFromRequestOrNull(request);
        if (id == null) {
            return create(request);
        }
        Specification<T> byId = byId(id);
        if (queryExecutor.exists(combine(byId, runtimeReadFilter()))) {
            return update(id, request);
        }
        if (queryExecutor.exists(byId)) {
            throw new ForbiddenException(
                    entityClass.getSimpleName()
                            + " with ID '"
                            + id
                            + "' exists but is not visible for upsert.");
        }
        return create(request);
    }

    /**
     * Creates all supplied request DTOs in iteration order.
     *
     * @param requests request DTOs to create
     * @return created response DTOs
     */
    @Override
    @Transactional
    public List<R> createAll(Collection<U> requests) {
        return requests.stream().map(this::create).toList();
    }

    /**
     * Creates all supplied request DTOs and captures per-item failures.
     *
     * <p>This result-oriented variant is intentionally not wrapped in one
     * outer transaction. Each item is executed through the normal single-item
     * operation path, so clients can retry only entries listed in
     * {@link BulkResult#failed()} using the reported zero-based input indexes
     * after correcting the failure cause.
     *
     * @param requests request DTOs to create
     * @return succeeded responses and failed item indexes
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BulkResult<R> createAllResult(Collection<U> requests) {
        return bulkResult(requests, this::create);
    }

    /**
     * Creates or updates all supplied request DTOs in iteration order.
     *
     * @param requests request DTOs to create or update
     * @return created or updated response DTOs
     */
    @Override
    @Transactional
    public List<R> upsertAll(Collection<U> requests) {
        return requests.stream().map(this::upsert).toList();
    }

    /**
     * Creates or updates all supplied request DTOs and captures per-item failures.
     *
     * <p>This result-oriented variant is intentionally not wrapped in one
     * outer transaction. Each item is executed through the normal single-item
     * operation path, so clients can retry only failed indexes.
     *
     * @param requests request DTOs to create or update
     * @return succeeded responses and failed item indexes
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BulkResult<R> upsertAllResult(Collection<U> requests) {
        return bulkResult(requests, this::upsert);
    }

    /**
     * Replaces all supplied visible entities in list order.
     *
     * @param requests identifiers paired with replacement request DTOs
     * @return updated response DTOs
     */
    @Override
    @Transactional
    public List<R> updateAll(List<Identified<ID, U>> requests) {
        return requests.stream()
                .map(request -> update(request.getId(), request.getData()))
                .toList();
    }

    /**
     * Replaces all supplied visible entities and captures per-item failures.
     *
     * <p>This result-oriented variant is intentionally not wrapped in one
     * outer transaction. Each item is executed through the normal single-item
     * operation path, so clients can retry only failed indexes.
     *
     * @param requests identifiers paired with replacement request DTOs
     * @return succeeded responses and failed item indexes
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BulkResult<R> updateAllResult(List<Identified<ID, U>> requests) {
        return bulkResult(requests, request -> update(request.getId(), request.getData()));
    }

    /**
     * Partially updates all supplied visible entities in list order.
     *
     * @param requests identifiers paired with patch request DTOs
     * @return patched response DTOs
     */
    @Override
    @Transactional
    public List<R> patchAll(List<Identified<ID, U>> requests) {
        return requests.stream().map(request -> patch(request.getId(), request.getData())).toList();
    }

    /**
     * Partially updates all supplied visible entities and captures per-item failures.
     *
     * <p>This result-oriented variant is intentionally not wrapped in one
     * outer transaction. Each item is executed through the normal single-item
     * operation path, so clients can retry only failed indexes.
     *
     * @param requests identifiers paired with patch request DTOs
     * @return succeeded responses and failed item indexes
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BulkResult<R> patchAllResult(List<Identified<ID, U>> requests) {
        return bulkResult(requests, request -> patch(request.getId(), request.getData()));
    }

    /**
     * Deletes a visible entity by identifier.
     *
     * @param id identifier to delete
     * @throws ResourceNotFoundException when no visible entity exists for the id
     */
    @Override
    @Transactional
    public void delete(ID id) {
        T entity = loadEntity(id);
        beforeDelete(entity);
        preDelete(entity);
        repository.delete(entity);
        postDelete(entity);
    }

    /**
     * Deletes all visible entities for the supplied identifiers.
     *
     * @param ids identifiers to delete
     */
    @Override
    @Transactional
    public void deleteAllByIds(Collection<ID> ids) {
        ids.forEach(this::delete);
    }

    /**
     * Deletes all supplied identifiers and captures per-item failures.
     *
     * <p>This result-oriented variant is intentionally not wrapped in one
     * outer transaction. Each item is executed through the normal single-item
     * operation path, so clients can retry only failed indexes.
     *
     * @param ids identifiers to delete
     * @return deleted identifiers and failed item indexes
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BulkResult<ID> deleteAllByIdsResult(Collection<ID> ids) {
        return bulkResult(
                ids,
                id -> {
                    delete(id);
                    return id;
                });
    }

    protected List<CrudRuntimeExtension<T, U>> runtimeExtensions() {
        return List.of();
    }

    protected void preSave(T entity, U request) {
        consume(entity, request);
    }

    protected void postSave(T entity) {
        consume(entity);
    }

    protected void preDelete(T entity) {
        consume(entity);
    }

    protected void postDelete(T entity) {
        consume(entity);
    }

    /**
     * Supplies optional mapper customization hooks for the generated service.
     *
     * @return customizer used after generated mapper calls
     */
    protected EntityMapperCustomizer<T, U, R, F> mapperCustomizer() {
        return EntityMapperCustomizer.noOp();
    }

    private ID idFromRequestOrNull(U request) {
        try {
            return mapper.getIdFromRequest(request);
        } catch (MapperException ex) {
            if (hasCause(ex, NotReadablePropertyException.class)) {
                return null;
            }
            throw ex;
        }
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private <V> V mapWithContext(String operation, U request, MapperCall<V> mapperCall) {
        try {
            return mapperCall.execute();
        } catch (MapperException ex) {
            throw new MapperException(
                    "Mapper operation failed",
                    Map.of(
                            "operation",
                            operation,
                            "entity",
                            entityClass.getName(),
                            "request",
                            request == null ? "null" : request.getClass().getName(),
                            "exceptionType",
                            ex.getClass().getName()),
                    ex);
        } catch (RuntimeException ex) {
            throw new MapperException(
                    "Mapper operation failed",
                    Map.of(
                            "operation",
                            operation,
                            "entity",
                            entityClass.getName(),
                            "request",
                            request == null ? "null" : request.getClass().getName(),
                            "exceptionType",
                            ex.getClass().getName()),
                    ex);
        }
    }

    @FunctionalInterface
    private interface MapperCall<V> {
        V execute();
    }

    @Override
    protected Specification<T> runtimeReadFilter() {
        Specification<T> spec = null;
        for (CrudRuntimeExtension<T, U> extension : allExtensions()) {
            spec = combine(spec, extension.readFilter(entityClass));
        }
        return spec;
    }

    private <I, O> BulkResult<O> bulkResult(Collection<I> inputs, Function<I, O> operation) {
        if (inputs == null || inputs.isEmpty()) {
            return BulkResult.empty();
        }
        List<O> succeeded = new ArrayList<>();
        List<BulkResult.Failure> failed = new ArrayList<>();
        int index = 0;
        for (I input : inputs) {
            try {
                succeeded.add(executeBulkItem(operation, input));
            } catch (RuntimeException ex) {
                failed.add(new BulkResult.Failure(index, bulkFailureMessage(ex)));
            }
            index++;
        }
        return new BulkResult<>(succeeded, failed);
    }

    private <I, O> O executeBulkItem(Function<I, O> operation, I input) {
        PlatformTransactionManager transactionManager = collaborators().transactionManager();
        if (transactionManager == null) {
            return operation.apply(input);
        }
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template.execute(status -> operation.apply(input));
    }

    private String bulkFailureMessage(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
    }

    @SuppressFBWarnings(
            value = "UC_USELESS_VOID_METHOD",
            justification =
                    "This helper intentionally consumes default-hook parameters in no-op base"
                            + " implementations to keep extension signatures stable and static"
                            + " analysis-friendly.")
    private static void consume(Object... values) {
        for (Object value : values) {
            if (value == null) {
                return;
            }
        }
    }

    private U beforeCreate(U request) {
        U current = request;
        for (CrudRuntimeExtension<T, U> extension : allExtensions()) {
            current = extension.beforeCreate(current);
        }
        return current;
    }

    private U beforeUpdate(U request, T existing) {
        U current = request;
        for (CrudRuntimeExtension<T, U> extension : allExtensions()) {
            current = extension.beforeUpdate(current, existing);
        }
        return current;
    }

    private void beforeSave(T entity) {
        for (CrudRuntimeExtension<T, U> extension : allExtensions()) {
            extension.beforeSave(entity);
        }
    }

    private void beforeDelete(T entity) {
        for (CrudRuntimeExtension<T, U> extension : allExtensions()) {
            extension.beforeDelete(entity);
        }
    }

    @Override
    protected <P> P afterRead(P dto) {
        P current = dto;
        for (CrudRuntimeExtension<T, U> extension : allExtensions()) {
            current = extension.afterRead(current);
        }
        return current;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <P> P customizeMappedResponse(T entity, P dto) {
        if (dto == null) {
            return null;
        }
        if (responseClass.isInstance(dto)) {
            return (P) afterToResponse(entity, responseClass.cast(dto));
        }
        if (refClass.isInstance(dto)) {
            return (P) afterToRef(entity, refClass.cast(dto));
        }
        return dto;
    }

    private EntityMapperCustomizer<T, U, R, F> requiredMapperCustomizer() {
        return Objects.requireNonNull(mapperCustomizer(), "mapperCustomizer");
    }

    private T afterFromRequest(T entity, U request) {
        return Objects.requireNonNull(
                requiredMapperCustomizer().afterFromRequest(entity, request),
                "mapperCustomizer.afterFromRequest");
    }

    private T afterUpdate(T entity, U request) {
        return Objects.requireNonNull(
                requiredMapperCustomizer().afterUpdate(entity, request),
                "mapperCustomizer.afterUpdate");
    }

    private T afterPatch(T entity, U request) {
        return Objects.requireNonNull(
                requiredMapperCustomizer().afterPatch(entity, request),
                "mapperCustomizer.afterPatch");
    }

    private R afterToResponse(T entity, R response) {
        return Objects.requireNonNull(
                requiredMapperCustomizer().afterToResponse(response, entity),
                "mapperCustomizer.afterToResponse");
    }

    private F afterToRef(T entity, F ref) {
        return Objects.requireNonNull(
                requiredMapperCustomizer().afterToRef(ref, entity),
                "mapperCustomizer.afterToRef");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Specification<T> searchSpecification(Object searchRequest) {
        if (searchRequest == null) {
            return null;
        }
        if (searchRequest instanceof SpecificationProvider<?> provider) {
            return (Specification<T>) provider.toSpecification();
        }
        throw new IllegalArgumentException(
                "Unsupported search request type: "
                        + searchRequest.getClass().getName()
                        + ". Request must implement "
                        + SpecificationProvider.class.getName()
                        + ".");
    }

    private List<CrudRuntimeExtension<T, U>> allExtensions() {
        List<CrudRuntimeExtension<T, U>> cached = allExtensions;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            cached = allExtensions;
            if (cached == null) {
                List<CrudRuntimeExtension<T, U>> resolved = new ArrayList<>();
                resolved.addAll(collaborators.contextExtensions());
                resolved.addAll(runtimeExtensions());
                cached = List.copyOf(resolved);
                allExtensions = cached;
            }
            return cached;
        }
    }

    /**
     * Resolves the JPA identifier attribute name for keyset sorting.
     *
     * <p>The metamodel-derived value is cached after first successful resolution. Runtime entity
     * redefinition is not supported; generated services assume entity metadata remains stable after
     * the persistence context has been initialized.
     *
     * @return identifier attribute name
     */
    @Override
    protected String resolveIdAttributeName() {
        if (metamodel == null) {
            return DEFAULT_ID_ATTRIBUTE;
        }
        String cached = idAttributeName;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            String initialized = idAttributeName;
            if (initialized == null) {
                initialized = resolveIdAttributeNameInternal();
                idAttributeName = initialized;
            }
            return initialized;
        }
    }

    private String resolveIdAttributeNameInternal() {
        if (metamodel != null) {
            EntityType<T> type = metamodel.entity(entityClass);
            SingularAttribute<? super T, ?> id = type.getId(type.getIdType().getJavaType());
            return id.getName();
        }
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                return field.getName();
            }
        }
        return DEFAULT_ID_ATTRIBUTE;
    }
}

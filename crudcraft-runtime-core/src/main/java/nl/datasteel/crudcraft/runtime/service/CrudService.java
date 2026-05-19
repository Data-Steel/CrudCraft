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
import nl.datasteel.crudcraft.runtime.Identified;


/**
 * Defines the basic CRUD API surface, including pagination, partial updates (patch), upsert, and
 * bulk operations.
 *
 * @param <T> the JPA entity type
 * @param <U> the request/upsert/patch DTO type
 * @param <R> the full response DTO type
 * @param <F> the reference DTO type
 * @param <ID> the identifier type (e.g. UUID, Long)
 */
@SuppressWarnings("Java:S119")
public interface CrudService<T, U, R, F, ID> extends CrudQueryOperations<T, R, F> {

    /**
     * Retrieves entities matching the supplied identifiers.
     *
     * <p>This is a read-only operation. Missing identifiers are ignored by the underlying
     * repository, so the returned list may be smaller than the input collection and ordering is not
     * guaranteed unless the concrete repository enforces it.
     *
     * @param ids identifiers to fetch; must not be {@code null}
     * @return matching entities as full response DTOs
     */
    List<R> findByIds(Collection<ID> ids);

    /**
     * Retrieves a single entity by identifier without throwing when it is absent.
     *
     * <p>This is a read-only operation and returns the default response DTO type.
     *
     * @param id identifier to fetch; must not be {@code null}
     * @return optional response DTO, empty when the entity does not exist
     */
    Optional<R> findByIdOptional(ID id);

    /**
     * Retrieves a single entity by identifier.
     *
     * <p>This is a read-only operation and returns the default response DTO type.
     *
     * @param id identifier to fetch; must not be {@code null}
     * @return found entity as response DTO
     * @throws nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException when no entity
     *     exists for the identifier
     */
    R findById(ID id);

    /**
     * Retrieves a single entity by identifier and maps it to a projection type.
     *
     * <p>This is a read-only operation. When {@code projection} is {@code null}, the service maps
     * to the configured default response DTO type {@code R}.
     *
     * @param id identifier to fetch; must not be {@code null}
     * @param projection projection class to use, or {@code null} for the default response DTO
     * @param <P> the projection type
     * @return found entity as projection
     * @throws nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException when no entity
     *     exists for the identifier
     */
    <P> P findById(ID id, Class<P> projection);

    /**
     * Returns a JPA reference for the entity.
     *
     * <p>The persistence provider may return a lazy proxy without immediately hitting the database.
     * Accessing the proxy can still fail later if the entity does not exist.
     *
     * @param id identifier to reference; must not be {@code null}
     * @return JPA reference entity
     */
    T findReferenceById(ID id);

    /**
     * Creates a new entity from a request DTO.
     *
     * <p>This is a read-write operation. Implementations usually map the request, persist the
     * entity, repair relationships, and map the saved entity back to the response DTO.
     *
     * @param request create request DTO; must not be {@code null}
     * @return created entity as response DTO
     * @throws jakarta.validation.ValidationException when validation performed by the concrete
     *     service or persistence layer fails
     */
    R create(U request);

    /**
     * Replaces mutable fields on an existing entity from a request DTO.
     *
     * <p>This is a read-write operation. Fields present in the request follow full update
     * semantics rather than patch semantics.
     *
     * @param id identifier of the entity to update; must not be {@code null}
     * @param request update request DTO; must not be {@code null}
     * @return updated entity as response DTO
     * @throws nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException when no entity
     *     exists for the identifier
     */
    R update(ID id, U request);

    /**
     * Partially updates an existing entity.
     *
     * <p>This is a read-write operation. Null or absent properties are interpreted according to the
     * mapper's patch configuration, typically "ignore null values".
     *
     * @param id identifier of the entity to patch; must not be {@code null}
     * @param request patch request DTO; must not be {@code null}
     * @return patched entity as response DTO
     * @throws nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException when no entity
     *     exists for the identifier
     */
    R patch(ID id, U request);

    /**
     * Creates or updates an entity based on the identifier carried by the DTO.
     *
     * <p>This is a read-write operation. Concrete mappers determine how the identifier is read from
     * the request; absent identifiers normally result in create semantics.
     *
     * @param request upsert request DTO; must not be {@code null}
     * @return upserted entity as response DTO
     */
    R upsert(U request);

    /**
     * Creates multiple entities from request DTOs.
     *
     * <p>This is a read-write operation. Implementations should treat the collection as one service
     * operation; transaction boundaries are determined by the concrete implementation.
     *
     * @param requests create request DTOs; must not be {@code null}
     * @return created entities as response DTOs
     */
    List<R> createAll(Collection<U> requests);

    /**
     * Creates multiple entities and returns per-item success/failure details.
     *
     * <p>The default implementation preserves compatibility for custom {@code CrudService}
     * implementations by delegating to {@link #createAll(Collection)}. Generated services override
     * this with isolated item transactions so partial failures can be reported without losing
     * successful items.
     *
     * @param requests create request DTOs; must not be {@code null}
     * @return bulk result containing created entities and failed input indexes
     */
    default BulkResult<R> createAllResult(Collection<U> requests) {
        return new BulkResult<>(createAll(requests), List.of());
    }

    /**
     * Creates or updates multiple entities from request DTOs.
     *
     * <p>This is a read-write operation. Each element follows the same semantics as {@link
     * #upsert(Object)}.
     *
     * @param requests upsert request DTOs; must not be {@code null}
     * @return upserted entities as response DTOs
     */
    List<R> upsertAll(Collection<U> requests);

    /**
     * Creates or updates multiple entities and returns per-item success/failure details.
     *
     * @param requests upsert request DTOs; must not be {@code null}
     * @return bulk result containing upserted entities and failed input indexes
     */
    default BulkResult<R> upsertAllResult(Collection<U> requests) {
        return new BulkResult<>(upsertAll(requests), List.of());
    }

    /**
     * Updates multiple existing entities.
     *
     * <p>This is a read-write operation. Each element must carry the target identifier and update
     * DTO.
     *
     * @param requests identified update request DTOs; must not be {@code null}
     * @return updated entities as response DTOs
     * @throws nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException when any target
     *     entity is absent
     */
    List<R> updateAll(List<Identified<ID, U>> requests);

    /**
     * Updates multiple entities and returns per-item success/failure details.
     *
     * @param requests identified update request DTOs; must not be {@code null}
     * @return bulk result containing updated entities and failed input indexes
     */
    default BulkResult<R> updateAllResult(List<Identified<ID, U>> requests) {
        return new BulkResult<>(updateAll(requests), List.of());
    }

    /**
     * Partially updates multiple existing entities.
     *
     * <p>This is a read-write operation. Each element follows the same patch semantics as {@link
     * #patch(Object, Object)}.
     *
     * @param requests identified patch request DTOs; must not be {@code null}
     * @return patched entities as response DTOs
     * @throws nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException when any target
     *     entity is absent
     */
    List<R> patchAll(List<Identified<ID, U>> requests);

    /**
     * Partially updates multiple entities and returns per-item success/failure details.
     *
     * @param requests identified patch request DTOs; must not be {@code null}
     * @return bulk result containing patched entities and failed input indexes
     */
    default BulkResult<R> patchAllResult(List<Identified<ID, U>> requests) {
        return new BulkResult<>(patchAll(requests), List.of());
    }

    /**
     * Deletes an entity by identifier.
     *
     * <p>This is a read-write operation.
     *
     * @param id identifier to delete; must not be {@code null}
     * @throws nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException when no entity
     *     exists for the identifier
     */
    void delete(ID id);

    /**
     * Deletes multiple entities by identifier.
     *
     * <p>This is a read-write operation. Missing identifiers are handled by the concrete
     * implementation and may either be ignored or surface as a not-found error.
     *
     * @param ids identifiers to delete; must not be {@code null}
     */
    void deleteAllByIds(Collection<ID> ids);

    /**
     * Deletes multiple entities and returns per-item success/failure details.
     *
     * @param ids identifiers to delete; must not be {@code null}
     * @return bulk result containing deleted identifiers and failed input indexes
     */
    default BulkResult<ID> deleteAllByIdsResult(Collection<ID> ids) {
        deleteAllByIds(ids);
        return new BulkResult<>(ids == null ? List.of() : List.copyOf(ids), List.of());
    }

    /**
     * Checks whether an entity exists for the identifier.
     *
     * <p>This is a read-only operation.
     *
     * @param id identifier to check; must not be {@code null}
     * @return {@code true} when entity exists
     */
    boolean existsById(ID id);

    /**
     * Counts all entities visible to the service.
     *
     * <p>This is a read-only operation. Concrete services may apply row-security or tenant filters
     * before counting.
     *
     * @return entity count
     */
    long count();
}

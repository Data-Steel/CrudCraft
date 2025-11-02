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
package nl.datasteel.crudcraft.runtime.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import nl.datasteel.crudcraft.runtime.Identified;
import nl.datasteel.crudcraft.runtime.search.SearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Defines the basic CRUD API surface, including pagination, search,
 * partial updates (patch), upsert, and bulk operations.
 *
 * @param <T>  the JPA entity type
 * @param <U>  the request/upsert/patch DTO type
 * @param <R>  the full response DTO type
 * @param <F>  the reference DTO type
 * @param <ID> the identifier type (e.g. UUID, Long)
 */
public interface CrudService<T, U, R, F, ID> {

    /**
     * Retrieve a page of entities, optionally filtered by a search query.
     */
    Page<R> findAll(Pageable pageable, String searchQuery);

    /**
     * Execute a typed search using a generated search request object.
     */
    Page<R> search(SearchRequest<T> request, Pageable pageable);

    /**
     * Execute a typed search returning reference DTOs.
     */
    Page<F> searchRef(SearchRequest<T> request, Pageable pageable);

    /**
     * Retrieve entities by a collection of IDs.
     */
    List<R> findByIds(Collection<ID> ids);

    /**
     * Find by ID, returning an Optional.
     */
    Optional<R> findByIdOptional(ID id);

    /**
     * Find by ID or throw ResourceNotFoundException.
     */
    R findById(ID id);

    /**
     * Get a reference proxy to the entity (no immediate DB hit).
     */
    T findReferenceById(ID id);

    /**
     * Create a new entity from the DTO.
     */
    R create(U request);

    /**
     * Update an existing entity by ID from the DTO.
     */
    R update(ID id, U request);

    /**
     * Partially update an existing entity (patch semantics).
     */
    R patch(ID id, U request);

    /**
     * Create or update (upsert) based on presence/existence of ID in DTO.
     */
    R upsert(U request);

    /**
     * Bulk create from a collection of DTOs.
     */
    List<R> createAll(Collection<U> requests);

    /**
     * Bulk upsert from a collection of DTOs.
     */
    List<R> upsertAll(Collection<U> requests);

    /**
     * Bulk update from a collection of DTOs.
     */
    List<R> updateAll(List<Identified<ID, U>> requests);

    /**
     * Bulk patch from a collection of DTOs.
     */
    List<R> patchAll(List<Identified<ID, U>> requests);

    /**
     * Delete an entity by ID.
     */
    void delete(ID id);

    /**
     * Bulk delete entities by their IDs.
     */
    void deleteAllByIds(Collection<ID> ids);

    /**
     * Check existence by ID.
     */
    boolean existsById(ID id);

    /**
     * Count total number of entities.
     */
    long count();
}

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

// CHECKSTYLE.SUPPRESS: OverloadMethodsDeclarationOrder for +1000 lines

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;


/**
 * Neutral query primitives exposed by the core runtime for optional modules.
 *
 * @param <T> entity type
 * @param <R> full response DTO type
 * @param <F> reference DTO type
 */
public interface CrudQueryOperations<T, R, F> {

    /**
     * Lists full response DTOs.
     *
     * @param pageable requested page and sort
     * @return paged full response DTOs
     */
    Page<R> findAll(Pageable pageable);

    /**
     * Lists reference DTOs.
     *
     * @param pageable requested page and sort
     * @return paged reference DTOs
     */
    Page<F> findAllRef(Pageable pageable);

    /**
     * Lists projected DTOs.
     *
     * @param pageable requested page and sort
     * @param projection projection type
     * @param <P> projection DTO type
     * @return paged projected DTOs
     */
    <P> Page<P> findAll(Pageable pageable, Class<P> projection);

    /**
     * Executes a projected query with an optional specification.
     *
     * @param specification optional filter specification
     * @param pageable requested page and sort
     * @param projection projection type
     * @param <P> projection DTO type
     * @return paged projected query results
     */
    <P> Page<P> findAll(Specification<T> specification, Pageable pageable, Class<P> projection);

    /**
     * Builds the effective read specification used by this service for the supplied search object.
     *
     * <p>The returned specification combines caller search criteria with runtime read filters (for
     * example row-security or tenant constraints).
     *
     * @param searchRequest generated or custom search request object
     * @return effective read specification (nullable when no filters apply)
     */
    Specification<T> effectiveReadSpecification(Object searchRequest);

    /**
     * Executes keyset paging with an optional specification.
     *
     * @param specification optional filter specification
     * @param limit max rows to return
     * @param cursor cursor token from previous page
     * @param sort requested ordering
     * @param projection projection type
     * @param <P> projection DTO type
     * @return keyset page result
     */
    <P> KeysetPage<P> findAllKeyset(
            Specification<T> specification,
            int limit,
            String cursor,
            Sort sort,
            Class<P> projection);
}

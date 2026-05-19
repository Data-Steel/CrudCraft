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

package nl.datasteel.crudcraft.annotations.security;

/**
 * Row-level security contract for generated CRUD services.
 *
 * <p>Row security complements field security. Field security controls DTO attributes after a row is
 * found; row security controls which entity rows can be read or written at all. Generated services
 * combine {@link #rowFilter()} with caller-supplied query filters for read operations, single-row
 * lookups, counts, exports, search, and projections. Write operations call {@link #apply(Object)}
 * before persistence so implementations can stamp ownership fields or reject mismatched rows.
 *
 * <p>Implementations are regular runtime collaborators and should be thread-safe. In Spring
 * applications they are usually singleton beans, so keep mutable request state out of the handler
 * itself. Read the current user, tenant, or claims from thread-bound security context accessors or
 * other request-aware collaborators.
 *
 * <p>Null handling is intentionally strict for writes: framework calls pass a non-null entity to
 * {@link #apply(Object)}. Implementations may throw {@link NullPointerException} for a null
 * entity. For reads, return a non-null {@link RowPredicate}. To deny all rows, return a predicate
 * that produces {@code cb.disjunction()}; to allow all rows, return one that produces
 * {@code cb.conjunction()}.
 *
 * @param <T> entity type
 */
@FunctionalInterface
public interface RowSecurityHandler<T> {
    /**
     * Returns a row predicate that restricts which rows are visible for the current user.
     *
     * <p>The returned predicate is evaluated during repository queries and must not mutate entities
     * or depend on per-invocation mutable state stored in the handler. Missing principals or claims
     * should fail closed unless the application deliberately allows anonymous row access.
     *
     * @return a row predicate restricting which rows are visible for the current user
     */
    RowPredicate<T> rowFilter();

    /**
     * Apply row-security constraints to the given entity prior to persistence. Implementations may
     * mutate unset scope fields, for example by writing an owner or tenant id, or throw a runtime
     * access-denied exception when the current principal is not permitted to operate on the row.
     *
     * <p>The default implementation is a no-op for handlers that only need read filtering. Override
     * this method when create, update, patch, upsert, or delete operations must be scope checked.
     *
     * @param entity the entity to check or mutate
     */
    default void apply(T entity) {
        // no-op by default
    }
}

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

package nl.datasteel.crudcraft.runtime.projection.api;

/**
 * Executes projection queries and returns DTO results.
 *
 * <p>Implementations apply filtering, sorting, paging, field-security checks, and projection
 * metadata in one read-only operation. Result lists are returned through {@link ProjectionResult};
 * callers should treat them as snapshots and avoid mutating DTOs when they are records or generated
 * immutable types.
 */
public interface ProjectionExecutor {
    /**
     * Executes a projection for the given entity type and DTO type.
     *
     * <p>The query may include filters, sort orders, pageable information, and requested field
     * paths. Missing projection metadata is implementation-specific; the standard JPA
     * implementation falls back to regular DTO construction where possible and reports unsupported
     * paths as projection errors.
     *
     * @param entityType the entity class
     * @param dtoType the DTO class to project to
     * @param query the projection query definition
     * @return result containing the projected DTOs
     * @param <T> entity type
     * @param <D> dto type
     * @throws RuntimeException when the query or projection metadata cannot be resolved by the
     *     implementation
     */
    <T, D> ProjectionResult<D> project(
            Class<T> entityType, Class<D> dtoType, ProjectionQuery<T> query);
}

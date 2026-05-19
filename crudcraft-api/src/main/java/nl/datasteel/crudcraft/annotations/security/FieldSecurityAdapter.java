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
 * Adapter for field-security decision logic used by runtime integrations.
 *
 * <p>Implementations are expected to be stateless and thread-safe because a single Spring bean is
 * shared across requests. Request-specific security state must be read from the active security
 * context or from a correctly scoped collaborator, never from mutable instance fields. If an
 * adapter uses caches, those caches must be concurrency-safe and bounded by application policy.
 *
 * <p>{@link #filterRead(Object)} runs independently for each DTO that leaves the generated service
 * chain. The returned object is the one sent downstream; adapters may return the same instance only
 * when no denied field is exposed. For concurrent requests, one caller's read decision must not
 * influence another caller's DTO filtering. {@link #filterWrite(Object, Object)} follows the same
 * singleton-threading model for create, replace, and patch requests.
 *
 * <p>Auto-configuration registers a default no-op adapter only when no custom bean is present;
 * applications that need custom behaviour should provide one replacement bean.
 *
 * <p>A custom adapter is part of the application security boundary. CrudCraft can optionally assert
 * read filtering in development through {@code crudcraft.security.field.assert-filtered}, but
 * production correctness depends on the adapter returning DTOs that honor {@link #canReadField}
 * and write policies for every secured field.
 */
public interface FieldSecurityAdapter {

    /** No-op implementation that leaves all field operations unchanged. */
    FieldSecurityAdapter NOOP = new FieldSecurityAdapter() {};

    /**
     * Filters a DTO before it is returned to the caller.
     *
     * <p>Implementations must remove, mask, or replace every field for which {@link
     * #canReadField(Class, String)} returns {@code false}. Returning a defensive copy or immutable
     * DTO is preferred when redaction changes any value.
     *
     * @param dto dto to filter
     * @param <T> dto type
     * @return filtered dto
     */
    default <T> T filterRead(T dto) {
        return dto;
    }

    /**
     * Filters a write request before it is applied.
     *
     * <p>The {@code existing} entity is {@code null} for creates and populated for update-style
     * operations when available. Implementations must not mutate shared security state while
     * filtering one caller's request.
     *
     * @param request request dto
     * @param existing existing entity, if available
     * @param <T> request type
     * @return filtered request dto
     */
    default <T> T filterWrite(T request, Object existing) {
        return request;
    }

    /**
     * Determines whether a field may be exposed in a read response.
     *
     * @param dtoType dto class
     * @param fieldName field name
     * @return {@code true} when field can be read
     */
    default boolean canReadField(Class<?> dtoType, String fieldName) {
        return true;
    }
}

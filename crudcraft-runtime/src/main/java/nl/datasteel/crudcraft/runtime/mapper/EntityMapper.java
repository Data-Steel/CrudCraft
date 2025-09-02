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

package nl.datasteel.crudcraft.runtime.mapper;

/**
 * Maps between JPA entities and Data Transfer Objects (DTOs).
 * This interface defines methods for converting between:
 * <ul>
 *   <li>T – the JPA entity type</li>
 *   <li>U – the create/update/patch/upsert request DTO type</li>
 *   <li>R – the full response DTO type</li>
 *   <li>F – the reference DTO type</li>
 *   <li>ID – the identifier type</li>
 * </ul>
 *
 * <p>Implementations must handle deep copies of properties and ID extraction.
 * They may also consult {@code @FieldSecurity} annotations and use
 * {@link nl.datasteel.crudcraft.runtime.security.FieldSecurityUtil}
 * to omit or redact fields based on the current user's roles.</p>
 *
 * @param <T>  entity type
 * @param <U>  request/upsert/patch DTO type
 * @param <R>  full response DTO type
 * @param <F>  reference DTO type
 * @param <ID> identifier type
 */
public interface EntityMapper<T, U, R, F, ID> {

    /**
     * Instantiate and populate a new entity from the request DTO.
     */
    T fromRequest(U request);

    /**
     * Copy all properties from the DTO into an existing entity.
     *
     * @return the updated entity
     */
    T update(T entity, U request);

    /**
     * Copy only non-null properties from the DTO into the entity.
     *
     * @return the patched entity
     */
    T patch(T entity, U request);

    /**
     * Instantiate and populate a full response DTO from the entity.
     */
    R toResponse(T entity);

    /**
     * Instantiate and populate a reference DTO from the entity.
     */
    F toRef(T entity);

    /**
     * Extract the identifier value from the DTO (for upsert logic).
     */
    ID getIdFromRequest(U request);
}

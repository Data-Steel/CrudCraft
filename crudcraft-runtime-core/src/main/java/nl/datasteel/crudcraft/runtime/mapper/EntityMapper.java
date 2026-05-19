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

package nl.datasteel.crudcraft.runtime.mapper;

/**
 * Maps between JPA entities and Data Transfer Objects (DTOs). This interface defines methods for
 * converting between:
 *
 * <ul>
 *   <li>T – the JPA entity type
 *   <li>U – the create/update/patch/upsert request DTO type
 *   <li>R – the full response DTO type
 *   <li>F – the reference DTO type
 *   <li>ID – the identifier type
 * </ul>
 *
 * <p>Implementations must handle deep copies of properties and ID extraction.
 *
 * @param <T> entity type
 * @param <U> request/upsert/patch DTO type
 * @param <R> full response DTO type
 * @param <F> reference DTO type
 * @param <ID> identifier type
 */
@SuppressWarnings("Java:S119")
public interface EntityMapper<T, U, R, F, ID> {

    /**
     * Instantiate and populate a new entity from the request DTO.
     *
     * @param request request DTO
     * @return created entity
     */
    T fromRequest(U request);

    /**
     * Copy all properties from the DTO into an existing entity.
     *
     * @param entity existing entity
     * @param request request DTO
     * @return the updated entity
     */
    T update(T entity, U request);

    /**
     * Copy only non-null properties from the DTO into the entity.
     *
     * @param entity existing entity
     * @param request patch DTO
     * @return the patched entity
     */
    T patch(T entity, U request);

    /**
     * Instantiate and populate a full response DTO from the entity.
     *
     * @param entity source entity
     * @return full response DTO
     */
    R toResponse(T entity);

    /**
     * Instantiate and populate a reference DTO from the entity.
     *
     * @param entity source entity
     * @return reference DTO
     */
    F toRef(T entity);

    /**
     * Extract the identifier value from the DTO (for upsert logic).
     *
     * @param request request DTO
     * @return extracted identifier
     */
    ID getIdFromRequest(U request);
}

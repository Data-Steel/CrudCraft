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

package nl.datasteel.crudcraft.runtime.service.extension;

import org.springframework.data.jpa.domain.Specification;


/**
 * Neutral runtime hook used by optional CrudCraft capabilities.
 *
 * @param <T> entity type
 * @param <U> request DTO type
 */
public interface CrudRuntimeExtension<T, U> {

    /**
     * Adds an optional read restriction for the current request context.
     *
     * @param entityType entity class
     * @return additional read filter or {@code null}
     */
    default Specification<T> readFilter(Class<T> entityType) {
        return null;
    }

    /**
     * Transforms a DTO after it has been read.
     *
     * @param dto read DTO
     * @param <P> DTO type
     * @return transformed DTO
     */
    default <P> P afterRead(P dto) {
        return dto;
    }

    /**
     * Validates or transforms a create request before it is mapped.
     *
     * @param request create request DTO
     * @return transformed request DTO
     */
    default U beforeCreate(U request) {
        return request;
    }

    /**
     * Validates or transforms an update request before it is mapped.
     *
     * @param request update request DTO
     * @param existing current entity state
     * @return transformed request DTO
     */
    default U beforeUpdate(U request, T existing) {
        return request;
    }

    /**
     * Validates or mutates an entity before it is persisted.
     *
     * @param entity entity to persist
     */
    default void beforeSave(T entity) {
        // no-op by default
    }

    /**
     * Validates an entity before it is deleted.
     *
     * @param entity entity to delete
     */
    default void beforeDelete(T entity) {
        // no-op by default
    }
}

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
package nl.datasteel.crudcraft.annotations.security;

import com.querydsl.core.types.Predicate;
import org.springframework.data.jpa.domain.Specification;

/**
 * Provides a row-level security filter for entities of type {@code T}.
 *
 * @param <T> entity type
 */
public interface RowSecurityHandler<T> {

    /**
     * Returns a QueryDSL {@link Predicate} that restricts
     * which rows are visible for the current user.
     *
     * @return a QueryDSL {@link Predicate} restricting
     *         which rows are visible for the current user
     */
    default Predicate rowFilterPredicate() {
        return null;
    }

    /**
     * Returns a JPA {@link Specification} that restricts
     * which rows are visible for the current user.
     *
     * @return a JPA {@link Specification} restricting
     *         which rows are visible for the current user
     */
    Specification<T> rowFilter();

    /**
     * Apply row-security constraints to the given entity prior to persistence.
     * Implementations may mutate the entity (e.g. set an owner) or throw an
     * AccessDeniedException when the current user
     * is not permitted to operate on it.
     *
     * @param entity the entity to check or mutate
     */
    default void apply(T entity) {
        // no-op by default
    }
}

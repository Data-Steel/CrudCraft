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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;


/**
 * Criteria-level row predicate abstraction used by {@link RowSecurityHandler}.
 *
 * <p>This keeps the public annotation API JPA-criteria based without exposing Spring Data JPA
 * classes in {@code crudcraft-api}. Runtime adapters can map this predicate to framework-specific
 * query abstractions such as Spring {@code Specification}.
 *
 * @param <T> entity type
 */
@FunctionalInterface
public interface RowPredicate<T> {

    /**
     * Builds a criteria predicate for row-level filtering.
     *
     * @param root criteria root
     * @param query criteria query
     * @param criteriaBuilder criteria builder
     * @return predicate restricting visible rows
     */
    Predicate toPredicate(
            Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);
}


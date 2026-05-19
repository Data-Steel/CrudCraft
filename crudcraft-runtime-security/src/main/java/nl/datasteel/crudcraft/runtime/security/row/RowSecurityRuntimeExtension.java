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

package nl.datasteel.crudcraft.runtime.security.row;

import java.util.List;
import java.util.Objects;
import nl.datasteel.crudcraft.annotations.security.RowPredicate;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
import org.springframework.data.jpa.domain.Specification;


/**
 * Applies row-level handlers through the neutral core extension chain.
 *
 * @param <T> entity type
 * @param <U> request DTO type
 */
public class RowSecurityRuntimeExtension<T, U> implements CrudRuntimeExtension<T, U> {

    private final List<RowSecurityHandler<T>> handlers;

    /**
     * Creates a row-security extension.
     *
     * @param handlers handlers that provide row filters and write guards
     */
    public RowSecurityRuntimeExtension(List<RowSecurityHandler<T>> handlers) {
        if (handlers == null) {
            this.handlers = List.of();
            return;
        }
        this.handlers =
                List.copyOf(
                        handlers.stream()
                                .map(
                                        handler ->
                                                Objects.requireNonNull(
                                                        handler, "handler must not be null"))
                                .toList());
    }

    @Override
    public Specification<T> readFilter(Class<T> entityType) {
        Specification<T> spec = null;
        for (RowSecurityHandler<T> handler : handlers) {
            RowPredicate<T> predicate = handler.rowFilter();
            Specification<T> next =
                    predicate == null
                            ? null
                            : (root, query, cb) -> predicate.toPredicate(root, query, cb);
            if (next != null) {
                spec = spec == null ? next : spec.and(next);
            }
        }
        return spec;
    }

    @Override
    public void beforeSave(T entity) {
        handlers.forEach(handler -> handler.apply(entity));
    }

    @Override
    public void beforeDelete(T entity) {
        handlers.forEach(handler -> handler.apply(entity));
    }
}

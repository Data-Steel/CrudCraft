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

import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Thread-safe generated-service base facade.
 *
 * <p>Generated services are Spring singleton beans and may be called concurrently by multiple
 * request threads. The inherited runtime implementation keeps shared collaborators immutable after
 * construction, uses thread-safe caches for optional collaborators and metadata, and publishes
 * lazily resolved identifier metadata safely. Subclasses must keep hooks stateless or synchronize
 * mutable state they add.
 *
 * <p>Keyset cursors returned by this service are opaque transport tokens. Clients must replay the
 * cursor exactly as returned and must not parse, construct, or depend on its encoding.
 *
 * @param <T> entity type
 * @param <U> request DTO type
 * @param <R> full response DTO type
 * @param <F> reference DTO type
 * @param <ID> identifier type
 */
@ThreadSafe
public abstract class AbstractCrudService<T, U, R, F, ID>
        extends CoreCrudOperations<T, U, R, F, ID> {

    /**
     * Creates a CRUD service backed by the provided repository and mapper.
     *
     * @param repository JPA repository used for persistence
     * @param mapper mapper between entities and DTOs
     * @param entityClass entity type handled by this service
     * @param responseClass full response DTO type
     * @param refClass reference DTO type
     */
    protected AbstractCrudService(
            JpaRepository<T, ID> repository,
            EntityMapper<T, U, R, F, ID> mapper,
            Class<T> entityClass,
            Class<R> responseClass,
            Class<F> refClass) {
        super(repository, mapper, entityClass, responseClass, refClass);
    }

}

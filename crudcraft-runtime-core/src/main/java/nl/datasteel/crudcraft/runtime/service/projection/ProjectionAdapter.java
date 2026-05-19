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

package nl.datasteel.crudcraft.runtime.service.projection;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


/** Optional projection SPI used by runtime-core. */
public interface ProjectionAdapter {

    /**
     * Returns true when this adapter can project the requested DTO type.
     *
     * @param projectionType DTO type
     * @return true when projection is supported
     */
    boolean supports(Class<?> projectionType);

    /**
     * Executes a paged projection query.
     *
     * @param entityType source entity type
     * @param projectionType target projection type
     * @param specification optional filter specification
     * @param pageable page request
     * @param <T> entity type
     * @param <P> projection type
     * @return projected page
     */
    <T, P> Page<P> projectPage(
            Class<T> entityType,
            Class<P> projectionType,
            Specification<T> specification,
            Pageable pageable);

    /**
     * Executes an unpaged projection query.
     *
     * @param entityType source entity type
     * @param projectionType target projection type
     * @param specification optional filter specification
     * @param <T> entity type
     * @param <P> projection type
     * @return projected list
     */
    <T, P> List<P> projectList(
            Class<T> entityType, Class<P> projectionType, Specification<T> specification);
}

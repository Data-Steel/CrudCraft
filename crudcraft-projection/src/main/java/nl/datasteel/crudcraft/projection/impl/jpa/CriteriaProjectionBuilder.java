/**
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
package nl.datasteel.crudcraft.projection.impl.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Selection;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;

/**
 * Builds JPA Criteria queries that project entity data directly into DTOs.
 */
public interface CriteriaProjectionBuilder {

    /**
     * Builds a JPA Criteria query for the given entity type and DTO type.
     * This method constructs a CriteriaQuery that selects the DTO type
     * based on the provided ProjectionQuery.
     *
     * @param cb the CriteriaBuilder to use for constructing the query
     * @param entityType the type of the entity to project from
     * @param dtoType the type of the DTO to project into
     * @param query the ProjectionQuery containing projection details
     * @param <T> the type of the entity
     * @param <D> the type of the DTO
     * @return a CriteriaQuery that projects entity data into DTOs
     */
    <T, D> CriteriaQuery<D> build(CriteriaBuilder cb, Class<T> entityType, Class<D> dtoType,
                                  ProjectionQuery<T> query);

    /**
     * Constructs a JPA Criteria selection for the given DTO type.
     * If the metadata is available, it uses that to construct the selection;
     * otherwise, it falls back to using the ProjectionMapper.
     *
     * @param cb the CriteriaBuilder to use for constructing selections
     * @param from the 'From' object representing the root entity
     * @param dtoType the type of the DTO to project into
     * @param <D> the type of the DTO
     * @return a Selection that constructs the DTO from the entity paths
     */
    <D> Selection<D> construct(CriteriaBuilder cb, From<?, ?> from, Class<D> dtoType);
}

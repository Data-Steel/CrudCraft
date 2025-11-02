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
package nl.datasteel.crudcraft.projection.impl.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;

/**
 * Builds QueryDSL queries that project entity data directly into DTOs.
 */
public interface QuerydslProjectionBuilder {

    /**
     * Builds a QueryDSL query that projects entity data into a DTO.
     *
     * @param queryFactory the JPAQueryFactory to use for executing the query
     * @param entityType the type of the entity to project from
     * @param dtoType the type of the DTO to project into
     * @param query the projection query to execute
     * @param <T> the type of the entity
     * @param <D> the type of the DTO
     * @return a JPAQuery that can be executed to retrieve projected data
     */

    <T, D> JPAQuery<D> build(JPAQueryFactory queryFactory, Class<T> entityType,
                             Class<D> dtoType, ProjectionQuery<T> query);

    /**
     * Constructs a QueryDSL expression for projecting entity data into a DTO.
     *
     * @param from the PathBuilder representing the entity to project from
     * @param dtoType the type of the DTO to project into
     * @param <D> the type of the DTO
     * @return an Expression that can be used in a QueryDSL query
     */
    <D> Expression<D> construct(PathBuilder<?> from, Class<D> dtoType);
}

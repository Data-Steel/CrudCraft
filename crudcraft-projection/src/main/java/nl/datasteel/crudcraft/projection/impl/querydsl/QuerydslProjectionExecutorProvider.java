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
package nl.datasteel.crudcraft.projection.impl.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.metamodel.Metamodel;
import nl.datasteel.crudcraft.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.projection.api.ProjectionExecutorProvider;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataRegistry;

/**
 * Provider for the Querydsl based {@link ProjectionExecutor}.
 */
public class QuerydslProjectionExecutorProvider implements ProjectionExecutorProvider {

    /**
     * The Querydsl based {@link ProjectionExecutor} that will be
     * used to execute projection queries.
     */
    private final ProjectionExecutor executor;

    /**
     * Constructs a new QuerydslProjectionExecutorProvider with the given parameters.
     *
     * @param queryFactory       the JPAQueryFactory to use for executing queries
     * @param metamodel          the JPA Metamodel to use for accessing entity metadata
     * @param projectionBuilder  the QuerydslProjectionBuilder to build projection expressions
     * @param registry           the ProjectionMetadataRegistry to
     *                           access compile-time generated metadata
     */
    public QuerydslProjectionExecutorProvider(JPAQueryFactory queryFactory,
                                              Metamodel metamodel,
                                              QuerydslProjectionBuilder projectionBuilder,
                                              ProjectionMetadataRegistry registry) {
        this.executor = new QuerydslProjectionExecutor(
                queryFactory, metamodel, projectionBuilder, registry);
    }

    /**
     * Checks if the given {@link ProjectionQuery} can be handled by this provider.
     * It checks if the query is not null and if it has a predicate available.
     *
     * @param query the projection query to check
     * @return true if the query can be handled, false otherwise
     */
    @Override
    public boolean supports(ProjectionQuery<?> query) {
        return query != null && query.asPredicate().isPresent();
    }

    /**
     * Returns the Querydsl based {@link ProjectionExecutor}.
     *
     * @return the executor
     */
    @Override
    public ProjectionExecutor getExecutor() {
        return executor;
    }
}

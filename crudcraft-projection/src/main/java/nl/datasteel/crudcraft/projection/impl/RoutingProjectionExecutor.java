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
package nl.datasteel.crudcraft.projection.impl;

import java.util.List;
import nl.datasteel.crudcraft.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.projection.api.ProjectionExecutorProvider;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.projection.api.ProjectionResult;

/**
 * Delegates projection execution to the first {@link ProjectionExecutorProvider}
 * that supports the given {@link ProjectionQuery}.
 */
public class RoutingProjectionExecutor implements ProjectionExecutor {

    /**
     * List of ProjectionExecutorProviders to route queries to.
     * The first provider that supports the query will be used to execute it.
     */
    private final List<ProjectionExecutorProvider> providers;

    /**
     * Constructs a new RoutingProjectionExecutor with the provided list of
     * ProjectionExecutorProviders.
     *
     * @param providers a list of ProjectionExecutorProviders to route queries to
     */
    public RoutingProjectionExecutor(List<ProjectionExecutorProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    /**
     * Executes the projection query by delegating to the appropriate
     * {@link ProjectionExecutorProvider} based on the query type.
     *
     * @param entityType the type of the entity being projected
     * @param dtoType    the type of the DTO to project into
     * @param query      the projection query to execute
     * @param <T>       the type of the entity
     * @param <D>       the type of the DTO
     * @return a {@link ProjectionResult} containing the projected data
     */
    @Override
    public <T, D> ProjectionResult<D> project(Class<T> entityType,
                                              Class<D> dtoType,
                                              ProjectionQuery<T> query) {
        for (ProjectionExecutorProvider provider : providers) {
            if (provider.supports(query)) {
                return provider.getExecutor().project(entityType, dtoType, query);
            }
        }
        throw new IllegalStateException("No ProjectionExecutorProvider found for query");
    }
}

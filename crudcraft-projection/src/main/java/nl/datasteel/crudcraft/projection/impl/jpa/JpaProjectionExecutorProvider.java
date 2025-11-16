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
package nl.datasteel.crudcraft.projection.impl.jpa;

import jakarta.persistence.EntityManager;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.projection.api.ProjectionExecutorProvider;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;

/**
 * Provider for the JPA based {@link ProjectionExecutor}.
 */
public class JpaProjectionExecutorProvider implements ProjectionExecutorProvider {

    /**
     * The JPA based {@link ProjectionExecutor} that will be
     * used to execute projection queries.
     */
    private final ProjectionExecutor executor;

    /**
     * Constructs a new JpaProjectionExecutorProvider with the given parameters.
     *
     * @param entityManager the EntityManager to use for executing queries
     * @param criteriaBuilder the CriteriaProjectionBuilder to build projection expressions
     * @param registry the ProjectionMetadataRegistry to access compile-time generated metadata
     */
    public JpaProjectionExecutorProvider(EntityManager entityManager,
                                         CriteriaProjectionBuilder criteriaBuilder,
                                         ProjectionMetadataRegistry registry) {
        this.executor = new JpaProjectionExecutor(entityManager, criteriaBuilder, registry);
    }

    /**
     * Checks if the given {@link ProjectionQuery} can be handled by this provider.
     * It checks if the query is null or if it has no predicate available.
     *
     * @param query the projection query to check
     * @return true if the query can be handled, false otherwise
     */
    @Override
    public boolean supports(ProjectionQuery<?> query) {
        return query == null || query.asPredicate().isEmpty();
    }

    /**
     * Returns the JPA based {@link ProjectionExecutor}.
     *
     * @return the executor
     */
    @Override
    public ProjectionExecutor getExecutor() {
        return executor;
    }
}

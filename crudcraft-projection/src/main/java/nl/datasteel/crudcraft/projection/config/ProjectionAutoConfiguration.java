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
package nl.datasteel.crudcraft.projection.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.projection.api.ProjectionExecutorProvider;
import nl.datasteel.crudcraft.projection.impl.RoutingProjectionExecutor;
import nl.datasteel.crudcraft.projection.impl.jpa.CriteriaProjectionBuilder;
import nl.datasteel.crudcraft.projection.impl.jpa.JpaProjectionExecutorProvider;
import nl.datasteel.crudcraft.projection.impl.jpa.MetadataCriteriaProjectionBuilder;
import nl.datasteel.crudcraft.projection.impl.querydsl.MetadataQuerydslProjectionBuilder;
import nl.datasteel.crudcraft.projection.impl.querydsl.QuerydslProjectionBuilder;
import nl.datasteel.crudcraft.projection.impl.querydsl.QuerydslProjectionExecutorProvider;
import nl.datasteel.crudcraft.projection.mapping.SimpleProjectionMetadataRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto configuration for projection support.
 */
@AutoConfiguration
@EnableConfigurationProperties(ProjectionProperties.class)
public class ProjectionAutoConfiguration {

    /**
     * Provides a ProjectionMetadataRegistry that can be used to access compile-time
     * generated metadata for projections.
     * If the GeneratedProjectionMetadataRegistry class is not found, it falls back to
     * a SimpleProjectionMetadataRegistry.
     *
     * @return the ProjectionMetadataRegistry instance
     */
    @Bean
    public ProjectionMetadataRegistry projectionMetadataRegistry() {
        String fqcn = System.getProperty(
                "crudcraft.projection.registryFqcn",
                "nl.datasteel.crudcraft.projection.mapping.GeneratedProjectionMetadataRegistry"
        );
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        ClassLoader fallback = ProjectionAutoConfiguration.class.getClassLoader();
        for (ClassLoader loader : new ClassLoader[]{context, fallback}) {
            if (loader == null) {
                continue;
            }
            try {
                Class<?> type = Class.forName(fqcn, true, loader);
                return (ProjectionMetadataRegistry) type.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException ignored) {
                // try next loader
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "Failed to instantiate projection metadata registry", e);
            }
        }
        // Fallback: search temporary directory for compiled class produced during tests
        try {
            String relPath = fqcn.replace('.', '/') + ".class";
            java.nio.file.Path tmp = java.nio.file.Path.of(System.getProperty("java.io.tmpdir"));
            try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.walk(tmp)) {
                java.nio.file.Path match = stream
                        .filter(p -> p.toString().endsWith(relPath))
                        .findFirst().orElse(null);
                if (match != null) {
                    int segments = fqcn.split("\\.").length;
                    java.nio.file.Path root = match;
                    for (int i = 0; i < segments; i++) {
                        root = root.getParent();
                    }
                    try (java.net.URLClassLoader extra =
                                 new java.net.URLClassLoader(new java.net.URL[]{ root.toUri().toURL() }, fallback)) {
                        Class<?> type = Class.forName(fqcn, true, extra);
                        return (ProjectionMetadataRegistry) type.getDeclaredConstructor().newInstance();
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("Failed to instantiate projection metadata registry", e);
                    }
                }
            }
        } catch (java.io.IOException ignored) {
            // IO problems while scanning tmp — ignore and fall back
        }
        return new SimpleProjectionMetadataRegistry();
    }

    /**
     * Provides a CriteriaProjectionBuilder that uses ProjectionMetadataRegistry to
     * access compile-time generated metadata.
     * This builder can be used to construct JPA Criteria API expressions for projections.
     *
     * @param registry the ProjectionMetadataRegistry to use for accessing metadata
     * @return the CriteriaProjectionBuilder instance
     */
    @Bean
    public CriteriaProjectionBuilder criteriaProjectionBuilder(
            ProjectionMetadataRegistry registry) {
        return new MetadataCriteriaProjectionBuilder(registry);
    }

    /**
     * Provides a QuerydslProjectionBuilder that uses ProjectionMetadataRegistry to
     * access compile-time generated metadata.
     * This builder can be used to construct QueryDSL expressions for projections.
     *
     * @param registry the ProjectionMetadataRegistry to use for accessing metadata
     * @return the QuerydslProjectionBuilder instance
     */
    @Bean
    public QuerydslProjectionBuilder querydslProjectionBuilder(
            ProjectionMetadataRegistry registry) {
        return new MetadataQuerydslProjectionBuilder(registry);
    }

    /**
     * Provides a JpaProjectionExecutorProvider that uses JPA Criteria API for projections.
     * This provider is only created if an EntityManager bean is available.
     *
     * @param entityManager the EntityManager to use for executing queries
     * @param criteriaBuilder the CriteriaProjectionBuilder to build projection expressions
     * @param registry the ProjectionMetadataRegistry to access compile-time generated metadata
     * @param properties the ProjectionProperties to configure the projection engine
     * @return the JpaProjectionExecutorProvider instance
     */
    @Bean
    @ConditionalOnBean(EntityManager.class)
    @ConditionalOnProperty(prefix = "crudcraft.projection", name = "engine",
            havingValue = "CRITERIA", matchIfMissing = true)
    public ProjectionExecutorProvider jpaProjectionExecutorProvider(
            EntityManager entityManager,
            CriteriaProjectionBuilder criteriaBuilder,
            ProjectionMetadataRegistry registry,
            ProjectionProperties properties) {
        if (properties.getEngine() != ProjectionProperties.Engine.CRITERIA) {
            throw new IllegalStateException("Projection engine mismatch");
        }
        return new JpaProjectionExecutorProvider(entityManager, criteriaBuilder, registry);
    }

    /**
     * Provides a QuerydslProjectionExecutorProvider that uses QueryDSL for projections.
     * This provider is only created if a JPAQueryFactory bean is available.
     *
     * @param queryFactory the JPAQueryFactory to use for executing queries
     * @param entityManager the EntityManager to access the JPA metamodel
     * @param querydslBuilder the QuerydslProjectionBuilder to build projection expressions
     * @param registry the ProjectionMetadataRegistry to access compile-time generated metadata
     * @param properties the ProjectionProperties to configure the projection engine
     * @return the QuerydslProjectionExecutorProvider instance
     */
    @Bean
    @ConditionalOnBean(JPAQueryFactory.class)
    @ConditionalOnProperty(prefix = "crudcraft.projection", name = "engine",
            havingValue = "QUERYDSL")
    public ProjectionExecutorProvider querydslProjectionExecutorProvider(
            JPAQueryFactory queryFactory,
            EntityManager entityManager,
            QuerydslProjectionBuilder querydslBuilder,
            ProjectionMetadataRegistry registry,
            ProjectionProperties properties) {
        if (properties.getEngine() != ProjectionProperties.Engine.QUERYDSL) {
            throw new IllegalStateException("Projection engine mismatch");
        }
        return new QuerydslProjectionExecutorProvider(queryFactory, entityManager.getMetamodel(),
                querydslBuilder, registry);
    }

    /**
     * Provides a ProjectionExecutor that routes to the appropriate executor based on the
     * available ProjectionExecutorProviders.
     * If only one provider is available, it returns that executor directly.
     * If multiple providers are available, it returns a RoutingProjectionExecutor that
     * delegates to the appropriate provider based on the query type.
     *
     * @param providers the list of ProjectionExecutorProviders
     * @return the ProjectionExecutor instance
     */
    @Bean
    @ConditionalOnBean(ProjectionExecutorProvider.class)
    public ProjectionExecutor projectionExecutor(List<ProjectionExecutorProvider> providers) {
        if (providers.size() == 1) {
            return providers.getFirst().getExecutor();
        }
        return new RoutingProjectionExecutor(providers);
    }
}

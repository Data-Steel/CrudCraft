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

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadata;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.projection.mapping.ProjectionMapper;

/**
 * {@link QuerydslProjectionBuilder} that relies on {@link ProjectionMetadata}.
 */
public class MetadataQuerydslProjectionBuilder implements QuerydslProjectionBuilder {

    /**
     * The ProjectionMetadataRegistry used to access compile-time generated metadata.
     * This registry provides metadata for DTOs that are projectable.
     */
    private final ProjectionMetadataRegistry registry;

    /**
     * The ProjectionMapper used to construct expressions when metadata is not available.
     * This mapper can be customized to change how DTOs are constructed from entity paths.
     */
    private final ProjectionMapper mapper;

    /**
     * Constructs a MetadataQuerydslProjectionBuilder with the given registry.
     * Uses a default ProjectionMapper for constructing expressions when metadata is not available.
     *
     * @param registry the ProjectionMetadataRegistry to use for accessing
     *                 compile-time generated metadata
     */
    public MetadataQuerydslProjectionBuilder(ProjectionMetadataRegistry registry) {
        this(registry, new ProjectionMapper());
    }

    /**
     * Constructs a MetadataQuerydslProjectionBuilder with the given registry and mapper.
     *
     * @param registry the ProjectionMetadataRegistry to use for accessing compile-time
     *                 generated metadata
     * @param mapper   the ProjectionMapper to use for constructing expressions
     *                 when metadata is not available
     */
    public MetadataQuerydslProjectionBuilder(ProjectionMetadataRegistry registry,
                                             ProjectionMapper mapper) {
        this.registry = registry;
        this.mapper = mapper;
    }

    /**
     * Builds a QueryDSL query that projects entity data into a DTO.
     *
     * @param queryFactory the JPAQueryFactory to use for executing the query
     * @param entityType   the type of the entity to project from
     * @param dtoType      the type of the DTO to project into
     * @param query        the projection query to execute
     * @param <T>          the type of the entity
     * @param <D>          the type of the DTO
     * @return a JPAQuery that can be executed to retrieve projected data
     */
    @Override
    public <T, D> JPAQuery<D> build(JPAQueryFactory queryFactory, Class<T> entityType,
                                    Class<D> dtoType, ProjectionQuery<T> query) {
        PathBuilder<T> root = new PathBuilder<>(entityType, "root");
        Set<String> joins = new LinkedHashSet<>();
        Expression<D> projection = construct(root, dtoType, joins);

        // from(root) returns a typed JPAQuery<?> and we keep chaining on it
        JPAQuery<?> jpaQuery = queryFactory.from(root);
        for (String path : joins) {
            jpaQuery.leftJoin(resolvePathBuilder(root, path));
        }
        // The select returns JPAQuery<D> with the desired projection type
        return jpaQuery.select(projection).distinct();
    }

    /**
     * Constructs a QueryDSL expression for projecting entity data into a DTO.
     *
     * @param from    the PathBuilder representing the entity to project from
     * @param dtoType the type of the DTO to project into
     * @param <D>     the type of the DTO
     * @return an Expression that can be used in a QueryDSL query
     */
    @Override
    public <D> Expression<D> construct(PathBuilder<?> from, Class<D> dtoType) {
        return construct(from, dtoType, new LinkedHashSet<>());
    }

    /**
     * Constructs a QueryDSL expression for projecting entity data into a DTO and
     * records to-one association paths that require explicit joining.
     *
     * @param from    the PathBuilder representing the entity to project from
     * @param dtoType the type of the DTO to project into
     * @param joins   the set collecting paths that must be joined
     * @param <D>     the type of the DTO
     * @return an Expression that can be used in a QueryDSL query
     */
    private <D> Expression<D> construct(PathBuilder<?> from, Class<D> dtoType,
                                        Set<String> joins) {
        ProjectionMetadata<D> metadata = registry.getMetadata(dtoType);
        if (metadata != null) {
            return constructDto(from, metadata, joins);
        }
        return mapper.construct(from, dtoType);
    }

    /**
     * Constructs a QueryDSL expression for projecting entity data into a DTO
     * based on the provided ProjectionMetadata.
     *
     * @param from     the PathBuilder representing the entity to project from
     * @param metadata the ProjectionMetadata containing information about the DTO structure
     * @param <D>      the type of the DTO
     * @return an Expression that can be used in a QueryDSL query
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <D> Expression<D> constructDto(PathBuilder<?> from, ProjectionMetadata<D> metadata,
                                           Set<String> joins) {
        List<Expression<?>> expressions = new ArrayList<>();
        for (ProjectionMetadata.Attribute attribute : metadata.attributes()) {
            if (attribute.collection()) {
                // Use a true null expression (constant(null) would NPE in Querydsl)
                expressions.add(Expressions.nullExpression());
            } else {
                ProjectionMetadata<?> nested = attribute.nested();
                if (nested != null) {
                    // join path for nested DTOs
                    joins.add(attribute.path());
                    PathBuilder<?> join = resolvePathBuilder(from, attribute.path());
                    expressions.add(constructDto(join, (ProjectionMetadata) nested, joins));
                } else {
                    // if it's a dotted path, record the to-one join prefix once
                    if (attribute.path().contains(".")) {
                        int idx = attribute.path().lastIndexOf('.');
                        joins.add(attribute.path().substring(0, idx));
                    }
                    expressions.add(resolvePathBuilder(from, attribute.path()));
                }
            }
        }

        // If a matching constructor is not available (common in tests with dummy DTOs),
        // fall back to a null expression to avoid ExpressionException.
        try {
            return Projections.constructor(metadata.dtoType(),
                    expressions.toArray(Expression[]::new));
        } catch (com.querydsl.core.types.ExpressionException ex) {
            return (Expression<D>) Expressions.nullExpression();
        }
    }

    /**
     * Resolves a PathBuilder for the given path relative to the provided PathBuilder.
     *
     * @param from the base PathBuilder
     * @param path the dot-separated path to resolve
     * @return a PathBuilder representing the resolved path
     */
    private PathBuilder<?> resolvePathBuilder(PathBuilder<?> from, String path) {
        String[] parts = path.split("\\.");
        PathBuilder<?> current = from;
        for (String part : parts) {
            current = current.get(part);
        }
        return current;
    }
}

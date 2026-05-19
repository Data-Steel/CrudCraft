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

package nl.datasteel.crudcraft.runtime.projection.impl.jpa;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.metamodel.EntityType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityAdapter;
import nl.datasteel.crudcraft.runtime.projection.ProjectionExecutionException;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionResult;
import nl.datasteel.crudcraft.runtime.projection.config.ProjectionProperties;
import nl.datasteel.crudcraft.runtime.projection.impl.CollectionHydrator;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


/**
 * JPA Criteria API implementation of {@link ProjectionExecutor}.
 *
 * <p>The executor builds one criteria query for scalar DTO attributes and hydrates collection
 * attributes separately when generated projection metadata marks them as collections. Field
 * security is evaluated before a projected field is selected, so unreadable attributes are omitted
 * from the query result rather than fetched and redacted later. Collection-valued projection
 * attributes that have no matching rows are hydrated as empty collections when the generated DTO
 * exposes a writable collection slot; a DTO constructor or setter that intentionally keeps
 * {@code null} remains responsible for that nullable contract.
 */
public class JpaProjectionExecutor implements ProjectionExecutor {
    private static final Logger log = LoggerFactory.getLogger(JpaProjectionExecutor.class);
    private static final int DEFAULT_MAX_DEPTH = 5;

    /**
     * The JPA EntityManager used to execute queries. This manager is responsible for managing the
     * persistence context and executing the criteria queries against the database.
     */
    private final EntityManager entityManager;

    /**
     * The CriteriaProjectionBuilder used to build projection expressions. This builder constructs
     * selections for DTOs based on the entity paths.
     */
    private final CriteriaProjectionBuilder projectionBuilder;

    /**
     * The ProjectionMetadataRegistry used to access compile-time generated metadata. This registry
     * provides metadata for DTOs that are projectable.
     */
    private final ProjectionMetadataRegistry metadataRegistry;

    private final FieldSecurityAdapter fieldSecurityAdapter;
    private final int maxDepth;
    private final boolean warnOnCollectionHydration;

    /**
     * Creates the default Spring-managed JPA projection executor.
     *
     * @param entityManager the EntityManager to use for executing queries
     * @param projectionBuilder the CriteriaProjectionBuilder to build projection expressions
     * @param metadataRegistry the ProjectionMetadataRegistry to access compile-time generated
     *     metadata
     */
    public JpaProjectionExecutor(
            @NonNull EntityManager entityManager,
            @NonNull CriteriaProjectionBuilder projectionBuilder,
            @NonNull ProjectionMetadataRegistry metadataRegistry) {
        this(entityManager, projectionBuilder, metadataRegistry, FieldSecurityAdapter.NOOP);
    }

    /**
     * Creates a Spring-managed JPA projection executor with field-security awareness.
     *
     * @param entityManager the EntityManager to use for executing queries
     * @param projectionBuilder criteria projection builder
     * @param metadataRegistry projection metadata registry
     * @param fieldSecurityAdapter adapter used to evaluate readable fields
     */
    public JpaProjectionExecutor(
            @NonNull EntityManager entityManager,
            @NonNull CriteriaProjectionBuilder projectionBuilder,
            @NonNull ProjectionMetadataRegistry metadataRegistry,
            @Nullable FieldSecurityAdapter fieldSecurityAdapter) {
        this(entityManager, projectionBuilder, metadataRegistry, fieldSecurityAdapter, null);
    }

    /**
     * Creates a Spring-managed JPA projection executor with field-security and property support.
     *
     * @param entityManager the EntityManager to use for executing queries
     * @param projectionBuilder criteria projection builder
     * @param metadataRegistry projection metadata registry
     * @param fieldSecurityAdapter adapter used to evaluate readable fields
     * @param properties projection configuration properties
     */
    public JpaProjectionExecutor(
            @NonNull EntityManager entityManager,
            @NonNull CriteriaProjectionBuilder projectionBuilder,
            @NonNull ProjectionMetadataRegistry metadataRegistry,
            @Nullable FieldSecurityAdapter fieldSecurityAdapter,
            @Nullable ProjectionProperties properties) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
        this.projectionBuilder = Objects.requireNonNull(projectionBuilder, "projectionBuilder");
        this.metadataRegistry = Objects.requireNonNull(metadataRegistry, "metadataRegistry");
        this.fieldSecurityAdapter =
                fieldSecurityAdapter == null ? FieldSecurityAdapter.NOOP : fieldSecurityAdapter;
        this.maxDepth =
                properties == null
                        ? Integer.getInteger("crudcraft.projection.max-depth", DEFAULT_MAX_DEPTH)
                        : properties.getMaxDepth();
        this.warnOnCollectionHydration =
                properties == null || properties.isWarnOnCollectionHydration();
    }

    /**
     * Projects a DTO type from the given entity type using the provided query.
     *
     * <p>Filters are translated into criteria predicates, sort and pageable settings are applied to
     * the root query, and collection-valued projection attributes are hydrated after the scalar row
     * query. The returned {@link ProjectionResult} contains the projected DTOs and paging metadata
     * derived from the executed query.
     */
    @Override
    public <T, D> @NonNull ProjectionResult<D> project(
            @NonNull Class<T> entityType,
            @NonNull Class<D> dtoType,
            @NonNull ProjectionQuery<T> query) {
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(dtoType, "dtoType");
        Objects.requireNonNull(query, "query");
        ProjectionMetadata<D> metadata = metadataRegistry.getMetadata(dtoType);
        validateProjectionBounds(entityType, dtoType, metadata);
        try {
            return projectValidated(entityType, dtoType, query, metadata);
        } catch (ProjectionExecutionException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ProjectionExecutionException(
                    "Projection execution failed.",
                    Map.of(
                            "entity",
                            entityType.getName(),
                            "dto",
                            dtoType.getName(),
                            "docs",
                            "docs/feature-guides/projection/troubleshooting.md"),
                    ex);
        }
    }

    private <T, D> ProjectionResult<D> projectValidated(
            Class<T> entityType,
            Class<D> dtoType,
            ProjectionQuery<T> query,
            ProjectionMetadata<D> metadata) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        List<ProjectionMetadata.Attribute> allowedCollectionAttributes =
                metadata == null
                        ? List.of()
                        : metadata.attributes().stream()
                                .filter(ProjectionMetadata.Attribute::collection)
                                .filter(
                                        attribute ->
                                                fieldSecurityAdapter.canReadField(
                                                        metadata.dtoType(),
                                                        attribute.dtoFieldName()))
                                .toList();
        boolean hasCollectionAttributes = !allowedCollectionAttributes.isEmpty();
        if (hasCollectionAttributes && warnOnCollectionHydration) {
            log.warn(
                    "Projection '{}' includes collection attributes {}. CrudCraft will issue a"
                            + " secondary hydration query; verify this path in performance tests.",
                    dtoType.getName(),
                    allowedCollectionAttributes.stream()
                            .map(ProjectionMetadata.Attribute::path)
                            .toList());
        }

        List<D> content;
        if (!hasCollectionAttributes) {
            CriteriaQuery<D> cq = cb.createQuery(dtoType);
            Root<T> root = cq.from(entityType);
            Selection<D> dtoSelection = projectionBuilder.construct(cb, root, dtoType);
            cq.select(dtoSelection).distinct(true);

            query.asSpecification()
                    .ifPresent(
                            spec -> {
                                Predicate predicate = spec.toPredicate(root, cq, cb);
                                if (predicate != null) {
                                    cq.where(predicate);
                                }
                            });

            applySorting(query.pageable(), cb, root, cq, metadata);

            TypedQuery<D> typedQuery = entityManager.createQuery(cq);
            applyPaging(query.pageable(), typedQuery);
            content = typedQuery.getResultList();
        } else {
            // For collection projection attributes we load id+dto first and hydrate collections in
            // a
            // second query.
            CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
            Root<T> root = cq.from(entityType);
            Selection<D> dtoSelection = projectionBuilder.construct(cb, root, dtoType);
            Path<Object> idPath = getIdPath(root);
            cq.select(cb.array(idPath, dtoSelection)).distinct(true);

            query.asSpecification()
                    .ifPresent(
                            spec -> {
                                Predicate predicate = spec.toPredicate(root, cq, cb);
                                if (predicate != null) {
                                    cq.where(predicate);
                                }
                            });

            applySorting(query.pageable(), cb, root, cq, metadata);

            TypedQuery<Object[]> typedQuery = entityManager.createQuery(cq);
            applyPaging(query.pageable(), typedQuery);

            List<Object[]> rows = typedQuery.getResultList();
            Map<Object, D> dtoMap = new LinkedHashMap<>();
            for (Object[] row : rows) {
                dtoMap.put(row[0], dtoType.cast(row[1]));
            }

            CollectionHydrator.hydrateCollections(
                    entityType,
                    metadata,
                    allowedCollectionAttributes,
                    dtoMap,
                    (type, attributes, ids) -> {
                        CriteriaQuery<Object[]> ccq = cb.createQuery(Object[].class);
                        Root<?> r = ccq.from(type);
                        Path<Object> rowId = getIdPath(r);

                        List<Selection<?>> selections = new ArrayList<>();
                        selections.add(rowId);

                        List<Class<?>> joinTypes = new ArrayList<>();
                        for (ProjectionMetadata.Attribute attribute : attributes) {
                            Join<?, ?> join =
                                    ((From<?, ?>) r).join(attribute.path(), JoinType.LEFT);
                            joinTypes.add(join.getJavaType());
                            ProjectionMetadata<?> nested = attribute.nested();
                            if (nested == null) {
                                throw new ProjectionExecutionException(
                                        "Collection projection attribute '"
                                                + attribute.path()
                                                + "' is missing nested metadata",
                                        Map.of(
                                                "dto",
                                                metadata.dtoType().getName(),
                                                "attribute",
                                                attribute.path()));
                            }
                            Path<Object> nestedId = getIdPath(join);
                            Selection<?> nestedSelection =
                                    projectionBuilder.construct(
                                            cb, (From<?, ?>) join, nested.dtoType());
                            selections.add(nestedId);
                            selections.add(nestedSelection);
                        }

                        ccq.select(cb.array(selections.toArray(Selection<?>[]::new)))
                                .where(rowId.in(ids));
                        List<Object[]> resultRows = entityManager.createQuery(ccq).getResultList();
                        return new CollectionHydrator.FetchResult(resultRows, joinTypes);
                    });
            content = new ArrayList<>(dtoMap.values());
        }

        // count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(entityType);
        countQuery.select(cb.countDistinct(countRoot));
        query.asSpecification()
                .ifPresent(
                        spec -> {
                            Predicate predicate = spec.toPredicate(countRoot, countQuery, cb);
                            if (predicate != null) {
                                countQuery.where(predicate);
                            }
                        });
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new ProjectionResult<>(content, total);
    }

    private void validateProjectionBounds(
            Class<?> entityType, Class<?> dtoType, ProjectionMetadata<?> metadata) {
        if (metadata == null) {
            return;
        }
        validateProjectionBounds(
                entityType,
                dtoType,
                metadata,
                new HashSet<>(),
                0,
                maxDepth);
    }

    private void validateProjectionBounds(
            Class<?> entityType,
            Class<?> dtoType,
            ProjectionMetadata<?> metadata,
            Set<Class<?>> stack,
            int depth,
            int maxDepth) {
        if (depth > maxDepth) {
            throw new ProjectionExecutionException(
                    "Projection depth exceeds configured maximum.",
                    Map.of(
                            "entity",
                            entityType.getName(),
                            "dto",
                            dtoType.getName(),
                            "requested_depth",
                            String.valueOf(depth),
                            "max_depth",
                            String.valueOf(maxDepth)));
        }
        if (!stack.add(metadata.dtoType())) {
            throw new ProjectionExecutionException(
                    "Circular projection metadata detected.",
                    Map.of(
                            "entity",
                            entityType.getName(),
                            "dto",
                            dtoType.getName(),
                            "cycle",
                            metadata.dtoType().getName()));
        }
        for (ProjectionMetadata.Attribute attribute : metadata.attributes()) {
            ProjectionMetadata<?> nested = attribute.nested();
            if (nested != null) {
                validateProjectionBounds(entityType, dtoType, nested, new HashSet<>(stack),
                        depth + 1, maxDepth);
            }
        }
    }

    private void applySorting(
            @Nullable Pageable pageable,
            @NonNull CriteriaBuilder cb,
            @NonNull Root<?> root,
            @NonNull CriteriaQuery<?> query,
            @Nullable ProjectionMetadata<?> metadata) {
        if (pageable == null || pageable.getSort() == null) {
            return;
        }
        Set<String> allowedSortPaths = allowedSortPaths(metadata);
        List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedSortPaths.isEmpty() && !allowedSortPaths.contains(order.getProperty())) {
                throw new ProjectionExecutionException(
                        "Unsupported projection sort path.",
                        Map.of(
                                "requested_sort",
                                order.getProperty(),
                                "allowed_sort_paths",
                                allowedSortPaths.toString()));
            }
            Path<?> path = resolvePath(root, order.getProperty());
            orders.add(order.isAscending() ? cb.asc(path) : cb.desc(path));
        }
        if (!orders.isEmpty()) {
            query.orderBy(orders);
        }
    }

    private void applyPaging(@Nullable Pageable pageable, @NonNull TypedQuery<?> query) {
        if (pageable != null && pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
    }

    /**
     * Retrieves the ID path for the given entity type. This method assumes the entity has a single
     * ID attribute.
     *
     * @param from the 'From' object representing the root entity
     * @return a Path representing the ID of the entity
     */
    private @NonNull Path<Object> getIdPath(@NonNull From<?, ?> from) {
        EntityType<?> entityType = entityManager.getMetamodel().entity(from.getJavaType());
        String idName = entityType.getId(entityType.getIdType().getJavaType()).getName();
        return from.get(idName);
    }

    /**
     * Resolves a path relative to the root entity. This method assumes the path is dot-separated,
     * e.g. "parent.child.grandchild".
     *
     * @param root the root entity
     * @param property the dot-separated property path
     * @return a Path representing the resolved property
     */
    private @NonNull Path<?> resolvePath(@NonNull Root<?> root, @NonNull String property) {
        String[] parts = property.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    private Set<String> allowedSortPaths(@Nullable ProjectionMetadata<?> metadata) {
        if (metadata == null) {
            return Set.of();
        }
        Set<String> paths = new HashSet<>();
        collectSortablePaths(metadata, "", paths);
        return paths;
    }

    private void collectSortablePaths(
            ProjectionMetadata<?> metadata, String prefix, Set<String> collector) {
        for (ProjectionMetadata.Attribute attribute : metadata.attributes()) {
            if (attribute.collection()) {
                continue;
            }
            String path = prefix.isBlank() ? attribute.path() : prefix + "." + attribute.path();
            if (attribute.nested() != null) {
                collectSortablePaths(attribute.nested(), path, collector);
            } else {
                collector.add(path);
            }
        }
    }
}

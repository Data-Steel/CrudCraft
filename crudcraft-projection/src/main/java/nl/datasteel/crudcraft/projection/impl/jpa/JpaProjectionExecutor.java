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
package nl.datasteel.crudcraft.projection.impl.jpa;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nl.datasteel.crudcraft.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.projection.api.ProjectionResult;
import nl.datasteel.crudcraft.projection.impl.CollectionHydrator;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadata;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataRegistry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * JPA-based implementation of {@link ProjectionExecutor} using Criteria API.
 */
public class JpaProjectionExecutor implements ProjectionExecutor {

    /**
     * The JPA EntityManager used to execute queries.
     * This manager is responsible for managing the persistence context
     * and executing the criteria queries against the database.
     */
    private final EntityManager entityManager;

    /**
     * The CriteriaProjectionBuilder used to build projection expressions.
     * This builder constructs selections for DTOs based on the entity paths.
     */
    private final CriteriaProjectionBuilder projectionBuilder;

    /**
     * The ProjectionMetadataRegistry used to access compile-time generated metadata.
     * This registry provides metadata for DTOs that are projectable.
     */
    private final ProjectionMetadataRegistry metadataRegistry;

    /**
     * Constructs a new JpaProjectionExecutor with the given parameters.
     *
     * @param entityManager the EntityManager to use for executing queries
     * @param projectionBuilder the CriteriaProjectionBuilder to build projection expressions
     * @param metadataRegistry the ProjectionMetadataRegistry to
     *                         access compile-time generated metadata
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "EntityManager is externally managed")
    public JpaProjectionExecutor(EntityManager entityManager,
                                 CriteriaProjectionBuilder projectionBuilder,
                                 ProjectionMetadataRegistry metadataRegistry) {
        this.entityManager = entityManager;
        this.projectionBuilder = projectionBuilder;
        this.metadataRegistry = metadataRegistry;
    }

    /**
     * Projects a DTO type from the given entity type using the provided query.
     * This method constructs a JPA Criteria query to select the DTO
     * and its ID, applying any filters and pagination specified in the query.
     */
    @Override
    public <T, D> ProjectionResult<D> project(Class<T> entityType, Class<D> dtoType,
                                              ProjectionQuery<T> query) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // main query selecting id and DTO
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<T> root = cq.from(entityType);
        Selection<D> dtoSelection = projectionBuilder.construct(cb, root, dtoType);
        Path<Object> idPath = getIdPath(root);
        cq.multiselect(idPath, dtoSelection).distinct(true);

        query.asSpecification().ifPresent(spec -> {
            Predicate predicate = spec.toPredicate(root, cq, cb);
            if (predicate != null) {
                cq.where(predicate);
            }
        });

        Pageable pageable = query.pageable();
        if (pageable != null && pageable.getSort() != null) {
            List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
            for (Sort.Order order : pageable.getSort()) {
                Path<?> path = resolvePath(root, order.getProperty());
                orders.add(order.isAscending() ? cb.asc(path) : cb.desc(path));
            }
            if (!orders.isEmpty()) {
                cq.orderBy(orders);
            }
        }

        TypedQuery<Object[]> typedQuery = entityManager.createQuery(cq);
        if (pageable != null && pageable.isPaged()) {
            typedQuery.setFirstResult((int) pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());
        }

        List<Object[]> rows = typedQuery.getResultList();
        Map<Object, D> dtoMap = new LinkedHashMap<>();
        for (Object[] row : rows) {
            dtoMap.put(row[0], (D) row[1]);
        }

        // hydrate collection attributes
        ProjectionMetadata<D> metadata = metadataRegistry.getMetadata(dtoType);
        if (metadata != null && !dtoMap.isEmpty()) {
            CollectionHydrator.hydrateCollections(entityType, metadata, dtoMap,
                    (type, attributes, ids) -> {
                        CriteriaQuery<Object[]> ccq = cb.createQuery(Object[].class);
                        Root<?> r = ccq.from(type);
                        Path<Object> rId = getIdPath(r);

                        List<Selection<?>> selections = new ArrayList<>();
                        selections.add(rId);

                        List<Class<?>> joinTypes = new ArrayList<>();
                        for (ProjectionMetadata.Attribute attribute : attributes) {
                            Join<?, ?> join = ((From<?, ?>) r).join(
                                    attribute.path(), JoinType.LEFT);
                            joinTypes.add(join.getJavaType());
                            ProjectionMetadata<?> nested = attribute.nested();
                            Path<Object> nestedId = getIdPath(join);
                            Selection<?> nestedSelection = projectionBuilder.construct(
                                    cb, (From<?, ?>) join, nested.dtoType());
                            selections.add(nestedId);
                            selections.add(nestedSelection);
                        }

                        ccq.multiselect(selections).where(rId.in(ids));
                        List<Object[]> resultRows = entityManager.createQuery(ccq).getResultList();
                        return new CollectionHydrator.FetchResult(resultRows, joinTypes);
                    });
        }

        // count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(entityType);
        countQuery.select(cb.count(countRoot));
        query.asSpecification().ifPresent(spec -> {
            Predicate predicate = spec.toPredicate(countRoot, countQuery, cb);
            if (predicate != null) {
                countQuery.where(predicate);
            }
        });
        long total = entityManager.createQuery(countQuery).getSingleResult();
        List<D> content = new ArrayList<>(dtoMap.values());

        return new ProjectionResult<>(content, total);
    }

    /**
     * Retrieves the ID path for the given entity type.
     * This method assumes the entity has a single ID attribute.
     *
     * @param from the 'From' object representing the root entity
     * @return a Path representing the ID of the entity
     */
    private Path<Object> getIdPath(From<?, ?> from) {
        EntityType<?> entityType = entityManager.getMetamodel().entity(from.getJavaType());
        String idName = entityType.getId(entityType.getIdType().getJavaType()).getName();
        return from.get(idName);
    }

    /**
     * Resolves a path relative to the root entity.
     * This method assumes the path is dot-separated, e.g. "parent.child.grandchild".
     *
     * @param root the root entity
     * @param property the dot-separated property path
     * @return a Path representing the resolved property
     */
    private Path<?> resolvePath(Root<?> root, String property) {
        String[] parts = property.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }
}

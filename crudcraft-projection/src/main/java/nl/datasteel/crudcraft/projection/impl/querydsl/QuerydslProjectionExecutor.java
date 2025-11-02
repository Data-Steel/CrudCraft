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

import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadata;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.projection.api.ProjectionResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.util.*;

/**
 * QueryDSL-based implementation of {@link ProjectionExecutor}.
 */
public class QuerydslProjectionExecutor implements ProjectionExecutor {

    private final JPAQueryFactory queryFactory;
    private final QuerydslProjectionBuilder projectionBuilder;
    private final ProjectionMetadataRegistry metadataRegistry;
    private final Metamodel metamodel;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Query factory is externally managed")
    public QuerydslProjectionExecutor(JPAQueryFactory queryFactory,
                                      Metamodel metamodel,
                                      QuerydslProjectionBuilder projectionBuilder,
                                      ProjectionMetadataRegistry metadataRegistry) {
        this.queryFactory = queryFactory;
        this.metamodel = metamodel;
        this.projectionBuilder = projectionBuilder;
        this.metadataRegistry = metadataRegistry;
    }

    @Override
    public <T, D> ProjectionResult<D> project(Class<T> entityType, Class<D> dtoType,
                                              ProjectionQuery<T> query) {
        PathBuilder<T> root = new PathBuilder<>(entityType, "root");
        String idName = getIdName(entityType); // root entity id must be resolvable
        PathBuilder<Object> idPath = root.get(idName);
        Expression<D> dtoExpr = projectionBuilder.construct(root, dtoType);

        JPAQuery<Tuple> jpaQuery = queryFactory.select(idPath, dtoExpr).from(root);

        applyFilter(jpaQuery, query.asPredicate());
        applySortingAndPaging(jpaQuery, root, query.pageable());

        List<Tuple> rows = jpaQuery.fetch();
        Map<Object, D> dtoMap = new LinkedHashMap<>();
        for (Tuple row : rows) {
            dtoMap.put(row.get(idPath), row.get(dtoExpr));
        }
        List<D> content = new ArrayList<>(dtoMap.values());

        ProjectionMetadata<D> metadata = metadataRegistry.getMetadata(dtoType);
        if (metadata != null && !dtoMap.isEmpty()) {
            //noinspection unchecked,rawtypes
            hydrateCollections(entityType, metadata, (Map) dtoMap);
        }

        JPAQuery<Long> countQuery = queryFactory.select(root.count()).from(root);
        applyFilter(countQuery, query.asPredicate());
        Long totalResult = countQuery.fetchOne();
        long total = totalResult != null ? totalResult : 0L;

        return new ProjectionResult<>(content, total);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void hydrateCollections(Class<?> entityType, ProjectionMetadata<?> metadata,
                                    Map<Object, Object> dtoMap) {
        hydrateCollections(entityType, metadata, dtoMap, new HashMap<>());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void hydrateCollections(Class<?> entityType, ProjectionMetadata<?> metadata,
                                    Map<Object, Object> dtoMap,
                                    Map<Class<?>, Set<Object>> visited) {
        Set<Object> visitedIds = visited.computeIfAbsent(entityType, k -> new HashSet<>());
        List<Object> ids = dtoMap.keySet().stream().filter(visitedIds::add).toList();
        if (ids.isEmpty()) {
            return;
        }

        List<ProjectionMetadata.Attribute> attributes = metadata.attributes().stream()
                .filter(ProjectionMetadata.Attribute::collection)
                .toList();
        if (attributes.isEmpty()) {
            return;
        }

        PathBuilder<?> root = new PathBuilder(entityType, "root");
        PathBuilder<Object> rootId = root.get(getIdName(entityType));

        for (ProjectionMetadata.Attribute attribute : attributes) {
            PathBuilder<?> join = resolvePath(root, attribute.path());
            Class<?> joinType = join.getType();

            // Try to resolve child id name, fall back to "id" when metamodel can't.
            String childIdName = safeGetIdName(joinType);
            PathBuilder<Object> childId = join.get(childIdName);

            ProjectionMetadata<?> nested = attribute.nested();
            if (nested == null) {
                continue;
            }
            Expression<?> childDto = projectionBuilder.construct(join, nested.dtoType());

            Map<Object, Map<Object, Object>> result = (Map) queryFactory.from(root)
                    .leftJoin(join)
                    .where(rootId.in(ids))
                    .transform(GroupBy.groupBy(rootId).as(GroupBy.map(childId, childDto)));

            for (Map.Entry<Object, Object> entry : dtoMap.entrySet()) {
                Map<Object, Object> children = result.getOrDefault(entry.getKey(), Collections.emptyMap());
                attribute.mutator().accept(entry.getValue(), new ArrayList<>(children.values()));
            }

            if (!result.isEmpty()) {
                Map<Object, Object> nestedMap = new LinkedHashMap<>();
                for (Map<Object, Object> m : result.values()) {
                    nestedMap.putAll(m);
                }
                hydrateCollections(joinType, nested, nestedMap, visited);
            }
        }
    }

    private void applyFilter(JPAQuery<?> query, Optional<Predicate> predicate) {
        predicate.ifPresent(query::where);
    }

    private void applySortingAndPaging(JPAQuery<?> query, PathBuilder<?> root, Pageable pageable) {
        if (pageable == null) return;

        if (pageable.getSort() != null) {
            for (Sort.Order order : pageable.getSort()) {
                String property = order.getProperty();

                // Resolve and validate the property's Java type
                Class<?> rawType = resolvePropertyJavaType(root.getType(), property);
                Class<?> boxedType = boxIfPrimitive(rawType);
                if (!Comparable.class.isAssignableFrom(boxedType)) {
                    throw new IllegalArgumentException("Cannot sort by non-comparable property: " + property);
                }

                @SuppressWarnings("unchecked")
                Expression<? extends Comparable<?>> propertyPath =
                        (Expression<? extends Comparable<?>>) root.getComparable(property, (Class<? extends Comparable<?>>) boxedType);

                query.orderBy(new OrderSpecifier<>(
                        order.isAscending() ? Order.ASC : Order.DESC,
                        propertyPath
                ));
            }
        }

        if (pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
    }

    /** Box primitives to their wrappers so Comparable checks work. */
    private static Class<?> boxIfPrimitive(Class<?> type) {
        if (type == null || !type.isPrimitive()) return type != null ? type : Object.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == boolean.class) return Boolean.class;
        if (type == char.class) return Character.class;
        if (type == short.class) return Short.class;
        if (type == byte.class) return Byte.class;
        return type;
    }

    private PathBuilder<?> resolvePath(PathBuilder<?> root, String property) {
        String[] parts = property.split("\\.");
        PathBuilder<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    private String getIdName(Class<?> type) {
        EntityType<?> entity = metamodel.entity(type);
        return entity.getId(entity.getIdType().getJavaType()).getName();
    }

    /**
     * Like {@link #getIdName(Class)} but never throws; falls back to "id" when unknown.
     */
    private String safeGetIdName(Class<?> type) {
        try {
            EntityType<?> entity = metamodel.entity(type);
            if (entity == null) {
                return "id";
            }
            return entity.getId(entity.getIdType().getJavaType()).getName();
        } catch (RuntimeException ex) {
            // IllegalArgumentException / NPE when type isn't a managed entity, etc.
            return "id";
        }
    }

    /**
     * Resolve the Java type for a (possibly nested) property path by:
     * 1) Reflecting fields on the Java classes in the path,
     * 2) Falling back to the JPA metamodel,
     * 3) If still unknown, return Object.class.
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    private Class<?> resolvePropertyJavaType(Class<?> rootType, String propertyPath) {
        Class<?> current = rootType;
        for (String segment : propertyPath.split("\\.")) {
            boolean resolved = false;

            // Try reflection
            try {
                Field f = current.getDeclaredField(segment);
                current = f.getType();
                resolved = true;
            } catch (NoSuchFieldException ignored) {
                // fall through
            }

            // Try JPA metamodel
            if (!resolved) {
                try {
                    EntityType<?> et = metamodel.entity(current);
                    if (et != null) {
                        // singular attribute path expected for sorting
                        current = et.getSingularAttribute(segment).getJavaType();
                        resolved = true;
                    }
                } catch (RuntimeException ignored) {
                    // fall through
                }
            }

            // Unknown segment: mark as Object, keep iterating (no branching at loop end)
            if (!resolved) {
                current = Object.class;
            }
        }
        return current;
    }
}

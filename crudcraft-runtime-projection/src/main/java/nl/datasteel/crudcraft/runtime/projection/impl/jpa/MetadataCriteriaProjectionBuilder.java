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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityAdapter;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.runtime.projection.mapping.ProjectionMapper;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;


/**
 * {@link CriteriaProjectionBuilder} that relies on pre-generated {@link ProjectionMetadata} instead
 * of reflection.
 */
public class MetadataCriteriaProjectionBuilder implements CriteriaProjectionBuilder {

    /**
     * The ProjectionMetadataRegistry used to access compile-time generated metadata. This registry
     * provides metadata for DTOs that are projectable.
     */
    private final ProjectionMetadataRegistry registry;

    /**
     * The ProjectionMapper used to construct selections when metadata is not available. This mapper
     * can be customized to change how DTOs are constructed from entity paths.
     */
    private final ProjectionMapper mapper;

    private final FieldSecurityAdapter fieldSecurityAdapter;

    /**
     * Constructs a MetadataCriteriaProjectionBuilder with the given registry. This constructor uses
     * a default ProjectionMapper for constructing selections when metadata is not available.
     *
     * @param registry the ProjectionMetadataRegistry to use for accessing compile-time generated
     *     metadata
     */
    public MetadataCriteriaProjectionBuilder(ProjectionMetadataRegistry registry) {
        this(registry, new ProjectionMapper(), FieldSecurityAdapter.NOOP);
    }

    /**
     * Constructs a MetadataCriteriaProjectionBuilder with the given registry and mapper. This
     * allows for custom mapping logic if needed.
     *
     * @param registry the ProjectionMetadataRegistry to use for accessing compile-time generated
     *     metadata
     * @param mapper the ProjectionMapper to use for constructing selections when metadata is not
     *     available
     */
    public MetadataCriteriaProjectionBuilder(
            ProjectionMetadataRegistry registry, ProjectionMapper mapper) {
        this(registry, mapper, FieldSecurityAdapter.NOOP);
    }

    /**
     * Constructs a MetadataCriteriaProjectionBuilder with registry, mapper and field-security
     * adapter.
     *
     * @param registry metadata registry
     * @param fieldSecurityAdapter adapter used to evaluate readable DTO fields
     */
    public MetadataCriteriaProjectionBuilder(
            ProjectionMetadataRegistry registry, FieldSecurityAdapter fieldSecurityAdapter) {
        this(registry, new ProjectionMapper(), fieldSecurityAdapter);
    }

    /**
     * Constructs a MetadataCriteriaProjectionBuilder with registry, mapper and field-security
     * adapter.
     *
     * @param registry metadata registry
     * @param mapper mapper used when no generated metadata is available
     * @param fieldSecurityAdapter adapter used to evaluate readable DTO fields
     */
    public MetadataCriteriaProjectionBuilder(
            ProjectionMetadataRegistry registry,
            ProjectionMapper mapper,
            FieldSecurityAdapter fieldSecurityAdapter) {
        this.registry = registry;
        this.mapper = mapper;
        this.fieldSecurityAdapter =
                fieldSecurityAdapter == null ? FieldSecurityAdapter.NOOP : fieldSecurityAdapter;
    }

    /**
     * Builds a JPA Criteria query that projects entity data into a DTO. This method constructs the
     * query using the provided CriteriaBuilder, entity type, DTO type, and ProjectionQuery.
     *
     * @param cb the CriteriaBuilder to use for constructing the query
     * @param entityType the type of the entity to project from
     * @param dtoType the type of the DTO to project into
     * @param query the ProjectionQuery containing additional query parameters
     * @param <T> the type of the entity
     * @param <D> the type of the DTO
     * @return a CriteriaQuery that projects entity data into a DTO
     */
    @Override
    public <T, D> CriteriaQuery<D> build(
            CriteriaBuilder cb, Class<T> entityType, Class<D> dtoType, ProjectionQuery<T> query) {
        CriteriaQuery<D> criteria = cb.createQuery(dtoType);
        var root = criteria.from(entityType);
        Selection<D> selection = construct(cb, root, dtoType);
        criteria.select(selection).distinct(true);
        return criteria;
    }

    /**
     * Constructs a JPA Criteria selection for the given DTO type. If the metadata is available, it
     * uses that to construct the selection; otherwise, it falls back to using the ProjectionMapper.
     *
     * @param cb the CriteriaBuilder to use for constructing selections
     * @param from the From object representing the root entity
     * @param dtoType the type of the DTO to project into
     * @param <D> the type of the DTO
     * @return a Selection that constructs the DTO from the entity paths
     */
    @Override
    public <D> Selection<D> construct(CriteriaBuilder cb, From<?, ?> from, Class<D> dtoType) {
        ProjectionMetadata<D> metadata = registry.getMetadata(dtoType);
        if (metadata != null) {
            return constructDto(cb, from, metadata, new HashMap<>());
        }
        return mapper.construct(cb, from, dtoType);
    }

    /**
     * Constructs a JPA Criteria selection for the given DTO type using the provided metadata. If
     * the metadata is not available, it falls back to using the ProjectionMapper.
     *
     * @param cb the CriteriaBuilder to use for constructing selections
     * @param from the From object representing the root entity
     * @param metadata the ProjectionMetadata for the DTO type
     * @param joins a map of existing joins to avoid duplicate joins
     * @param <D> the type of the DTO
     * @return a Selection that constructs the DTO from the entity paths
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private <D> Selection<D> constructDto(
            CriteriaBuilder cb,
            From<?, ?> from,
            ProjectionMetadata<D> metadata,
            Map<String, Join<?, ?>> joins) {
        List<Selection<?>> selections = new ArrayList<>();
        for (ProjectionMetadata.Attribute attribute : metadata.attributes()) {
            if (attribute.collection()) {
                continue;
            }
            if (!fieldSecurityAdapter.canReadField(metadata.dtoType(), attribute.dtoFieldName())) {
                selections.add(deniedSelection(cb, from, metadata, attribute, joins));
                continue;
            }
            ProjectionMetadata nested = attribute.nested();
            if (nested != null) {
                Join<?, ?> join = getOrCreateJoin(from, attribute.path(), joins);
                selections.add(constructDto(cb, join, nested, new HashMap<>()));
            } else {
                selections.add(resolvePath(from, attribute.path(), joins));
            }
        }
        return cb.construct(metadata.dtoType(), selections.toArray(new Selection[0]));
    }

    @SuppressWarnings("unchecked")
    private Selection<?> deniedSelection(
            CriteriaBuilder cb,
            From<?, ?> from,
            ProjectionMetadata<?> metadata,
            ProjectionMetadata.Attribute attribute,
            Map<String, Join<?, ?>> joins) {
        ProjectionMetadata<?> nested = attribute.nested();
        if (nested != null) {
            return cb.nullLiteral((Class<Object>) nested.dtoType());
        }
        Class<?> javaType = dtoFieldType(metadata.dtoType(), attribute.dtoFieldName());
        if (javaType == null) {
            Path<?> path = resolvePath(from, attribute.path(), joins);
            javaType = path.getJavaType();
        }
        if (javaType.isPrimitive()) {
            return cb.literal(defaultPrimitiveValue(javaType));
        }
        return cb.nullLiteral((Class<Object>) javaType);
    }

    private Class<?> dtoFieldType(Class<?> dtoType, String fieldName) {
        if (dtoType == null || fieldName == null || fieldName.isBlank()) {
            return null;
        }
        if (dtoType.isRecord()) {
            for (java.lang.reflect.RecordComponent component : dtoType.getRecordComponents()) {
                if (fieldName.equals(component.getName())) {
                    return component.getType();
                }
            }
        }
        Class<?> current = dtoType;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName).getType();
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private Object defaultPrimitiveValue(Class<?> primitiveType) {
        return switch (primitiveType.getName()) {
            case "boolean" -> false;
            case "byte" -> (byte) 0;
            case "short" -> (short) 0;
            case "int" -> 0;
            case "long" -> 0L;
            case "float" -> 0f;
            case "double" -> 0d;
            case "char" -> '\0';
            default -> null;
        };
    }

    /**
     * Resolve a path from the given From object using the specified path string. The path string
     * can contain nested attributes separated by dots (e.g., "parent.child").
     *
     * @param from the base From to resolve the path from
     * @param path the dot-separated path string
     * @param joins a map of existing joins to avoid duplicate joins
     * @return the resolved Path for the specified attribute
     */
    private Path<?> resolvePath(From<?, ?> from, String path, Map<String, Join<?, ?>> joins) {
        String[] parts = path.split("\\.");
        From<?, ?> current = from;
        for (int i = 0; i < parts.length - 1; i++) {
            current = getOrCreateJoin(current, parts[i], joins);
        }
        return current.get(parts[parts.length - 1]);
    }

    /**
     * Get or create a left join for the specified attribute. If the join already exists, it will be
     * returned; otherwise, a new join will be created.
     *
     * @param from the base From to join from
     * @param attribute the attribute to join on
     * @param joins a map of existing joins to avoid duplicate joins
     * @return the Join for the specified attribute
     */
    private Join<?, ?> getOrCreateJoin(
            From<?, ?> from, String attribute, Map<String, Join<?, ?>> joins) {
        return joins.computeIfAbsent(attribute, k -> from.join(attribute, JoinType.LEFT));
    }
}

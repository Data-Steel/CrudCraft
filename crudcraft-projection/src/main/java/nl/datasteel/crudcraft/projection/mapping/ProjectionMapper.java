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
package nl.datasteel.crudcraft.projection.mapping;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadata;

/**
 * Maps DTO classes to entity paths at runtime using reflection. When no
 * compile-time {@link ProjectionMetadata} is available this mapper constructs
 * the necessary projection expressions based on field names and
 * {@link nl.datasteel.crudcraft.annotations.fields.ProjectionField} annotations.
 */
public class ProjectionMapper {

    /**
     * The field resolver used to map DTO fields to entity attributes.
     * This resolver can be customized to change how fields are resolved.
     */
    private final ProjectionFieldResolver fieldResolver;

    /**
     * Constructs a ProjectionMapper using the default field resolver.
     * This resolver uses reflection to map DTO fields to entity attributes.
     * This constructor is useful when no compile-time metadata is available,
     * allowing for dynamic projection mapping at runtime.
     */
    public ProjectionMapper() {
        this.fieldResolver = new ProjectionFieldResolver();
    }

    /**
     * Constructs a ProjectionMapper with a custom field resolver.
     * This allows for custom resolution logic if needed.
     *
     * @param fieldResolver the resolver to use for mapping fields
     */
    public ProjectionMapper(ProjectionFieldResolver fieldResolver) {
        this.fieldResolver = fieldResolver;
    }

    /**
     * Construct a JPA Criteria selection for the given DTO type.
     */
    public <D> Selection<D> construct(CriteriaBuilder cb, From<?, ?> from, Class<D> dtoType) {
        List<ProjectionFieldResolver.FieldMapping> mappings = fieldResolver.resolve(dtoType);
        List<Selection<?>> selections = new ArrayList<>();
        for (ProjectionFieldResolver.FieldMapping mapping : mappings) {
            if (mapping.collection()) {
                selections.add(cb.literal(Collections.emptyList()));
            } else if (mapping.nestedType() != null) {
                From<?, ?> join = join(from, mapping.path());
                selections.add(construct(cb, join, mapping.nestedType()));
            } else {
                selections.add(resolvePath(from, mapping.path()));
            }
        }
        Selection[] array = selections.toArray(new Selection[0]);
        return cb.construct(dtoType, array);
    }

    /**
     * Construct a Querydsl expression for the given DTO type.
     */
    public <D> Expression<D> construct(PathBuilder<?> from, Class<D> dtoType) {
        List<ProjectionFieldResolver.FieldMapping> mappings = fieldResolver.resolve(dtoType);
        List<Expression<?>> expressions = new ArrayList<>();
        for (ProjectionFieldResolver.FieldMapping mapping : mappings) {
            if (mapping.collection()) {
                expressions.add(Expressions.nullExpression());
            } else if (mapping.nestedType() != null) {
                PathBuilder<?> join = resolvePathBuilder(from, mapping.path());
                expressions.add(construct(join, mapping.nestedType()));
            } else {
                expressions.add(resolvePathBuilder(from, mapping.path()));
            }
        }
        try {
            return Projections.constructor(dtoType, expressions.toArray(Expression[]::new));
        } catch (com.querydsl.core.types.ExpressionException ex) {
            // If Querydsl cannot resolve a suitable constructor (e.g. non-public
            // constructor), fall back to a null expression to avoid failing.
            return Expressions.nullExpression();
        }
    }

    /**
     * Join the given From with the specified path, creating a left join for each
     * segment in the path.
     *
     * @param from the base From to join from
     * @param path the path to join, e.g. "parent.child.grandchild"
     * @return the joined From
     */
    private From<?, ?> join(From<?, ?> from, String path) {
        String[] parts = path.split("\\.");
        From<?, ?> current = from;
        for (String part : parts) {
            current = current.join(part, JoinType.LEFT);
        }
        return current;
    }

    /**
     * Resolve a Path relative to the provided From.
     * This method assumes the path is relative to the current entity or join.
     */
    private Path<?> resolvePath(From<?, ?> from, String path) {
        String[] parts = path.split("\\.");
        From<?, ?> current = from;
        for (int i = 0; i < parts.length - 1; i++) {
            current = current.join(parts[i], JoinType.LEFT);
        }
        return current.get(parts[parts.length - 1]);
    }

    /**
     * Resolve a PathBuilder for the given path relative to the provided PathBuilder.
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


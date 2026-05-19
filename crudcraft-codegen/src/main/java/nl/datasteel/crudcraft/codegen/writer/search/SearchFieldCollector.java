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

package nl.datasteel.crudcraft.codegen.writer.search;

// CHECKSTYLE.SUPPRESS: LineLength for +1000 lines
// CHECKSTYLE.SUPPRESS: Indentation for +1000 lines

import com.palantir.javapoet.TypeName;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.exception.CodegenValidationException;
import nl.datasteel.crudcraft.codegen.reader.AnnotationModelReader;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;


/**
 * * Collects searchable fields from a model descriptor and its child models up to a specified
 * depth. .
 */
public class SearchFieldCollector {

    private final WriteContext ctx;

    /**
     * Creates a search field collector.
     *
     * @param ctx write context snapshot used for type lookups
     */
    public SearchFieldCollector(WriteContext ctx) {
        this.ctx = WriteContext.snapshotOf(ctx);
    }

    /**
     * Collects searchable fields from the given model descriptor and its child models up to the
     * specified depth.
     *
     * <p>Depth semantics: - 0 → nothing - 1 → only root searchable fields - n → root + nested up to
     * (n-1) hops
     *
     * @param root root model descriptor
     * @param depth traversal depth
     * @return flattened searchable fields
     */
    public List<SearchField> collect(ModelDescriptor root, int depth) {
        if (root == null) {
            throw new CodegenValidationException(
                    "SearchFieldCollector", "root model must not be null");
        }
        if (depth <= 0) {
            return List.of();
        }

        List<SearchField> result = new ArrayList<>();
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(new Node(root, "", depth, Set.of(modelId(root))));
        Set<String> seen = new HashSet<>();

        while (!stack.isEmpty()) {
            Node node = stack.pop();
            ModelDescriptor md = node.md();
            String prefix = node.prefix();
            int remaining = node.depth();
            String id = modelId(md) + "@" + prefix;
            if (!seen.add(id)) {
                continue;
            }

            for (FieldDescriptor fd : md.getFields()) {
                if (!fd.isSearchable()) {
                    continue;
                }

                String path = prefix.isEmpty() ? fd.getName() : prefix + "." + fd.getName();
                String prop = toProperty(path);

                // Get all configured operators for the field
                //  - if configured operators list is non-empty, use all of them
                //  - otherwise default to EQUALS only
                List<SearchOperator> operators = fd.getSearchOperators();

                // Recurse into CRUD-target children if we can still go deeper
                String candidateFqcn = fd.getTargetType();
                if (candidateFqcn == null) {
                    candidateFqcn = TypeName.get(fd.getType()).toString();
                }
                var te = ctx.findTypeElement(candidateFqcn);
                boolean willRecurse = false;
                boolean isEntity = false;
                if (te != null) {
                    // Check if the type is actually a CrudCraft entity or JPA entity
                    // by looking for @CrudCrafted or @Entity annotations
                    boolean hasCrudCraftedAnnotation =
                            te.getAnnotation(
                                            nl.datasteel.crudcraft.annotations.classes.CrudCrafted
                                                    .class)
                                    != null;
                    boolean hasEntityAnnotation =
                            te.getAnnotation(jakarta.persistence.Entity.class) != null;

                    if (hasCrudCraftedAnnotation || hasEntityAnnotation) {
                        try {
                            ModelDescriptor child = AnnotationModelReader.parse(te, ctx.env());
                            String childId = modelId(child);
                            if (node.ancestry().contains(childId)) {
                                ctx.env()
                                        .getMessager()
                                        .printMessage(
                                                Diagnostic.Kind.NOTE,
                                                "Skipping cyclical @Searchable path " + path);
                                continue;
                            }
                            // If we got here, it's an entity that could be recursed into
                            isEntity = true;
                            int availableDepth = remaining - 1;
                            int configuredDepth = fd.getSearchDepth();
                            int next =
                                    Math.min(
                                            configuredDepth > 0 ? configuredDepth : availableDepth,
                                            availableDepth);
                            if (next > 0) {
                                willRecurse = true;
                                ctx.env()
                                        .getMessager()
                                        .printMessage(
                                                Diagnostic.Kind.NOTE,
                                                "Collecting search fields for "
                                                        + child.getName()
                                                        + " at depth "
                                                        + next);
                                Set<String> childAncestry = new HashSet<>(node.ancestry());
                                childAncestry.add(childId);
                                stack.push(new Node(child, path, next, Set.copyOf(childAncestry)));
                            }
                        } catch (Exception e) {
                            // Not a valid entity, treat as regular type
                            isEntity = false;
                        }
                    }
                }

                // Only add the parent entity field itself if we're NOT recursing into it
                // AND it's not an entity type (to avoid exposing full entity schemas)
                // When we recurse, we only want the flattened nested fields, not the parent
                // entity
                if (!willRecurse && !isEntity) {
                    // Generate one SearchField entry for each operator
                    for (SearchOperator op : operators) {
                        String property =
                                switch (op) {
                                    case SIZE_EQUALS, SIZE_GT, SIZE_LT -> prop + "Size";
                                    default -> prop;
                                };

                        result.add(
                                new SearchField(fd, property, buildCriteriaPath(path), op));
                    }
                }
            }
        }

        return List.copyOf(result);
    }

    /**
     * Converts a dotted entity path to the generated search-request property name.
     *
     * @param path dotted entity path
     * @return flattened request property name
     */
    static String toProperty(String path) {
        String[] parts = path.split("\\.");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return sb.toString();
    }

    /**
     * Builds a Criteria API path expression from a dotted entity path.
     *
     * @param path dotted entity path
     * @return Criteria API path expression
     */
    static String buildCriteriaPath(String path) {
        String[] parts = path.split("\\.");
        StringBuilder sb = new StringBuilder("root");
        for (int i = 0; i < parts.length; i++) {
            if (i < parts.length - 1) {
                sb.append(".join(\"").append(parts[i]).append("\")");
            } else {
                sb.append(".get(\"").append(parts[i]).append("\")");
            }
        }
        return sb.toString();
    }

    private static String modelId(ModelDescriptor descriptor) {
        return descriptor.getPackageName() + "." + descriptor.getName();
    }

    private record Node(ModelDescriptor md, String prefix, int depth, Set<String> ancestry) {}
}

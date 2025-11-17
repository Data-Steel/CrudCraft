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
package nl.datasteel.crudcraft.codegen.writer.search;

import com.squareup.javapoet.TypeName;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.reader.AnnotationModelReader;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;

/**
 * Collects searchable fields from a model descriptor and its child models up to a specified depth.
 */
public class SearchFieldCollector {

    private final WriteContext ctx;

    public SearchFieldCollector(WriteContext ctx) {
        this.ctx = WriteContext.snapshotOf(ctx);
    }

    /**
     * Collects searchable fields from the given model descriptor and its child models up
     * to the specified depth.
     *
     * Depth semantics:
     *  - 0 → nothing
     *  - 1 → only root searchable fields
     *  - n → root + nested up to (n-1) hops
     */
    public List<SearchField> collect(ModelDescriptor root, int depth) {
        if (root == null) throw new NullPointerException("root model must not be null");
        if (depth <= 0) return List.of();

        List<SearchField> result = new ArrayList<>();
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(new Node(root, "", depth));
        Set<String> seen = new HashSet<>();

        while (!stack.isEmpty()) {
            Node node = stack.pop();
            ModelDescriptor md = node.md();
            String prefix = node.prefix();
            int remaining = node.depth();
            String id = md.getPackageName() + "." + md.getName() + "@" + prefix;
            if (!seen.add(id)) continue;

            // Only add for this level if we have budget
            if (remaining > 0) {
                for (FieldDescriptor fd : md.getFields()) {
                    if (!fd.isSearchable()) continue;

                    String path = prefix.isEmpty() ? fd.getName() : prefix + "." + fd.getName();
                    String prop = SearchPathUtil.toProperty(path);

                    // Choose a SINGLE operator for the field:
                    //  - if configured operators list is non-empty, pick the first
                    //  - otherwise default to EQUALS
                    SearchOperator op = fd.getSearchOperators().isEmpty()
                            ? SearchOperator.EQUALS
                            : fd.getSearchOperators().get(0);

                    String property = switch (op) {
                        case SIZE_EQUALS, SIZE_GT, SIZE_LT -> prop + "Size";
                        default -> prop;
                    };

                    // Check if this field will recurse into nested searchable fields
                    boolean willRecurse = false;
                    boolean hasExplicitDepth = fd.getSearchDepth() > 1;
                    
                    // Calculate current path depth (number of dots in prefix + 1)
                    int pathDepth = prefix.isEmpty() ? 0 : prefix.split("\\.").length;
                    // Limit total path depth to prevent infinite recursion in circular references
                    int MAX_PATH_DEPTH = 2;
                    
                    // Only recurse if we have depth budget remaining AND path isn't too deep
                    // OR if the field has explicit depth annotation (which we honor up to MAX_PATH_DEPTH)
                    if (pathDepth < MAX_PATH_DEPTH && (remaining > 1 || (hasExplicitDepth && remaining > 0))) {
                        String candidateFqcn = fd.getTargetType();
                        if (candidateFqcn == null) {
                            candidateFqcn = TypeName.get(fd.getType()).toString();
                        }
                        
                        var te = ctx.findTypeElement(candidateFqcn);
                        
                        if (te != null) {
                            ModelDescriptor child = AnnotationModelReader.parse(te, ctx.env());
                            
                            // Calculate next depth:
                            // - If field has explicit depth > 1, use it - 1 (going one level deeper)
                            // - Otherwise use remaining - 1 (normal depth budget)
                            int next;
                            if (hasExplicitDepth) {
                                next = fd.getSearchDepth() - 1;
                            } else {
                                next = Math.max(0, remaining - 1);
                            }
                            
                            // Check if the child entity has any searchable fields
                            boolean hasSearchableFields = child.getFields().stream()
                                    .anyMatch(FieldDescriptor::isSearchable);
                            
                            if (next > 0 && hasSearchableFields) {
                                willRecurse = true;
                                stack.push(new Node(child, path, next));
                            }
                        }
                    }

                    // Only add the field itself if:
                    // 1. It's NOT a relationship field (e.g., primitive, String, enum), OR
                    // 2. It's a relationship field but won't recurse into nested fields
                    // This prevents generating Set<Author>, Set<Tag> in SearchRequest when
                    // we're going to flatten author.name -> authorName instead.
                    boolean isRelationship = fd.getRelType() != RelationshipType.NONE;
                    if (!isRelationship || !willRecurse) {
                        result.add(new SearchField(
                                fd,
                                property,
                                SearchPathUtil.buildPath(path),
                                op
                        ));
                    }
                }
            }
        }

        return List.copyOf(result);
    }

    private record Node(ModelDescriptor md, String prefix, int depth) {}
}

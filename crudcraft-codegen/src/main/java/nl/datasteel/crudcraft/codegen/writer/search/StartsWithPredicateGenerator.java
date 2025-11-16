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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import nl.datasteel.crudcraft.annotations.SearchOperator;

/**
 * Generates a predicate for checking if a string field starts with a given value.
 * This is used in search requests to filter results based on the starting characters
 * of a string property.
 */
public class StartsWithPredicateGenerator
        extends AbstractPredicateGenerator
        implements PredicateGenerator {

    /**
     * Generates a CodeBlock that checks if the search request's property
     * starts with a specified value.
     *
     * @param f the search field containing the property and operator information
     * @return a CodeBlock representing the predicate logic for the starts with condition
     */
    @Override
    public CodeBlock generate(SearchField f) {
        String m = cap(f.property());
        ClassName predicate = ClassName.get("jakarta.persistence.criteria", "Predicate");
        return CodeBlock.builder()
                .beginControlFlow("if (request.get$L() != null && !request.get$L().isEmpty() && request.get$LOp() "
                        + " == $T.STARTS_WITH)",
                        m,
                        m,
                        m,
                        SearchOperator.class)
                .addStatement("$T[] predicates = new $T[request.get$L().size()]",
                        predicate,
                        predicate,
                        m)
                .addStatement("int i = 0")
                .beginControlFlow("for (String value : request.get$L())", m)
                .addStatement("predicates[i++] = cb.like($L, value + \"%\")",
                        f.path())
                .endControlFlow()
                .addStatement("p = cb.and(p, cb.or(predicates))")
                .endControlFlow()
                .build();
    }
}

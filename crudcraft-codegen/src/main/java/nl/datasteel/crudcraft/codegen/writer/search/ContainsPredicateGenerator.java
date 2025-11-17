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
 * Generates predicates for the {@code CONTAINS} operator.
 */
public class ContainsPredicateGenerator
        extends AbstractPredicateGenerator
        implements PredicateGenerator {

    @Override
    public CodeBlock generate(SearchField f) {
        String m = cap(f.property());
        ClassName predicate = ClassName.get("jakarta.persistence.criteria", "Predicate");
        return CodeBlock.builder()
                .beginControlFlow(
                        "if (request.get$L() != null && !request.get$L().isEmpty() && request.get$LOp() == $T.CONTAINS)",
                        m,
                        m,
                        m,
                        SearchOperator.class
                )
                .addStatement("$T[] predicates = new $T[request.get$L().size()]",
                        predicate,
                        predicate,
                        m)
                .addStatement("int i = 0")
                .beginControlFlow("for (String value : request.get$L())", m)
                .addStatement("predicates[i++] = cb.like($L, \"%\" + value + \"%\")",
                        f.path())
                .endControlFlow()
                .addStatement("p = cb.and(p, cb.or(predicates))")
                .endControlFlow()
                .build();
    }
}

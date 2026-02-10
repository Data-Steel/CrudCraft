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
 * Generates predicates for the {@code CONTAINS_KEY} operator.
 */
public class ContainsKeyPredicateGenerator
        extends AbstractPredicateGenerator
        implements PredicateGenerator {

    @Override
    public CodeBlock generate(SearchField f) {
        String m = cap(f.property());
        return CodeBlock.builder()
                .beginControlFlow(
                        "if (request.get$L() != null && request.get$LOp() == $T.CONTAINS_KEY)",
                        m,
                        m,
                        SearchOperator.class
                )
                .addStatement("p = logic == $T.AND ? cb.and(p, cb.isMember(request.get$L(), $L.keySet())) : cb.or(p, cb.isMember(request.get$L(), $L.keySet()))",
                        ClassName.get("nl.datasteel.crudcraft.runtime.search", "SearchLogic"),
                        m,
                        f.path(),
                        m,
                        f.path())
                .endControlFlow()
                .build();
    }
}

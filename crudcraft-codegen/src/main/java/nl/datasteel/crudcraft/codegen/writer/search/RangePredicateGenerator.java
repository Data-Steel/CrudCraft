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

import com.squareup.javapoet.CodeBlock;
import nl.datasteel.crudcraft.annotations.SearchOperator;

/**
 * Generates a predicate for checking if a field's value falls within a specified range
 * in a search request. This is used to filter results based on a range of values for a property.
 */
public class RangePredicateGenerator
        extends AbstractPredicateGenerator
        implements PredicateGenerator {

    /**
     * Generates a CodeBlock that checks if the search request's property
     * has a range defined by start and end values.
     *
     * @param f the search field containing the property and operator information
     * @return a CodeBlock representing the predicate logic for the range condition
     */
    @Override
    public CodeBlock generate(SearchField f) {
        String m = cap(f.property());
        return CodeBlock.builder()
                .beginControlFlow(
                        "if (request.get$LStart() != null "
                                + "&& request.get$LEnd() != null && request.get$LOp() "
                                + "== $T.RANGE)",
                        m,
                        m,
                        m,
                        SearchOperator.class)
                .addStatement(
                        combinePredicateStatement("cb.between($L, request.get$LStart(), request.get$LEnd())"),
                        f.path(),
                        m,
                        m)
                .endControlFlow()
                .build();
    }
}

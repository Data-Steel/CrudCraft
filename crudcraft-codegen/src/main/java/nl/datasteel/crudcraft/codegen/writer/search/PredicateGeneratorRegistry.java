/**
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
package nl.datasteel.crudcraft.codegen.writer.search;

import java.util.EnumMap;
import nl.datasteel.crudcraft.annotations.SearchOperator;

/**
 * Registry for predicate generators that maps search operators to their
 * corresponding predicate generator implementations. This allows for dynamic
 * retrieval of the appropriate generator based on the search operator used in
 * a search request.
 */
public class PredicateGeneratorRegistry {

    /**
     * Private constructor to prevent instantiation of the registry.
     * This class is designed to be a utility class
     */
    private PredicateGeneratorRegistry() {
    }

    /**
     * A map that associates each {@link SearchOperator} with its corresponding
     * {@link PredicateGenerator} implementation. This allows for quick lookup
     * of the appropriate generator based on the operator used in a search request.
     */
    private static final EnumMap<SearchOperator, PredicateGenerator> MAP =
            new EnumMap<>(SearchOperator.class);

    static {
        MAP.put(SearchOperator.EQUALS, new EqualsPredicateGenerator());
        MAP.put(SearchOperator.NOT_EQUALS, new NotEqualsPredicateGenerator());
        MAP.put(SearchOperator.CONTAINS, new ContainsPredicateGenerator());
        MAP.put(SearchOperator.STARTS_WITH, new StartsWithPredicateGenerator());
        MAP.put(SearchOperator.ENDS_WITH, new EndsWithPredicateGenerator());
        MAP.put(SearchOperator.IN, new InPredicateGenerator());
        MAP.put(SearchOperator.NOT_IN, new NotInPredicateGenerator());
        MAP.put(SearchOperator.REGEX, new RegexPredicateGenerator());
        MAP.put(SearchOperator.GT, new GreaterThanPredicateGenerator());
        MAP.put(SearchOperator.GTE, new GreaterThanOrEqualPredicateGenerator());
        MAP.put(SearchOperator.LT, new LessThanPredicateGenerator());
        MAP.put(SearchOperator.LTE, new LessThanOrEqualPredicateGenerator());
        MAP.put(SearchOperator.RANGE, new RangePredicateGenerator());
        MAP.put(SearchOperator.BEFORE, new BeforePredicateGenerator());
        MAP.put(SearchOperator.AFTER, new AfterPredicateGenerator());
        MAP.put(SearchOperator.BETWEEN, new BetweenPredicateGenerator());
        MAP.put(SearchOperator.IS_EMPTY, new IsEmptyPredicateGenerator());
        MAP.put(SearchOperator.NOT_EMPTY, new NotEmptyPredicateGenerator());
        MAP.put(SearchOperator.SIZE_EQUALS,
                new SizeEqualsPredicateGenerator());
        MAP.put(SearchOperator.SIZE_GT,
                new SizeGtPredicateGenerator());
        MAP.put(SearchOperator.SIZE_LT,
                new SizeLtPredicateGenerator());
        MAP.put(SearchOperator.CONTAINS_ALL,
                new ContainsAllPredicateGenerator());
        MAP.put(SearchOperator.CONTAINS_KEY,
                new ContainsKeyPredicateGenerator());
        MAP.put(SearchOperator.CONTAINS_VALUE,
                new ContainsValuePredicateGenerator());
    }

    /**
     * Retrieves the appropriate {@link PredicateGenerator} for the given
     * {@link SearchOperator}. This method allows for dynamic selection of the
     * generator based on the operator used in a search request.
     *
     * @param op the search operator for which to retrieve the predicate generator
     * @return the corresponding predicate generator,
     *         or null if no generator is found for the operator
     */
    public static PredicateGenerator of(SearchOperator op) {
        return MAP.get(op);
    }
}

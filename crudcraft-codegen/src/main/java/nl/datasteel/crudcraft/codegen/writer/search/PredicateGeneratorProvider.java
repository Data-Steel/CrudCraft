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

import java.util.Map;
import nl.datasteel.crudcraft.annotations.SearchOperator;


/** ServiceLoader extension point for overriding or adding predicate generators. */
public interface PredicateGeneratorProvider {

    /**
     * Returns predicate generators keyed by supported operator.
     *
     * @return operator-to-generator mapping
     */
    Map<SearchOperator, PredicateGenerator> generators();
}

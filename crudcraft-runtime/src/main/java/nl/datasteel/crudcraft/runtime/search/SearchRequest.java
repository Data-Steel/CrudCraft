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
package nl.datasteel.crudcraft.runtime.search;

import com.querydsl.core.types.Predicate;
import org.springframework.data.jpa.domain.Specification;

/**
 * Marker interface for generated search request objects that can produce
 * a QueryDSL {@link Predicate} for searching, while still supporting
 * a JPA {@link Specification}.
 */
public interface SearchRequest<T> {
    /**
     * Returns a QueryDSL predicate representing the search criteria.
     */
    default Predicate toPredicate() {
        return null;
    }

    /**
     * Returns a JPA specification representing the search criteria.
     */
    Specification<T> toSpecification();
}

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
package nl.datasteel.crudcraft.projection.api;

import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.springframework.data.jpa.domain.Specification;
import static org.junit.jupiter.api.Assertions.*;

class FilterCriteriaTest {

    @Test
    void defaultMethodsReturnEmpty() {
        FilterCriteria<Object> criteria = new FilterCriteria<>() {};
        assertTrue(criteria.asSpecification().isEmpty());
        assertTrue(criteria.asPredicate().isEmpty());
    }

    @Test
    void ofSpecificationWrapsSpecification() {
        Specification<String> spec = (root, query, cb) -> null;
        FilterCriteria<String> criteria = FilterCriteria.ofSpecification(spec);
        assertEquals(spec, criteria.asSpecification().orElseThrow());
        assertTrue(criteria.asPredicate().isEmpty());
    }

    @Test
    void ofPredicateWrapsPredicate() {
        Predicate predicate = mock(Predicate.class);
        FilterCriteria<String> criteria = FilterCriteria.ofPredicate(predicate);
        assertSame(predicate, criteria.asPredicate().orElseThrow());
        assertTrue(criteria.asSpecification().isEmpty());
    }
}

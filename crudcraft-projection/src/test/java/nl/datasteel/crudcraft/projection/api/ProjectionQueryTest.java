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
package nl.datasteel.crudcraft.projection.api;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import static org.junit.jupiter.api.Assertions.*;

class ProjectionQueryTest {

    static class TestFilter<T> implements FilterCriteria<T> {
        private final Optional<?> spec;
        private final Optional<?> predicate;
        TestFilter(Optional<?> spec, Optional<?> predicate) {
            this.spec = spec;
            this.predicate = predicate;
        }
        @Override
        public Optional asSpecification() {
            return spec;
        }
        @Override
        public Optional asPredicate() {
            return predicate;
        }
    }

    @Test
    void ofCreatesProjectionQuery() {
        FilterCriteria<String> filter = new TestFilter<>(Optional.empty(), Optional.empty());
        ProjectionQuery<String> q = ProjectionQuery.of(filter, PageRequest.of(0, 10));
        assertSame(filter, q.filter());
        assertEquals(PageRequest.of(0,10), q.pageable());
    }

    @Test
    void asSpecificationDelegatesToFilter() {
        var spec = Optional.of("spec");
        ProjectionQuery<String> q = new ProjectionQuery<>(new TestFilter<>(spec, Optional.empty()), PageRequest.of(0,1));
        assertEquals(spec, q.asSpecification());
        assertTrue(q.asPredicate().isEmpty());
    }

    @Test
    void asPredicateDelegatesToFilter() {
        var pred = Optional.of("pred");
        ProjectionQuery<String> q = new ProjectionQuery<>(new TestFilter<>(Optional.empty(), pred), PageRequest.of(0,1));
        assertEquals(pred, q.asPredicate());
        assertTrue(q.asSpecification().isEmpty());
    }
}

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
package nl.datasteel.crudcraft.runtime.search;

import static org.junit.jupiter.api.Assertions.*;

import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

class SearchRequestTest {

    static class DummySearch implements SearchRequest<Object> {
        private final Specification<Object> spec;
        DummySearch(Specification<Object> spec) { this.spec = spec; }
        @Override public Specification<Object> toSpecification() { return spec; }
    }

    @Test
    void defaultPredicateIsNull() {
        SearchRequest<Object> req = new DummySearch((root, query, cb) -> cb.conjunction());
        assertNull(req.toPredicate());
    }

    @Test
    void specificationIsReturned() {
        Specification<Object> spec = (root, query, cb) -> cb.disjunction();
        DummySearch req = new DummySearch(spec);
        assertSame(spec, req.toSpecification());
    }
}

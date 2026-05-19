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

package nl.datasteel.crudcraft.runtime.projection.api;

import java.util.ArrayList;
import java.util.List;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ProjectionApiTest {

    @Test
    void defaultFilterCriteriaReturnsEmptySpecification() {
        FilterCriteria<Object> criteria = new FilterCriteria<>() {};

        assertTrue(criteria.asSpecification().isEmpty());
    }

    @Test
    void specificationFilterCriteriaReturnsProvidedSpecification() {
        Specification<Object> specification = (root, query, cb) -> cb.conjunction();

        FilterCriteria<Object> criteria = FilterCriteria.ofSpecification(specification);

        assertTrue(criteria.asSpecification().isPresent());
        assertSame(specification, criteria.asSpecification().orElseThrow());
    }

    @Test
    void specificationFilterCriteriaTreatsNullSpecificationAsEmpty() {
        FilterCriteria<Object> criteria = FilterCriteria.ofSpecification(null);

        assertTrue(criteria.asSpecification().isEmpty());
    }

    @Test
    void projectionQueryDelegatesToFilterAndStoresPageable() {
        Specification<Object> specification = (root, query, cb) -> cb.conjunction();
        Pageable pageable = PageRequest.of(1, 5);

        ProjectionQuery<Object> query =
                ProjectionQuery.of(FilterCriteria.ofSpecification(specification), pageable);

        assertEquals(pageable, query.pageable());
        assertTrue(query.asSpecification().isPresent());
        assertSame(specification, query.asSpecification().orElseThrow());
    }

    @Test
    void projectionQueryTreatsNullFilterAsNoop() {
        ProjectionQuery<Object> query = ProjectionQuery.of(null, null);

        assertTrue(query.asSpecification().isEmpty());
    }

    @Test
    void projectionResultPerformsDefensiveCopies() {
        List<String> source = new ArrayList<>(List.of("alpha"));
        ProjectionResult<String> result = new ProjectionResult<>(source, 4L);
        source.add("beta");

        assertEquals(List.of("alpha"), result.content());
        assertEquals(4L, result.totalElements());
        assertThrows(UnsupportedOperationException.class, () -> result.content().add("gamma"));
        assertFalse(result.content().isEmpty());
    }

    @Test
    void attributeDefaultDtoFieldNameFallsBackToPath() {
        ProjectionMetadata.Attribute attribute =
                new ProjectionMetadata.Attribute() {
                    @Override
                    public String path() {
                        return "entity.path";
                    }

                    @Override
                    public ProjectionMetadata<?> nested() {
                        return null;
                    }

                    @Override
                    public boolean collection() {
                        return false;
                    }

                    @Override
                    public java.util.function.BiConsumer<Object, List<?>> mutator() {
                        return null;
                    }
                };

        assertEquals("entity.path", attribute.dtoFieldName());
    }
}

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

package nl.datasteel.crudcraft.runtime.projection.impl.jpa;

import java.util.List;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionResult;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SuppressWarnings({"unchecked", "rawtypes"})
class JpaCriteriaProjectionAdapterTest {

    @Test
    void supportsReturnsTrueOnlyWhenMetadataExists() {
        ProjectionExecutor executor = mock(ProjectionExecutor.class);
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        when(registry.getMetadata(ProjectionDto.class)).thenReturn(mock(ProjectionMetadata.class));
        when(registry.getMetadata(String.class)).thenReturn(null);
        JpaCriteriaProjectionAdapter adapter = new JpaCriteriaProjectionAdapter(executor, registry);

        assertTrue(adapter.supports(ProjectionDto.class));
        assertFalse(adapter.supports(String.class));
    }

    @Test
    void projectPageDelegatesAndWrapsResultAsPage() {
        ProjectionExecutor executor = mock(ProjectionExecutor.class);
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        JpaCriteriaProjectionAdapter adapter = new JpaCriteriaProjectionAdapter(executor, registry);
        PageRequest pageable = PageRequest.of(0, 2);
        Specification<Entity> specification = (root, query, cb) -> cb.conjunction();

        when(executor.project(any(), any(), any()))
                .thenReturn(new ProjectionResult<>(List.of(new ProjectionDto("a")), 5L));

        Page<ProjectionDto> page =
                adapter.projectPage(Entity.class, ProjectionDto.class, specification, pageable);

        assertEquals(1, page.getContent().size());
        assertEquals(5L, page.getTotalElements());
        assertEquals(pageable, page.getPageable());

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<ProjectionQuery<Entity>> captor =
                (ArgumentCaptor) ArgumentCaptor.forClass(ProjectionQuery.class);
        verify(executor).project(any(), any(), captor.capture());
        ProjectionQuery<Entity> query = captor.getValue();
        assertEquals(pageable, query.pageable());
        assertTrue(query.asSpecification().isPresent());
    }

    @Test
    void projectListDelegatesUsingUnpagedAndNoSpecificationWhenNullIsProvided() {
        ProjectionExecutor executor = mock(ProjectionExecutor.class);
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        JpaCriteriaProjectionAdapter adapter = new JpaCriteriaProjectionAdapter(executor, registry);
        when(executor.project(any(), any(), any()))
                .thenReturn(new ProjectionResult<>(List.of(new ProjectionDto("x")), 1L));

        List<ProjectionDto> projected =
                adapter.projectList(Entity.class, ProjectionDto.class, null);

        assertEquals(1, projected.size());
        assertEquals("x", projected.getFirst().value);

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<ProjectionQuery<Entity>> captor =
                (ArgumentCaptor) ArgumentCaptor.forClass(ProjectionQuery.class);
        verify(executor).project(any(), any(), captor.capture());
        ProjectionQuery<Entity> query = captor.getValue();
        assertEquals(Pageable.unpaged(), query.pageable());
        assertTrue(query.asSpecification().isEmpty());
    }

    @Test
    void projectPageTreatsNullPageableAsUnpaged() {
        ProjectionExecutor executor = mock(ProjectionExecutor.class);
        ProjectionMetadataRegistry registry = mock(ProjectionMetadataRegistry.class);
        JpaCriteriaProjectionAdapter adapter = new JpaCriteriaProjectionAdapter(executor, registry);
        when(executor.project(any(), any(), any()))
                .thenReturn(new ProjectionResult<>(List.of(new ProjectionDto("z")), 1L));

        Page<ProjectionDto> page =
                adapter.projectPage(Entity.class, ProjectionDto.class, null, null);

        assertEquals(1, page.getContent().size());
        assertEquals(Pageable.unpaged(), page.getPageable());

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<ProjectionQuery<Entity>> captor =
                (ArgumentCaptor) ArgumentCaptor.forClass(ProjectionQuery.class);
        verify(executor).project(any(), any(), captor.capture());
        ProjectionQuery<Entity> query = captor.getValue();
        assertEquals(Pageable.unpaged(), query.pageable());
    }

    static final class Entity {}

    static final class ProjectionDto {
        private final String value;

        ProjectionDto(String value) {
            this.value = value;
        }
    }
}

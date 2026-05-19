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
import nl.datasteel.crudcraft.runtime.projection.api.FilterCriteria;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionResult;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.runtime.service.projection.ProjectionAdapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


/** Core projection SPI adapter backed by the JPA Criteria projection executor. */
public class JpaCriteriaProjectionAdapter implements ProjectionAdapter {

    private final ProjectionExecutor projectionExecutor;
    private final ProjectionMetadataRegistry metadataRegistry;

    /**
     * Creates a new adapter backed by the provided projection services.
     *
     * @param projectionExecutor projection executor implementation
     * @param metadataRegistry registry used to detect supported projection types
     */
    public JpaCriteriaProjectionAdapter(
            ProjectionExecutor projectionExecutor, ProjectionMetadataRegistry metadataRegistry) {
        this.projectionExecutor = projectionExecutor;
        this.metadataRegistry = metadataRegistry;
    }

    @Override
    public boolean supports(Class<?> projectionType) {
        return metadataRegistry.getMetadata(projectionType) != null;
    }

    @Override
    public <T, P> Page<P> projectPage(
            Class<T> entityType,
            Class<P> projectionType,
            Specification<T> specification,
            Pageable pageable) {
        Pageable effectivePageable = pageable == null ? Pageable.unpaged() : pageable;
        ProjectionResult<P> result =
                project(entityType, projectionType, specification, effectivePageable);
        return new PageImpl<>(result.content(), effectivePageable, result.totalElements());
    }

    @Override
    public <T, P> List<P> projectList(
            Class<T> entityType, Class<P> projectionType, Specification<T> specification) {
        ProjectionResult<P> result =
                project(entityType, projectionType, specification, Pageable.unpaged());
        return result.content();
    }

    private <T, P> ProjectionResult<P> project(
            Class<T> entityType,
            Class<P> projectionType,
            Specification<T> specification,
            Pageable pageable) {
        FilterCriteria<T> filter =
                specification == null
                        ? new FilterCriteria<>() {}
                        : FilterCriteria.ofSpecification(specification);
        ProjectionQuery<T> query = ProjectionQuery.of(filter, pageable);
        return projectionExecutor.project(entityType, projectionType, query);
    }
}

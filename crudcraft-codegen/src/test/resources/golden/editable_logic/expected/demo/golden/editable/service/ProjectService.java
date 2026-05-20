package demo.golden.editable.service;

import demo.golden.editable.Project;
import demo.golden.editable.dto.ref.ProjectRef;
import demo.golden.editable.dto.request.ProjectRequestDto;
import demo.golden.editable.dto.response.ProjectResponseDto;
import demo.golden.editable.mapper.ProjectMapper;
import demo.golden.editable.meta.ProjectRelationshipMeta;
import demo.golden.editable.repository.ProjectRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated Service layer stub for Project.
 * @CrudCraft:generated
 * @CrudCraft:editable
 *
 * This Service stub extends CrudCraft's base implementation. Override protected hooks and add custom endpoints here. Avoid overriding generated public endpoint methods unless you intentionally replace the HTTP contract.
 *
 * You are allowed to modify this file. It extends CrudCraft's abstract base (ProjectServiceBase)
 * which already implements full CRUD logic.
 *
 * This file was generated only once. CrudCraft will not overwrite it in future
 * builds. If you delete it, it will be regenerated.
 *
 * Features provided by CrudCraft:
 * - Standard CRUD workflow already implemented
 * - DTO mapping and repository calls wired up
 *
 * Generation context:
 * - Source model: Project
 * - Package: demo.golden.editable.service
 * - Generator: ServiceGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * Recommendations:
 * - You may customize method behavior, add validation, or extend with additional endpoints.
 * - Signature changes are allowed, but may desync from service or mapper layer—proceed with care.
 * - Do not manually copy or paste other CrudCraft stubs into this class.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@SuppressWarnings("PMD")
@Service
public class ProjectService extends AbstractCrudService<Project, ProjectRequestDto, ProjectResponseDto, ProjectRef, UUID> {
    public ProjectService(ProjectRepository repository, ProjectMapper mapper) {
        super(repository, mapper, Project.class, ProjectResponseDto.class, ProjectRef.class);
    }

    @Override
    protected void postSave(Project entity) {
        ProjectRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(Project entity) {
        ProjectRelationshipMeta.clear(entity);
    }
}

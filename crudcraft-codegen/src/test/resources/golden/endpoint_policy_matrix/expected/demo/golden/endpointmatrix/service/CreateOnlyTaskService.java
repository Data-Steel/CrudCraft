package demo.golden.endpointmatrix.service;

import demo.golden.endpointmatrix.CreateOnlyTask;
import demo.golden.endpointmatrix.dto.ref.CreateOnlyTaskRef;
import demo.golden.endpointmatrix.dto.request.CreateOnlyTaskRequestDto;
import demo.golden.endpointmatrix.dto.response.CreateOnlyTaskResponseDto;
import demo.golden.endpointmatrix.mapper.CreateOnlyTaskMapper;
import demo.golden.endpointmatrix.meta.CreateOnlyTaskRelationshipMeta;
import demo.golden.endpointmatrix.repository.CreateOnlyTaskRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for CreateOnlyTask; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: CreateOnlyTask
 * - Package: demo.golden.endpointmatrix.service
 * - Generator: ServiceGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * To make changes, edit the entity model class and rebuild the project.
 * Do not edit or rename this file manually.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@SuppressWarnings("PMD")
@Service
public class CreateOnlyTaskService extends AbstractCrudService<CreateOnlyTask, CreateOnlyTaskRequestDto, CreateOnlyTaskResponseDto, CreateOnlyTaskRef, UUID> {
    public CreateOnlyTaskService(CreateOnlyTaskRepository repository, CreateOnlyTaskMapper mapper) {
        super(repository, mapper, CreateOnlyTask.class, CreateOnlyTaskResponseDto.class, CreateOnlyTaskRef.class);
    }

    @Override
    protected void postSave(CreateOnlyTask entity) {
        CreateOnlyTaskRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(CreateOnlyTask entity) {
        CreateOnlyTaskRelationshipMeta.clear(entity);
    }
}

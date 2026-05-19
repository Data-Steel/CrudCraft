package demo.golden.search.service;

import demo.golden.search.OperatorPlayground;
import demo.golden.search.dto.ref.OperatorPlaygroundRef;
import demo.golden.search.dto.request.OperatorPlaygroundRequestDto;
import demo.golden.search.dto.response.OperatorPlaygroundResponseDto;
import demo.golden.search.mapper.OperatorPlaygroundMapper;
import demo.golden.search.meta.OperatorPlaygroundRelationshipMeta;
import demo.golden.search.repository.OperatorPlaygroundRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for OperatorPlayground; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: OperatorPlayground
 * - Package: demo.golden.search.service
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
@Service
public class OperatorPlaygroundService extends AbstractCrudService<OperatorPlayground, OperatorPlaygroundRequestDto, OperatorPlaygroundResponseDto, OperatorPlaygroundRef, UUID> {
    public OperatorPlaygroundService(OperatorPlaygroundRepository repository,
            OperatorPlaygroundMapper mapper) {
        super(repository, mapper, OperatorPlayground.class, OperatorPlaygroundResponseDto.class, OperatorPlaygroundRef.class);
    }

    @Override
    protected void postSave(OperatorPlayground entity) {
        OperatorPlaygroundRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(OperatorPlayground entity) {
        OperatorPlaygroundRelationshipMeta.clear(entity);
    }
}

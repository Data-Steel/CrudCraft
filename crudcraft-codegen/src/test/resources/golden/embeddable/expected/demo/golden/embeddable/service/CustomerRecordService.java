package demo.golden.embeddable.service;

import demo.golden.embeddable.CustomerRecord;
import demo.golden.embeddable.dto.ref.CustomerRecordRef;
import demo.golden.embeddable.dto.request.CustomerRecordRequestDto;
import demo.golden.embeddable.dto.response.CustomerRecordResponseDto;
import demo.golden.embeddable.mapper.CustomerRecordMapper;
import demo.golden.embeddable.meta.CustomerRecordRelationshipMeta;
import demo.golden.embeddable.repository.CustomerRecordRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for CustomerRecord; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: CustomerRecord
 * - Package: demo.golden.embeddable.service
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
public class CustomerRecordService extends AbstractCrudService<CustomerRecord, CustomerRecordRequestDto, CustomerRecordResponseDto, CustomerRecordRef, UUID> {
    public CustomerRecordService(CustomerRecordRepository repository, CustomerRecordMapper mapper) {
        super(repository, mapper, CustomerRecord.class, CustomerRecordResponseDto.class, CustomerRecordRef.class);
    }

    @Override
    protected void postSave(CustomerRecord entity) {
        CustomerRecordRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(CustomerRecord entity) {
        CustomerRecordRelationshipMeta.clear(entity);
    }
}

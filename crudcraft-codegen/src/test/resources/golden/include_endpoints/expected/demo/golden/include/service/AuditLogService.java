package demo.golden.include.service;

import demo.golden.include.AuditLog;
import demo.golden.include.dto.ref.AuditLogRef;
import demo.golden.include.dto.request.AuditLogRequestDto;
import demo.golden.include.dto.response.AuditLogResponseDto;
import demo.golden.include.mapper.AuditLogMapper;
import demo.golden.include.meta.AuditLogRelationshipMeta;
import demo.golden.include.repository.AuditLogRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for AuditLog; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: AuditLog
 * - Package: demo.golden.include.service
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
public class AuditLogService extends AbstractCrudService<AuditLog, AuditLogRequestDto, AuditLogResponseDto, AuditLogRef, UUID> {
    public AuditLogService(AuditLogRepository repository, AuditLogMapper mapper) {
        super(repository, mapper, AuditLog.class, AuditLogResponseDto.class, AuditLogRef.class);
    }

    @Override
    protected void postSave(AuditLog entity) {
        AuditLogRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(AuditLog entity) {
        AuditLogRelationshipMeta.clear(entity);
    }
}

package demo.golden.withers.service;

import demo.golden.withers.Setting;
import demo.golden.withers.dto.ref.SettingRef;
import demo.golden.withers.dto.request.SettingRequestDto;
import demo.golden.withers.dto.response.SettingResponseDto;
import demo.golden.withers.mapper.SettingMapper;
import demo.golden.withers.meta.SettingRelationshipMeta;
import demo.golden.withers.repository.SettingRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for Setting; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: Setting
 * - Package: demo.golden.withers.service
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
public class SettingService extends AbstractCrudService<Setting, SettingRequestDto, SettingResponseDto, SettingRef, UUID> {
    public SettingService(SettingRepository repository, SettingMapper mapper) {
        super(repository, mapper, Setting.class, SettingResponseDto.class, SettingRef.class);
    }

    @Override
    protected void postSave(Setting entity) {
        SettingRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(Setting entity) {
        SettingRelationshipMeta.clear(entity);
    }
}

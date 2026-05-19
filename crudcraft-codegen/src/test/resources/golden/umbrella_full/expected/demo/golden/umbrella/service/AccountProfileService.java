package demo.golden.umbrella.service;

import demo.golden.umbrella.AccountProfile;
import demo.golden.umbrella.dto.ref.AccountProfileRef;
import demo.golden.umbrella.dto.request.AccountProfileRequestDto;
import demo.golden.umbrella.dto.response.AccountProfileResponseDto;
import demo.golden.umbrella.mapper.AccountProfileMapper;
import demo.golden.umbrella.meta.AccountProfileRelationshipMeta;
import demo.golden.umbrella.repository.AccountProfileRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for AccountProfile; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: AccountProfile
 * - Package: demo.golden.umbrella.service
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
public class AccountProfileService extends AbstractCrudService<AccountProfile, AccountProfileRequestDto, AccountProfileResponseDto, AccountProfileRef, UUID> {
    public AccountProfileService(AccountProfileRepository repository, AccountProfileMapper mapper) {
        super(repository, mapper, AccountProfile.class, AccountProfileResponseDto.class, AccountProfileRef.class);
    }

    @Override
    protected void postSave(AccountProfile entity) {
        AccountProfileRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(AccountProfile entity) {
        AccountProfileRelationshipMeta.clear(entity);
    }
}

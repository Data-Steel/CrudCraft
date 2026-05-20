package demo.golden.umbrella.service;

import demo.golden.umbrella.AccountTag;
import demo.golden.umbrella.dto.ref.AccountTagRef;
import demo.golden.umbrella.dto.request.AccountTagRequestDto;
import demo.golden.umbrella.dto.response.AccountTagResponseDto;
import demo.golden.umbrella.mapper.AccountTagMapper;
import demo.golden.umbrella.meta.AccountTagRelationshipMeta;
import demo.golden.umbrella.repository.AccountTagRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for AccountTag; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: AccountTag
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
@SuppressWarnings("PMD")
@Service
public class AccountTagService extends AbstractCrudService<AccountTag, AccountTagRequestDto, AccountTagResponseDto, AccountTagRef, UUID> {
    public AccountTagService(AccountTagRepository repository, AccountTagMapper mapper) {
        super(repository, mapper, AccountTag.class, AccountTagResponseDto.class, AccountTagRef.class);
    }

    @Override
    protected void postSave(AccountTag entity) {
        AccountTagRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(AccountTag entity) {
        AccountTagRelationshipMeta.clear(entity);
    }
}

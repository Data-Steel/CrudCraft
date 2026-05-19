package demo.golden.relationship.service;

import demo.golden.relationship.UserAccount;
import demo.golden.relationship.dto.ref.UserAccountRef;
import demo.golden.relationship.dto.request.UserAccountRequestDto;
import demo.golden.relationship.dto.response.UserAccountResponseDto;
import demo.golden.relationship.mapper.UserAccountMapper;
import demo.golden.relationship.meta.UserAccountRelationshipMeta;
import demo.golden.relationship.repository.UserAccountRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for UserAccount; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: UserAccount
 * - Package: demo.golden.relationship.service
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
public class UserAccountService extends AbstractCrudService<UserAccount, UserAccountRequestDto, UserAccountResponseDto, UserAccountRef, UUID> {
    public UserAccountService(UserAccountRepository repository, UserAccountMapper mapper) {
        super(repository, mapper, UserAccount.class, UserAccountResponseDto.class, UserAccountRef.class);
    }

    @Override
    protected void postSave(UserAccount entity) {
        UserAccountRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(UserAccount entity) {
        UserAccountRelationshipMeta.clear(entity);
    }
}

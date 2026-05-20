package demo.golden.relationship.service;

import demo.golden.relationship.UserProfile;
import demo.golden.relationship.dto.ref.UserProfileRef;
import demo.golden.relationship.dto.request.UserProfileRequestDto;
import demo.golden.relationship.dto.response.UserProfileResponseDto;
import demo.golden.relationship.mapper.UserProfileMapper;
import demo.golden.relationship.meta.UserProfileRelationshipMeta;
import demo.golden.relationship.repository.UserProfileRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for UserProfile; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: UserProfile
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
@SuppressWarnings("PMD")
@Service
public class UserProfileService extends AbstractCrudService<UserProfile, UserProfileRequestDto, UserProfileResponseDto, UserProfileRef, UUID> {
    public UserProfileService(UserProfileRepository repository, UserProfileMapper mapper) {
        super(repository, mapper, UserProfile.class, UserProfileResponseDto.class, UserProfileRef.class);
    }

    @Override
    protected void postSave(UserProfile entity) {
        UserProfileRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(UserProfile entity) {
        UserProfileRelationshipMeta.clear(entity);
    }
}

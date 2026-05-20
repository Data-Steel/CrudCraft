package demo.golden.patchonly.service;

import demo.golden.patchonly.Profile;
import demo.golden.patchonly.dto.ref.ProfileRef;
import demo.golden.patchonly.dto.request.ProfileRequestDto;
import demo.golden.patchonly.dto.response.ProfileResponseDto;
import demo.golden.patchonly.mapper.ProfileMapper;
import demo.golden.patchonly.meta.ProfileRelationshipMeta;
import demo.golden.patchonly.repository.ProfileRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for Profile; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: Profile
 * - Package: demo.golden.patchonly.service
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
public class ProfileService extends AbstractCrudService<Profile, ProfileRequestDto, ProfileResponseDto, ProfileRef, UUID> {
    public ProfileService(ProfileRepository repository, ProfileMapper mapper) {
        super(repository, mapper, Profile.class, ProfileResponseDto.class, ProfileRef.class);
    }

    @Override
    protected void postSave(Profile entity) {
        ProfileRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(Profile entity) {
        ProfileRelationshipMeta.clear(entity);
    }
}

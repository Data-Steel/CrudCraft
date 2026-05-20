package demo.golden.endpointmatrix.service;

import demo.golden.endpointmatrix.SecureInternalSecret;
import demo.golden.endpointmatrix.dto.ref.SecureInternalSecretRef;
import demo.golden.endpointmatrix.dto.request.SecureInternalSecretRequestDto;
import demo.golden.endpointmatrix.dto.response.SecureInternalSecretResponseDto;
import demo.golden.endpointmatrix.mapper.SecureInternalSecretMapper;
import demo.golden.endpointmatrix.meta.SecureInternalSecretRelationshipMeta;
import demo.golden.endpointmatrix.repository.SecureInternalSecretRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for SecureInternalSecret; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: SecureInternalSecret
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
public class SecureInternalSecretService extends AbstractCrudService<SecureInternalSecret, SecureInternalSecretRequestDto, SecureInternalSecretResponseDto, SecureInternalSecretRef, UUID> {
    public SecureInternalSecretService(SecureInternalSecretRepository repository,
            SecureInternalSecretMapper mapper) {
        super(repository, mapper, SecureInternalSecret.class, SecureInternalSecretResponseDto.class, SecureInternalSecretRef.class);
    }

    @Override
    protected void postSave(SecureInternalSecret entity) {
        SecureInternalSecretRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(SecureInternalSecret entity) {
        SecureInternalSecretRelationshipMeta.clear(entity);
    }
}

package demo.golden.projection.service;

import demo.golden.projection.Purchase;
import demo.golden.projection.dto.ref.PurchaseRef;
import demo.golden.projection.dto.request.PurchaseRequestDto;
import demo.golden.projection.dto.response.PurchaseResponseDto;
import demo.golden.projection.mapper.PurchaseMapper;
import demo.golden.projection.meta.PurchaseRelationshipMeta;
import demo.golden.projection.repository.PurchaseRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for Purchase; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: Purchase
 * - Package: demo.golden.projection.service
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
public class PurchaseService extends AbstractCrudService<Purchase, PurchaseRequestDto, PurchaseResponseDto, PurchaseRef, UUID> {
    public PurchaseService(PurchaseRepository repository, PurchaseMapper mapper) {
        super(repository, mapper, Purchase.class, PurchaseResponseDto.class, PurchaseRef.class);
    }

    @Override
    protected void postSave(Purchase entity) {
        PurchaseRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(Purchase entity) {
        PurchaseRelationshipMeta.clear(entity);
    }
}

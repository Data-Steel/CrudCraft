package demo.golden.immutable.service;

import demo.golden.immutable.Invoice;
import demo.golden.immutable.dto.ref.InvoiceRef;
import demo.golden.immutable.dto.request.InvoiceRequestDto;
import demo.golden.immutable.dto.response.InvoiceResponseDto;
import demo.golden.immutable.mapper.InvoiceMapper;
import demo.golden.immutable.meta.InvoiceRelationshipMeta;
import demo.golden.immutable.repository.InvoiceRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for Invoice; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: Invoice
 * - Package: demo.golden.immutable.service
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
public class InvoiceService extends AbstractCrudService<Invoice, InvoiceRequestDto, InvoiceResponseDto, InvoiceRef, UUID> {
    public InvoiceService(InvoiceRepository repository, InvoiceMapper mapper) {
        super(repository, mapper, Invoice.class, InvoiceResponseDto.class, InvoiceRef.class);
    }

    @Override
    protected void postSave(Invoice entity) {
        InvoiceRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(Invoice entity) {
        InvoiceRelationshipMeta.clear(entity);
    }
}

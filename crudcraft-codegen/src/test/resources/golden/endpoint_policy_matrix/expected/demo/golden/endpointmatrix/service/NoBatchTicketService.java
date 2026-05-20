package demo.golden.endpointmatrix.service;

import demo.golden.endpointmatrix.NoBatchTicket;
import demo.golden.endpointmatrix.dto.ref.NoBatchTicketRef;
import demo.golden.endpointmatrix.dto.request.NoBatchTicketRequestDto;
import demo.golden.endpointmatrix.dto.response.NoBatchTicketResponseDto;
import demo.golden.endpointmatrix.mapper.NoBatchTicketMapper;
import demo.golden.endpointmatrix.meta.NoBatchTicketRelationshipMeta;
import demo.golden.endpointmatrix.repository.NoBatchTicketRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for NoBatchTicket; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: NoBatchTicket
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
public class NoBatchTicketService extends AbstractCrudService<NoBatchTicket, NoBatchTicketRequestDto, NoBatchTicketResponseDto, NoBatchTicketRef, UUID> {
    public NoBatchTicketService(NoBatchTicketRepository repository, NoBatchTicketMapper mapper) {
        super(repository, mapper, NoBatchTicket.class, NoBatchTicketResponseDto.class, NoBatchTicketRef.class);
    }

    @Override
    protected void postSave(NoBatchTicket entity) {
        NoBatchTicketRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(NoBatchTicket entity) {
        NoBatchTicketRelationshipMeta.clear(entity);
    }
}

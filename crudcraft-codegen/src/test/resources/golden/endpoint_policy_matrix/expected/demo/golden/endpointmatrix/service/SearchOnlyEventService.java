package demo.golden.endpointmatrix.service;

import demo.golden.endpointmatrix.SearchOnlyEvent;
import demo.golden.endpointmatrix.dto.ref.SearchOnlyEventRef;
import demo.golden.endpointmatrix.dto.request.SearchOnlyEventRequestDto;
import demo.golden.endpointmatrix.dto.response.SearchOnlyEventResponseDto;
import demo.golden.endpointmatrix.mapper.SearchOnlyEventMapper;
import demo.golden.endpointmatrix.meta.SearchOnlyEventRelationshipMeta;
import demo.golden.endpointmatrix.repository.SearchOnlyEventRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for SearchOnlyEvent; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: SearchOnlyEvent
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
@Service
public class SearchOnlyEventService extends AbstractCrudService<SearchOnlyEvent, SearchOnlyEventRequestDto, SearchOnlyEventResponseDto, SearchOnlyEventRef, UUID> {
    public SearchOnlyEventService(SearchOnlyEventRepository repository,
            SearchOnlyEventMapper mapper) {
        super(repository, mapper, SearchOnlyEvent.class, SearchOnlyEventResponseDto.class, SearchOnlyEventRef.class);
    }

    @Override
    protected void postSave(SearchOnlyEvent entity) {
        SearchOnlyEventRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(SearchOnlyEvent entity) {
        SearchOnlyEventRelationshipMeta.clear(entity);
    }
}

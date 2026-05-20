package demo.golden.endpointmatrix.service;

import demo.golden.endpointmatrix.CustomPolicyReport;
import demo.golden.endpointmatrix.dto.ref.CustomPolicyReportRef;
import demo.golden.endpointmatrix.dto.request.CustomPolicyReportRequestDto;
import demo.golden.endpointmatrix.dto.response.CustomPolicyReportResponseDto;
import demo.golden.endpointmatrix.mapper.CustomPolicyReportMapper;
import demo.golden.endpointmatrix.meta.CustomPolicyReportRelationshipMeta;
import demo.golden.endpointmatrix.repository.CustomPolicyReportRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for CustomPolicyReport; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: CustomPolicyReport
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
public class CustomPolicyReportService extends AbstractCrudService<CustomPolicyReport, CustomPolicyReportRequestDto, CustomPolicyReportResponseDto, CustomPolicyReportRef, UUID> {
    public CustomPolicyReportService(CustomPolicyReportRepository repository,
            CustomPolicyReportMapper mapper) {
        super(repository, mapper, CustomPolicyReport.class, CustomPolicyReportResponseDto.class, CustomPolicyReportRef.class);
    }

    @Override
    protected void postSave(CustomPolicyReport entity) {
        CustomPolicyReportRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(CustomPolicyReport entity) {
        CustomPolicyReportRelationshipMeta.clear(entity);
    }
}

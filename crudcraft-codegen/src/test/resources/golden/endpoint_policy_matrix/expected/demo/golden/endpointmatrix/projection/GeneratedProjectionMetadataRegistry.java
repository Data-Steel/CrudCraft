package demo.golden.endpointmatrix.projection;

import demo.golden.endpointmatrix.dto.request.CreateOnlyTaskRequestDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.request.CustomPolicyReportRequestDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.request.LightPublicPageRequestDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.request.NoBatchTicketRequestDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.request.NoDeleteRecordRequestDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.request.SearchOnlyEventRequestDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.request.SecureInternalSecretRequestDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.request.ValidationOnlyDraftRequestDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.response.CreateOnlyTaskResponseDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.response.CustomPolicyReportResponseDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.response.LightPublicPageResponseDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.response.NoBatchTicketResponseDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.response.NoDeleteRecordResponseDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.response.SearchOnlyEventResponseDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.response.SecureInternalSecretResponseDtoProjectionMetadata;
import demo.golden.endpointmatrix.dto.response.ValidationOnlyDraftResponseDtoProjectionMetadata;
import java.util.HashMap;
import java.util.Map;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import org.springframework.stereotype.Component;

@Component
public class GeneratedProjectionMetadataRegistry implements ProjectionMetadataRegistry {
    private final Map<Class<?>, ProjectionMetadata<?>> metadata = new HashMap<>();

    public GeneratedProjectionMetadataRegistry() {
        register(new CreateOnlyTaskRequestDtoProjectionMetadata());
        register(new CreateOnlyTaskResponseDtoProjectionMetadata());
        register(new CustomPolicyReportRequestDtoProjectionMetadata());
        register(new CustomPolicyReportResponseDtoProjectionMetadata());
        register(new LightPublicPageRequestDtoProjectionMetadata());
        register(new LightPublicPageResponseDtoProjectionMetadata());
        register(new NoBatchTicketRequestDtoProjectionMetadata());
        register(new NoBatchTicketResponseDtoProjectionMetadata());
        register(new NoDeleteRecordRequestDtoProjectionMetadata());
        register(new NoDeleteRecordResponseDtoProjectionMetadata());
        register(new SearchOnlyEventRequestDtoProjectionMetadata());
        register(new SearchOnlyEventResponseDtoProjectionMetadata());
        register(new SecureInternalSecretRequestDtoProjectionMetadata());
        register(new SecureInternalSecretResponseDtoProjectionMetadata());
        register(new ValidationOnlyDraftRequestDtoProjectionMetadata());
        register(new ValidationOnlyDraftResponseDtoProjectionMetadata());
    }

    private <D> void register(ProjectionMetadata<D> pm) {
        metadata.put(pm.dtoType(), pm);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D> ProjectionMetadata<D> getMetadata(Class<D> dtoType) {
        return (ProjectionMetadata<D>) metadata.get(dtoType);
    }
}

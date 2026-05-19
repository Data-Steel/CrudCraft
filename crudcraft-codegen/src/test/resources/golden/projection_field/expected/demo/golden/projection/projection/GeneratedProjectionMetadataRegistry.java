package demo.golden.projection.projection;

import demo.golden.projection.dto.request.CustomerRequestDtoProjectionMetadata;
import demo.golden.projection.dto.request.PurchaseRequestDtoProjectionMetadata;
import demo.golden.projection.dto.response.CustomerResponseDtoProjectionMetadata;
import demo.golden.projection.dto.response.PurchaseResponseDtoProjectionMetadata;
import java.util.HashMap;
import java.util.Map;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import org.springframework.stereotype.Component;

@Component
public class GeneratedProjectionMetadataRegistry implements ProjectionMetadataRegistry {
    private final Map<Class<?>, ProjectionMetadata<?>> metadata = new HashMap<>();

    public GeneratedProjectionMetadataRegistry() {
        register(new CustomerRequestDtoProjectionMetadata());
        register(new CustomerResponseDtoProjectionMetadata());
        register(new PurchaseRequestDtoProjectionMetadata());
        register(new PurchaseResponseDtoProjectionMetadata());
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

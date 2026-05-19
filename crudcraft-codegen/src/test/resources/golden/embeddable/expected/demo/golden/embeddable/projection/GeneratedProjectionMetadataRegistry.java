package demo.golden.embeddable.projection;

import demo.golden.embeddable.dto.request.AddressRequestDtoProjectionMetadata;
import demo.golden.embeddable.dto.request.CustomerRecordRequestDtoProjectionMetadata;
import demo.golden.embeddable.dto.response.AddressResponseDtoProjectionMetadata;
import demo.golden.embeddable.dto.response.CustomerRecordResponseDtoProjectionMetadata;
import java.util.HashMap;
import java.util.Map;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import org.springframework.stereotype.Component;

@Component
public class GeneratedProjectionMetadataRegistry implements ProjectionMetadataRegistry {
    private final Map<Class<?>, ProjectionMetadata<?>> metadata = new HashMap<>();

    public GeneratedProjectionMetadataRegistry() {
        register(new CustomerRecordRequestDtoProjectionMetadata());
        register(new CustomerRecordResponseDtoProjectionMetadata());
        register(new AddressRequestDtoProjectionMetadata());
        register(new AddressResponseDtoProjectionMetadata());
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

package demo.golden.relationship.projection;

import demo.golden.relationship.dto.request.AccessGroupRequestDtoProjectionMetadata;
import demo.golden.relationship.dto.request.UserAccountRequestDtoProjectionMetadata;
import demo.golden.relationship.dto.request.UserProfileRequestDtoProjectionMetadata;
import demo.golden.relationship.dto.response.AccessGroupResponseDtoProjectionMetadata;
import demo.golden.relationship.dto.response.UserAccountResponseDtoProjectionMetadata;
import demo.golden.relationship.dto.response.UserProfileResponseDtoProjectionMetadata;
import java.util.HashMap;
import java.util.Map;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import org.springframework.stereotype.Component;

@Component
public class GeneratedProjectionMetadataRegistry implements ProjectionMetadataRegistry {
    private final Map<Class<?>, ProjectionMetadata<?>> metadata = new HashMap<>();

    public GeneratedProjectionMetadataRegistry() {
        register(new AccessGroupRequestDtoProjectionMetadata());
        register(new AccessGroupResponseDtoProjectionMetadata());
        register(new UserAccountRequestDtoProjectionMetadata());
        register(new UserAccountResponseDtoProjectionMetadata());
        register(new UserProfileRequestDtoProjectionMetadata());
        register(new UserProfileResponseDtoProjectionMetadata());
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

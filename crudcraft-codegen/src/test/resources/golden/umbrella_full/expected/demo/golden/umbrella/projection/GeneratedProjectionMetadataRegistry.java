package demo.golden.umbrella.projection;

import demo.golden.umbrella.dto.request.AccountProfileRequestDtoProjectionMetadata;
import demo.golden.umbrella.dto.request.AccountRequestDtoProjectionMetadata;
import demo.golden.umbrella.dto.request.AccountTagRequestDtoProjectionMetadata;
import demo.golden.umbrella.dto.response.AccountDetailResponseDtoProjectionMetadata;
import demo.golden.umbrella.dto.response.AccountListResponseDtoProjectionMetadata;
import demo.golden.umbrella.dto.response.AccountProfileResponseDtoProjectionMetadata;
import demo.golden.umbrella.dto.response.AccountResponseDtoProjectionMetadata;
import demo.golden.umbrella.dto.response.AccountTagResponseDtoProjectionMetadata;
import java.util.HashMap;
import java.util.Map;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import org.springframework.stereotype.Component;

@Component
public class GeneratedProjectionMetadataRegistry implements ProjectionMetadataRegistry {
    private final Map<Class<?>, ProjectionMetadata<?>> metadata = new HashMap<>();

    public GeneratedProjectionMetadataRegistry() {
        register(new AccountRequestDtoProjectionMetadata());
        register(new AccountResponseDtoProjectionMetadata());
        register(new AccountListResponseDtoProjectionMetadata());
        register(new AccountDetailResponseDtoProjectionMetadata());
        register(new AccountProfileRequestDtoProjectionMetadata());
        register(new AccountProfileResponseDtoProjectionMetadata());
        register(new AccountTagRequestDtoProjectionMetadata());
        register(new AccountTagResponseDtoProjectionMetadata());
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

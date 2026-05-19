package demo.golden.dtovariants.projection;

import demo.golden.dtovariants.dto.request.CatalogItemRequestDtoProjectionMetadata;
import demo.golden.dtovariants.dto.response.CatalogItemDetailResponseDtoProjectionMetadata;
import demo.golden.dtovariants.dto.response.CatalogItemListResponseDtoProjectionMetadata;
import demo.golden.dtovariants.dto.response.CatalogItemResponseDtoProjectionMetadata;
import java.util.HashMap;
import java.util.Map;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import org.springframework.stereotype.Component;

@Component
public class GeneratedProjectionMetadataRegistry implements ProjectionMetadataRegistry {
    private final Map<Class<?>, ProjectionMetadata<?>> metadata = new HashMap<>();

    public GeneratedProjectionMetadataRegistry() {
        register(new CatalogItemRequestDtoProjectionMetadata());
        register(new CatalogItemResponseDtoProjectionMetadata());
        register(new CatalogItemListResponseDtoProjectionMetadata());
        register(new CatalogItemDetailResponseDtoProjectionMetadata());
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

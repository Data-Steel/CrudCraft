package demo.golden.search.projection;

import demo.golden.search.dto.request.OperatorPlaygroundRequestDtoProjectionMetadata;
import demo.golden.search.dto.request.SearchTagRequestDtoProjectionMetadata;
import demo.golden.search.dto.response.OperatorPlaygroundResponseDtoProjectionMetadata;
import demo.golden.search.dto.response.SearchTagResponseDtoProjectionMetadata;
import java.util.HashMap;
import java.util.Map;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import org.springframework.stereotype.Component;

@Component
public class GeneratedProjectionMetadataRegistry implements ProjectionMetadataRegistry {
    private final Map<Class<?>, ProjectionMetadata<?>> metadata = new HashMap<>();

    public GeneratedProjectionMetadataRegistry() {
        register(new OperatorPlaygroundRequestDtoProjectionMetadata());
        register(new OperatorPlaygroundResponseDtoProjectionMetadata());
        register(new SearchTagRequestDtoProjectionMetadata());
        register(new SearchTagResponseDtoProjectionMetadata());
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

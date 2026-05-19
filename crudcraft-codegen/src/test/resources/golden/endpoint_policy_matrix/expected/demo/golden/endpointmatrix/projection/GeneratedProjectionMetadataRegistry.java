/*
 * Copyright (c) 2026 CrudCraft contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

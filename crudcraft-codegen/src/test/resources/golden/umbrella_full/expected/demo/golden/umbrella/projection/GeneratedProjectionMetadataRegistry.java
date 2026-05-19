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

/*
 * Copyright (c) 2025 CrudCraft contributors
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
package nl.datasteel.crudcraft.codegen.writer.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.*;

/**
 * Registry of the default CRUD endpoint specifications used by CrudCraft.
 */
public final class ControllerEndpoints {

    private static final List<EndpointSpecProvider> DEFAULTS = List.of(
            new GetOneEndpoint(),
            new GetAllEndpoint(),
            new GetAllRefEndpoint(),
            new CreateEndpoint(),
            new UpdateEndpoint(),
            new PatchEndpoint(),
            new DeleteEndpoint(),
            new BulkCreateEndpoint(),
            new BulkUpdateEndpoint(),
            new BulkPatchEndpoint(),
            new BulkUpsertEndpoint(),
            new BulkDeleteEndpoint(),
            new FindByIdsEndpoint(),
            new ExistsEndpoint(),
            new CountEndpoint(),
            new SearchEndpoint(),
            new ValidateEndpoint(),
            new ExportEndpoint()
    );

    private ControllerEndpoints() {}

    public static Map<CrudEndpoint, EndpointSpec> defaults(ModelDescriptor modelDescriptor) {
        return DEFAULTS.stream()
                .collect(Collectors.toMap(EndpointSpecProvider::endpoint,
                        p -> p.create(modelDescriptor)));
    }
}

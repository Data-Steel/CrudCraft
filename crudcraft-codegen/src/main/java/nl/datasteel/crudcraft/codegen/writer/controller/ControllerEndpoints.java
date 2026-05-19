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

package nl.datasteel.crudcraft.codegen.writer.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.BulkCreateEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.BulkDeleteEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.BulkPatchEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.BulkUpdateEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.BulkUpsertEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.CountEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.CreateEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.DeleteEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.EndpointSpecProvider;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.EndpointSupport;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.ExistsEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.ExportEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.FindByIdsEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.GetAllEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.GetAllRefEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.GetOneEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.PatchEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.SearchEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.UpdateEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.ValidateEndpoint;


/** Registry of the default CRUD endpoint specifications used by CrudCraft. */
public final class ControllerEndpoints {

    private static final List<EndpointSpecProvider> BASE_DEFAULTS =
            List.of(
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
                    new ValidateEndpoint(),
                    new ExportEndpoint());

    /** Utility class constructor. */
    private ControllerEndpoints() {}

    /**
     * Builds default endpoint specifications for a model.
     *
     * @param modelDescriptor model descriptor
     * @return endpoint spec map by endpoint
     */
    public static Map<CrudEndpoint, EndpointSpec> defaults(ModelDescriptor modelDescriptor) {
        Objects.requireNonNull(modelDescriptor);
        List<EndpointSpecProvider> defaults = new java.util.ArrayList<>(BASE_DEFAULTS);
        if (EndpointSupport.hasSearchFields(modelDescriptor)) {
            defaults.add(new SearchEndpoint());
        }
        return defaults.stream()
                .collect(
                        Collectors.toMap(
                                EndpointSpecProvider::endpoint,
                                p -> p.create(modelDescriptor),
                                (left, right) -> right,
                                LinkedHashMap::new));
    }
}

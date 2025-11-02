/*
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.codegen.writer.controller;

import java.util.List;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;

/** Utility factory for creating simple {@link ModelDescriptor} instances for tests. */
public final class TestModelDescriptorFactory {
    private TestModelDescriptorFactory() {}

    public static ModelDescriptor create() {
        ModelIdentity identity = new ModelIdentity(
                "Sample", "com.example", List.of(), "com.example");
        ModelFlags flags = new ModelFlags(true, true, false, false);
        EndpointOptions endpoints = new EndpointOptions(
                CrudTemplate.FULL, new CrudEndpoint[0], new CrudEndpoint[0], null);
        ModelSecurity security = new ModelSecurity(false, null, List.of());
        return new ModelDescriptor(identity, flags, endpoints, security);
    }
}

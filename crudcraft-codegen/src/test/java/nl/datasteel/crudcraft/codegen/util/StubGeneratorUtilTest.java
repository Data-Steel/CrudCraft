/**
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
package nl.datasteel.crudcraft.codegen.util;

import java.util.List;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class StubGeneratorUtilTest {

    private ModelDescriptor model(boolean editable) {
        ModelIdentity id = new ModelIdentity("User", "com.example", List.of(), "com.example");
        ModelFlags flags = new ModelFlags(editable, true, false);
        EndpointOptions options = new EndpointOptions(null, null, null, null);
        ModelSecurity security = new ModelSecurity(false, null, List.of());
        return new ModelDescriptor(id, flags, options, security);
    }

    @Test
    void stubMetaUsesEditableHeaderWhenModelEditable() {
        StubGeneratorUtil.StubMeta meta = StubGeneratorUtil.stubMeta(
                model(true), "service", "Service", "Service", StubGeneratorUtilTest.class);
        assertEquals("com.example.service", meta.pkg());
        assertEquals("UserService", meta.name());
        assertTrue(meta.header().contains("Override methods"));
    }

    @Test
    void stubMetaUsesStrictHeaderWhenModelNotEditable() {
        StubGeneratorUtil.StubMeta meta = StubGeneratorUtil.stubMeta(
                model(false), "service", "Service", "Service", StubGeneratorUtilTest.class);
        assertTrue(meta.header().contains("default implementation"));
    }
}

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

package nl.datasteel.crudcraft.codegen.descriptor;

import java.util.List;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ModelSecurityTest {

    static class P implements CrudSecurityPolicy {
        @Override public String getSecurityExpression(nl.datasteel.crudcraft.annotations.CrudEndpoint endpoint) { return ""; }
    }

    @Test
    void gettersReturnValues() {
        ModelSecurity sec = new ModelSecurity(true, P.class, List.of("Handler"));
        assertTrue(sec.isSecure());
        assertEquals(P.class, sec.getSecurityPolicy());
        assertEquals(List.of("Handler"), sec.getRowSecurityHandlers());
    }

    @Test
    void rowHandlersListNotDefensivelyCopied() {
        java.util.ArrayList<String> handlers = new java.util.ArrayList<>();
        handlers.add("A");
        ModelSecurity sec = new ModelSecurity(true, P.class, handlers);
        handlers.add("B");
        assertEquals(2, sec.getRowSecurityHandlers().size());
    }
}

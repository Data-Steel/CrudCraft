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

import com.squareup.javapoet.MethodSpec;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.method.ControllerMethodContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ControllerMethodContextTest {

    @Test
    void endpointDelegatesToSpec() {
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.GET_ONE,
                "m",
                md -> null,
                md -> null,
                java.util.List.of(),
                (b, md) -> {}
        );
        ControllerMethodContext ctx = new ControllerMethodContext(MethodSpec.methodBuilder("m"),
                TestModelDescriptorFactory.create(), spec, null);
        assertEquals(CrudEndpoint.GET_ONE, ctx.endpoint());
    }

    @Test
    void endpointThrowsWhenSpecNull() {
        ControllerMethodContext ctx = new ControllerMethodContext(MethodSpec.methodBuilder("m"),
                TestModelDescriptorFactory.create(), null, null);
        assertThrows(NullPointerException.class, ctx::endpoint);
    }
}

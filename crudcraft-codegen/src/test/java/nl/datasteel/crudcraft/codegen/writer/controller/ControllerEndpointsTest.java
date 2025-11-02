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
import java.util.EnumSet;
import java.util.Map;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ControllerEndpointsTest {

    private final ModelDescriptor model = TestModelDescriptorFactory.create();

    @Test
    void defaultsReturnAllEndpoints() {
        Map<CrudEndpoint, EndpointSpec> map = ControllerEndpoints.defaults(model);
        assertEquals(EnumSet.allOf(CrudEndpoint.class), map.keySet());
    }

    @Test
    void defaultsThrowsOnNullDescriptor() {
        assertThrows(NullPointerException.class, () -> ControllerEndpoints.defaults(null));
    }

    @Test
    void specFunctionsProduceNonNull() {
        Map<CrudEndpoint, EndpointSpec> map = ControllerEndpoints.defaults(model);
        map.values().forEach(spec -> {
            assertNotNull(spec.mapping().apply(model));
            assertNotNull(spec.returnType().apply(model));
            spec.params().forEach(p -> assertNotNull(p.apply(model)));
            assertDoesNotThrow(() -> spec.body().accept(MethodSpec.methodBuilder(spec.methodName()), model));
        });
    }
}

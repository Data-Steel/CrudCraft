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
package nl.datasteel.crudcraft.codegen.writer.controller.method;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;
import nl.datasteel.crudcraft.codegen.writer.controller.TestModelDescriptorFactory;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.EndpointSupport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ParameterComponentTest {

    @Test
    void addsParametersFromSpec() {
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.GET_ONE,
                "m",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> null,
                java.util.List.of(
                        md -> ParameterSpec.builder(EndpointSupport.UUID_CLASS, "id").build(),
                        md -> ParameterSpec.builder(ClassName.get(String.class), "name").build()
                ),
                (b, md) -> {}
        );
        ControllerMethodContext ctx = new ControllerMethodContext(MethodSpec.methodBuilder("m"),
                TestModelDescriptorFactory.create(), spec, null);
        new ParameterComponent().apply(ctx);
        MethodSpec built = ctx.builder().build();
        assertEquals(2, built.parameters.size());
    }

    @Test
    void failsWhenParameterNull() {
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.GET_ONE,
                "m",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> null,
                java.util.List.of(md -> null),
                (b, md) -> {}
        );
        ControllerMethodContext ctx = new ControllerMethodContext(MethodSpec.methodBuilder("m"),
                TestModelDescriptorFactory.create(), spec, null);
        assertThrows(NullPointerException.class, () -> new ParameterComponent().apply(ctx));
    }
}

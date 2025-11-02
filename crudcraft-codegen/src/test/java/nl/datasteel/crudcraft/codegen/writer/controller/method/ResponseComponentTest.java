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
package nl.datasteel.crudcraft.codegen.writer.controller.method;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;
import nl.datasteel.crudcraft.codegen.writer.controller.TestModelDescriptorFactory;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.EndpointSupport;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ResponseComponentTest {

    @Test
    void setsReturnTypeAndBody() {
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.COUNT,
                "count",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> ClassName.get(String.class),
                java.util.List.of(),
                (b, md) -> b.addStatement("return\n")
        );
        ControllerMethodContext ctx = new ControllerMethodContext(MethodSpec.methodBuilder("count"),
                TestModelDescriptorFactory.create(), spec, null);
        new ResponseComponent().apply(ctx);
        MethodSpec built = ctx.builder().build();
        assertEquals("java.lang.String", built.returnType.toString());
        assertTrue(built.code.toString().contains("return"));
    }

    @Test
    void failsWhenReturnTypeNull() {
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.COUNT,
                "count",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> null,
                java.util.List.of(),
                (b, md) -> {}
        );
        ControllerMethodContext ctx = new ControllerMethodContext(MethodSpec.methodBuilder("count"),
                TestModelDescriptorFactory.create(), spec, null);
        assertThrows(NullPointerException.class, () -> new ResponseComponent().apply(ctx));
    }
}

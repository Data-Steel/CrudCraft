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
import com.squareup.javapoet.MethodSpec;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;
import nl.datasteel.crudcraft.codegen.writer.controller.TestModelDescriptorFactory;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.EndpointSupport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class RouteDeclarationComponentTest {

    @Test
    void appliesMappingAnnotation() {
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.COUNT,
                "count",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING)
                        .addMember("value", "$S", "/count").build(),
                md -> null,
                java.util.List.of(),
                (b, md) -> {}
        );
        ControllerMethodContext ctx = new ControllerMethodContext(MethodSpec.methodBuilder("count"),
                TestModelDescriptorFactory.create(), spec, null);
        new RouteDeclarationComponent().apply(ctx);
        MethodSpec built = ctx.builder().build();
        assertEquals(1, built.annotations.size());
        assertEquals("org.springframework.web.bind.annotation.GetMapping", built.annotations.get(0).type.toString());
    }

    @Test
    void failsWhenMappingIsNull() {
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.COUNT,
                "count",
                md -> null,
                md -> null,
                java.util.List.of(),
                (b, md) -> {}
        );
        ControllerMethodContext ctx = new ControllerMethodContext(MethodSpec.methodBuilder("count"),
                TestModelDescriptorFactory.create(), spec, null);
        assertThrows(NullPointerException.class, () -> new RouteDeclarationComponent().apply(ctx));
    }
}

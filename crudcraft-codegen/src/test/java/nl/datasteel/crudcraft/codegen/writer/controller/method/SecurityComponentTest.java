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
package nl.datasteel.crudcraft.codegen.writer.controller.method;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;
import nl.datasteel.crudcraft.codegen.writer.controller.TestModelDescriptorFactory;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.EndpointSupport;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SecurityComponentTest {

    @Test
    void appliesPreAuthorizeWhenPolicyPresent() {
        CrudSecurityPolicy policy = endpoint -> "hasRole('ADMIN')";
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.GET_ONE,
                "m",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> null,
                java.util.List.of(),
                (b, md) -> {}
        );
        ControllerMethodContext ctx = new ControllerMethodContext(MethodSpec.methodBuilder("m"),
                TestModelDescriptorFactory.create(), spec, policy);
        new SecurityComponent().apply(ctx);
        MethodSpec built = ctx.builder().build();
        assertEquals(1, built.annotations.size());
        assertTrue(built.annotations.get(0).toString().contains("hasRole('ADMIN')"));
    }

    @Test
    void skipsWhenNoPolicy() {
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.GET_ONE,
                "m",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> null,
                java.util.List.of(),
                (b, md) -> {}
        );
        ControllerMethodContext ctx = new ControllerMethodContext(MethodSpec.methodBuilder("m"),
                TestModelDescriptorFactory.create(), spec, null);
        new SecurityComponent().apply(ctx);
        MethodSpec built = ctx.builder().build();
        assertEquals(0, built.annotations.size());
    }

    @Test
    void failsWhenExpressionNull() {
        CrudSecurityPolicy policy = endpoint -> null;
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.GET_ONE,
                "m",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> null,
                java.util.List.of(),
                (b, md) -> {}
        );
        ControllerMethodContext ctx = new ControllerMethodContext(MethodSpec.methodBuilder("m"),
                TestModelDescriptorFactory.create(), spec, policy);
        assertThrows(NullPointerException.class, () -> new SecurityComponent().apply(ctx));
    }
}

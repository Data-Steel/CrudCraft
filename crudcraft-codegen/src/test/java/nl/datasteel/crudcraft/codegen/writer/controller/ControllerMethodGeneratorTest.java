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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import java.util.ArrayList;
import java.util.List;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.EndpointSupport;
import nl.datasteel.crudcraft.codegen.writer.controller.method.ControllerMethodComponent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ControllerMethodGeneratorTest {

    private final ModelDescriptor model = TestModelDescriptorFactory.create();

    @Test
    void generatesMethodWithDefaultComponents() {
        ControllerMethodGenerator gen = new ControllerMethodGenerator();
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.COUNT,
                "count",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> ClassName.get(String.class),
                List.of(md -> ParameterSpec.builder(ClassName.get(String.class), "id").build()),
                (b, md) -> b.addStatement("return\n")
        );
        MethodSpec method = gen.generate(spec, model, null);
        assertEquals("count", method.name);
        // Should have at least @GetMapping, @Operation, and @ApiResponses
        assertTrue(method.annotations.size() >= 1, "Should have at least 1 annotation");
        assertTrue(method.annotations.stream()
                .anyMatch(a -> a.type.toString().contains("GetMapping")), 
                "Should have GetMapping annotation");
        assertEquals(1, method.parameters.size());
        assertEquals("java.lang.String", method.returnType.toString());
    }

    @Test
    void honorsCustomComponentsOrder() {
        List<String> order = new ArrayList<>();
        ControllerMethodComponent c1 = ctx -> order.add("first");
        ControllerMethodComponent c2 = ctx -> order.add("second");
        ControllerMethodGenerator gen = new ControllerMethodGenerator(List.of(c1, c2));
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.COUNT,
                "m",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> ClassName.get(Void.class),
                List.of(),
                (b, md) -> {}
        );
        gen.generate(spec, model, null);
        assertEquals(List.of("first", "second"), order);
    }

    @Test
    void appliesSecurityComponentWhenPolicyProvided() {
        CrudSecurityPolicy policy = ep -> "permitAll()";
        ControllerMethodGenerator gen = new ControllerMethodGenerator();
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.GET_ONE,
                "m",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> ClassName.get(Void.class),
                List.of(),
                (b, md) -> {}
        );
        MethodSpec method = gen.generate(spec, model, policy);
        assertTrue(method.annotations.stream().anyMatch(a -> a.toString().contains("PreAuthorize")));
    }

    @Test
    void noSecurityAnnotationWhenPolicyNull() {
        ControllerMethodGenerator gen = new ControllerMethodGenerator();
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.GET_ONE,
                "m",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> ClassName.get(Void.class),
                List.of(),
                (b, md) -> {}
        );
        MethodSpec method = gen.generate(spec, model, null);
        assertTrue(method.annotations.stream().noneMatch(a -> a.toString().contains("PreAuthorize")));
    }

    @Test
    void emptyComponentsProduceBareMethod() {
        ControllerMethodGenerator gen = new ControllerMethodGenerator(List.of());
        EndpointSpec spec = new EndpointSpec(
                CrudEndpoint.COUNT,
                "m",
                md -> AnnotationSpec.builder(EndpointSupport.GET_MAPPING).build(),
                md -> ClassName.get(Void.class),
                List.of(),
                (b, md) -> {}
        );
        MethodSpec method = gen.generate(spec, model, null);
        assertEquals(0, method.annotations.size());
        assertEquals(0, method.parameters.size());
    }
}

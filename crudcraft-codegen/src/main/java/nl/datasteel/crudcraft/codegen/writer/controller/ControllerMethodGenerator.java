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
import java.util.List;
import javax.lang.model.element.Modifier;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.controller.method.ControllerMethodComponent;
import nl.datasteel.crudcraft.codegen.writer.controller.method.ControllerMethodContext;
import nl.datasteel.crudcraft.codegen.writer.controller.method.OpenApiDocComponent;
import nl.datasteel.crudcraft.codegen.writer.controller.method.ParameterComponent;
import nl.datasteel.crudcraft.codegen.writer.controller.method.ResponseComponent;
import nl.datasteel.crudcraft.codegen.writer.controller.method.RouteDeclarationComponent;
import nl.datasteel.crudcraft.codegen.writer.controller.method.SecurityComponent;

/**
 * Coordinates the individual components that form a controller method.
 * Additional components can be supplied to support custom endpoints.
 */
public class ControllerMethodGenerator {

    private final List<ControllerMethodComponent> components;

    public ControllerMethodGenerator(List<ControllerMethodComponent> components) {
        this.components = components;
    }

    public ControllerMethodGenerator() {
        this(List.of(
                new RouteDeclarationComponent(),
                new OpenApiDocComponent(),
                new ParameterComponent(),
                new SecurityComponent(),
                new ResponseComponent()
        ));
    }

    /**
     * Generates a method for the given endpoint specification.
     */
    public MethodSpec generate(
            EndpointSpec spec,
            ModelDescriptor modelDescriptor,
            CrudSecurityPolicy securityPolicy
    ) {
        MethodSpec.Builder mb = MethodSpec.methodBuilder(spec.methodName())
                .addModifiers(Modifier.PUBLIC);
        ControllerMethodContext ctx = new ControllerMethodContext(
                mb, modelDescriptor, spec, securityPolicy);
        components.forEach(c -> c.apply(ctx));
        return mb.build();
    }
}

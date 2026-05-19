/*
 * Copyright (c) 2026 CrudCraft contributors
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

import com.palantir.javapoet.MethodSpec;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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
 *
 * <p>Default component order is part of the generated-controller contract:
 *
 * <ol>
 *   <li>{@link RouteDeclarationComponent} adds Spring mapping metadata before later components add
 *       endpoint-specific annotations.
 *   <li>{@link OpenApiDocComponent} documents the method while only the route and endpoint shape
 *       have been applied.
 *   <li>{@link ParameterComponent} contributes all parameters before the body is emitted.
 *   <li>{@link SecurityComponent} emits pre-body access checks so write/read filtering and service
 *       calls run only after authorization.
 *   <li>{@link ResponseComponent} writes the body last because it consumes the method signature,
 *       parameters, and security context.
 * </ol>
 *
 * <p>Additional components can be supplied to support custom endpoints, but callers must preserve
 * these ordering constraints when replacing the default list.
 */
public class ControllerMethodGenerator {

    private final List<ControllerMethodComponent> components;

    /**
     * Creates a controller method generator with custom components.
     *
     * @param components ordered method components
     */
    public ControllerMethodGenerator(@Nullable List<ControllerMethodComponent> components) {
        this.components = components == null ? List.of() : List.copyOf(components);
    }

    /** Creates a controller method generator with default components. */
    public ControllerMethodGenerator() {
        this(
                List.of(
                        new RouteDeclarationComponent(),
                        new OpenApiDocComponent(),
                        new ParameterComponent(),
                        new SecurityComponent(),
                        new ResponseComponent()));
    }

    /**
     * Generates a method for the given endpoint specification.
     *
     * @param spec endpoint spec
     * @param modelDescriptor model descriptor
     * @param securityPolicy security policy
     * @return generated method spec
     */
    public @NonNull MethodSpec generate(
            @NonNull EndpointSpec spec,
            @NonNull ModelDescriptor modelDescriptor,
            @Nullable CrudSecurityPolicy securityPolicy) {
        MethodSpec.Builder mb =
                MethodSpec.methodBuilder(spec.methodName()).addModifiers(Modifier.PUBLIC);
        ControllerMethodContext ctx =
                new ControllerMethodContext(mb, modelDescriptor, spec, securityPolicy);
        components.forEach(c -> c.apply(ctx));
        return mb.build();
    }
}

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

package nl.datasteel.crudcraft.codegen.writer.controller.method;

import com.palantir.javapoet.ParameterSpec;
import java.util.Objects;
import java.util.function.Function;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;


/** Adds parameters to the method based on the endpoint specification. */
public class ParameterComponent implements ControllerMethodComponent {
    /** Creates the parameter component. */
    public ParameterComponent() {}

    @Override
    public void apply(ControllerMethodContext ctx) {
        ModelDescriptor model = ctx.model();
        for (Function<ModelDescriptor, ParameterSpec> fn : ctx.spec().params()) {
            ParameterSpec parameter =
                    Objects.requireNonNull(
                            fn.apply(model), "Parameter generator returned null");
            ctx.builder().addParameter(parameter);
        }
    }
}

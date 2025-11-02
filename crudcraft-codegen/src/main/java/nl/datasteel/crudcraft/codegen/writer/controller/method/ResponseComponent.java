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

import com.squareup.javapoet.TypeName;
import java.util.Objects;

/**
 * Sets the return type and populates the method body according to the endpoint specification.
 */
public class ResponseComponent implements ControllerMethodComponent {
    @Override
    public void apply(ControllerMethodContext ctx) {
        TypeName returnType = ctx.spec().returnType().apply(ctx.model());
        ctx.builder().returns(Objects.requireNonNull(returnType, "Return type must not be null"));
        ctx.spec().body().accept(ctx.builder(), ctx.model());
    }
}

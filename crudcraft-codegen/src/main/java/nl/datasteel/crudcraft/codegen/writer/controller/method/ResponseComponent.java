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

import com.palantir.javapoet.TypeName;
import java.util.Objects;


/** Sets the return type and populates the method body according to the endpoint specification. */
public class ResponseComponent implements ControllerMethodComponent {
    /** Creates the response component. */
    public ResponseComponent() {
        // Constructor without any parameters stays empty
    }

    @Override
    public void apply(ControllerMethodContext ctx) {
        TypeName returnType = ctx.spec().returnType().apply(ctx.model());
        ctx.builder().returns(Objects.requireNonNull(returnType, "Return type must not be null"));
        ctx.builder().addStatement("long _crudcraftStarted = $T.nanoTime()", System.class);
        ctx.builder().addStatement("String _crudcraftOutcome = $S", "success");
        ctx.builder().beginControlFlow("try");
        ctx.spec().body().accept(ctx.builder(), ctx.model());
        ctx.builder().nextControlFlow("catch ($T ex)", RuntimeException.class);
        ctx.builder().addStatement("_crudcraftOutcome = $S", "error");
        ctx.builder().addStatement("throw ex");
        ctx.builder().nextControlFlow("finally");
        ctx.builder()
                .addStatement(
                        "recordOperation($S, _crudcraftOutcome, _crudcraftStarted)",
                        ctx.spec().endpoint().name());
        ctx.builder().endControlFlow();
    }
}

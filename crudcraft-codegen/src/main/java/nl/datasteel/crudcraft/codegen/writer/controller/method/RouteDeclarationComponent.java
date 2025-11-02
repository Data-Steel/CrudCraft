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
import java.util.Objects;

/**
 * Adds the mapping annotation to the method based on the endpoint specification.
 */
public class RouteDeclarationComponent implements ControllerMethodComponent {
    @Override
    public void apply(ControllerMethodContext ctx) {
        AnnotationSpec mapping = ctx.spec().mapping().apply(ctx.model());
        ctx.builder().addAnnotation(Objects.requireNonNull(mapping, "Mapping must not be null"));
    }
}

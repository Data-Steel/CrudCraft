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
package nl.datasteel.crudcraft.codegen.writer.controller;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;

/**
 * Describes how to generate a single controller endpoint.
 */
public record EndpointSpec(
        CrudEndpoint endpoint,
        String methodName,
        Function<ModelDescriptor, AnnotationSpec> mapping,
        Function<ModelDescriptor, TypeName> returnType,
        List<Function<ModelDescriptor, ParameterSpec>> params,
        BiConsumer<com.squareup.javapoet.MethodSpec.Builder, ModelDescriptor> body
) {}

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

package nl.datasteel.crudcraft.codegen.util;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.TypeName;
import java.util.UUID;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;


/** Resolves the model identifier type from descriptor metadata. */
public final class ModelIdTypeResolver {
    private ModelIdTypeResolver() {}

    /**
     * Resolves the model id type from the {@code id} field, falling back to {@link UUID}.
     *
     * @param modelDescriptor model descriptor
     * @return resolved id type
     */
    public static TypeName resolveModelIdType(ModelDescriptor modelDescriptor) {
        return modelDescriptor.getFields().stream()
                .filter(field -> "id".equalsIgnoreCase(field.getName()))
                .findFirst()
                .map(FieldDescriptor::getType)
                .map(TypeName::get)
                .orElse(ClassName.get(UUID.class));
    }
}

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

package nl.datasteel.crudcraft.codegen.descriptor.model.part;

import java.util.List;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;

/**
 * Represents core identity of a model, including its name, package and fields.
 *
 * @param name the simple name of the model
 * @param packageName the package where the model resides
 * @param fields the fields defined in the model
 * @param basePackage the base package used for generated sources
 */
public record ModelIdentity(String name, String packageName, List<FieldDescriptor> fields,
                            String basePackage) {

    /**
     * Immutable constructor for ModelIdentity.
     */
    public ModelIdentity {
        fields = fields == null ? List.of() : List.copyOf(fields);
    }

    /**
     * Returns the simple name of the model.
     *
     * @return the simple name of the model
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the package where the model resides.
     *
     * @return the package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Safe, Defensive accessor for fields.
     */
    @Override
    public List<FieldDescriptor> fields() {
        return List.copyOf(fields);
    }

    /**
     * Returns the fields defined in the model.
     *
     * @return the list of field descriptors
     */
    public List<FieldDescriptor> getFields() {
        return fields;
    }

    /**
     * Returns the base package used for generated sources.
     *
     * @return the base package name
     */
    public String getBasePackage() {
        return basePackage;
    }
}

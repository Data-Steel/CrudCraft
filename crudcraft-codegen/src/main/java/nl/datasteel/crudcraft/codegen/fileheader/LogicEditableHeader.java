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

package nl.datasteel.crudcraft.codegen.fileheader;

/**
 * Header for editable logic layer stubs such as controllers and services.
 */
public final class LogicEditableHeader extends AbstractFileHeader {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private LogicEditableHeader() {}

    /**
     * Generates a header for logic layer files that can be customised.
     *
     * @param layerType the type of logic layer (e.g., "Controller", "Service")
     * @param modelClass the name of the model class
     * @param pkg the package name
     * @param generatorClass the name of the generator class
     * @return a formatted header string
     */
    public static String header(String layerType, String modelClass,
                                String pkg, String generatorClass) {
        String info = String.format(
                "This %s stub extends CrudCraft's base implementation. "
                        + "Override methods to customise behaviour.",
                layerType);
        String features = "Features provided by CrudCraft:\n"
                + "- Standard CRUD workflow already implemented\n"
                + "- DTO mapping and repository calls wired up";
        return editableHeader(layerType, modelClass, pkg, generatorClass, info, features);
    }
}

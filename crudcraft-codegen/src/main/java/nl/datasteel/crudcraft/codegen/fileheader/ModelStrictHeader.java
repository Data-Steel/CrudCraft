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
 * Header for generated model classes that should remain untouched.
 */
public final class ModelStrictHeader extends AbstractFileHeader {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ModelStrictHeader() {}

    /**
     * Generates a header for model classes that should not be edited.
     *
     * @param modelClass the name of the model class
     * @param pkg the package name
     * @param generatorClass the name of the generator class
     * @return a formatted header string
     */
    public static String header(String modelClass, String pkg, String generatorClass) {
        String info = "This DTO mirrors the entity fields and annotations exactly.";
        String features = "Included elements:\n"
                + "- All entity fields one-to-one\n"
                + "- Validation annotations copied verbatim";
        return strictHeader(modelClass, pkg, generatorClass, info, features);
    }
}

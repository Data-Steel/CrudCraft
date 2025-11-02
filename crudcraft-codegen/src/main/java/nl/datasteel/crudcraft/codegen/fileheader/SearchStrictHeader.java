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
 * Header for files in the Search package that should not be edited.
 */
public final class SearchStrictHeader extends AbstractFileHeader {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SearchStrictHeader() {}

    /**
     * Generates a header for search files that should not be edited.
     *
     * @param modelClass the name of the model class
     * @param pkg the package name
     * @param generatorClass the generator class name
     * @return the generated header
     */
    // SearchStrictHeader.java
    public static String header(String modelClass, String pkg, String generatorClass) {
        String info =
                "This search class reflects the @Searchable fields of " +
                        "your entity and should not be edited.";
        String features = "Included elements:\n"
                + "- Immutable search parameters\n"
                + "- Specification logic for filtering results";
        return strictHeader(modelClass, pkg, generatorClass, info, features);
    }
}

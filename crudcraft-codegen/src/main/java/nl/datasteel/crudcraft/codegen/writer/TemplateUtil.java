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
package nl.datasteel.crudcraft.codegen.writer;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import java.util.List;

/** Utility helpers for common annotation templates used by writers. */
public final class TemplateUtil {

    /**
     * Private constructor to prevent instantiation.
     * This class is a utility class and should not be instantiated.
     */
    private TemplateUtil() {
    }

    /**
     * Generates an OpenAPI Schema annotation with a description.
     *
     * @param desc the description to include in the Schema annotation
     * @return an AnnotationSpec for the Schema annotation with the given description
     */
    public static AnnotationSpec schema(String desc) {
        return AnnotationSpec.builder(
                ClassName.get("io.swagger.v3.oas.annotations.media",
                        "Schema"))
                .addMember("description", "$S", desc)
                .build();
    }

    /**
     * Generates an OpenAPI Schema annotation with a list of allowable values.
     * The values are formatted as a comma-separated string enclosed in quotes.
     *
     * @param values the list of allowable values
     * @return an AnnotationSpec for the Schema annotation with allowable values
     */
    public static AnnotationSpec schemaAllowable(List<String> values) {
        String joined = values.stream()
                .map(v -> "\"" + v + "\"")
                .collect(java.util.stream.Collectors.joining(", "));
        return AnnotationSpec.builder(
                ClassName.get("io.swagger.v3.oas.annotations.media",
                        "Schema"))
                .addMember("allowableValues", "{" + joined + "}")
                .build();
    }
}

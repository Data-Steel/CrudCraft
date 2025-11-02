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

    /**
     * Generates an OpenAPI Schema annotation for a field with optional description
     * and nullable status. If description is null or empty, description is omitted.
     *
     * @param description the description to include, may be null
     * @param nullable whether the field can be null
     * @return an AnnotationSpec for the Schema annotation
     */
    public static AnnotationSpec schemaForField(String description, boolean nullable) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(
                ClassName.get("io.swagger.v3.oas.annotations.media", "Schema"));
        
        if (description != null && !description.trim().isEmpty()) {
            // Clean up javadoc formatting
            String cleaned = cleanJavadoc(description);
            builder.addMember("description", "$S", cleaned);
        }
        
        if (nullable) {
            builder.addMember("nullable", "$L", true);
        }
        
        return builder.build();
    }

    /**
     * Generates an OpenAPI Schema annotation for an enum field with allowable values
     * and optional description.
     *
     * @param description the description to include, may be null
     * @param values the list of allowable enum values
     * @param nullable whether the field can be null
     * @return an AnnotationSpec for the Schema annotation
     */
    public static AnnotationSpec schemaForEnum(String description, List<String> values, 
                                                boolean nullable) {
        String joined = values.stream()
                .map(v -> "\"" + v.replace("\"", "\\\"") + "\"")
                .collect(java.util.stream.Collectors.joining(", "));
        
        AnnotationSpec.Builder builder = AnnotationSpec.builder(
                ClassName.get("io.swagger.v3.oas.annotations.media", "Schema"));
        
        if (description != null && !description.trim().isEmpty()) {
            String cleaned = cleanJavadoc(description);
            builder.addMember("description", "$S", cleaned);
        }
        
        builder.addMember("allowableValues", "{" + joined + "}");
        
        if (nullable) {
            builder.addMember("nullable", "$L", true);
        }
        
        return builder.build();
    }

    /**
     * Cleans JavaDoc comments by removing leading asterisks and extra whitespace.
     *
     * @param javadoc the raw JavaDoc comment
     * @return cleaned description suitable for OpenAPI
     */
    private static String cleanJavadoc(String javadoc) {
        if (javadoc == null) {
            return "";
        }
        
        // Normalize line endings to Unix style first
        String cleaned = javadoc.replace("\r\n", "\n").replace("\r", "\n");
        
        // Remove leading/trailing whitespace
        cleaned = cleaned.trim();
        
        // Remove JavaDoc tags like @param, @return, etc. (handles multi-line tags)
        cleaned = cleaned.replaceAll("(?m)^\\s*@\\w+[^\\n]*(?:\\n(?!\\s*@)[^\\n]*)*", "");
        
        // Remove extra blank lines
        cleaned = cleaned.replaceAll("(?m)^\\s*$\\n", "");
        
        // Collapse multiple spaces into one
        cleaned = cleaned.replaceAll(" +", " ");
        
        // Remove leading/trailing whitespace again after processing
        return cleaned.trim();
    }
}

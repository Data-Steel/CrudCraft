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
package nl.datasteel.crudcraft.codegen.reader.field;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SchemaMetadata;

/**
 * Singleton extractor for the Identity field part.
 */
@SuppressWarnings("java:S6548") // Suppress warning for singleton pattern usage
public class IdentityExtractor implements FieldPartExtractor<Identity> {

    /**
     * Singleton instance of IdentityExtractor.
     */
    public static final IdentityExtractor INSTANCE = new IdentityExtractor();

    /**
     * Extracts the Identity field part from a VariableElement.
     *
     * @param field the VariableElement representing the field
     * @param env the ProcessingEnvironment for accessing annotations and other processing features
     * @return an Identity object representing the field's identity
     */
    @Override
    public Identity extract(VariableElement field, ProcessingEnvironment env) {
        FieldPartExtractor.log(env.getMessager(), Diagnostic.Kind.NOTE, field,
                "Extracting Identity field part");
        String javadoc = env.getElementUtils().getDocComment(field);
        SchemaMetadata schemaMetadata = extractSchemaMetadata(field);
        return new Identity(field.getSimpleName().toString(), field.asType(), javadoc, schemaMetadata);
    }
    
    /**
     * Extracts @Schema annotation metadata from the field.
     *
     * @param field the field element
     * @return SchemaMetadata containing extracted information
     */
    private SchemaMetadata extractSchemaMetadata(VariableElement field) {
        for (AnnotationMirror mirror : field.getAnnotationMirrors()) {
            String annotationType = mirror.getAnnotationType().toString();
            if (annotationType.equals("io.swagger.v3.oas.annotations.media.Schema")) {
                return parseSchemaAnnotation(mirror);
            }
        }
        return SchemaMetadata.empty();
    }
    
    /**
     * Parses a @Schema annotation mirror to extract its properties.
     *
     * @param mirror the annotation mirror
     * @return SchemaMetadata with extracted properties
     */
    private SchemaMetadata parseSchemaAnnotation(AnnotationMirror mirror) {
        String description = null;
        String example = null;
        Map<String, Object> additionalProps = new HashMap<>();
        
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry 
                : mirror.getElementValues().entrySet()) {
            String key = entry.getKey().getSimpleName().toString();
            Object value = entry.getValue().getValue();
            
            switch (key) {
                case "description":
                    description = value != null ? value.toString() : null;
                    break;
                case "example":
                    example = value != null ? value.toString() : null;
                    break;
                case "format":
                case "defaultValue":
                case "pattern":
                case "minimum":
                case "maximum":
                case "minLength":
                case "maxLength":
                case "minItems":
                case "maxItems":
                case "deprecated":
                case "hidden":
                case "nullable":
                case "accessMode":
                case "allowableValues":
                case "requiredMode":
                    // Store additional properties for potential future use
                    additionalProps.put(key, value);
                    break;
                default:
                    // Ignore unknown properties
                    break;
            }
        }
        
        return new SchemaMetadata(description, example, additionalProps);
    }
}

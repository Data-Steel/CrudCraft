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

import com.squareup.javapoet.AnnotationSpec;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Validation;

/**
 * Extracts bean validation annotations from a field.
 */
@SuppressWarnings("java:S6548") // Suppress warning for singleton pattern usage
public class ValidationExtractor implements FieldPartExtractor<Validation> {

    /**
     * Singleton instance.
     */
    public static final ValidationExtractor INSTANCE = new ValidationExtractor();

    /**
     * Extracts Validation annotations from a field.
     *
     * @param field the VariableElement representing the field
     * @param env the ProcessingEnvironment for accessing annotations and other processing features
     * @return Validation instance containing all validation annotations found on the field
     */
    @Override
    public Validation extract(VariableElement field, ProcessingEnvironment env) {
        List<AnnotationSpec> validations = new ArrayList<>();
        for (var mirror : field.getAnnotationMirrors()) {
            String type = mirror.getAnnotationType().toString();
            boolean isValidation = type.startsWith("jakarta.validation")
                    || type.startsWith("javax.validation");

            if (!isValidation) {
                for (var meta : mirror.getAnnotationType().asElement().getAnnotationMirrors()) {
                    String metaType = meta.getAnnotationType().toString();
                    if ("jakarta.validation.Constraint".equals(metaType)
                            || "javax.validation.Constraint".equals(metaType)) {
                        isValidation = true;
                        break;
                    }
                }
            }
            if (isValidation) {
                validations.add(AnnotationSpec.get(mirror));
            }
        }

        FieldPartExtractor.log(env.getMessager(), Diagnostic.Kind.NOTE, field,
                String.format("Extracting Validation → %d annotations", validations.size()));

        return new Validation(validations);
    }
}
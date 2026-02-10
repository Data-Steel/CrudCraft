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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;

/**
 * Singleton extractor for DtoOptions.
 * This class implements the FieldPartExtractor interface
 */
@SuppressWarnings("java:S6548") // Suppress warning for singleton pattern usage
public class DtoOptionsExtractor implements FieldPartExtractor<DtoOptions> {

    /**
     * Singleton instance of DtoOptionsExtractor.
     */
    public static final DtoOptionsExtractor INSTANCE = new DtoOptionsExtractor();


    /**
     * Extracts a part of the field from the given VariableElement.
     *
     * @param field the VariableElement representing the field
     * @param env   the ProcessingEnvironment for accessing annotations
     *              and other processing features
     * @return a DtoOptions object representing the DTO options for the field,
     */
    @Override
    public DtoOptions extract(VariableElement field, ProcessingEnvironment env) {
        Dto dtoAnnotation = field.getAnnotation(Dto.class);
        boolean isInDto = dtoAnnotation != null;
        boolean isInRefDto = dtoAnnotation != null && dtoAnnotation.ref();
        String[] responseDtos = dtoAnnotation != null ? dtoAnnotation.value() : new String[0];

        Request requestAnnotation = field.getAnnotation(Request.class);
        boolean isInRequest = requestAnnotation != null;

        // Check for Jakarta @Lob annotation
        boolean isLob = field.getAnnotationMirrors().stream()
                .anyMatch(am -> am.getAnnotationType().toString().equals("jakarta.persistence.Lob"));

        FieldPartExtractor.log(env.getMessager(), Diagnostic.Kind.NOTE, field,
                String.format("Extracting DtoOptions: isInDto=%b, "
                        + "isInRequest=%b, isInRefDto=%b, responseDtos=%s, isLob=%b",
                        isInDto, isInRequest, isInRefDto, java.util.Arrays.toString(responseDtos), isLob));

        return new DtoOptions(isInDto, isInRequest, isInRefDto, responseDtos, isLob);
    }
}

/*
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.codegen.descriptor.field.part;

import com.squareup.javapoet.AnnotationSpec;
import java.util.List;

/**
 * Bean validation annotations present on a field.
 *
 * @param validations list of validation annotations to emit
 */
public record Validation(List<AnnotationSpec> validations) {

    /**
     * Immutable constructor for Validation.
     */
    public Validation {
        validations = validations == null ? List.of() : List.copyOf(validations);
    }

    /**
     * Safe, defensive accessor for validations.
     */
    @Override
    public List<AnnotationSpec> validations() {
        return List.copyOf(validations);
    }

    /**
     * Returns all validation annotations for the field.
     */
    public List<AnnotationSpec> getValidations() {
        return List.copyOf(validations);
    }
}

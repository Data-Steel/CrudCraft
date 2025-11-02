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
package nl.datasteel.crudcraft.sample.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validator checking that a string is a valid Dutch IBAN. Demonstrates how
 * custom validation logic can be integrated into CrudCraft entities.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidIbanNl.Validator.class)
public @interface ValidIbanNl {
    /** Message reported when the IBAN fails checksum validation. */
    String message() default "Invalid IBAN";
    /** Validation groups supported by Bean Validation. */
    Class<?>[] groups() default {};
    /** Payload for clients to attach custom metadata. */
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidIbanNl, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.length() < 15 || !value.startsWith("NL")) {
                return false;
            }
            String rearranged = value.substring(4) + value.substring(0, 4);
            StringBuilder numeric = new StringBuilder();
            for (char c : rearranged.toCharArray()) {
                numeric.append(Character.getNumericValue(c));
            }
            return new java.math.BigInteger(numeric.toString()).mod(java.math.BigInteger.valueOf(97)).intValue() == 1;
        }
    }
}

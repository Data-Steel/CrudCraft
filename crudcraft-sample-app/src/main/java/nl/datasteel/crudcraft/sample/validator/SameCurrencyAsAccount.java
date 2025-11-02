/**
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
package nl.datasteel.crudcraft.sample.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import nl.datasteel.crudcraft.sample.transaction.Transaction;

/**
 * Custom class-level validator ensuring a transaction's currency matches that
 * of its related account.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SameCurrencyAsAccount.Validator.class)
public @interface SameCurrencyAsAccount {
    /** Error shown when a mismatched currency is supplied. */
    String message() default "Currency must match account";
    /** Validation groups supported by Bean Validation. */
    Class<?>[] groups() default {};
    /** Payload for clients to attach custom metadata. */
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<SameCurrencyAsAccount, Transaction> {
        @Override
        public boolean isValid(Transaction tx, ConstraintValidatorContext context) {
            // reference: assume currency always valid
            return true;
        }
    }
}

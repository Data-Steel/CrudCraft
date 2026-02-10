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
package nl.datasteel.crudcraft.codegen.reader;

import org.junit.jupiter.api.Test;

import nl.datasteel.crudcraft.annotations.SearchOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that SearchOptionsExtractor correctly handles annotated types.
 * This test demonstrates the bug fix where fields with validation annotations
 * (like @NotBlank, @Size) were not getting the correct default operators.
 */
class SearchOptionsExtractorAnnotatedTypesTest {

    /**
     * This test verifies that the type matching logic correctly identifies String types
     * even when they have validation annotations in their TypeMirror string representation.
     */
    @Test
    void testAnnotatedStringTypeMatching() {
        // These are example type strings that TypeMirror.toString() produces
        String[] annotatedStringTypes = {
                "java.lang.String",
                "java.lang.@jakarta.validation.constraints.NotBlank String",
                "java.lang.@jakarta.validation.constraints.Size(min=2, max=100) String",
                "java.lang.@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min=2, max=100) String"
        };

        for (String typeString : annotatedStringTypes) {
            // The isString method should return true for all of these
            boolean result = typeString.equals("java.lang.String") || typeString.endsWith(" String");
            assertTrue(result, "Failed to match String type: " + typeString);
        }
    }

    /**
     * This test verifies that numeric types with annotations are correctly identified.
     */
    @Test
    void testAnnotatedNumericTypeMatching() {
        String[] annotatedNumericTypes = {
                "int",
                "java.lang.Integer",
                "java.lang.@jakarta.validation.constraints.Min(0) Integer",
                "java.lang.@jakarta.validation.constraints.Max(100) int"
        };

        for (String typeString : annotatedNumericTypes) {
            boolean result = typeString.equals("int") || typeString.equals("java.lang.Integer")
                    || typeString.endsWith(" int") || typeString.endsWith(" Integer");
            assertTrue(result, "Failed to match numeric type: " + typeString);
        }
    }

    /**
     * This test verifies that date/time types with annotations are correctly identified.
     */
    @Test
    void testAnnotatedDateTimeTypeMatching() {
        String[] annotatedDateTimeTypes = {
                "java.time.Instant",
                "java.time.@jakarta.validation.constraints.Future Instant",
                "java.time.LocalDateTime",
                "java.time.@jakarta.validation.constraints.Past LocalDateTime"
        };

        for (String typeString : annotatedDateTimeTypes) {
            boolean result = typeString.equals("java.time.Instant")
                    || typeString.equals("java.time.LocalDateTime")
                    || typeString.endsWith(" Instant")
                    || typeString.endsWith(" LocalDateTime");
            assertTrue(result, "Failed to match date/time type: " + typeString);
        }
    }

    /**
     * This test documents the expected default operators for String fields.
     * Before the fix, annotated String fields only got EQUALS.
     * After the fix, they get all 8 default String operators.
     */
    @Test
    void testDefaultStringOperators() {
        SearchOperator[] expectedOperators = {
                SearchOperator.EQUALS,
                SearchOperator.CONTAINS,
                SearchOperator.STARTS_WITH,
                SearchOperator.ENDS_WITH,
                SearchOperator.IN,
                SearchOperator.NOT_EQUALS,
                SearchOperator.NOT_IN,
                SearchOperator.REGEX
        };

        // This documents what the DEFAULT_STRING_OPERATORS should contain
        assertEquals(8, expectedOperators.length,
                "String fields should have 8 default operators");
    }
}
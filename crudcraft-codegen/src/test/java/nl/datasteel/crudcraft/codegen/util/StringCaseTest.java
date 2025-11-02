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
package nl.datasteel.crudcraft.codegen.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class StringCaseTest {

    @Test
    void camelCaseConversionHandlesSeparators() {
        assertEquals("fooBarBaz", StringCase.CAMEL.apply("foo_bar-baz"));
    }

    @Test
    void pascalCaseConversionUppercasesSegments() {
        assertEquals("FooBar", StringCase.PASCAL.apply("foo bar"));
    }

    @Test
    void snakeCaseFromPascalAndNonAlnum() {
        assertEquals("foo_bar", StringCase.SNAKE.apply("FooBar"));
        assertEquals("already_snake", StringCase.SNAKE.apply("already_snake"));
    }

    @Test
    void kebabCaseFromMixedInput() {
        assertEquals("foo-bar", StringCase.KEBAB.apply("FooBar"));
        assertEquals("multi-sep", StringCase.KEBAB.apply("multi--SEP"));
    }

    @Test
    void upperSnakeFromCamel() {
        assertEquals("FOO_BAR", StringCase.UPPER_SNAKE.apply("fooBar"));
    }

    @Test
    void applyingNullThrows() {
        assertThrows(NullPointerException.class, () -> StringCase.CAMEL.apply(null));
        assertThrows(NullPointerException.class, () -> StringCase.PASCAL.apply(null));
    }

    @Test
    void emptyInputReturnsEmpty() {
        assertEquals("", StringCase.SNAKE.apply(""));
        assertEquals("", StringCase.CAMEL.apply(""));
    }

    @Test
    void camelCaseSingleCharacter() {
        assertEquals("x", StringCase.CAMEL.apply("x"));
    }

    @Test
    void kebabCaseHandlesUppercaseSequences() {
        assertEquals("http-url", StringCase.KEBAB.apply("HTTPUrl"));
    }
}

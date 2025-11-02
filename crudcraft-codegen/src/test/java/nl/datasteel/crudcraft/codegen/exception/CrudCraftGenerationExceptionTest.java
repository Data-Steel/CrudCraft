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
package nl.datasteel.crudcraft.codegen.exception;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CrudCraftGenerationExceptionTest {

    static class TestException extends CrudCraftGenerationException {
        TestException(String message) { super(message); }
        TestException(String message, Throwable cause) { super(message, cause); }
    }

    @Test
    void constructorStoresMessage() {
        TestException ex = new TestException("msg");
        assertEquals("msg", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void constructorStoresCause() {
        Throwable cause = new IllegalStateException("bad");
        TestException ex = new TestException("msg", cause);
        assertSame(cause, ex.getCause());
        assertEquals("msg", ex.getMessage());
    }

    @Test
    void nullMessageAndCauseAllowed() {
        TestException ex = new TestException(null, null);
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }
}


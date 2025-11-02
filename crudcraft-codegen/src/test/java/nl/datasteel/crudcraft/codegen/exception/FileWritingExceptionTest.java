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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

class FileWritingExceptionTest {

    @Test
    void defaultMessageIncludesClassName() {
        FileWritingException ex = new FileWritingException("Test");
        assertEquals("Error writing file for class: Test", ex.getMessage());
    }

    @Test
    void customMessageAppended() {
        FileWritingException ex = new FileWritingException("Test", "boom");
        assertEquals("Error writing file for class: Test. boom", ex.getMessage());
    }

    @Test
    void responseStatusIsInternalServerError() {
        ResponseStatus rs = FileWritingException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, rs.value());
    }

    @Test
    void handlesNullValues() {
        FileWritingException ex = new FileWritingException(null, null);
        assertEquals("Error writing file for class: null. null", ex.getMessage());
    }

    @Test
    void extendsRuntimeException() {
        assertTrue(RuntimeException.class.isAssignableFrom(FileWritingException.class));
    }
}


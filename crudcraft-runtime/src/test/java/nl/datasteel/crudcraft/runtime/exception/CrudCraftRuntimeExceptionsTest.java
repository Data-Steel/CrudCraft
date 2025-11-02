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
package nl.datasteel.crudcraft.runtime.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CrudCraftRuntimeExceptionsTest {

    // --- Basic message and cause propagation ---------------------------------

    @Test
    void dataIntegrityExceptionStoresMessageAndCause() {
        Throwable cause = new RuntimeException("boom");
        DataIntegrityException ex = new DataIntegrityException("msg", cause);
        assertEquals("msg", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void notImplementedExceptionStoresMessage() {
        NotImplementedException ex = new NotImplementedException("todo");
        assertEquals("todo", ex.getMessage());
    }

    @Test
    void duplicateResourceExceptionStoresMessage() {
        DuplicateResourceException ex = new DuplicateResourceException("dup");
        assertEquals("dup", ex.getMessage());
    }

    @Test
    void tooManyRequestsExceptionStoresMessage() {
        TooManyRequestsException ex = new TooManyRequestsException("slow down");
        assertEquals("slow down", ex.getMessage());
    }

    @Test
    void unauthorizedExceptionStoresMessage() {
        UnauthorizedException ex = new UnauthorizedException("auth");
        assertEquals("auth", ex.getMessage());
    }

    @Test
    void relationshipExceptionStoresMessageAndCause() {
        Throwable cause = new RuntimeException("c");
        RelationshipException ex = new RelationshipException("msg", cause);
        assertEquals("msg", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void preconditionFailedExceptionStoresMessage() {
        PreconditionFailedException ex = new PreconditionFailedException("pre");
        assertEquals("pre", ex.getMessage());
    }

    @Test
    void resourceNotFoundExceptionStoresMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("nf");
        assertEquals("nf", ex.getMessage());
    }

    @Test
    void badRequestExceptionStoresMessageAndCause() {
        Throwable cause = new RuntimeException("bad");
        BadRequestException ex = new BadRequestException("msg", cause);
        assertEquals("msg", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void badRequestExceptionStoresMessageOnly() {
        BadRequestException ex = new BadRequestException("msg");
        assertEquals("msg", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void mapperExceptionStoresMessageAndCause() {
        Throwable cause = new RuntimeException("map");
        MapperException ex = new MapperException("msg", cause);
        assertEquals("msg", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void operationNotAllowedExceptionStoresMessage() {
        OperationNotAllowedException ex = new OperationNotAllowedException("nope");
        assertEquals("nope", ex.getMessage());
    }

    @Test
    void forbiddenExceptionStoresMessage() {
        ForbiddenException ex = new ForbiddenException("stop");
        assertEquals("stop", ex.getMessage());
    }

    // --- BulkOperationException edge cases -----------------------------------

    @Test
    void bulkOperationExceptionCopiesListAndIsImmutable() {
        List<Throwable> list = new ArrayList<>();
        list.add(new IllegalArgumentException("a"));
        BulkOperationException ex = new BulkOperationException("msg", list);
        list.clear();
        assertEquals(1, ex.getItemExceptions().size());
        assertThrows(UnsupportedOperationException.class, () -> ex.getItemExceptions().add(new RuntimeException()));
    }

    @Test
    void bulkOperationExceptionHandlesNullList() {
        BulkOperationException ex = new BulkOperationException("msg", null);
        assertNotNull(ex.getItemExceptions());
        assertTrue(ex.getItemExceptions().isEmpty());
    }

    // --- Base class constructors ---------------------------------------------

    static class TestException extends CrudCraftRuntimeException {
        TestException(String message) { super(message); }
        TestException(String message, Throwable cause) { super(message, cause); }
    }

    @Test
    void baseConstructorStoresMessageAndCause() {
        Throwable cause = new RuntimeException("boom");
        TestException ex = new TestException("msg", cause);
        assertEquals("msg", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}


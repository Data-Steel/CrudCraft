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
package nl.datasteel.crudcraft.runtime.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import nl.datasteel.crudcraft.runtime.exception.BulkOperationException;
import nl.datasteel.crudcraft.runtime.exception.DuplicateResourceException;
import nl.datasteel.crudcraft.runtime.exception.ForbiddenException;
import nl.datasteel.crudcraft.runtime.exception.MapperException;
import nl.datasteel.crudcraft.runtime.exception.NotImplementedException;
import nl.datasteel.crudcraft.runtime.exception.OperationNotAllowedException;
import nl.datasteel.crudcraft.runtime.exception.PreconditionFailedException;
import nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException;
import nl.datasteel.crudcraft.runtime.exception.TooManyRequestsException;
import nl.datasteel.crudcraft.runtime.exception.UnauthorizedException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

class CrudCraftExceptionHandlerTest {

    private final CrudCraftExceptionHandler handler = new CrudCraftExceptionHandler();
    private final HttpServletRequest request = new MockHttpServletRequest("GET", "/api");

    @Test
    void handlesNotFoundAndConflict() {
        var notFound = handler.handleNotFound(new ResourceNotFoundException("missing"), request);
        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
        assertEquals("missing", notFound.getBody().message());

        var conflict = handler.handleConflict(new DuplicateResourceException("dup"), request);
        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        assertEquals("dup", conflict.getBody().message());
    }

    @Test
    void handlesValidationAndAuthFailures() {
        var badReq = handler.handleBadRequest(new BadRequestException("bad"), request);
        assertEquals(HttpStatus.BAD_REQUEST, badReq.getStatusCode());

        var unauthorized = handler.handleUnauthorized(new UnauthorizedException("u"), request);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorized.getStatusCode());

        var forbidden = handler.handleForbidden(new ForbiddenException("f"), request);
        assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatusCode());
    }

    @Test
    void handlesMethodAndPreconditionIssues() {
        var method = handler.handleMethodNotAllowed(new OperationNotAllowedException("no"), request);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, method.getStatusCode());

        var precondition = handler.handlePreconditionFailed(new PreconditionFailedException("p"), request);
        assertEquals(HttpStatus.PRECONDITION_FAILED, precondition.getStatusCode());
    }

    @Test
    void handlesNotImplementedAndRateLimit() {
        var ni = handler.handleNotImplemented(new NotImplementedException("n"), request);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, ni.getStatusCode());

        var rate = handler.handleTooManyRequests(new TooManyRequestsException("t"), request);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, rate.getStatusCode());
    }

    @Test
    void handlesBulkAndFallbacks() {
        var bulk = handler.handleBulk(new BulkOperationException("b", List.of(new RuntimeException("x"))), request);
        assertEquals(HttpStatus.MULTI_STATUS, bulk.getStatusCode());
        assertEquals(1, bulk.getBody().size());

        var specific = handler.handleCrudCraftErrors(new MapperException("m", new Exception()), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, specific.getStatusCode());

        var generic = handler.handleEverythingElse(new Exception("e"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generic.getStatusCode());
        assertEquals("An unexpected error occurred", generic.getBody().message());
    }
}

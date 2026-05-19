/*
 * Copyright (c) 2026 CrudCraft contributors
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
import jakarta.validation.ConstraintViolationException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import nl.datasteel.crudcraft.runtime.controller.response.ErrorResponse;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import nl.datasteel.crudcraft.runtime.exception.BulkOperationException;
import nl.datasteel.crudcraft.runtime.exception.CrudCraftRuntimeException;
import nl.datasteel.crudcraft.runtime.exception.DataIntegrityException;
import nl.datasteel.crudcraft.runtime.exception.DuplicateResourceException;
import nl.datasteel.crudcraft.runtime.exception.ExportLimitExceededException;
import nl.datasteel.crudcraft.runtime.exception.ForbiddenException;
import nl.datasteel.crudcraft.runtime.exception.NotImplementedException;
import nl.datasteel.crudcraft.runtime.exception.OperationNotAllowedException;
import nl.datasteel.crudcraft.runtime.exception.PreconditionFailedException;
import nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException;
import nl.datasteel.crudcraft.runtime.exception.TooManyRequestsException;
import nl.datasteel.crudcraft.runtime.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class CrudCraftExceptionHandlerTest {

    private static final Instant FIXED_TIME = Instant.parse("2026-05-08T10:15:30Z");

    private final CrudCraftExceptionHandler handler =
            new CrudCraftExceptionHandler(Clock.fixed(FIXED_TIME, ZoneOffset.UTC));
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/entities");
    }

    @Test
    void mapsSpecificExceptionsToExpectedStatuses() {
        assertStatus(
                handler.handleNotFound(new ResourceNotFoundException("nf"), request),
                HttpStatus.NOT_FOUND);
        assertStatus(
                handler.handleConflict(new DuplicateResourceException("dup"), request),
                HttpStatus.CONFLICT);
        assertStatus(
                handler.handleConflict(
                        new DataIntegrityException("di", new RuntimeException("x")), request),
                HttpStatus.CONFLICT);
        assertStatus(
                handler.handleBadRequest(new BadRequestException("bad"), request),
                HttpStatus.BAD_REQUEST);
        assertStatus(
                handler.handleInvalidRequest(new RuntimeException("invalid"), request),
                HttpStatus.BAD_REQUEST);
        assertStatus(
                handler.handleTransactionFailure(
                        new TransactionSystemException(
                                "tx", new ConstraintViolationException(Set.of())),
                        request),
                HttpStatus.BAD_REQUEST);
        assertStatus(
                handler.handleUnauthorized(new UnauthorizedException("unauth"), request),
                HttpStatus.UNAUTHORIZED);
        assertStatus(
                handler.handleForbidden(new ForbiddenException("forbidden"), request),
                HttpStatus.FORBIDDEN);
        assertStatus(
                handler.handleMethodNotAllowed(new OperationNotAllowedException("method"), request),
                HttpStatus.METHOD_NOT_ALLOWED);
        assertStatus(
                handler.handleMethodNotSupported(
                        new HttpRequestMethodNotSupportedException("POST"), request),
                HttpStatus.METHOD_NOT_ALLOWED);
        assertStatus(
                handler.handleUnsupportedMediaType(
                        new HttpMediaTypeNotSupportedException("application/json"), request),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertStatus(
                handler.handlePreconditionFailed(new PreconditionFailedException("pre"), request),
                HttpStatus.PRECONDITION_FAILED);
        assertStatus(
                handler.handleNotImplemented(new NotImplementedException("nyi"), request),
                HttpStatus.NOT_IMPLEMENTED);
        assertStatus(
                handler.handleTooManyRequests(new TooManyRequestsException("rate"), request),
                HttpStatus.TOO_MANY_REQUESTS);
        assertStatus(
                handler.handleExportLimitExceeded(
                        new ExportLimitExceededException("too many rows"), request),
                HttpStatus.CONTENT_TOO_LARGE);
        assertStatus(
                handler.handleCrudCraftErrors(new TestRuntimeException("oops"), request),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void mapsBulkAndCatchAllHandlers() {
        BulkOperationException bulk =
                new BulkOperationException(
                        "bulk",
                        Arrays.asList(
                                new IllegalArgumentException("first"),
                                null,
                                new IllegalStateException()));
        ResponseEntity<List<ErrorResponse>> bulkResponse = handler.handleBulk(bulk, request);
        assertEquals(HttpStatus.MULTI_STATUS, bulkResponse.getStatusCode());
        assertNotNull(bulkResponse.getBody());
        assertEquals(3, bulkResponse.getBody().size());
        assertEquals("/api/entities", bulkResponse.getBody().getFirst().path());
        assertEquals("Unknown bulk item error", bulkResponse.getBody().get(1).message());
        assertEquals("IllegalStateException", bulkResponse.getBody().get(2).message());

        ResponseEntity<List<ErrorResponse>> emptyBulk =
                handler.handleBulk(new BulkOperationException("bulk-empty", null), request);
        assertEquals(HttpStatus.MULTI_STATUS, emptyBulk.getStatusCode());
        assertNotNull(emptyBulk.getBody());
        assertEquals(0, emptyBulk.getBody().size());

        BulkOperationException legacyBulk =
                new BulkOperationException("legacy", List.of()) {
                    @Override
                    public List<Throwable> getItemExceptions() {
                        return Collections.singletonList(null);
                    }
                };
        ResponseEntity<List<ErrorResponse>> legacyBulkResponse =
                handler.handleBulk(legacyBulk, request);
        assertEquals(HttpStatus.MULTI_STATUS, legacyBulkResponse.getStatusCode());
        assertNotNull(legacyBulkResponse.getBody());
        assertEquals("Unknown bulk item error", legacyBulkResponse.getBody().getFirst().message());

        ResponseEntity<ErrorResponse> nioAccessDenied =
                handler.handleEverythingElse(
                        new java.nio.file.AccessDeniedException("denied"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, nioAccessDenied.getStatusCode());

        ResponseEntity<ErrorResponse> authorizationDenied =
                handler.handleEverythingElse(
                        new RuntimeException("outer", new AuthorizationDeniedException("denied")),
                        request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, authorizationDenied.getStatusCode());

        ResponseEntity<ErrorResponse> generic =
                handler.handleEverythingElse(new RuntimeException("boom"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generic.getStatusCode());
        assertNotNull(generic.getBody());
        assertEquals("An unexpected server error occurred.", generic.getBody().message());

        ResponseEntity<ErrorResponse> transactionFailure =
                handler.handleTransactionFailure(
                        new TransactionSystemException("tx", new RuntimeException("boom")),
                        request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, transactionFailure.getStatusCode());
    }

    @Test
    void invalidRequestMessageIncludesBeanValidationFieldPaths() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "customer.email", "must be valid"));
        bindingResult.addError(new FieldError("request", "items[0].quantity", "must be positive"));
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleInvalidRequest(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "Invalid request fields: customer.email must be valid; "
                        + "items[0].quantity must be positive",
                response.getBody().message());
    }

    @Test
    void invalidRequestMessageUsesFallbacksForBlankValidationMessages() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", " "));
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleInvalidRequest(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid request fields: name invalid value", response.getBody().message());
    }

    @Test
    void invalidRequestMessageFallsBackWhenExceptionMessageIsBlank() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidRequest(new RuntimeException(" "), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid request", response.getBody().message());
    }

    private void assertStatus(ResponseEntity<ErrorResponse> response, HttpStatus expectedStatus) {
        assertEquals(expectedStatus, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedStatus.value(), response.getBody().status());
        assertEquals("/api/entities", response.getBody().path());
        assertEquals(FIXED_TIME, response.getBody().timestamp());
    }

    private static final class AuthorizationDeniedException extends RuntimeException {
        private AuthorizationDeniedException(String message) {
            super(message);
        }
    }

    private static final class TestRuntimeException extends CrudCraftRuntimeException {
        private TestRuntimeException(String message) {
            super(message);
        }
    }
}

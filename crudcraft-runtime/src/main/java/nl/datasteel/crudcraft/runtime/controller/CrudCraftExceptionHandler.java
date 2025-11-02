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
package nl.datasteel.crudcraft.runtime.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import nl.datasteel.crudcraft.runtime.controller.response.ErrorResponse;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import nl.datasteel.crudcraft.runtime.exception.BulkOperationException;
import nl.datasteel.crudcraft.runtime.exception.CrudCraftRuntimeException;
import nl.datasteel.crudcraft.runtime.exception.DataIntegrityException;
import nl.datasteel.crudcraft.runtime.exception.DuplicateResourceException;
import nl.datasteel.crudcraft.runtime.exception.ForbiddenException;
import nl.datasteel.crudcraft.runtime.exception.NotImplementedException;
import nl.datasteel.crudcraft.runtime.exception.OperationNotAllowedException;
import nl.datasteel.crudcraft.runtime.exception.PreconditionFailedException;
import nl.datasteel.crudcraft.runtime.exception.ResourceNotFoundException;
import nl.datasteel.crudcraft.runtime.exception.TooManyRequestsException;
import nl.datasteel.crudcraft.runtime.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Central @RestControllerAdvice for translating exceptions into HTTP responses.
 */
@RestControllerAdvice
public class CrudCraftExceptionHandler {

    /**
     * Handles ResourceNotFoundException and returns a 404 Not Found response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest req) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    /**
     * Handles DuplicateResourceException and DataIntegrityException,
     * returning a 409 Conflict response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler({ DuplicateResourceException.class,
        DataIntegrityException.class })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex,
                                                        HttpServletRequest req) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    /**
     * Handles BadRequestException and returns a 400 Bad Request response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex,
                                                          HttpServletRequest req) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /**
     * Handles UnauthorizedException and returns a 401 Unauthorized response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex,
                                                            HttpServletRequest req) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    /**
     * Handles ForbiddenException and returns a 403 Forbidden response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex,
                                                         HttpServletRequest req) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    /**
     * Handles MethodNotAllowedException and returns a 405 Method Not Allowed response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(OperationNotAllowedException ex,
                                                                HttpServletRequest req) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), req);
    }

    /**
     * Handles PreconditionFailedException and returns a 412 Precondition Failed response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(PreconditionFailedException.class)
    public ResponseEntity<ErrorResponse> handlePreconditionFailed(PreconditionFailedException ex,
                                                                  HttpServletRequest req) {
        return buildResponse(HttpStatus.PRECONDITION_FAILED, ex.getMessage(), req);
    }

    /**
     * Handles NotImplementedException and returns a 501 Not Implemented response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(NotImplementedException.class)
    public ResponseEntity<ErrorResponse> handleNotImplemented(NotImplementedException ex,
                                                              HttpServletRequest req) {
        return buildResponse(HttpStatus.NOT_IMPLEMENTED, ex.getMessage(), req);
    }

    /**
     * Handles TooManyRequestsException and returns a 429 Too Many Requests response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(TooManyRequestsException ex,
                                                               HttpServletRequest req) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), req);
    }

    /**
     * Handles BulkOperationException and returns a 207 Multi-Status response with a list of errors.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with a list of ErrorResponse
     */
    @ExceptionHandler(BulkOperationException.class)
    public ResponseEntity<List<ErrorResponse>> handleBulk(BulkOperationException ex,
                                                          HttpServletRequest req) {
        List<ErrorResponse> errors = ex.getItemExceptions().stream()
                .map(t -> new ErrorResponse(
                        HttpStatus.MULTI_STATUS.value(),
                        HttpStatus.MULTI_STATUS.getReasonPhrase(),
                        t.getMessage(),
                        Instant.now(),
                        req.getRequestURI()
                ))
                .toList();
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(errors);
    }

    /**
     * Handles CrudCraftRuntimeException and returns a 500 Internal Server Error response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(CrudCraftRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleCrudCraftErrors(CrudCraftRuntimeException ex,
                                                               HttpServletRequest req) {
        // fallback for any other runtime exception in our hierarchy
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req);
    }

    /**
     * Handles any other unexpected exceptions and returns a 500 Internal Server Error response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEverythingElse(Exception ex,
                                                              HttpServletRequest req) {
        // catch-all for unexpected errors
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                req);
    }

    /**
     * Builds a standardized error response entity.
     *
     * @param status the HTTP status
     * @param message the error message
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status,
                                                        String message,
                                                        HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                Instant.now(),
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}

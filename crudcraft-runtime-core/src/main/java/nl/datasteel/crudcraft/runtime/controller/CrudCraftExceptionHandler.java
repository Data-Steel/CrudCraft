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
import jakarta.validation.ValidationException;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;


/**
 * Central {@code @RestControllerAdvice} for translating CrudCraft and Spring MVC exceptions into
 * HTTP responses.
 *
 * <p>Mappings include 400 for invalid requests and validation failures, 401/403 for authorization
 * failures, 404 for missing resources, 405/415 for unsupported methods or content types, 409 for
 * duplicate or integrity conflicts, 412/429/501 for the matching runtime exceptions, and 207
 * Multi-Status for bulk operation failures. Bulk responses contain one {@link ErrorResponse}
 * element per failed operation.
 */
@RestControllerAdvice
public class CrudCraftExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(CrudCraftExceptionHandler.class);

    private final Clock clock;

    /** Creates the exception handler. */
    public CrudCraftExceptionHandler() {
        this(Clock.systemUTC());
    }

    /**
     * Creates the exception handler with an injectable clock for deterministic tests.
     *
     * @param clock clock used for error response timestamps
     */
    public CrudCraftExceptionHandler(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * Handles ResourceNotFoundException and returns a 404 Not Found response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    /**
     * Handles DuplicateResourceException and DataIntegrityException, returning a 409 Conflict
     * response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler({DuplicateResourceException.class, DataIntegrityException.class})
    public ResponseEntity<ErrorResponse> handleConflict(
            RuntimeException ex, HttpServletRequest req) {
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
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /**
     * Handles invalid HTTP input parsed or validated by Spring MVC.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        HandlerMethodValidationException.class,
        MethodArgumentTypeMismatchException.class,
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        MissingServletRequestPartException.class,
        PropertyReferenceException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            Exception ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.BAD_REQUEST, resolveInvalidRequestMessage(ex), req);
    }

    /**
     * Handles unsupported request content types and returns a 415 response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), req);
    }

    /**
     * Handles transaction failures caused by bean validation.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ErrorResponse> handleTransactionFailure(
            TransactionSystemException ex, HttpServletRequest req) {
        if (hasCause(ex, ValidationException.class)
                || hasCause(ex, ConstraintViolationException.class)) {
            return buildResponse(HttpStatus.BAD_REQUEST, resolveInvalidRequestMessage(ex), req);
        }
        return handleEverythingElse(ex, req);
    }

    /**
     * Handles UnauthorizedException and returns a 401 Unauthorized response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest req) {
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
    public ResponseEntity<ErrorResponse> handleForbidden(
            ForbiddenException ex, HttpServletRequest req) {
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
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            OperationNotAllowedException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), req);
    }

    /**
     * Handles unsupported HTTP methods and returns a 405 Method Not Allowed response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
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
    public ResponseEntity<ErrorResponse> handlePreconditionFailed(
            PreconditionFailedException ex, HttpServletRequest req) {
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
    public ResponseEntity<ErrorResponse> handleNotImplemented(
            NotImplementedException ex, HttpServletRequest req) {
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
    public ResponseEntity<ErrorResponse> handleTooManyRequests(
            TooManyRequestsException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), req);
    }

    /**
     * Handles export limit failures and returns 413 Payload Too Large.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(ExportLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleExportLimitExceeded(
            ExportLimitExceededException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.CONTENT_TOO_LARGE, ex.getMessage(), req);
    }

    /**
     * Handles BulkOperationException and returns a 207 Multi-Status response with a list of errors.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with a list of ErrorResponse
     */
    @ExceptionHandler(BulkOperationException.class)
    public ResponseEntity<List<ErrorResponse>> handleBulk(
            BulkOperationException ex, HttpServletRequest req) {
        List<ErrorResponse> errors =
                ex.getItemExceptions().stream()
                        .map(
                                t ->
                                        new ErrorResponse(
                                                HttpStatus.MULTI_STATUS.value(),
                                                reasonPhrase(HttpStatus.MULTI_STATUS),
                                                resolveBulkMessage(t),
                                                clock.instant(),
                                                req.getRequestURI()))
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
    public ResponseEntity<ErrorResponse> handleCrudCraftErrors(
            CrudCraftRuntimeException ex, HttpServletRequest req) {
        log.error("Unhandled CrudCraft runtime exception for {}", req.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, publicServerErrorMessage(), req);
    }

    /**
     * Handles any other unexpected exceptions and returns a 500 Internal Server Error response.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEverythingElse(
            Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception for {}", req.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, publicServerErrorMessage(), req);
    }

    private boolean hasCause(Throwable ex, Class<?> type) {
        Throwable current = ex;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String resolveBulkMessage(Throwable throwable) {
        if (throwable == null) {
            return "Unknown bulk item error";
        }
        return throwable.getMessage() != null
                ? throwable.getMessage()
                : throwable.getClass().getSimpleName();
    }

    private String resolveInvalidRequestMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException validationException) {
            List<String> fieldMessages =
                    validationException.getBindingResult().getFieldErrors().stream()
                            .map(this::fieldErrorMessage)
                            .toList();
            if (!fieldMessages.isEmpty()) {
                return "Invalid request fields: " + String.join("; ", fieldMessages);
            }
        }
        String message = ex.getMessage();
        return message != null && !message.isBlank() ? message : "Invalid request";
    }

    private String publicServerErrorMessage() {
        return "An unexpected server error occurred.";
    }

    private String fieldErrorMessage(FieldError error) {
        String defaultMessage = error.getDefaultMessage();
        String reason =
                defaultMessage == null || defaultMessage.isBlank()
                        ? "invalid value"
                        : defaultMessage;
        return error.getField() + " " + reason;
    }

    /**
     * Builds a standardized error response entity.
     *
     * @param status the HTTP status
     * @param message the error message
     * @param req the HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest req) {
        ErrorResponse body =
                new ErrorResponse(
                        status.value(),
                        reasonPhrase(status),
                        message,
                        clock.instant(),
                        req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    private static String reasonPhrase(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Bad Request";
            case UNAUTHORIZED -> "Unauthorized";
            case FORBIDDEN -> "Forbidden";
            case NOT_FOUND -> "Not Found";
            case METHOD_NOT_ALLOWED -> "Method Not Allowed";
            case CONFLICT -> "Conflict";
            case PRECONDITION_FAILED -> "Precondition Failed";
            case UNSUPPORTED_MEDIA_TYPE -> "Unsupported Media Type";
            case CONTENT_TOO_LARGE -> "Content Too Large";
            case TOO_MANY_REQUESTS -> "Too Many Requests";
            case MULTI_STATUS -> "Multi-Status";
            case INTERNAL_SERVER_ERROR -> "Internal Server Error";
            case NOT_IMPLEMENTED -> "Not Implemented";
            default -> status.name().replace('_', ' ');
        };
    }
}

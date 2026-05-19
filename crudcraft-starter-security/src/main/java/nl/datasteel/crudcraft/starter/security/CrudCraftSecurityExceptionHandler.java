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

package nl.datasteel.crudcraft.starter.security;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.Objects;
import nl.datasteel.crudcraft.runtime.controller.response.ErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/** Maps Spring Security authorization failures without coupling runtime-core to Spring Security. */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class CrudCraftSecurityExceptionHandler {
    private final Clock clock;

    /** Creates the exception handler. */
    public CrudCraftSecurityExceptionHandler() {
        this(Clock.systemUTC());
    }

    CrudCraftSecurityExceptionHandler(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * Handles Spring Security access-denied exceptions.
     *
     * @param ex the exception
     * @param req the HTTP request
     * @return a 403 response with CrudCraft's standard error shape
     */
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            RuntimeException ex, HttpServletRequest req) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = "Access is denied.";
        }
        ErrorResponse body =
                new ErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        "Forbidden",
                        message,
                        clock.instant(),
                        req.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}

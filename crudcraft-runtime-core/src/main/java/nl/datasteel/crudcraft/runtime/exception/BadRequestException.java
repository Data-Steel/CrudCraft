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

package nl.datasteel.crudcraft.runtime.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/** 400 Bad Request — client sent invalid data or request. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends CrudCraftRuntimeException {

    /**
     * Constructs a BadRequestException with a default message.
     *
     * @param message detail message
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Constructs a BadRequestException with a message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a BadRequestException with diagnostic context.
     *
     * @param message detail message
     * @param context diagnostic context
     */
    public BadRequestException(String message, Map<String, String> context) {
        super(message, context);
    }

    /**
     * Constructs a BadRequestException with diagnostic context and cause.
     *
     * @param message detail message
     * @param context diagnostic context
     * @param cause the cause of the exception
     */
    public BadRequestException(String message, Map<String, String> context, Throwable cause) {
        super(message, context, cause);
    }
}

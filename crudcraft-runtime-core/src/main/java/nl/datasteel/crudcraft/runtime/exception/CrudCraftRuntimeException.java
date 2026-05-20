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
import java.util.StringJoiner;

/** Base class for all CrudCraft runtime exceptions. */
public abstract class CrudCraftRuntimeException extends RuntimeException {
    /** Structured diagnostic context carried by the exception. */
    private final Map<String, String> context;

    /**
     * Constructs a CrudCraftRuntimeException with a default message.
     *
     * @param message detail message
     */
    protected CrudCraftRuntimeException(String message) {
        this(message, Map.of(), null);
    }

    /**
     * Constructs a CrudCraftRuntimeException with a message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    protected CrudCraftRuntimeException(String message, Throwable cause) {
        this(message, Map.of(), cause);
    }

    /**
     * Constructs a CrudCraftRuntimeException with structured diagnostic context.
     *
     * @param message detail message
     * @param context diagnostic context for logs and error messages
     */
    protected CrudCraftRuntimeException(String message, Map<String, String> context) {
        this(message, context, null);
    }

    /**
     * Constructs a CrudCraftRuntimeException with structured diagnostic context and cause.
     *
     * @param message detail message
     * @param context diagnostic context for logs and error messages
     * @param cause the cause of the exception
     */
    protected CrudCraftRuntimeException(
            String message, Map<String, String> context, Throwable cause) {
        super(formatMessage(message, context), cause);
        this.context = Map.copyOf(context == null ? Map.of() : context);
    }

    /**
     * Returns immutable diagnostic context.
     *
     * @return context map
     */
    public Map<String, String> getContext() {
        return Map.copyOf(context);
    }

    private static String formatMessage(String message, Map<String, String> context) {
        if (context == null || context.isEmpty()) {
            return message;
        }
        StringJoiner joiner = new StringJoiner(", ", " [", "]");
        context.forEach((key, value) -> joiner.add(key + "=" + value));
        return message + joiner;
    }
}

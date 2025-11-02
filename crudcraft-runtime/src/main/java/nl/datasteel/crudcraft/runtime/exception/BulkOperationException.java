/**
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

import java.util.List;
import java.util.Objects;

/**
 * Represents partial or complete failures in bulk operations.
 * Contains list of individual item-level exceptions.
 * Results in 207 Multi-Status or 500 by default, handle via ControllerAdvice.
 */
public class BulkOperationException extends CrudCraftRuntimeException {

    /** Immutable snapshot of item-level exceptions. */
    private final List<Throwable> itemExceptions;

    /**
     * Constructs a BulkOperationException with a message and item exceptions.
     *
     * @param message the detail message
     * @param itemExceptions list of exceptions for each item in the bulk operation
     */
    public BulkOperationException(String message, List<Throwable> itemExceptions) {
        super(message);
        // Defensive copy + make unmodifiable, null-safe
        this.itemExceptions = List.copyOf(Objects.requireNonNullElseGet(itemExceptions, List::of));
    }

    /** Returns an immutable list of item exceptions. */
    public List<Throwable> getItemExceptions() {
        return itemExceptions;
    }
}

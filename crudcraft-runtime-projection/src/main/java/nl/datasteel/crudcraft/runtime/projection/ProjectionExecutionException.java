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

package nl.datasteel.crudcraft.runtime.projection;

import java.util.Map;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;


/** Thrown when a projection request violates safety limits or cannot be executed safely. */
public final class ProjectionExecutionException extends BadRequestException {

    /**
     * Creates a projection execution exception.
     *
     * @param message detail message
     * @param context diagnostic context
     */
    public ProjectionExecutionException(String message, Map<String, String> context) {
        super(message, context);
    }

    /**
     * Creates a projection execution exception with a root cause.
     *
     * @param message detail message
     * @param context diagnostic context
     * @param cause root cause
     */
    public ProjectionExecutionException(
            String message, Map<String, String> context, Throwable cause) {
        super(message, context, cause);
    }
}

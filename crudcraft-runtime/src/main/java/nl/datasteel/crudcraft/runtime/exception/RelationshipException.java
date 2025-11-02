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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 409 Conflict — relationship constraint violation.
 * This exception is thrown when an operation violates a relationship constraint,
 * such as attempting to delete an entity that is still referenced by another entity.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class RelationshipException extends CrudCraftRuntimeException {

    /**
     * Constructs a RelationshipException with a default message.
     *
     * @param message the detail message
     */
    public RelationshipException(String message, Throwable cause) {
        super(message, cause);
    }
}
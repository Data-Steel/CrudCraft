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

package nl.datasteel.crudcraft.runtime.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 412 Precondition Failed — e.g. conditional request headers not met.
 */
@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class PreconditionFailedException extends CrudCraftRuntimeException {

    /**
     * Constructs a PreconditionFailedException with a default message.
     */
    public PreconditionFailedException(String message) {
        super(message);
    }
}

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
package nl.datasteel.crudcraft.codegen.exception;

/**
 * Base class for all CrudCraft generation exceptions.
 */
public abstract class CrudCraftGenerationException extends RuntimeException {

    /**
     * Default constructor.
     */
    protected CrudCraftGenerationException(String message) {
        super(message);
    }

    /**
     * Constructor with a message and a cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    protected CrudCraftGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

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

package nl.datasteel.crudcraft.codegen.reader.field;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;

/**
 * Interface for extracting parts of a field from a VariableElement.
 *
 * @param <T> the type of the extracted part
 */
public interface FieldPartExtractor<T> {

    /**
     * Extracts a part of the field from the given VariableElement.
     *
     * @param field the VariableElement representing the field
     * @param env the ProcessingEnvironment for accessing annotations and other processing features
     * @return the extracted part of the field
     */
    T extract(VariableElement field, ProcessingEnvironment env);

    /**
     * Logs a message related to the field extraction.
     *
     * @param messager the Messager for logging messages
     * @param kind the kind of message (e.g., ERROR, WARNING, etc.)
     * @param field the VariableElement representing the field
     * @param msg the message to log
     */
    static void log(Messager messager, Kind kind, VariableElement field, String msg) {
        messager.printMessage(kind, "[FieldPartExtractor] "
                + msg + " → Field: " + field.getSimpleName());
    }
}


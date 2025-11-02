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
package nl.datasteel.crudcraft.codegen.reader.model;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

/**
 * Interface for extracting parts of a model from a TypeElement.
 *
 * @param <T> type of the part to extract
 */
public interface ModelPartExtractor<T> {

    /**
     * Extracts a part of the model from the given TypeElement.
     *
     * @param model the TypeElement representing the model class
     * @param env processing environment for annotation utilities
     * @return extracted part
     */
    T extract(TypeElement model, ProcessingEnvironment env);

    /**
     * Utility method for logging during extraction.
     */
    static void log(Messager messager, Kind kind, TypeElement model, String msg) {
        messager.printMessage(kind, "[ModelPartExtractor] "
                + msg + " → Class: " + model.getSimpleName());
    }
}

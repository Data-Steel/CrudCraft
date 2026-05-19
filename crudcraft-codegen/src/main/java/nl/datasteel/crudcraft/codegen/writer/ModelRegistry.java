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

package nl.datasteel.crudcraft.codegen.writer;

import java.util.Set;
import javax.lang.model.element.TypeElement;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;


/** Narrow view for model lookup and per-round generated artifact registration. */
public interface ModelRegistry {
    /**
     * Registers a model for the per-processing-run Insomnia export.
     *
     * @param model model descriptor to include
     */
    void registerInsomniaModel(ModelDescriptor model);

    /**
     * Returns a stable snapshot of models collected for the current processing context.
     *
     * @return immutable model snapshot
     */
    Set<ModelDescriptor> insomniaModels();

    /**
     * Finds a {@link TypeElement} by its fully qualified class name.
     *
     * @param fqcn fully qualified class name
     * @return the {@link TypeElement} or {@code null} if not found
     */
    TypeElement findTypeElement(String fqcn);
}

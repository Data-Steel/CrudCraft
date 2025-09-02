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

package nl.datasteel.crudcraft.codegen.reader;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import nl.datasteel.crudcraft.codegen.reader.model.ModelPartExtractorRegistry;

/**
 * Reads an entity's annotations and builds a {@link ModelDescriptor} using part extractors.
 */
public class AnnotationModelReader {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AnnotationModelReader() {
    }

    /**
     * Parses the given element to create a {@link ModelDescriptor}.
     *
     * @param element the element representing the model class
     * @param env the processing environment for annotation utilities
     * @return a ModelDescriptor containing the extracted parts of the model
     */
    public static ModelDescriptor parse(Element element, ProcessingEnvironment env) {
        TypeElement cls = (TypeElement) element;

        env.getMessager().printMessage(
                javax.tools.Diagnostic.Kind.NOTE,
                "Parsing model: " + cls.getQualifiedName()
        );

        return new ModelDescriptor(
                ModelPartExtractorRegistry.get(ModelIdentity.class).extract(cls, env),
                ModelPartExtractorRegistry.get(ModelFlags.class).extract(cls, env),
                ModelPartExtractorRegistry.get(EndpointOptions.class).extract(cls, env),
                ModelPartExtractorRegistry.get(ModelSecurity.class).extract(cls, env)
        );
    }
}

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

package nl.datasteel.crudcraft.codegen.reader.model;

import jakarta.persistence.Embeddable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;

/**
 * Singleton extractor for {@link ModelFlags}.
 * Extracts {@link ModelFlags} from a model.
 */
@SuppressWarnings("java:S6548") // Suppress warning for singleton pattern usage
public class FlagsExtractor implements ModelPartExtractor<ModelFlags> {

    /**
     * Singleton instance of FlagsExtractor.
     */
    public static final FlagsExtractor INSTANCE = new FlagsExtractor();

    /**
     * Extracts flags from the given model class.
     *
     * @param cls the TypeElement representing the model class
     * @param env processing environment for annotation utilities
     * @return a ModelFlags object containing the extracted flags
     */
    @Override
    public ModelFlags extract(TypeElement cls, ProcessingEnvironment env) {
        CrudCrafted craft = cls.getAnnotation(CrudCrafted.class);
        boolean editable = craft != null && craft.editable();
        boolean crudEntity = craft != null;
        boolean embeddable = cls.getAnnotation(Embeddable.class) != null;
        boolean abstractClass = cls.getModifiers().contains(javax.lang.model.element.Modifier.ABSTRACT);
        return new ModelFlags(editable, crudEntity, embeddable, abstractClass);
    }
}

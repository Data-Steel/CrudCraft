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

import java.util.Map;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;

/**
 * Registry for model part extractors.
 * Provides a mapping from model part types to their corresponding extractors.
 * This allows for dynamic extraction of model parts based on their type.
 */
public final class ModelPartExtractorRegistry {

    /**
     * Private constructor to prevent instantiation.
     */
    private ModelPartExtractorRegistry() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    /**
     * A map that associates model part types with their corresponding extractors.
     * Each extractor is responsible for extracting a specific part of a model.
     */
    private static final Map<Class<?>, ModelPartExtractor<?>> EXTRACTORS = Map.of(
            ModelIdentity.class, IdentityExtractor.INSTANCE,
            ModelFlags.class, FlagsExtractor.INSTANCE,
            EndpointOptions.class, EndpointOptionsExtractor.INSTANCE,
            ModelSecurity.class, ModelSecurityExtractor.INSTANCE
    );

    /**
     * Retrieves the extractor for a given model part type.
     *
     * @param partType the class of the model part type
     * @param <T>      the type of the model part
     * @return the extractor for the specified model part type, or null if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> ModelPartExtractor<T> get(Class<T> partType) {
        return (ModelPartExtractor<T>) EXTRACTORS.get(partType);
    }
}

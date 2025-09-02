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

import java.util.Map;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.EnumOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Relationship;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SearchOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Security;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Validation;

/**
 * Registry for field part extractors.
 * Provides a mapping from field part types to their corresponding extractors.
 * This allows for dynamic extraction of field parts based on their type.
 */
public final class FieldPartExtractorRegistry {

    /**
     * Private constructor to prevent instantiation.
     */
    private FieldPartExtractorRegistry() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    /**
     * A map that associates field part types with their corresponding extractors.
     * Each extractor is responsible for extracting a specific part of a field.
     */
    public static final Map<Class<?>, FieldPartExtractor<?>> EXTRACTORS = Map.of(
            Identity.class, IdentityExtractor.INSTANCE,
            DtoOptions.class, DtoOptionsExtractor.INSTANCE,
            EnumOptions.class, EnumOptionsExtractor.INSTANCE,
            Relationship.class, RelationshipExtractor.INSTANCE,
            Validation.class, ValidationExtractor.INSTANCE,
            SearchOptions.class, SearchOptionsExtractor.INSTANCE,
            Security.class, SecurityExtractor.INSTANCE
    );

    /**
     * Retrieves the extractor for a given field part type.
     *
     * @param partType the class of the field part type
     * @param <T>      the type of the field part
     * @return the extractor for the specified field part type, or null if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> FieldPartExtractor<T> get(Class<T> partType) {
        return (FieldPartExtractor<T>) EXTRACTORS.get(partType);
    }
}

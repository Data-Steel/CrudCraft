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

package nl.datasteel.crudcraft.runtime.projection.mapping;

import java.util.HashMap;
import java.util.Map;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;


/**
 * Simple in-memory registry for projection metadata. In a real application the entries would be
 * generated at compile time.
 */
public class SimpleProjectionMetadataRegistry implements ProjectionMetadataRegistry {

    /**
     * A map that holds the metadata for each DTO type. The key is the class type of the DTO, and
     * the value is the corresponding ProjectionMetadata.
     */
    private final Map<Class<?>, ProjectionMetadata<?>> metadata = new HashMap<>();

    /** Creates an empty in-memory metadata registry. */
    public SimpleProjectionMetadataRegistry() {
        // Constructor without any parameters stays empty
    }

    /**
     * Registers new projection metadata.
     *
     * @param projectionMetadata the metadata to register
     * @param <D> DTO type represented by the metadata
     */
    public <D> void register(ProjectionMetadata<D> projectionMetadata) {
        metadata.put(projectionMetadata.dtoType(), projectionMetadata);
    }

    /**
     * Retrieves the metadata for a given DTO type.
     *
     * @param dtoType the class type of the DTO
     * @param <D> the type of the DTO
     * @return the projection metadata for the specified DTO type, or null if not found
     */
    @SuppressWarnings("unchecked")
    @Override
    public <D> ProjectionMetadata<D> getMetadata(Class<D> dtoType) {
        return (ProjectionMetadata<D>) metadata.get(dtoType);
    }
}

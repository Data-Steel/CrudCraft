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
package nl.datasteel.crudcraft.runtime.export;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for entity metadata, caching introspected entity information.
 * Thread-safe and lazy-loading.
 */
public class EntityMetadataRegistry {
    
    private final EntityMetadataIntrospector introspector;
    private final Map<Class<?>, EntityMetadata> cache;
    
    /**
     * Creates a new registry.
     */
    public EntityMetadataRegistry() {
        this.introspector = new EntityMetadataIntrospector();
        this.cache = new ConcurrentHashMap<>();
    }
    
    /**
     * Gets metadata for an entity class, introspecting it if necessary.
     *
     * @param entityClass the entity class
     * @return entity metadata
     * @throws IllegalArgumentException if the class is not an entity
     */
    public EntityMetadata getMetadata(Class<?> entityClass) {
        return cache.computeIfAbsent(entityClass, introspector::introspect);
    }
    
    /**
     * Checks if metadata exists for a class.
     *
     * @param entityClass the entity class
     * @return true if metadata is cached
     */
    public boolean hasMetadata(Class<?> entityClass) {
        return cache.containsKey(entityClass);
    }
    
    /**
     * Clears all cached metadata. Useful for testing.
     */
    public void clear() {
        cache.clear();
    }
}

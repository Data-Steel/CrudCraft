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
package nl.datasteel.crudcraft.projection.api;

/**
 * Executes projection queries and returns DTO results.
 */
public interface ProjectionExecutor {
    /**
     * Execute a projection for the given entity type and DTO type.
     *
     * @param entityType the entity class
     * @param dtoType    the DTO class to project to
     * @param query      the projection query definition
     * @return result containing the projected DTOs
     * @param <T> entity type
     * @param <D> dto type
     */
    <T, D> ProjectionResult<D> project(Class<T> entityType, Class<D> dtoType,
                                       ProjectionQuery<T> query);
}

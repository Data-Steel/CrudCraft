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

package nl.datasteel.crudcraft.runtime.mapper;

/**
 * Customizes entities and DTOs immediately after the generated {@link EntityMapper} has mapped
 * them.
 *
 * <p>Generated services can override their mapper customizer hook to return an implementation of
 * this interface. This keeps generated MapStruct mapper files replaceable while still allowing
 * application-specific whole-object mapping adjustments.
 *
 * @param <T> entity type
 * @param <U> request DTO type
 * @param <R> full response DTO type
 * @param <F> reference DTO type
 */
@SuppressWarnings("Java:S119")
public interface EntityMapperCustomizer<T, U, R, F> {

    /**
     * Returns a customizer that leaves every mapped object unchanged.
     *
     * @param <T> entity type
     * @param <U> request DTO type
     * @param <R> full response DTO type
     * @param <F> reference DTO type
     * @return no-op customizer
     */
    @SuppressWarnings("unchecked")
    static <T, U, R, F> EntityMapperCustomizer<T, U, R, F> noOp() {
        return (EntityMapperCustomizer<T, U, R, F>) NoOpHolder.INSTANCE;
    }

    /**
     * Customizes an entity created from a request DTO.
     *
     * @param entity mapped entity
     * @param request source request DTO
     * @return entity to persist
     */
    default T afterFromRequest(T entity, U request) {
        consume(request);
        return entity;
    }

    /**
     * Customizes an entity after a full update mapping.
     *
     * @param entity mapped entity
     * @param request source request DTO
     * @return entity to persist
     */
    default T afterUpdate(T entity, U request) {
        consume(request);
        return entity;
    }

    /**
     * Customizes an entity after a patch mapping.
     *
     * @param entity mapped entity
     * @param request source request DTO
     * @return entity to persist
     */
    default T afterPatch(T entity, U request) {
        consume(request);
        return entity;
    }

    /**
     * Customizes a full response DTO after entity mapping.
     *
     * @param response mapped response DTO
     * @param entity source entity
     * @return response DTO to return
     */
    default R afterToResponse(R response, T entity) {
        consume(entity);
        return response;
    }

    /**
     * Customizes a reference DTO after entity mapping.
     *
     * @param ref mapped reference DTO
     * @param entity source entity
     * @return reference DTO to return
     */
    default F afterToRef(F ref, T entity) {
        consume(entity);
        return ref;
    }

    private static void consume(Object value) {
        if (value != null) {
            value.getClass();
        }
    }

    /** Holder for the shared no-op instance. */
    final class NoOpHolder {
        private static final EntityMapperCustomizer<?, ?, ?, ?> INSTANCE =
                new EntityMapperCustomizer<>() {};

        private NoOpHolder() {}
    }
}

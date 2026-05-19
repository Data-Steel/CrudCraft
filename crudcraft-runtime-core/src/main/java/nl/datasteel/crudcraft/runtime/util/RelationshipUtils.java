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

package nl.datasteel.crudcraft.runtime.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Utility class for invoking generated bidirectional relationship metadata. */
public final class RelationshipUtils {
    private static final Logger log = LoggerFactory.getLogger(RelationshipUtils.class);

    private RelationshipUtils() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    /**
     * Invokes the compile-generated {@code RelationshipMeta.fix(entity)} method after save.
     *
     * <p>Reflection is used because generated services only know the entity type at compile time;
     * the matching relationship metadata class follows a generated naming convention instead of a
     * shared runtime interface. The lookup and invocation run once per service hook call and touch
     * only the generated metadata class, so the cost is small relative to the surrounding JPA save.
     * Missing metadata is treated as a generation or classpath error instead of a silent no-op.
     *
     * @param entity entity to fix
     * @param <E> entity type
     */
    public static <E> void fixBidirectional(E entity) {
        Class<?> entityType = requireEntityType(entity, "fix");
        Class<?> meta = findMetaClass(entityType);
        try {
            Method method = meta.getMethod("fix", entityType);
            method.invoke(null, entity);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.warn(
                    "Failed to invoke relationship metadata fix method for entity {} "
                            + "using {}: {}",
                    entityType.getName(),
                    meta.getName(),
                    e.toString());
            throw new IllegalStateException("Failed to fix bidirectional relationship", e);
        }
    }

    /**
     * Invokes the compile-generated {@code RelationshipMeta.clear(entity)} method before delete.
     *
     * <p>Reflection keeps core independent from generated metadata types while still allowing the
     * generated service lifecycle to repair owning/inverse sides consistently. The method performs
     * one class lookup and one static method invocation per delete hook, which is negligible beside
     * the persistence work that follows. Missing metadata is treated as a generation or classpath
     * error instead of a silent no-op.
     *
     * @param entity entity to clear
     * @param <E> entity type
     */
    public static <E> void clearBidirectional(E entity) {
        Class<?> entityType = requireEntityType(entity, "clear");
        Class<?> meta = findMetaClass(entityType);
        try {
            Method method = meta.getMethod("clear", entityType);
            method.invoke(null, entity);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.warn(
                    "Failed to invoke relationship metadata clear method for entity {} "
                            + "using {}: {}",
                    entityType.getName(),
                    meta.getName(),
                    e.toString());
            throw new IllegalStateException("Failed to clear bidirectional relationship", e);
        }
    }

    private static <E> Class<?> requireEntityType(E entity, String operation) {
        Objects.requireNonNull(entity, "Entity must not be null when calling " + operation);
        return entity.getClass();
    }

    private static Class<?> findMetaClass(Class<?> entityType) {
        String pkg = entityType.getPackageName() + ".meta";
        String metaClass = pkg + "." + entityType.getSimpleName() + "RelationshipMeta";
        try {
            return Class.forName(metaClass);
        } catch (ClassNotFoundException e) {
            log.warn(
                    "Generated relationship metadata class {} was not found for {}",
                    metaClass,
                    entityType.getName());
            throw new IllegalStateException(
                    "Generated relationship metadata not found for " + entityType.getName(), e);
        }
    }
}

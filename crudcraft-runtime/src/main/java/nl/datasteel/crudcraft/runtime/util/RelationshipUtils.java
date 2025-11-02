/*
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
package nl.datasteel.crudcraft.runtime.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import nl.datasteel.crudcraft.runtime.exception.RelationshipException;

/**
 * Utility class for fixing or clearing bidirectional relationships in entities.
 */
public final class RelationshipUtils {
    private RelationshipUtils() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    /**
     * Call after save: invoke compile-generated Meta.fix(entity), if present.
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public static <E> void fixBidirectional(E entity) {
        try {
            String pkg = entity.getClass().getPackageName() + ".meta";
            String metaClass = pkg + "." + entity.getClass().getSimpleName() + "RelationshipMeta";
            Method m = Class.forName(metaClass).getMethod("fix", entity.getClass());
            m.invoke(null, entity);
        } catch (ClassNotFoundException cnf) {
            // No generated metadata means skip the class,
            // this is normal for entities without relationships
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RelationshipException("Failed to fix bidirectional relationship", e);
        }
    }

    /**
     * Call before delete: invoke compile-generated Meta.clear(entity), if present.
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public static <E> void clearBidirectional(E entity) {
        try {
            String pkg = entity.getClass().getPackageName() + ".meta";
            String metaClass = pkg + "." + entity.getClass().getSimpleName() + "RelationshipMeta";
            Method m = Class.forName(metaClass).getMethod("clear", entity.getClass());
            m.invoke(null, entity);

        } catch (ClassNotFoundException cnf) {
            // No generated metadata means skip the class
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RelationshipException("Failed to clear bidirectional relationship", e);
        }
    }
}

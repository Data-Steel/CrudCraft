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

package nl.datasteel.crudcraft.runtime.security;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;


/** Shared recursive traversal helpers for field-security filtering. */
final class FieldSecurityTraversal {

    private FieldSecurityTraversal() {}

    static Object processReadRecursively(
            Object value,
            Map<Object, Boolean> visited,
            Predicate<Class<?>> scalarType,
            BiFunction<Object, Map<Object, Boolean>, Object> nestedReadFilter) {
        if (value == null) {
            return null;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            Object copy = Array.newInstance(value.getClass().getComponentType(), length);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(value, i);
                if (element != null && !scalarType.test(element.getClass())) {
                    element = nestedReadFilter.apply(element, visited);
                }
                Array.set(copy, i, element);
            }
            return copy;
        }
        if (value instanceof Collection<?> collection) {
            ArrayList<Object> filtered = new ArrayList<>(collection.size());
            for (Object element : collection) {
                if (element != null && !scalarType.test(element.getClass())) {
                    element = nestedReadFilter.apply(element, visited);
                }
                filtered.add(element);
            }
            if (value instanceof Set<?>) {
                return Collections.unmodifiableSet(new LinkedHashSet<>(filtered));
            }
            return Collections.unmodifiableList(filtered);
        }
        if (!scalarType.test(value.getClass())) {
            return nestedReadFilter.apply(value, visited);
        }
        return value;
    }

    static Object processWriteRecursively(
            Object value,
            Object existingValue,
            Map<Object, Boolean> visited,
            Predicate<Class<?>> scalarType,
            TriFunction<Object, Object, Map<Object, Boolean>, Object> nestedWriteFilter) {
        if (value == null) {
            return null;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            int existingLength =
                    existingValue != null && existingValue.getClass().isArray()
                            ? Array.getLength(existingValue)
                            : 0;
            Object copy = Array.newInstance(value.getClass().getComponentType(), length);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(value, i);
                Object existingElement = i < existingLength ? Array.get(existingValue, i) : null;
                if (element != null && !scalarType.test(element.getClass())) {
                    element = nestedWriteFilter.apply(element, existingElement, visited);
                }
                Array.set(copy, i, element);
            }
            return copy;
        }
        if (value instanceof Collection<?> collection) {
            Collection<?> existingCollection =
                    existingValue instanceof Collection<?> existing ? existing : null;
            Iterator<?> existingIter =
                    existingCollection == null ? null : existingCollection.iterator();
            ArrayList<Object> filtered = new ArrayList<>(collection.size());
            for (Object element : collection) {
                Object existingElement =
                        existingIter != null && existingIter.hasNext() ? existingIter.next() : null;
                if (element != null && !scalarType.test(element.getClass())) {
                    element = nestedWriteFilter.apply(element, existingElement, visited);
                }
                filtered.add(element);
            }
            if (value instanceof Set<?>) {
                return Collections.unmodifiableSet(new LinkedHashSet<>(filtered));
            }
            return Collections.unmodifiableList(filtered);
        }
        if (!scalarType.test(value.getClass())) {
            return nestedWriteFilter.apply(value, existingValue, visited);
        }
        return value;
    }

    @FunctionalInterface
    interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}

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

package nl.datasteel.crudcraft.runtime.security.row;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;


final class RowSecurityBeanProperty {
    private final Method reader;
    private final Method writer;
    private final Class<?> propertyType;

    private RowSecurityBeanProperty(
            @Nullable Method reader, @Nullable Method writer, Class<?> propertyType) {
        this.reader = reader;
        this.writer = writer;
        this.propertyType = Objects.requireNonNull(propertyType, "propertyType");
    }

    static RowSecurityBeanProperty forField(Class<?> type, String fieldName) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(fieldName, "fieldName");
        if (fieldName.isBlank()) {
            throw new IllegalArgumentException("fieldName must not be blank");
        }
        String suffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        Method reader = findReader(type, suffix);
        Method writer = findWriter(type, suffix, reader == null ? null : reader.getReturnType());
        if (reader == null && writer == null) {
            throw new IllegalStateException(
                    "Scope field '" + fieldName + "' not found on " + type.getName());
        }
        Class<?> propertyType =
                reader == null ? writer.getParameterTypes()[0] : reader.getReturnType();
        return new RowSecurityBeanProperty(reader, writer, propertyType);
    }

    @Nullable Method reader() {
        return reader;
    }

    @Nullable Method writer() {
        return writer;
    }

    Class<?> propertyType() {
        return propertyType;
    }

    Object read(Object target) throws IllegalAccessException, InvocationTargetException {
        if (reader == null) {
            throw new IllegalStateException("No readable scope property on " + target.getClass());
        }
        return reader.invoke(target);
    }

    void write(Object target, Object value)
            throws IllegalAccessException, InvocationTargetException {
        if (writer == null) {
            throw new IllegalStateException("No writable scope property on " + target.getClass());
        }
        writer.invoke(target, value);
    }

    private static @Nullable Method findReader(Class<?> type, String suffix) {
        Method getter = findNoArg(type, "get" + suffix);
        if (getter != null && getter.getReturnType() != Void.TYPE) {
            return getter;
        }
        Method booleanGetter = findNoArg(type, "is" + suffix);
        if (booleanGetter != null
                && (booleanGetter.getReturnType() == boolean.class
                        || booleanGetter.getReturnType() == Boolean.class)) {
            return booleanGetter;
        }
        return null;
    }

    private static @Nullable Method findNoArg(Class<?> type, String name) {
        try {
            return type.getMethod(name);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    private static @Nullable Method findWriter(
            Class<?> type, String suffix, @Nullable Class<?> preferredType) {
        return Arrays.stream(type.getMethods())
                .filter(method -> method.getName().equals("set" + suffix))
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> preferredType == null || sameBoxedType(method, preferredType))
                .sorted(Comparator.comparing(method -> method.getParameterTypes()[0].getName()))
                .findFirst()
                .orElse(null);
    }

    private static boolean sameBoxedType(Method method, Class<?> preferredType) {
        return box(method.getParameterTypes()[0]).equals(box(preferredType));
    }

    private static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return Void.class;
    }
}

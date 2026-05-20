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

package nl.datasteel.crudcraft.runtime.service;

import jakarta.persistence.criteria.From;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.InternalApi;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import org.springframework.util.ReflectionUtils;


/** Encodes opaque keyset cursors and extracts cursor boundary values. */
@InternalApi
final class KeysetCursorCodec {

    private KeysetCursorCodec() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    static String encode(String property, String direction, Object sortValue, Object idValue) {
        String payload = property + "\n" + direction + "\n" + sortValue + "\n" + idValue;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    static String encodeCursor(
            String property, String direction, Object sortValue, Object idValue) {
        return encode(property, direction, sortValue, idValue);
    }

    static CursorData decode(String cursor) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            String[] parts = new String(decoded, StandardCharsets.UTF_8).split("\n", -1);
            if (parts.length != 4) {
                throw invalidCursor();
            }
            return new CursorData(parts[0], parts[1], parts[2], parts[3]);
        } catch (IllegalArgumentException ex) {
            throw invalidCursor();
        }
    }

    static CursorData decodeCursor(String cursor) {
        return decode(cursor);
    }

    static Object parseValue(Class<?> javaType, String raw) {
        if (raw == null) {
            return null;
        }
        if (String.class.equals(javaType)) {
            return raw;
        }
        if (Long.class.equals(javaType) || long.class.equals(javaType)) {
            return parseLong(raw);
        }
        if (Integer.class.equals(javaType) || int.class.equals(javaType)) {
            return parseInteger(raw);
        }
        if (Double.class.equals(javaType) || double.class.equals(javaType)) {
            return parseDouble(raw);
        }
        if (Float.class.equals(javaType) || float.class.equals(javaType)) {
            return parseFloat(raw);
        }
        if (UUID.class.equals(javaType)) {
            return parseUuid(raw);
        }
        if (Instant.class.equals(javaType)) {
            return Instant.parse(raw);
        }
        if (LocalDateTime.class.equals(javaType)) {
            return LocalDateTime.parse(raw);
        }
        if (LocalDate.class.equals(javaType)) {
            return LocalDate.parse(raw);
        }
        if (Boolean.class.equals(javaType) || boolean.class.equals(javaType)) {
            return Boolean.valueOf(raw);
        }
        if (Enum.class.isAssignableFrom(javaType)) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Object value = Enum.valueOf((Class<? extends Enum>) javaType, raw);
            return value;
        }
        return raw;
    }

    static Class<?> resolveJavaType(From<?, ?> root, String property) {
        String[] parts = property.split("\\.");
        jakarta.persistence.criteria.Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path.getJavaType();
    }

    @SuppressWarnings("unchecked")
    static <V extends Comparable<? super V>> jakarta.persistence.criteria.Path<V> resolvePath(
            From<?, ?> from, String property) {
        jakarta.persistence.criteria.Path<?> path = from;
        for (String part : property.split("\\.")) {
            path = path.get(part);
        }
        return (jakarta.persistence.criteria.Path<V>) path;
    }

    static Object extractPathValue(Object root, String path) {
        Object current = root;
        for (String part : path.split("\\.")) {
            if (current == null) {
                return null;
            }
            current = readProperty(current, part);
        }
        return current;
    }

    private static Object readProperty(Object source, String name) {
        Method getter = findGetter(source.getClass(), name);
        if (getter != null) {
            try {
                return getter.invoke(source);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new IllegalStateException("Unable to read property '" + name + "'", ex);
            }
        }
        return readField(source, name);
    }

    private static Method findGetter(Class<?> type, String name) {
        String base = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (String candidate : List.of("get" + base, "is" + base)) {
            try {
                return type.getMethod(candidate);
            } catch (NoSuchMethodException ignored) {
                // Try next candidate.
            }
        }
        return null;
    }

    private static Object readField(Object source, String fieldName) {
        for (Class<?> type = source.getClass(); type != Object.class; type = type.getSuperclass()) {
            Field field = ReflectionUtils.findField(type, fieldName);
            if (field != null) {
                field.trySetAccessible();
                return ReflectionUtils.getField(field, source);
            }
        }
        throw new IllegalStateException(
                "Field '" + fieldName + "' not found on " + source.getClass().getName());
    }

    private static BadRequestException invalidCursor() {
        return new BadRequestException(
                "Invalid keyset cursor. Use the opaque cursor returned by a previous keyset"
                        + " response; do not edit or construct cursor values.");
    }

    private static BadRequestException invalidCursor(Throwable cause) {
        return new BadRequestException(
                "Invalid keyset cursor. Use the opaque cursor returned by a previous keyset"
                        + " response; do not edit or construct cursor values.",
                cause);
    }

    private static Long parseLong(String raw) {
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw invalidCursor(ex);
        }
    }

    private static Integer parseInteger(String raw) {
        try {
            return Integer.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw invalidCursor(ex);
        }
    }

    private static Double parseDouble(String raw) {
        try {
            return Double.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw invalidCursor(ex);
        }
    }

    private static Float parseFloat(String raw) {
        try {
            return Float.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw invalidCursor(ex);
        }
    }

    private static UUID parseUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw invalidCursor(ex);
        }
    }

    record CursorData(String property, String direction, String sortValue, String idValue) {}
}

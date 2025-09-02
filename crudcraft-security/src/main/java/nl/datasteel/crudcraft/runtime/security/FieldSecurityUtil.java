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

package nl.datasteel.crudcraft.runtime.security;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility methods for enforcing field-level security on DTOs.
 */
public final class FieldSecurityUtil {

    private FieldSecurityUtil() {
    }

    /**
     * Determines if the current user can read based on the given roles.
     *
     * @param roles roles required for read access
     * @return {@code true} if the user has any of the roles
     */
    public static boolean canRead(String[] roles) {
        return hasAnyRole(roles);
    }

    /**
     * Determines if the current user can write based on the given roles.
     *
     * @param roles roles required for write access
     * @return {@code true} if the user has any of the roles
     */
    public static boolean canWrite(String[] roles) {
        return hasAnyRole(roles);
    }

    /**
     * Filters the provided DTO for read operations.
     *
     * @param dto the object to filter
     * @return the filtered DTO
     */
    public static <T> T filterRead(T dto) {
        return filterRead(dto, new IdentityHashMap<>());
    }

    /**
     * Internal helper for recursively filtering DTOs during read operations.
     *
     * @param dto     the object to filter
     * @param visited previously visited objects to prevent cycles
     * @return the filtered DTO
     */
    private static <T> T filterRead(T dto, Map<Object, Boolean> visited) {
        if (dto == null || visited.containsKey(dto)) {
            return dto;
        }
        visited.put(dto, Boolean.TRUE);

        Class<?> clazz = dto.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                try {
                    Object value = f.get(dto);
                    FieldSecurity fs = f.getAnnotation(FieldSecurity.class);
                    if (fs != null && !canRead(fs.readRoles())) {
                        setFieldToRedacted(f, dto);
                    } else {
                        processReadRecursively(value, visited);
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return dto;
    }

    /**
     * Processes nested objects recursively applying field security rules for read operations.
     *
     * @param value   the value to inspect
     * @param visited already visited objects to avoid cycles
     * @throws IllegalAccessException if reflection fails
     */
    private static void processReadRecursively(Object value, Map<Object, Boolean> visited)
            throws IllegalAccessException {
        if (value == null) {
            return;
        }

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(value, i);
                if (element != null && !isJavaLangType(element.getClass())) {
                    filterRead(element, visited);
                }
            }
        } else if (value instanceof Collection<?> collection) {
            for (Object element : collection) {
                if (element != null && !isJavaLangType(element.getClass())) {
                    filterRead(element, visited);
                }
            }
        } else if (!isJavaLangType(value.getClass())) {
            filterRead(value, visited);
        }
    }

    /**
     * Filters the provided DTO for write operations.
     *
     * @param dto the object to filter
     * @return the filtered DTO
     */
    public static <T> T filterWrite(T dto) {
        return filterWrite(dto, null, new IdentityHashMap<>());
    }

    /**
     * Filters the DTO using values from an existing object to restore fields the
     * caller is not permitted to modify.
     *
     * @param dto      the DTO containing new values
     * @param existing the existing object to source immutable field values from
     * @return the filtered DTO
     */
    public static <T> T filterWrite(T dto, Object existing) {
        return filterWrite(dto, existing, new IdentityHashMap<>());
    }

    /**
     * Internal helper for recursively filtering DTOs during write operations.
     *
     * @param dto      the object to filter
     * @param existing the existing object for immutable values
     * @param visited  previously visited objects to prevent cycles
     * @return the filtered DTO
     */
    @SuppressWarnings("unchecked")
    private static <T> T filterWrite(T dto, Object existing, Map<Object, Boolean> visited) {
        if (dto == null || visited.containsKey(dto)) {
            return dto;
        }
        visited.put(dto, Boolean.TRUE);

        Class<?> clazz = dto.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                try {
                    Object value = f.get(dto);
                    Object existingValue = null;
                    Field existingField = existing == null ? null : findField(existing.getClass(), f.getName());
                    if (existingField != null) {
                        existingField.setAccessible(true);
                        existingValue = existingField.get(existing);
                    }
                    FieldSecurity fs = f.getAnnotation(FieldSecurity.class);
                    if (fs == null || canWrite(fs.writeRoles())) {
                        processWriteRecursively(value, existingValue, visited);
                    } else if (fs.writePolicy() == WritePolicy.FAIL_ON_DENIED) {
                        throw new AccessDeniedException("Write denied to field: " + f.getName());
                    } else { 
                      // SKIP_ON_DENIED
                        if (existingField != null) {
                            f.set(dto, existingValue);
                            processWriteRecursively(existingValue, existingValue, visited);
                        } else {
                            setFieldToRedacted(f, dto);
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return dto;
    }

    /**
     * Processes nested objects recursively applying field security rules for write operations.
     *
     * @param value         the value to inspect
     * @param existingValue the existing object to source immutable data from
     * @param visited       already visited objects to avoid cycles
     * @throws IllegalAccessException if reflection fails
     */
    private static void processWriteRecursively(Object value, Object existingValue,
                                                Map<Object, Boolean> visited)
            throws IllegalAccessException {
        if (value == null) {
            return;
        }

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            int existingLength = existingValue != null && existingValue.getClass().isArray()
                    ? Array.getLength(existingValue) : 0;
            for (int i = 0; i < length; i++) {
                Object element = Array.get(value, i);
                Object existingElement = i < existingLength ? Array.get(existingValue, i) : null;
                if (element != null && !isJavaLangType(element.getClass())) {
                    filterWrite(element, existingElement, visited);
                }
            }
        } else if (value instanceof Collection<?> collection) {
            Collection<?> existingCollection = null;
            if (existingValue instanceof Collection<?>) {
                existingCollection = (Collection<?>) existingValue;
            }
            Iterator<?> existingIter = existingCollection == null
                    ? null : existingCollection.iterator();
            for (Object element : collection) {
                Object existingElement = existingIter != null && existingIter.hasNext()
                        ? existingIter.next() : null;
                if (element != null && !isJavaLangType(element.getClass())) {
                    filterWrite(element, existingElement, visited);
                }
            }
        } else if (!isJavaLangType(value.getClass())) {
            filterWrite(value, existingValue, visited);
        }
    }

    /**
     * Locates a field with the given name on the supplied class or any superclass.
     *
     * @param clazz the class to inspect
     * @param name  the field name
     * @return the {@link Field} if found, otherwise {@code null}
     */
    private static Field findField(Class<?> clazz, String name) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Sets the given field to a redacted value when access is denied.
     *
     * @param f      the field to update
     * @param target the object containing the field
     * @throws IllegalAccessException if reflection fails
     */
    private static void setFieldToRedacted(Field f, Object target)
            throws IllegalAccessException {
        Class<?> type = f.getType();
        if (type.isPrimitive()) {
            f.set(target, defaultPrimitiveValue(type));
        } else {
            f.set(target, null);
        }
    }

    /**
     * Returns the default value for the given primitive type.
     *
     * @param type the primitive class
     * @return the default primitive value
     */
    private static Object defaultPrimitiveValue(Class<?> type) {
        return switch (type.getName()) {
            case "boolean" -> false;
            case "byte" -> (byte) 0;
            case "short" -> (short) 0;
            case "int" -> 0;
            case "long" -> 0L;
            case "float" -> 0f;
            case "double" -> 0d;
            case "char" -> '\0';
            default -> null;
        };
    }

    /**
     * Checks whether the current user has any of the supplied roles.
     *
     * @param roles the roles to verify
     * @return {@code true} if the user has any role or none are specified
     */
    private static boolean hasAnyRole(String[] roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }

        Set<String> required = Arrays.stream(roles).collect(Collectors.toSet());
        if (required.contains("ALL")) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }

        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .anyMatch(required::contains);
    }

    /**
     * Determines whether the given class is part of the Java standard library or primitive.
     *
     * @param clazz the class to inspect
     * @return {@code true} if it is a Java type
     */
    private static boolean isJavaLangType(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.isEnum()
                || clazz.getPackageName().startsWith("java.");
    }
}

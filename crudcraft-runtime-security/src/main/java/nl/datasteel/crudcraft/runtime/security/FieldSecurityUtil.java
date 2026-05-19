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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * Utility methods for enforcing field-level security on DTOs.
 *
 * <p>Threat model and contract:
 *
 * <ul>
 *   <li>Unreadable reference fields are redacted by assigning {@code null}; primitive fields are
 *       reset to their JVM default value because primitives cannot carry {@code null}.
 *   <li>Nested DTOs, arrays, and collections are traversed recursively. Java platform types,
 *       primitives, and enum values are treated as scalar leaf values.
 *   <li>Cycles are guarded with an {@link IdentityHashMap}; already visited object identities are
 *       not traversed again.
 *   <li>Generated {@link FieldSecurityMetadata} is preferred when present. Reflection is the
 *       fallback contract for DTOs that still use annotation-only metadata.
 *   <li>Callers must invoke this utility after the Spring Security subject is bound to
 *       {@link SecurityContextHolder}; missing authentication fails closed for role-protected
 *       fields.
 * </ul>
 */
public final class FieldSecurityUtil {

    /*
     * Thread-safe reflection metadata cache. Entries are populated lazily per DTO class and store
     * Optional.empty() for classes without generated metadata. Repeated concurrent lookups do not
     * redo classpath probing or require external synchronization.
     */
    private static final ConcurrentMap<Class<?>, Optional<FieldSecurityMetadata<?>>>
            METADATA_CACHE = new ConcurrentHashMap<>();

    private FieldSecurityUtil() {}

    /**
     * Determines if the current user can read based on the given roles.
     *
     * @param roles roles required for read access
     * @return {@code true} if the user has any of the roles
     */
    public static boolean canRead(@Nullable String[] roles) {
        return hasAnyRole(roles);
    }

    /**
     * Determines if the current user can write based on the given roles.
     *
     * @param roles roles required for write access
     * @return {@code true} if the user has any of the roles
     */
    public static boolean canWrite(@Nullable String[] roles) {
        return hasAnyRole(roles);
    }

    /**
     * Filters the provided DTO for read operations.
     *
     * @param dto the object to filter
     * @param <T> dto type
     * @return the filtered DTO
     */
    public static <T> @Nullable T filterRead(@Nullable T dto) {
        return filterReadInternal(dto, new IdentityHashMap<>());
    }

    /**
     * Determines whether a DTO field is readable for the active principal.
     *
     * @param dtoType DTO class
     * @param fieldName DTO field name
     * @return true when the field is readable
     */
    public static boolean canReadField(@Nullable Class<?> dtoType, @Nullable String fieldName) {
        if (dtoType == null || fieldName == null || fieldName.isBlank()) {
            return true;
        }
        Optional<FieldSecurityMetadata<?>> metadata = metadataFor(dtoType);
        if (metadata.isPresent()) {
            for (FieldSecurityMetadata.FieldRule<?> rule : metadata.get().fields()) {
                if (fieldName.equals(rule.name())) {
                    return !rule.secured() || hasAnyRole(rule.readRoles());
                }
            }
            return true;
        }
        Field field = findField(dtoType, fieldName);
        if (field == null) {
            return true;
        }
        FieldSecurity annotation = field.getAnnotation(FieldSecurity.class);
        if (annotation == null) {
            return true;
        }
        return hasAnyRole(annotation.readRoles());
    }

    /**
     * Internal helper for recursively filtering DTOs during read operations.
     *
     * @param dto the object to filter
     * @param visited previously visited objects to prevent cycles
     * @return the filtered DTO
     */
    @SuppressWarnings("unchecked")
    private static <T> T filterReadInternal(T dto, Map<Object, Boolean> visited) {
        if (dto == null || visited.containsKey(dto)) {
            return dto;
        }
        visited.put(dto, Boolean.TRUE);

        Optional<FieldSecurityMetadata<?>> metadata = metadataFor(dto.getClass());
        if (metadata.isPresent()) {
            FieldSecurityMetadata<T> typedMetadata = (FieldSecurityMetadata<T>) metadata.get();
            if (dto.getClass().isRecord()) {
                return applyReadRecordWithMetadata(typedMetadata, dto, visited);
            }
            applyReadWithMetadata(typedMetadata, dto, visited);
            return dto;
        }

        Class<?> clazz = dto.getClass();
        while (clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                Object value = getFieldValue(f, dto);
                FieldSecurity fs = f.getAnnotation(FieldSecurity.class);
                if (fs != null && !canRead(fs.readRoles())) {
                    setFieldToRedacted(f, dto);
                } else {
                    processReadRecursively(value, visited);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return dto;
    }

    private static <T> void applyReadWithMetadata(
            FieldSecurityMetadata<T> metadata, T dto, Map<Object, Boolean> visited) {
        for (FieldSecurityMetadata.FieldRule<T> rule : metadata.fields()) {
            Object value = rule.reader().apply(dto);
            if (rule.secured() && !hasAnyRole(rule.readRoles())) {
                if (rule.writer() != null) {
                    rule.writer().accept(dto, null);
                }
            } else {
                Object filtered = processReadRecursively(value, visited);
                if (filtered != value && rule.writer() != null) {
                    rule.writer().accept(dto, filtered);
                }
            }
        }
    }

    private static <T> T applyReadRecordWithMetadata(
            FieldSecurityMetadata<T> metadata, T dto, Map<Object, Boolean> visited) {
        RecordComponent[] components = dto.getClass().getRecordComponents();
        Map<String, FieldSecurityMetadata.FieldRule<T>> rules = rulesByName(metadata);
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            FieldSecurityMetadata.FieldRule<T> rule = rules.get(component.getName());
            if (rule == null || rule.secured() && !hasAnyRole(rule.readRoles())) {
                args[i] = redactedValue(component.getType());
                continue;
            }
            args[i] = processReadRecursively(rule.reader().apply(dto), visited);
        }
        return instantiateRecord(dto, components, args);
    }

    /**
     * Processes nested objects recursively applying field security rules for read operations.
     *
     * @param value the value to inspect
     * @param visited already visited objects to avoid cycles
     */
    private static Object processReadRecursively(Object value, Map<Object, Boolean> visited) {
        return FieldSecurityTraversal.processReadRecursively(
                value,
                visited,
                FieldSecurityUtil::isJavaLangType,
                (candidate, seen) -> filterReadInternal(candidate, seen));
    }

    /**
     * Filters the provided DTO for write operations.
     *
     * @param dto the object to filter
     * @param <T> dto type
     * @return the filtered DTO
     */
    public static <T> @Nullable T filterWrite(@Nullable T dto) {
        return filterWrite(dto, null, new IdentityHashMap<>());
    }

    /**
     * Filters the DTO using values from an existing object to restore fields the caller is not
     * permitted to modify.
     *
     * @param dto the DTO containing new values
     * @param existing the existing object to source immutable field values from
     * @param <T> dto type
     * @return the filtered DTO
     */
    public static <T> @Nullable T filterWrite(@Nullable T dto, @Nullable Object existing) {
        return filterWrite(dto, existing, new IdentityHashMap<>());
    }

    /**
     * Internal helper for recursively filtering DTOs during write operations.
     *
     * @param dto the object to filter
     * @param existing the existing object for immutable values
     * @param visited previously visited objects to prevent cycles
     * @return the filtered DTO
     */
    @SuppressWarnings("unchecked")
    private static <T> T filterWrite(T dto, Object existing, Map<Object, Boolean> visited) {
        if (dto == null || visited.containsKey(dto)) {
            return dto;
        }
        visited.put(dto, Boolean.TRUE);

        Optional<FieldSecurityMetadata<?>> metadata = metadataFor(dto.getClass());
        if (metadata.isPresent()) {
            FieldSecurityMetadata<T> typedMetadata = (FieldSecurityMetadata<T>) metadata.get();
            FieldSecurityMetadata<Object> existingMetadata =
                    existing == null
                            ? null
                            : metadataFor(existing.getClass())
                                    .map(value -> (FieldSecurityMetadata<Object>) value)
                                    .orElse(null);
            if (dto.getClass().isRecord()) {
                return applyWriteRecordWithMetadata(
                        typedMetadata, dto, existing, existingMetadata, visited);
            }
            applyWriteWithMetadata(typedMetadata, dto, existing, existingMetadata, visited);
            return dto;
        }

        Class<?> clazz = dto.getClass();
        while (clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                Object value = getFieldValue(f, dto);
                Object existingValue = null;
                Field existingField =
                        existing == null ? null : findField(existing.getClass(), f.getName());
                if (existingField != null) {
                    existingField.setAccessible(true);
                    existingValue = getFieldValue(existingField, existing);
                }
                FieldSecurity fs = f.getAnnotation(FieldSecurity.class);
                if (fs == null || canWrite(fs.writeRoles())) {
                    processWriteRecursively(value, existingValue, visited);
                } else if (fs.writePolicy() == WritePolicy.FAIL_ON_DENIED) {
                    throw new AccessDeniedException("Write denied to field: " + f.getName());
                } else {
                    if (existingField != null) {
                        setFieldValue(f, dto, existingValue);
                        processWriteRecursively(existingValue, existingValue, visited);
                    } else {
                        setFieldToRedacted(f, dto);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return dto;
    }

    private static <T> void applyWriteWithMetadata(
            FieldSecurityMetadata<T> metadata,
            T dto,
            Object existing,
            FieldSecurityMetadata<Object> existingMetadata,
            Map<Object, Boolean> visited) {
        Map<String, FieldSecurityMetadata.FieldRule<Object>> existingRules =
                existingMetadata == null
                        ? Map.of()
                        : existingMetadata.fields().stream()
                                .collect(
                                        Collectors.toMap(
                                                FieldSecurityMetadata.FieldRule::name,
                                                rule -> rule,
                                                (left, right) -> left));

        for (FieldSecurityMetadata.FieldRule<T> rule : metadata.fields()) {
            Object value = rule.reader().apply(dto);
            Object existingValue = readExisting(existing, existingRules, rule.name());
            if (!rule.secured() || hasAnyRole(rule.writeRoles())) {
                Object filtered = processWriteRecursively(value, existingValue, visited);
                if (filtered != value && rule.writer() != null) {
                    rule.writer().accept(dto, filtered);
                }
            } else if (rule.writePolicy() == WritePolicy.FAIL_ON_DENIED) {
                throw new AccessDeniedException("Write denied to field: " + rule.name());
            } else if (rule.writer() != null) {
                rule.writer().accept(dto, existingValue);
                Object filtered = processWriteRecursively(existingValue, existingValue, visited);
                if (filtered != existingValue) {
                    rule.writer().accept(dto, filtered);
                }
            }
        }
    }

    private static <T> T applyWriteRecordWithMetadata(
            FieldSecurityMetadata<T> metadata,
            T dto,
            Object existing,
            FieldSecurityMetadata<Object> existingMetadata,
            Map<Object, Boolean> visited) {
        RecordComponent[] components = dto.getClass().getRecordComponents();
        Map<String, FieldSecurityMetadata.FieldRule<T>> rules = rulesByName(metadata);
        Map<String, FieldSecurityMetadata.FieldRule<Object>> existingRules =
                existingMetadata == null
                        ? Map.of()
                        : existingMetadata.fields().stream()
                                .collect(
                                        Collectors.toMap(
                                                FieldSecurityMetadata.FieldRule::name,
                                                rule -> rule,
                                                (left, right) -> left));
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            FieldSecurityMetadata.FieldRule<T> rule = rules.get(component.getName());
            if (rule == null) {
                args[i] = redactedValue(component.getType());
                continue;
            }
            Object value = rule.reader().apply(dto);
            Object existingValue = readExisting(existing, existingRules, rule.name());
            if (!rule.secured() || hasAnyRole(rule.writeRoles())) {
                args[i] = processWriteRecursively(value, existingValue, visited);
            } else if (rule.writePolicy() == WritePolicy.FAIL_ON_DENIED) {
                throw new AccessDeniedException("Write denied to field: " + rule.name());
            } else {
                args[i] =
                        existingValue == null
                                ? redactedValue(component.getType())
                                : processWriteRecursively(existingValue, existingValue, visited);
            }
        }
        return instantiateRecord(dto, components, args);
    }

    private static Object readExisting(
            Object existing,
            Map<String, FieldSecurityMetadata.FieldRule<Object>> existingRules,
            String fieldName) {
        if (existing == null) {
            return null;
        }
        FieldSecurityMetadata.FieldRule<Object> existingRule = existingRules.get(fieldName);
        if (existingRule != null) {
            return existingRule.reader().apply(existing);
        }
        Field existingField = findField(existing.getClass(), fieldName);
        if (existingField == null) {
            return null;
        }
        existingField.setAccessible(true);
        return getFieldValue(existingField, existing);
    }

    /**
     * Processes nested objects recursively applying field security rules for write operations.
     *
     * @param value the value to inspect
     * @param existingValue the existing object to source immutable data from
     * @param visited already visited objects to avoid cycles
     */
    private static Object processWriteRecursively(
            Object value, Object existingValue, Map<Object, Boolean> visited) {
        return FieldSecurityTraversal.processWriteRecursively(
                value,
                existingValue,
                visited,
                FieldSecurityUtil::isJavaLangType,
                (candidate, existingCandidate, seen) ->
                        filterWrite(candidate, existingCandidate, seen));
    }

    private static <T> Map<String, FieldSecurityMetadata.FieldRule<T>> rulesByName(
            FieldSecurityMetadata<T> metadata) {
        return metadata.fields().stream()
                .collect(
                        Collectors.toMap(
                                FieldSecurityMetadata.FieldRule::name,
                                rule -> rule,
                                (left, right) -> left));
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiateRecord(T source, RecordComponent[] components, Object[] args) {
        Class<?>[] parameterTypes =
                Arrays.stream(components)
                        .map(RecordComponent::getType)
                        .toArray(Class<?>[]::new);
        try {
            Constructor<?> constructor = source.getClass().getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return (T) constructor.newInstance(args);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(
                    "Failed to construct filtered record DTO: " + source.getClass().getName(), ex);
        }
    }

    private static Object redactedValue(Class<?> type) {
        return type.isPrimitive() ? defaultPrimitiveValue(type) : null;
    }

    /**
     * Locates a field with the given name on the supplied class or any superclass.
     *
     * @param clazz the class to inspect
     * @param name the field name
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

    private static Object getFieldValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void setFieldValue(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Sets the given field to a redacted value when access is denied.
     *
     * @param f the field to update
     * @param target the object containing the field
     */
    private static void setFieldToRedacted(Field f, Object target) {
        Class<?> type = f.getType();
        if (type.isPrimitive()) {
            setFieldValue(f, target, defaultPrimitiveValue(type));
        } else {
            setFieldValue(f, target, null);
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
    private static boolean hasAnyRole(@Nullable String[] roles) {
        return hasAnyRole(roles == null ? List.of() : Arrays.asList(roles));
    }

    private static boolean hasAnyRole(@Nullable List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }

        Set<String> required =
                roles.stream()
                        .filter(role -> role != null && !role.isBlank())
                        .collect(Collectors.toSet());
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
        return clazz.isPrimitive()
                || clazz.isEnum()
                || clazz.getPackageName().startsWith("java.");
    }

    private static @NonNull Optional<FieldSecurityMetadata<?>> metadataFor(
            @Nullable Class<?> type) {
        if (type == null) {
            return Optional.empty();
        }
        return METADATA_CACHE.computeIfAbsent(type, FieldSecurityUtil::resolveMetadata);
    }

    static void clearMetadataCacheForTesting() {
        METADATA_CACHE.clear();
    }

    private static Optional<FieldSecurityMetadata<?>> resolveMetadata(Class<?> type) {
        if (type == null) {
            return Optional.empty();
        }
        try {
            Method method = type.getMethod("fieldSecurityMetadata");
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                return Optional.empty();
            }
            if (!FieldSecurityMetadata.class.isAssignableFrom(method.getReturnType())) {
                return Optional.empty();
            }
            Object value = method.invoke(null);
            if (value instanceof FieldSecurityMetadata<?> metadata) {
                return Optional.of(metadata);
            }
            return Optional.empty();
        } catch (NoSuchMethodException ex) {
            return Optional.empty();
        } catch (IllegalAccessException ex) {
            return Optional.empty();
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException(
                    "Failed to resolve generated field security metadata", cause);
        }
    }
}

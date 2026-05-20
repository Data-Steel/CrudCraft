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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import nl.datasteel.crudcraft.annotations.security.RowPredicate;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.security.AccessDeniedException;
import nl.datasteel.crudcraft.runtime.security.scope.PrincipalScopeAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Generic claim-based row security handler for owner/tenant/client scenarios.
 *
 * @param <T> entity type
 */
public final class ClaimScopedRowSecurityHandler<T> implements RowSecurityHandler<T> {

    private static final Logger log = LoggerFactory.getLogger(ClaimScopedRowSecurityHandler.class);

    private static final ConcurrentMap<String, RowSecurityBeanProperty> PROPERTY_CACHE =
            new ConcurrentHashMap<>();

    private final String scopeName;
    private final String field;
    private final String claim;
    private final PrincipalScopeAccessor principalScopeAccessor;

    /**
     * Creates a claim-scoped row security handler.
     *
     * @param scopeName logical scope name for messages
     * @param field entity field that must match the claim
     * @param claim claim key that provides the scoped value
     * @param principalScopeAccessor accessor for principal claims
     */
    public ClaimScopedRowSecurityHandler(
            @NonNull String scopeName,
            @NonNull String field,
            @NonNull String claim,
            @NonNull PrincipalScopeAccessor principalScopeAccessor) {
        this.scopeName = requireText(scopeName, "scopeName");
        this.field = requireText(field, "field");
        this.claim = requireText(claim, "claim");
        this.principalScopeAccessor =
                Objects.requireNonNull(
                        principalScopeAccessor, "principalScopeAccessor must not be null");
    }

    @Override
    public @NonNull RowPredicate<T> rowFilter() {
        return (root, query, cb) ->
                scopedClaim()
                        .map(
                                value -> {
                                    RowSecurityBeanProperty property =
                                            propertyDescriptor(root.getJavaType(), field);
                                    Object converted = convertValue(value, property.propertyType());
                                    return cb.equal(root.get(field), converted);
                                })
                        .orElseGet(cb::disjunction);
    }

    @Override
    public void apply(@NonNull T entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        Object claimValue =
                scopedClaim()
                        .orElseThrow(
                                () ->
                                        new AccessDeniedException(
                                                "Missing required claim '"
                                                        + claim
                                                        + "' for scope "
                                                        + scopeName));
        RowSecurityBeanProperty property = propertyDescriptor(entity.getClass(), field);
        Method reader = property.reader();
        if (reader == null) {
            throw new AccessDeniedException("Scope field '" + field + "' is not readable");
        }
        try {
            Object current = property.read(entity);
            Object converted = convertValue(claimValue, property.propertyType());
            if (isUnsetValue(current, property.propertyType())) {
                if (property.writer() == null) {
                    throw new AccessDeniedException("Scope field '" + field + "' is not writable");
                }
                property.write(entity, converted);
            } else if (!Objects.equals(current, converted)) {
                throw new AccessDeniedException("Scope mismatch for '" + scopeName + "'");
            }
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Cannot apply scope '" + scopeName + "'", ex);
        } catch (InvocationTargetException ex) {
            Throwable cause = Optional.ofNullable(ex.getCause()).orElse(ex);
            if (cause instanceof Error error) {
                throw error;
            }
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Failed to apply scope '" + scopeName + "'", cause);
        }
    }

    private @NonNull Optional<Object> scopedClaim() {
        Optional<Object> value = principalScopeAccessor.claim(claim);
        if (value.isEmpty()) {
            log.warn("Claim {} missing in JWT", claim);
        }
        return value;
    }

    private @NonNull RowSecurityBeanProperty propertyDescriptor(
            @NonNull Class<?> type, @NonNull String fieldName) {
        return PROPERTY_CACHE.computeIfAbsent(
                cacheKey(type, fieldName), ignored -> descriptorFor(type, fieldName));
    }

    /**
     * Builds cache keys as fully qualified class name plus field name.
     *
     * <p>The explicit {@code class.name.field} format avoids collisions between entities that share
     * a simple class name or field name.
     */
    private static @NonNull String cacheKey(@NonNull Class<?> type, @NonNull String fieldName) {
        return type.getName() + "." + fieldName;
    }

    private @NonNull RowSecurityBeanProperty descriptorFor(
            @NonNull Class<?> type, @NonNull String fieldName) {
        return RowSecurityBeanProperty.forField(type, fieldName);
    }

    private @Nullable Object convertValue(@Nullable Object value, @NonNull Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        String asString = String.valueOf(value);
        if (String.class.equals(targetType)) {
            return asString;
        }
        if (Long.class.equals(targetType) || long.class.equals(targetType)) {
            return parseLong(asString, targetType);
        }
        if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
            return parseInteger(asString, targetType);
        }
        if (Boolean.class.equals(targetType) || boolean.class.equals(targetType)) {
            return parseBoolean(asString);
        }
        if (Short.class.equals(targetType) || short.class.equals(targetType)) {
            return parseShort(asString, targetType);
        }
        if (Byte.class.equals(targetType) || byte.class.equals(targetType)) {
            return parseByte(asString, targetType);
        }
        if (Double.class.equals(targetType) || double.class.equals(targetType)) {
            return parseDouble(asString, targetType);
        }
        if (Float.class.equals(targetType) || float.class.equals(targetType)) {
            return parseFloat(asString, targetType);
        }
        if (java.util.UUID.class.equals(targetType)) {
            return parseUuid(asString, targetType);
        }
        if (targetType.isEnum()) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Object enumValue = Enum.valueOf((Class<? extends Enum>) targetType, asString);
            return enumValue;
        }
        return value;
    }

    private static @NonNull String requireText(@NonNull String value, @NonNull String name) {
        String candidate = Objects.requireNonNull(value, name + " must not be null");
        if (candidate.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return candidate;
    }

    private static @NonNull Boolean parseBoolean(@NonNull String raw) {
        if ("true".equalsIgnoreCase(raw)) {
            return true;
        }
        if ("false".equalsIgnoreCase(raw)) {
            return false;
        }
        throw new IllegalArgumentException("Invalid boolean value: " + raw);
    }

    private static @NonNull Long parseLong(
            @NonNull String raw, @NonNull Class<?> targetType) {
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw invalidClaimValue(raw, targetType, ex);
        }
    }

    private static @NonNull Integer parseInteger(
            @NonNull String raw, @NonNull Class<?> targetType) {
        try {
            return Integer.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw invalidClaimValue(raw, targetType, ex);
        }
    }

    private static @NonNull Short parseShort(
            @NonNull String raw, @NonNull Class<?> targetType) {
        try {
            return Short.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw invalidClaimValue(raw, targetType, ex);
        }
    }

    private static @NonNull Byte parseByte(
            @NonNull String raw, @NonNull Class<?> targetType) {
        try {
            return Byte.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw invalidClaimValue(raw, targetType, ex);
        }
    }

    private static @NonNull Double parseDouble(
            @NonNull String raw, @NonNull Class<?> targetType) {
        try {
            return Double.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw invalidClaimValue(raw, targetType, ex);
        }
    }

    private static @NonNull Float parseFloat(
            @NonNull String raw, @NonNull Class<?> targetType) {
        try {
            return Float.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw invalidClaimValue(raw, targetType, ex);
        }
    }

    private static @NonNull java.util.UUID parseUuid(
            @NonNull String raw, @NonNull Class<?> targetType) {
        try {
            return java.util.UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw invalidClaimValue(raw, targetType, ex);
        }
    }

    private static IllegalArgumentException invalidClaimValue(
            String raw, Class<?> targetType, RuntimeException cause) {
        return new IllegalArgumentException(
                "Invalid scoped claim value '" + raw + "' for " + targetType.getSimpleName(),
                cause);
    }

    private static boolean isUnsetValue(
            @Nullable Object currentValue, @NonNull Class<?> propertyType) {
        if (currentValue == null) {
            return true;
        }
        if (!propertyType.isPrimitive()) {
            return false;
        }
        return switch (propertyType.getName()) {
            case "boolean" -> Boolean.FALSE.equals(currentValue);
            case "byte" -> Byte.valueOf((byte) 0).equals(currentValue);
            case "short" -> Short.valueOf((short) 0).equals(currentValue);
            case "int" -> Integer.valueOf(0).equals(currentValue);
            case "long" -> Long.valueOf(0L).equals(currentValue);
            case "float" -> Float.valueOf(0f).equals(currentValue);
            case "double" -> Double.valueOf(0d).equals(currentValue);
            case "char" -> Character.valueOf('\0').equals(currentValue);
            default -> false;
        };
    }
}

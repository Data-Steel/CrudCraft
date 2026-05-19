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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Objects;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityAdapter;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;


/**
 * Bridges field-level filtering into the neutral core extension chain.
 *
 * @param <T> entity type
 * @param <U> request DTO type
 */
public class FieldSecurityRuntimeExtension<T, U> implements CrudRuntimeExtension<T, U> {

    private final FieldSecurityAdapter fieldSecurityAdapter;
    private final boolean assertFieldSecurity;

    /**
     * Creates a field-security extension.
     *
     * @param fieldSecurityAdapter adapter used for read/write filtering
     */
    public FieldSecurityRuntimeExtension(FieldSecurityAdapter fieldSecurityAdapter) {
        this(fieldSecurityAdapter, Boolean.getBoolean("crudcraft.security.field.assert-filtered"));
    }

    /**
     * Creates a field-security extension with optional post-filter validation.
     *
     * <p>When validation is enabled, fields denied by
     * {@link FieldSecurityAdapter#canReadField(Class, String)} must be removed or changed by
     * {@link FieldSecurityAdapter#filterRead(Object)} before the DTO leaves the extension chain.
     * This mode is intended for development and integration tests of custom adapters.
     *
     * @param fieldSecurityAdapter adapter used for read/write filtering
     * @param assertFieldSecurity whether denied readable fields are validated after filtering
     */
    public FieldSecurityRuntimeExtension(
            FieldSecurityAdapter fieldSecurityAdapter, boolean assertFieldSecurity) {
        this.fieldSecurityAdapter =
                Objects.requireNonNull(
                        fieldSecurityAdapter, "fieldSecurityAdapter must not be null");
        this.assertFieldSecurity = assertFieldSecurity;
    }

    @Override
    public <P> P afterRead(P dto) {
        P filtered = fieldSecurityAdapter.filterRead(dto);
        if (assertFieldSecurity) {
            validateReadFilter(dto, filtered);
        }
        return filtered;
    }

    @Override
    public U beforeCreate(U request) {
        return fieldSecurityAdapter.filterWrite(request, null);
    }

    @Override
    public U beforeUpdate(U request, T existing) {
        return fieldSecurityAdapter.filterWrite(request, existing);
    }

    private void validateReadFilter(Object original, Object filtered) {
        if (original == null || filtered == null) {
            return;
        }
        validateReadFilter(original, filtered, new IdentityHashMap<>());
    }

    private void validateReadFilter(
            Object original, Object filtered, IdentityHashMap<Object, Boolean> visited) {
        if (original == null || filtered == null || visited.containsKey(original)) {
            return;
        }
        visited.put(original, Boolean.TRUE);
        Class<?> dtoType = original.getClass();
        if (dtoType.isRecord()) {
            validateRecordFilter(dtoType, original, filtered, visited);
            return;
        }
        for (Field field : dtoType.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Object originalValue = readField(field, original);
            Object filteredValue = readField(field, filtered);
            if (!fieldSecurityAdapter.canReadField(dtoType, field.getName())) {
                if (originalValue != null && Objects.equals(originalValue, filteredValue)) {
                    throw deniedFieldException(dtoType, field.getName());
                }
                continue;
            }
            validateNestedReadFilter(originalValue, filteredValue, visited);
        }
    }

    private void validateRecordFilter(
            Class<?> dtoType,
            Object original,
            Object filtered,
            IdentityHashMap<Object, Boolean> visited) {
        for (RecordComponent component : dtoType.getRecordComponents()) {
            Object originalValue = readRecordComponent(component, original);
            Object filteredValue = readRecordComponent(component, filtered);
            if (!fieldSecurityAdapter.canReadField(dtoType, component.getName())) {
                if (originalValue != null && Objects.equals(originalValue, filteredValue)) {
                    throw deniedFieldException(dtoType, component.getName());
                }
                continue;
            }
            validateNestedReadFilter(originalValue, filteredValue, visited);
        }
    }

    private void validateNestedReadFilter(
            Object originalValue, Object filteredValue, IdentityHashMap<Object, Boolean> visited) {
        if (originalValue == null || filteredValue == null) {
            return;
        }
        Class<?> type = originalValue.getClass();
        if (isLeafType(type)) {
            return;
        }
        if (type.isArray()) {
            int length = java.lang.reflect.Array.getLength(originalValue);
            int filteredLength = java.lang.reflect.Array.getLength(filteredValue);
            int max = Math.min(length, filteredLength);
            for (int i = 0; i < max; i++) {
                validateNestedReadFilter(
                        java.lang.reflect.Array.get(originalValue, i),
                        java.lang.reflect.Array.get(filteredValue, i),
                        visited);
            }
            return;
        }
        if (originalValue instanceof Collection<?> originalCollection
                && filteredValue instanceof Collection<?> filteredCollection) {
            java.util.Iterator<?> originalIterator = originalCollection.iterator();
            java.util.Iterator<?> filteredIterator = filteredCollection.iterator();
            while (originalIterator.hasNext() && filteredIterator.hasNext()) {
                validateNestedReadFilter(originalIterator.next(), filteredIterator.next(), visited);
            }
            return;
        }
        validateReadFilter(originalValue, filteredValue, visited);
    }

    private boolean isLeafType(Class<?> type) {
        return type.isPrimitive()
                || type.isEnum()
                || type.getPackageName().startsWith("java.");
    }

    private Object readRecordComponent(RecordComponent component, Object target) {
        try {
            return component.getAccessor().invoke(target);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(
                    "Could not validate field-security redaction for " + component.getName(), ex);
        }
    }

    private IllegalStateException deniedFieldException(Class<?> dtoType, String fieldName) {
        return new IllegalStateException(
                "Field-security adapter did not redact denied field: "
                        + dtoType.getName()
                        + "."
                        + fieldName);
    }

    private Object readField(Field field, Object target) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(
                    "Could not validate field-security redaction for " + field.getName(), ex);
        }
    }
}

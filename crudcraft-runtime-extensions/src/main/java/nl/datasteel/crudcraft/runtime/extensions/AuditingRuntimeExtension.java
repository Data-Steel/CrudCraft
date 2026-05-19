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

package nl.datasteel.crudcraft.runtime.extensions;

import java.lang.reflect.Field;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;


/**
 * CrudCraft runtime hook that updates {@link AuditableExtension} embeddables before persistence.
 */
public class AuditingRuntimeExtension implements CrudRuntimeExtension<Object, Object> {

    /** Creates an auditing runtime extension. */
    public AuditingRuntimeExtension() {}

    @Override
    public void beforeSave(Object entity) {
        if (entity == null) {
            return;
        }
        Class<?> current = entity.getClass();
        while (current != Object.class) {
            touchAuditableFields(entity, current);
            current = current.getSuperclass();
        }
    }

    private void touchAuditableFields(Object entity, Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (!AuditableExtension.class.isAssignableFrom(field.getType())) {
                continue;
            }
            touchAuditableField(entity, field);
        }
    }

    private void touchAuditableField(Object entity, Field field) {
        try {
            field.setAccessible(true);
            AuditableExtension audit = (AuditableExtension) field.get(entity);
            if (audit == null) {
                audit = new AuditableExtension();
                field.set(entity, audit);
            }
            if (audit.getCreatedAt() == null) {
                audit.markCreated();
            } else {
                audit.markUpdated();
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot update auditable field: " + field.getName(), e);
        }
    }
}

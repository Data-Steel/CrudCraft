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

import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AuditingRuntimeExtensionTest {

    private final AuditingRuntimeExtension extension = new AuditingRuntimeExtension();

    @Test
    void beforeSaveInitializesNullAuditEmbeddable() {
        EntityWithNullAudit entity = new EntityWithNullAudit();

        extension.beforeSave(entity);

        assertNotNull(entity.audit);
        assertNotNull(entity.audit.getCreatedAt());
        assertEquals(entity.audit.getCreatedAt(), entity.audit.getUpdatedAt());
    }

    @Test
    void beforeSaveUpdatesExistingAuditEmbeddable() {
        EntityWithAudit entity = new EntityWithAudit();
        entity.audit.markCreated();
        Instant createdAt = entity.audit.getCreatedAt();
        Instant updatedAt = entity.audit.getUpdatedAt();

        extension.beforeSave(entity);

        assertEquals(createdAt, entity.audit.getCreatedAt());
        assertTrue(!entity.audit.getUpdatedAt().isBefore(updatedAt));
    }

    @Test
    void beforeSaveScansSuperclassFields() {
        ChildEntity entity = new ChildEntity();

        extension.beforeSave(entity);

        AuditableExtension audit = ((EntityWithAudit) entity).audit();
        assertNotNull(audit.getCreatedAt());
        assertNotNull(audit.getUpdatedAt());
    }

    @Test
    void beforeSaveIgnoresEntitiesWithoutAuditFields() {
        EntityWithoutAudit entity = new EntityWithoutAudit();

        extension.beforeSave(entity);

        assertEquals("unchanged", entity.value);
    }

    @Test
    void beforeSaveIgnoresNullEntity() {
        extension.beforeSave(null);
    }

    private static class EntityWithAudit {
        private AuditableExtension audit = new AuditableExtension();

        private AuditableExtension audit() {
            return audit;
        }
    }

    private static class EntityWithNullAudit {
        private AuditableExtension audit;
    }

    private static class EntityWithoutAudit {
        private String value = "unchanged";
    }

    private static class ChildEntity extends EntityWithAudit {}
}

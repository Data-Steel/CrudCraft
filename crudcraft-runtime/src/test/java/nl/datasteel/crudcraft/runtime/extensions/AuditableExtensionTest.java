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
package nl.datasteel.crudcraft.runtime.extensions;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AuditableExtensionTest {

    @Test
    void onCreateSetsTimestamps() {
        AuditableExtension ext = new AuditableExtension();
        ext.onCreate();
        assertNotNull(ext.getCreatedAt());
        assertNotNull(ext.getUpdatedAt());
        assertEquals(ext.getCreatedAt(), ext.getUpdatedAt());
    }

    @Test
    void onUpdateChangesUpdatedAtOnly() throws InterruptedException {
        AuditableExtension ext = new AuditableExtension();
        ext.onCreate();
        var created = ext.getCreatedAt();
        Thread.sleep(5);
        ext.onUpdate();
        assertEquals(created, ext.getCreatedAt());
        assertTrue(ext.getUpdatedAt().isAfter(created));
    }
}

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SoftDeleteExtensionTest {

    @Test
    void defaultsToNotDeleted() {
        SoftDeleteExtension extension = new SoftDeleteExtension();

        assertFalse(extension.isDeleted());
        assertNull(extension.getDeletedAt());
    }

    @Test
    void setDeletedTogglesState() {
        SoftDeleteExtension extension = new SoftDeleteExtension();

        extension.setDeleted(true);
        assertTrue(extension.isDeleted());
        assertNotNull(extension.getDeletedAt());
        Instant firstDeletedAt = extension.getDeletedAt();

        extension.setDeleted(true);
        assertSame(firstDeletedAt, extension.getDeletedAt());

        extension.setDeleted(false);
        assertFalse(extension.isDeleted());
        assertNull(extension.getDeletedAt());
    }
}

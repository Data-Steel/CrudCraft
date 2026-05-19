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

package nl.datasteel.crudcraft.runtime.export;

import jakarta.persistence.Entity;
import nl.datasteel.crudcraft.runtime.metadata.EntityMetadata;
import nl.datasteel.crudcraft.runtime.metadata.EntityMetadataRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


class EntityMetadataRegistryTest {

    @Test
    void registryCachesAndClearsMetadata() {
        EntityMetadataRegistry registry = new EntityMetadataRegistry();

        assertFalse(registry.hasMetadata(SampleEntity.class));

        EntityMetadata first = registry.getMetadata(SampleEntity.class);
        EntityMetadata second = registry.getMetadata(SampleEntity.class);

        assertTrue(registry.hasMetadata(SampleEntity.class));
        assertSame(first, second);

        registry.clear();

        assertFalse(registry.hasMetadata(SampleEntity.class));
    }

    @Entity
    private static final class SampleEntity {
        private Long id;
    }
}

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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import nl.datasteel.crudcraft.annotations.fields.Dto;


/**
 * AuditableExtension is an embeddable class that provides auditing capabilities for entities,
 * tracking creation and update timestamps.
 */
@Embeddable
public class AuditableExtension {
    /** Creates the embeddable extension. */
    public AuditableExtension() {
        // Constructor without any parameters stays empty
    }

    /** The timestamp when the entity was created. */
    @Dto
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** The timestamp when the entity was last updated. */
    @Dto
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Sets both audit timestamps for a newly persisted entity. */
    public void markCreated() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** Updates the modification timestamp for an existing entity. */
    public void markUpdated() {
        this.updatedAt = Instant.now();
    }

    void onCreate() {
        markCreated();
    }

    void onUpdate() {
        markUpdated();
    }

    /**
     * Gets the createdAt timestamp.
     *
     * @return the createdAt timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the updatedAt timestamp.
     *
     * @return the updatedAt timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

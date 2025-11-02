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

import com.querydsl.core.annotations.QueryExclude;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;

/**
 * AuditableExtension is an embeddable class that provides auditing capabilities
 * for entities, tracking creation and update timestamps.
 */
@QueryExclude
@Embeddable
public class AuditableExtension {

    /**
     * The timestamp when the entity was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * The timestamp when the entity was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Hooks into the JPA lifecycle to set the createdAt and updatedAt timestamps
     * when the entity is first persisted.
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Hooks into the JPA lifecycle to update the updatedAt timestamp
     * whenever the entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
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

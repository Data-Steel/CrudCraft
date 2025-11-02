/**
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.runtime.extensions;

import com.querydsl.core.annotations.QueryExclude;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;

/**
 * SoftDeleteExtension is an embeddable class that provides soft delete functionality
 * for entities, allowing them to be marked as deleted without being physically removed
 * from the database.
 */
@QueryExclude
@Embeddable
public class SoftDeleteExtension {

    /**
     * Indicates whether the entity is marked as deleted.
     * Default value is false, meaning the entity is not deleted.
     */
    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;

    /**
     * The timestamp when the entity was marked as deleted.
     * This field is nullable, as it will only be set when the entity is actually deleted.
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Sets the deleted status and the timestamp when the entity is marked as deleted.
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Sets the deleted status of the entity.
     * If set to true, it also sets the deletedAt timestamp to the current time.
     *
     * @param deleted true if the entity is marked as deleted, false otherwise
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

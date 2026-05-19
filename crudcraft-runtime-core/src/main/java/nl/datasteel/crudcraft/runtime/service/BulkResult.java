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

package nl.datasteel.crudcraft.runtime.service;

import java.util.List;


/**
 * Result envelope for bulk operations.
 *
 * <p>Successful items are returned in completion order. Failed items include the zero-based input
 * index so clients can retry only the failed inputs after correcting the reported error. Bulk
 * operations are not guaranteed to be atomic unless the concrete service adds a transaction policy
 * that rolls back the whole batch.
 *
 * @param succeeded successfully processed item values
 * @param failed failed input items with their source index and message
 * @param <T> success item type
 */
public record BulkResult<T>(List<T> succeeded, List<Failure> failed) {

    /** Creates an immutable bulk result. */
    public BulkResult {
        succeeded = succeeded == null ? List.of() : List.copyOf(succeeded);
        failed = failed == null ? List.of() : List.copyOf(failed);
    }

    /**
     * Returns an empty result.
     *
     * @param <T> success item type
     * @return empty result
     */
    public static <T> BulkResult<T> empty() {
        return new BulkResult<>(List.of(), List.of());
    }

    /**
     * Returns whether at least one input failed.
     *
     * @return true when failed items are present
     */
    public boolean hasFailures() {
        return !failed.isEmpty();
    }

    /**
     * Failed input item.
     *
     * @param index zero-based input index
     * @param message actionable failure message
     */
    public record Failure(int index, String message) {}
}

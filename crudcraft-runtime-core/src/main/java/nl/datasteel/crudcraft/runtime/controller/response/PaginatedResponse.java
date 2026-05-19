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

package nl.datasteel.crudcraft.runtime.controller.response;

import java.util.List;


/**
 * Uniform container for paginated REST responses.
 *
 * @param <T> the DTO type
 * @param content page content
 * @param page zero-based page number
 * @param size page size
 * @param totalPages total page count
 * @param totalElements total element count
 * @param first whether this is the first page
 * @param last whether this is the last page
 */
public record PaginatedResponse<T>(
        List<T> content,
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean first,
        boolean last) {
    /** Defensive copy and validation constructor. */
    public PaginatedResponse {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size < 0) {
            throw new IllegalArgumentException("size must be >= 0");
        }
        if (totalPages < 0) {
            throw new IllegalArgumentException("totalPages must be >= 0");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements must be >= 0");
        }
        content = (content == null) ? List.of() : List.copyOf(content);
    }
}

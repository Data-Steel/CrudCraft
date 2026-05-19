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
import java.util.Objects;


/**
 * Keyset paging result containing one page of data and an optional next cursor.
 *
 * <p>The cursor is an opaque, URL-safe token produced by CrudCraft from ordered sort values. It is
 * not Java object serialization and carries no cryptographic integrity guarantee; treat it as an
 * untrusted pagination hint that the service validates before use.
 *
 * @param <T> row type
 */
public final class KeysetPage<T> {

    private final List<T> content;
    private final String nextCursor;

    /**
     * Creates a keyset page with defensive copying of the content list.
     *
     * @param content page content
     * @param nextCursor cursor for fetching the next page, or {@code null}
     */
    public KeysetPage(List<T> content, String nextCursor) {
        this.content = List.copyOf(Objects.requireNonNull(content, "content"));
        this.nextCursor = nextCursor;
    }

    /**
     * Returns the page content.
     *
     * @return the page content
     */
    public List<T> content() {
        return content;
    }

    /**
     * Returns the cursor for the next page, if any.
     *
     * @return the next cursor, or {@code null}
     */
    public String nextCursor() {
        return nextCursor;
    }
}

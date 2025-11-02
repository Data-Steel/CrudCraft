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
package nl.datasteel.crudcraft.projection.api;

import java.util.List;

/**
 * Holds projected DTOs and total count metadata.
 */
public record ProjectionResult<D>(List<D> content, long totalElements) {

    /**
     * Defensive copy constructor to ensure immutability of the content list.
     */
    public ProjectionResult {
        content = List.copyOf(content);
    }

    /**
     * Returns the content of this ProjectionResult.
     */
    @Override
    public List<D> content() {
        return List.copyOf(content);
    }
}

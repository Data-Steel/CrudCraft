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

package nl.datasteel.crudcraft.runtime.security.scope;

import java.util.Optional;
import java.util.Set;


/** Reads security scope data from the current authenticated principal. */
public interface PrincipalScopeAccessor {

    /**
     * Reads an arbitrary claim by name.
     *
     * @param claimName claim key
     * @return optional claim value
     */
    Optional<Object> claim(String claimName);

    /**
     * Returns the current user ID when available.
     *
     * @return optional user id from the principal
     */
    default Optional<String> currentUserId() {
        return claim("sub").map(String::valueOf);
    }

    /**
     * Returns normalized authorities without ROLE_ prefix.
     *
     * @return normalized role names
     */
    Set<String> roles();

    /**
     * Returns true when an authenticated principal is available.
     *
     * @return {@code true} when the current principal is authenticated
     */
    boolean isAuthenticated();
}

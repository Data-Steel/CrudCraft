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

import nl.datasteel.crudcraft.runtime.InternalApi;


/**
 * Optional audit hook invoked when a row exists but is denied by runtime read filters.
 *
 * <p>Implementations can publish structured security audit events for hidden-row access attempts.
 */
@InternalApi
public interface ReadDeniedAuditHook {

    /**
     * Called when a read operation targets an existing row that is hidden by runtime read filters.
     *
     * @param entityType entity type that was requested
     * @param id requested identifier
     * @param operation read operation name (for example {@code findById} or {@code update})
     */
    void onReadDenied(Class<?> entityType, Object id, String operation);
}


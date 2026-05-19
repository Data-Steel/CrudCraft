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

package nl.datasteel.crudcraft.annotations.security;

/**
 * Field-level write behavior when the current principal cannot write a DTO property.
 *
 * <p>The policy is evaluated by the runtime field-security extension before data is mapped into an
 * entity. Read security and row security are independent: this enum only controls what happens to
 * a denied write for a field that appears in a create, update, patch, or upsert request.
 */
public enum WritePolicy {
    /**
     * Reject the write by throwing the runtime access-denied exception.
     *
     * <p>Use this for sensitive or integrity-critical fields where clients must know that the
     * requested change was not accepted, such as roles, ownership, prices, or approval states.
     */
    FAIL_ON_DENIED,

    /**
     * Ignore the denied field value and continue processing the rest of the request.
     *
     * <p>Use this for partial-update workflows where a denied optional field should be preserved
     * without failing the entire request. The existing entity value is left untouched.
     */
    SKIP_ON_DENIED
}

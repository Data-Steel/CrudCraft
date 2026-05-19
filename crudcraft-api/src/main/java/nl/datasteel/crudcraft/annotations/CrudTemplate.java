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

package nl.datasteel.crudcraft.annotations;

import java.util.EnumSet;
import java.util.Set;


/**
 * Standard endpoint bundles for {@link nl.datasteel.crudcraft.annotations.classes.CrudCrafted}.
 *
 * <p>A template is an allow-list: endpoints present in {@link #resolveEndpoints()} are generated,
 * and endpoints absent from the set are not generated unless explicitly added via
 * {@code includeEndpoints}. The final generation set is the selected template minus omitted
 * endpoints plus included endpoints.
 */
public enum CrudTemplate implements CrudEndpointPolicy {
    /** Enables all core CRUD, bulk, lookup, existence, count, ref, and validation endpoints. */
    FULL(
            EnumSet.of(
                    CrudEndpoint.GET_ALL,
                    CrudEndpoint.GET_ALL_REF,
                    CrudEndpoint.GET_ONE,
                    CrudEndpoint.POST,
                    CrudEndpoint.PUT,
                    CrudEndpoint.PATCH,
                    CrudEndpoint.DELETE,
                    CrudEndpoint.BULK_CREATE,
                    CrudEndpoint.BULK_UPDATE,
                    CrudEndpoint.BULK_PATCH,
                    CrudEndpoint.BULK_UPSERT,
                    CrudEndpoint.BULK_DELETE,
                    CrudEndpoint.FIND_BY_IDS,
                    CrudEndpoint.EXISTS,
                    CrudEndpoint.COUNT,
                    CrudEndpoint.VALIDATE)),

    /** Enables read-only endpoints: list, ref list, get-one, find-by-ids, exists, and count. */
    READ_ONLY(
            EnumSet.of(
                    CrudEndpoint.GET_ALL,
                    CrudEndpoint.GET_ALL_REF,
                    CrudEndpoint.GET_ONE,
                    CrudEndpoint.FIND_BY_IDS,
                    CrudEndpoint.EXISTS,
                    CrudEndpoint.COUNT)),

    /** Enables reads and create endpoints, but omits update, patch, and delete endpoints. */
    IMMUTABLE_WRITE(
            EnumSet.of(
                    CrudEndpoint.GET_ALL,
                    CrudEndpoint.GET_ALL_REF,
                    CrudEndpoint.GET_ONE,
                    CrudEndpoint.POST,
                    CrudEndpoint.BULK_CREATE,
                    CrudEndpoint.FIND_BY_IDS,
                    CrudEndpoint.EXISTS,
                    CrudEndpoint.COUNT)),

    /** Enables basic reads plus single and bulk patch endpoints; omits create, update, delete. */
    PATCH_ONLY(
            EnumSet.of(
                    CrudEndpoint.GET_ALL,
                    CrudEndpoint.GET_ONE,
                    CrudEndpoint.PATCH,
                    CrudEndpoint.BULK_PATCH,
                    CrudEndpoint.EXISTS,
                    CrudEndpoint.COUNT)),

    /** Enables the full core API except single and bulk delete endpoints. */
    NO_DELETE(
            EnumSet.of(
                    CrudEndpoint.GET_ALL,
                    CrudEndpoint.GET_ALL_REF,
                    CrudEndpoint.GET_ONE,
                    CrudEndpoint.POST,
                    CrudEndpoint.PUT,
                    CrudEndpoint.PATCH,
                    CrudEndpoint.BULK_CREATE,
                    CrudEndpoint.BULK_UPDATE,
                    CrudEndpoint.BULK_PATCH,
                    CrudEndpoint.BULK_UPSERT,
                    CrudEndpoint.FIND_BY_IDS,
                    CrudEndpoint.EXISTS,
                    CrudEndpoint.COUNT,
                    CrudEndpoint.VALIDATE)),

    /** Enables single-entity CRUD, exists, count, ref list, and validation; omits all bulk APIs. */
    NO_BATCH(
            EnumSet.of(
                    CrudEndpoint.GET_ALL,
                    CrudEndpoint.GET_ALL_REF,
                    CrudEndpoint.GET_ONE,
                    CrudEndpoint.POST,
                    CrudEndpoint.PUT,
                    CrudEndpoint.PATCH,
                    CrudEndpoint.DELETE,
                    CrudEndpoint.EXISTS,
                    CrudEndpoint.COUNT,
                    CrudEndpoint.VALIDATE)),

    /** Enables only create, bulk create, and bulk upsert endpoints. */
    CREATE_ONLY(EnumSet.of(CrudEndpoint.POST, CrudEndpoint.BULK_CREATE, CrudEndpoint.BULK_UPSERT)),

    /** Enables only the generated search endpoint. */
    SEARCH_ONLY(EnumSet.of(CrudEndpoint.SEARCH)),

    /** Enables only metadata-style count and exists endpoints. */
    META_ONLY(EnumSet.of(CrudEndpoint.COUNT, CrudEndpoint.EXISTS)),

    /** Enables lightweight public read endpoints: ref list and get-one. */
    LIGHT_PUBLIC(EnumSet.of(CrudEndpoint.GET_ALL_REF, CrudEndpoint.GET_ONE)),

    /** Enables single-entity internal APIs and validation, omitting bulk, search, and export. */
    SECURE_INTERNAL(
            EnumSet.of(
                    CrudEndpoint.GET_ALL,
                    CrudEndpoint.GET_ALL_REF,
                    CrudEndpoint.GET_ONE,
                    CrudEndpoint.POST,
                    CrudEndpoint.PUT,
                    CrudEndpoint.PATCH,
                    CrudEndpoint.DELETE,
                    CrudEndpoint.EXISTS,
                    CrudEndpoint.COUNT,
                    CrudEndpoint.VALIDATE)),

    /** Enables only the request validation endpoint. */
    VALIDATION_ONLY(EnumSet.of(CrudEndpoint.VALIDATE));

    /** The set of endpoints defined by this template. */
    private final Set<CrudEndpoint> endpoints;

    /**
     * Constructs a CrudTemplate with the specified set of endpoints.
     *
     * @param endpoints the set of CRUD endpoints that this template will include
     */
    CrudTemplate(Set<CrudEndpoint> endpoints) {
        this.endpoints = EnumSet.copyOf(endpoints);
    }

    /**
     * Returns the endpoints defined by this template.
     *
     * @return a set of {@link CrudEndpoint} instances that this template includes
     */
    @Override
    public Set<CrudEndpoint> resolveEndpoints() {
        return EnumSet.copyOf(endpoints);
    }

    /**
     * Return the complete set of endpoints that are effective when adding and removing other
     * endpoints from the template.
     *
     * @param omitted the set of endpoints to be omitted from the template
     * @param included the set of endpoints to be included in the template
     * @return a set of {@link CrudEndpoint} instances that are effective after applying the omitted
     *     and included endpoints
     */
    public Set<CrudEndpoint> getEffectiveEndpoints(
            Set<CrudEndpoint> omitted, Set<CrudEndpoint> included) {
        Set<CrudEndpoint> effective = EnumSet.copyOf(endpoints);
        if (omitted != null) {
            effective.removeAll(omitted);
        }
        if (included != null) {
            effective.addAll(included);
        }
        return effective;
    }
}

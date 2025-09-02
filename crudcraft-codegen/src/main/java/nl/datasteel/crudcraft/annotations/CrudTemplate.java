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

package nl.datasteel.crudcraft.annotations;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enum representing various CRUD endpoint templates.
 * Each template defines a specific set of CRUD operations that can be performed.
 * These templates can be used to configure the behavior of CRUD endpoints in applications.
 */
public enum CrudTemplate implements CrudEndpointPolicy {
    /** Full API with every endpoint enabled. */
    FULL(EnumSet.allOf(CrudEndpoint.class)),

    /** Only read operations. */
    READ_ONLY(EnumSet.of(
            CrudEndpoint.GET_ALL,
            CrudEndpoint.GET_ALL_REF,
            CrudEndpoint.GET_ONE,
            CrudEndpoint.FIND_BY_IDS,
            CrudEndpoint.EXISTS,
            CrudEndpoint.COUNT,
            CrudEndpoint.SEARCH,
            CrudEndpoint.EXPORT
    )),

    /** GET + POST operations, immutable once created. */
    IMMUTABLE_WRITE(EnumSet.of(
            CrudEndpoint.GET_ALL,
            CrudEndpoint.GET_ALL_REF,
            CrudEndpoint.GET_ONE,
            CrudEndpoint.POST,
            CrudEndpoint.BULK_CREATE,
            CrudEndpoint.FIND_BY_IDS,
            CrudEndpoint.EXISTS,
            CrudEndpoint.COUNT,
            CrudEndpoint.SEARCH,
            CrudEndpoint.EXPORT
    )),

    /** Only PATCH for modifications, plus basic GET. */
    PATCH_ONLY(EnumSet.of(
            CrudEndpoint.GET_ALL,
            CrudEndpoint.GET_ONE,
            CrudEndpoint.PATCH,
            CrudEndpoint.BULK_PATCH,
            CrudEndpoint.EXISTS,
            CrudEndpoint.COUNT
    )),

    /** Everything except delete. */
    NO_DELETE(EnumSet.complementOf(EnumSet.of(
            CrudEndpoint.DELETE,
            CrudEndpoint.BULK_DELETE
    ))),

    /** No bulk operations at all. */
    NO_BATCH(EnumSet.of(
            CrudEndpoint.GET_ALL,
            CrudEndpoint.GET_ALL_REF,
            CrudEndpoint.GET_ONE,
            CrudEndpoint.POST,
            CrudEndpoint.PUT,
            CrudEndpoint.PATCH,
            CrudEndpoint.DELETE,
            CrudEndpoint.EXISTS,
            CrudEndpoint.COUNT,
            CrudEndpoint.SEARCH,
            CrudEndpoint.EXPORT,
            CrudEndpoint.VALIDATE
    )),

    /** Only creation endpoints. */
    CREATE_ONLY(EnumSet.of(
            CrudEndpoint.POST,
            CrudEndpoint.BULK_CREATE,
            CrudEndpoint.BULK_UPSERT
    )),

    /** Only search style endpoints. */
    SEARCH_ONLY(EnumSet.of(
            CrudEndpoint.SEARCH,
            CrudEndpoint.EXPORT
    )),

    /** Only metadata operations. */
    META_ONLY(EnumSet.of(
            CrudEndpoint.COUNT,
            CrudEndpoint.EXISTS
    )),

    /** Public lightweight endpoints. */
    LIGHT_PUBLIC(EnumSet.of(
            CrudEndpoint.GET_ALL_REF,
            CrudEndpoint.GET_ONE,
            CrudEndpoint.SEARCH,
            CrudEndpoint.EXPORT
    )),

    /** All except bulk and export style endpoints. */
    SECURE_INTERNAL(EnumSet.of(
            CrudEndpoint.GET_ALL,
            CrudEndpoint.GET_ALL_REF,
            CrudEndpoint.GET_ONE,
            CrudEndpoint.POST,
            CrudEndpoint.PUT,
            CrudEndpoint.PATCH,
            CrudEndpoint.DELETE,
            CrudEndpoint.EXISTS,
            CrudEndpoint.COUNT,
            CrudEndpoint.SEARCH,
            CrudEndpoint.VALIDATE
    )),

    /** Only validate endpoint. */
    VALIDATION_ONLY(EnumSet.of(
            CrudEndpoint.VALIDATE
    ));

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
     * Return the complete set of endpoints that are effective
     * when adding and removing other endpoints from the template.
     *
     * @param omitted the set of endpoints to be omitted from the template
     * @param included the set of endpoints to be included in the template
     * @return a set of {@link CrudEndpoint} instances that are effective
     *         after applying the omitted and included endpoints
     */
    public Set<CrudEndpoint> getEffectiveEndpoints(Set<CrudEndpoint> omitted,
                                                   Set<CrudEndpoint> included) {
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
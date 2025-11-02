/*
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
package nl.datasteel.crudcraft.annotations;

import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CrudTemplateTest {

    @Test
    void fullReturnsAllEndpoints() {
        assertEquals(EnumSet.allOf(CrudEndpoint.class), CrudTemplate.FULL.resolveEndpoints());
    }

    @Test
    void readOnlyOmitsWriteEndpoints() {
        Set<CrudEndpoint> endpoints = CrudTemplate.READ_ONLY.resolveEndpoints();
        assertFalse(endpoints.contains(CrudEndpoint.POST));
        assertTrue(endpoints.contains(CrudEndpoint.GET_ONE));
    }

    @Test
    void resolveEndpointsReturnsCopy() {
        Set<CrudEndpoint> first = CrudTemplate.NO_DELETE.resolveEndpoints();
        first.add(CrudEndpoint.DELETE);
        assertFalse(CrudTemplate.NO_DELETE.resolveEndpoints().contains(CrudEndpoint.DELETE));
    }

    @Test
    void getEffectiveEndpointsHandlesNullsAndOverrides() {
        Set<CrudEndpoint> result = CrudTemplate.NO_BATCH.getEffectiveEndpoints(null, null);
        assertEquals(CrudTemplate.NO_BATCH.resolveEndpoints(), result);

        result = CrudTemplate.NO_BATCH.getEffectiveEndpoints(
                EnumSet.of(CrudEndpoint.GET_ALL), EnumSet.of(CrudEndpoint.DELETE));
        assertFalse(result.contains(CrudEndpoint.GET_ALL));
        assertTrue(result.contains(CrudEndpoint.DELETE));

        result = CrudTemplate.NO_BATCH.getEffectiveEndpoints(
                EnumSet.of(CrudEndpoint.DELETE), EnumSet.of(CrudEndpoint.DELETE));
        assertTrue(result.contains(CrudEndpoint.DELETE));
    }

    @Test
    void getEffectiveEndpointsWithEmptySetsLeavesTemplateUnchanged() {
        Set<CrudEndpoint> result = CrudTemplate.READ_ONLY.getEffectiveEndpoints(
                EnumSet.noneOf(CrudEndpoint.class), EnumSet.noneOf(CrudEndpoint.class));
        assertEquals(CrudTemplate.READ_ONLY.resolveEndpoints(), result);
    }

    @Test
    void getEffectiveEndpointsIgnoresUnknownAndDuplicates() {
        Set<CrudEndpoint> result = CrudTemplate.CREATE_ONLY.getEffectiveEndpoints(
                EnumSet.of(CrudEndpoint.DELETE), EnumSet.of(CrudEndpoint.POST));
        assertEquals(CrudTemplate.CREATE_ONLY.resolveEndpoints(), result);
    }
}


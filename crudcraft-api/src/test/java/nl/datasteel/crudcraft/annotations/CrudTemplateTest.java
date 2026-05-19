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
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class CrudTemplateTest {

    @ParameterizedTest
    @EnumSource(CrudTemplate.class)
    void resolveEndpointsReturnsNonEmptyCopy(CrudTemplate template) {
        Set<CrudEndpoint> original = template.resolveEndpoints();
        Set<CrudEndpoint> copy = template.resolveEndpoints();

        assertFalse(copy.isEmpty());
        assertEquals(original, copy);
        assertTrue(copy.containsAll(original));

        copy.remove(copy.iterator().next());

        assertNotSame(copy, original);
        assertEquals(original, template.resolveEndpoints());
    }

    @Test
    void getEffectiveEndpointsHandlesNullOverrides() {
        Set<CrudEndpoint> endpoints = CrudTemplate.FULL.getEffectiveEndpoints(null, null);

        assertEquals(16, endpoints.size());
        assertTrue(endpoints.contains(CrudEndpoint.GET_ALL));
        assertFalse(endpoints.contains(CrudEndpoint.SEARCH));
        assertFalse(endpoints.contains(CrudEndpoint.EXPORT));
    }

    @Test
    void getEffectiveEndpointsRemovesAndAddsEndpoints() {
        Set<CrudEndpoint> endpoints =
                CrudTemplate.READ_ONLY.getEffectiveEndpoints(
                        EnumSet.of(CrudEndpoint.GET_ONE, CrudEndpoint.COUNT),
                        EnumSet.of(CrudEndpoint.DELETE, CrudEndpoint.POST));

        assertFalse(endpoints.contains(CrudEndpoint.GET_ONE));
        assertFalse(endpoints.contains(CrudEndpoint.COUNT));
        assertTrue(endpoints.contains(CrudEndpoint.DELETE));
        assertTrue(endpoints.contains(CrudEndpoint.POST));
    }

    @Test
    void getEffectiveEndpointsPrefersIncludeWhenAlsoOmitted() {
        Set<CrudEndpoint> endpoints =
                CrudTemplate.FULL.getEffectiveEndpoints(
                        EnumSet.of(CrudEndpoint.GET_ALL), EnumSet.of(CrudEndpoint.GET_ALL));

        assertTrue(endpoints.contains(CrudEndpoint.GET_ALL));
    }

    @Test
    void getEffectiveEndpointsRejectsNullEntryInIncludedSet() {
        Set<CrudEndpoint> included = new HashSet<>();
        included.add(null);

        assertThrows(
                NullPointerException.class,
                () -> CrudTemplate.READ_ONLY.getEffectiveEndpoints(null, included));
    }
}

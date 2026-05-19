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

package nl.datasteel.crudcraft.codegen.descriptor;

import java.util.Set;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class EndpointOptionsTest {
    private static final class TestPolicy implements CrudEndpointPolicy {
        @Override
        public Set<CrudEndpoint> resolveEndpoints() {
            return Set.of(CrudEndpoint.GET_ONE);
        }

        @Override
        public String name() {
            return "TEST";
        }
    }

    @Test
    void arraysAreCopiedAndEqualsWorks() {
        CrudEndpoint[] omit = {CrudEndpoint.DELETE};
        CrudEndpoint[] include = {CrudEndpoint.POST};
        EndpointOptions opts =
                new EndpointOptions(CrudTemplate.FULL, omit, include, CrudTemplate.class);
        omit[0] = CrudEndpoint.GET_ONE;
        include[0] = CrudEndpoint.GET_ALL;
        assertArrayEquals(new CrudEndpoint[] {CrudEndpoint.DELETE}, opts.getOmitEndpoints());
        assertArrayEquals(new CrudEndpoint[] {CrudEndpoint.POST}, opts.getIncludeEndpoints());
        EndpointOptions same =
                new EndpointOptions(
                        CrudTemplate.FULL,
                        new CrudEndpoint[] {CrudEndpoint.DELETE},
                        new CrudEndpoint[] {CrudEndpoint.POST},
                        CrudTemplate.class);
        int expectedHash = java.util.Objects.hash(CrudTemplate.FULL, CrudTemplate.class);
        expectedHash =
                31 * expectedHash
                        + java.util.Arrays.hashCode(new CrudEndpoint[] {CrudEndpoint.DELETE});
        expectedHash =
                31 * expectedHash
                        + java.util.Arrays.hashCode(new CrudEndpoint[] {CrudEndpoint.POST});
        assertEquals(opts, same);
        assertEquals(expectedHash, opts.hashCode());
        assertArrayEquals(new CrudEndpoint[] {CrudEndpoint.DELETE}, opts.omitEndpoints());
        assertArrayEquals(new CrudEndpoint[] {CrudEndpoint.POST}, opts.includeEndpoints());
        assertEquals(CrudTemplate.FULL, opts.getTemplate());
        assertEquals(CrudTemplate.class, opts.getEndpointPolicy());
        assertEquals(
                "EndpointOptions{template=FULL, omitEndpoints=[DELETE], includeEndpoints=[POST],"
                        + " endpointPolicy=class nl.datasteel.crudcraft.annotations.CrudTemplate}",
                opts.toString());
    }

    @Test
    void equalityAndNullArrayNormalizationBranches() {
        EndpointOptions base =
                new EndpointOptions(CrudTemplate.FULL, null, null, CrudTemplate.class);
        assertTrue(base.getOmitEndpoints().length == 0);
        assertTrue(base.getIncludeEndpoints().length == 0);

        assertEquals(base, base);
        assertNotEquals(base, null);
        assertNotEquals(base, "x");
        assertNotEquals(
                base,
                new EndpointOptions(
                        CrudTemplate.READ_ONLY,
                        new CrudEndpoint[0],
                        new CrudEndpoint[0],
                        CrudTemplate.class));
        assertNotEquals(
                base,
                new EndpointOptions(
                        CrudTemplate.FULL,
                        new CrudEndpoint[] {CrudEndpoint.DELETE},
                        new CrudEndpoint[0],
                        CrudTemplate.class));
        assertNotEquals(
                base,
                new EndpointOptions(
                        CrudTemplate.FULL,
                        new CrudEndpoint[0],
                        new CrudEndpoint[] {CrudEndpoint.POST},
                        CrudTemplate.class));
        assertNotEquals(
                base,
                new EndpointOptions(
                        CrudTemplate.FULL,
                        new CrudEndpoint[0],
                        new CrudEndpoint[0],
                        TestPolicy.class));
    }
}

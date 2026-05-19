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

package nl.datasteel.crudcraft.runtime.search;

import java.util.Set;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SearchPathGuardTest {

    @BeforeEach
    void clearCycleCache() {
        SearchPathGuard.clearCycleCacheForTests();
    }

    @Test
    void enforceMaxDepthAcceptsPathsWithinLimit() {
        assertEquals("a.b.c", SearchPathGuard.enforceMaxDepth("a.b.c", 3));
        assertEquals("a.b.c", SearchPathGuard.enforceMaxDepth("a.b.c", 5));
        assertEquals("name", SearchPathGuard.enforceMaxDepth("name", 1));
    }

    @Test
    void enforceMaxDepthRejectsPathsBeyondLimit() {
        BadRequestException ex =
                assertThrows(
                        BadRequestException.class,
                        () -> SearchPathGuard.enforceMaxDepth("a.b.c.d", 3));
        assertTrue(ex.getMessage().contains("exceeds the configured maximum depth"));
        assertTrue(ex.getMessage().contains("a.b.c.d"));
    }

    @Test
    void enforceMaxDepthIgnoresUnboundedOrBlankInputs() {
        assertEquals(
                "a.b.c.d.e.f",
                SearchPathGuard.enforceMaxDepth("a.b.c.d.e.f", Integer.MAX_VALUE));
        assertEquals("", SearchPathGuard.enforceMaxDepth("", 1));
        assertNull(SearchPathGuard.enforceMaxDepth(null, 1));
    }

    @Test
    void enforceMaxDepthRejectsNonPositiveConfiguration() {
        assertThrows(BadRequestException.class, () -> SearchPathGuard.enforceMaxDepth("a.b", 0));
        assertThrows(BadRequestException.class, () -> SearchPathGuard.enforceMaxDepth("a.b", -1));
    }

    @Test
    void rejectCyclesAcceptsWellFormedPathsAndBlankInputs() {
        assertEquals("author.address.city", SearchPathGuard.rejectCycles("author.address.city"));
        assertEquals("", SearchPathGuard.rejectCycles(""));
        assertNull(SearchPathGuard.rejectCycles(null));
    }

    @Test
    void rejectCyclesAllowsRepeatedSegmentsConsistently() {
        assertEquals("parent.child.parent", SearchPathGuard.rejectCycles("parent.child.parent"));
        assertEquals("parent.child.parent", SearchPathGuard.rejectCycles("parent.child.parent"));
    }

    @Test
    void rejectCyclesWithMetadataRejectsSegmentRevisit() {
        Set<String> allowedPaths = Set.of("parent.child.parent", "parent.child.name");
        BadRequestException ex =
                assertThrows(
                        BadRequestException.class,
                        () -> SearchPathGuard.rejectCycles("parent.child.parent", allowedPaths));

        assertTrue(ex.getMessage().contains("cyclic traversal"));
    }

    @Test
    void rejectCyclesWithMetadataAcceptsAcyclicPath() {
        Set<String> allowedPaths = Set.of("author.address.city", "author.address.country");
        assertEquals(
                "author.address.city",
                SearchPathGuard.rejectCycles("author.address.city", allowedPaths));
    }

    @Test
    void rejectCyclesRejectsMalformedSegmentBoundaries() {
        BadRequestException first =
                assertThrows(
                        BadRequestException.class,
                        () -> SearchPathGuard.rejectCycles("parent..child"));
        BadRequestException second =
                assertThrows(
                        BadRequestException.class,
                        () -> SearchPathGuard.rejectCycles("parent..child"));

        assertTrue(first.getMessage().contains("Invalid searchable path rejected"));
        assertEquals(first.getMessage(), second.getMessage());
    }

    @Test
    void rejectCyclesParsesEveryDottedSegmentBoundaryForMalformedInput() {
        assertEquals("a.bb.ccc", SearchPathGuard.rejectCycles("a.bb.ccc"));
        assertEquals("alpha.beta.gamma.beta", SearchPathGuard.rejectCycles("alpha.beta.gamma.beta"));
        assertEquals("root.child.leaf.root", SearchPathGuard.rejectCycles("root.child.leaf.root"));

        assertThrows(
                BadRequestException.class,
                () -> SearchPathGuard.rejectCycles("alpha.beta..gamma"));
        assertThrows(
                BadRequestException.class,
                () -> SearchPathGuard.rejectCycles(".root.child.leaf"));
    }
}

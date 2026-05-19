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

package nl.datasteel.crudcraft.codegen.fileheader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AbstractFileHeaderTest {

    private static final class H extends AbstractFileHeader {}

    @Test
    void protectedConstructorIsCoveredForStrictMutationLineCoverage() {
        assertNotNull(new H());
    }

    @Test
    void editableHeaderHandlesNullAndBlankExtras() {
        String defaultHeader = H.editableHeader("Service", "User", "com.example", "Gen");
        assertTrue(defaultHeader.contains("Generated Service layer stub for User."));
        assertTrue(defaultHeader.contains("UserServiceBase"));
        assertTrue(defaultHeader.contains("Generation time: 2026-01-01T00:00:00Z"));

        String extraOnly = H.editableHeader("Service", "User", "com.example", "Gen", "Extra only");
        assertTrue(extraOnly.contains("Extra only"));

        String withNullExtras =
                H.editableHeader("Service", "User", "com.example", "Gen", null, "   ");
        assertTrue(withNullExtras.contains(AbstractFileHeader.MARKER_GENERATED));
        assertTrue(withNullExtras.contains(AbstractFileHeader.MARKER_EDITABLE));

        String withExtras =
                H.editableHeader(
                        "Service", "User", "com.example", "Gen", "Extra info", "Feature line");
        assertTrue(withExtras.contains("Extra info"));
        assertTrue(withExtras.contains("Feature line"));
    }

    @Test
    void strictHeaderHandlesNullAndBlankExtras() {
        String defaultHeader = H.strictHeader("User", "com.example", "Gen");
        assertTrue(defaultHeader.contains("Generated model file for User"));
        assertTrue(defaultHeader.contains("Generation time: 2026-01-01T00:00:00Z"));

        String extraOnly = H.strictHeader("User", "com.example", "Gen", "Extra only");
        assertTrue(extraOnly.contains("Extra only"));

        String withNullExtras = H.strictHeader("User", "com.example", "Gen", null, "  ");
        assertTrue(withNullExtras.contains(AbstractFileHeader.MARKER_GENERATED));

        String withExtras =
                H.strictHeader("User", "com.example", "Gen", "Extra strict", "Feature strict");
        assertTrue(withExtras.contains("Extra strict"));
        assertTrue(withExtras.contains("Feature strict"));
    }
}

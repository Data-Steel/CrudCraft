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

import nl.datasteel.crudcraft.annotations.security.WritePolicy;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Security;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SecurityTest {

    @Test
    void defaultsAndDefensiveCopiesAreApplied() {
        String[] read = new String[] {"USER"};
        String[] write = new String[] {"ADMIN"};
        Security security = new Security(true, read, write, null);
        read[0] = "CHANGED";
        write[0] = "CHANGED";

        assertArrayEquals(new String[] {"USER"}, security.getReadRoles());
        assertArrayEquals(new String[] {"ADMIN"}, security.getWriteRoles());
        assertEquals(WritePolicy.SKIP_ON_DENIED, security.getWritePolicy());
        assertTrue(security.hasFieldSecurity());
    }

    @Test
    void equalsAndHashCodeCoverBranches() {
        Security base =
                new Security(
                        true,
                        new String[] {"USER"},
                        new String[] {"ADMIN"},
                        WritePolicy.SKIP_ON_DENIED);
        Security same =
                new Security(
                        true,
                        new String[] {"USER"},
                        new String[] {"ADMIN"},
                        WritePolicy.SKIP_ON_DENIED);

        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());
        assertEquals(base, base);
        assertNotEquals(base, null);
        assertNotEquals(base, "x");
        assertNotEquals(
                base,
                new Security(
                        false,
                        new String[] {"USER"},
                        new String[] {"ADMIN"},
                        WritePolicy.SKIP_ON_DENIED));
        assertNotEquals(
                base,
                new Security(
                        true,
                        new String[] {"OTHER"},
                        new String[] {"ADMIN"},
                        WritePolicy.SKIP_ON_DENIED));
        assertNotEquals(
                base,
                new Security(
                        true,
                        new String[] {"USER"},
                        new String[] {"OTHER"},
                        WritePolicy.SKIP_ON_DENIED));
        assertNotEquals(
                base,
                new Security(
                        true,
                        new String[] {"USER"},
                        new String[] {"ADMIN"},
                        WritePolicy.FAIL_ON_DENIED));
    }
}

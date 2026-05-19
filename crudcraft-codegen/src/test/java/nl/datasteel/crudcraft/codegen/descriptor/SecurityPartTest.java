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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


class SecurityPartTest {

    @Test
    void arraysAreDefensivelyCopied() {
        String[] read = {"r1"};
        String[] write = {"w1"};
        Security sec = new Security(true, read, write);
        read[0] = "r2";
        write[0] = "w2";
        assertArrayEquals(new String[] {"r1"}, sec.getReadRoles());
        assertArrayEquals(new String[] {"w1"}, sec.getWriteRoles());
        String[] returned = sec.getReadRoles();
        returned[0] = "x";
        assertArrayEquals(new String[] {"r1"}, sec.getReadRoles());
    }

    @Test
    void equalsAndHashCodeDependOnValues() {
        Security a = new Security(true, new String[] {"r"}, new String[] {"w"});
        Security b = new Security(true, new String[] {"r"}, new String[] {"w"});
        int expectedHash = Boolean.hashCode(true);
        expectedHash = 31 * expectedHash + java.util.Arrays.hashCode(new String[] {"r"});
        expectedHash = 31 * expectedHash + java.util.Arrays.hashCode(new String[] {"w"});
        expectedHash = 31 * expectedHash + WritePolicy.SKIP_ON_DENIED.hashCode();
        assertEquals(a, b);
        assertEquals(expectedHash, a.hashCode());
        assertArrayEquals(new String[] {"r"}, a.readRoles());
        assertArrayEquals(new String[] {"w"}, a.writeRoles());
        assertEquals(
                "Security{defined=true, readRoles=[r], writeRoles=[w],"
                        + " writePolicy=SKIP_ON_DENIED}",
                a.toString());
    }

    @Test
    void notEqualWhenRolesDiffer() {
        Security a = new Security(true, new String[] {"r"}, new String[] {"w"});
        Security b = new Security(true, new String[] {"r2"}, new String[] {"w"});
        assertNotEquals(a, b);
        assertNotEquals(a, new Security(false, new String[] {"r"}, new String[] {"w"}));
        assertNotEquals(a, new Security(true, new String[] {"r"}, new String[] {"w2"}));
        assertNotEquals(
                a,
                new Security(
                        true, new String[] {"r"}, new String[] {"w"}, WritePolicy.FAIL_ON_DENIED));
        assertNotEquals(a, "x");
    }

    @Test
    void hasFieldSecurityReflectsDefined() {
        Security s = new Security(false, null, null);
        assertFalse(s.hasFieldSecurity());
    }

    @Test
    void writePolicyDefaultsToSkipOnDenied() {
        Security security = new Security(true, new String[] {"A"}, new String[] {"B"}, null);
        assertEquals(WritePolicy.SKIP_ON_DENIED, security.getWritePolicy());
    }
}

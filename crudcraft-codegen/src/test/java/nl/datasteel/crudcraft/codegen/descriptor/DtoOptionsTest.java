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

import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class DtoOptionsTest {

    @Test
    void gettersReflectConstructorArguments() {
        String[] dtos = {"A", "B"};
        DtoOptions opts = new DtoOptions(true, false, true, dtos, false);
        assertTrue(opts.isInDto());
        assertFalse(opts.isInRequest());
        assertTrue(opts.isInRef());
        assertArrayEquals(dtos, opts.getResponseDtos());
    }

    @Test
    void booleanAccessorsReflectFalseAndTrueValues() {
        DtoOptions allFalse = new DtoOptions(false, false, false, new String[0], false);
        assertFalse(allFalse.isInDto());
        assertFalse(allFalse.inDto());
        assertFalse(allFalse.isInRequest());
        assertFalse(allFalse.inRequest());
        assertFalse(allFalse.isInRef());
        assertFalse(allFalse.inRef());
        assertFalse(allFalse.isLob());

        DtoOptions allTrue = new DtoOptions(true, true, true, new String[0], true);
        assertTrue(allTrue.isInDto());
        assertTrue(allTrue.inDto());
        assertTrue(allTrue.isInRequest());
        assertTrue(allTrue.inRequest());
        assertTrue(allTrue.isInRef());
        assertTrue(allTrue.inRef());
        assertTrue(allTrue.isLob());
    }

    @Test
    void allowsNullResponseDtos() {
        DtoOptions opts = new DtoOptions(false, false, false, null, false);
        assertNull(opts.getResponseDtos());
        assertNull(opts.responseDtos());
    }

    @Test
    void responseDtoArrayDefensivelyCopied() {
        String[] dtos = {"A"};
        DtoOptions opts = new DtoOptions(true, true, true, dtos, false);
        dtos[0] = "B";
        assertArrayEquals(new String[] {"A"}, opts.getResponseDtos());

        String[] fromGetter = opts.getResponseDtos();
        fromGetter[0] = "C";
        assertArrayEquals(new String[] {"A"}, opts.getResponseDtos());
    }

    @Test
    void equalsAndHashCodeCoverAllFields() {
        DtoOptions base = new DtoOptions(true, false, true, new String[] {"X"}, false);
        int expectedHash =
                31 * java.util.Objects.hash(true, false, true, false)
                        + java.util.Arrays.hashCode(new String[] {"X"});
        assertEquals(base, base);
        assertNotEquals(base, null);
        assertNotEquals(base, "x");
        assertEquals(base, new DtoOptions(true, false, true, new String[] {"X"}, false));
        assertEquals(expectedHash, base.hashCode());
        assertNotEquals(base, new DtoOptions(false, false, true, new String[] {"X"}, false));
        assertNotEquals(base, new DtoOptions(true, true, true, new String[] {"X"}, false));
        assertNotEquals(base, new DtoOptions(true, false, false, new String[] {"X"}, false));
        assertNotEquals(base, new DtoOptions(true, false, true, new String[] {"Y"}, false));
        assertNotEquals(base, new DtoOptions(true, false, true, new String[] {"X"}, true));
        assertArrayEquals(new String[] {"X"}, base.responseDtos());
        assertTrue(base.inDto());
        assertFalse(base.inRequest());
        assertTrue(base.inRef());
        assertFalse(base.isLob());
        assertEquals(
                "DtoOptions{inDto=true, inRequest=false, inRef=true, responseDtos=[X],"
                        + " isLob=false}",
                base.toString());
    }
}

/**
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
package nl.datasteel.crudcraft.codegen.descriptor;

import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DtoOptionsTest {

    @Test
    void gettersReflectConstructorArguments() {
        String[] dtos = {"A", "B"};
        DtoOptions opts = new DtoOptions(true, false, true, dtos);
        assertTrue(opts.isInDto());
        assertFalse(opts.isInRequest());
        assertTrue(opts.isInRef());
        assertArrayEquals(dtos, opts.getResponseDtos());
    }

    @Test
    void allowsNullResponseDtos() {
        DtoOptions opts = new DtoOptions(false, false, false, null);
        assertNull(opts.getResponseDtos());
    }

    @Test
    void responseDtoArrayNotDefensivelyCopied() {
        String[] dtos = {"A"};
        DtoOptions opts = new DtoOptions(true, true, true, dtos);
        dtos[0] = "B";
        assertArrayEquals(new String[]{"B"}, opts.getResponseDtos());
    }
}

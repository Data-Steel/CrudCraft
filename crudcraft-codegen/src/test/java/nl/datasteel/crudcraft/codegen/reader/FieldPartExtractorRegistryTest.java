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

package nl.datasteel.crudcraft.codegen.reader;

import java.lang.reflect.InvocationTargetException;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;
import nl.datasteel.crudcraft.codegen.reader.field.FieldPartExtractor;
import nl.datasteel.crudcraft.codegen.reader.field.FieldPartExtractorRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class FieldPartExtractorRegistryTest {

    private static final class UnknownPart {}

    @Test
    void returnsExtractorForKnownPart() {
        FieldPartExtractor<DtoOptions> ex = FieldPartExtractorRegistry.get(DtoOptions.class);
        assertNotNull(ex);
    }

    @Test
    void returnsNullForUnknownPart() {
        assertNull(FieldPartExtractorRegistry.get(UnknownPart.class));
    }

    @Test
    void constructorRejectsReflectionInstantiation() throws Exception {
        var constructor = FieldPartExtractorRegistry.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown =
                assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(thrown.getCause() instanceof IllegalStateException);
    }
}

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
package nl.datasteel.crudcraft.codegen.descriptor;

import com.squareup.javapoet.AnnotationSpec;
import java.util.ArrayList;
import java.util.List;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Validation;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ValidationPartTest {

    @Test
    void validationsCopiedAndImmutable() {
        List<AnnotationSpec> list = new ArrayList<>();
        list.add(AnnotationSpec.builder(Deprecated.class).build());
        Validation v = new Validation(list);
        list.clear();
        assertEquals(1, v.getValidations().size());
        assertThrows(UnsupportedOperationException.class, () -> v.getValidations().add(null));
    }

    @Test
    void nullListGivesEmpty() {
        Validation v = new Validation(null);
        assertTrue(v.getValidations().isEmpty());
    }
}

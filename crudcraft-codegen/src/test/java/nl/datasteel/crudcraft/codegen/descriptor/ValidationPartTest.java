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

import com.palantir.javapoet.AnnotationSpec;
import java.util.ArrayList;
import java.util.List;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ValidationPartTest {

    @Test
    void validationsCopiedAndImmutable() {
        List<AnnotationSpec> list = new ArrayList<>();
        list.add(AnnotationSpec.builder(Deprecated.class).build());
        Validation v = new Validation(list);
        list.clear();
        assertEquals(1, v.getValidations().size());
        assertEquals(1, v.validations().size());
        assertThrows(UnsupportedOperationException.class, () -> v.getValidations().add(null));
        assertThrows(UnsupportedOperationException.class, () -> v.validations().add(null));
    }

    @Test
    void nullListGivesEmpty() {
        Validation v = new Validation(null);
        assertTrue(v.getValidations().isEmpty());
    }
}

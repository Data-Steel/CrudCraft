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

import java.util.ArrayList;
import java.util.List;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ModelIdentityTest {

    @Test
    void fieldsAreDefensivelyCopied() {
        FieldDescriptor fd = new FieldDescriptor(new Identity("f", null), null, null, null, null, null, null);
        List<FieldDescriptor> list = new ArrayList<>();
        list.add(fd);
        ModelIdentity mi = new ModelIdentity("Name", "pkg", list, "base");
        list.clear();
        assertEquals(1, mi.getFields().size());
        assertThrows(UnsupportedOperationException.class, () -> mi.getFields().add(fd));
    }
}

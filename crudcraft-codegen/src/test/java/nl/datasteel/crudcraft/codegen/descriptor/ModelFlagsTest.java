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

import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ModelFlagsTest {

    @Test
    void gettersReflectValues() {
        ModelFlags flags = new ModelFlags(true, false, true);
        assertTrue(flags.isEditable());
        assertFalse(flags.isCrudCraftEntity());
        assertTrue(flags.isEmbeddable());
    }

    @Test
    void handlesAllFalse() {
        ModelFlags flags = new ModelFlags(false, false, false);
        assertFalse(flags.isEditable());
        assertFalse(flags.isCrudCraftEntity());
        assertFalse(flags.isEmbeddable());
    }
}

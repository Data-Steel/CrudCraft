/*
 * Copyright (c) 2025 CrudCraft contributors
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

import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.reader.model.ModelPartExtractor;
import nl.datasteel.crudcraft.codegen.reader.model.ModelPartExtractorRegistry;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class ModelPartExtractorRegistryTest {

    @Test
    void returnsExtractorForKnownPart() {
        ModelPartExtractor<ModelFlags> ex = ModelPartExtractorRegistry.get(ModelFlags.class);
        assertNotNull(ex);
    }

    @Test
    void returnsNullForUnknownPart() {
        class X {}
        assertNull(ModelPartExtractorRegistry.get(X.class));
    }
}

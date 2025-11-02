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
package nl.datasteel.crudcraft.projection.mapping;

import java.util.List;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadata;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SimpleProjectionMetadataRegistryTest {

    static class DummyMetadata implements ProjectionMetadata<String> {
        @Override
        public Class<String> dtoType() { return String.class; }

        /**
         * Return attributes in the order of the DTO constructor parameters.
         */
        @Override
        public List<Attribute> attributes() {
            return null;
        }
    }

    @Test
    void registerAndRetrieveMetadata() {
        SimpleProjectionMetadataRegistry registry = new SimpleProjectionMetadataRegistry();
        DummyMetadata md = new DummyMetadata();
        registry.register(md);
        assertSame(md, registry.getMetadata(String.class));
    }

    @Test
    void getMetadataReturnsNullWhenAbsent() {
        SimpleProjectionMetadataRegistry registry = new SimpleProjectionMetadataRegistry();
        assertNull(registry.getMetadata(Integer.class));
    }
}

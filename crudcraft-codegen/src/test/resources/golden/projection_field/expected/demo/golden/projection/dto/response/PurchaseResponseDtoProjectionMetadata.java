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
package demo.golden.projection.dto.response;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;

public final class PurchaseResponseDtoProjectionMetadata implements ProjectionMetadata<PurchaseResponseDto> {
    private static final List<ProjectionMetadata.Attribute> ATTRIBUTES = List.of(new Attr("id","id",() -> null,false,null),new Attr("reference","reference",() -> null,false,null),new Attr("customer","customer",() -> null,false,null),new Attr("customerName","customer.name",() -> null,false,null));

    @Override
    public Class<PurchaseResponseDto> dtoType() {
        return PurchaseResponseDto.class;
    }

    @Override
    public List<ProjectionMetadata.Attribute> attributes() {
        return ATTRIBUTES;
    }

    private static class Attr implements ProjectionMetadata.Attribute {
        private final String dtoFieldName;

        private final String path;

        private final Supplier<ProjectionMetadata<?>> nested;

        private final boolean collection;

        private final BiConsumer<Object, List<?>> mutator;

        Attr(String dtoFieldName, String path, Supplier<ProjectionMetadata<?>> nested,
                boolean collection, BiConsumer<Object, List<?>> mutator) {
            this.dtoFieldName = dtoFieldName;
            this.path = path;
            this.nested = nested;
            this.collection = collection;
            this.mutator = mutator;
        }

        @Override
        public String dtoFieldName() {
            return dtoFieldName;
        }

        @Override
        public String path() {
            return path;
        }

        @Override
        public ProjectionMetadata<?> nested() {
            return nested.get();
        }

        @Override
        public boolean collection() {
            return collection;
        }

        @Override
        public BiConsumer<Object, List<?>> mutator() {
            return mutator;
        }
    }
}

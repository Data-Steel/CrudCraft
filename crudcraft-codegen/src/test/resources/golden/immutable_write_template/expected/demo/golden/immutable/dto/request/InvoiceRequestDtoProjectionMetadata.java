package demo.golden.immutable.dto.request;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;

public final class InvoiceRequestDtoProjectionMetadata implements ProjectionMetadata<InvoiceRequestDto> {
    private static final List<ProjectionMetadata.Attribute> ATTRIBUTES = List.of(new Attr("number","number",() -> null,false,null),new Attr("amount","amount",() -> null,false,null));

    @Override
    public Class<InvoiceRequestDto> dtoType() {
        return InvoiceRequestDto.class;
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

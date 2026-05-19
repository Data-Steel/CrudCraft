package demo.golden.umbrella.dto.request;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;

public final class AccountRequestDtoProjectionMetadata implements ProjectionMetadata<AccountRequestDto> {
    private static final List<ProjectionMetadata.Attribute> ATTRIBUTES = List.of(new Attr("name","name",() -> null,false,null),new Attr("tenantId","tenantId",() -> null,false,null),new Attr("ownerId","ownerId",() -> null,false,null),new Attr("type","type",() -> null,false,null),new Attr("createdAt","createdAt",() -> null,false,null),new Attr("secret","secret",() -> null,false,null),new Attr("profile","profile",() -> null,false,null),new Attr("tagIds","tagIds",() -> null,true,null),new Attr("logo","logo",() -> null,false,null));

    @Override
    public Class<AccountRequestDto> dtoType() {
        return AccountRequestDto.class;
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

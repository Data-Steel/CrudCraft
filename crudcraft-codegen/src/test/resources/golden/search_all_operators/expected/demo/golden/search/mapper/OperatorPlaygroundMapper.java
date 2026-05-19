package demo.golden.search.mapper;

import demo.golden.search.OperatorPlayground;
import demo.golden.search.SearchTag;
import demo.golden.search.dto.ref.OperatorPlaygroundRef;
import demo.golden.search.dto.request.OperatorPlaygroundRequestDto;
import demo.golden.search.dto.response.OperatorPlaygroundResponseDto;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import nl.datasteel.crudcraft.runtime.exception.MapperException;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Generated model file for OperatorPlayground; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: OperatorPlayground
 * - Package: demo.golden.search.mapper
 * - Generator: MapperGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * To make changes, edit the entity model class and rebuild the project.
 * Do not edit or rename this file manually.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE,
        injectionStrategy = InjectionStrategy.FIELD,
        uses = {SearchTagMapper.class}
)
public interface OperatorPlaygroundMapper extends EntityMapper<OperatorPlayground, OperatorPlaygroundRequestDto, OperatorPlaygroundResponseDto, OperatorPlaygroundRef, UUID> {
    /**
     * Maps a create or upsert request DTO into a new entity instance.
     * Nested relationship identifiers are resolved through generated @Named helper methods.
     */
    @Override
    @Mapping(
            target = "tags",
            source = "tagIds",
            qualifiedByName = "OperatorPlaygroundMapSearchTagSet"
    )
    OperatorPlayground fromRequest(OperatorPlaygroundRequestDto request);

    /**
     * Applies a full update request DTO to an existing entity.
     * Relationship id fields replace the corresponding entity relationships.
     */
    @Override
    @Mapping(
            target = "tags",
            source = "tagIds",
            qualifiedByName = "OperatorPlaygroundMapSearchTagSet"
    )
    OperatorPlayground update(@MappingTarget OperatorPlayground entity,
            OperatorPlaygroundRequestDto request);

    /**
     * Applies patch semantics to an existing entity.
     * Null request properties are ignored by MapStruct so existing entity values remain unchanged.
     */
    @Override
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(
            target = "tags",
            source = "tagIds",
            qualifiedByName = "OperatorPlaygroundMapSearchTagSet"
    )
    OperatorPlayground patch(@MappingTarget OperatorPlayground entity,
            OperatorPlaygroundRequestDto request);

    /**
     * Maps an entity to the full response DTO.
     * Relationship fields are represented by generated reference DTO mappings where applicable.
     */
    @Override
    OperatorPlaygroundResponseDto toResponse(OperatorPlayground entity);

    /**
     * Maps an entity to its lightweight reference DTO.
     */
    @Override
    OperatorPlaygroundRef toRef(OperatorPlayground entity);

    /**
     * Extracts the identifier from a request DTO for upsert operations.
     * Generated record DTOs expose the id as a record component.
     */
    @Override
    default UUID getIdFromRequest(OperatorPlaygroundRequestDto request) {
        try {
            if (request != null && request.getClass().isRecord()) {
                return (UUID) request.getClass().getMethod("id").invoke(request);
            }
            var wrapper = new BeanWrapperImpl(request);
            Object idVal = wrapper.getPropertyValue("id");
            return (UUID) idVal;
        } catch (Exception e) {
            throw new MapperException("Failed to read 'id' property from request DTO: " + (request == null ? "<null>" : request.getClass().getName()), e);
        }
    }

    @Named("OperatorPlaygroundMapSearchTag")
    default SearchTag mapSearchTag(UUID id) {
        if (id == null) {
            return null;
        }
        SearchTag entity = new SearchTag();
        entity.setId(id);
        return entity;
    }

    @Named("OperatorPlaygroundMapSearchTagSet")
    default Set<SearchTag> mapSearchTagSet(Set<UUID> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream().map(this::mapSearchTag).collect(Collectors.toSet());
    }
}

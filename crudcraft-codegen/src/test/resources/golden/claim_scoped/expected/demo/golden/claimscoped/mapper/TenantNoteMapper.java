package demo.golden.claimscoped.mapper;

import demo.golden.claimscoped.TenantNote;
import demo.golden.claimscoped.dto.ref.TenantNoteRef;
import demo.golden.claimscoped.dto.request.TenantNoteRequestDto;
import demo.golden.claimscoped.dto.response.TenantNoteResponseDto;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.exception.MapperException;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Generated model file for TenantNote; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: TenantNote
 * - Package: demo.golden.claimscoped.mapper
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
        injectionStrategy = InjectionStrategy.FIELD
)
public interface TenantNoteMapper extends EntityMapper<TenantNote, TenantNoteRequestDto, TenantNoteResponseDto, TenantNoteRef, UUID> {
    /**
     * Maps a create or upsert request DTO into a new entity instance.
     * Nested relationship identifiers are resolved through generated @Named helper methods.
     */
    @Override
    TenantNote fromRequest(TenantNoteRequestDto request);

    /**
     * Applies a full update request DTO to an existing entity.
     * Relationship id fields replace the corresponding entity relationships.
     */
    @Override
    TenantNote update(@MappingTarget TenantNote entity, TenantNoteRequestDto request);

    /**
     * Applies patch semantics to an existing entity.
     * Null request properties are ignored by MapStruct so existing entity values remain unchanged.
     */
    @Override
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    TenantNote patch(@MappingTarget TenantNote entity, TenantNoteRequestDto request);

    /**
     * Maps an entity to the full response DTO.
     * Relationship fields are represented by generated reference DTO mappings where applicable.
     */
    @Override
    TenantNoteResponseDto toResponse(TenantNote entity);

    /**
     * Maps an entity to its lightweight reference DTO.
     */
    @Override
    TenantNoteRef toRef(TenantNote entity);

    /**
     * Extracts the identifier from a request DTO for upsert operations.
     * Generated record DTOs expose the id as a record component.
     */
    @Override
    default UUID getIdFromRequest(TenantNoteRequestDto request) {
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
}

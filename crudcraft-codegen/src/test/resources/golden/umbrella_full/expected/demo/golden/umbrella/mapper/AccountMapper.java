package demo.golden.umbrella.mapper;

import demo.golden.umbrella.Account;
import demo.golden.umbrella.AccountTag;
import demo.golden.umbrella.dto.ref.AccountRef;
import demo.golden.umbrella.dto.request.AccountRequestDto;
import demo.golden.umbrella.dto.response.AccountDetailResponseDto;
import demo.golden.umbrella.dto.response.AccountListResponseDto;
import demo.golden.umbrella.dto.response.AccountResponseDto;
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
 * Generated model file for Account; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: Account
 * - Package: demo.golden.umbrella.mapper
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
        uses = {AccountTagMapper.class}
)
public interface AccountMapper extends EntityMapper<Account, AccountRequestDto, AccountResponseDto, AccountRef, UUID> {
    /**
     * Maps a create or upsert request DTO into a new entity instance.
     * Nested relationship identifiers are resolved through generated @Named helper methods.
     */
    @Override
    @Mapping(
            target = "tags",
            source = "tagIds",
            qualifiedByName = "AccountMapAccountTagSet"
    )
    Account fromRequest(AccountRequestDto request);

    /**
     * Applies a full update request DTO to an existing entity.
     * Relationship id fields replace the corresponding entity relationships.
     */
    @Override
    @Mapping(
            target = "tags",
            source = "tagIds",
            qualifiedByName = "AccountMapAccountTagSet"
    )
    Account update(@MappingTarget Account entity, AccountRequestDto request);

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
            qualifiedByName = "AccountMapAccountTagSet"
    )
    Account patch(@MappingTarget Account entity, AccountRequestDto request);

    /**
     * Maps an entity to the full response DTO.
     * Relationship fields are represented by generated reference DTO mappings where applicable.
     */
    @Override
    @Mapping(
            target = "logo",
            expression = "java(entity.getLogo())"
    )
    AccountResponseDto toResponse(Account entity);

    /**
     * Maps an entity to its lightweight reference DTO.
     */
    @Override
    AccountRef toRef(Account entity);

    /**
     * Extracts the identifier from a request DTO for upsert operations.
     * Generated record DTOs expose the id as a record component.
     */
    @Override
    default UUID getIdFromRequest(AccountRequestDto request) {
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

    @Named("AccountMapAccountTag")
    default AccountTag mapAccountTag(UUID id) {
        if (id == null) {
            return null;
        }
        AccountTag entity = new AccountTag();
        entity.setId(id);
        return entity;
    }

    @Named("AccountMapAccountTagSet")
    default Set<AccountTag> mapAccountTagSet(Set<UUID> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream().map(this::mapAccountTag).collect(Collectors.toSet());
    }

    AccountListResponseDto toListResponse(Account entity);

    AccountDetailResponseDto toDetailResponse(Account entity);
}

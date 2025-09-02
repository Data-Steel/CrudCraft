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

package nl.datasteel.crudcraft.sample.user.mapper;

import java.util.UUID;
import nl.datasteel.crudcraft.runtime.exception.MapperException;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import nl.datasteel.crudcraft.sample.branch.Branch;
import nl.datasteel.crudcraft.sample.branch.dto.ref.BranchRef;
import nl.datasteel.crudcraft.sample.tenant.Tenant;
import nl.datasteel.crudcraft.sample.tenant.dto.ref.TenantRef;
import nl.datasteel.crudcraft.sample.user.User;
import nl.datasteel.crudcraft.sample.user.dto.ref.UserRef;
import nl.datasteel.crudcraft.sample.user.dto.request.UserRequestDto;
import nl.datasteel.crudcraft.sample.user.dto.response.UserResponseDto;
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
 * Generated Mapper layer stub for User.
 * @CrudCraft:generated
 * @CrudCraft:editable
 *
 * This Mapper stub extends CrudCraft's base implementation. Override methods to customise behaviour.
 *
 * You are allowed to modify this file. It extends CrudCraft's abstract base (UserMapperBase)
 * which already implements full CRUD logic.
 *
 * This file was generated only once. CrudCraft will not overwrite it in future
 * builds. If you delete it, it will be regenerated.
 *
 * Features provided by CrudCraft:
 * - Standard CRUD workflow already implemented
 * - DTO mapping and repository calls wired up
 *
 * Generation context:
 * - Source model: User
 * - Package: nl.datasteel.crudcraft.sample.user.mapper
 * - Generator: MapperGenerator
 * - Generation time: 2025-09-02T09:10:33.9334971+02:00
 * - CrudCraft version: 0.1.0
 *
 * Recommendations:
 * - You may customize method behavior, add validation, or extend with additional endpoints.
 * - Signature changes are allowed, but may desync from service or mapper layer—proceed with care.
 * - Do not manually copy or paste other CrudCraft stubs into this class.
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
public interface UserMapper extends EntityMapper<User, UserRequestDto, UserResponseDto, UserRef, UUID> {
    @Override
    @Mapping(
            target = "tenant",
            source = "tenantId",
            qualifiedByName = "UserMapTenant"
    )
    @Mapping(
            target = "branch",
            source = "branchId",
            qualifiedByName = "UserMapBranch"
    )
    User fromRequest(UserRequestDto request);

    @Override
    @Mapping(
            target = "tenant",
            source = "tenantId",
            qualifiedByName = "UserMapTenant"
    )
    @Mapping(
            target = "branch",
            source = "branchId",
            qualifiedByName = "UserMapBranch"
    )
    User update(@MappingTarget User entity, UserRequestDto request);

    @Override
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(
            target = "tenant",
            source = "tenantId",
            qualifiedByName = "UserMapTenant"
    )
    @Mapping(
            target = "branch",
            source = "branchId",
            qualifiedByName = "UserMapBranch"
    )
    User patch(@MappingTarget User entity, UserRequestDto request);

    @Override
    @Mapping(
            target = "tenant",
            qualifiedByName = "UserToTenantRef"
    )
    @Mapping(
            target = "branch",
            qualifiedByName = "UserToBranchRef"
    )
    UserResponseDto toResponse(User entity);

    @Override
    UserRef toRef(User entity);

    @Override
    default UUID getIdFromRequest(UserRequestDto request) {
        try {
            var wrapper = new BeanWrapperImpl(request);
            Object idVal = wrapper.getPropertyValue("id");
            return (UUID) idVal;
        } catch (Exception e) {
            throw new MapperException("Failed to read 'id' property from request DTO: " + request.getClass(), e);
        }
    }

    @Named("UserToTenantRef")
    TenantRef toTenantRef(Tenant tenant);

    @Named("UserToBranchRef")
    BranchRef toBranchRef(Branch branch);

    @Named("UserMapTenant")
    default Tenant mapTenant(UUID id) {
        if (id == null) {
            return null;
        }
        Tenant entity = new Tenant();
        entity.setId(id);
        return entity;
    }

    @Named("UserMapBranch")
    default Branch mapBranch(UUID id) {
        if (id == null) {
            return null;
        }
        Branch entity = new Branch();
        entity.setId(id);
        return entity;
    }
}

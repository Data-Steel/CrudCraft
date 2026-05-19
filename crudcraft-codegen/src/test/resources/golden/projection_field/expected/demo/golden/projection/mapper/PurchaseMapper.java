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
package demo.golden.projection.mapper;

import demo.golden.projection.Customer;
import demo.golden.projection.Purchase;
import demo.golden.projection.dto.ref.CustomerRef;
import demo.golden.projection.dto.ref.PurchaseRef;
import demo.golden.projection.dto.request.PurchaseRequestDto;
import demo.golden.projection.dto.response.PurchaseResponseDto;
import java.util.UUID;
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
 * Generated model file for Purchase; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: Purchase
 * - Package: demo.golden.projection.mapper
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
public interface PurchaseMapper extends EntityMapper<Purchase, PurchaseRequestDto, PurchaseResponseDto, PurchaseRef, UUID> {
    /**
     * Maps a create or upsert request DTO into a new entity instance.
     * Nested relationship identifiers are resolved through generated @Named helper methods.
     */
    @Override
    @Mapping(
            target = "customer",
            source = "customerId",
            qualifiedByName = "PurchaseMapCustomer"
    )
    Purchase fromRequest(PurchaseRequestDto request);

    /**
     * Applies a full update request DTO to an existing entity.
     * Relationship id fields replace the corresponding entity relationships.
     */
    @Override
    @Mapping(
            target = "customer",
            source = "customerId",
            qualifiedByName = "PurchaseMapCustomer"
    )
    Purchase update(@MappingTarget Purchase entity, PurchaseRequestDto request);

    /**
     * Applies patch semantics to an existing entity.
     * Null request properties are ignored by MapStruct so existing entity values remain unchanged.
     */
    @Override
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(
            target = "customer",
            source = "customerId",
            qualifiedByName = "PurchaseMapCustomer"
    )
    Purchase patch(@MappingTarget Purchase entity, PurchaseRequestDto request);

    /**
     * Maps an entity to the full response DTO.
     * Relationship fields are represented by generated reference DTO mappings where applicable.
     */
    @Override
    @Mapping(
            target = "customer",
            qualifiedByName = "PurchaseToCustomerRef"
    )
    PurchaseResponseDto toResponse(Purchase entity);

    /**
     * Maps an entity to its lightweight reference DTO.
     */
    @Override
    PurchaseRef toRef(Purchase entity);

    /**
     * Extracts the identifier from a request DTO for upsert operations.
     * Generated record DTOs expose the id as a record component.
     */
    @Override
    default UUID getIdFromRequest(PurchaseRequestDto request) {
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

    @Named("PurchaseToCustomerRef")
    CustomerRef toCustomerRef(Customer customer);

    @Named("PurchaseMapCustomer")
    default Customer mapCustomer(UUID id) {
        if (id == null) {
            return null;
        }
        Customer entity = new Customer();
        entity.setId(id);
        return entity;
    }
}

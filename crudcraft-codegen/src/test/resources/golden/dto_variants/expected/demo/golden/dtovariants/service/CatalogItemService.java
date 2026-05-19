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
package demo.golden.dtovariants.service;

import demo.golden.dtovariants.CatalogItem;
import demo.golden.dtovariants.dto.ref.CatalogItemRef;
import demo.golden.dtovariants.dto.request.CatalogItemRequestDto;
import demo.golden.dtovariants.dto.response.CatalogItemResponseDto;
import demo.golden.dtovariants.mapper.CatalogItemMapper;
import demo.golden.dtovariants.meta.CatalogItemRelationshipMeta;
import demo.golden.dtovariants.repository.CatalogItemRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for CatalogItem; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: CatalogItem
 * - Package: demo.golden.dtovariants.service
 * - Generator: ServiceGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * To make changes, edit the entity model class and rebuild the project.
 * Do not edit or rename this file manually.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@Service
public class CatalogItemService extends AbstractCrudService<CatalogItem, CatalogItemRequestDto, CatalogItemResponseDto, CatalogItemRef, UUID> {
    public CatalogItemService(CatalogItemRepository repository, CatalogItemMapper mapper) {
        super(repository, mapper, CatalogItem.class, CatalogItemResponseDto.class, CatalogItemRef.class);
    }

    @Override
    protected void postSave(CatalogItem entity) {
        CatalogItemRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(CatalogItem entity) {
        CatalogItemRelationshipMeta.clear(entity);
    }
}

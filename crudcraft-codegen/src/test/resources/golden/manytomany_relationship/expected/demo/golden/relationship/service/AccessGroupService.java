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
package demo.golden.relationship.service;

import demo.golden.relationship.AccessGroup;
import demo.golden.relationship.dto.ref.AccessGroupRef;
import demo.golden.relationship.dto.request.AccessGroupRequestDto;
import demo.golden.relationship.dto.response.AccessGroupResponseDto;
import demo.golden.relationship.mapper.AccessGroupMapper;
import demo.golden.relationship.meta.AccessGroupRelationshipMeta;
import demo.golden.relationship.repository.AccessGroupRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for AccessGroup; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: AccessGroup
 * - Package: demo.golden.relationship.service
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
public class AccessGroupService extends AbstractCrudService<AccessGroup, AccessGroupRequestDto, AccessGroupResponseDto, AccessGroupRef, UUID> {
    public AccessGroupService(AccessGroupRepository repository, AccessGroupMapper mapper) {
        super(repository, mapper, AccessGroup.class, AccessGroupResponseDto.class, AccessGroupRef.class);
    }

    @Override
    protected void postSave(AccessGroup entity) {
        AccessGroupRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(AccessGroup entity) {
        AccessGroupRelationshipMeta.clear(entity);
    }
}

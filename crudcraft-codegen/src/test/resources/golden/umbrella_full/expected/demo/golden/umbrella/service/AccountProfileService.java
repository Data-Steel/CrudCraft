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
package demo.golden.umbrella.service;

import demo.golden.umbrella.AccountProfile;
import demo.golden.umbrella.dto.ref.AccountProfileRef;
import demo.golden.umbrella.dto.request.AccountProfileRequestDto;
import demo.golden.umbrella.dto.response.AccountProfileResponseDto;
import demo.golden.umbrella.mapper.AccountProfileMapper;
import demo.golden.umbrella.meta.AccountProfileRelationshipMeta;
import demo.golden.umbrella.repository.AccountProfileRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for AccountProfile; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: AccountProfile
 * - Package: demo.golden.umbrella.service
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
public class AccountProfileService extends AbstractCrudService<AccountProfile, AccountProfileRequestDto, AccountProfileResponseDto, AccountProfileRef, UUID> {
    public AccountProfileService(AccountProfileRepository repository, AccountProfileMapper mapper) {
        super(repository, mapper, AccountProfile.class, AccountProfileResponseDto.class, AccountProfileRef.class);
    }

    @Override
    protected void postSave(AccountProfile entity) {
        AccountProfileRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(AccountProfile entity) {
        AccountProfileRelationshipMeta.clear(entity);
    }
}

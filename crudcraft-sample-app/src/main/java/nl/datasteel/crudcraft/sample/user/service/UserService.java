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

package nl.datasteel.crudcraft.sample.user.service;

import java.util.List;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import nl.datasteel.crudcraft.runtime.util.RelationshipUtils;
import nl.datasteel.crudcraft.sample.security.OwnTenantRowSecurityHandler;
import nl.datasteel.crudcraft.sample.user.User;
import nl.datasteel.crudcraft.sample.user.dto.ref.UserRef;
import nl.datasteel.crudcraft.sample.user.dto.request.UserRequestDto;
import nl.datasteel.crudcraft.sample.user.dto.response.UserResponseDto;
import nl.datasteel.crudcraft.sample.user.mapper.UserMapper;
import nl.datasteel.crudcraft.sample.user.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Generated Service layer stub for User.
 * @CrudCraft:generated
 * @CrudCraft:editable
 *
 * This Service stub extends CrudCraft's base implementation. Override methods to customise behaviour.
 *
 * You are allowed to modify this file. It extends CrudCraft's abstract base (UserServiceBase)
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
 * - Package: nl.datasteel.crudcraft.sample.user.service
 * - Generator: ServiceGenerator
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
@Service
public class UserService extends AbstractCrudService<User, UserRequestDto, UserResponseDto, UserRef, UUID> {
    private final List<RowSecurityHandler<?>> rowSecurityHandlers;

    public UserService(UserRepository repository, UserMapper mapper,
            OwnTenantRowSecurityHandler rowSecurity0) {
        super(repository, mapper, User.class, UserResponseDto.class, UserRef.class);
        this.rowSecurityHandlers = List.<RowSecurityHandler<?>>of(rowSecurity0);
    }

    @Override
    protected void postSave(User entity) {
        RelationshipUtils.fixBidirectional(entity);
    }

    @Override
    protected void preDelete(User entity) {
        RelationshipUtils.clearBidirectional(entity);
    }

    @Override
    protected List<RowSecurityHandler<?>> rowSecurityHandlers() {
        return rowSecurityHandlers;
    }
}

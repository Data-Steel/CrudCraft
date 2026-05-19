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

import demo.golden.umbrella.Account;
import demo.golden.umbrella.dto.ref.AccountRef;
import demo.golden.umbrella.dto.request.AccountRequestDto;
import demo.golden.umbrella.dto.response.AccountResponseDto;
import demo.golden.umbrella.mapper.AccountMapper;
import demo.golden.umbrella.meta.AccountRelationshipMeta;
import demo.golden.umbrella.repository.AccountRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.security.row.ClaimScopedRowSecurityHandler;
import nl.datasteel.crudcraft.runtime.security.row.RowSecurityRuntimeExtension;
import nl.datasteel.crudcraft.runtime.security.scope.PrincipalScopeAccessor;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
import org.springframework.stereotype.Service;

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
public class AccountService extends AbstractCrudService<Account, AccountRequestDto, AccountResponseDto, AccountRef, UUID> {
    private final List<CrudRuntimeExtension<Account, AccountRequestDto>> runtimeExtensions;

    public AccountService(AccountRepository repository, AccountMapper mapper,
            PrincipalScopeAccessor principalScopeAccessor) {
        super(repository, mapper, Account.class, AccountResponseDto.class, AccountRef.class);
        List<RowSecurityHandler<Account>> rowSecurityHandlerList = new ArrayList<>();
        rowSecurityHandlerList.add(new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant_id", principalScopeAccessor));
        rowSecurityHandlerList.add(new ClaimScopedRowSecurityHandler<>("owner", "ownerId", "sub", principalScopeAccessor));
        this.runtimeExtensions = List.of(new RowSecurityRuntimeExtension<>(rowSecurityHandlerList));
    }

    @Override
    protected void postSave(Account entity) {
        AccountRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(Account entity) {
        AccountRelationshipMeta.clear(entity);
    }

    @Override
    protected List<CrudRuntimeExtension<Account, AccountRequestDto>> runtimeExtensions() {
        return runtimeExtensions;
    }
}

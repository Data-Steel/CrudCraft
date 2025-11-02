/*
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.sample.transaction.service;

import java.util.List;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import nl.datasteel.crudcraft.runtime.util.RelationshipUtils;
import nl.datasteel.crudcraft.sample.security.OwnAccountRowSecurityHandler;
import nl.datasteel.crudcraft.sample.security.OwnTenantRowSecurityHandler;
import nl.datasteel.crudcraft.sample.transaction.Transaction;
import nl.datasteel.crudcraft.sample.transaction.dto.ref.TransactionRef;
import nl.datasteel.crudcraft.sample.transaction.dto.request.TransactionRequestDto;
import nl.datasteel.crudcraft.sample.transaction.dto.response.TransactionResponseDto;
import nl.datasteel.crudcraft.sample.transaction.mapper.TransactionMapper;
import nl.datasteel.crudcraft.sample.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

/**
 * Generated Service layer stub for Transaction.
 * @CrudCraft:generated
 * @CrudCraft:editable
 *
 * This Service stub extends CrudCraft's base implementation. Override methods to customise behaviour.
 *
 * You are allowed to modify this file. It extends CrudCraft's abstract base (TransactionServiceBase)
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
 * - Source model: Transaction
 * - Package: nl.datasteel.crudcraft.sample.transaction.service
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
public class TransactionService extends AbstractCrudService<Transaction, TransactionRequestDto, TransactionResponseDto, TransactionRef, UUID> {
    private final List<RowSecurityHandler<?>> rowSecurityHandlers;

    public TransactionService(TransactionRepository repository, TransactionMapper mapper,
            OwnTenantRowSecurityHandler rowSecurity0, OwnAccountRowSecurityHandler rowSecurity1) {
        super(repository, mapper, Transaction.class, TransactionResponseDto.class, TransactionRef.class);
        this.rowSecurityHandlers = List.<RowSecurityHandler<?>>of(rowSecurity0, rowSecurity1);
    }

    @Override
    protected void postSave(Transaction entity) {
        RelationshipUtils.fixBidirectional(entity);
    }

    @Override
    protected void preDelete(Transaction entity) {
        RelationshipUtils.clearBidirectional(entity);
    }

    @Override
    protected List<RowSecurityHandler<?>> rowSecurityHandlers() {
        return rowSecurityHandlers;
    }
}

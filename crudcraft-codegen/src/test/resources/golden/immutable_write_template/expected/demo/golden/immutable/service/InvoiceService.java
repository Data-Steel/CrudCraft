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
package demo.golden.immutable.service;

import demo.golden.immutable.Invoice;
import demo.golden.immutable.dto.ref.InvoiceRef;
import demo.golden.immutable.dto.request.InvoiceRequestDto;
import demo.golden.immutable.dto.response.InvoiceResponseDto;
import demo.golden.immutable.mapper.InvoiceMapper;
import demo.golden.immutable.meta.InvoiceRelationshipMeta;
import demo.golden.immutable.repository.InvoiceRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for Invoice; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: Invoice
 * - Package: demo.golden.immutable.service
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
public class InvoiceService extends AbstractCrudService<Invoice, InvoiceRequestDto, InvoiceResponseDto, InvoiceRef, UUID> {
    public InvoiceService(InvoiceRepository repository, InvoiceMapper mapper) {
        super(repository, mapper, Invoice.class, InvoiceResponseDto.class, InvoiceRef.class);
    }

    @Override
    protected void postSave(Invoice entity) {
        InvoiceRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(Invoice entity) {
        InvoiceRelationshipMeta.clear(entity);
    }
}

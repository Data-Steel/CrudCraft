/**
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
package nl.datasteel.crudcraft.sample.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.security.RowSecurity;
import nl.datasteel.crudcraft.runtime.extensions.AuditableExtension;
import nl.datasteel.crudcraft.sample.security.OwnTenantRowSecurityHandler;

/**
 * Tenant represents an isolated client in the platform, illustrating how
 * CrudCraft can model multi-tenant data with auditing support.
 */
@CrudCrafted(editable = false)
@RowSecurity(handlers = OwnTenantRowSecurityHandler.class)
@Entity
@Table(name = "tenants")
public class Tenant {

    /**
     * Surrogate key for the tenant. Not writable to API clients.
     */
    @Dto
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Human-readable tenant name. Required and unique across all tenants and
     * writable via the generated endpoints.
     */
    @Dto
    @Request
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Auditing fields automatically managed by CrudCraft.
     */
    @Embedded
    private AuditableExtension audit = new AuditableExtension();

    /**
     * Returns the unique identifier of the tenant.
     *
     * @return the tenant id
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the tenant.
     *
     * @param id the tenant id
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Returns the name of the tenant.
     *
     * @return tenant name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the name of the tenant.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the auditing extension.
     *
     * @return audit extension
     */
    public AuditableExtension getAudit() {
        return audit;
    }

    /**
     * Updates the auditing extension.
     *
     * @param audit the new auditing extension
     */
    public void setAudit(AuditableExtension audit) {
        this.audit = audit;
    }
}

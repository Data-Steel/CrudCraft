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
package nl.datasteel.crudcraft.sample.branch;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.fields.Searchable;
import nl.datasteel.crudcraft.annotations.security.RowSecurity;
import nl.datasteel.crudcraft.runtime.extensions.AuditableExtension;
import nl.datasteel.crudcraft.sample.tenant.Tenant;
import nl.datasteel.crudcraft.sample.security.OwnTenantRowSecurityHandler;

/**
 * Bank branch belonging to a tenant. Serves as a simple example of an entity
 * with searchable fields and auditing enabled.
 */
@CrudCrafted(template = CrudTemplate.FULL, secure = true)
@RowSecurity(handlers = OwnTenantRowSecurityHandler.class)
@Entity
@Table(name = "branches")
public class Branch {

    /**
     * Unique identifier of the branch. Generated automatically and exposed in
     * responses but not writable by clients.
     */
    @Dto
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Tenant owning this branch. The relation allows CrudCraft to include the
     * tenant's identifier and enforce tenant-specific security.
     */
    @Dto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /**
     * Short branch code. Marked {@link Searchable} so clients can filter by
     * code via query parameters.
     */
    @Dto(ref = true)
    @Request
    @Searchable
    @Column(nullable = false, length = 16)
    private String code;

    /**
     * Human-readable branch name, writable through the API.
     */
    @Dto(ref = true)
    @Request
    @Column(nullable = false)
    private String name;

    /**
     * Auditing metadata automatically managed by CrudCraft.
     */
    @Embedded
    private AuditableExtension audit = new AuditableExtension();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AuditableExtension getAudit() {
        return audit;
    }

    public void setAudit(AuditableExtension audit) {
        this.audit = audit;
    }

}

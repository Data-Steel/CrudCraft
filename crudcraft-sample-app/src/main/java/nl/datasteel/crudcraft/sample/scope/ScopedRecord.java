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

package nl.datasteel.crudcraft.sample.scope;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Searchable;
import nl.datasteel.crudcraft.annotations.security.ClientScoped;
import nl.datasteel.crudcraft.annotations.security.CrudSecurity;
import nl.datasteel.crudcraft.annotations.security.OwnedBy;
import nl.datasteel.crudcraft.annotations.security.TenantScoped;


/**
 * Dedicated security showcase entity. Demonstrates includeEndpoints + secure endpoints +
 * tenant/client/owner row scopes.
 */
@Entity
@Table(name = "scope_records")
@CrudCrafted(includeEndpoints = CrudEndpoint.EXPORT, secure = true)
@CrudSecurity(readRoles = {"USER"})
@TenantScoped(field = "tenantId", claim = "tenant_id")
@ClientScoped(field = "clientId", claim = "client_id")
@OwnedBy(field = "ownerId", claim = "sub")
public class ScopedRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Dto
    @Searchable
    private UUID id;

    @Column(nullable = false, length = 120)
    @Dto
    @Searchable
    private String name;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "client_id", nullable = false, length = 64)
    private String clientId;

    @Column(name = "owner_id", nullable = false, length = 64)
    private String ownerId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}

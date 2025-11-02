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
package nl.datasteel.crudcraft.sample.user;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.fields.Searchable;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.policy.AdminOnlySecurityPolicy;
import nl.datasteel.crudcraft.annotations.security.RowSecurity;
import nl.datasteel.crudcraft.runtime.extensions.AuditableExtension;
import nl.datasteel.crudcraft.sample.branch.Branch;
import nl.datasteel.crudcraft.sample.enums.RoleType;
import nl.datasteel.crudcraft.sample.tenant.Tenant;
import nl.datasteel.crudcraft.sample.security.OwnTenantRowSecurityHandler;

/**
 * Application user with roles.
 * Demonstrates field-level security and DTO generation.
 * Generated DTOs can be built fluently:
 * {@code UserRequestDto.builder().username("alice").build();}
 */
@CrudCrafted(editable = true, securityPolicy = AdminOnlySecurityPolicy.class)
@RowSecurity(handlers = OwnTenantRowSecurityHandler.class)
@Entity
@Table(name = "app_users")
public class User {

    /**
     * Identifier exposed through the generated DTO. Because it is marked as
     * {@link jakarta.persistence.Id} and not included in {@link Request},
     * clients cannot overwrite it.
     */
    @Dto
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Owning tenant reference. Security policies can use this relationship to
     * scope queries so users only see data from their own tenant.
     */
    @Dto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /**
     * Optional branch link. Included in the DTO so API clients know in which
     * branch the user operates.
     */
    @Dto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    /**
     * Login name for the user. Marked as {@link Request} to allow creation and
     * update via the REST API and {@link Searchable} so queries can filter by
     * username.
     */
    @Dto
    @Request
    @Searchable
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * Password hash is write-only, so while it is in the dto (so it gets projected, and we can
     * use it to validate the hash), it will always be set to {@code null} by
     * {@link nl.datasteel.crudcraft.runtime.security.FieldSecurityUtil#filterRead(Object)}
     * before responses are sent, keeping the value hidden from API consumers.
     * You can try this by performing a GET request on the /users endpoint, while logged in
     * as Alice or Carol (ADMIN)
     */
    @Dto
    @FieldSecurity(readRoles = {})
    @Request
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Dto
    @Request
    @Searchable
    private Set<Instant> test;

    /**
     * Roles assigned to the user. CrudCraft maps this element collection to a
     * separate table and ensures the set is copied to prevent external
     * modification of the entity's internal state.
     */
    @Dto
    @Request
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<RoleType> roles = new HashSet<>();

    /**
     * Embedded audit information (created/updated timestamps and actor). The
     * {@link AuditableExtension} is filled automatically by CrudCraft hooks.
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

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Set<RoleType> getRoles() {
        return roles == null ? Set.of() : Set.copyOf(roles);
    }

    public void setRoles(Set<RoleType> roles) {
        this.roles = roles == null ? new HashSet<>() : new HashSet<>(roles);
    }

    public AuditableExtension getAudit() {
        return audit;
    }

    public void setAudit(AuditableExtension audit) {
        this.audit = audit;
    }
}

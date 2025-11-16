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

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.fields.Searchable;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.policy.AdminOnlySecurityPolicy;
import nl.datasteel.crudcraft.runtime.extensions.AuditableExtension;
import nl.datasteel.crudcraft.sample.security.RoleType;

/**
 * Application user with roles.
 * Demonstrates field-level security and AdminOnly security policy.
 */
@CrudCrafted(editable = true, securityPolicy = AdminOnlySecurityPolicy.class)
@Entity
@Table(name = "app_users")
public class User {

    @Dto
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Dto
    @Request
    @Searchable
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * Password hash is write-only. It can be set via the API but will never
     * be returned in responses due to {@link FieldSecurity} with empty readRoles.
     */
    @Dto
    @FieldSecurity(readRoles = {})
    @Request
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Dto
    @Request
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<RoleType> roles = new HashSet<>();

    @Embedded
    private AuditableExtension audit = new AuditableExtension();

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

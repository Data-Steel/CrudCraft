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

package nl.datasteel.crudcraft.sample.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.security.RowSecurity;
import nl.datasteel.crudcraft.sample.security.OwnAccountRowSecurityHandler;

/**
 * Limits associated with a bank account. Illustrates one-to-one relations
 * and read-only endpoints using {@link nl.datasteel.crudcraft.annotations.classes.CrudCrafted}.
 */
@CrudCrafted(editable = false)
@RowSecurity(handlers = OwnAccountRowSecurityHandler.class)
@Entity
@Table(name = "account_limits")
public class AccountLimits {

    /**
     * Surrogate key for this limits record. Not writable because the entity is
     * read-only to clients.
     */
    @Dto
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Owning account. CrudCraft links the one-to-one relationship and exposes
     * the account identifier in DTOs.
     */
    @Dto
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    /**
     * Daily withdrawal limit. {@link Request} allows administrators to update
     * it via the API.
     */
    @Dto
    @Request
    @Column(name = "daily_limit", precision = 19, scale = 4)
    private BigDecimal dailyLimit;

    /**
     * Monthly withdrawal limit, also writable through CrudCraft's generated
     * endpoints.
     */
    @Dto
    @Request
    @Column(name = "monthly_limit", precision = 19, scale = 4)
    private BigDecimal monthlyLimit;

    /**
     * Flag indicating whether overdrafts are allowed. Exposed and writable so
     * clients can toggle this behaviour.
     */
    @Dto
    @Request
    @Column(name = "overdraft_enabled")
    private boolean overdraftEnabled;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public boolean isOverdraftEnabled() {
        return overdraftEnabled;
    }

    public void setOverdraftEnabled(boolean overdraftEnabled) {
        this.overdraftEnabled = overdraftEnabled;
    }
}

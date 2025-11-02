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
package nl.datasteel.crudcraft.sample.transaction;

import jakarta.persistence.Column;
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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.RowSecurity;
import nl.datasteel.crudcraft.runtime.extensions.AuditableExtension;
import nl.datasteel.crudcraft.sample.account.Account;
import nl.datasteel.crudcraft.sample.enums.TransactionDirection;
import nl.datasteel.crudcraft.sample.enums.TransactionStatus;
import nl.datasteel.crudcraft.sample.security.TransactionSecurityPolicy;
import nl.datasteel.crudcraft.sample.security.OwnAccountRowSecurityHandler;
import nl.datasteel.crudcraft.sample.security.OwnTenantRowSecurityHandler;
import nl.datasteel.crudcraft.sample.tenant.Tenant;
import nl.datasteel.crudcraft.sample.validator.PositiveAmount;
import nl.datasteel.crudcraft.sample.validator.SameCurrencyAsAccount;

/**
 * Booking line on an account. Demonstrates field-level security, validation
 * annotations, and a custom {@link nl.datasteel.crudcraft.sample.security.TransactionSecurityPolicy}.
 * Generated DTOs support a fluent builder, e.g.:
 * {@code TransactionRequestDto.builder().amount(BigDecimal.ONE).build();}
 */
@SameCurrencyAsAccount
@CrudCrafted(editable = true, omitEndpoints = {CrudEndpoint.DELETE, CrudEndpoint.BULK_DELETE},
        securityPolicy = TransactionSecurityPolicy.class)
@RowSecurity(handlers = {OwnTenantRowSecurityHandler.class, OwnAccountRowSecurityHandler.class})
@Entity
@Table(name = "transactions")
public class Transaction {

    /**
     * Unique identifier for the transaction, generated on insert.
     */
    @Dto
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Tenant to which the transaction belongs; enables multi-tenant scoping.
     */
    @Dto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /**
     * Account affected by this booking line.
     */
    @Dto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * Moment the transaction was registered.
     */
    @Dto
    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    /**
     * Indicates debit or credit. Stored as an enum for type safety.
     */
    @Dto
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionDirection direction;

    /**
     * Monetary amount validated to be positive via {@link PositiveAmount}.
     */
    @Dto
    @Column(nullable = false, precision = 19, scale = 4)
    @PositiveAmount
    private BigDecimal amount;

    /**
     * ISO currency code. The {@link SameCurrencyAsAccount} class-level
     * validator ensures it matches the account's currency.
     */
    @Dto
    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    /**
     * Identifier linking two sides of a transfer, if applicable.
     */
    @Dto
    @Column(name = "transfer_id")
    private UUID transferId;

    /**
     * Reference to the counterpart account for internal transfers.
     */
    @Dto
    @Column(name = "opposite_account_id")
    private UUID oppositeAccountId;

    /**
     * Counterparty IBAN. Readable only for back-office roles via
     * {@link FieldSecurity} and otherwise filtered to {@code null}.
     */
    @FieldSecurity(readRoles = {"ADMIN","AUDITOR","TELLER"})
    @Dto
    private String counterpartyIban;

    /**
     * Name of the counterparty shown on statements.
     */
    @Dto
    private String counterpartyName;

    /**
     * Client-provided reference for reconciliation.
     */
    @Dto
    @Column(name = "client_reference", nullable = false)
    private String clientReference;

    /**
     * Processing state of the transaction.
     */
    @Dto
    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.BOOKED;

    /**
     * Optional free-text note.
     */
    @Dto
    private String note;

    /**
     * Standard auditing metadata maintained by CrudCraft.
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionDirection getDirection() {
        return direction;
    }

    public void setDirection(TransactionDirection direction) {
        this.direction = direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public void setTransferId(UUID transferId) {
        this.transferId = transferId;
    }

    public UUID getOppositeAccountId() {
        return oppositeAccountId;
    }

    public void setOppositeAccountId(UUID oppositeAccountId) {
        this.oppositeAccountId = oppositeAccountId;
    }

    public String getCounterpartyIban() {
        return counterpartyIban;
    }

    public void setCounterpartyIban(String counterpartyIban) {
        this.counterpartyIban = counterpartyIban;
    }

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public void setCounterpartyName(String counterpartyName) {
        this.counterpartyName = counterpartyName;
    }

    public String getClientReference() {
        return clientReference;
    }

    public void setClientReference(String clientReference) {
        this.clientReference = clientReference;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public AuditableExtension getAudit() {
        return audit;
    }

    public void setAudit(AuditableExtension audit) {
        this.audit = audit;
    }
}

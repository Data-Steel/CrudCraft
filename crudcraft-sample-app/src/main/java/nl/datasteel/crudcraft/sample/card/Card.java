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
package nl.datasteel.crudcraft.sample.card;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.RowSecurity;
import nl.datasteel.crudcraft.runtime.extensions.AuditableExtension;
import nl.datasteel.crudcraft.sample.account.Account;
import nl.datasteel.crudcraft.sample.enums.CardNetwork;
import nl.datasteel.crudcraft.sample.enums.CardStatus;
import nl.datasteel.crudcraft.sample.security.OwnAccountRowSecurityHandler;

/**
 * Payment card linked to an account. Highlights field-level security on
 * sensitive data like the PAN token and composition with {@link CardPin}.
 */
@CrudCrafted(editable = false)
@RowSecurity(handlers = OwnAccountRowSecurityHandler.class)
@Entity
@Table(name = "cards")
public class Card {

    /**
     * Internal identifier for the card. Exposed in DTOs but not writable.
     */
    @Dto
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Account to which this card is linked. CrudCraft uses the relation to
     * include the account ID in responses.
     */
    @Dto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * Current status of the card (ACTIVE, BLOCKED...). Stored as an enum for
     * clarity.
     */
    @Dto
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status = CardStatus.ACTIVE;

    /**
     * Payment network of the card such as VISA or MASTERCARD.
     */
    @Dto
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardNetwork network;

    /**
     * Tokenised Primary Account Number. {@link FieldSecurity} with an empty
     * role set makes it write-only so the token never leaves the server.
     */
    @FieldSecurity(readRoles = {})
    @Request
    @Column(name = "pan_token", nullable = false)
    private String panToken;

    /**
     * Last four digits of the PAN, safe to expose for user recognition.
     */
    @Dto
    @Column(name = "pan_last4", length = 4)
    private String panLast4;

    /**
     * Expiry month of the card; writable to allow card renewals.
     */
    @Dto
    @Request
    @Column(name = "expiry_month")
    private int expiryMonth;

    /**
     * Expiry year accompanying the month above.
     */
    @Dto
    @Request
    @Column(name = "expiry_year")
    private int expiryYear;

    /**
     * Auditing fields filled by CrudCraft to track creation and updates.
     */
    @Embedded
    private AuditableExtension audit = new AuditableExtension();

    /**
     * Associated PIN information stored in a separate secured entity.
     */
    @Dto
    @OneToOne(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private CardPin pin;

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

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public CardNetwork getNetwork() {
        return network;
    }

    public void setNetwork(CardNetwork network) {
        this.network = network;
    }

    public String getPanToken() {
        return panToken;
    }

    public void setPanToken(String panToken) {
        this.panToken = panToken;
    }

    public String getPanLast4() {
        return panLast4;
    }

    public void setPanLast4(String panLast4) {
        this.panLast4 = panLast4;
    }

    public int getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(int expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public int getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(int expiryYear) {
        this.expiryYear = expiryYear;
    }

    public AuditableExtension getAudit() {
        return audit;
    }

    public void setAudit(AuditableExtension audit) {
        this.audit = audit;
    }

    public CardPin getPin() {
        return pin;
    }

    public void setPin(CardPin pin) {
        this.pin = pin;
    }
}

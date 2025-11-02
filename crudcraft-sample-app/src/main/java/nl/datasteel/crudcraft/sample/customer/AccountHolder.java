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

package nl.datasteel.crudcraft.sample.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.security.RowSecurity;
import nl.datasteel.crudcraft.sample.account.Account;
import nl.datasteel.crudcraft.sample.enums.HolderType;
import nl.datasteel.crudcraft.sample.security.OwnAccountRowSecurityHandler;
import nl.datasteel.crudcraft.sample.security.OwnTenantRowSecurityHandler;

/**
 * Join entity linking customers to accounts. Highlights composite keys and
 * many-to-many relationships in CrudCraft.
 */
@CrudCrafted(editable = false)
@RowSecurity(handlers = {OwnTenantRowSecurityHandler.class, OwnAccountRowSecurityHandler.class})
@Entity
@Table(name = "account_holders")
@IdClass(AccountHolder.Pk.class)
public class AccountHolder {

    /**
     * Reference to the account. Part of the composite key and included in the
     * DTO so clients know which account the holder relates to.
     */
    @Dto
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * Reference to the customer also forming part of the key.
     */
    @Dto(ref = true)
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Type of relationship (OWNER, SIGNATORY...). Marked {@link Request} so the
     * link can be adjusted through the API.
     */
    @Dto(ref = true)
    @Request
    @Enumerated(EnumType.STRING)
    @Column(name = "holder_type", nullable = false)
    private HolderType holderType = HolderType.OWNER;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public HolderType getHolderType() {
        return holderType;
    }

    public void setHolderType(HolderType holderType) {
        this.holderType = holderType;
    }

    /** Composite key class identifying an AccountHolder link. */
    public static class Pk implements Serializable {
        /** Account identifier component of the key. */
        private UUID account;
        /** Customer identifier component of the key. */
        private UUID customer;

        public UUID getAccount() {
            return account;
        }

        public void setAccount(UUID account) {
            this.account = account;
        }

        public UUID getCustomer() {
            return customer;
        }

        public void setCustomer(UUID customer) {
            this.customer = customer;
        }
    }
}

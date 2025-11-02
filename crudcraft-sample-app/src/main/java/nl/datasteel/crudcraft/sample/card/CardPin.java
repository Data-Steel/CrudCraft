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
package nl.datasteel.crudcraft.sample.card;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;

/**
 * Holds hashed PIN data for a card. Demonstrates how CrudCraft can expose
 * a related entity while completely hiding sensitive fields using
 * {@link nl.datasteel.crudcraft.annotations.security.FieldSecurity}.
 */
@CrudCrafted(editable = false)
@Entity
@Table(name = "card_pins")
public class CardPin {

    /**
     * Primary key for the PIN record. Hidden from all readers to avoid
     * exposing internal identifiers.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @FieldSecurity(readRoles = {})
    private UUID id;

    /**
     * Back-reference to the owning card. Also hidden to keep the entire PIN
     * structure private.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @FieldSecurity(readRoles = {})
    @JoinColumn(name = "card_id", nullable = false, unique = true)
    private Card card;

    /**
     * Hashed PIN value supplied by the client. Write-only to prevent leaking
     * the hash.
     */
    @FieldSecurity(readRoles = {})
    @Request
    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    /**
     * Salt used to hash the PIN. Also write-only for security reasons.
     */
    @Request
    @Column(name = "pin_salt", nullable = false)
    @FieldSecurity(readRoles = {})
    private String pinSalt;

    /**
     * Version of the hashing algorithm, allowing upgrades over time.
     */
    @Request
    @FieldSecurity(readRoles = {})
    @Column(name = "pin_version")
    private int pinVersion;

    /**
     * Number of failed attempts. Keeping it hidden avoids revealing security
     * thresholds to clients.
     */
    @Request
    @FieldSecurity(readRoles = {})
    @Column(name = "try_count")
    private int tryCount;

    /**
     * If present, the time until which the card is blocked. Also read-protected
     * so only the server can inspect it.
     */
    @FieldSecurity(readRoles = {})
    @Request
    @Column(name = "blocked_until")
    private OffsetDateTime blockedUntil;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public String getPinSalt() {
        return pinSalt;
    }

    public void setPinSalt(String pinSalt) {
        this.pinSalt = pinSalt;
    }

    public int getPinVersion() {
        return pinVersion;
    }

    public void setPinVersion(int pinVersion) {
        this.pinVersion = pinVersion;
    }

    public int getTryCount() {
        return tryCount;
    }

    public void setTryCount(int tryCount) {
        this.tryCount = tryCount;
    }

    public OffsetDateTime getBlockedUntil() {
        return blockedUntil;
    }

    public void setBlockedUntil(OffsetDateTime blockedUntil) {
        this.blockedUntil = blockedUntil;
    }
}

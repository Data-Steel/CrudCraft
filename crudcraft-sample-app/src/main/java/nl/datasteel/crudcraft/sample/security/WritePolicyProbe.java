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

package nl.datasteel.crudcraft.sample.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;


/** Dedicated sample entity for exercising write-policy behavior through generated endpoints. */
@CrudCrafted
@Entity
@Table(name = "write_policy_probes")
public class WritePolicyProbe {

    @Dto(ref = true)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Dto
    @Request
    @Column(nullable = false)
    private String name;

    @Dto
    @Request
    @FieldSecurity(
            readRoles = {"ADMIN"},
            writeRoles = {"ADMIN"},
            writePolicy = WritePolicy.FAIL_ON_DENIED)
    @Column(name = "guarded_secret")
    private String guardedSecret;

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

    public String getGuardedSecret() {
        return guardedSecret;
    }

    public void setGuardedSecret(String guardedSecret) {
        this.guardedSecret = guardedSecret;
    }
}

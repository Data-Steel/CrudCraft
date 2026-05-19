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

package demo.golden.umbrella;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.fields.Searchable;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.OwnedBy;
import nl.datasteel.crudcraft.annotations.security.TenantScoped;

@Entity
@CrudCrafted(
        secure = true,
        includeEndpoints = {CrudEndpoint.EXPORT, CrudEndpoint.SEARCH})
@TenantScoped
@OwnedBy
public class Account {
    @Id @Dto(ref = true) private UUID id;
    @Dto({"List", "Detail"}) @Request @Searchable @NotBlank private String name;
    @Dto("Detail") @Request @Searchable private String tenantId;
    @Dto @Request private String ownerId;
    @Dto @Request @Enumerated private AccountType type;
    @Dto @Request @Searchable(operators = SearchOperator.AFTER) private Instant createdAt;
    @Dto @Request @FieldSecurity(readRoles = "ADMIN", writeRoles = "ADMIN") private String secret;
    @Dto @Request @OneToOne private AccountProfile profile;
    @Dto @Request @ManyToMany private Set<AccountTag> tags;
    @Dto @Request @Lob private byte[] logo;
}

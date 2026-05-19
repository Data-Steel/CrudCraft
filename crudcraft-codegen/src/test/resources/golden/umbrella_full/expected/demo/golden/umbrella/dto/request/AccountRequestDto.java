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
package demo.golden.umbrella.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import demo.golden.umbrella.AccountProfile;
import demo.golden.umbrella.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;

/**
 * Generated model file for Account; do not edit manually.
 * @CrudCraft:generated
 *
 * This DTO mirrors the entity fields and annotations exactly. Fields are nullable unless validation annotations such as @NotNull, @NotBlank, or @NotEmpty are present.
 *
 * Included elements:
 * - All entity fields one-to-one
 * - Validation annotations copied verbatim
 * - Nullness and range expectations are expressed through validation annotations and component Javadoc
 *
 * Generation context:
 * - Source model: Account
 * - Package: demo.golden.umbrella.dto.request
 * - Generator: DtoGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * To make changes, edit the entity model class and rebuild the project.
 * Do not edit or rename this file manually.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 *
 * <p>Nullness: request DTO instances for Account are non-null. Fields may be null unless Jakarta validation marks them required; PATCH treats null according to mapper patch semantics, while PUT supplies replacement values.
 *
 * @param name name - nullable unless copied validation annotations require a value or constrain its range.
 * @param tenantId tenantId - nullable unless copied validation annotations require a value or constrain its range.
 * @param ownerId ownerId - nullable unless copied validation annotations require a value or constrain its range.
 * @param type type - nullable unless copied validation annotations require a value or constrain its range.
 * @param createdAt createdAt - nullable unless copied validation annotations require a value or constrain its range.
 * @param secret secret - nullable unless copied validation annotations require a value or constrain its range.
 * @param profile profile - nullable unless copied validation annotations require a value or constrain its range.
 * @param tagIds tagIds - nullable unless copied validation annotations require a value or constrain its range.
 * @param logo logo - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Create/update DTO for Account"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AccountRequestDto(@Nullable @NotBlank String name, @Nullable String tenantId,
        @Nullable String ownerId, @Nullable AccountType type, @Nullable Instant createdAt,
        @Nullable @FieldSecurity(readRoles = {"ADMIN"}, writeRoles = {"ADMIN"}, writePolicy = WritePolicy.SKIP_ON_DENIED) String secret,
        @Nullable AccountProfile profile, @Nullable Set<UUID> tagIds, @Nullable byte[] logo) {
    private static final FieldSecurityMetadata<AccountRequestDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("name", dto -> dto.name(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("tenantId", dto -> dto.tenantId(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("ownerId", dto -> dto.ownerId(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("type", dto -> dto.type(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("createdAt", dto -> dto.createdAt(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("secret", dto -> dto.secret(), null, true, List.of("ADMIN"), List.of("ADMIN"), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("profile", dto -> dto.profile(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("tagIds", dto -> dto.tagIds(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("logo", dto -> dto.logo(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    /**
     * @param name name - nullable unless copied validation annotations require a value or constrain its range.
     * @param tenantId tenantId - nullable unless copied validation annotations require a value or constrain its range.
     * @param ownerId ownerId - nullable unless copied validation annotations require a value or constrain its range.
     * @param type type - nullable unless copied validation annotations require a value or constrain its range.
     * @param createdAt createdAt - nullable unless copied validation annotations require a value or constrain its range.
     * @param secret secret - nullable unless copied validation annotations require a value or constrain its range.
     * @param profile profile - nullable unless copied validation annotations require a value or constrain its range.
     * @param tagIds tagIds - nullable unless copied validation annotations require a value or constrain its range.
     * @param logo logo - nullable unless copied validation annotations require a value or constrain its range.
     */
    public AccountRequestDto {
        tagIds = tagIds == null ? null : Set.copyOf(tagIds);
        logo = logo == null ? null : Arrays.copyOf(logo, logo.length);
    }

    public byte[] logo() {
        return logo == null ? null : Arrays.copyOf(logo, logo.length);
    }

    public static FieldSecurityMetadata<AccountRequestDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public AccountRequestDto withName(@Nullable @NotBlank String name) {
        return new AccountRequestDto(name, this.tenantId, this.ownerId, this.type, this.createdAt, this.secret, this.profile, this.tagIds, this.logo);
    }

    public AccountRequestDto withTenantId(@Nullable String tenantId) {
        return new AccountRequestDto(this.name, tenantId, this.ownerId, this.type, this.createdAt, this.secret, this.profile, this.tagIds, this.logo);
    }

    public AccountRequestDto withOwnerId(@Nullable String ownerId) {
        return new AccountRequestDto(this.name, this.tenantId, ownerId, this.type, this.createdAt, this.secret, this.profile, this.tagIds, this.logo);
    }

    public AccountRequestDto withType(@Nullable AccountType type) {
        return new AccountRequestDto(this.name, this.tenantId, this.ownerId, type, this.createdAt, this.secret, this.profile, this.tagIds, this.logo);
    }

    public AccountRequestDto withCreatedAt(@Nullable Instant createdAt) {
        return new AccountRequestDto(this.name, this.tenantId, this.ownerId, this.type, createdAt, this.secret, this.profile, this.tagIds, this.logo);
    }

    public AccountRequestDto withSecret(@Nullable String secret) {
        return new AccountRequestDto(this.name, this.tenantId, this.ownerId, this.type, this.createdAt, secret, this.profile, this.tagIds, this.logo);
    }

    public AccountRequestDto withProfile(@Nullable AccountProfile profile) {
        return new AccountRequestDto(this.name, this.tenantId, this.ownerId, this.type, this.createdAt, this.secret, profile, this.tagIds, this.logo);
    }

    public AccountRequestDto withTagIds(@Nullable Set<UUID> tagIds) {
        return new AccountRequestDto(this.name, this.tenantId, this.ownerId, this.type, this.createdAt, this.secret, this.profile, tagIds, this.logo);
    }

    public AccountRequestDto withLogo(@Nullable byte[] logo) {
        return new AccountRequestDto(this.name, this.tenantId, this.ownerId, this.type, this.createdAt, this.secret, this.profile, this.tagIds, logo);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        @NotBlank
        private String name;

        @Nullable
        private String tenantId;

        @Nullable
        private String ownerId;

        @Nullable
        private AccountType type;

        @Nullable
        private Instant createdAt;

        @Nullable
        private String secret;

        @Nullable
        private AccountProfile profile;

        @Nullable
        private Set<UUID> tagIds;

        @Nullable
        private byte[] logo;

        public Builder name(@Nullable @NotBlank String name) {
            this.name = name;
            return this;
        }

        public Builder tenantId(@Nullable String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder ownerId(@Nullable String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder type(@Nullable AccountType type) {
            this.type = type;
            return this;
        }

        public Builder createdAt(@Nullable Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder secret(@Nullable String secret) {
            this.secret = secret;
            return this;
        }

        public Builder profile(@Nullable AccountProfile profile) {
            this.profile = profile;
            return this;
        }

        public Builder tagIds(@Nullable Set<UUID> tagIds) {
            this.tagIds = tagIds == null ? null : Set.copyOf(tagIds);
            return this;
        }

        public Builder logo(@Nullable byte[] logo) {
            this.logo = logo == null ? null : Arrays.copyOf(logo, logo.length);
            return this;
        }

        public AccountRequestDto build() {
            return new AccountRequestDto(this.name, this.tenantId, this.ownerId, this.type, this.createdAt, this.secret, this.profile, this.tagIds, this.logo);
        }
    }
}

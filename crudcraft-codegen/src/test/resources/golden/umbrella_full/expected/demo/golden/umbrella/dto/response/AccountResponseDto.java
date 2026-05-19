package demo.golden.umbrella.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import demo.golden.umbrella.AccountProfile;
import demo.golden.umbrella.AccountType;
import demo.golden.umbrella.dto.ref.AccountTagRef;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
 * - Package: demo.golden.umbrella.dto.response
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
 * <p>Nullness: response IDs are non-null after persistence; other fields may be null when database values, projections, or field-level security omit them.
 *
 * @param id id - nullable unless copied validation annotations require a value or constrain its range.
 * @param name name - nullable unless copied validation annotations require a value or constrain its range.
 * @param tenantId tenantId - nullable unless copied validation annotations require a value or constrain its range.
 * @param ownerId ownerId - nullable unless copied validation annotations require a value or constrain its range.
 * @param type type - nullable unless copied validation annotations require a value or constrain its range.
 * @param createdAt createdAt - nullable unless copied validation annotations require a value or constrain its range.
 * @param secret secret - nullable unless copied validation annotations require a value or constrain its range.
 * @param profile profile - nullable unless copied validation annotations require a value or constrain its range.
 * @param tags tags - nullable unless copied validation annotations require a value or constrain its range.
 * @param logo logo - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Response DTO for Account"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AccountResponseDto(@NotNull UUID id, @Nullable @NotBlank String name,
        @Nullable String tenantId, @Nullable String ownerId, @Nullable AccountType type,
        @Nullable Instant createdAt,
        @Nullable @FieldSecurity(readRoles = {"ADMIN"}, writeRoles = {"ADMIN"}, writePolicy = WritePolicy.SKIP_ON_DENIED) String secret,
        @Nullable AccountProfile profile, @Nullable Set<AccountTagRef> tags,
        @Nullable byte[] logo) {
    private static final FieldSecurityMetadata<AccountResponseDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("id", dto -> dto.id(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("name", dto -> dto.name(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("tenantId", dto -> dto.tenantId(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("ownerId", dto -> dto.ownerId(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("type", dto -> dto.type(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("createdAt", dto -> dto.createdAt(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("secret", dto -> dto.secret(), null, true, List.of("ADMIN"), List.of("ADMIN"), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("profile", dto -> dto.profile(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("tags", dto -> dto.tags(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("logo", dto -> dto.logo(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    /**
     * @param id id - nullable unless copied validation annotations require a value or constrain its range.
     * @param name name - nullable unless copied validation annotations require a value or constrain its range.
     * @param tenantId tenantId - nullable unless copied validation annotations require a value or constrain its range.
     * @param ownerId ownerId - nullable unless copied validation annotations require a value or constrain its range.
     * @param type type - nullable unless copied validation annotations require a value or constrain its range.
     * @param createdAt createdAt - nullable unless copied validation annotations require a value or constrain its range.
     * @param secret secret - nullable unless copied validation annotations require a value or constrain its range.
     * @param profile profile - nullable unless copied validation annotations require a value or constrain its range.
     * @param tags tags - nullable unless copied validation annotations require a value or constrain its range.
     * @param logo logo - nullable unless copied validation annotations require a value or constrain its range.
     */
    public AccountResponseDto {
        tags = tags == null ? null : Set.copyOf(tags);
        logo = logo == null ? null : Arrays.copyOf(logo, logo.length);
    }

    public byte[] logo() {
        return logo == null ? null : Arrays.copyOf(logo, logo.length);
    }

    public static FieldSecurityMetadata<AccountResponseDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @NotNull
        private UUID id;

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
        private Set<AccountTagRef> tags;

        @Nullable
        private byte[] logo;

        public Builder id(@NotNull UUID id) {
            this.id = id;
            return this;
        }

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

        public Builder tags(@Nullable Set<AccountTagRef> tags) {
            this.tags = tags == null ? null : Set.copyOf(tags);
            return this;
        }

        public Builder logo(@Nullable byte[] logo) {
            this.logo = logo == null ? null : Arrays.copyOf(logo, logo.length);
            return this;
        }

        public AccountResponseDto build() {
            return new AccountResponseDto(this.id, this.name, this.tenantId, this.ownerId, this.type, this.createdAt, this.secret, this.profile, this.tags, this.logo);
        }
    }
}

package demo.golden.umbrella.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
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
 */
@Schema(
        description = "Response DTO Detail for Account"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AccountDetailResponseDto(@NotNull UUID id, @Nullable @NotBlank String name,
        @Nullable String tenantId) {
    private static final FieldSecurityMetadata<AccountDetailResponseDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("id", dto -> dto.id(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("name", dto -> dto.name(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("tenantId", dto -> dto.tenantId(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    public static FieldSecurityMetadata<AccountDetailResponseDto> fieldSecurityMetadata() {
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

        public AccountDetailResponseDto build() {
            return new AccountDetailResponseDto(this.id, this.name, this.tenantId);
        }
    }
}

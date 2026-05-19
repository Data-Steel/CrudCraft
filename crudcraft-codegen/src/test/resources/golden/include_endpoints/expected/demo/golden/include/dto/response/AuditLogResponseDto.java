package demo.golden.include.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;

/**
 * Generated model file for AuditLog; do not edit manually.
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
 * - Source model: AuditLog
 * - Package: demo.golden.include.dto.response
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
 * @param actor actor - nullable unless copied validation annotations require a value or constrain its range.
 * @param happenedAt happenedAt - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Response DTO for AuditLog"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuditLogResponseDto(@NotNull UUID id, @Nullable String actor,
        @Nullable Instant happenedAt) {
    private static final FieldSecurityMetadata<AuditLogResponseDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("id", dto -> dto.id(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("actor", dto -> dto.actor(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("happenedAt", dto -> dto.happenedAt(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    public static FieldSecurityMetadata<AuditLogResponseDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @NotNull
        private UUID id;

        @Nullable
        private String actor;

        @Nullable
        private Instant happenedAt;

        public Builder id(@NotNull UUID id) {
            this.id = id;
            return this;
        }

        public Builder actor(@Nullable String actor) {
            this.actor = actor;
            return this;
        }

        public Builder happenedAt(@Nullable Instant happenedAt) {
            this.happenedAt = happenedAt;
            return this;
        }

        public AuditLogResponseDto build() {
            return new AuditLogResponseDto(this.id, this.actor, this.happenedAt);
        }
    }
}

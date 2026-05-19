package demo.golden.relationship.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import demo.golden.relationship.UserProfile;
import demo.golden.relationship.dto.ref.AccessGroupRef;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;

/**
 * Generated model file for UserAccount; do not edit manually.
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
 * - Source model: UserAccount
 * - Package: demo.golden.relationship.dto.response
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
 * @param username username - nullable unless copied validation annotations require a value or constrain its range.
 * @param profile profile - nullable unless copied validation annotations require a value or constrain its range.
 * @param groups groups - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Response DTO for UserAccount"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserAccountResponseDto(@NotNull UUID id, @Nullable String username,
        @Nullable UserProfile profile, @Nullable Set<AccessGroupRef> groups) {
    private static final FieldSecurityMetadata<UserAccountResponseDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("id", dto -> dto.id(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("username", dto -> dto.username(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("profile", dto -> dto.profile(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("groups", dto -> dto.groups(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    /**
     * @param id id - nullable unless copied validation annotations require a value or constrain its range.
     * @param username username - nullable unless copied validation annotations require a value or constrain its range.
     * @param profile profile - nullable unless copied validation annotations require a value or constrain its range.
     * @param groups groups - nullable unless copied validation annotations require a value or constrain its range.
     */
    public UserAccountResponseDto {
        groups = groups == null ? null : Set.copyOf(groups);
    }

    public static FieldSecurityMetadata<UserAccountResponseDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @NotNull
        private UUID id;

        @Nullable
        private String username;

        @Nullable
        private UserProfile profile;

        @Nullable
        private Set<AccessGroupRef> groups;

        public Builder id(@NotNull UUID id) {
            this.id = id;
            return this;
        }

        public Builder username(@Nullable String username) {
            this.username = username;
            return this;
        }

        public Builder profile(@Nullable UserProfile profile) {
            this.profile = profile;
            return this;
        }

        public Builder groups(@Nullable Set<AccessGroupRef> groups) {
            this.groups = groups == null ? null : Set.copyOf(groups);
            return this;
        }

        public UserAccountResponseDto build() {
            return new UserAccountResponseDto(this.id, this.username, this.profile, this.groups);
        }
    }
}

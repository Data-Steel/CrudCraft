package demo.golden.relationship.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import demo.golden.relationship.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
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
 * - Package: demo.golden.relationship.dto.request
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
 * <p>Nullness: request DTO instances for UserAccount are non-null. Fields may be null unless Jakarta validation marks them required; PATCH treats null according to mapper patch semantics, while PUT supplies replacement values.
 *
 * @param username username - nullable unless copied validation annotations require a value or constrain its range.
 * @param profile profile - nullable unless copied validation annotations require a value or constrain its range.
 * @param groupIds groupIds - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Create/update DTO for UserAccount"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserAccountRequestDto(@Nullable String username, @Nullable UserProfile profile,
        @Nullable Set<UUID> groupIds) {
    private static final FieldSecurityMetadata<UserAccountRequestDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("username", dto -> dto.username(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("profile", dto -> dto.profile(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("groupIds", dto -> dto.groupIds(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    /**
     * @param username username - nullable unless copied validation annotations require a value or constrain its range.
     * @param profile profile - nullable unless copied validation annotations require a value or constrain its range.
     * @param groupIds groupIds - nullable unless copied validation annotations require a value or constrain its range.
     */
    public UserAccountRequestDto {
        groupIds = groupIds == null ? null : Set.copyOf(groupIds);
    }

    public static FieldSecurityMetadata<UserAccountRequestDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        private String username;

        @Nullable
        private UserProfile profile;

        @Nullable
        private Set<UUID> groupIds;

        public Builder username(@Nullable String username) {
            this.username = username;
            return this;
        }

        public Builder profile(@Nullable UserProfile profile) {
            this.profile = profile;
            return this;
        }

        public Builder groupIds(@Nullable Set<UUID> groupIds) {
            this.groupIds = groupIds == null ? null : Set.copyOf(groupIds);
            return this;
        }

        public UserAccountRequestDto build() {
            return new UserAccountRequestDto(this.username, this.profile, this.groupIds);
        }
    }
}

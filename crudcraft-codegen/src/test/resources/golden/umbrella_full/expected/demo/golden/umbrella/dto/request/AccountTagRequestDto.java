package demo.golden.umbrella.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;

/**
 * Generated model file for AccountTag; do not edit manually.
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
 * - Source model: AccountTag
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
 * <p>Nullness: request DTO instances for AccountTag are non-null. Fields may be null unless Jakarta validation marks them required; PATCH treats null according to mapper patch semantics, while PUT supplies replacement values.
 */
@Schema(
        description = "Create/update DTO for AccountTag"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AccountTagRequestDto() {
    private static final FieldSecurityMetadata<AccountTagRequestDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of());

    public static FieldSecurityMetadata<AccountTagRequestDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        public AccountTagRequestDto build() {
            return new AccountTagRequestDto();
        }
    }
}

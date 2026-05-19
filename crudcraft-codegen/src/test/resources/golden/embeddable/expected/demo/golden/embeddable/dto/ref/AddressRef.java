package demo.golden.embeddable.dto.ref;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;

/**
 * Generated model file for Address; do not edit manually.
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
 * - Source model: Address
 * - Package: demo.golden.embeddable.dto.ref
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
 */
@Schema(
        description = "Reference DTO for Address"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AddressRef() {
    private static final FieldSecurityMetadata<AddressRef> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of());

    public static FieldSecurityMetadata<AddressRef> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        public AddressRef build() {
            return new AddressRef();
        }
    }
}

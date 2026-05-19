package demo.golden.embeddable.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import java.util.List;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;

/**
 * Generated model file for CustomerRecord; do not edit manually.
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
 * - Source model: CustomerRecord
 * - Package: demo.golden.embeddable.dto.request
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
 * <p>Nullness: request DTO instances for CustomerRecord are non-null. Fields may be null unless Jakarta validation marks them required; PATCH treats null according to mapper patch semantics, while PUT supplies replacement values.
 *
 * @param name name - nullable unless copied validation annotations require a value or constrain its range.
 * @param street street - nullable unless copied validation annotations require a value or constrain its range.
 * @param city city - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Create/update DTO for CustomerRecord"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomerRecordRequestDto(@Nullable String name, @Nullable String street,
        @Nullable String city) {
    private static final FieldSecurityMetadata<CustomerRecordRequestDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("name", dto -> dto.name(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("street", dto -> dto.street(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("city", dto -> dto.city(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    public static FieldSecurityMetadata<CustomerRecordRequestDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        private String name;

        @Nullable
        private String street;

        @Nullable
        private String city;

        public Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        public Builder street(@Nullable String street) {
            this.street = street;
            return this;
        }

        public Builder city(@Nullable String city) {
            this.city = city;
            return this;
        }

        public CustomerRecordRequestDto build() {
            return new CustomerRecordRequestDto(this.name, this.street, this.city);
        }
    }
}

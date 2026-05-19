package demo.golden.securefield.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import java.util.List;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;

/**
 * Generated model file for Customer; do not edit manually.
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
 * - Source model: Customer
 * - Package: demo.golden.securefield.dto.request
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
 * <p>Nullness: request DTO instances for Customer are non-null. Fields may be null unless Jakarta validation marks them required; PATCH treats null according to mapper patch semantics, while PUT supplies replacement values.
 *
 * @param name name - nullable unless copied validation annotations require a value or constrain its range.
 * @param loyaltyPoints loyaltyPoints - nullable unless copied validation annotations require a value or constrain its range.
 * @param vip vip - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Create/update DTO for Customer"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomerRequestDto(@Nullable String name,
        @Nullable @FieldSecurity(readRoles = {"SUPPORT"}, writeRoles = {"ADMIN"}, writePolicy = WritePolicy.SKIP_ON_DENIED) Integer loyaltyPoints,
        @Nullable @FieldSecurity(readRoles = {"SUPPORT", "ADMIN"}, writeRoles = {"ADMIN"}, writePolicy = WritePolicy.FAIL_ON_DENIED) Boolean vip) {
    private static final FieldSecurityMetadata<CustomerRequestDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("name", dto -> dto.name(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("loyaltyPoints", dto -> dto.loyaltyPoints(), null, true, List.of("SUPPORT"), List.of("ADMIN"), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("vip", dto -> dto.vip(), null, true, List.of("SUPPORT", "ADMIN"), List.of("ADMIN"), WritePolicy.FAIL_ON_DENIED)));

    public static FieldSecurityMetadata<CustomerRequestDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        private String name;

        @Nullable
        private Integer loyaltyPoints;

        @Nullable
        private Boolean vip;

        public Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        public Builder loyaltyPoints(@Nullable Integer loyaltyPoints) {
            this.loyaltyPoints = loyaltyPoints;
            return this;
        }

        public Builder vip(@Nullable Boolean vip) {
            this.vip = vip;
            return this;
        }

        public CustomerRequestDto build() {
            return new CustomerRequestDto(this.name, this.loyaltyPoints, this.vip);
        }
    }
}

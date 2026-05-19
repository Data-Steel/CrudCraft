package demo.golden.projection.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;

/**
 * Generated model file for Purchase; do not edit manually.
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
 * - Source model: Purchase
 * - Package: demo.golden.projection.dto.request
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
 * <p>Nullness: request DTO instances for Purchase are non-null. Fields may be null unless Jakarta validation marks them required; PATCH treats null according to mapper patch semantics, while PUT supplies replacement values.
 *
 * @param reference reference - nullable unless copied validation annotations require a value or constrain its range.
 * @param customerId customerId - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Create/update DTO for Purchase"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PurchaseRequestDto(@Nullable String reference, @Nullable UUID customerId) {
    private static final FieldSecurityMetadata<PurchaseRequestDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("reference", dto -> dto.reference(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("customerId", dto -> dto.customerId(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    public static FieldSecurityMetadata<PurchaseRequestDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        private String reference;

        @Nullable
        private UUID customerId;

        public Builder reference(@Nullable String reference) {
            this.reference = reference;
            return this;
        }

        public Builder customerId(@Nullable UUID customerId) {
            this.customerId = customerId;
            return this;
        }

        public PurchaseRequestDto build() {
            return new PurchaseRequestDto(this.reference, this.customerId);
        }
    }
}

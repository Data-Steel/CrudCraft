package demo.golden.projection.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import demo.golden.projection.dto.ref.CustomerRef;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;
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
 * - Package: demo.golden.projection.dto.response
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
 * @param reference reference - nullable unless copied validation annotations require a value or constrain its range.
 * @param customer customer - nullable unless copied validation annotations require a value or constrain its range.
 * @param customerName customerName - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Response DTO for Purchase"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PurchaseResponseDto(@NotNull UUID id, @Nullable String reference,
        @Nullable CustomerRef customer,
        @Nullable @ProjectionField("customer.name") String customerName) {
    private static final FieldSecurityMetadata<PurchaseResponseDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("id", dto -> dto.id(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("reference", dto -> dto.reference(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("customer", dto -> dto.customer(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("customerName", dto -> dto.customerName(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    public static FieldSecurityMetadata<PurchaseResponseDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @NotNull
        private UUID id;

        @Nullable
        private String reference;

        @Nullable
        private CustomerRef customer;

        @Nullable
        private String customerName;

        public Builder id(@NotNull UUID id) {
            this.id = id;
            return this;
        }

        public Builder reference(@Nullable String reference) {
            this.reference = reference;
            return this;
        }

        public Builder customer(@Nullable CustomerRef customer) {
            this.customer = customer;
            return this;
        }

        public Builder customerName(@Nullable String customerName) {
            this.customerName = customerName;
            return this;
        }

        public PurchaseResponseDto build() {
            return new PurchaseResponseDto(this.id, this.reference, this.customer, this.customerName);
        }
    }
}

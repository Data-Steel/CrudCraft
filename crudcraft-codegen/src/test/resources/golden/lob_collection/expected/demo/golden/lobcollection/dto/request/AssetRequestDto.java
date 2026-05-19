package demo.golden.lobcollection.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;

/**
 * Generated model file for Asset; do not edit manually.
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
 * - Source model: Asset
 * - Package: demo.golden.lobcollection.dto.request
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
 * <p>Nullness: request DTO instances for Asset are non-null. Fields may be null unless Jakarta validation marks them required; PATCH treats null according to mapper patch semantics, while PUT supplies replacement values.
 *
 * @param name name - nullable unless copied validation annotations require a value or constrain its range.
 * @param attachments attachments - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Create/update DTO for Asset"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssetRequestDto(@Nullable String name, @Nullable Set<byte[]> attachments) {
    private static final FieldSecurityMetadata<AssetRequestDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("name", dto -> dto.name(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("attachments", dto -> dto.attachments(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    /**
     * @param name name - nullable unless copied validation annotations require a value or constrain its range.
     * @param attachments attachments - nullable unless copied validation annotations require a value or constrain its range.
     */
    public AssetRequestDto {
        attachments = attachments == null ? null : Set.copyOf(attachments);
    }

    public static FieldSecurityMetadata<AssetRequestDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public AssetRequestDto withName(@Nullable String name) {
        return new AssetRequestDto(name, this.attachments);
    }

    public AssetRequestDto withAttachments(@Nullable Set<byte[]> attachments) {
        return new AssetRequestDto(this.name, attachments);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        private String name;

        @Nullable
        private Set<byte[]> attachments;

        public Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        public Builder attachments(@Nullable Set<byte[]> attachments) {
            this.attachments = attachments == null ? null : Set.copyOf(attachments);
            return this;
        }

        public AssetRequestDto build() {
            return new AssetRequestDto(this.name, this.attachments);
        }
    }
}

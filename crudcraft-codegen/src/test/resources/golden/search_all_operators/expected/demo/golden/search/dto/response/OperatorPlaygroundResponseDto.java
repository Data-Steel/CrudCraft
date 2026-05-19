package demo.golden.search.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import demo.golden.search.dto.ref.SearchTagRef;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;

/**
 * Generated model file for OperatorPlayground; do not edit manually.
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
 * - Source model: OperatorPlayground
 * - Package: demo.golden.search.dto.response
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
 * @param title title - nullable unless copied validation annotations require a value or constrain its range.
 * @param score score - nullable unless copied validation annotations require a value or constrain its range.
 * @param publishedAt publishedAt - nullable unless copied validation annotations require a value or constrain its range.
 * @param labels labels - nullable unless copied validation annotations require a value or constrain its range.
 * @param tags tags - nullable unless copied validation annotations require a value or constrain its range.
 * @param attributes attributes - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Response DTO for OperatorPlayground"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OperatorPlaygroundResponseDto(@NotNull UUID id, @Nullable String title,
        @Nullable BigDecimal score, @Nullable Instant publishedAt, @Nullable Set<String> labels,
        @Nullable Set<SearchTagRef> tags, @Nullable Map<String, String> attributes) {
    private static final FieldSecurityMetadata<OperatorPlaygroundResponseDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("id", dto -> dto.id(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("title", dto -> dto.title(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("score", dto -> dto.score(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("publishedAt", dto -> dto.publishedAt(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("labels", dto -> dto.labels(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("tags", dto -> dto.tags(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("attributes", dto -> dto.attributes(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    /**
     * @param id id - nullable unless copied validation annotations require a value or constrain its range.
     * @param title title - nullable unless copied validation annotations require a value or constrain its range.
     * @param score score - nullable unless copied validation annotations require a value or constrain its range.
     * @param publishedAt publishedAt - nullable unless copied validation annotations require a value or constrain its range.
     * @param labels labels - nullable unless copied validation annotations require a value or constrain its range.
     * @param tags tags - nullable unless copied validation annotations require a value or constrain its range.
     * @param attributes attributes - nullable unless copied validation annotations require a value or constrain its range.
     */
    public OperatorPlaygroundResponseDto {
        labels = labels == null ? null : Set.copyOf(labels);
        tags = tags == null ? null : Set.copyOf(tags);
        attributes = attributes == null ? null : Map.copyOf(attributes);
    }

    public static FieldSecurityMetadata<OperatorPlaygroundResponseDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @NotNull
        private UUID id;

        @Nullable
        private String title;

        @Nullable
        private BigDecimal score;

        @Nullable
        private Instant publishedAt;

        @Nullable
        private Set<String> labels;

        @Nullable
        private Set<SearchTagRef> tags;

        @Nullable
        private Map<String, String> attributes;

        public Builder id(@NotNull UUID id) {
            this.id = id;
            return this;
        }

        public Builder title(@Nullable String title) {
            this.title = title;
            return this;
        }

        public Builder score(@Nullable BigDecimal score) {
            this.score = score;
            return this;
        }

        public Builder publishedAt(@Nullable Instant publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public Builder labels(@Nullable Set<String> labels) {
            this.labels = labels == null ? null : Set.copyOf(labels);
            return this;
        }

        public Builder tags(@Nullable Set<SearchTagRef> tags) {
            this.tags = tags == null ? null : Set.copyOf(tags);
            return this;
        }

        public Builder attributes(@Nullable Map<String, String> attributes) {
            this.attributes = attributes == null ? null : Map.copyOf(attributes);
            return this;
        }

        public OperatorPlaygroundResponseDto build() {
            return new OperatorPlaygroundResponseDto(this.id, this.title, this.score, this.publishedAt, this.labels, this.tags, this.attributes);
        }
    }
}

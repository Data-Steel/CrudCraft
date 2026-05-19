/*
 * Copyright (c) 2026 CrudCraft contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo.golden.dtovariants.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import java.util.List;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;

/**
 * Generated model file for CatalogItem; do not edit manually.
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
 * - Source model: CatalogItem
 * - Package: demo.golden.dtovariants.dto.request
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
 * <p>Nullness: request DTO instances for CatalogItem are non-null. Fields may be null unless Jakarta validation marks them required; PATCH treats null according to mapper patch semantics, while PUT supplies replacement values.
 *
 * @param name name - nullable unless copied validation annotations require a value or constrain its range.
 * @param description description - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Create/update DTO for CatalogItem"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CatalogItemRequestDto(@Nullable String name, @Nullable String description) {
    private static final FieldSecurityMetadata<CatalogItemRequestDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("name", dto -> dto.name(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("description", dto -> dto.description(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    public static FieldSecurityMetadata<CatalogItemRequestDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        private String name;

        @Nullable
        private String description;

        public Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        public Builder description(@Nullable String description) {
            this.description = description;
            return this;
        }

        public CatalogItemRequestDto build() {
            return new CatalogItemRequestDto(this.name, this.description);
        }
    }
}

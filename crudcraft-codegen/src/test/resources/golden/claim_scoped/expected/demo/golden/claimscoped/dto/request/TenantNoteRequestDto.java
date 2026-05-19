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
package demo.golden.claimscoped.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;

/**
 * Generated model file for TenantNote; do not edit manually.
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
 * - Source model: TenantNote
 * - Package: demo.golden.claimscoped.dto.request
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
 * <p>Nullness: request DTO instances for TenantNote are non-null. Fields may be null unless Jakarta validation marks them required; PATCH treats null according to mapper patch semantics, while PUT supplies replacement values.
 *
 * @param tenantId tenantId - nullable unless copied validation annotations require a value or constrain its range.
 * @param ownerId ownerId - nullable unless copied validation annotations require a value or constrain its range.
 * @param body body - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Create/update DTO for TenantNote"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TenantNoteRequestDto(@Nullable String tenantId, @Nullable UUID ownerId,
        @Nullable String body) {
    private static final FieldSecurityMetadata<TenantNoteRequestDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("tenantId", dto -> dto.tenantId(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("ownerId", dto -> dto.ownerId(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("body", dto -> dto.body(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    public static FieldSecurityMetadata<TenantNoteRequestDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        private String tenantId;

        @Nullable
        private UUID ownerId;

        @Nullable
        private String body;

        public Builder tenantId(@Nullable String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder ownerId(@Nullable UUID ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder body(@Nullable String body) {
            this.body = body;
            return this;
        }

        public TenantNoteRequestDto build() {
            return new TenantNoteRequestDto(this.tenantId, this.ownerId, this.body);
        }
    }
}

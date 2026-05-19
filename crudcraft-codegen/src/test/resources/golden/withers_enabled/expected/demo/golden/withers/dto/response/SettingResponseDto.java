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
package demo.golden.withers.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;

/**
 * Generated model file for Setting; do not edit manually.
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
 * - Source model: Setting
 * - Package: demo.golden.withers.dto.response
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
 * @param name name - nullable unless copied validation annotations require a value or constrain its range.
 * @param value value - nullable unless copied validation annotations require a value or constrain its range.
 */
@Schema(
        description = "Response DTO for Setting"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SettingResponseDto(@NotNull UUID id, @Nullable String name, @Nullable String value) {
    private static final FieldSecurityMetadata<SettingResponseDto> FIELD_SECURITY_METADATA = FieldSecurityMetadata.of(List.of(new FieldSecurityMetadata.FieldRule<>("id", dto -> dto.id(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("name", dto -> dto.name(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED),
    new FieldSecurityMetadata.FieldRule<>("value", dto -> dto.value(), null, false, List.of(), List.of(), WritePolicy.SKIP_ON_DENIED)));

    public static FieldSecurityMetadata<SettingResponseDto> fieldSecurityMetadata() {
        return FIELD_SECURITY_METADATA;
    }

    public SettingResponseDto withId(@NotNull UUID id) {
        return new SettingResponseDto(id, this.name, this.value);
    }

    public SettingResponseDto withName(@Nullable String name) {
        return new SettingResponseDto(this.id, name, this.value);
    }

    public SettingResponseDto withValue(@Nullable String value) {
        return new SettingResponseDto(this.id, this.name, value);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @NotNull
        private UUID id;

        @Nullable
        private String name;

        @Nullable
        private String value;

        public Builder id(@NotNull UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        public Builder value(@Nullable String value) {
            this.value = value;
            return this;
        }

        public SettingResponseDto build() {
            return new SettingResponseDto(this.id, this.name, this.value);
        }
    }
}

/*
 * Copyright (c) 2025 CrudCraft contributors
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
package nl.datasteel.crudcraft.annotations.fields;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as permanently excluded from exports.
 * Fields annotated with @ExportExclude will never be included in any export,
 * regardless of includeFields or excludeFields settings in ExportRequest.
 *
 * <p><strong>Note:</strong> This annotation is currently a marker for future implementation.
 * Enforcement is not yet wired into the export pipeline. To exclude fields from export now,
 * ensure they are not marked with {@code @Dto} annotation, or use the {@code excludeFields}
 * parameter in {@code ExportRequest}.
 *
 * <p>This is useful for fields that should never be exported for security,
 * performance, or business logic reasons.
 *
 * <p>Example:
 * <pre>
 * {@code
 * @ExportExclude  // Planned for future enforcement
 * private String internalProcessingData;
 * }
 * </pre>
 *
 * @see nl.datasteel.crudcraft.runtime.export.ExportRequest
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExportExclude {
}

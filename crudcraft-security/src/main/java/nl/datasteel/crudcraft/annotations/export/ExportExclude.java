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
package nl.datasteel.crudcraft.annotations.export;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as permanently excluded from exports.
 * Fields annotated with @ExportExclude will never be included in any export,
 * regardless of includeFields or excludeFields settings in ExportRequest.
 *
 * <p>This annotation is enforced by the entity export system. In entity export mode,
 * fields marked with @ExportExclude are automatically excluded during metadata introspection.
 * In DTO export mode, ensure excluded fields are not marked with {@code @Dto} annotation.
 *
 * <p>This is useful for fields that should never be exported for security,
 * performance, or business logic reasons.
 *
 * <p>Example:
 * <pre>
 * {@code
 * @Entity
 * public class User {
 *     private String name;  // Exportable
 *     
 *     @ExportExclude
 *     private String passwordHash;  // Never exported
 * }
 * }
 * </pre>
 *
 * @see ExportRequest (in crudcraft-runtime module)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExportExclude {
}

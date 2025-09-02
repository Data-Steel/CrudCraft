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

package nl.datasteel.crudcraft.codegen.writer.search;

import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;

/**
 * Holds field metadata together with the operator used for generating
 * predicates. The {@code property} value corresponds to the field name in the
 * generated search request object while {@code path} contains the criteria API
 * path expression.
 */
public record SearchField(
        FieldDescriptor descriptor,
        String property,
        String path,
        SearchOperator operator
) {
}

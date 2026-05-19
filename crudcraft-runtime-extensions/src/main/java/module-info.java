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

/**
 * Runtime extension implementations and generated-service relationship helpers.
 */
module nl.datasteel.crudcraft.runtime.extensions {
    requires transitive jakarta.persistence;
    requires transitive nl.datasteel.crudcraft.runtime.core;
    requires org.slf4j;
    requires spring.data.jpa;

    exports nl.datasteel.crudcraft.runtime.extensions;
    exports nl.datasteel.crudcraft.runtime.extensions.util;

    opens nl.datasteel.crudcraft.runtime.extensions;
}

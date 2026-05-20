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
 * Umbrella Spring Boot starter aggregating all CrudCraft starter capabilities.
 */
@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module nl.datasteel.crudcraft.starter {
    requires transitive nl.datasteel.crudcraft.starter.core;
    requires transitive nl.datasteel.crudcraft.starter.export;
    requires transitive nl.datasteel.crudcraft.starter.extensions;
    requires transitive nl.datasteel.crudcraft.starter.observability;
    requires transitive nl.datasteel.crudcraft.starter.projection;
    requires transitive nl.datasteel.crudcraft.starter.search;
    requires transitive nl.datasteel.crudcraft.starter.security;

    exports nl.datasteel.crudcraft.starter.bundle;
}

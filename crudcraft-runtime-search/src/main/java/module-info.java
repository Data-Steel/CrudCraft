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
 * Runtime search request and execution support for generated CrudCraft endpoints.
 */
module nl.datasteel.crudcraft.runtime.search {
    requires transitive nl.datasteel.crudcraft.api;
    requires transitive nl.datasteel.crudcraft.runtime.core;
    requires transitive spring.data.commons;
    requires transitive spring.data.jpa;
    requires spring.beans;
    requires spring.boot;
    requires spring.context;
    requires static com.github.spotbugs.annotations;

    exports nl.datasteel.crudcraft.runtime.search;
    exports nl.datasteel.crudcraft.runtime.search.config;
}

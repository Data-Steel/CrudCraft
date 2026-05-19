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
 * Runtime CSV and Excel export support for CrudCraft services.
 */
@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module nl.datasteel.crudcraft.runtime.export {
    requires transitive jakarta.persistence;
    requires transitive nl.datasteel.crudcraft.api;
    requires transitive nl.datasteel.crudcraft.runtime.core;
    requires transitive spring.data.commons;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.apache.commons.csv;
    requires org.slf4j;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.tx;
    requires spring.webmvc;
    requires static com.github.spotbugs.annotations;

    exports nl.datasteel.crudcraft.runtime.export;
    exports nl.datasteel.crudcraft.runtime.export.config;
    exports nl.datasteel.crudcraft.runtime.export.service;
    exports nl.datasteel.crudcraft.runtime.export.util;
}

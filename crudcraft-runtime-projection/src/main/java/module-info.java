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
 * Runtime projection query APIs, metadata, mapping, and JPA execution support.
 */
module nl.datasteel.crudcraft.runtime.projection {
    requires transitive jakarta.persistence;
    requires transitive nl.datasteel.crudcraft.api;
    requires transitive nl.datasteel.crudcraft.runtime.core;
    requires transitive spring.data.commons;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.boot.hibernate;
    requires spring.context;
    requires org.slf4j;
    requires static com.github.spotbugs.annotations;

    exports nl.datasteel.crudcraft.runtime.projection;
    exports nl.datasteel.crudcraft.runtime.projection.api;
    exports nl.datasteel.crudcraft.runtime.projection.config;
    exports nl.datasteel.crudcraft.runtime.projection.mapping;
    exports nl.datasteel.crudcraft.runtime.projection.metadata;

    opens nl.datasteel.crudcraft.runtime.projection.impl to
            spring.beans,
            spring.core;
    opens nl.datasteel.crudcraft.runtime.projection.impl.jpa to
            spring.beans,
            spring.core;
}

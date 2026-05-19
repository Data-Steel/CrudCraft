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
 * Core runtime contracts, controller base types, metadata, mapping, and CRUD services.
 */
module nl.datasteel.crudcraft.runtime.core {
    requires transitive jakarta.persistence;
    requires transitive jakarta.servlet;
    requires transitive jakarta.validation;
    requires transitive nl.datasteel.crudcraft.api;
    requires jakarta.annotation;
    requires spring.data.commons;
    requires spring.data.jpa;
    requires spring.beans;
    requires spring.aop;
    requires spring.context;
    requires spring.core;
    requires spring.tx;
    requires spring.web;
    requires org.slf4j;
    requires static com.github.spotbugs.annotations;

    exports nl.datasteel.crudcraft.runtime;
    exports nl.datasteel.crudcraft.runtime.controller;
    exports nl.datasteel.crudcraft.runtime.controller.response;
    exports nl.datasteel.crudcraft.runtime.exception;
    exports nl.datasteel.crudcraft.runtime.mapper;
    exports nl.datasteel.crudcraft.runtime.metadata;
    exports nl.datasteel.crudcraft.runtime.service;
    exports nl.datasteel.crudcraft.runtime.service.extension;
    exports nl.datasteel.crudcraft.runtime.service.projection;
    exports nl.datasteel.crudcraft.runtime.service.strategy;
    exports nl.datasteel.crudcraft.runtime.util;

    opens nl.datasteel.crudcraft.runtime.service to
            spring.beans,
            spring.core;
}

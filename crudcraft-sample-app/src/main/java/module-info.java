/*
 * Copyright (c) 2026 CrudCraft contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 * Spring Boot sample application that exercises generated CrudCraft runtime integrations.
 */
open module nl.datasteel.crudcraft.sample.app {
    requires com.auth0.jwt;
    requires com.fasterxml.jackson.databind;
    requires jakarta.el;
    requires jakarta.annotation;
    requires jakarta.persistence;
    requires jakarta.validation;
    requires java.naming;
    requires micrometer.core;
    requires nl.datasteel.crudcraft.api;
    requires nl.datasteel.crudcraft.runtime.core;
    requires nl.datasteel.crudcraft.runtime.export;
    requires nl.datasteel.crudcraft.runtime.extensions;
    requires nl.datasteel.crudcraft.runtime.search;
    requires nl.datasteel.crudcraft.runtime.security;
    requires nl.datasteel.crudcraft.starter;
    requires org.mapstruct;
    requires org.glassfish.expressly;
    requires org.slf4j;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.boot.security;
    requires spring.context;
    requires spring.core;
    requires spring.data.commons;
    requires spring.data.jpa;
    requires spring.security.config;
    requires spring.security.core;
    requires spring.security.oauth2.jose;
    requires spring.security.oauth2.resource.server;
    requires spring.security.web;
    requires spring.tx;
    requires spring.web;
    requires spring.webmvc;
    requires static com.github.spotbugs.annotations;
    requires static io.swagger.v3.oas.annotations;

    exports nl.datasteel.crudcraft.sample;
    exports nl.datasteel.crudcraft.sample.blog;
    exports nl.datasteel.crudcraft.sample.blog.content;
    exports nl.datasteel.crudcraft.sample.blog.content.controller;
    exports nl.datasteel.crudcraft.sample.blog.content.dto.ref;
    exports nl.datasteel.crudcraft.sample.blog.content.dto.request;
    exports nl.datasteel.crudcraft.sample.blog.content.dto.response;
    exports nl.datasteel.crudcraft.sample.blog.content.mapper;
    exports nl.datasteel.crudcraft.sample.blog.content.meta;
    exports nl.datasteel.crudcraft.sample.blog.content.repository;
    exports nl.datasteel.crudcraft.sample.blog.content.search;
    exports nl.datasteel.crudcraft.sample.blog.content.service;
    exports nl.datasteel.crudcraft.sample.blog.controller;
    exports nl.datasteel.crudcraft.sample.blog.dto.ref;
    exports nl.datasteel.crudcraft.sample.blog.dto.request;
    exports nl.datasteel.crudcraft.sample.blog.dto.response;
    exports nl.datasteel.crudcraft.sample.blog.mapper;
    exports nl.datasteel.crudcraft.sample.blog.meta;
    exports nl.datasteel.crudcraft.sample.blog.repository;
    exports nl.datasteel.crudcraft.sample.blog.search;
    exports nl.datasteel.crudcraft.sample.blog.service;
    exports nl.datasteel.crudcraft.sample.scope;
    exports nl.datasteel.crudcraft.sample.scope.controller;
    exports nl.datasteel.crudcraft.sample.scope.dto.ref;
    exports nl.datasteel.crudcraft.sample.scope.dto.request;
    exports nl.datasteel.crudcraft.sample.scope.dto.response;
    exports nl.datasteel.crudcraft.sample.scope.mapper;
    exports nl.datasteel.crudcraft.sample.scope.meta;
    exports nl.datasteel.crudcraft.sample.scope.repository;
    exports nl.datasteel.crudcraft.sample.scope.search;
    exports nl.datasteel.crudcraft.sample.scope.service;
    exports nl.datasteel.crudcraft.sample.security;
    exports nl.datasteel.crudcraft.sample.projection;
    exports nl.datasteel.crudcraft.sample.user;
    exports nl.datasteel.crudcraft.sample.user.controller;
    exports nl.datasteel.crudcraft.sample.user.dto.ref;
    exports nl.datasteel.crudcraft.sample.user.dto.request;
    exports nl.datasteel.crudcraft.sample.user.dto.response;
    exports nl.datasteel.crudcraft.sample.user.mapper;
    exports nl.datasteel.crudcraft.sample.user.meta;
    exports nl.datasteel.crudcraft.sample.user.repository;
    exports nl.datasteel.crudcraft.sample.user.search;
    exports nl.datasteel.crudcraft.sample.user.service;
}

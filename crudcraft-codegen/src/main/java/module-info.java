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
 * CrudCraft annotation processors and generation model APIs.
 */
module nl.datasteel.crudcraft.codegen {
    requires transitive java.compiler;
    requires transitive nl.datasteel.crudcraft.api;
    requires java.annotation;
    requires com.fasterxml.jackson.annotation;
    requires com.palantir.javapoet;
    requires jakarta.persistence;
    requires jakarta.validation;
    requires spring.web;
    requires static com.github.spotbugs.annotations;
    requires static io.swagger.v3.oas.annotations;
    requires static jdk.compiler;

    exports nl.datasteel.crudcraft.codegen;
    exports nl.datasteel.crudcraft.codegen.descriptor;
    exports nl.datasteel.crudcraft.codegen.descriptor.field;
    exports nl.datasteel.crudcraft.codegen.descriptor.field.part;
    exports nl.datasteel.crudcraft.codegen.descriptor.model;
    exports nl.datasteel.crudcraft.codegen.descriptor.model.part;
    exports nl.datasteel.crudcraft.codegen.exception;
    exports nl.datasteel.crudcraft.codegen.fileheader;
    exports nl.datasteel.crudcraft.codegen.projection;
    exports nl.datasteel.crudcraft.codegen.util;
    exports nl.datasteel.crudcraft.codegen.writer;
    exports nl.datasteel.crudcraft.codegen.writer.search;

    uses nl.datasteel.crudcraft.codegen.writer.Generator;
    uses nl.datasteel.crudcraft.codegen.writer.search.PredicateGeneratorProvider;

    provides javax.annotation.processing.Processor with
            nl.datasteel.crudcraft.codegen.CrudCraftProcessor,
            nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataProcessor;
    provides nl.datasteel.crudcraft.codegen.writer.Generator with
            nl.datasteel.crudcraft.codegen.writer.DtoGenerator,
            nl.datasteel.crudcraft.codegen.writer.SearchGenerator,
            nl.datasteel.crudcraft.codegen.writer.RelationshipMetaGenerator,
            nl.datasteel.crudcraft.codegen.writer.stubs.RepositoryGenerator,
            nl.datasteel.crudcraft.codegen.writer.stubs.MapperGenerator,
            nl.datasteel.crudcraft.codegen.writer.stubs.ServiceGenerator,
            nl.datasteel.crudcraft.codegen.writer.stubs.ControllerGenerator,
            nl.datasteel.crudcraft.codegen.writer.InsomniaGenerator;
}

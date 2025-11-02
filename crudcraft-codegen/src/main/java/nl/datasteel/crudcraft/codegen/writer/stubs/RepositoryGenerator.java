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
package nl.datasteel.crudcraft.codegen.writer.stubs;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import java.util.UUID;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.util.JavaPoetUtils;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;
import nl.datasteel.crudcraft.codegen.writer.Generator;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;

/**
 * Generates a Spring Data JPA repository interface for the given model.
 */
public class RepositoryGenerator implements StubGenerator {

    private static final String REPOSITORY_CAMEL = "Repository";

    @Override
    public List<JavaFile> generate(ModelDescriptor modelDescriptor, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(modelDescriptor, ctx)) {
            return List.of();
        }
        
        // Skip repository generation for abstract classes
        if (modelDescriptor.isAbstract()) {
            ctx.env().getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "Skipping repository generation for abstract entity: " + modelDescriptor.getName()
            );
            return List.of();
        }
        
        return List.of(build(modelDescriptor, ctx));
    }

    /**
     * Generates the repository interface for the given model descriptor.
     * This method creates a JPA repository interface with the necessary
     * annotations and type parameters.
     *
     * @param modelDescriptor the model descriptor containing metadata about the entity
     * @param ctx shared write context
     * @return a JavaFile representing the generated repository interface
     */
    @Override
    public JavaFile build(ModelDescriptor modelDescriptor, WriteContext ctx) {
        ctx.env().getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Generating repository for " + modelDescriptor.getName()
                        + " in package " + modelDescriptor.getBasePackage()
        );

        String entityPackage = modelDescriptor.getPackageName();
        String modelName = modelDescriptor.getName();
        var meta = StubGeneratorUtil.stubMeta(
                modelDescriptor,
                "repository",
                REPOSITORY_CAMEL,
                REPOSITORY_CAMEL,
                this.getClass()
        );
        String repoPackage = meta.pkg();
        String repoName = meta.name();
        String header = meta.header();

        ClassName modelClass = JavaPoetUtils.getClassName(entityPackage, modelName);
        ClassName uuidClass  = ClassName.get(UUID.class);
        ClassName jpaRepo    = JavaPoetUtils.getClassName(
                "org.springframework.data.jpa.repository", "JpaRepository");
        ClassName specExec   = JavaPoetUtils.getClassName(
                "org.springframework.data.jpa.repository", "JpaSpecificationExecutor");
        ClassName querydslExec = JavaPoetUtils.getClassName(
                "org.springframework.data.querydsl", "QuerydslPredicateExecutor");
        ClassName repoAnn    = JavaPoetUtils.getClassName(
                "org.springframework.stereotype", REPOSITORY_CAMEL);

        TypeName super1 = ParameterizedTypeName.get(jpaRepo, modelClass, uuidClass);
        TypeName super2 = ParameterizedTypeName.get(specExec, modelClass);
        TypeName super3 = ParameterizedTypeName.get(querydslExec, modelClass);

        TypeSpec repository = TypeSpec.interfaceBuilder(repoName)
                .addJavadoc(header)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(repoAnn)
                .addSuperinterface(super1)
                .addSuperinterface(super2)
                .addSuperinterface(super3)
                .build();

        return JavaFile.builder(repoPackage, repository)
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    @Override
    public boolean requiresCrudEntity() {
        return true;
    }

    @Override
    public int order() {
        return 1;
    }
}

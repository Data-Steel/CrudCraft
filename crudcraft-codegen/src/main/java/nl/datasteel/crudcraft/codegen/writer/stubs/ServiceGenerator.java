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

package nl.datasteel.crudcraft.codegen.writer.stubs;

// CHECKSTYLE.SUPPRESS: LineLength for +1000 lines

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.RowScope;
import nl.datasteel.crudcraft.codegen.util.JavaPoetUtils;
import nl.datasteel.crudcraft.codegen.util.ModelIdTypeResolver;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;
import nl.datasteel.crudcraft.codegen.writer.Generator;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;


/**
 * Generates a service stub for the given model descriptor, with hooks to fix and clear
 * bidirectional links.
 */
public class ServiceGenerator implements StubGenerator {

    /** Creates a service generator. */
    public ServiceGenerator() {
        // Constructor without any parameters stays empty
    }

    @Override
    public List<JavaFile> generate(ModelDescriptor modelDescriptor, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(modelDescriptor, ctx)) {
            return List.of();
        }

        // Skip service generation for abstract classes
        if (modelDescriptor.isAbstract()) {
            ctx.env()
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.NOTE,
                            "Skipping service generation for abstract entity: "
                                    + modelDescriptor.getName());
            return List.of();
        }

        return List.of(build(modelDescriptor, ctx));
    }

    /** Generates the service class for the given model descriptor. */
    @Override
    public JavaFile build(ModelDescriptor modelDescriptor, WriteContext ctx) {
        ctx.env()
                .getMessager()
                .printMessage(
                        Diagnostic.Kind.NOTE,
                        "Generating service for "
                                + modelDescriptor.getName()
                                + " in package "
                                + modelDescriptor.getBasePackage());

        String entityPackage = modelDescriptor.getPackageName();

        String modelName = modelDescriptor.getName();
        var meta =
                StubGeneratorUtil.stubMeta(
                        modelDescriptor, "service", "Service", "Service", this.getClass());
        String servicePackage = meta.pkg();

        String serviceName = meta.name();
        String header = meta.header();

        ClassName entity = JavaPoetUtils.getClassName(entityPackage, modelName);
        ClassName reqDto =
                JavaPoetUtils.getClassName(
                        modelDescriptor.getPackageName() + ".dto.request",
                        modelName + "RequestDto");
        ClassName respDto =
                JavaPoetUtils.getClassName(
                        modelDescriptor.getPackageName() + ".dto.response",
                        modelName + "ResponseDto");
        ClassName refDto =
                JavaPoetUtils.getClassName(
                        modelDescriptor.getPackageName() + ".dto.ref", modelName + "Ref");
        TypeName idType = ModelIdTypeResolver.resolveModelIdType(modelDescriptor).box();
        ClassName absSvc =
                JavaPoetUtils.getClassName(
                        "nl.datasteel.crudcraft.runtime.service", "AbstractCrudService");
        ClassName svcAnn = JavaPoetUtils.getClassName("org.springframework.stereotype", "Service");
        ClassName repoCls =
                JavaPoetUtils.getClassName(
                        modelDescriptor.getBasePackage() + ".repository", modelName + "Repository");
        ClassName mapCls =
                JavaPoetUtils.getClassName(
                        modelDescriptor.getBasePackage() + ".mapper", modelName + "Mapper");
        ClassName relationshipMeta =
                JavaPoetUtils.getClassName(
                        modelDescriptor.getPackageName() + ".meta",
                        modelName + "RelationshipMeta");

        // Build the stub class
        TypeSpec.Builder b =
                TypeSpec.classBuilder(serviceName)
                        .addJavadoc(header)
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(svcAnn)
                        .superclass(
                                ParameterizedTypeName.get(
                                        absSvc, entity, reqDto, respDto, refDto, idType))

                        // Add the constructor
                        .addMethod(
                                constructor(
                                        repoCls, mapCls, entity, respDto, refDto, modelDescriptor))
                        .addMethod(postSave(entity, relationshipMeta))
                        .addMethod(preDelete(entity, relationshipMeta));

        addRowSecurityHandlers(b, modelDescriptor, entity, reqDto);

        // Build the JavaFile
        return JavaPoetUtils.javaFile(servicePackage, b.build())
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .build();
    }

    private MethodSpec constructor(
            ClassName repoCls,
            ClassName mapCls,
            ClassName entity,
            ClassName respDto,
            ClassName refDto,
            ModelDescriptor modelDescriptor) {
        MethodSpec.Builder b =
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(repoCls, "repository")
                        .addParameter(mapCls, "mapper");

        var handlers = modelDescriptor.getRowSecurityHandlers();
        var scopes = modelDescriptor.getRowScopes();
        List<String> rowSecurityParameters = new ArrayList<>();
        int handlerIndex = 0;
        for (String handler : handlers) {
            ClassName handlerCls = ClassName.bestGuess(handler);
            String parameterName = "rowSecurity" + handlerIndex;
            b.addParameter(handlerCls, parameterName);
            rowSecurityParameters.add(parameterName);
            handlerIndex++;
        }
        if (!scopes.isEmpty()) {
            b.addParameter(
                    ClassName.bestGuess(
                            "nl.datasteel.crudcraft.runtime.security.scope.PrincipalScopeAccessor"),
                    "principalScopeAccessor");
        }

        b.addStatement(
                "super(repository, mapper, $T.class, $T.class, $T.class)", entity, respDto, refDto);

        if (!handlers.isEmpty() || !scopes.isEmpty()) {
            b.addStatement(
                    "$T rowSecurityHandlerList = new $T<>()",
                    ParameterizedTypeName.get(
                            ClassName.get(List.class),
                            ParameterizedTypeName.get(
                                    ClassName.get(RowSecurityHandler.class), entity)),
                    ArrayList.class);
            for (String parameterName : rowSecurityParameters) {
                b.addStatement("rowSecurityHandlerList.add($L)", parameterName);
            }
            for (RowScope scope : scopes) {
                b.addStatement(
                        "rowSecurityHandlerList.add(new $T<>($S, $S, $S, principalScopeAccessor))",
                        ClassName.bestGuess(
                                "nl.datasteel.crudcraft.runtime.security.row.ClaimScopedRowSecurityHandler"),
                        scope.kind().name().toLowerCase(),
                        scope.field(),
                        scope.claim());
            }
            b.addStatement(
                    "this.runtimeExtensions = $T.of(new $T<>(rowSecurityHandlerList))",
                    List.class,
                    ClassName.bestGuess(
                            "nl.datasteel.crudcraft.runtime.security.row.RowSecurityRuntimeExtension"));
        }

        return b.build();
    }

    private MethodSpec postSave(ClassName entity, ClassName relationshipMeta) {
        return MethodSpec.methodBuilder("postSave")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(entity, "entity")
                .addStatement("$T.fix(entity)", relationshipMeta)
                .build();
    }

    private MethodSpec preDelete(ClassName entity, ClassName relationshipMeta) {
        return MethodSpec.methodBuilder("preDelete")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(entity, "entity")
                .addStatement("$T.clear(entity)", relationshipMeta)
                .build();
    }

    private void addRowSecurityHandlers(
            TypeSpec.Builder b,
            ModelDescriptor modelDescriptor,
            ClassName entity,
            ClassName reqDto) {
        var handlers = modelDescriptor.getRowSecurityHandlers();
        var scopes = modelDescriptor.getRowScopes();
        if ((handlers == null || handlers.isEmpty()) && (scopes == null || scopes.isEmpty())) {
            return;
        }

        ClassName extension =
                ClassName.get(
                        "nl.datasteel.crudcraft.runtime.service.extension", "CrudRuntimeExtension");
        TypeName extensionType = ParameterizedTypeName.get(extension, entity, reqDto);
        ParameterizedTypeName listType =
                ParameterizedTypeName.get(ClassName.get(List.class), extensionType);

        b.addField(
                FieldSpec.builder(listType, "runtimeExtensions", Modifier.PRIVATE, Modifier.FINAL)
                        .build());

        b.addMethod(
                MethodSpec.methodBuilder("runtimeExtensions")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PROTECTED)
                        .returns(listType)
                        .addStatement("return runtimeExtensions")
                        .build());
    }

    @Override
    public boolean requiresCrudEntity() {
        return true;
    }

    @Override
    public int order() {
        return 3;
    }
}

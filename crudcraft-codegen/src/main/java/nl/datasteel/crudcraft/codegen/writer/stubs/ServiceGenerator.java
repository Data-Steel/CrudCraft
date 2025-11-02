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
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.util.List;
import java.util.UUID;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.util.JavaPoetUtils;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;
import nl.datasteel.crudcraft.codegen.writer.Generator;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;

/**
 * Generates a service stub for the given model descriptor,
 * with hooks to fix and clear bidirectional links.
 */
public class ServiceGenerator implements StubGenerator {

    @Override
    public List<JavaFile> generate(ModelDescriptor modelDescriptor, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(modelDescriptor, ctx)) {
            return List.of();
        }
        
        // Skip service generation for abstract classes
        if (modelDescriptor.isAbstract()) {
            ctx.env().getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "Skipping service generation for abstract entity: " + modelDescriptor.getName()
            );
            return List.of();
        }
        
        return List.of(build(modelDescriptor, ctx));
    }

    /**
     * Generates the service class for the given model descriptor.
     */
    @Override
    public JavaFile build(ModelDescriptor modelDescriptor, WriteContext ctx) {
        ctx.env().getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Generating service for " + modelDescriptor.getName()
                        + " in package " + modelDescriptor.getBasePackage()
        );

        String entityPackage = modelDescriptor.getPackageName();
        String modelName = modelDescriptor.getName();
        var meta = StubGeneratorUtil.stubMeta(
                modelDescriptor,
                "service",
                "Service",
                "Service",
                this.getClass()
        );
        String servicePackage = meta.pkg();
        String serviceName = meta.name();
        String header = meta.header();

        ClassName entity   = JavaPoetUtils.getClassName(entityPackage, modelName);
        ClassName reqDto   = JavaPoetUtils.getClassName(
                modelDescriptor.getPackageName() + ".dto.request", modelName + "RequestDto");
        ClassName respDto  = JavaPoetUtils.getClassName(
                modelDescriptor.getPackageName() + ".dto.response", modelName + "ResponseDto");
        ClassName refDto = JavaPoetUtils.getClassName(
                modelDescriptor.getPackageName() + ".dto.ref", modelName + "Ref");
        ClassName uuid     = ClassName.get(UUID.class);
        ClassName absSvc   = JavaPoetUtils.getClassName(
                "nl.datasteel.crudcraft.runtime.service", "AbstractCrudService");
        ClassName svcAnn   = JavaPoetUtils.getClassName("org.springframework.stereotype", "Service");
        ClassName repoCls  = JavaPoetUtils.getClassName(
                modelDescriptor.getBasePackage() + ".repository", modelName + "Repository");
        ClassName mapCls   = JavaPoetUtils.getClassName(
                modelDescriptor.getBasePackage() + ".mapper", modelName + "Mapper");
        ClassName relUtils = JavaPoetUtils.getClassName(
                "nl.datasteel.crudcraft.runtime.util", "RelationshipUtils");

        // Build the stub class
        TypeSpec.Builder b = TypeSpec.classBuilder(serviceName)
                .addJavadoc(header)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(svcAnn)
                .superclass(ParameterizedTypeName.get(absSvc, entity, reqDto, respDto, refDto,
                        uuid))

                // Add the constructor
                .addMethod(constructor(repoCls, mapCls, entity, respDto, refDto, modelDescriptor))
                .addMethod(postSave(entity, relUtils))
                .addMethod(preDelete(entity, relUtils));

        addRowSecurityHandlers(b, modelDescriptor);

        // Build the JavaFile
        return JavaFile.builder(servicePackage, b.build())
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    private MethodSpec constructor(
            ClassName repoCls,
            ClassName mapCls,
            ClassName entity,
            ClassName respDto,
            ClassName refDto,
            ModelDescriptor modelDescriptor
    ) {
        MethodSpec.Builder b = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(repoCls, "repository")
                .addParameter(mapCls, "mapper");

        var handlers = modelDescriptor.getRowSecurityHandlers();
        for (int i = 0; i < handlers.size(); i++) {
            ClassName handlerCls = ClassName.bestGuess(handlers.get(i));
            b.addParameter(handlerCls, "rowSecurity" + i);
        }

        b.addStatement("super(repository, mapper, $T.class, $T.class, $T.class)",
                entity, respDto, refDto);

        if (!handlers.isEmpty()) {
            String params = java.util.stream.IntStream.range(0, handlers.size())
                    .mapToObj(i -> "rowSecurity" + i)
                    .collect(java.util.stream.Collectors.joining(", "));
            ClassName rsh = ClassName.get(RowSecurityHandler.class);
            TypeName rshType = ParameterizedTypeName.get(rsh, WildcardTypeName.subtypeOf(Object.class));
            b.addStatement("this.rowSecurityHandlers = $T.<$T>of(" + params + ")",
                    List.class, rshType);
        }

        return b.build();
    }

    private MethodSpec postSave(ClassName entity, ClassName relUtils) {
        return MethodSpec.methodBuilder("postSave")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(entity, "entity")
                .addStatement("$T.fixBidirectional(entity)", relUtils)
                .build();
    }

    private MethodSpec preDelete(ClassName entity, ClassName relUtils) {
        return MethodSpec.methodBuilder("preDelete")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(entity, "entity")
                .addStatement("$T.clearBidirectional(entity)", relUtils)
                .build();
    }

    private void addRowSecurityHandlers(TypeSpec.Builder b, ModelDescriptor modelDescriptor) {
        var handlers = modelDescriptor.getRowSecurityHandlers();
        if (handlers == null || handlers.isEmpty()) {
            return;
        }

        ClassName rsh = ClassName.get(RowSecurityHandler.class);
        ClassName list = ClassName.get(List.class);
        TypeName rshType = ParameterizedTypeName.get(rsh, WildcardTypeName.subtypeOf(Object.class));
        ParameterizedTypeName listType = ParameterizedTypeName.get(list, rshType);

        b.addField(FieldSpec.builder(listType, "rowSecurityHandlers", Modifier.PRIVATE, Modifier.FINAL).build());

        b.addMethod(MethodSpec.methodBuilder("rowSecurityHandlers")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .returns(listType)
                .addStatement("return rowSecurityHandlers")
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

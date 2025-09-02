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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.FilerException;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.util.JavaPoetUtils;
import nl.datasteel.crudcraft.codegen.util.Pluralizer;
import nl.datasteel.crudcraft.codegen.util.StringCase;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;
import nl.datasteel.crudcraft.codegen.writer.Generator;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import nl.datasteel.crudcraft.codegen.writer.controller.ControllerEndpoints;
import nl.datasteel.crudcraft.codegen.writer.controller.ControllerMethodGenerator;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;

/**
 * Generates REST controllers directly from templates without relying on an abstract base class.
 */
public class ControllerGenerator implements StubGenerator {

    @Override
    public JavaFile build(ModelDescriptor modelDescriptor, WriteContext ctx) {
        EndpointContext epCtx = resolveEndpoints(modelDescriptor, ctx);
        return build(modelDescriptor, ctx, epCtx);
    }

    private JavaFile build(ModelDescriptor modelDescriptor, WriteContext ctx, EndpointContext epCtx) {
        ctx.env().getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Generating controller for " + modelDescriptor.getName()
                        + " in package " + modelDescriptor.getBasePackage()
        );

        String modelName = modelDescriptor.getName();
        var meta = StubGeneratorUtil.stubMeta(
                modelDescriptor,
                "controller",
                "Controller",
                "Controller",
                this.getClass()
        );
        String controllerPackage = meta.pkg();
        String controllerName = meta.name();
        String header = meta.header();
        String path = "/" + Pluralizer.pluralize(StringCase.CAMEL.apply(modelName));

        ClassName restCtrl = JavaPoetUtils.getClassName(
                "org.springframework.web.bind.annotation", "RestController");
        ClassName reqMap = JavaPoetUtils.getClassName(
                "org.springframework.web.bind.annotation", "RequestMapping");
        ClassName svcClass = ClassName.get(modelDescriptor.getBasePackage() + ".service", modelName + "Service");
        ClassName valueAnn = JavaPoetUtils.getClassName("org.springframework.beans.factory.annotation", "Value");
        ClassName pageableClass = JavaPoetUtils.getClassName("org.springframework.data.domain", "Pageable");
        ClassName pageRequest = JavaPoetUtils.getClassName("org.springframework.data.domain", "PageRequest");

        FieldSpec serviceField = FieldSpec.builder(svcClass, "service", Modifier.PRIVATE, Modifier.FINAL).build();
        FieldSpec maxPageSize = FieldSpec.builder(int.class, "maxPageSize", Modifier.PROTECTED)
                .addAnnotation(AnnotationSpec.builder(valueAnn)
                        .addMember("value", "$S", "${crudcraft.api.max-page-size:100}")
                        .build())
                .build();
        FieldSpec maxCsvRows = FieldSpec.builder(int.class, "maxCsvRows", Modifier.PROTECTED)
                .addAnnotation(AnnotationSpec.builder(valueAnn)
                        .addMember("value", "$S", "${crudcraft.export.max-csv-rows:100000}")
                        .build())
                .build();
        FieldSpec maxJsonRows = FieldSpec.builder(int.class, "maxJsonRows", Modifier.PROTECTED)
                .addAnnotation(AnnotationSpec.builder(valueAnn)
                        .addMember("value", "$S", "${crudcraft.export.max-json-rows:50000}")
                        .build())
                .build();
        FieldSpec maxXlsxRows = FieldSpec.builder(int.class, "maxXlsxRows", Modifier.PROTECTED)
                .addAnnotation(AnnotationSpec.builder(valueAnn)
                        .addMember("value", "$S", "${crudcraft.export.max-xlsx-rows:25000}")
                        .build())
                .build();

        MethodSpec ctor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(svcClass, "service")
                .addStatement("this.service = service")
                .build();

        MethodSpec clampPageable = MethodSpec.methodBuilder("clampPageable")
                .addModifiers(Modifier.PRIVATE)
                .returns(pageableClass)
                .addParameter(pageableClass, "pageable")
                .beginControlFlow("if (pageable == null)")
                .addStatement("return $T.of(0, maxPageSize)", pageRequest)
                .endControlFlow()
                .addStatement("int size = Math.min(pageable.getPageSize(), maxPageSize)")
                .addStatement("return $T.of(pageable.getPageNumber(), size, pageable.getSort())", pageRequest)
                .build();

        TypeSpec.Builder builder = TypeSpec.classBuilder(controllerName)
                .addJavadoc(header)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(restCtrl)
                .addAnnotation(AnnotationSpec.builder(reqMap)
                        .addMember("value", "$S", path)
                        .build())
                .addField(serviceField)
                .addField(maxPageSize)
                .addField(maxCsvRows)
                .addField(maxJsonRows)
                .addField(maxXlsxRows)
                .addMethod(ctor)
                .addMethod(clampPageable);

        ControllerMethodGenerator methodGen = new ControllerMethodGenerator();
        for (CrudEndpoint ep : epCtx.allowed()) {
            EndpointSpec raw = epCtx.specs().get(ep);
            if (raw != null) {
                String alias = canonicalMethodName(ep, raw.methodName());
                EndpointSpec spec = withMethodName(raw, alias);
                builder.addMethod(methodGen.generate(spec, modelDescriptor, epCtx.secPol()));
            }
        }

        TypeSpec controller = builder.build();
        return JavaFile.builder(controllerPackage, controller)
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    @Override
    public void write(ModelDescriptor modelDescriptor, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(modelDescriptor, ctx)) {
            return;
        }
        EndpointContext epCtx = resolveEndpoints(modelDescriptor, ctx);
        JavaFile javaFile = build(modelDescriptor, ctx, epCtx);
        String code = javaFile.toString();
        if (modelDescriptor.isEditable() && !epCtx.disabled().isEmpty()) {
            ControllerMethodGenerator methodGen = new ControllerMethodGenerator();
            StringBuilder sb = new StringBuilder();
            sb.append('\n');
            for (CrudEndpoint ep : epCtx.disabled()) {
                EndpointSpec spec = epCtx.specs().get(ep);
                if (spec != null) {
                    MethodSpec m = methodGen.generate(spec, modelDescriptor, epCtx.secPol());
                    sb.append("    /*\n");
                    sb.append("    Endpoint omitted by generation template (+ include/exclude). Since this stub is editable, it is commented out, so it can easily be added later.\n");
                    for (String line : m.toString().split("\n")) {
                        sb.append("    ").append(line).append('\n');
                    }
                    sb.append("    */\n\n");
                }
            }
            int insert = code.lastIndexOf('}');
            code = code.substring(0, insert) + sb + "}\n";
        }
        try {
            JavaFileObject file = ctx.env().getFiler()
                    .createSourceFile(javaFile.packageName + "." + javaFile.typeSpec.name);
            try (Writer writer = file.openWriter()) {
                writer.write(code);
            }
        } catch (FilerException e) {
            ctx.env().getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Skipping generation of existing type "
                            + javaFile.packageName + "." + javaFile.typeSpec.name);
        } catch (IOException e) {
            ctx.env().getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to write " + javaFile.packageName + "." + javaFile.typeSpec.name + ": "
                            + e.getMessage());
        }
    }

    private EndpointContext resolveEndpoints(ModelDescriptor modelDescriptor, WriteContext ctx) {
        Set<CrudEndpoint> allowed;
        String modelName = modelDescriptor.getName();
        if (modelDescriptor.getEndpointPolicy() == CrudTemplate.class) {
            allowed = new HashSet<>(modelDescriptor.getTemplate().resolveEndpoints());
        } else {
            try {
                CrudEndpointPolicy policy = modelDescriptor.getEndpointPolicy()
                        .getDeclaredConstructor().newInstance();
                allowed = new HashSet<>(policy.resolveEndpoints());
            } catch (Exception e) {
                ctx.env().getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Could not instantiate policy for " + modelName + ": " + e.getMessage());
                throw new IllegalStateException("Failed to instantiate endpoint policy for " + modelName, e);
            }
        }
        Arrays.asList(modelDescriptor.getOmitEndpoints()).forEach(allowed::remove);
        allowed.addAll(Arrays.asList(modelDescriptor.getIncludeEndpoints()));

        Map<CrudEndpoint, EndpointSpec> specs = ControllerEndpoints.defaults(modelDescriptor);

        Set<CrudEndpoint> disabled = new HashSet<>(specs.keySet());
        disabled.removeAll(allowed);

        CrudSecurityPolicy secPol = null;
        if (modelDescriptor.isSecure()) {
            try {
                secPol = modelDescriptor.getSecurityPolicy().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                ctx.env().getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Could not instantiate security policy for " + modelName + ": " + e.getMessage());
                throw new IllegalStateException("Failed to instantiate security policy for " + modelName, e);
            }
        }

        return new EndpointContext(specs, allowed, disabled, secPol);
    }

    private static String canonicalMethodName(CrudEndpoint ep, String current) {
        return switch (ep) {
            case GET_ONE -> "getOne";
            case GET_ALL -> "getAll";
            case POST    -> "post";
            default      -> current;
        };
    }

    private static EndpointSpec withMethodName(EndpointSpec s, String name) {
        if (name.equals(s.methodName())) return s;
        return new EndpointSpec(
                s.endpoint(),
                name,
                s.mapping(),
                s.returnType(),
                s.params(),
                s.body()
        );
    }

    private record EndpointContext(
            Map<CrudEndpoint, EndpointSpec> specs,
            Set<CrudEndpoint> allowed,
            Set<CrudEndpoint> disabled,
            CrudSecurityPolicy secPol) {
    }

    @Override
    public boolean requiresCrudEntity() {
        return true;
    }

    @Override
    public int order() {
        return 4;
    }
}

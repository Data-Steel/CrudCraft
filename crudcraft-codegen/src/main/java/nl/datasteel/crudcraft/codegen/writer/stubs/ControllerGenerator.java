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

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
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
import nl.datasteel.crudcraft.codegen.reader.model.ResolvedCrudSecurityPolicy;
import nl.datasteel.crudcraft.codegen.util.JavaPoetUtils;
import nl.datasteel.crudcraft.codegen.util.Pluralizer;
import nl.datasteel.crudcraft.codegen.util.StringCase;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;
import nl.datasteel.crudcraft.codegen.writer.Generator;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import nl.datasteel.crudcraft.codegen.writer.controller.ControllerEndpoints;
import nl.datasteel.crudcraft.codegen.writer.controller.ControllerMethodGenerator;
import nl.datasteel.crudcraft.codegen.writer.controller.EndpointSpec;
import nl.datasteel.crudcraft.codegen.writer.controller.endpoints.EndpointSupport;


/** Generates REST controllers directly from templates without relying on an abstract base class. */
public class ControllerGenerator implements StubGenerator {
    private static final ClassName SUPPRESS_FB_WARNINGS =
            ClassName.get("edu.umd.cs.findbugs.annotations", "SuppressFBWarnings");

    /** Creates a controller generator. */
    public ControllerGenerator() {}

    @Override
    public List<JavaFile> generate(ModelDescriptor modelDescriptor, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(modelDescriptor, ctx)) {
            return List.of();
        }

        // Skip controller generation for abstract classes
        if (modelDescriptor.isAbstract()) {
            ctx.env()
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.NOTE,
                            "Skipping controller generation for abstract entity: "
                                    + modelDescriptor.getName());
            return List.of();
        }

        return List.of(build(modelDescriptor, ctx));
    }

    @Override
    public JavaFile build(ModelDescriptor modelDescriptor, WriteContext ctx) {
        EndpointContext epCtx = resolveEndpoints(modelDescriptor, ctx);
        return build(modelDescriptor, ctx, epCtx);
    }

    private JavaFile build(
            ModelDescriptor modelDescriptor, WriteContext ctx, EndpointContext epCtx) {
        ctx.env()
                .getMessager()
                .printMessage(
                        Diagnostic.Kind.NOTE,
                        "Generating controller for "
                                + modelDescriptor.getName()
                                + " in package "
                                + modelDescriptor.getBasePackage());

        boolean exportEnabled = epCtx.allowed().contains(CrudEndpoint.EXPORT);
        boolean searchEnabled = EndpointSupport.hasSearchFields(modelDescriptor);

        String modelName = modelDescriptor.getName();
        var meta =
                StubGeneratorUtil.stubMeta(
                        modelDescriptor, "controller", "Controller", "Controller", this.getClass());

        String controllerName = meta.name();
        final String header = meta.header();
        final String path = "/" + Pluralizer.pluralize(StringCase.CAMEL.apply(modelName));

        final ClassName restCtrl =
                JavaPoetUtils.getClassName(
                        "org.springframework.web.bind.annotation", "RestController");
        final ClassName reqMap =
                JavaPoetUtils.getClassName(
                        "org.springframework.web.bind.annotation", "RequestMapping");
        ClassName svcClass =
                ClassName.get(modelDescriptor.getBasePackage() + ".service", modelName + "Service");
        ClassName valueAnn =
                JavaPoetUtils.getClassName("org.springframework.beans.factory.annotation", "Value");
        ClassName objectProvider =
                JavaPoetUtils.getClassName("org.springframework.beans.factory", "ObjectProvider");
        ClassName meterRegistry =
                JavaPoetUtils.getClassName("io.micrometer.core.instrument", "MeterRegistry");
        final ClassName timer =
                JavaPoetUtils.getClassName("io.micrometer.core.instrument", "Timer");
        ClassName logger = JavaPoetUtils.getClassName("org.slf4j", "Logger");
        ClassName loggerFactory = JavaPoetUtils.getClassName("org.slf4j", "LoggerFactory");
        final ClassName pageableClass =
                JavaPoetUtils.getClassName("org.springframework.data.domain", "Pageable");
        final ClassName pageRequest =
                JavaPoetUtils.getClassName("org.springframework.data.domain", "PageRequest");
        ClassName exportServiceClass =
                exportEnabled
                        ? JavaPoetUtils.getClassName(
                                "nl.datasteel.crudcraft.runtime.export.service",
                                "EnhancedExportService")
                        : null;
        ClassName exportServiceFactoryClass =
                exportEnabled
                        ? JavaPoetUtils.getClassName(
                                "nl.datasteel.crudcraft.runtime.export.service",
                                "EnhancedExportServiceFactory")
                        : null;
        ClassName entityClass =
                exportEnabled
                        ? ClassName.get(modelDescriptor.getPackageName(), modelName)
                        : null;
        ClassName dtoRespClass =
                exportEnabled
                        ? ClassName.get(
                                modelDescriptor.getPackageName() + ".dto.response",
                                modelName + "ResponseDto")
                        : null;
        final ClassName specificationClass =
                exportEnabled
                        ? JavaPoetUtils.getClassName(
                                "org.springframework.data.jpa.domain", "Specification")
                        : null;
        final ClassName crudQueryOperationsClass =
                exportEnabled
                        ? JavaPoetUtils.getClassName(
                                "nl.datasteel.crudcraft.runtime.service", "CrudQueryOperations")
                        : null;
        ClassName searchReqClass =
                exportEnabled && searchEnabled
                        ? ClassName.get(
                                modelDescriptor.getPackageName() + ".search",
                                modelName + "SearchRequest")
                        : null;

        final FieldSpec serviceField =
                FieldSpec.builder(svcClass, "service", Modifier.PRIVATE, Modifier.FINAL)
                        .addAnnotation(exposeWarningsSuppression())
                        .build();
        final FieldSpec loggerField =
                FieldSpec.builder(logger, "LOG", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$T.getLogger($L.class)", loggerFactory, controllerName)
                        .build();
        final FieldSpec meterRegistryField =
                FieldSpec.builder(meterRegistry, "meterRegistry", Modifier.PRIVATE, Modifier.FINAL)
                        .build();
        final FieldSpec exportServiceField =
                exportEnabled
                        ? FieldSpec.builder(
                                        ParameterizedTypeName.get(
                                                exportServiceClass,
                                                dtoRespClass,
                                                entityClass,
                                                searchEnabled
                                                        ? searchReqClass
                                                        : ClassName.get(Object.class)),
                                        "exportService",
                                        Modifier.PRIVATE,
                                        Modifier.FINAL)
                                .build()
                        : null;
        final FieldSpec maxPageSize =
                FieldSpec.builder(int.class, "maxPageSize", Modifier.PROTECTED).build();
        final FieldSpec maxCsvRows =
                FieldSpec.builder(int.class, "maxCsvRows", Modifier.PROTECTED).build();
        final FieldSpec maxRows =
                FieldSpec.builder(int.class, "maxRows", Modifier.PROTECTED).build();
        final FieldSpec maxJsonRows =
                FieldSpec.builder(int.class, "maxJsonRows", Modifier.PROTECTED).build();
        final FieldSpec maxXlsxRows =
                FieldSpec.builder(int.class, "maxXlsxRows", Modifier.PROTECTED).build();
        final FieldSpec maxDepth =
                FieldSpec.builder(int.class, "maxDepth", Modifier.PROTECTED).build();

        MethodSpec.Builder ctorBuilder =
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(svcClass, "service")
                        .addParameter(
                                ParameterizedTypeName.get(objectProvider, meterRegistry),
                                "meterRegistry")
                        .addParameter(
                                ParameterSpec.builder(int.class, "maxPageSize")
                                        .addAnnotation(
                                                AnnotationSpec.builder(valueAnn)
                                                        .addMember(
                                                                "value",
                                                                "$S",
                                                        "${crudcraft.api.max-page-size:100}")
                                                        .build())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(int.class, "maxRows")
                                        .addAnnotation(
                                                AnnotationSpec.builder(valueAnn)
                                                        .addMember(
                                                                "value",
                                                                "$S",
                                                                "${crudcraft.export.max-rows:-1}")
                                                        .build())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(int.class, "maxCsvRows")
                                        .addAnnotation(
                                                AnnotationSpec.builder(valueAnn)
                                                        .addMember(
                                                                "value",
                                                                "$S",
                                                                "$"
                                                                        + "{crudcraft.export."
                                                                        + "max-csv-rows:100000}")
                                                        .build())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(int.class, "maxJsonRows")
                                        .addAnnotation(
                                                AnnotationSpec.builder(valueAnn)
                                                        .addMember(
                                                                "value",
                                                                "$S",
                                                                "$"
                                                                        + "{crudcraft.export."
                                                                        + "max-json-rows:50000}")
                                                        .build())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(int.class, "maxXlsxRows")
                                        .addAnnotation(
                                                AnnotationSpec.builder(valueAnn)
                                                        .addMember(
                                                                "value",
                                                                "$S",
                                                                "$"
                                                                        + "{crudcraft.export."
                                                                        + "max-xlsx-rows:25000}")
                                                        .build())
                        .build())
                        .addParameter(
                                ParameterSpec.builder(int.class, "maxDepth")
                                        .addAnnotation(
                                                AnnotationSpec.builder(valueAnn)
                                                        .addMember(
                                                                "value",
                                                                "$S",
                                                                "$"
                                                                        + "{crudcraft.export."
                                                                        + "max-depth:5}")
                                                        .build())
                                        .build())
                        .beginControlFlow("if (maxPageSize <= 0)")
                        .addStatement(
                                "throw new $T($S + maxPageSize)",
                                IllegalArgumentException.class,
                                "crudcraft.api.max-page-size must be positive; got ")
                        .endControlFlow()
                        .addStatement("this.service = service")
                        .addStatement("this.meterRegistry = resolveMeterRegistry(meterRegistry)")
                        .addStatement("this.maxPageSize = maxPageSize")
                        .addStatement("this.maxRows = maxRows")
                        .addStatement("this.maxCsvRows = maxCsvRows")
                        .addStatement("this.maxJsonRows = maxJsonRows")
                        .addStatement("this.maxXlsxRows = maxXlsxRows")
                        .addStatement("this.maxDepth = maxDepth");
        ctorBuilder.addAnnotation(constructorThrowSuppression());
        if (exportEnabled) {
            ctorBuilder.addParameter(
                    ParameterizedTypeName.get(objectProvider, exportServiceFactoryClass),
                    "exportServiceFactoryProvider");
        }
        if (exportEnabled) {
            ctorBuilder.addStatement(
                    "this.exportService = createExportService(exportServiceFactoryProvider,"
                            + " maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize,"
                            + " $T.class)",
                    entityClass);
        }
        MethodSpec ctor = ctorBuilder.build();

        MethodSpec clampPageable =
                MethodSpec.methodBuilder("clampPageable")
                        .addModifiers(Modifier.PRIVATE)
                        .returns(pageableClass)
                        .addParameter(pageableClass, "pageable")
                        .beginControlFlow("if (pageable == null)")
                        .addStatement("return $T.of(0, maxPageSize)", pageRequest)
                        .endControlFlow()
                        .addStatement(
                                "int size = Math.clamp(pageable.getPageSize(), 1, maxPageSize)")
                        .addStatement(
                                "return $T.of(pageable.getPageNumber(), size, pageable.getSort())",
                                pageRequest)
                        .build();

        MethodSpec resolveMeterRegistry =
                MethodSpec.methodBuilder("resolveMeterRegistry")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                        .returns(meterRegistry)
                        .addParameter(
                                ParameterizedTypeName.get(objectProvider, meterRegistry),
                                "provider")
                        .beginControlFlow("if (provider == null)")
                        .addStatement("return null")
                        .endControlFlow()
                        .beginControlFlow("try")
                        .addStatement("return provider.getIfAvailable()")
                        .nextControlFlow("catch ($T ex)", RuntimeException.class)
                        .addStatement(
                                "LOG.debug($S, ex)",
                                "Micrometer registry lookup failed; generated controller metrics"
                                        + " disabled")
                        .addStatement("return null")
                        .endControlFlow()
                        .build();

        MethodSpec recordOperation =
                MethodSpec.methodBuilder("recordOperation")
                        .addModifiers(Modifier.PRIVATE)
                        .addParameter(String.class, "operation")
                        .addParameter(String.class, "outcome")
                        .addParameter(long.class, "started")
                        .addStatement("long durationNanos = $T.nanoTime() - started", System.class)
                        .addStatement(
                                "LOG.info($S, $S, operation, outcome,"
                                    + " $T.NANOSECONDS.toMillis(durationNanos))",
                                "crudcraft.generated.operation model={} operation={} outcome={}"
                                        + " duration_ms={}",
                                modelName,
                                ClassName.get("java.util.concurrent", "TimeUnit"))
                        .beginControlFlow("if (meterRegistry != null)")
                        .addStatement(
                                "$T.builder($S).tag($S, $S).tag($S, operation).tag($S,"
                                    + " outcome).register(meterRegistry).record(durationNanos,"
                                    + " $T.NANOSECONDS)",
                                timer,
                                "crudcraft.generated.operation",
                                "model",
                                modelName,
                                "operation",
                                "outcome",
                                ClassName.get("java.util.concurrent", "TimeUnit"))
                        .endControlFlow()
                        .build();

        MethodSpec createExportService =
                exportEnabled
                        ? MethodSpec.methodBuilder("createExportService")
                                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                .returns(exportServiceField.type())
                                .addParameter(
                                        ParameterizedTypeName.get(
                                                objectProvider, exportServiceFactoryClass),
                                        "provider")
                                .addParameter(int.class, "maxRows")
                                .addParameter(int.class, "maxCsvRows")
                                .addParameter(int.class, "maxJsonRows")
                                .addParameter(int.class, "maxXlsxRows")
                                .addParameter(int.class, "maxPageSize")
                                .addParameter(
                                        ParameterizedTypeName.get(
                                                ClassName.get(Class.class), entityClass),
                                        "entityType")
                                .addAnnotation(
                                        AnnotationSpec.builder(SuppressWarnings.class)
                                                .addMember("value", "$S", "unchecked")
                                                .build())
                                .beginControlFlow("if (provider != null)")
                                .addStatement(
                                        "$T factory = provider.getIfAvailable()",
                                        exportServiceFactoryClass)
                                .beginControlFlow("if (factory != null)")
                                .addStatement(
                                        "return factory.create(maxRows, maxCsvRows, maxJsonRows,"
                                                + " maxXlsxRows, maxPageSize, entityType)")
                                .endControlFlow()
                                .endControlFlow()
                                .addStatement(
                                        "return new $T<>(new $T(maxRows, maxCsvRows, maxJsonRows,"
                                                + " maxXlsxRows, maxPageSize), null,"
                                                + " entityType, false)",
                                        exportServiceClass,
                                        ClassName.get(
                                                "nl.datasteel.crudcraft.runtime.export.service",
                                                "ExportService",
                                                "ExportConfig"))
                                .build()
                        : null;
        MethodSpec effectiveReadSpecification =
                exportEnabled
                        ? MethodSpec.methodBuilder("effectiveReadSpecification")
                                .addModifiers(Modifier.PRIVATE)
                                .addParameter(Object.class, "searchRequest")
                                .returns(ParameterizedTypeName.get(specificationClass, entityClass))
                                .addAnnotation(
                                        AnnotationSpec.builder(SuppressWarnings.class)
                                                .addMember("value", "$S", "unchecked")
                                                .build())
                                .addStatement(
                                        "return (($T<$T, ?, ?>) service)"
                                                + ".effectiveReadSpecification(searchRequest)",
                                        crudQueryOperationsClass,
                                        entityClass)
                                .build()
                        : null;

        TypeSpec.Builder builder =
                TypeSpec.classBuilder(controllerName)
                        .addJavadoc(header)
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(restCtrl)
                        .addAnnotation(
                                AnnotationSpec.builder(reqMap)
                                        .addMember("value", "$S", path)
                                        .build())
                        .addField(loggerField)
                        .addField(serviceField)
                        .addField(meterRegistryField)
                        .addField(maxPageSize)
                        .addField(maxRows)
                        .addField(maxCsvRows)
                        .addField(maxJsonRows)
                        .addField(maxXlsxRows)
                        .addField(maxDepth)
                        .addMethod(ctor)
                        .addMethod(resolveMeterRegistry)
                        .addMethod(clampPageable)
                        .addMethod(recordOperation);
        if (exportEnabled) {
            builder.addField(exportServiceField);
            builder.addMethod(createExportService);
            builder.addMethod(effectiveReadSpecification);
        }

        ControllerMethodGenerator methodGen = new ControllerMethodGenerator();
        for (CrudEndpoint ep : epCtx.allowed()) {
            EndpointSpec raw = epCtx.specs().get(ep);
            if (raw != null) {
                String alias = canonicalMethodName(ep, raw.methodName());
                EndpointSpec spec = withMethodName(raw, alias);
                builder.addMethod(methodGen.generate(spec, modelDescriptor, epCtx.secPol()));
            }
        }

        // Generate specialized DTO endpoints (e.g., /list, /map)
        List<MethodSpec> specializedEndpoints =
                SpecializedDtoEndpoints.generate(modelDescriptor, epCtx.secPol());
        specializedEndpoints.forEach(builder::addMethod);

        TypeSpec controller = builder.build();
        return JavaPoetUtils.javaFile(meta.pkg(), controller)
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .build();
    }

    @Override
    public void write(ModelDescriptor modelDescriptor, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(modelDescriptor, ctx)) {
            return;
        }

        // Skip controller generation for abstract classes
        if (modelDescriptor.isAbstract()) {
            ctx.env()
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.NOTE,
                            "Skipping controller generation for abstract entity: "
                                    + modelDescriptor.getName());
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
                    sb.append(
                            "    Endpoint omitted by generation template (+ include/exclude). Since"
                                + " this stub is editable, it is commented out, so it can easily be"
                                + " added later.\n");
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
            JavaFileObject file =
                    ctx.env()
                            .getFiler()
                            .createSourceFile(
                                    javaFile.packageName() + "." + javaFile.typeSpec().name());
            try (Writer writer = file.openWriter()) {
                writer.write(code);
            }
        } catch (FilerException e) {
            ctx.env()
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.NOTE,
                            "Skipping generation of existing type "
                                    + javaFile.packageName()
                                    + "."
                                    + javaFile.typeSpec().name());
        } catch (IOException e) {
            ctx.env()
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.ERROR,
                            "Failed to write "
                                    + javaFile.packageName()
                                    + "."
                                    + javaFile.typeSpec().name()
                                    + ": "
                                    + e.getMessage());
        }
    }

    private EndpointContext resolveEndpoints(ModelDescriptor modelDescriptor, WriteContext ctx) {
        Set<CrudEndpoint> allowed;
        String modelName = modelDescriptor.getName();
        if (modelDescriptor.getEndpointPolicy() == CrudTemplate.class) {
            allowed = new LinkedHashSet<>(modelDescriptor.getTemplate().resolveEndpoints());
        } else {
            try {
                CrudEndpointPolicy policy =
                        modelDescriptor.getEndpointPolicy().getDeclaredConstructor().newInstance();
                allowed = new LinkedHashSet<>(policy.resolveEndpoints());
            } catch (Exception e) {
                ctx.env()
                        .getMessager()
                        .printMessage(
                                Diagnostic.Kind.ERROR,
                                "Could not instantiate policy for "
                                        + modelName
                                        + ": "
                                        + e.getMessage());
                throw new IllegalStateException(
                        "Failed to instantiate endpoint policy for " + modelName, e);
            }
        }
        Arrays.asList(modelDescriptor.getOmitEndpoints()).forEach(allowed::remove);
        allowed.addAll(Arrays.asList(modelDescriptor.getIncludeEndpoints()));
        if (EndpointSupport.hasSearchFields(modelDescriptor)) {
            allowed.add(CrudEndpoint.SEARCH);
        } else {
            allowed.remove(CrudEndpoint.SEARCH);
        }

        Map<CrudEndpoint, EndpointSpec> specs = ControllerEndpoints.defaults(modelDescriptor);

        Set<CrudEndpoint> disabled = new LinkedHashSet<>(specs.keySet());
        disabled.removeAll(allowed);

        CrudSecurityPolicy secPol = null;
        if (modelDescriptor.isSecure()) {
            if (modelDescriptor.hasEndpointExpressions()) {
                secPol = new ResolvedCrudSecurityPolicy(modelDescriptor.getEndpointExpressions());
            } else if (modelDescriptor.getSecurityPolicy() == CrudSecurityPolicy.class) {
                secPol = endpoint -> "isAuthenticated()";
            } else {
                try {
                    secPol =
                            modelDescriptor
                                    .getSecurityPolicy()
                                    .getDeclaredConstructor()
                                    .newInstance();
                } catch (Exception e) {
                    secPol = securityPolicyInstantiationFailure(ctx, modelName, e);
                }
            }
        }

        return new EndpointContext(specs, allowed, disabled, secPol);
    }

    private CrudSecurityPolicy securityPolicyInstantiationFailure(
            WriteContext ctx, String modelName, Exception cause) {
        ctx.env()
                .getMessager()
                .printMessage(
                        Diagnostic.Kind.ERROR,
                        "Could not instantiate security policy for "
                                + modelName
                                + ": "
                                + cause.getMessage());
        throw new IllegalStateException(
                "Failed to instantiate security policy for " + modelName, cause);
    }

    private static String canonicalMethodName(CrudEndpoint ep, String current) {
        return switch (ep) {
            case GET_ONE -> "getOne";
            case GET_ALL -> "getAll";
            case POST -> "post";
            default -> current;
        };
    }

    private static EndpointSpec withMethodName(EndpointSpec s, String name) {
        if (name.equals(s.methodName())) {
            return s;
        }
        return new EndpointSpec(
                s.endpoint(), name, s.mapping(), s.returnType(), s.params(), s.body());
    }

    private AnnotationSpec exposeWarningsSuppression() {
        return AnnotationSpec.builder(SUPPRESS_FB_WARNINGS)
                .addMember("value", "$S", "EI_EXPOSE_REP2")
                .build();
    }

    private AnnotationSpec constructorThrowSuppression() {
        return AnnotationSpec.builder(SUPPRESS_FB_WARNINGS)
                .addMember("value", "$S", "CT_CONSTRUCTOR_THROW")
                .addMember(
                        "justification",
                        "$S",
                        "Generated controllers may validate optional infrastructure during"
                                + " construction.")
                .build();
    }

    private record EndpointContext(
            Map<CrudEndpoint, EndpointSpec> specs,
            Set<CrudEndpoint> allowed,
            Set<CrudEndpoint> disabled,
            CrudSecurityPolicy secPol) {}

    @Override
    public boolean requiresCrudEntity() {
        return true;
    }

    @Override
    public int order() {
        return 4;
    }
}

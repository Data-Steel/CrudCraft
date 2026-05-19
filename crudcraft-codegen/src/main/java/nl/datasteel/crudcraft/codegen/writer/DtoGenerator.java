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

package nl.datasteel.crudcraft.codegen.writer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.lang.model.element.Modifier;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityMetadata;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.fileheader.ModelStrictHeader;
import nl.datasteel.crudcraft.codegen.util.JavaPoetUtils;
import nl.datasteel.crudcraft.codegen.util.StringCase;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;


/** Generates immutable request, response, ref, and additional response DTO records. */
public class DtoGenerator implements Generator {
    static final String GENERATE_WITHERS_OPTION = "crudcraft.dto.generateWithers";
    private static final ClassName JAKARTA_NULLABLE =
            ClassName.get("jakarta.annotation", "Nullable");
    private static final ClassName JAKARTA_NOT_NULL =
            ClassName.get("jakarta.validation.constraints", "NotNull");
    private static final ClassName JAKARTA_VALID =
            ClassName.get("jakarta.validation", "Valid");
    private static final String JAKARTA_NOT_NULL_NAME = "jakarta.validation.constraints.NotNull";
    private static final String JAVAX_NOT_NULL_NAME = "javax.validation.constraints.NotNull";

    /** Creates a DTO generator. */
    public DtoGenerator() {}

    @Override
    public List<JavaFile> generate(ModelDescriptor md, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(md, ctx)) {
            return List.of();
        }

        DtoFieldSelection selection = selectDtoFields(md);

        if (md.isAbstract()) {
            ctx.env()
                    .getMessager()
                    .printMessage(
                            javax.tools.Diagnostic.Kind.NOTE,
                            "Skipping Request and Response DTOs for abstract entity: "
                                    + md.getName()
                                    + ". Only generating Ref DTO.");
            return List.of(
                    generateDto(
                            DtoType.REF,
                            md,
                            selection.refFields(),
                            shouldGenerateWithers(ctx)));
        }

        List<JavaFile> all = new ArrayList<>();
        boolean generateWithers = shouldGenerateWithers(ctx);
        boolean requestWithers = generateWithers || md.hasLobFields();
        all.add(generateDto(DtoType.REQUEST, md, selection.requestFields(), requestWithers));
        all.add(generateDto(DtoType.RESPONSE, md, selection.dtoFields(), generateWithers));
        all.add(generateDto(DtoType.REF, md, selection.refFields(), generateWithers));

        for (String name : selection.responseNames()) {
            all.add(
                    generateAdditionalResponseDto(
                            md, name, fieldsForName(md, name), generateWithers));
        }

        return all;
    }

    private DtoFieldSelection selectDtoFields(ModelDescriptor md) {
        List<FieldDescriptor> dtoFields =
                md.getFields().stream().filter(FieldDescriptor::inDto).toList();
        List<FieldDescriptor> refFields = dtoFields.stream().filter(this::isRefField).toList();
        List<FieldDescriptor> requestFields =
                md.getFields().stream().filter(this::isRequestField).toList();
        Set<String> responseNames = new HashSet<>();
        for (FieldDescriptor fd : md.getFields()) {
            responseNames.addAll(Arrays.asList(fd.getResponseDtos()));
        }
        return new DtoFieldSelection(dtoFields, refFields, requestFields, responseNames);
    }

    private boolean isRefField(FieldDescriptor fd) {
        return (fd.inRef() || "id".equalsIgnoreCase(fd.getName())) && !fd.isLob();
    }

    private boolean isRequestField(FieldDescriptor fd) {
        return fd.inRequest()
                || (fd.inDto() && fd.getRelType() != RelationshipType.NONE && !fd.isEmbedded());
    }

    private List<FieldDescriptor> fieldsForName(ModelDescriptor md, String name) {
        return md.getFields().stream()
                .filter(
                        fd ->
                                Arrays.asList(fd.getResponseDtos()).contains(name)
                                        || "id".equalsIgnoreCase(fd.getName()))
                .toList();
    }

    @Override
    public int order() {
        return 0;
    }

    private boolean shouldGenerateWithers(WriteContext ctx) {
        if (ctx == null || ctx.env() == null || ctx.env().getOptions() == null) {
            return false;
        }
        String configured = ctx.env().getOptions().get(GENERATE_WITHERS_OPTION);
        return configured != null && Boolean.parseBoolean(configured);
    }

    private JavaFile generateDto(
            DtoType type, ModelDescriptor md, List<FieldDescriptor> fields, boolean withers) {
        String pkg = md.getPackageName() + type.packageSuffix();
        String className = md.getName() + type.classSuffix();
        TypeSpec.Builder builder =
                TypeSpec.recordBuilder(className)
                        .addJavadoc(
                                ModelStrictHeader.header(
                                        md.getName(), pkg, this.getClass().getSimpleName()))
                        .addJavadoc(dtoNullnessContract(type, md.getName()))
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(TemplateUtil.schema(type.schemaDescription(md.getName())))
                        .addAnnotation(jsonIncludeNonNull());
        TypeName modelIdType = resolveModelIdType(md);
        List<ResolvedField> components = resolveFields(type, fields, false, modelIdType);
        addRecordConstructor(builder, components);
        addArrayAccessors(builder, components);
        addFieldSecurityMetadata(builder, className, type, fields, modelIdType);
        if (withers) {
            addWithers(builder, pkg, className, components);
        }
        addBuilder(builder, pkg, className, components);
        return javaFile(pkg, builder);
    }

    private JavaFile generateAdditionalResponseDto(
            ModelDescriptor md, String name, List<FieldDescriptor> fields, boolean withers) {
        String pkg = md.getPackageName() + ".dto.response";
        String className = md.getName() + StringCase.PASCAL.apply(name) + "ResponseDto";
        TypeSpec.Builder builder =
                TypeSpec.recordBuilder(className)
                        .addJavadoc(
                                ModelStrictHeader.header(
                                        md.getName(), pkg, this.getClass().getSimpleName()))
                        .addJavadoc(
                                "\n<p>Nullness: response IDs are non-null after persistence; other"
                                        + " fields may be null when database values, projections,"
                                        + " or field-level security omit them.\n")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(
                                TemplateUtil.schema(
                                        "Response DTO %s for %s".formatted(name, md.getName())))
                        .addAnnotation(jsonIncludeNonNull());
        TypeName modelIdType = resolveModelIdType(md);
        List<ResolvedField> components =
                resolveFields(DtoType.RESPONSE, fields, true, modelIdType);
        addRecordConstructor(builder, components);
        addArrayAccessors(builder, components);
        addFieldSecurityMetadata(
                builder, className, DtoType.RESPONSE, fields, modelIdType);
        if (withers) {
            addWithers(builder, pkg, className, components);
        }
        addBuilder(builder, pkg, className, components);
        return javaFile(pkg, builder);
    }

    private JavaFile javaFile(String pkg, TypeSpec.Builder builder) {
        return JavaPoetUtils.javaFile(pkg, builder.build())
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .build();
    }

    private String dtoNullnessContract(DtoType type, String modelName) {
        if (type == DtoType.REQUEST) {
            return "\n<p>Nullness: request DTO instances for "
                    + modelName
                    + " are non-null. Fields may be null unless Jakarta validation marks them"
                    + " required; PATCH treats null according to mapper patch semantics, while"
                    + " PUT supplies replacement values.\n";
        }
        return "\n<p>Nullness: response IDs are non-null after persistence; other fields may be"
                + " null when database values, projections, or field-level security omit them.\n";
    }

    private AnnotationSpec jsonIncludeNonNull() {
        return AnnotationSpec.builder(JsonInclude.class)
                .addMember("value", "$T.$L", JsonInclude.Include.class, "NON_NULL")
                .build();
    }

    private List<ResolvedField> resolveFields(
            DtoType type,
            List<FieldDescriptor> fields,
            boolean additionalResponse,
            TypeName modelIdType) {
        List<ResolvedField> result = new ArrayList<>();
        for (FieldDescriptor fd : fields) {
            ResolvedField resolved = resolveField(type, fd, modelIdType);
            AnnotationSpec security =
                    fd.hasFieldSecurity() ? buildFieldSecurityAnnotation(fd) : null;
            AnnotationSpec schema =
                    additionalResponse
                            ? additionalSchemaFromDescriptor(fd, resolved.type())
                            : schemaFromDescriptor(fd, resolved.type());
            result.add(
                    new ResolvedField(
                            resolved.name(),
                            resolved.type(),
                            fd.getValidations(),
                            schema,
                            security,
                            shouldCascadeValidation(type, fd),
                            fd.getProjectionPath()));
        }
        return result;
    }

    private boolean shouldCascadeValidation(DtoType type, FieldDescriptor fd) {
        return type.isRequest() && fd.isEmbedded();
    }

    private void addRecordConstructor(TypeSpec.Builder dtoClass, List<ResolvedField> components) {
        MethodSpec.Builder constructor =
                MethodSpec.compactConstructorBuilder().addModifiers(Modifier.PUBLIC);
        for (ResolvedField component : components) {
            ParameterSpec.Builder parameter =
                    ParameterSpec.builder(component.type(), component.name())
                            .addJavadoc(
                                    "$L - nullable unless copied validation annotations require a"
                                            + " value or constrain its range.\n",
                                    component.name());
            AnnotationSpec nullness = nullnessAnnotation(component);
            if (nullness != null) {
                parameter.addAnnotation(nullness);
            }
            if (component.cascadeValidation()) {
                parameter.addAnnotation(JAKARTA_VALID);
            }
            if (component.schemaAnnotation() != null) {
                parameter.addAnnotation(component.schemaAnnotation());
            }
            if (!component.projectionPath().isBlank()) {
                parameter.addAnnotation(
                        AnnotationSpec.builder(
                                        ClassName.get(
                                                "nl.datasteel.crudcraft.annotations.fields",
                                                "ProjectionField"))
                                .addMember("value", "$S", component.projectionPath())
                                .build());
            }
            for (AnnotationSpec validation : component.validations()) {
                parameter.addAnnotation(validation);
            }
            if (component.securityAnnotation() != null) {
                parameter.addAnnotation(component.securityAnnotation());
            }
            constructor.addParameter(parameter.build());
            addConstructorAssignment(constructor, component.name(), component.type());
        }
        dtoClass.recordConstructor(constructor.build());
    }

    private AnnotationSpec nullnessAnnotation(ResolvedField component) {
        if (component.type().isPrimitive() || hasNotNullValidation(component)) {
            return null;
        }
        if ("id".equals(component.name())) {
            return AnnotationSpec.builder(JAKARTA_NOT_NULL).build();
        }
        return AnnotationSpec.builder(JAKARTA_NULLABLE).build();
    }

    private boolean hasNotNullValidation(ResolvedField component) {
        return component.validations().stream()
                .anyMatch(
                        validation -> {
                            String type = validation.type().toString();
                            return JAKARTA_NOT_NULL_NAME.equals(type)
                                    || JAVAX_NOT_NULL_NAME.equals(type);
                        });
    }

    private void addArrayAccessors(TypeSpec.Builder dtoClass, List<ResolvedField> fields) {
        for (ResolvedField field : fields) {
            if (!isArray(field.type())) {
                continue;
            }
            dtoClass.addMethod(
                    MethodSpec.methodBuilder(field.name())
                            .addModifiers(Modifier.PUBLIC)
                            .returns(field.type())
                            .addStatement(
                                    "return $1N == null ? null : $2T.copyOf($1N, $1N.length)",
                                    field.name(),
                                    Arrays.class)
                            .build());
        }
    }

    private void addWithers(
            TypeSpec.Builder dtoClass, String pkg, String className, List<ResolvedField> fields) {
        ClassName outer = ClassName.get(pkg, className);
        for (ResolvedField field : fields) {
            dtoClass.addMethod(
                    MethodSpec.methodBuilder("with" + capitalize(field.name()))
                            .addModifiers(Modifier.PUBLIC)
                            .returns(outer)
                            .addParameter(parameterWithNullness(field))
                            .addStatement(
                                    "return new $T($L)",
                                    outer,
                                    constructorArgs(fields, field.name()))
                            .build());
        }
    }

    private void addBuilder(
            TypeSpec.Builder dtoClass, String pkg, String className, List<ResolvedField> fields) {
        ClassName outer = ClassName.get(pkg, className);
        ClassName builderClass = outer.nestedClass("Builder");
        TypeSpec.Builder builder =
                TypeSpec.classBuilder("Builder").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        for (ResolvedField field : fields) {
            builder.addField(fieldWithNullness(field));
            builder.addMethod(
                    MethodSpec.methodBuilder(field.name())
                            .addModifiers(Modifier.PUBLIC)
                            .returns(builderClass)
                            .addParameter(parameterWithNullness(field))
                            .addStatement(builderAssignment(field))
                            .addStatement("return this")
                            .build());
        }
        builder.addMethod(
                MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(outer)
                        .addStatement("return new $T($L)", outer, constructorArgs(fields, null))
                        .build());
        dtoClass.addType(builder.build());
        dtoClass.addMethod(
                MethodSpec.methodBuilder("builder")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(builderClass)
                        .addStatement("return new Builder()")
                        .build());
    }

    private CodeBlock builderAssignment(ResolvedField field) {
        Class<?> copyType = collectionCopyType(field.type());
        if (copyType != null) {
            return CodeBlock.of(
                    "this.$1N = $1N == null ? null : $2T.copyOf($1N)",
                    field.name(),
                    copyType);
        }
        if (isArray(field.type())) {
            return CodeBlock.of(
                    "this.$1N = $1N == null ? null : $2T.copyOf($1N, $1N.length)",
                    field.name(),
                    Arrays.class);
        }
        return CodeBlock.of("this.$1N = $1N", field.name());
    }

    private ParameterSpec parameterWithNullness(ResolvedField field) {
        ParameterSpec.Builder parameter = ParameterSpec.builder(field.type(), field.name());
        AnnotationSpec nullness = nullnessAnnotation(field);
        if (nullness != null) {
            parameter.addAnnotation(nullness);
        }
        if (field.cascadeValidation()) {
            parameter.addAnnotation(JAKARTA_VALID);
        }
        for (AnnotationSpec validation : field.validations()) {
            parameter.addAnnotation(validation);
        }
        return parameter.build();
    }

    private FieldSpec fieldWithNullness(ResolvedField field) {
        FieldSpec.Builder fieldSpec =
                FieldSpec.builder(field.type(), field.name(), Modifier.PRIVATE);
        AnnotationSpec nullness = nullnessAnnotation(field);
        if (nullness != null) {
            fieldSpec.addAnnotation(nullness);
        }
        if (field.cascadeValidation()) {
            fieldSpec.addAnnotation(JAKARTA_VALID);
        }
        for (AnnotationSpec validation : field.validations()) {
            fieldSpec.addAnnotation(validation);
        }
        return fieldSpec.build();
    }

    private String constructorArgs(List<ResolvedField> fields, String replacementName) {
        return fields.stream()
                .map(
                        field ->
                                field.name().equals(replacementName)
                                        ? field.name()
                                        : "this." + field.name())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private void addFieldSecurityMetadata(
            TypeSpec.Builder dtoClass,
            String className,
            DtoType type,
            List<FieldDescriptor> fields,
            TypeName modelIdType) {
        ClassName dtoClassName = ClassName.bestGuess(className);
        ClassName metadataType = ClassName.get(FieldSecurityMetadata.class);
        ClassName ruleType = metadataType.nestedClass("FieldRule");
        CodeBlock.Builder rules = CodeBlock.builder();
        for (int i = 0; i < fields.size(); i++) {
            FieldDescriptor fd = fields.get(i);
            ResolvedField resolved = resolveField(type, fd, modelIdType);
            rules.add(
                    "new $T<>($S, dto -> dto.$L(), null, $L, $T.of($L), $T.of($L), $T.$L)",
                    ruleType,
                    resolved.name(),
                    resolved.name(),
                    fd.hasFieldSecurity(),
                    List.class,
                    rolesCode(fd.getReadRoles()),
                    List.class,
                    rolesCode(fd.getWriteRoles()),
                    WritePolicy.class,
                    fd.getWritePolicy().name());
            if (i < fields.size() - 1) {
                rules.add(",\n");
            }
        }

        dtoClass.addField(
                FieldSpec.builder(
                                ParameterizedTypeName.get(metadataType, dtoClassName),
                                "FIELD_SECURITY_METADATA",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .initializer("$T.of($T.of($L))", metadataType, List.class, rules.build())
                        .build());

        dtoClass.addMethod(
                MethodSpec.methodBuilder("fieldSecurityMetadata")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(ParameterizedTypeName.get(metadataType, dtoClassName))
                        .addStatement("return FIELD_SECURITY_METADATA")
                        .build());
    }

    private CodeBlock rolesCode(String[] roles) {
        CodeBlock.Builder code = CodeBlock.builder();
        for (int i = 0; i < roles.length; i++) {
            code.add("$S", roles[i]);
            if (i < roles.length - 1) {
                code.add(", ");
            }
        }
        return code.build();
    }

    private ResolvedField resolveField(DtoType type, FieldDescriptor fd, TypeName modelIdType) {
        TypeName originalType = TypeName.get(fd.getType());
        TypeName typeName = originalType;
        String fieldName = fd.getName();

        if (!hasRelationOrEmbedded(fd)) {
            typeName = boxIfSecuredPrimitive(fd, typeName);
            return new ResolvedField(fieldName, typeName);
        }

        if (!type.isRequest()) {
            typeName = resolveDtoType(typeName, fd.getTargetType(), type, fd.isEmbedded());
            typeName = boxIfSecuredPrimitive(fd, typeName);
            return new ResolvedField(fieldName, typeName);
        }

        if (isReferenceRelation(fd)) {
            typeName = toIdType(originalType, relationshipIdType(fd, modelIdType));
            fieldName = idFieldName(fd, typeName);
        } else {
            typeName = resolveDtoType(originalType, fd.getTargetType(), type, fd.isEmbedded());
        }

        typeName = boxIfSecuredPrimitive(fd, typeName);
        return new ResolvedField(fieldName, typeName);
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings(
            value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "Used reflectively by compatibility tests")
    private ResolvedField resolveField(DtoType type, FieldDescriptor fd) {
        return resolveField(type, fd, ClassName.get(UUID.class));
    }

    private boolean hasRelationOrEmbedded(FieldDescriptor fd) {
        return fd.getRelType() != RelationshipType.NONE || fd.isEmbedded();
    }

    private boolean isReferenceRelation(FieldDescriptor fd) {
        return fd.getRelType() != RelationshipType.NONE && !fd.isEmbedded();
    }

    private TypeName boxIfSecuredPrimitive(FieldDescriptor fd, TypeName typeName) {
        return fd.hasFieldSecurity() && typeName.isPrimitive() ? typeName.box() : typeName;
    }

    private AnnotationSpec schemaFromDescriptor(FieldDescriptor fd, TypeName typeName) {
        String javadoc = fd.getJavadoc();
        boolean nullable = !typeName.isPrimitive();
        if (fd.isEnumString()) {
            return TemplateUtil.schemaForEnum(javadoc, fd.getEnumValues(), nullable);
        }
        return TemplateUtil.schemaFromMetadata(javadoc, fd.getSchemaMetadata(), nullable);
    }

    private AnnotationSpec additionalSchemaFromDescriptor(FieldDescriptor fd, TypeName typeName) {
        String javadoc = fd.getJavadoc();
        boolean nullable = !typeName.isPrimitive();
        if (fd.isEnumString()) {
            return TemplateUtil.schemaForEnum(javadoc, fd.getEnumValues(), nullable);
        }
        return javadoc != null && !javadoc.trim().isEmpty()
                ? TemplateUtil.schemaForField(javadoc, nullable)
                : null;
    }

    private TypeName resolveDtoType(
            TypeName original, String targetFqn, DtoType parentType, boolean embedded) {
        return DtoTypeResolver.resolveDtoType(original, targetFqn, parentType, embedded);
    }

    private TypeName toIdType(TypeName original, TypeName idType) {
        return DtoTypeResolver.toIdType(original, idType);
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings(
            value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "Used reflectively by compatibility tests")
    private TypeName toIdType(TypeName original) {
        return DtoTypeResolver.toIdType(original, ClassName.get(UUID.class));
    }

    private TypeName resolveModelIdType(ModelDescriptor md) {
        return DtoTypeResolver.resolveModelIdType(md);
    }

    private TypeName relationshipIdType(FieldDescriptor fd, TypeName modelIdType) {
        return DtoTypeResolver.relationshipIdType(fd, modelIdType);
    }

    private String idFieldName(FieldDescriptor fd, TypeName typeName) {
        return DtoTypeResolver.idFieldName(fd, typeName);
    }

    private void addConstructorAssignment(
            MethodSpec.Builder ctor, String fieldName, TypeName typeName) {
        Class<?> copyType = collectionCopyType(typeName);
        if (copyType != null) {
            ctor.addStatement("$1L = $1L == null ? null : $2T.copyOf($1L)", fieldName, copyType);
            return;
        }
        if (isArray(typeName)) {
            ctor.addStatement(
                    "$1L = $1L == null ? null : $2T.copyOf($1L, $1L.length)",
                    fieldName,
                    Arrays.class);
        }
    }

    private Class<?> collectionCopyType(TypeName typeName) {
        return DtoTypeResolver.collectionCopyType(typeName);
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings(
            value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "Used reflectively by compatibility tests")
    private boolean isList(TypeName typeName) {
        return List.class.equals(collectionCopyType(typeName));
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings(
            value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "Used reflectively by compatibility tests")
    private boolean isSet(TypeName typeName) {
        return Set.class.equals(collectionCopyType(typeName));
    }

    private boolean isArray(TypeName typeName) {
        return DtoTypeResolver.isArray(typeName);
    }

    boolean isSimpleImmutableType(TypeName typeName) {
        if (typeName.isPrimitive()) {
            return true;
        }
        if (!(typeName instanceof ClassName className)) {
            return false;
        }
        String canonical = className.canonicalName();
        return canonical.equals(String.class.getCanonicalName())
                || canonical.equals(UUID.class.getCanonicalName())
                || canonical.equals(Boolean.class.getCanonicalName())
                || canonical.equals(Byte.class.getCanonicalName())
                || canonical.equals(Short.class.getCanonicalName())
                || canonical.equals(Integer.class.getCanonicalName())
                || canonical.equals(Long.class.getCanonicalName())
                || canonical.equals(Float.class.getCanonicalName())
                || canonical.equals(Double.class.getCanonicalName())
                || canonical.equals(Character.class.getCanonicalName())
                || canonical.startsWith("java.time.");
    }

    TypeName metadataCastType(TypeName typeName) {
        return typeName.isPrimitive() ? typeName.box() : typeName;
    }

    String getterName(String fieldName, TypeName typeName) {
        String prefix = TypeName.BOOLEAN.equals(typeName) ? "is" : "get";
        return prefix + capitalize(fieldName);
    }

    private AnnotationSpec buildFieldSecurityAnnotation(FieldDescriptor fd) {
        AnnotationSpec.Builder annotation =
                AnnotationSpec.builder(ClassName.get(FieldSecurity.class));
        annotation.addMember("readRoles", rolesArray(fd.getReadRoles()));
        annotation.addMember("writeRoles", rolesArray(fd.getWriteRoles()));
        annotation.addMember("writePolicy", "$T.$L", WritePolicy.class, fd.getWritePolicy().name());
        return annotation.build();
    }

    private CodeBlock rolesArray(String[] roles) {
        CodeBlock.Builder code = CodeBlock.builder();
        code.add("{");
        for (int i = 0; i < roles.length; i++) {
            code.add("$S", roles[i]);
            if (i < roles.length - 1) {
                code.add(", ");
            }
        }
        code.add("}");
        return code.build();
    }

    private String capitalize(String in) {
        return Character.toUpperCase(in.charAt(0)) + in.substring(1);
    }

    private record ResolvedField(
            String name,
            TypeName type,
            List<AnnotationSpec> validations,
            AnnotationSpec schemaAnnotation,
            AnnotationSpec securityAnnotation,
            boolean cascadeValidation,
            String projectionPath) {
        private ResolvedField(String name, TypeName type) {
            this(name, type, List.of(), null, null, false, "");
        }
    }

    private record DtoFieldSelection(
            List<FieldDescriptor> dtoFields,
            List<FieldDescriptor> refFields,
            List<FieldDescriptor> requestFields,
            Set<String> responseNames) {}
}

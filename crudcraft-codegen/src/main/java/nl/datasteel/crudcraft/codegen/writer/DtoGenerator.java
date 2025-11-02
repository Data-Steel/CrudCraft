/*
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.codegen.writer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.fileheader.ModelStrictHeader;
import nl.datasteel.crudcraft.codegen.util.JavaPoetUtils;
import nl.datasteel.crudcraft.codegen.util.Pluralizer;
import nl.datasteel.crudcraft.codegen.util.StringCase;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;

/**
 * Generates Request, Response and Ref DTOs
 * honoring @Dto, @Request, @EnumString, @AuditTrail,
 * embedded classes and JPA-relaties.
 */
public class DtoGenerator implements Generator {

    /**
     * Generates DTO classes for the given model descriptor.
     * It creates Request, Response, and Ref DTOs
     * based on the fields defined in the model descriptor.
     *
     * @param md the model descriptor containing metadata about the entity
     * @param ctx the write context providing environment and utilities
     * @return a list of JavaFile objects representing the generated DTO classes
     */
    @Override
    public List<JavaFile> generate(ModelDescriptor md, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(md, ctx)) {
            return List.of();
        }

        List<FieldDescriptor> dtoFields = md.getFields().stream()
                .filter(FieldDescriptor::inDto)
                .toList();

        List<FieldDescriptor> refFields = dtoFields.stream()
                .filter(fd -> fd.inRef() || "id".equalsIgnoreCase(fd.getName()))
                .toList();

        // For abstract classes, only generate Ref DTO
        if (md.isAbstract()) {
            ctx.env().getMessager().printMessage(
                    javax.tools.Diagnostic.Kind.NOTE,
                    "Skipping Request and Response DTOs for abstract entity: " + md.getName()
                            + ". Only generating Ref DTO."
            );
            JavaFile ref = generateDto(DtoType.REF, md, refFields);
            return List.of(ref);
        }

        // For non-abstract classes, generate all DTOs
        List<FieldDescriptor> requestFields = md.getFields().stream()
                .filter(fd -> fd.inRequest()
                        || (fd.inDto() && fd.getRelType() != RelationshipType.NONE
                        && !fd.isEmbedded()))
                .toList();

        JavaFile req = generateDto(DtoType.REQUEST, md, requestFields);
        JavaFile resp = generateDto(DtoType.RESPONSE, md, dtoFields);
        JavaFile ref = generateDto(DtoType.REF, md, refFields);

        // Generate additional response DTOs based on @Dto(value = {}) entries
        Set<String> responseNames = md.getFields().stream()
                .flatMap(fd -> Arrays.stream(fd.getResponseDtos()))
                .collect(Collectors.toSet());

        List<JavaFile> extraResponses = new ArrayList<>();
        for (String name : responseNames) {
            List<FieldDescriptor> fieldsForName = md.getFields().stream()
                    .filter(fd -> Arrays.asList(fd.getResponseDtos()).contains(name)
                            || "id".equalsIgnoreCase(fd.getName()))
                    .toList();
            extraResponses.add(generateAdditionalResponseDto(md, name, fieldsForName));
        }

        List<JavaFile> all = new ArrayList<>();
        all.add(req);
        all.add(resp);
        all.add(ref);
        all.addAll(extraResponses);

        return all;
    }

    @Override
    public int order() {
        return 0;
    }

    /**
     * Generates a DTO class based on the provided model descriptor and field descriptors.
     * The type of DTO (Request, Response, Ref) is determined by the DtoType.
     *
     * @param type the type of DTO to generate
     * @param md the model descriptor containing metadata about the entity
     * @param fields the list of field descriptors to include in the DTO
     * @return a JavaFile representing the generated DTO class
     */
    private JavaFile generateDto(DtoType type, ModelDescriptor md, List<FieldDescriptor> fields) {
        String pkg = md.getPackageName() + type.packageSuffix();
        String className = md.getName() + type.classSuffix();

        TypeSpec.Builder b;
        if (type.isRequest()) {
            b = TypeSpec.classBuilder(className)
                    .addJavadoc(ModelStrictHeader.header(md.getName(), pkg,
                            this.getClass().getSimpleName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(TemplateUtil.schema(type.schemaDescription(md.getName())))
                    .addAnnotation(AnnotationSpec.builder(JsonInclude.class)
                            .addMember("value", "$T.$L", JsonInclude.Include.class,
                                    "NON_NULL")
                            .build());

            for (FieldDescriptor fd : fields) {
                TypeName originalType = TypeName.get(fd.getType());
                TypeName typeName = originalType;
                String fieldName = fd.getName();

                if (fd.getRelType() != RelationshipType.NONE || fd.isEmbedded()) {
                    if (fd.getRelType() != RelationshipType.NONE && !fd.isEmbedded()) {
                        typeName = toIdType(originalType);
                        fieldName = idFieldName(fd, typeName);
                    } else {
                        typeName = resolveDtoType(originalType, fd.getTargetType(), type, fd.isEmbedded());
                    }
                }

                if (fd.hasFieldSecurity() && typeName.isPrimitive()) {
                    typeName = typeName.box();
                }

                AnnotationSpec security = fd.hasFieldSecurity()
                        ? buildFieldSecurityAnnotation(fd) : null;
                AnnotationSpec enumSchema = fd.isEnumString()
                        ? TemplateUtil.schemaAllowable(fd.getEnumValues())
                        : null;
                JavaPoetUtils.addFieldWithAccessors(
                        b,
                        fieldName,
                        typeName,
                        fd.getValidations(),
                        enumSchema,
                        security
                );
            }
        } else {
            b = TypeSpec.classBuilder(className)
                    .addJavadoc(ModelStrictHeader.header(md.getName(), pkg,
                            this.getClass().getSimpleName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(TemplateUtil.schema(type.schemaDescription(md.getName())))
                    .addAnnotation(AnnotationSpec.builder(JsonInclude.class)
                            .addMember("value", "$T.$L", JsonInclude.Include.class,
                                    "NON_NULL")
                            .build());

            MethodSpec.Builder ctor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);

            for (FieldDescriptor fd : fields) {
                TypeName typeName = TypeName.get(fd.getType());

                if (fd.getRelType() != RelationshipType.NONE || fd.isEmbedded()) {
                    typeName = resolveDtoType(typeName, fd.getTargetType(), type, fd.isEmbedded());
                }

                if (fd.hasFieldSecurity() && typeName.isPrimitive()) {
                    typeName = typeName.box();
                }

                AnnotationSpec security = fd.hasFieldSecurity()
                        ? buildFieldSecurityAnnotation(fd) : null;
                AnnotationSpec enumSchema = fd.isEnumString()
                        ? TemplateUtil.schemaAllowable(fd.getEnumValues())
                        : null;

                if (isSet(typeName) || isList(typeName)) {
                    JavaPoetUtils.addFieldWithAccessors(
                            b,
                            fd.getName(),
                            typeName,
                            fd.getValidations(),
                            enumSchema,
                            security
                    );
                } else {
                    JavaPoetUtils.addFieldWithGetter(
                            b,
                            fd.getName(),
                            typeName,
                            fd.getValidations(),
                            enumSchema,
                            security
                    );
                }

                ctor.addParameter(typeName, fd.getName());
                if (isSet(typeName)) {
                    ctor.addStatement("this.$1L = $1L == null ? null : $2T.copyOf($1L)",
                            fd.getName(), Set.class);
                } else if (isList(typeName)) {
                    ctor.addStatement("this.$1L = $1L == null ? null : $2T.copyOf($1L)",
                            fd.getName(), List.class);
                } else if (typeName instanceof ArrayTypeName) {
                    ctor.addStatement("this.$1L = $1L == null ? null : $2T.copyOf($1L, $1L.length)",
                            fd.getName(), Arrays.class);
                } else {
                    ctor.addStatement("this.$1L = $1L", fd.getName());
                }
            }

            b.addMethod(ctor.build());
        }

        addBuilder(b, type, pkg, className, fields);

        return JavaFile.builder(pkg, b.build())
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    /**
     * Generates an additional response DTO class for the given name. The class is placed
     * in the {@code .dto.response} package of the entity and contains all fields
     * that specify the provided name in their {@code @Dto} annotation, plus the
     * entity identifier.
     *
     * @param md     the model descriptor
     * @param name   custom DTO name as specified in {@code @Dto}
     * @param fields fields to include in the DTO
     * @return a JavaFile representing the generated DTO
     */
    private JavaFile generateAdditionalResponseDto(ModelDescriptor md, String name, List<FieldDescriptor> fields) {
        String pkg = md.getPackageName() + ".dto.response";
        String className = md.getName() + StringCase.PASCAL.apply(name) + "ResponseDto";

        TypeSpec.Builder b = TypeSpec.classBuilder(className)
                .addJavadoc(ModelStrictHeader.header(md.getName(), pkg,
                        this.getClass().getSimpleName()))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(TemplateUtil.schema("Response DTO %s for %s".formatted(name, md.getName())))
                .addAnnotation(AnnotationSpec.builder(JsonInclude.class)
                        .addMember("value", "$T.$L", JsonInclude.Include.class,
                                "NON_NULL")
                        .build());

        MethodSpec.Builder ctor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (FieldDescriptor fd : fields) {
            TypeName typeName = TypeName.get(fd.getType());

            if (fd.getRelType() != RelationshipType.NONE || fd.isEmbedded()) {
                typeName = resolveDtoType(typeName, fd.getTargetType(), DtoType.RESPONSE, fd.isEmbedded());
            }

            if (fd.hasFieldSecurity() && typeName.isPrimitive()) {
                typeName = typeName.box();
            }

            AnnotationSpec security = fd.hasFieldSecurity()
                    ? buildFieldSecurityAnnotation(fd) : null;
            AnnotationSpec enumSchema = fd.isEnumString()
                    ? TemplateUtil.schemaAllowable(fd.getEnumValues())
                    : null;

            if (isSet(typeName) || isList(typeName)) {
                JavaPoetUtils.addFieldWithAccessors(
                        b,
                        fd.getName(),
                        typeName,
                        fd.getValidations(),
                        enumSchema,
                        security
                );
            } else {
                JavaPoetUtils.addFieldWithGetter(
                        b,
                        fd.getName(),
                        typeName,
                        fd.getValidations(),
                        enumSchema,
                        security
                );
            }

            ctor.addParameter(typeName, fd.getName());
            if (isSet(typeName)) {
                ctor.addStatement("this.$1L = $1L == null ? null : $2T.copyOf($1L)",
                        fd.getName(), Set.class);
            } else if (isList(typeName)) {
                ctor.addStatement("this.$1L = $1L == null ? null : $2T.copyOf($1L)",
                        fd.getName(), List.class);
            } else if (typeName instanceof ArrayTypeName) {
                ctor.addStatement("this.$1L = $1L == null ? null : $2T.copyOf($1L, $1L.length)",
                        fd.getName(), Arrays.class);
            } else {
                ctor.addStatement("this.$1L = $1L", fd.getName());
            }
        }

        b.addMethod(ctor.build());

        addBuilder(b, DtoType.RESPONSE, pkg, className, fields);

        return JavaFile.builder(pkg, b.build())
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private methods
    // ──────────────────────────────────────────────────────────────────────────

    private void addBuilder(TypeSpec.Builder dtoClass, DtoType type, String pkg,
            String className, List<FieldDescriptor> fields) {
        ClassName outer = ClassName.get(pkg, className);
        ClassName builderClass = outer.nestedClass("Builder");
        TypeSpec.Builder bb = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        List<String> fieldNames = new ArrayList<>();

        for (FieldDescriptor fd : fields) {
            TypeName original = TypeName.get(fd.getType());
            TypeName typeName = original;
            String fieldName = fd.getName();

            if (type.isRequest()) {
                if (fd.getRelType() != RelationshipType.NONE || fd.isEmbedded()) {
                    if (fd.getRelType() != RelationshipType.NONE && !fd.isEmbedded()) {
                        typeName = toIdType(original);
                        fieldName = idFieldName(fd, typeName);
                    } else {
                        typeName = resolveDtoType(original, fd.getTargetType(), type, fd.isEmbedded());
                    }
                }
            } else {
                if (fd.getRelType() != RelationshipType.NONE || fd.isEmbedded()) {
                    typeName = resolveDtoType(typeName, fd.getTargetType(), type, fd.isEmbedded());
                }
            }

            if (fd.hasFieldSecurity() && typeName.isPrimitive()) {
                typeName = typeName.box();
            }

            bb.addField(typeName, fieldName, Modifier.PRIVATE);
            bb.addMethod(MethodSpec.methodBuilder(fieldName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(builderClass)
                    .addParameter(typeName, fieldName)
                    .addStatement("this.$N = $N", fieldName, fieldName)
                    .addStatement("return this")
                    .build());
            fieldNames.add(fieldName);
        }

        MethodSpec.Builder build = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(outer);
        if (type.isRequest()) {
            build.addStatement("$T dto = new $T()", outer, outer);
            for (String name : fieldNames) {
                String up = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                build.addStatement("dto.set$L(this.$L)", up, name);
            }
            build.addStatement("return dto");
        } else {
            String args = String.join(", ", fieldNames);
            build.addStatement("return new $T($L)", outer, args);
        }
        bb.addMethod(build.build());

        dtoClass.addType(bb.build());
        dtoClass.addMethod(MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(builderClass)
                .addStatement("return new Builder()")
                .build());
    }

    /**
     * Resolves the DTO type for a given field based on its original type and target FQN.
     * If the field is a relationship or embedded, it generates a Request or Response DTO type.
     *
     * @param original the original type of the field
     * @param targetFqn the fully qualified name of the target type
     * @param request true if this is a request DTO, false for response
     * @return the resolved DTO type
     */
    private TypeName resolveDtoType(
            TypeName original,
            String targetFqn,
            DtoType parentType,
            boolean embedded
    ) {
        String simple = targetFqn.substring(targetFqn.lastIndexOf('.') + 1);
        String pkgBase = targetFqn.substring(0, targetFqn.lastIndexOf('.'));
        String subPkg;
        String suffix;
        if (embedded) {
            subPkg = parentType.isRequest() ? ".dto.request" : ".dto.response";
            suffix = parentType.isRequest() ? "RequestDto" : "ResponseDto";
        } else {
            subPkg = parentType.isRequest() ? ".dto.request" : ".dto.ref";
            suffix = parentType.isRequest() ? "RequestDto" : "Ref";
        }

        ClassName dtoClass = ClassName.get(pkgBase + subPkg, simple + suffix);

        if (original instanceof ParameterizedTypeName ptn) {
            return ParameterizedTypeName.get(ptn.rawType, dtoClass);
        } else {
            return dtoClass;
        }
    }

    private TypeName toIdType(TypeName original) {
        ClassName uuid = ClassName.get(UUID.class);
        if (isSet(original)) {
            return ParameterizedTypeName.get(ClassName.get(Set.class), uuid);
        } else if (isList(original)) {
            return ParameterizedTypeName.get(ClassName.get(List.class), uuid);
        } else {
            return uuid;
        }
    }

    private String idFieldName(FieldDescriptor fd, TypeName typeName) {
        if (fd.getRelType() == RelationshipType.NONE || fd.isEmbedded()) {
            return fd.getName();
        }
        boolean isCollection = isSet(typeName) || isList(typeName);
        if (isCollection) {
            String base = Pluralizer.singularize(fd.getName());
            return base + "Ids";
        }
        return fd.getName() + "Id";
    }

    private boolean isSet(TypeName type) {
        return type instanceof ParameterizedTypeName pt
                && pt.rawType.equals(ClassName.get(Set.class));
    }

    private boolean isList(TypeName type) {
        return type instanceof ParameterizedTypeName pt
                && pt.rawType.equals(ClassName.get(List.class));
    }

    /**
     * Builds the FieldSecurity annotation for a given field descriptor.
     * If the field has read or write roles, they are added to the annotation.
     *
     * @param fd the field descriptor containing security roles
     * @return an AnnotationSpec for the FieldSecurity annotation
     */
    private AnnotationSpec buildFieldSecurityAnnotation(FieldDescriptor fd) {
        ClassName fsClass = ClassName.get(FieldSecurity.class);
        AnnotationSpec.Builder ab = AnnotationSpec.builder(fsClass);
        ab.addMember("readRoles", rolesArray(fd.getReadRoles()));
        ab.addMember("writeRoles", rolesArray(fd.getWriteRoles()));
        return ab.build();
    }

    /**
     * Builds a CodeBlock representing an array of roles.
     * Example: { "ROLE_USER", "ROLE_ADMIN" }
     *
     * @param roles the array of role strings
     * @return a CodeBlock representing the roles array
     */
    private CodeBlock rolesArray(String[] roles) {
        CodeBlock.Builder cb = CodeBlock.builder();
        cb.add("{");
        for (int i = 0; i < roles.length; i++) {
            cb.add("$S", roles[i]);
            if (i < roles.length - 1) {
                cb.add(", ");
            }
        }
        cb.add("}");
        return cb.build();
    }

}

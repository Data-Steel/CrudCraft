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
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.util.CollectionTypes;
import nl.datasteel.crudcraft.codegen.util.JavaPoetUtils;
import nl.datasteel.crudcraft.codegen.util.ModelIdTypeResolver;
import nl.datasteel.crudcraft.codegen.util.StringCase;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;
import nl.datasteel.crudcraft.codegen.writer.Generator;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;


/** Generates a MapStruct mapper interface for the given model descriptor. */
public class MapperGenerator implements StubGenerator {
    /** Creates a mapper generator. */
    public MapperGenerator() {}

    @Override
    public List<JavaFile> generate(ModelDescriptor modelDescriptor, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(modelDescriptor, ctx)) {
            return List.of();
        }

        // Skip mapper generation for abstract classes
        if (modelDescriptor.isAbstract()) {
            ctx.env()
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.NOTE,
                            "Skipping mapper generation for abstract entity: "
                                    + modelDescriptor.getName());
            return List.of();
        }

        return List.of(build(modelDescriptor, ctx));
    }

    @Override
    public JavaFile build(ModelDescriptor modelDescriptor, WriteContext ctx) {
        ctx.env()
                .getMessager()
                .printMessage(
                        Diagnostic.Kind.NOTE,
                        "Generating mapper for "
                                + modelDescriptor.getName()
                                + " in package "
                                + modelDescriptor.getBasePackage());

        String entityPackage = modelDescriptor.getPackageName();

        String modelName = modelDescriptor.getName();
        var meta =
                StubGeneratorUtil.stubMeta(
                        modelDescriptor, "mapper", "Mapper", "Mapper", this.getClass());
        final String mapperPackage = meta.pkg();

        String mapperName = meta.name();
        String header = meta.header();

        // Types
        ClassName entity = JavaPoetUtils.getClassName(entityPackage, modelName);
        ClassName requestDto =
                JavaPoetUtils.getClassName(
                        entityPackage + ".dto.request", modelName + "RequestDto");

        ClassName responseDto =
                JavaPoetUtils.getClassName(
                        entityPackage + ".dto.response", modelName + "ResponseDto");

        ClassName refDto =
                JavaPoetUtils.getClassName(entityPackage + ".dto.ref", modelName + "Ref");

        ClassName entityMapper =
                JavaPoetUtils.getClassName("nl.datasteel.crudcraft.runtime.mapper", "EntityMapper");
        ClassName mapperAnn = JavaPoetUtils.getClassName("org.mapstruct", "Mapper");
        ClassName beanMapping = JavaPoetUtils.getClassName("org.mapstruct", "BeanMapping");
        ClassName mapping = JavaPoetUtils.getClassName("org.mapstruct", "Mapping");
        ClassName mappingTarget = JavaPoetUtils.getClassName("org.mapstruct", "MappingTarget");
        ClassName nvStrategy =
                JavaPoetUtils.getClassName("org.mapstruct", "NullValuePropertyMappingStrategy");
        ClassName reportingPolicy = JavaPoetUtils.getClassName("org.mapstruct", "ReportingPolicy");
        ClassName collectionStrategy =
                JavaPoetUtils.getClassName("org.mapstruct", "CollectionMappingStrategy");
        ClassName injectionStrategy =
                JavaPoetUtils.getClassName("org.mapstruct", "InjectionStrategy");
        ClassName beanWrapper =
                JavaPoetUtils.getClassName("org.springframework.beans", "BeanWrapperImpl");
        ClassName exceptionCls =
                JavaPoetUtils.getClassName(
                        "nl.datasteel.crudcraft.runtime.exception", "MapperException");
        TypeName modelIdType = ModelIdTypeResolver.resolveModelIdType(modelDescriptor).box();

        // super type: EntityMapper<E, U, R, S, UUID>
        ParameterizedTypeName superType =
                ParameterizedTypeName.get(
                        entityMapper, entity, requestDto, responseDto, refDto, modelIdType);

        // Methods
        List<FieldDescriptor> manyToOne = manyToOneFields(modelDescriptor);
        List<FieldDescriptor> relFields = relationFields(modelDescriptor);
        List<FieldDescriptor> abstractRelFields = abstractRelationFields(modelDescriptor);
        MethodSpec fromRequest =
                fromRequest(modelName, entity, requestDto, mapping, relFields, abstractRelFields);
        MethodSpec update =
                update(
                        modelName,
                        entity,
                        requestDto,
                        mappingTarget,
                        mapping,
                        relFields,
                        abstractRelFields);
        MethodSpec patch =
                patch(
                        modelName,
                        entity,
                        requestDto,
                        mappingTarget,
                        beanMapping,
                        nvStrategy,
                        mapping,
                        relFields,
                        abstractRelFields);
        MethodSpec toResponse =
                toResponse(
                        modelName,
                        entity,
                        responseDto,
                        mapping,
                        manyToOne,
                        abstractRelFields,
                        modelDescriptor);
        MethodSpec toRef = toRef(entity, refDto);
        MethodSpec getIdFromRequest =
                getIdFromRequest(requestDto, beanWrapper, modelIdType, exceptionCls);
        List<MethodSpec> refHelpers = manyToOneRefHelpers(modelName, manyToOne);
        List<MethodSpec> idMappers = relationIdHelpers(modelName, relFields, modelIdType, ctx);

        // Generate specialized DTO mapper methods
        List<MethodSpec> specializedMappers =
                generateSpecializedMappers(
                        modelDescriptor,
                        entity,
                        entityPackage,
                        modelName,
                        mapping,
                        manyToOne,
                        abstractRelFields);

        // Determine child mappers we need — include ALL relations (incl. MANY_TO_ONE)
        List<ClassName> uses = determineUses(modelDescriptor);

        // @Mapper annotation
        AnnotationSpec mapperAnnotation =
                mapperAnnotation(
                        mapperAnn, reportingPolicy, collectionStrategy, injectionStrategy, uses);

        // interface
        TypeSpec.Builder mapperBuilder =
                TypeSpec.interfaceBuilder(mapperName)
                        .addJavadoc(header)
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(
                                AnnotationSpec.builder(SuppressWarnings.class)
                                        .addMember("value", "$S", "PMD")
                                        .build())
                        .addSuperinterface(superType)
                        .addAnnotation(mapperAnnotation)
                        .addMethods(
                                List.of(
                                        fromRequest,
                                        update,
                                        patch,
                                        toResponse,
                                        toRef,
                                        getIdFromRequest));
        refHelpers.forEach(mapperBuilder::addMethod);
        idMappers.forEach(mapperBuilder::addMethod);
        specializedMappers.forEach(mapperBuilder::addMethod);

        return JavaPoetUtils.javaFile(mapperPackage, mapperBuilder.build())
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .build();
    }

    private List<ClassName> determineUses(ModelDescriptor modelDescriptor) {
        List<ClassName> uses = new ArrayList<>();
        for (FieldDescriptor fd : modelDescriptor.getFields()) {
            if (fd.isTargetCrud()
                    && fd.getRelType() != RelationshipType.NONE
                    && fd.getRelType() != RelationshipType.MANY_TO_ONE
                    && !fd.isEmbedded()) {
                ClassName target = ClassName.bestGuess(fd.getTargetType());
                uses.add(
                        JavaPoetUtils.getClassName(
                                target.packageName() + ".mapper",
                                target.simpleName() + "Mapper"));
            }
        }
        return uses;
    }

    private List<FieldDescriptor> manyToOneFields(ModelDescriptor modelDescriptor) {
        List<FieldDescriptor> fields = new ArrayList<>();
        for (FieldDescriptor fd : modelDescriptor.getFields()) {
            // Only include if field is in Response DTO and target is not abstract
            if (!fd.isTargetCrud()) {
                continue;
            }
            if (fd.getRelType() != RelationshipType.MANY_TO_ONE) {
                continue;
            }
            if (fd.isTargetAbstract()) {
                continue;
            }
            if (!fd.inDto()) {
                continue;
            }
            fields.add(fd);
        }
        return fields;
    }

    private List<FieldDescriptor> relationFields(ModelDescriptor modelDescriptor) {
        List<FieldDescriptor> fields = new ArrayList<>();
        for (FieldDescriptor fd : modelDescriptor.getFields()) {
            // Only include if field is in Request DTO and target is not abstract
            if (fd.isTargetCrud()
                    && fd.getRelType() != RelationshipType.NONE
                    && !fd.isEmbedded()
                    && !fd.isTargetAbstract()
                    && isInRequestDto(fd)) {
                fields.add(fd);
            }
        }
        return fields;
    }

    private List<FieldDescriptor> abstractRelationFields(ModelDescriptor modelDescriptor) {
        List<FieldDescriptor> fields = new ArrayList<>();
        for (FieldDescriptor fd : modelDescriptor.getFields()) {
            // Find all abstract relation fields that need to be ignored in mappings
            if (fd.isTargetCrud()
                    && fd.getRelType() != RelationshipType.NONE
                    && !fd.isEmbedded()
                    && fd.isTargetAbstract()) {
                fields.add(fd);
            }
        }
        return fields;
    }

    private MethodSpec fromRequest(
            String modelName,
            ClassName entity,
            ClassName requestDto,
            ClassName mapping,
            List<FieldDescriptor> relFields,
            List<FieldDescriptor> abstractRelFields) {
        MethodSpec.Builder builder =
                MethodSpec.methodBuilder("fromRequest")
                        .addJavadoc(
                                "Maps a create or upsert request DTO into a new entity instance.\n"
                                        + "Nested relationship identifiers are resolved through"
                                        + " generated @Named helper methods.\n")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(entity)
                        .addParameter(requestDto, "request");
        for (FieldDescriptor fd : relFields) {
            String simple = ClassName.bestGuess(fd.getTargetType()).simpleName();
            String qualifier = modelName + "Map" + simple + collectionSuffix(fd);
            builder.addAnnotation(
                    AnnotationSpec.builder(mapping)
                            .addMember("target", "$S", fd.getName())
                            .addMember("source", "$S", idFieldName(fd))
                            .addMember("qualifiedByName", "$S", qualifier)
                            .build());
        }
        // Add ignore mappings for abstract relation fields
        for (FieldDescriptor fd : abstractRelFields) {
            builder.addAnnotation(
                    AnnotationSpec.builder(mapping)
                            .addMember("target", "$S", fd.getName())
                            .addMember("ignore", "$L", true)
                            .build());
        }
        return builder.build();
    }

    private MethodSpec update(
            String modelName,
            ClassName entity,
            ClassName requestDto,
            ClassName mappingTarget,
            ClassName mapping,
            List<FieldDescriptor> relFields,
            List<FieldDescriptor> abstractRelFields) {
        MethodSpec.Builder builder =
                MethodSpec.methodBuilder("update")
                        .addJavadoc(
                                "Applies a full update request DTO to an existing entity.\n"
                                        + "Relationship id fields replace the corresponding entity"
                                        + " relationships.\n")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(entity)
                        .addParameter(
                                ParameterSpec.builder(entity, "entity")
                                        .addAnnotation(mappingTarget)
                                        .build())
                        .addParameter(requestDto, "request");
        for (FieldDescriptor fd : relFields) {
            String simple = ClassName.bestGuess(fd.getTargetType()).simpleName();
            String qualifier = modelName + "Map" + simple + collectionSuffix(fd);
            builder.addAnnotation(
                    AnnotationSpec.builder(mapping)
                            .addMember("target", "$S", fd.getName())
                            .addMember("source", "$S", idFieldName(fd))
                            .addMember("qualifiedByName", "$S", qualifier)
                            .build());
        }
        // Add ignore mappings for abstract relation fields
        for (FieldDescriptor fd : abstractRelFields) {
            builder.addAnnotation(
                    AnnotationSpec.builder(mapping)
                            .addMember("target", "$S", fd.getName())
                            .addMember("ignore", "$L", true)
                            .build());
        }
        return builder.build();
    }

    private MethodSpec patch(
            String modelName,
            ClassName entity,
            ClassName requestDto,
            ClassName mappingTarget,
            ClassName beanMapping,
            ClassName nvStrategy,
            ClassName mapping,
            List<FieldDescriptor> relFields,
            List<FieldDescriptor> abstractRelFields) {
        MethodSpec.Builder builder =
                MethodSpec.methodBuilder("patch")
                        .addJavadoc(
                                "Applies patch semantics to an existing entity.\n"
                                        + "Null request properties are ignored by MapStruct so"
                                        + " existing entity values remain unchanged.\n")
                        .addAnnotation(Override.class)
                        .addAnnotation(
                                AnnotationSpec.builder(beanMapping)
                                        .addMember(
                                                "nullValuePropertyMappingStrategy",
                                                "$T.IGNORE",
                                                nvStrategy)
                                        .build())
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(entity)
                        .addParameter(
                                ParameterSpec.builder(entity, "entity")
                                        .addAnnotation(mappingTarget)
                                        .build())
                        .addParameter(requestDto, "request");
        for (FieldDescriptor fd : relFields) {
            String simple = ClassName.bestGuess(fd.getTargetType()).simpleName();
            String qualifier = modelName + "Map" + simple + collectionSuffix(fd);
            builder.addAnnotation(
                    AnnotationSpec.builder(mapping)
                            .addMember("target", "$S", fd.getName())
                            .addMember("source", "$S", idFieldName(fd))
                            .addMember("qualifiedByName", "$S", qualifier)
                            .build());
        }
        // Add ignore mappings for abstract relation fields
        for (FieldDescriptor fd : abstractRelFields) {
            builder.addAnnotation(
                    AnnotationSpec.builder(mapping)
                            .addMember("target", "$S", fd.getName())
                            .addMember("ignore", "$L", true)
                            .build());
        }
        return builder.build();
    }

    private MethodSpec toResponse(
            String modelName,
            ClassName entity,
            ClassName responseDto,
            ClassName mapping,
            List<FieldDescriptor> manyToOne,
            List<FieldDescriptor> abstractRelFields,
            ModelDescriptor modelDescriptor) {
        MethodSpec.Builder builder =
                MethodSpec.methodBuilder("toResponse")
                        .addJavadoc(
                                "Maps an entity to the full response DTO.\n"
                                        + "Relationship fields are represented by generated"
                                        + " reference DTO mappings where applicable.\n")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(responseDto)
                        .addParameter(entity, "entity");
        for (FieldDescriptor fd : manyToOne) {
            String simple = ClassName.bestGuess(fd.getTargetType()).simpleName();
            builder.addAnnotation(
                    AnnotationSpec.builder(mapping)
                            .addMember("target", "$S", fd.getName())
                            .addMember("qualifiedByName", "$S", modelName + "To" + simple + "Ref")
                            .build());
        }
        // Add ignore mappings for abstract relation fields in response
        for (FieldDescriptor fd : abstractRelFields) {
            builder.addAnnotation(
                    AnnotationSpec.builder(mapping)
                            .addMember("target", "$S", fd.getName())
                            .addMember("ignore", "$L", true)
                            .build());
        }
        // Force-load LOB fields so lazy-loaded bytes are present in the response DTO
        for (FieldDescriptor fd : modelDescriptor.getResponseLobFields()) {
            String getter =
                    "get"
                            + Character.toUpperCase(fd.getName().charAt(0))
                            + fd.getName().substring(1);
            builder.addAnnotation(
                    AnnotationSpec.builder(mapping)
                            .addMember("target", "$S", fd.getName())
                            .addMember("expression", "$S", "java(entity." + getter + "())")
                            .build());
        }
        return builder.build();
    }

    private MethodSpec toRef(ClassName entity, ClassName refDto) {
        return MethodSpec.methodBuilder("toRef")
                .addJavadoc("Maps an entity to its lightweight reference DTO.\n")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(refDto)
                .addParameter(entity, "entity")
                .build();
    }

    private MethodSpec getIdFromRequest(
            ClassName requestDto,
            ClassName beanWrapper,
            TypeName modelIdType,
            ClassName exceptionCls) {
        TypeName boxedIdType = modelIdType.box();
        return MethodSpec.methodBuilder("getIdFromRequest")
                .addJavadoc(
                        "Extracts the identifier from a request DTO for upsert operations.\n"
                                + "Generated record DTOs expose the id as a record component.\n")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                .returns(boxedIdType)
                .addParameter(requestDto, "request")
                .beginControlFlow("try")
                .beginControlFlow("if (request != null && request.getClass().isRecord())")
                .addStatement(
                        "return ($T) request.getClass().getMethod($S).invoke(request)",
                        boxedIdType,
                        "id")
                .endControlFlow()
                .addStatement("var wrapper = new $T(request)", beanWrapper)
                .addStatement("Object idVal = wrapper.getPropertyValue($S)", "id")
                .addStatement("return ($T) idVal", boxedIdType)
                .nextControlFlow("catch (Exception e)")
                .addStatement(
                        "throw new $T($S + (request == null ? $S : request.getClass().getName()),"
                                + " e)",
                        exceptionCls,
                        "Failed to read 'id' property from request DTO: ",
                        "<null>")
                .endControlFlow()
                .build();
    }

    private List<MethodSpec> manyToOneRefHelpers(String modelName, List<FieldDescriptor> fields) {
        Set<String> seen = new HashSet<>();
        List<MethodSpec> helpers = new ArrayList<>();
        ClassName named = JavaPoetUtils.getClassName("org.mapstruct", "Named");
        for (FieldDescriptor fd : fields) {
            String fq = fd.getTargetType();
            if (seen.add(fq)) {
                ClassName target = ClassName.bestGuess(fq);
                String simple = target.simpleName();
                ClassName ref =
                        JavaPoetUtils.getClassName(
                                target.packageName() + ".dto.ref", simple + "Ref");
                String param = Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
                String methodName = "to" + simple + "Ref";
                String qualifierName = modelName + "To" + simple + "Ref";
                helpers.add(
                        MethodSpec.methodBuilder(methodName)
                                .addAnnotation(
                                        AnnotationSpec.builder(named)
                                                .addMember("value", "$S", qualifierName)
                                                .build())
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .returns(ref)
                                .addParameter(target, param)
                                .build());
            }
        }
        return helpers;
    }

    private String idFieldName(FieldDescriptor fd) {
        TypeName type = TypeName.get(fd.getType());
        boolean collection = isSet(type) || isList(type);
        if (collection) {
            String base = fd.getName();
            if (base.endsWith("s") && !"s".equals(base)) {
                base = base.substring(0, base.length() - 1);
            }
            return base + "Ids";
        }
        return fd.getName() + "Id";
    }

    private List<MethodSpec> relationIdHelpers(
            String modelName,
            List<FieldDescriptor> fields,
            TypeName modelIdType,
            WriteContext ctx) {
        TypeName boxedIdType = modelIdType.box();
        List<MethodSpec> helpers = new ArrayList<>();
        ClassName named = JavaPoetUtils.getClassName("org.mapstruct", "Named");
        for (FieldDescriptor fd : fields) {
            String fq = fd.getTargetType();
            ClassName target = ClassName.bestGuess(fq);
            String simple = target.simpleName();
            String baseName = "map" + simple;
            String qualifierName = modelName + "Map" + simple;

            boolean simpleId = hasSingleId(fq, ctx);

            MethodSpec.Builder singleBuilder =
                    MethodSpec.methodBuilder(baseName)
                            .addAnnotation(
                                    AnnotationSpec.builder(named)
                                            .addMember("value", "$S", qualifierName)
                                            .build())
                            .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                            .returns(target)
                            .addParameter(boxedIdType, "id")
                            .beginControlFlow("if (id == null)")
                            .addStatement("return null")
                            .endControlFlow()
                            .addStatement("$T entity = new $T()", target, target);
            if (simpleId) {
                singleBuilder.addStatement("entity.setId(id)");
            }
            singleBuilder.addStatement("return entity");
            helpers.add(singleBuilder.build());

            TypeName fieldType = TypeName.get(fd.getType());
            boolean isSet = isSet(fieldType);
            boolean isList = isList(fieldType);
            if (isSet || isList) {
                ClassName raw = isSet ? ClassName.get(Set.class) : ClassName.get(List.class);
                TypeName returnType = ParameterizedTypeName.get(raw, target);
                TypeName paramType = ParameterizedTypeName.get(raw, boxedIdType);
                String suffix = isSet ? CollectionTypes.SET : CollectionTypes.LIST;
                String methodName = baseName + suffix;
                String collectionQualifier = modelName + "Map" + simple + suffix;
                MethodSpec collection =
                        MethodSpec.methodBuilder(methodName)
                                .addAnnotation(
                                        AnnotationSpec.builder(named)
                                                .addMember("value", "$S", collectionQualifier)
                                                .build())
                                .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                                .returns(returnType)
                                .addParameter(paramType, "ids")
                                .beginControlFlow("if (ids == null)")
                                .addStatement("return null")
                                .endControlFlow()
                                .addStatement(
                                        "return ids.stream().map(this::$L).collect($T.to$L())",
                                        baseName,
                                        ClassName.get(Collectors.class),
                                        isSet ? CollectionTypes.SET : CollectionTypes.LIST)
                                .build();
                helpers.add(collection);
            }
        }
        return helpers;
    }

    // Compatibility overload retained for tests and reflective tooling expecting the old signature.
    @SuppressWarnings("unused")
    List<MethodSpec> relationIdHelpers(
            String modelName,
            List<FieldDescriptor> fields,
            ClassName modelIdType,
            WriteContext ctx) {
        return relationIdHelpers(modelName, fields, (TypeName) modelIdType, ctx);
    }

    private boolean hasSingleId(String fqcn, WriteContext ctx) {
        TypeElement te = ctx.findTypeElement(fqcn);
        if (te == null) {
            return true;
        }
        long idCount =
                te.getEnclosedElements().stream()
                        .filter(e -> e.getAnnotation(Id.class) != null)
                        .count();
        boolean hasEmbedded =
                te.getEnclosedElements().stream()
                        .anyMatch(
                                e ->
                                        e.getAnnotation(EmbeddedId.class) != null);
        return idCount == 1 && !hasEmbedded;
    }

    private boolean isSet(TypeName type) {
        return type instanceof ParameterizedTypeName pt
                && pt.rawType().equals(ClassName.get(Set.class));
    }

    private boolean isList(TypeName type) {
        return type instanceof ParameterizedTypeName pt
                && pt.rawType().equals(ClassName.get(List.class));
    }

    private String collectionSuffix(FieldDescriptor fd) {
        TypeName fieldType = TypeName.get(fd.getType());
        if (isSet(fieldType)) {
            return CollectionTypes.SET;
        }
        if (isList(fieldType)) {
            return CollectionTypes.LIST;
        }
        return "";
    }

    private AnnotationSpec mapperAnnotation(
            ClassName mapperAnn,
            ClassName reportingPolicy,
            ClassName collectionStrategy,
            ClassName injectionStrategy,
            List<ClassName> uses) {
        AnnotationSpec.Builder mapperAnnotation =
                AnnotationSpec.builder(mapperAnn)
                        .addMember("componentModel", "$S", "spring")
                        .addMember("unmappedTargetPolicy", "$T.IGNORE", reportingPolicy)
                        .addMember(
                                "collectionMappingStrategy",
                                "$T.TARGET_IMMUTABLE",
                                collectionStrategy)
                        .addMember("injectionStrategy", "$T.FIELD", injectionStrategy);
        if (!uses.isEmpty()) {
            List<CodeBlock> useBlocks = new ArrayList<>();
            for (ClassName use : uses) {
                useBlocks.add(CodeBlock.of("$T.class", use));
            }
            mapperAnnotation.addMember("uses", "{$L}", CodeBlock.join(useBlocks, ", "));
        }
        return mapperAnnotation.build();
    }

    /**
     * Generates mapper methods for specialized DTOs (e.g., toListResponse, toMapResponse). These
     * DTOs are created when fields use @Dto(value = {"List", "Map", ...}).
     */
    private List<MethodSpec> generateSpecializedMappers(
            ModelDescriptor modelDescriptor,
            ClassName entity,
            String entityPackage,
            String modelName,
            ClassName mapping,
            List<FieldDescriptor> manyToOne,
            List<FieldDescriptor> abstractRelFields) {

        // Collect all unique specialized DTO names
        Set<String> specializedDtoNames =
                modelDescriptor.getFields().stream()
                        .flatMap(fd -> Arrays.stream(fd.getResponseDtos()))
                        .collect(Collectors.toSet());

        List<MethodSpec> methods = new ArrayList<>();

        for (String dtoName : specializedDtoNames) {
            String className = modelName + StringCase.PASCAL.apply(dtoName) + "ResponseDto";
            ClassName specializedDto =
                    JavaPoetUtils.getClassName(entityPackage + ".dto.response", className);

            String methodName = "to" + StringCase.PASCAL.apply(dtoName) + "Response";

            MethodSpec.Builder builder =
                    MethodSpec.methodBuilder(methodName)
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .returns(specializedDto)
                            .addParameter(entity, "entity");

            // Only add mappings for ManyToOne relations that are in this specialized DTO
            for (FieldDescriptor fd : manyToOne) {
                // Check if this field is in the specialized DTO
                if (Arrays.asList(fd.getResponseDtos()).contains(dtoName)) {
                    String simple = ClassName.bestGuess(fd.getTargetType()).simpleName();
                    builder.addAnnotation(
                            AnnotationSpec.builder(mapping)
                                    .addMember("target", "$S", fd.getName())
                                    .addMember(
                                            "qualifiedByName",
                                            "$S",
                                            modelName + "To" + simple + "Ref")
                                    .build());
                }
            }

            // Add ignore mappings for abstract relation fields
            for (FieldDescriptor fd : abstractRelFields) {
                builder.addAnnotation(
                        AnnotationSpec.builder(mapping)
                                .addMember("target", "$S", fd.getName())
                                .addMember("ignore", "$L", true)
                                .build());
            }

            methods.add(builder.build());
        }

        return methods;
    }

    /**
     * Determines if a field should be included in the Request DTO. This matches the logic in
     * DtoGenerator for consistency.
     */
    private boolean isInRequestDto(FieldDescriptor fd) {
        return fd.inRequest()
                || (fd.inDto() && fd.getRelType() != RelationshipType.NONE && !fd.isEmbedded());
    }

    @Override
    public boolean requiresCrudEntity() {
        return true;
    }

    @Override
    public int order() {
        return 2;
    }
}

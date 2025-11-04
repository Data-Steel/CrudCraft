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
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.util.JavaPoetUtils;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;
import nl.datasteel.crudcraft.codegen.writer.Generator;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;

/**
 * Generates a MapStruct mapper interface for the given model descriptor.
 */
public class MapperGenerator implements StubGenerator {

    @Override
    public List<JavaFile> generate(ModelDescriptor modelDescriptor, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(modelDescriptor, ctx)) {
            return List.of();
        }
        
        // Skip mapper generation for abstract classes
        if (modelDescriptor.isAbstract()) {
            ctx.env().getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "Skipping mapper generation for abstract entity: " + modelDescriptor.getName()
            );
            return List.of();
        }
        
        return List.of(build(modelDescriptor, ctx));
    }

    @Override
    public JavaFile build(ModelDescriptor modelDescriptor, WriteContext ctx) {
        ctx.env().getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Generating mapper for " + modelDescriptor.getName()
                        + " in package " + modelDescriptor.getBasePackage()
        );

        String entityPackage = modelDescriptor.getPackageName();
        String modelName = modelDescriptor.getName();
        var meta = StubGeneratorUtil.stubMeta(
                modelDescriptor, "mapper", "Mapper", "Mapper", this.getClass()
        );
        String mapperPackage = meta.pkg();
        String mapperName = meta.name();
        String header = meta.header();

        // Types
        ClassName entity = JavaPoetUtils.getClassName(entityPackage, modelName);
        ClassName requestDto = JavaPoetUtils.getClassName(
                entityPackage + ".dto.request", modelName + "RequestDto");
        ClassName responseDto = JavaPoetUtils.getClassName(
                entityPackage + ".dto.response", modelName + "ResponseDto");
        ClassName refDto = JavaPoetUtils.getClassName(
                entityPackage + ".dto.ref", modelName + "Ref");
        ClassName entityMapper = JavaPoetUtils.getClassName(
                "nl.datasteel.crudcraft.runtime.mapper", "EntityMapper");
        ClassName mapperAnn = JavaPoetUtils.getClassName("org.mapstruct","Mapper");
        ClassName beanMapping = JavaPoetUtils.getClassName("org.mapstruct","BeanMapping");
        ClassName mapping = JavaPoetUtils.getClassName("org.mapstruct","Mapping");
        ClassName mappingTarget = JavaPoetUtils.getClassName("org.mapstruct","MappingTarget");
        ClassName nvStrategy = JavaPoetUtils.getClassName("org.mapstruct","NullValuePropertyMappingStrategy");
        ClassName reportingPolicy = JavaPoetUtils.getClassName("org.mapstruct","ReportingPolicy");
        ClassName collectionStrategy = JavaPoetUtils.getClassName("org.mapstruct","CollectionMappingStrategy");
        ClassName injectionStrategy = JavaPoetUtils.getClassName("org.mapstruct", "InjectionStrategy");
        ClassName beanWrapper = JavaPoetUtils.getClassName("org.springframework.beans","BeanWrapperImpl");
        ClassName exceptionCls = JavaPoetUtils.getClassName(
                "nl.datasteel.crudcraft.runtime.exception", "MapperException");
        ClassName uuidClass = ClassName.get(UUID.class);

        // super type: EntityMapper<E, U, R, S, UUID>
        ParameterizedTypeName superType = ParameterizedTypeName.get(
                entityMapper, entity, requestDto, responseDto, refDto, uuidClass
        );

        // Methods
        List<FieldDescriptor> manyToOne = manyToOneFields(modelDescriptor);
        List<FieldDescriptor> relFields = relationFields(modelDescriptor);
        List<FieldDescriptor> abstractRelFields = abstractRelationFields(modelDescriptor);
        MethodSpec fromRequest = fromRequest(modelName, entity, requestDto, mapping, relFields, abstractRelFields);
        MethodSpec update = update(modelName, entity, requestDto, mappingTarget, mapping, relFields, abstractRelFields);
        MethodSpec patch = patch(modelName, entity, requestDto, mappingTarget, beanMapping, nvStrategy, mapping, relFields, abstractRelFields);
        MethodSpec toResponse = toResponse(modelName, entity, responseDto, mapping, manyToOne, abstractRelFields);
        MethodSpec toRef = toRef(entity, refDto);
        MethodSpec getIdFromRequest = getIdFromRequest(requestDto, beanWrapper, uuidClass, exceptionCls);
        List<MethodSpec> refHelpers = manyToOneRefHelpers(modelName, manyToOne);
        List<MethodSpec> idMappers = relationIdHelpers(modelName, relFields, uuidClass, ctx);

        // Determine child mappers we need — include ALL relations (incl. MANY_TO_ONE)
        List<ClassName> uses = determineUses(modelDescriptor);

        // @Mapper annotation
        AnnotationSpec mapperAnnotation = mapperAnnotation(
                mapperAnn, reportingPolicy, collectionStrategy, injectionStrategy, uses);

        // interface
        TypeSpec.Builder mapperBuilder = TypeSpec.interfaceBuilder(mapperName)
                .addJavadoc(header)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(superType)
                .addAnnotation(mapperAnnotation)
                .addMethods(List.of(fromRequest, update, patch, toResponse, toRef, getIdFromRequest));
        refHelpers.forEach(mapperBuilder::addMethod);
        idMappers.forEach(mapperBuilder::addMethod);

        return JavaFile.builder(mapperPackage, mapperBuilder.build())
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    private List<ClassName> determineUses(ModelDescriptor modelDescriptor) {
        List<ClassName> uses = new ArrayList<>();
        for (FieldDescriptor fd : modelDescriptor.getFields()) {
            if (fd.isTargetCrud()
                    && fd.getRelType() != RelationshipType.NONE
                    && fd.getRelType() != RelationshipType.MANY_TO_ONE
                    && !fd.isEmbedded()) {
                String fq = fd.getTargetType();
                String pkg = fq.substring(0, fq.lastIndexOf('.'));
                String simple = fq.substring(fq.lastIndexOf('.') + 1);
                uses.add(JavaPoetUtils.getClassName(pkg + ".mapper", simple + "Mapper"));
            }
        }
        return uses;
    }

    private List<FieldDescriptor> manyToOneFields(ModelDescriptor modelDescriptor) {
        List<FieldDescriptor> fields = new ArrayList<>();
        for (FieldDescriptor fd : modelDescriptor.getFields()) {
            // Skip abstract target types - they cannot be used in ref mappings
            if (fd.isTargetCrud() && fd.getRelType() == RelationshipType.MANY_TO_ONE 
                    && !fd.isTargetAbstract()) {
                fields.add(fd);
            }
        }
        return fields;
    }

    private List<FieldDescriptor> relationFields(ModelDescriptor modelDescriptor) {
        List<FieldDescriptor> fields = new ArrayList<>();
        for (FieldDescriptor fd : modelDescriptor.getFields()) {
            // Skip abstract target types - they cannot be instantiated in mappers
            if (fd.isTargetCrud() && fd.getRelType() != RelationshipType.NONE 
                    && !fd.isEmbedded() && !fd.isTargetAbstract()) {
                fields.add(fd);
            }
        }
        return fields;
    }

    private List<FieldDescriptor> abstractRelationFields(ModelDescriptor modelDescriptor) {
        List<FieldDescriptor> fields = new ArrayList<>();
        for (FieldDescriptor fd : modelDescriptor.getFields()) {
            // Find all abstract relation fields that need to be ignored in mappings
            if (fd.isTargetCrud() && fd.getRelType() != RelationshipType.NONE 
                    && !fd.isEmbedded() && fd.isTargetAbstract()) {
                fields.add(fd);
            }
        }
        return fields;
    }

    private MethodSpec fromRequest(String modelName, ClassName entity, ClassName requestDto, ClassName mapping,
                                   List<FieldDescriptor> relFields, List<FieldDescriptor> abstractRelFields) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("fromRequest")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(entity)
                .addParameter(requestDto, "request");
        for (FieldDescriptor fd : relFields) {
            String fq = fd.getTargetType();
            String simple = fq.substring(fq.lastIndexOf('.') + 1);
            TypeName fieldType = TypeName.get(fd.getType());
            boolean isSet = isSet(fieldType);
            boolean isList = isList(fieldType);
            String qualifier = modelName + "Map" + simple + (isSet ? "Set" : isList ? "List" : "");
            builder.addAnnotation(AnnotationSpec.builder(mapping)
                    .addMember("target", "$S", fd.getName())
                    .addMember("source", "$S", idFieldName(fd))
                    .addMember("qualifiedByName", "$S", qualifier)
                    .build());
        }
        // Add ignore mappings for abstract relation fields
        for (FieldDescriptor fd : abstractRelFields) {
            builder.addAnnotation(AnnotationSpec.builder(mapping)
                    .addMember("target", "$S", fd.getName())
                    .addMember("ignore", "$L", true)
                    .build());
        }
        return builder.build();
    }

    private MethodSpec update(String modelName, ClassName entity, ClassName requestDto, ClassName mappingTarget,
                              ClassName mapping, List<FieldDescriptor> relFields, List<FieldDescriptor> abstractRelFields) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("update")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(entity)
                .addParameter(ParameterSpec.builder(entity, "entity").addAnnotation(mappingTarget).build())
                .addParameter(requestDto, "request");
        for (FieldDescriptor fd : relFields) {
            String fq = fd.getTargetType();
            String simple = fq.substring(fq.lastIndexOf('.') + 1);
            TypeName fieldType = TypeName.get(fd.getType());
            boolean isSet = isSet(fieldType);
            boolean isList = isList(fieldType);
            String qualifier = modelName + "Map" + simple + (isSet ? "Set" : isList ? "List" : "");
            builder.addAnnotation(AnnotationSpec.builder(mapping)
                    .addMember("target", "$S", fd.getName())
                    .addMember("source", "$S", idFieldName(fd))
                    .addMember("qualifiedByName", "$S", qualifier)
                    .build());
        }
        // Add ignore mappings for abstract relation fields
        for (FieldDescriptor fd : abstractRelFields) {
            builder.addAnnotation(AnnotationSpec.builder(mapping)
                    .addMember("target", "$S", fd.getName())
                    .addMember("ignore", "$L", true)
                    .build());
        }
        return builder.build();
    }

    private MethodSpec patch(String modelName, ClassName entity, ClassName requestDto, ClassName mappingTarget,
                             ClassName beanMapping, ClassName nvStrategy, ClassName mapping,
                             List<FieldDescriptor> relFields, List<FieldDescriptor> abstractRelFields) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("patch")
                .addAnnotation(Override.class)
                .addAnnotation(AnnotationSpec.builder(beanMapping)
                        .addMember("nullValuePropertyMappingStrategy", "$T.IGNORE", nvStrategy)
                        .build())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(entity)
                .addParameter(ParameterSpec.builder(entity, "entity").addAnnotation(mappingTarget).build())
                .addParameter(requestDto, "request");
        for (FieldDescriptor fd : relFields) {
            String fq = fd.getTargetType();
            String simple = fq.substring(fq.lastIndexOf('.') + 1);
            TypeName fieldType = TypeName.get(fd.getType());
            boolean isSet = isSet(fieldType);
            boolean isList = isList(fieldType);
            String qualifier = modelName + "Map" + simple + (isSet ? "Set" : isList ? "List" : "");
            builder.addAnnotation(AnnotationSpec.builder(mapping)
                    .addMember("target", "$S", fd.getName())
                    .addMember("source", "$S", idFieldName(fd))
                    .addMember("qualifiedByName", "$S", qualifier)
                    .build());
        }
        // Add ignore mappings for abstract relation fields
        for (FieldDescriptor fd : abstractRelFields) {
            builder.addAnnotation(AnnotationSpec.builder(mapping)
                    .addMember("target", "$S", fd.getName())
                    .addMember("ignore", "$L", true)
                    .build());
        }
        return builder.build();
    }

    private MethodSpec toResponse(String modelName, ClassName entity, ClassName responseDto, ClassName mapping,
                                  List<FieldDescriptor> manyToOne, List<FieldDescriptor> abstractRelFields) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("toResponse")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(responseDto)
                .addParameter(entity, "entity");
        for (FieldDescriptor fd : manyToOne) {
            String fq = fd.getTargetType();
            String simple = fq.substring(fq.lastIndexOf('.') + 1);
            builder.addAnnotation(AnnotationSpec.builder(mapping)
                    .addMember("target", "$S", fd.getName())
                    .addMember("qualifiedByName", "$S", modelName + "To" + simple + "Ref")
                    .build());
        }
        // Add ignore mappings for abstract relation fields in response
        for (FieldDescriptor fd : abstractRelFields) {
            builder.addAnnotation(AnnotationSpec.builder(mapping)
                    .addMember("target", "$S", fd.getName())
                    .addMember("ignore", "$L", true)
                    .build());
        }
        return builder.build();
    }

    private MethodSpec toRef(ClassName entity, ClassName refDto) {
        return MethodSpec.methodBuilder("toRef")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(refDto)
                .addParameter(entity, "entity")
                .build();
    }

    private MethodSpec getIdFromRequest(ClassName requestDto, ClassName beanWrapper,
                                        ClassName uuidClass, ClassName exceptionCls) {
        return MethodSpec.methodBuilder("getIdFromRequest")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                .returns(uuidClass)
                .addParameter(requestDto, "request")
                .beginControlFlow("try")
                .addStatement("var wrapper = new $T(request)", beanWrapper)
                .addStatement("Object idVal = wrapper.getPropertyValue($S)", "id")
                .addStatement("return ($T) idVal", uuidClass)
                .nextControlFlow("catch (Exception e)")
                .addStatement("throw new $T($S + request.getClass(), e)", exceptionCls, "Failed to read 'id' property from request DTO: ")
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
                String pkg = fq.substring(0, fq.lastIndexOf('.'));
                String simple = fq.substring(fq.lastIndexOf('.') + 1);
                ClassName target = JavaPoetUtils.getClassName(pkg, simple);
                ClassName ref = JavaPoetUtils.getClassName(pkg + ".dto.ref", simple + "Ref");
                String param = Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
                String methodName = "to" + simple + "Ref";
                String qualifierName = modelName + "To" + simple + "Ref";
                helpers.add(MethodSpec.methodBuilder(methodName)
                        .addAnnotation(AnnotationSpec.builder(named).addMember("value", "$S", qualifierName).build())
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
            if (base.endsWith("s") && base.length() > 1) {
                base = base.substring(0, base.length() - 1);
            }
            return base + "Ids";
        }
        return fd.getName() + "Id";
    }

    private List<MethodSpec> relationIdHelpers(String modelName, List<FieldDescriptor> fields,
                                               ClassName uuidClass, WriteContext ctx) {
        List<MethodSpec> helpers = new ArrayList<>();
        ClassName named = JavaPoetUtils.getClassName("org.mapstruct", "Named");
        for (FieldDescriptor fd : fields) {
            String fq = fd.getTargetType();
            String pkg = fq.substring(0, fq.lastIndexOf('.'));
            String simple = fq.substring(fq.lastIndexOf('.') + 1);
            ClassName target = JavaPoetUtils.getClassName(pkg, simple);
            String baseName = "map" + simple;
            String qualifierName = modelName + "Map" + simple;

            boolean simpleId = hasSingleId(fq, ctx);

            MethodSpec.Builder singleBuilder = MethodSpec.methodBuilder(baseName)
                    .addAnnotation(AnnotationSpec.builder(named).addMember("value", "$S", qualifierName).build())
                    .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                    .returns(target)
                    .addParameter(uuidClass, "id")
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
                TypeName paramType = ParameterizedTypeName.get(raw, uuidClass);
                String methodName = baseName + (isSet ? "Set" : "List");
                String collectionQualifier = modelName + "Map" + simple + (isSet ? "Set" : "List");
                MethodSpec collection = MethodSpec.methodBuilder(methodName)
                        .addAnnotation(AnnotationSpec.builder(named).addMember("value", "$S", collectionQualifier).build())
                        .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                        .returns(returnType)
                        .addParameter(paramType, "ids")
                        .beginControlFlow("if (ids == null)")
                        .addStatement("return null")
                        .endControlFlow()
                        .addStatement("return ids.stream().map(this::$L).collect($T.to$L())", baseName, ClassName.get(Collectors.class), isSet ? "Set" : "List")
                        .build();
                helpers.add(collection);
            }
        }
        return helpers;
    }

    private boolean hasSingleId(String fqcn, WriteContext ctx) {
        TypeElement te = ctx.findTypeElement(fqcn);
        if (te == null) return true;
        long idCount = te.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .filter(e -> e.getAnnotation(Id.class) != null)
                .count();
        boolean hasEmbedded = te.getEnclosedElements().stream()
                .anyMatch(e -> e.getKind() == ElementKind.FIELD && e.getAnnotation(EmbeddedId.class) != null);
        return idCount == 1 && !hasEmbedded;
    }

    private boolean isSet(TypeName type) {
        return type instanceof ParameterizedTypeName pt && pt.rawType.equals(ClassName.get(Set.class));
    }

    private boolean isList(TypeName type) {
        return type instanceof ParameterizedTypeName pt && pt.rawType.equals(ClassName.get(List.class));
    }

    private AnnotationSpec mapperAnnotation(ClassName mapperAnn, ClassName reportingPolicy,
                                            ClassName collectionStrategy, ClassName injectionStrategy,
                                            List<ClassName> uses) {
        AnnotationSpec.Builder mapperAnnotation = AnnotationSpec.builder(mapperAnn)
                .addMember("componentModel", "$S", "spring")
                .addMember("unmappedTargetPolicy", "$T.IGNORE", reportingPolicy)
                .addMember("collectionMappingStrategy", "$T.TARGET_IMMUTABLE", collectionStrategy)
                .addMember("injectionStrategy", "$T.FIELD", injectionStrategy);
        if (!uses.isEmpty()) {
            CodeBlock.Builder cb = CodeBlock.builder();
            for (int i = 0; i < uses.size(); i++) {
                cb.add("$T.class", uses.get(i));
                if (i < uses.size() - 1) cb.add(", ");
            }
            mapperAnnotation.addMember("uses", "{$L}", cb.build());
        }
        return mapperAnnotation.build();
    }

    @Override public boolean requiresCrudEntity() { return true; }
    @Override public int order() { return 2; }
}

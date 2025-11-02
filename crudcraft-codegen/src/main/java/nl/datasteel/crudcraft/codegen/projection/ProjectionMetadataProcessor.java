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
package nl.datasteel.crudcraft.codegen.projection;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;

/**
 * Annotation processor that scans DTO classes and emits {@link ProjectionMetadata}
 * implementations along with a registry to expose them.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ProjectionMetadataProcessor extends AbstractProcessor {

    /**
     * List of generated metadata classes.
     */
    private final List<ClassName> generatedMetadata = new ArrayList<>();

    /**
     * Types utility for type operations.
     */
    private Types types;

    /**
     * Elements utility for element operations.
     */
    private Elements elements;

    /**
     * Initializes the processor with the processing environment.
     *
     * @param processingEnv the processing environment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.types = processingEnv.getTypeUtils();
        this.elements = processingEnv.getElementUtils();
    }

    /**
     * Processes the annotations in the round environment.
     * Scans for DTO classes and generates metadata for them.
     *
     * @param annotations the set of annotations
     * @param roundEnv the round environment
     * @return false to indicate no further processing is needed
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }
            TypeElement type = (TypeElement) element;
            if (!isDto(type)) {
                continue;
            }
            try {
                generateMetadata(type);
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Failed to generate projection metadata for "
                                + type.getQualifiedName() + ": " + e.getMessage());
            }
        }

        if (roundEnv.processingOver()) {
            try {
                generateRegistry();
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Failed to generate ProjectionMetadata registry: "
                                + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Checks if the given TypeElement is a DTO class.
     * A DTO is identified by its package containing ".dto." and its simple name ending with "Dto".
     *
     * @param type the TypeElement to check
     * @return true if the TypeElement is a DTO, false otherwise
     */
    private boolean isDto(TypeElement type) {
        String pkg = elements.getPackageOf(type).getQualifiedName().toString();
        String simple = type.getSimpleName().toString();
        boolean inDtoPkg = pkg.endsWith(".dto") || pkg.contains(".dto.");
        return inDtoPkg && simple.endsWith("Dto") && !simple.endsWith("ProjectionMetadata");
    }

    /**
     * Generates the projection metadata for the given DTO class.
     * This includes creating a metadata class that implements ProjectionMetadata
     * and contains attributes for each field in the DTO.
     *
     * @param dto the TypeElement representing the DTO class
     * @throws IOException if there is an error writing the generated file
     */
    private void generateMetadata(TypeElement dto) throws IOException {
        ClassName dtoClass = ClassName.get(dto);
        String pkg = dtoClass.packageName();
        String metadataSimple = dtoClass.simpleName() + "ProjectionMetadata";
        ClassName metadataClass = ClassName.get(pkg, metadataSimple);

        ClassName projectionMetadata = ClassName.get(ProjectionMetadata.class);
        TypeName metadataInterface = ParameterizedTypeName.get(projectionMetadata, dtoClass);
        ClassName attributeClass = projectionMetadata.nestedClass("Attribute");

        TypeName metadataWildcard = ParameterizedTypeName.get(projectionMetadata,
                WildcardTypeName.subtypeOf(Object.class));
        TypeName listWildcard = ParameterizedTypeName.get(ClassName.get(List.class),
                WildcardTypeName.subtypeOf(Object.class));
        TypeName biConsumer = ParameterizedTypeName.get(ClassName.get(BiConsumer.class),
                ClassName.get(Object.class), listWildcard);
        ClassName supplierCls = ClassName.get("java.util.function", "Supplier");
        TypeName supplierType = ParameterizedTypeName.get(supplierCls, metadataWildcard);
        TypeSpec attrClass = TypeSpec.classBuilder("Attr")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addSuperinterface(attributeClass)
                .addField(String.class, "path", Modifier.PRIVATE, Modifier.FINAL)
                .addField(supplierType, "nested", Modifier.PRIVATE, Modifier.FINAL)
                .addField(TypeName.BOOLEAN, "collection", Modifier.PRIVATE, Modifier.FINAL)
                .addField(biConsumer, "mutator", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(String.class, "path")
                        .addParameter(supplierType, "nested")
                        .addParameter(TypeName.BOOLEAN, "collection")
                        .addParameter(biConsumer, "mutator")
                        .addStatement("this.path = path")
                        .addStatement("this.nested = nested")
                        .addStatement("this.collection = collection")
                        .addStatement("this.mutator = mutator")
                        .build())
                .addMethod(MethodSpec.methodBuilder("path")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addStatement("return path")
                        .build())
                .addMethod(MethodSpec.methodBuilder("nested")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(metadataWildcard)
                        .addStatement("return nested.get()")
                        .build())
                .addMethod(MethodSpec.methodBuilder("collection")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.BOOLEAN)
                        .addStatement("return collection")
                        .build())
                .addMethod(MethodSpec.methodBuilder("mutator")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(biConsumer)
                        .addStatement("return mutator")
                        .build())
                .build();

        List<CodeBlock> attributeBlocks = new ArrayList<>();
        for (Element e : dto.getEnclosedElements()) {
            if (e.getKind() != ElementKind.FIELD) {
                continue;
            }
            TypeMirror fieldType = e.asType();
            String fieldName = e.getSimpleName().toString();
            String path = fieldName;
            ProjectionField projectionField = e.getAnnotation(ProjectionField.class);
            if (projectionField != null && !projectionField.value().isEmpty()) {
                path = projectionField.value();
            }
            boolean collection = isCollection(fieldType);
            TypeMirror valueType = fieldType;
            if (collection && fieldType instanceof DeclaredType dt
                    && !dt.getTypeArguments().isEmpty()) {
                valueType = dt.getTypeArguments().getFirst();
            }
            TypeName valueTypeName = TypeName.get(valueType);

            CodeBlock nestedBlock = CodeBlock.of("() -> null");
            TypeElement valueTypeElement = (TypeElement) types.asElement(valueType);
            if (valueTypeElement != null && isDto(valueTypeElement)) {
                ClassName nestedMetadata = ClassName.get(
                        elements.getPackageOf(valueTypeElement).getQualifiedName().toString(),
                        valueTypeElement.getSimpleName().toString()
                                + "ProjectionMetadata");
                nestedBlock = CodeBlock.of("() -> new $T()", nestedMetadata);
            }

            CodeBlock mutatorBlock = CodeBlock.of("null");
            if (collection) {
                String setter = "set" + capitalize(fieldName);
                TypeMirror erased = types.erasure(fieldType);
                TypeElement setType = elements.getTypeElement("java.util.Set");
                if (types.isAssignable(erased, types.erasure(setType.asType()))) {
                    mutatorBlock = CodeBlock.of(
                            "(d,v)->(($T)d).$L(new $T<$T>((java.util.Collection<$T>) v))",
                            dtoClass, setter, ClassName.get("java.util",
                                    "HashSet"), valueTypeName, valueTypeName);
                } else {
                    mutatorBlock = CodeBlock.of("(d,v)->(($T)d).$L(($T)v)",
                            dtoClass, setter, ClassName.get(List.class));
                }
            }

            attributeBlocks.add(CodeBlock.of("new Attr($S,$L,$L,$L)", path, nestedBlock,
                    collection, mutatorBlock));
        }

        FieldSpec attributesField = FieldSpec.builder(
                ParameterizedTypeName.get(ClassName.get(List.class), attributeClass),
                "ATTRIBUTES",
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$T.of($L)", List.class, CodeBlock.join(attributeBlocks, ","))
                .build();

        MethodSpec dtoType = MethodSpec.methodBuilder("dtoType")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(Class.class), dtoClass))
                .addStatement("return $T.class", dtoClass)
                .build();

        MethodSpec attributes = MethodSpec.methodBuilder("attributes")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), attributeClass))
                .addStatement("return ATTRIBUTES")
                .build();

        TypeSpec metadataType = TypeSpec.classBuilder(metadataClass)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(metadataInterface)
                .addField(attributesField)
                .addMethod(dtoType)
                .addMethod(attributes)
                .addType(attrClass)
                .build();

        JavaFile.builder(pkg, metadataType)
                .skipJavaLangImports(true)
                .indent("    ")
                .build()
                .writeTo(processingEnv.getFiler());
        generatedMetadata.add(metadataClass);
    }

    /**
     * Checks if the given type is a collection type.
     *
     * @param type the TypeMirror to check
     * @return true if the type is a collection, false otherwise
     */
    private boolean isCollection(TypeMirror type) {
        TypeElement collectionType = elements.getTypeElement("java.util.Collection");
        return types.isAssignable(types.erasure(type), types.erasure(collectionType.asType()));
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param in the input string
     * @return the input string with the first letter capitalized
     */
    private String capitalize(String in) {
        return in.substring(0, 1).toUpperCase() + in.substring(1);
    }

    /**
     * Generates the registry class that holds all generated projection metadata.
     * This class implements the ProjectionMetadataRegistry interface.
     *
     * @throws IOException if there is an error writing the generated file
     */
    private void generateRegistry() throws IOException {
        if (generatedMetadata.isEmpty()) {
            return;
        }
        ClassName projectionMetadata = ClassName.get(ProjectionMetadata.class);
        ClassName registryInterface = ClassName.get(
                "nl.datasteel.crudcraft.codegen.projection",
                "ProjectionMetadataRegistry");

        TypeName metadataWildcard = ParameterizedTypeName.get(projectionMetadata,
                WildcardTypeName.subtypeOf(Object.class));
        TypeName classWildcard = ParameterizedTypeName.get(ClassName.get(Class.class),
                WildcardTypeName.subtypeOf(Object.class));
        TypeName mapType = ParameterizedTypeName.get(ClassName.get(java.util.Map.class),
                classWildcard, metadataWildcard);

        FieldSpec mapField = FieldSpec.builder(mapType, "metadata",
                        Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T<>()", ClassName.get(java.util.HashMap.class))
                .build();

        MethodSpec.Builder ctor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        for (ClassName mc : generatedMetadata) {
            ctor.addStatement("register(new $T())", mc);
        }

        TypeVariableName dtype = TypeVariableName.get("D");
        TypeName metadataParam = ParameterizedTypeName.get(projectionMetadata, dtype);
        MethodSpec register = MethodSpec.methodBuilder("register")
                .addModifiers(Modifier.PRIVATE)
                .addTypeVariable(dtype)
                .addParameter(metadataParam, "pm")
                .addStatement("metadata.put(pm.dtoType(), pm)")
                .build();

        MethodSpec get = MethodSpec.methodBuilder("getMetadata")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(dtype)
                .returns(ParameterizedTypeName.get(projectionMetadata, dtype))
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), dtype),
                        "dtoType")
                .addStatement("return ($T) metadata.get(dtoType)",
                        ParameterizedTypeName.get(projectionMetadata, dtype))
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build())
                .build();

        TypeSpec registry = TypeSpec.classBuilder("GeneratedProjectionMetadataRegistry")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(registryInterface)
                .addField(mapField)
                .addMethod(ctor.build())
                .addMethod(register)
                .addMethod(get)
                .build();

        JavaFile.builder("nl.datasteel.crudcraft.projection.mapping", registry)
                .skipJavaLangImports(true)
                .indent("    ")
                .build()
                .writeTo(processingEnv.getFiler());
    }
}


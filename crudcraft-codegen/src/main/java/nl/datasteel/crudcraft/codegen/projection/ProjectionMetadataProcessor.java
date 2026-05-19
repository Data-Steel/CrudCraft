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

package nl.datasteel.crudcraft.codegen.projection;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import com.palantir.javapoet.TypeVariableName;
import com.palantir.javapoet.WildcardTypeName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;
import nl.datasteel.crudcraft.codegen.util.JavaPoetUtils;


/**
 * Annotation processor that scans DTO classes and emits {@link ProjectionMetadata} implementations
 * along with a registry to expose them.
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ProjectionMetadataProcessor extends AbstractProcessor {

    /** List of generated metadata classes. */
    private final List<ClassName> generatedMetadata = new ArrayList<>();

    /** Whether the projection metadata registry has already been generated. */
    private final AtomicBoolean registryGenerated = new AtomicBoolean();

    /** Types utility for type operations. */
    private Types types;

    /** Elements utility for element operations. */
    private Elements elements;

    /** Creates the projection metadata processor. */
    public ProjectionMetadataProcessor() {}

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
     * Processes the annotations in the round environment. Scans for DTO classes and generates
     * metadata for them.
     *
     * @param annotations the set of annotations
     * @param roundEnv the round environment
     * @return false to indicate no further processing is needed
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean generatedInRound = false;
        for (Element element : roundEnv.getRootElements()) {
            if (element.getKind() != ElementKind.CLASS && element.getKind() != ElementKind.RECORD) {
                continue;
            }
            TypeElement type = (TypeElement) element;
            if (type.getSimpleName().toString().endsWith("ProjectionMetadata")) {
                continue;
            }
            if (!isDto(type)) {
                continue;
            }
            try {
                generateMetadata(type);
                generatedInRound = true;
            } catch (IOException e) {
                processingEnv
                        .getMessager()
                        .printMessage(
                                Diagnostic.Kind.ERROR,
                                "Failed to generate projection metadata for "
                                        + type.getQualifiedName()
                                        + ": "
                                        + e.getMessage());
            }
        }

        if (shouldGenerateRegistry(roundEnv, generatedInRound)) {
            try {
                generateRegistry();
                registryGenerated.set(true);
            } catch (IOException e) {
                processingEnv
                        .getMessager()
                        .printMessage(
                                Diagnostic.Kind.ERROR,
                                "Failed to generate ProjectionMetadata registry: "
                                        + e.getMessage());
            }
        }
        return false;
    }

    private boolean shouldGenerateRegistry(RoundEnvironment roundEnv, boolean generatedInRound) {
        if (registryGenerated.get() || generatedMetadata.isEmpty()) {
            return false;
        }
        return generatedInRound || roundEnv.processingOver();
    }

    /**
     * Checks if the given TypeElement is a DTO class. A DTO is identified by its package containing
     * ".dto." and its simple name ending with "Dto".
     *
     * @param type the TypeElement to check
     * @return true if the TypeElement is a DTO, false otherwise
     */
    private boolean isDto(TypeElement type) {
        String pkg = elements.getPackageOf(type).getQualifiedName().toString();
        String simple = type.getSimpleName().toString();
        boolean inDtoPkg = pkg.endsWith(".dto") || pkg.contains(".dto.");
        return inDtoPkg && simple.endsWith("Dto");
    }

    /**
     * Generates the projection metadata for the given DTO class. This includes creating a metadata
     * class that implements ProjectionMetadata and contains attributes for each field in the DTO.
     *
     * @param dto the TypeElement representing the DTO class
     * @throws IOException if there is an error writing the generated file
     */
    private void generateMetadata(TypeElement dto) throws IOException {
        ClassName dtoClass = ClassName.get(dto);
        String pkg = dtoClass.packageName();
        String metadataSimple = dtoClass.simpleName() + "ProjectionMetadata";
        ClassName metadataClass = ClassName.get(pkg, metadataSimple);

        ClassName projectionMetadata =
                ClassName.get(
                        "nl.datasteel.crudcraft.runtime.projection.metadata", "ProjectionMetadata");
        TypeName metadataInterface = ParameterizedTypeName.get(projectionMetadata, dtoClass);
        ClassName attributeClass = projectionMetadata.nestedClass("Attribute");

        TypeName metadataWildcard =
                ParameterizedTypeName.get(
                        projectionMetadata, WildcardTypeName.subtypeOf(Object.class));
        TypeName listWildcard =
                ParameterizedTypeName.get(
                        ClassName.get(List.class), WildcardTypeName.subtypeOf(Object.class));
        TypeName biConsumer =
                ParameterizedTypeName.get(
                        ClassName.get(BiConsumer.class), ClassName.get(Object.class), listWildcard);
        ClassName supplierCls = ClassName.get("java.util.function", "Supplier");
        TypeName supplierType = ParameterizedTypeName.get(supplierCls, metadataWildcard);
        TypeSpec attrClass =
                TypeSpec.classBuilder("Attr")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                        .addSuperinterface(attributeClass)
                        .addField(String.class, "dtoFieldName", Modifier.PRIVATE, Modifier.FINAL)
                        .addField(String.class, "path", Modifier.PRIVATE, Modifier.FINAL)
                        .addField(supplierType, "nested", Modifier.PRIVATE, Modifier.FINAL)
                        .addField(TypeName.BOOLEAN, "collection", Modifier.PRIVATE, Modifier.FINAL)
                        .addField(biConsumer, "mutator", Modifier.PRIVATE, Modifier.FINAL)
                        .addMethod(
                                MethodSpec.constructorBuilder()
                                        .addParameter(String.class, "dtoFieldName")
                                        .addParameter(String.class, "path")
                                        .addParameter(supplierType, "nested")
                                        .addParameter(TypeName.BOOLEAN, "collection")
                                        .addParameter(biConsumer, "mutator")
                                        .addStatement("this.dtoFieldName = dtoFieldName")
                                        .addStatement("this.path = path")
                                        .addStatement("this.nested = nested")
                                        .addStatement("this.collection = collection")
                                        .addStatement("this.mutator = mutator")
                                        .build())
                        .addMethod(
                                MethodSpec.methodBuilder("dtoFieldName")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .returns(String.class)
                                        .addStatement("return dtoFieldName")
                                        .build())
                        .addMethod(
                                MethodSpec.methodBuilder("path")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .returns(String.class)
                                        .addStatement("return path")
                                        .build())
                        .addMethod(
                                MethodSpec.methodBuilder("nested")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .returns(metadataWildcard)
                                        .addStatement("return nested.get()")
                                        .build())
                        .addMethod(
                                MethodSpec.methodBuilder("collection")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .returns(TypeName.BOOLEAN)
                                        .addStatement("return collection")
                                        .build())
                        .addMethod(
                                MethodSpec.methodBuilder("mutator")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .returns(biConsumer)
                                        .addStatement("return mutator")
                                        .build())
                        .build();

        List<CodeBlock> attributeBlocks = new ArrayList<>();
        boolean recordDto = dto.getKind() == ElementKind.RECORD;
        for (Element e : dto.getEnclosedElements()) {
            if (e.getKind() != ElementKind.FIELD && e.getKind() != ElementKind.RECORD_COMPONENT) {
                continue;
            }
            if (recordDto && e.getKind() == ElementKind.FIELD) {
                continue;
            }
            if (e.getKind() == ElementKind.FIELD && e.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            TypeMirror fieldType = e.asType();
            String fieldName = e.getSimpleName().toString();
            String path = fieldName;
            ProjectionField projectionField = e.getAnnotation(ProjectionField.class);
            if (projectionField != null && !projectionField.value().isEmpty()) {
                path = projectionField.value();
                validateProjectionPath(dto, e, path);
            }
            boolean collection = isCollection(fieldType);
            TypeMirror valueType = fieldType;
            if (collection
                    && fieldType instanceof DeclaredType dt
                    && !dt.getTypeArguments().isEmpty()) {
                valueType = dt.getTypeArguments().getFirst();
            }
            TypeName valueTypeName = TypeName.get(valueType);

            CodeBlock nestedBlock = CodeBlock.of("() -> null");
            TypeElement valueTypeElement = (TypeElement) types.asElement(valueType);
            if (valueTypeElement != null && isDto(valueTypeElement)) {
                ClassName nestedMetadata =
                        ClassName.get(
                                elements.getPackageOf(valueTypeElement)
                                        .getQualifiedName()
                                        .toString(),
                                valueTypeElement.getSimpleName().toString() + "ProjectionMetadata");
                nestedBlock = CodeBlock.of("() -> new $T()", nestedMetadata);
            }

            CodeBlock mutatorBlock = CodeBlock.of("null");
            if (collection && !recordDto) {
                String setter = "set" + capitalize(fieldName);
                TypeMirror erased = types.erasure(fieldType);
                TypeElement setType = elements.getTypeElement("java.util.Set");
                if (types.isAssignable(erased, types.erasure(setType.asType()))) {
                    mutatorBlock =
                            CodeBlock.of(
                                    "(d,v)->(($T)d).$L(new $T<$T>((java.util.Collection<$T>) v))",
                                    dtoClass,
                                    setter,
                                    ClassName.get("java.util", "HashSet"),
                                    valueTypeName,
                                    valueTypeName);
                } else {
                    mutatorBlock =
                            CodeBlock.of(
                                    "(d,v)->(($T)d).$L(($T)v)",
                                    dtoClass,
                                    setter,
                                    ClassName.get(List.class));
                }
            }

            attributeBlocks.add(
                    CodeBlock.of(
                            "new Attr($S,$S,$L,$L,$L)",
                            fieldName,
                            path,
                            nestedBlock,
                            collection,
                            mutatorBlock));
        }

        FieldSpec attributesField =
                FieldSpec.builder(
                                ParameterizedTypeName.get(
                                        ClassName.get(List.class), attributeClass),
                                "ATTRIBUTES",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .initializer("$T.of($L)", List.class, CodeBlock.join(attributeBlocks, ","))
                        .build();

        MethodSpec dtoType =
                MethodSpec.methodBuilder("dtoType")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ParameterizedTypeName.get(ClassName.get(Class.class), dtoClass))
                        .addStatement("return $T.class", dtoClass)
                        .build();

        MethodSpec attributes =
                MethodSpec.methodBuilder("attributes")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(
                                ParameterizedTypeName.get(
                                        ClassName.get(List.class), attributeClass))
                        .addStatement("return ATTRIBUTES")
                        .build();

        TypeSpec metadataType =
                TypeSpec.classBuilder(metadataClass)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addSuperinterface(metadataInterface)
                        .addField(attributesField)
                        .addMethod(dtoType)
                        .addMethod(attributes)
                        .addType(attrClass)
                        .build();

        JavaPoetUtils.javaFile(pkg, metadataType)
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
        if (type == null || type.getKind().isPrimitive()) {
            return false;
        }
        TypeElement collectionType = elements.getTypeElement("java.util.Collection");
        if (collectionType == null) {
            return false;
        }
        return types.isAssignable(types.erasure(type), types.erasure(collectionType.asType()));
    }

    private void validateProjectionPath(TypeElement dto, Element field, String path) {
        TypeElement entityType = resolveEntityType(dto);
        if (entityType == null || path == null || path.isBlank()) {
            return;
        }
        if (canResolveProjectionPath(entityType, path)) {
            return;
        }
        if (isGeneratedCrudCraftType(dto)) {
            String relativePath = stripAliasPrefix(path);
            if (!relativePath.equals(path) && canResolveProjectionPath(entityType, relativePath)) {
                return;
            }
        }
        warnInvalidProjectionPath(field, path, entityType);
    }

    private boolean canResolveProjectionPath(TypeElement entityType, String path) {
        TypeMirror current = entityType.asType();
        for (String segment : path.split("\\.")) {
            if (segment.isBlank()) {
                return false;
            }
            current = unwrapCollection(current);
            TypeElement currentType = typeElement(current);
            if (currentType == null) {
                return false;
            }
            TypeMirror next = findReadableMemberType(currentType, segment);
            if (next == null) {
                return false;
            }
            current = next;
        }
        return true;
    }

    private boolean isGeneratedCrudCraftType(TypeElement type) {
        String docComment = elements.getDocComment(type);
        return docComment != null && docComment.contains("@CrudCraft:generated");
    }

    private String stripAliasPrefix(String path) {
        int dot = path.indexOf('.');
        if (dot <= 0 || dot == path.length() - 1 || path.contains("..")) {
            return path;
        }
        return path.substring(dot + 1);
    }

    private TypeElement resolveEntityType(TypeElement dto) {
        String dtoPackage = elements.getPackageOf(dto).getQualifiedName().toString();
        int dtoMarker = dtoPackage.indexOf(".dto");
        if (dtoMarker < 0) {
            return null;
        }
        String entityPackageName = dtoPackage.substring(0, dtoMarker);
        String dtoBaseName = dto.getSimpleName().toString();
        for (String suffix : List.of("RequestDto", "ResponseDto", "Ref")) {
            if (dtoBaseName.endsWith(suffix)) {
                dtoBaseName = dtoBaseName.substring(0, dtoBaseName.length() - suffix.length());
                break;
            }
        }

        TypeElement exact = elements.getTypeElement(entityPackageName + "." + dtoBaseName);
        if (exact != null) {
            return exact;
        }

        PackageElement entityPackage = elements.getPackageElement(entityPackageName);
        if (entityPackage == null) {
            return null;
        }
        TypeElement best = null;
        for (Element enclosed : entityPackage.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.CLASS
                    && enclosed.getKind() != ElementKind.RECORD) {
                continue;
            }
            String candidate = enclosed.getSimpleName().toString();
            if (dtoBaseName.startsWith(candidate)
                    && (best == null
                            || candidate.length() > best.getSimpleName().toString().length())) {
                best = (TypeElement) enclosed;
            }
        }
        return best;
    }

    private TypeMirror unwrapCollection(TypeMirror type) {
        if (isCollection(type)
                && type instanceof DeclaredType declared
                && !declared.getTypeArguments().isEmpty()) {
            return declared.getTypeArguments().getFirst();
        }
        return type;
    }

    private TypeElement typeElement(TypeMirror type) {
        Element element = types.asElement(type);
        return element instanceof TypeElement typeElement ? typeElement : null;
    }

    private TypeMirror findReadableMemberType(TypeElement type, String name) {
        String getter = "get" + capitalize(name);
        String booleanGetter = "is" + capitalize(name);
        for (Element member : type.getEnclosedElements()) {
            if (member.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            if ((member.getKind() == ElementKind.FIELD
                            || member.getKind() == ElementKind.RECORD_COMPONENT)
                    && member.getSimpleName().contentEquals(name)) {
                return member.asType();
            }
            if (member.getKind() == ElementKind.METHOD
                    && member instanceof ExecutableElement method
                    && method.getParameters().isEmpty()
                    && (method.getSimpleName().contentEquals(getter)
                            || method.getSimpleName().contentEquals(booleanGetter))) {
                return method.getReturnType();
            }
        }
        for (TypeMirror supertype : types.directSupertypes(type.asType())) {
            TypeElement superElement = typeElement(supertype);
            if (superElement == null
                    || superElement.getQualifiedName().contentEquals("java.lang.Object")) {
                continue;
            }
            TypeMirror inherited = findReadableMemberType(superElement, name);
            if (inherited != null) {
                return inherited;
            }
        }
        return null;
    }

    private void warnInvalidProjectionPath(Element field, String path, TypeElement entityType) {
        processingEnv
                .getMessager()
                .printMessage(
                        Diagnostic.Kind.ERROR,
                        "@ProjectionField path '"
                                + path
                                + "' could not be resolved against entity "
                                + entityType.getQualifiedName(),
                        field);
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
     * Generates the registry class that holds all generated projection metadata. This class
     * implements the ProjectionMetadataRegistry interface.
     *
     * @throws IOException if there is an error writing the generated file
     */
    private void generateRegistry() throws IOException {
        if (generatedMetadata.isEmpty()) {
            return;
        }
        ClassName projectionMetadata =
                ClassName.get(
                        "nl.datasteel.crudcraft.runtime.projection.metadata", "ProjectionMetadata");
        ClassName registryInterface =
                ClassName.get(
                        "nl.datasteel.crudcraft.runtime.projection.metadata",
                        "ProjectionMetadataRegistry");

        TypeName metadataWildcard =
                ParameterizedTypeName.get(
                        projectionMetadata, WildcardTypeName.subtypeOf(Object.class));
        TypeName classWildcard =
                ParameterizedTypeName.get(
                        ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class));
        TypeName mapType =
                ParameterizedTypeName.get(
                        ClassName.get(java.util.Map.class), classWildcard, metadataWildcard);

        FieldSpec mapField =
                FieldSpec.builder(mapType, "metadata", Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new $T<>()", ClassName.get(java.util.HashMap.class))
                        .build();

        MethodSpec.Builder ctor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        for (ClassName mc : generatedMetadata) {
            ctor.addStatement("register(new $T())", mc);
        }

        TypeVariableName dtype = TypeVariableName.get("D");
        TypeName metadataParam = ParameterizedTypeName.get(projectionMetadata, dtype);
        MethodSpec register =
                MethodSpec.methodBuilder("register")
                        .addModifiers(Modifier.PRIVATE)
                        .addTypeVariable(dtype)
                        .addParameter(metadataParam, "pm")
                        .addStatement("metadata.put(pm.dtoType(), pm)")
                        .build();

        MethodSpec get =
                MethodSpec.methodBuilder("getMetadata")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addTypeVariable(dtype)
                        .returns(ParameterizedTypeName.get(projectionMetadata, dtype))
                        .addParameter(
                                ParameterizedTypeName.get(ClassName.get(Class.class), dtype),
                                "dtoType")
                        .addStatement(
                                "return ($T) metadata.get(dtoType)",
                                ParameterizedTypeName.get(projectionMetadata, dtype))
                        .addAnnotation(
                                AnnotationSpec.builder(SuppressWarnings.class)
                                        .addMember("value", "$S", "unchecked")
                                        .build())
                        .build();

        TypeSpec registry =
                TypeSpec.classBuilder("GeneratedProjectionMetadataRegistry")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(ClassName.get("org.springframework.stereotype", "Component"))
                        .addSuperinterface(registryInterface)
                        .addField(mapField)
                        .addMethod(ctor.build())
                        .addMethod(register)
                        .addMethod(get)
                        .build();

        JavaPoetUtils.javaFile(registryPackage(), registry)
                .build()
                .writeTo(processingEnv.getFiler());
    }

    private String registryPackage() {
        String commonRoot = null;
        for (ClassName metadata : generatedMetadata) {
            String root = applicationRootPackage(metadata.packageName());
            if (commonRoot == null) {
                commonRoot = root;
            } else {
                commonRoot = commonPackage(commonRoot, root);
            }
        }
        if (commonRoot == null || commonRoot.isBlank()) {
            return "nl.datasteel.crudcraft.projection";
        }
        return commonRoot + ".projection";
    }

    private String applicationRootPackage(String metadataPackage) {
        int dtoIndex = metadataPackage.indexOf(".dto.");
        if (dtoIndex >= 0) {
            return metadataPackage.substring(0, dtoIndex);
        }
        if (metadataPackage.endsWith(".dto")) {
            return metadataPackage.substring(0, metadataPackage.length() - ".dto".length());
        }
        return metadataPackage;
    }

    private String commonPackage(String first, String second) {
        String[] firstParts = first.split("\\.");
        String[] secondParts = second.split("\\.");
        int length = Math.min(firstParts.length, secondParts.length);
        int index = 0;
        while (index < length && firstParts[index].equals(secondParts[index])) {
            index++;
        }
        return String.join(".", java.util.Arrays.copyOf(firstParts, index));
    }
}

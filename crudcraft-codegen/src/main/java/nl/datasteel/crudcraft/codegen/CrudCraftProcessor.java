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

package nl.datasteel.crudcraft.codegen;

import jakarta.persistence.Embeddable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.security.CrudSecurity;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.exception.CodegenValidationException;
import nl.datasteel.crudcraft.codegen.reader.AnnotationModelReader;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import nl.datasteel.crudcraft.codegen.writer.WriterRegistry;


/**
 * Annotation processor for CrudCraft. This processor scans for classes annotated with CrudCrafted
 * and Embeddable, and generates the necessary code based on the annotations present. It supports
 * various annotations related to CRUD operations, including Dto, Request, Response, Searchable, and
 * others.
 */
@SupportedAnnotationTypes({
    "nl.datasteel.crudcraft.annotations.classes.CrudCrafted",
    "nl.datasteel.crudcraft.annotations.fields.Dto",
    "nl.datasteel.crudcraft.annotations.fields.Request",
    "nl.datasteel.crudcraft.annotations.fields.Searchable",
    "nl.datasteel.crudcraft.annotations.fields.BatchFetched",
    "jakarta.persistence.Embeddable",
})
@SupportedOptions({
    "crudcraft.insomnia.outputDir",
    "crudcraft.dto.generateWithers",
    "crudcraft.embeddable.maxDepth"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class CrudCraftProcessor extends AbstractProcessor {
    private static final String BATCH_FETCHED_ANNOTATION =
            "nl.datasteel.crudcraft.annotations.fields.BatchFetched";

    private boolean mapStructProcessorCheckReported;
    private boolean mapStructOrderingCheckReported;

    /** Creates the CrudCraft annotation processor. */
    public CrudCraftProcessor() {
        // Constructor without any parameters stays empty
    }

    /**
     * Processes the annotations found in the current round. It collects all model descriptors from
     * classes annotated with CrudCrafted and Embeddable, and writes the generated code using the
     * WriterRegistry.
     *
     * @param annotations The set of annotations to process.
     * @param roundEnv The current round environment.
     * @return true if the annotations were processed successfully, false otherwise.
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ProcessingEnvironment env = processingEnv;
        WriteContext ctx = new WriteContext(env);

        for (Element element : getProcessableElements(roundEnv)) {
            warnWhenMapStructProcessorIsDetected(element);
            warnWhenMapStructProcessorIsMissing(element);
            try {
                ModelDescriptor modelDescriptor = AnnotationModelReader.parse(element, env);
                validateAnnotationCohesion(element, modelDescriptor);
                validateEmbeddableDepth(element);
                validateEndpointConflicts(element, modelDescriptor);
                WriterRegistry.writeAll(modelDescriptor, ctx);
            } catch (Exception e) {
                reportGenerationFailure(element, e);
            }
        }
        return true;
    }

    private void reportGenerationFailure(Element element, Exception exception) {
        String rootCause = findRootCauseMessage(exception);
        String commonCauses = commonCausesHint();
        processingEnv
                .getMessager()
                .printMessage(
                        Diagnostic.Kind.ERROR,
                        "CrudCraftProcessor failed for "
                                + ((TypeElement) element).getQualifiedName()
                                + ": "
                                + rootCause
                                + "\nCommon causes: "
                                + commonCauses,
                        element);
    }

    private String commonCausesHint() {
        return "annotation-processing classpath drift, MapStruct processor ordering/configuration,"
                + " stale generated sources after dependency upgrades, or invalid endpoint"
                + " include/omit combinations";
    }

    private void warnWhenMapStructProcessorIsMissing(Element element) {
        if (mapStructProcessorCheckReported || !isClassPresent("org.mapstruct.Mapper")) {
            return;
        }
        mapStructProcessorCheckReported = true;
        if (isClassPresent("org.mapstruct.ap.MappingProcessor")) {
            return;
        }
        note(
                element,
                "MapStruct API is present but org.mapstruct.ap.MappingProcessor is missing from"
                        + " annotation processing. Generated mapper interfaces compile, but"
                        + " mapper implementations will not be generated.");
    }

    private void warnWhenMapStructProcessorIsDetected(Element element) {
        if (mapStructOrderingCheckReported
                || !isClassPresent("org.mapstruct.ap.MappingProcessor")) {
            return;
        }
        mapStructOrderingCheckReported = true;
        note(
                element,
                "CrudCraft and MapStruct processors are both active. Keep crudcraft-codegen before"
                        + " mapstruct-processor on the annotation-processor path so generated DTO"
                        + " shapes are visible to MapStruct in the same compile.");
    }

    private boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private String findRootCauseMessage(Throwable exception) {
        Throwable current = exception;
        String fallback = exception.getClass().getSimpleName();
        while (current != null) {
            String message = current.getMessage();
            if (message != null && !message.isBlank()) {
                fallback = current.getClass().getSimpleName() + ": " + message;
            }
            current = current.getCause();
        }
        return fallback;
    }

    private List<Element> getProcessableElements(RoundEnvironment roundEnv) {
        Set<Element> elements = new LinkedHashSet<>();

        // Read CrudCrafted classes
        for (Element e : roundEnv.getElementsAnnotatedWith(CrudCrafted.class)) {
            addProcessableElement(elements, e);
        }

        // Read Embeddable classes
        for (Element e : roundEnv.getElementsAnnotatedWith(Embeddable.class)) {
            addProcessableElement(elements, e);
        }

        return List.copyOf(elements);
    }

    private void addProcessableElement(Set<Element> elements, Element element) {
        if (element.getKind() == ElementKind.CLASS) {
            elements.add(element);
        }
    }

    private void validateEndpointConflicts(Element element, ModelDescriptor modelDescriptor) {
        Set<CrudEndpoint> omitted =
                new LinkedHashSet<>(Arrays.asList(modelDescriptor.getOmitEndpoints()));
        Set<CrudEndpoint> included =
                new LinkedHashSet<>(Arrays.asList(modelDescriptor.getIncludeEndpoints()));
        for (CrudEndpoint endpoint : modelDescriptor.getIncludeEndpoints()) {
            if (omitted.contains(endpoint)) {
                throw new CodegenValidationException(
                        modelDescriptor.getName(),
                        "Endpoint "
                                + endpoint
                                + " cannot appear in both omitEndpoints and includeEndpoints");
            }
        }
        validateEndpointOverlayPair(modelDescriptor, omitted, included, CrudEndpoint.POST,
                CrudEndpoint.BULK_CREATE);
        validateEndpointOverlayPair(modelDescriptor, omitted, included, CrudEndpoint.PUT,
                CrudEndpoint.BULK_UPDATE);
        validateEndpointOverlayPair(modelDescriptor, omitted, included, CrudEndpoint.PATCH,
                CrudEndpoint.BULK_PATCH);
        validateEndpointOverlayPair(modelDescriptor, omitted, included, CrudEndpoint.DELETE,
                CrudEndpoint.BULK_DELETE);

        Set<CrudEndpoint> effective = effectiveEndpoints(modelDescriptor);
        warnIfMissingDependency(element, effective, CrudEndpoint.PATCH, CrudEndpoint.GET_ONE);
        warnIfMissingDependency(element, effective, CrudEndpoint.PUT, CrudEndpoint.GET_ONE);
        warnIfMissingDependency(element, effective, CrudEndpoint.DELETE, CrudEndpoint.GET_ONE);
    }

    private void validateEndpointOverlayPair(
            ModelDescriptor modelDescriptor,
            Set<CrudEndpoint> omitted,
            Set<CrudEndpoint> included,
            CrudEndpoint single,
            CrudEndpoint bulk) {
        if (included.contains(bulk) && omitted.contains(single)) {
            throw new CodegenValidationException(
                    modelDescriptor.getName(),
                    "Endpoint "
                            + bulk
                            + " cannot be explicitly included while its single-item counterpart "
                            + single
                            + " is explicitly omitted");
        }
        if (included.contains(single) && omitted.contains(bulk)) {
            throw new CodegenValidationException(
                    modelDescriptor.getName(),
                    "Endpoint "
                            + single
                            + " cannot be explicitly included while its bulk counterpart "
                            + bulk
                            + " is explicitly omitted");
        }
    }

    private void warnIfMissingDependency(
            Element element,
            Set<CrudEndpoint> effective,
            CrudEndpoint endpoint,
            CrudEndpoint dependency) {
        if (effective.contains(endpoint) && !effective.contains(dependency)) {
            note(
                    element,
                    "Endpoint "
                            + endpoint
                            + " is enabled without "
                            + dependency
                            + "; generated service logic still validates identifiers internally,"
                            + " but clients cannot preflight through that endpoint.");
        }
    }

    private Set<CrudEndpoint> effectiveEndpoints(ModelDescriptor modelDescriptor) {
        Set<CrudEndpoint> effective = new LinkedHashSet<>();
        if (modelDescriptor.getEndpointPolicy() == CrudTemplate.class) {
            effective.addAll(modelDescriptor.getTemplate().resolveEndpoints());
        } else {
            effective.addAll(resolveCustomEndpointPolicy(modelDescriptor).resolveEndpoints());
        }
        effective.removeAll(Arrays.asList(modelDescriptor.getOmitEndpoints()));
        effective.addAll(Arrays.asList(modelDescriptor.getIncludeEndpoints()));
        return effective;
    }

    private CrudEndpointPolicy resolveCustomEndpointPolicy(ModelDescriptor modelDescriptor) {
        try {
            return modelDescriptor.getEndpointPolicy().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new CodegenValidationException(
                    modelDescriptor.getName(),
                    "Endpoint policy "
                            + modelDescriptor.getEndpointPolicy().getName()
                            + " must have an accessible no-argument constructor");
        }
    }

    private void validateAnnotationCohesion(Element element, ModelDescriptor modelDescriptor) {
        if (!modelDescriptor.isSecure() && element.getAnnotation(CrudSecurity.class) != null) {
            note(
                    element,
                    "@CrudSecurity on "
                            + modelDescriptor.getName()
                            + " has no effect because @CrudCrafted(secure=false). Add"
                            + " secure=true or remove @CrudSecurity.");
        }
        for (FieldDescriptor field : modelDescriptor.getFields()) {
            if (field.hasFieldSecurity() && !field.inDto()) {
                throw new CodegenValidationException(
                        modelDescriptor.getName(),
                        "@FieldSecurity on field '"
                                + field.getName()
                                + "' has no effect because the field is not included in a DTO.");
            }
            if (field.isSearchable() && field.getRelType() != RelationshipType.NONE) {
                note(
                        element,
                        "@Searchable on relationship field '"
                                + field.getName()
                                + "' can create expensive joins; set a small depth and index the"
                                + " searched columns.");
                if (!hasBatchFetchedAnnotation(element, field.getName())) {
                    note(
                            element,
                            "Relationship field '"
                                    + field.getName()
                                    + "' is searchable but not annotated with @BatchFetched."
                                    + " Add @BatchFetched or an explicit fetch strategy to avoid"
                                    + " N+1 query cliffs.");
                }
            }
            if (field.getResponseDtos().length > 1 && field.getRelType() != RelationshipType.NONE) {
                note(
                        element,
                        "Relationship field '"
                                + field.getName()
                                + "' participates in multiple DTO variants; verify mapper"
                                + " customization handles each variant intentionally.");
            }
        }
    }

    private void validateEmbeddableDepth(Element element) {
        if (element.getAnnotation(Embeddable.class) == null) {
            return;
        }
        int maxDepth = embeddableMaxDepth(element);
        validateEmbeddableDepth((TypeElement) element, 0, maxDepth, new LinkedHashSet<>());
    }

    private void validateEmbeddableDepth(
            TypeElement element, int depth, int maxDepth, Set<String> visited) {
        String name = element.getQualifiedName().toString();
        if (depth > maxDepth) {
            throw new CodegenValidationException(
                    element.getSimpleName().toString(),
                    "Embeddable nesting depth "
                            + depth
                            + " exceeds crudcraft.embeddable.maxDepth="
                            + maxDepth);
        }
        if (!visited.add(name)) {
            throw new CodegenValidationException(
                    element.getSimpleName().toString(),
                    "Cyclical embeddable nesting detected: " + visited);
        }
        for (Element enclosed : element.getEnclosedElements()) {
            if (!(enclosed instanceof VariableElement field)) {
                continue;
            }
            TypeMirror fieldType = field.asType();
            Element fieldElement = processingEnv.getTypeUtils().asElement(fieldType);
            if (fieldElement instanceof TypeElement nested
                    && nested.getAnnotation(Embeddable.class) != null) {
                validateEmbeddableDepth(nested, depth + 1, maxDepth, new LinkedHashSet<>(visited));
            }
        }
    }

    private int embeddableMaxDepth(Element element) {
        String raw = processingEnv.getOptions().getOrDefault("crudcraft.embeddable.maxDepth", "5");
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            throw new CodegenValidationException(
                    element.getSimpleName().toString(),
                    "crudcraft.embeddable.maxDepth must be an integer: " + raw);
        }
    }

    private void note(Element element, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message, element);
    }

    private boolean hasBatchFetchedAnnotation(Element modelElement, String fieldName) {
        if (!(modelElement instanceof TypeElement typeElement)) {
            return false;
        }
        Element matchingField =
                typeElement.getEnclosedElements().stream()
                        .filter(enclosed -> enclosed.getKind() == ElementKind.FIELD)
                        .filter(enclosed -> fieldName.contentEquals(enclosed.getSimpleName()))
                        .findFirst()
                        .orElse(null);
        if (matchingField == null) {
            return false;
        }
        return matchingField.getAnnotationMirrors().stream()
                .map(annotationMirror -> annotationMirror.getAnnotationType().asElement())
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .map(type -> type.getQualifiedName().toString())
                .anyMatch(BATCH_FETCHED_ANNOTATION::equals);
    }
}

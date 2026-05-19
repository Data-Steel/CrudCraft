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

package nl.datasteel.crudcraft.codegen.reader.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import jakarta.persistence.Embedded;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.EnumOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Relationship;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SearchOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Security;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Validation;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.reader.field.FieldPartExtractorRegistry;


/** Extracts {@link ModelIdentity} from a model class. */
@SuppressWarnings("java:S6548") // Singleton pattern suppression
public final class IdentityExtractor implements ModelPartExtractor<ModelIdentity> {

    /** Singleton instance of IdentityExtractor. */
    public static final IdentityExtractor INSTANCE = new IdentityExtractor();

    /** Creates the model identity extractor. */
    public IdentityExtractor() {}

    /**
     * Extracts the identity of the model class. This includes the model name, package name, fields,
     * and base package.
     *
     * @param cls the TypeElement representing the model class
     * @param env processing environment for annotation utilities
     * @return a ModelIdentity object containing the extracted identity information
     */
    @Override
    public @NonNull ModelIdentity extract(
            @NonNull TypeElement cls, @NonNull ProcessingEnvironment env) {
        String name = cls.getSimpleName().toString();
        String packageName =
                ((PackageElement) cls.getEnclosingElement()).getQualifiedName().toString();

        String basePackage = extractBasePackage(cls, packageName);

        List<FieldDescriptor> fields = extractFields(cls, env);

        return new ModelIdentity(name, packageName, List.copyOf(fields), basePackage);
    }

    private static @NonNull String extractBasePackage(
            @NonNull TypeElement cls, @NonNull String packageName) {
        CrudCrafted annotation = cls.getAnnotation(CrudCrafted.class);
        if (annotation != null && !annotation.basePackage().isBlank()) {
            return annotation.basePackage().trim();
        }

        // If the package name ends with ".model", remove it to get the default base package.
        return packageName.endsWith(".model")
                ? packageName.substring(0, packageName.length() - ".model".length())
                : packageName;
    }

    /**
     * Extracts fields from the given TypeElement.
     *
     * @param cls the TypeElement representing the model class
     * @param env processing environment for annotation utilities
     * @return a list of FieldDescriptor objects representing the fields of the model
     */
    private static @NonNull List<FieldDescriptor> extractFields(
            @NonNull TypeElement cls, @NonNull ProcessingEnvironment env) {
        List<FieldDescriptor> fields = new ArrayList<>();

        // First, collect fields from parent classes (including abstract parents)
        fields.addAll(extractInheritedFields(cls, env));

        // Then add fields from the current class
        for (var element : cls.getEnclosedElements()) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            VariableElement field = (VariableElement) element;

            if (field.getAnnotation(Embedded.class) != null) {
                fields.addAll(readEmbeddedFields(field, env));
            } else {
                fields.add(readField(field, env));
            }
        }
        return fields;
    }

    /**
     * Recursively extracts fields from parent classes.
     *
     * @param cls the TypeElement representing the model class
     * @param env processing environment for annotation utilities
     * @return a list of FieldDescriptor objects from parent classes
     */
    private static @NonNull List<FieldDescriptor> extractInheritedFields(
            @NonNull TypeElement cls, @NonNull ProcessingEnvironment env) {
        List<FieldDescriptor> inheritedFields = new ArrayList<>();

        // Get the superclass
        TypeMirror superclass = cls.getSuperclass();
        if (superclass instanceof DeclaredType dt) {
            var superElement = dt.asElement();
            if (superElement instanceof TypeElement superType) {
                // Only process if it's not Object
                String superName = superType.getQualifiedName().toString();
                if (!"java.lang.Object".equals(superName)) {
                    // Recursively get fields from the parent
                    inheritedFields.addAll(extractInheritedFields(superType, env));

                    // Add fields from this parent
                    for (var element : superType.getEnclosedElements()) {
                        if (element.getKind() != ElementKind.FIELD) {
                            continue;
                        }
                        VariableElement field = (VariableElement) element;

                        // Skip static and transient fields
                        if (field.getModifiers().contains(javax.lang.model.element.Modifier.STATIC)
                                || field.getModifiers()
                                        .contains(javax.lang.model.element.Modifier.TRANSIENT)) {
                            continue;
                        }

                        if (field.getAnnotation(Embedded.class) != null) {
                            inheritedFields.addAll(readEmbeddedFields(field, env));
                        } else {
                            inheritedFields.add(readField(field, env));
                        }
                    }
                }
            }
        }

        return inheritedFields;
    }

    /**
     * Reads embedded fields from the given VariableElement.
     *
     * @param embeddedField the VariableElement representing the embedded field
     * @param env the ProcessingEnvironment for accessing annotations and other processing features
     * @return a list of FieldDescriptor objects representing the embedded fields
     */
    private static @NonNull List<FieldDescriptor> readEmbeddedFields(
            @NonNull VariableElement embeddedField, @NonNull ProcessingEnvironment env) {
        List<FieldDescriptor> embeddedFields = new ArrayList<>();
        TypeMirror tm = embeddedField.asType();

        if (tm instanceof DeclaredType dt) {
            TypeElement embeddedType = (TypeElement) dt.asElement();

            embeddedType.getEnclosedElements().stream()
                    .filter(f -> f.getKind() == ElementKind.FIELD)
                    .map(f -> readField((VariableElement) f, env))
                    .forEach(embeddedFields::add);
        }

        return embeddedFields;
    }

    /**
     * Reads a single field and extracts its parts.
     *
     * @param field the VariableElement representing the field
     * @param env the ProcessingEnvironment for accessing annotations and other processing features
     * @return a FieldDescriptor object representing the field
     */
    private static @NonNull FieldDescriptor readField(
            @NonNull VariableElement field, @NonNull ProcessingEnvironment env) {
        return new FieldDescriptor(
                FieldPartExtractorRegistry.get(Identity.class).extract(field, env),
                FieldPartExtractorRegistry.get(DtoOptions.class).extract(field, env),
                FieldPartExtractorRegistry.get(EnumOptions.class).extract(field, env),
                FieldPartExtractorRegistry.get(Relationship.class).extract(field, env),
                FieldPartExtractorRegistry.get(Validation.class).extract(field, env),
                FieldPartExtractorRegistry.get(SearchOptions.class).extract(field, env),
                FieldPartExtractorRegistry.get(Security.class).extract(field, env));
    }
}

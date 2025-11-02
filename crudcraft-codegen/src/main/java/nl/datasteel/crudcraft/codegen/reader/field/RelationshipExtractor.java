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
package nl.datasteel.crudcraft.codegen.reader.field;

import jakarta.persistence.Embedded;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Relationship;
import nl.datasteel.crudcraft.codegen.util.TypeUtils;

/**
 * Singleton extractor for extracting relationships from fields annotated with JPA annotations.
 * This class implements the FieldPartExtractor interface for Relationship type.
 */
@SuppressWarnings("java:S6548") // Suppress warning for singleton pattern usage
public class RelationshipExtractor implements FieldPartExtractor<Relationship> {

    /**
     * Singleton instance of RelationshipExtractor.
     */
    public static final RelationshipExtractor INSTANCE = new RelationshipExtractor();

    /**
     * Extracts a Relationship from a VariableElement representing a field.
     *
     * @param field the VariableElement to extract from
     * @param env the ProcessingEnvironment for accessing annotations
     * @return a Relationship instance representing the field's relationship
     */
    @Override
    public Relationship extract(VariableElement field, ProcessingEnvironment env) {
        TypeMirror fieldType = field.asType();
        Messager messager = env.getMessager();

        Embedded embeddedAnnotation = field.getAnnotation(Embedded.class);
        boolean isEmbedded = embeddedAnnotation != null;

        if (fieldType == null) {
            FieldPartExtractor.log(messager, Kind.ERROR, field, "Has null type.");
            return new Relationship(RelationshipType.NONE, "",
                    "java.lang.Object", false, isEmbedded);
        }

        try {
            // OneToMany
            OneToMany otm = field.getAnnotation(OneToMany.class);
            if (otm != null && !otm.mappedBy().isBlank()) {
                FieldPartExtractor.log(messager, Kind.NOTE, field,
                        "Detected @OneToMany with mappedBy = " + otm.mappedBy());
                return build(RelationshipType.ONE_TO_MANY, otm.mappedBy(),
                        TypeUtils.unwrapGeneric(fieldType, messager), env, isEmbedded);
            }

            // ManyToMany
            ManyToMany mtm = field.getAnnotation(ManyToMany.class);
            if (mtm != null) {
                FieldPartExtractor.log(messager, Kind.NOTE, field,
                        "Detected @ManyToMany with mappedBy = " + mtm.mappedBy());
                return build(RelationshipType.MANY_TO_MANY, mtm.mappedBy(),
                        extractTarget(field, fieldType, messager), env, isEmbedded);
            }

            // OneToOne
            OneToOne oto = field.getAnnotation(OneToOne.class);
            if (oto != null && !oto.mappedBy().isBlank()) {
                FieldPartExtractor.log(messager, Kind.NOTE, field,
                        "Detected @OneToOne with mappedBy = " + oto.mappedBy());
                return build(RelationshipType.ONE_TO_ONE, oto.mappedBy(),
                        TypeUtils.unwrapGeneric(fieldType, messager), env, isEmbedded);
            }

            // ManyToOne
            ManyToOne mto = field.getAnnotation(ManyToOne.class);
            if (mto != null) {
                FieldPartExtractor.log(messager, Kind.NOTE, field,
                        "Detected @ManyToOne");
                return build(RelationshipType.MANY_TO_ONE, "", fieldType.toString(),
                        env, isEmbedded);
            }

            // Default: no relation
            FieldPartExtractor.log(messager, Kind.NOTE, field,
                    "No relationship annotations found. Defaulting to NONE.");
            return new Relationship(RelationshipType.NONE, "",
                    fieldType.toString(), false, isEmbedded);
        } catch (Exception e) {
            messager.printMessage(Kind.ERROR, "Error extracting relationship from field: "
                    + e.getMessage());
            return new Relationship(RelationshipType.NONE, "",
                    "java.lang.Object", false, isEmbedded);
        }
    }

    /**
     * Builds a Relationship instance based on the provided parameters.
     *
     * @param relType the type of the relationship
     * @param mappedBy the mappedBy field, if applicable
     * @param targetType the target type of the relationship
     * @param env the ProcessingEnvironment for accessing annotations
     * @return a new Relationship instance
     */
    private static Relationship build(RelationshipType relType, String mappedBy,
                                      String targetType, ProcessingEnvironment env,
                                      boolean isEmbedded) {
        // Check if the target type is a managed CRUD entity
        TypeElement te = env.getElementUtils().getTypeElement(targetType);
        boolean isCrud = te != null && te.getAnnotation(CrudCrafted.class) != null;
        return new Relationship(relType, mappedBy, targetType, isCrud, isEmbedded);
    }

    /**
     * Extracts the target type from a ManyToMany annotation or unwraps the generic type.
     *
     * @param field the VariableElement to extract from
     * @param fieldType the TypeMirror of the field
     * @return the target type as a String
     */
    private static String extractTarget(VariableElement field, TypeMirror fieldType,
                                        Messager messager) {
        try {
            // Check for ManyToMany annotation
            for (var mirror : field.getAnnotationMirrors()) {
                if (isManyToManyAnnotation(mirror)) {
                    String extracted = TypeUtils.extractTargetEntityValue(mirror);
                    if (extracted != null) {
                        return extracted;
                    }
                }
            }
        } catch (Exception e) {
            messager.printMessage(Kind.WARNING,
                    "Failed to extract targetEntity from ManyToMany: " + e.getMessage());
        }
        return TypeUtils.unwrapGeneric(fieldType, messager);
    }

    /**
     * Checks if the given annotation mirror is a ManyToMany annotation.
     *
     * @param mirror the AnnotationMirror to check
     * @return true if it is a ManyToMany annotation, false otherwise
     */
    private static boolean isManyToManyAnnotation(AnnotationMirror mirror) {
        return ManyToMany.class.getCanonicalName().equals(mirror.getAnnotationType().toString());
    }
}

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

package nl.datasteel.crudcraft.codegen.writer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.fileheader.ModelStrictHeader;
import nl.datasteel.crudcraft.codegen.writer.relationship.OneToManyHandler;
import nl.datasteel.crudcraft.codegen.writer.relationship.OneToOneHandler;
import nl.datasteel.crudcraft.codegen.writer.relationship.RelationshipHandler;
import nl.datasteel.crudcraft.codegen.util.StubGeneratorUtil;

/**
 * Generates a compile-time “RelationshipMeta” class with
 * static fix(entity) and clear(entity) methods that
 * re-establish or clear bidirectional links in memory
 * via cached java.lang.reflect.Field references.
 */
public class RelationshipMetaGenerator implements Generator {

    /**
     * A map of relationship types to their corresponding handlers.
     * This allows for easy retrieval of the appropriate handler
     * based on the relationship type defined in the model descriptor.
     */
    private final Map<RelationshipType, RelationshipHandler> handlers = Map.of(
            RelationshipType.ONE_TO_MANY, new OneToManyHandler(),
            RelationshipType.MANY_TO_MANY, new OneToManyHandler(),
            RelationshipType.ONE_TO_ONE, new OneToOneHandler()
    );

    /**
     * Generates the RelationshipMeta class for the given model descriptor.
     * This class contains static methods to fix and clear bidirectional relationships.
     *
     * @param md The ModelDescriptor containing metadata about the model.
     * @param ctx The WriteContext providing access to the environment and utilities.
     * @return A list containing the generated JavaFile for the RelationshipMeta class.
     */
    @Override
    public List<JavaFile> generate(ModelDescriptor md, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(md, ctx)) {
            return List.of();
        }

        ctx.env().getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Generating RelationshipMeta for " + md.getName()
                        + " in package " + md.getPackageName() + ".meta"
        );

        String metaPkg = md.getPackageName() + ".meta";
        ClassName entityType = ClassName.get(md.getPackageName(), md.getName());
        List<FieldDescriptor> relFields = relationshipFields(md);

        TypeSpec.Builder cls = classSkeleton(md, metaPkg);
        addFieldCaches(cls, md, entityType, relFields);
        addFixMethod(cls, md, entityType, relFields);
        addClearMethod(cls, md, entityType, relFields);

        JavaFile javaFile = JavaFile.builder(metaPkg, cls.build())
                .addFileComment(StubGeneratorUtil.licenseHeader())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
        return List.of(javaFile);
    }

    @Override
    public boolean requiresCrudEntity() {
        return true;
    }

    @Override
    public int order() {
        return 0;
    }

    /**
     * Filters the fields of the given model descriptor to include only those
     * relationships for which a {@link RelationshipHandler} is available.
     * <p> Unidirectional relations such as {@code MANY_TO_ONE} do not require any
     * bidirectional fix/clear logic, so they are excluded to prevent generation
     * of empty try/catch blocks and unused reflection caches.</p>
     *
     * @param md The ModelDescriptor containing metadata about the model.
     * @return A list of FieldDescriptors that are supported relationship fields.
     */
    private List<FieldDescriptor> relationshipFields(ModelDescriptor md) {
        return md.getFields().stream()
                .filter(fd -> handlers.containsKey(fd.getRelType()))
                .toList();
    }

    /**
     * Creates the skeleton of the RelationshipMeta class.
     * This includes the class name, package, and a private constructor.
     *
     * @param md The ModelDescriptor containing metadata about the model.
     * @param metaPkg The package where the meta class will be generated.
     * @return A TypeSpec.Builder for the RelationshipMeta class.
     */
    private TypeSpec.Builder classSkeleton(ModelDescriptor md, String metaPkg) {
        String metaName = md.getName() + "RelationshipMeta";
        return TypeSpec.classBuilder(metaName)
                .addJavadoc(ModelStrictHeader.header(
                        md.getName(),
                        metaPkg,
                        this.getClass().getSimpleName()
                ))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .build());
    }

    /**
     * Adds static field caches for each relationship field in the given model descriptor.
     * Each field is stored as a static final java.lang.reflect.Field reference.
     * If the relationship is bidirectional, an additional field for the inverse relationship
     * is also created.
     *
     * @param cls The TypeSpec.Builder for the class being generated.
     * @param md The ModelDescriptor containing metadata about the model.
     * @param entityType The ClassName of the entity type for which fields are being cached.
     * @param relFields The list of relationship fields to be cached.
     */
    private void addFieldCaches(TypeSpec.Builder cls,
                                ModelDescriptor md,
                                ClassName entityType,
                                List<FieldDescriptor> relFields) {
        if (relFields.isEmpty()) {
            return;
        }
        for (FieldDescriptor fd : relFields) {
            String fieldName = fd.getName() + "Field";
            cls.addField(FieldSpec.builder(
                    ClassName.get("java.lang.reflect", "Field"),
                    fieldName,
                    Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL
            ).build());

            if (fd.getRelType().isBidirectional()) {
                String invProp = fd.getMappedBy() != null && !fd.getMappedBy().isBlank()
                        ? fd.getMappedBy()
                        : Character.toLowerCase(md.getName().charAt(0))
                                + md.getName().substring(1) + "s";
                String invFieldName = fd.getName() + "_" + invProp + "Field";

                cls.addField(FieldSpec.builder(
                        ClassName.get("java.lang.reflect", "Field"),
                        invFieldName,
                        Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL
                ).build());
            }
        }

        CodeBlock.Builder init = CodeBlock.builder().beginControlFlow("try");
        for (FieldDescriptor fd : relFields) {
            String fn = fd.getName() + "Field";
            init.addStatement(
                    "$L = $T.class.getDeclaredField($S)",
                    fn, entityType, fd.getName()
            );
            init.addStatement("$L.setAccessible(true)", fn);

            if (fd.getRelType().isBidirectional()) {
                String invProp = fd.getMappedBy() != null && !fd.getMappedBy().isBlank()
                        ? fd.getMappedBy()
                        : Character.toLowerCase(md.getName().charAt(0))
                                + md.getName().substring(1) + "s";
                String invFieldName = fd.getName() + "_" + invProp + "Field";
                ClassName targetType = ClassName.bestGuess(fd.getTargetType());

                init.addStatement(
                        "$L = $T.class.getDeclaredField($S)",
                        invFieldName, targetType, invProp
                );
                init.addStatement("$L.setAccessible(true)", invFieldName);
            }
        }
        init.nextControlFlow("catch (NoSuchFieldException e)")
                .addStatement("throw new $T($S, e)",
                        ClassName.get("nl.datasteel.crudcraft.runtime.exception",
                                "RelationshipException"),
                        "Failed to initialize RelationshipMeta for " + md.getName())
                .endControlFlow();
        cls.addStaticBlock(init.build());
    }

    /**
     * Adds a static method to fix bidirectional relationships for the given entity type.
     * This method iterates over all relationship fields and fixes them using the
     * appropriate handler.
     *
     * @param cls The TypeSpec.Builder for the class being generated.
     * @param md The ModelDescriptor containing metadata about the model.
     * @param entityType The ClassName of the entity type for which relationships are being fixed.
     * @param relFields The list of relationship fields to be fixed.
     */
    private void addFixMethod(TypeSpec.Builder cls,
                              ModelDescriptor md,
                              ClassName entityType,
                              List<FieldDescriptor> relFields) {
        MethodSpec.Builder fix = MethodSpec.methodBuilder("fix")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(entityType, "entity");

        if (relFields.isEmpty()) {
            fix.addStatement("// no bidirectional relationships to fix");
        } else {
            fix.beginControlFlow("try");
            for (FieldDescriptor fd : relFields) {
                RelationshipHandler handler = handlers.get(fd.getRelType());
                if (handler != null) {
                    handler.addFix(md, fd, fix, entityType);
                }
            }
            fix.nextControlFlow("catch ($T e)", IllegalAccessException.class)
                    .addStatement("throw new $T($S, e)",
                            ClassName.get("nl.datasteel.crudcraft.runtime.exception",
                                    "RelationshipException"),
                            "Failed to fix relationships for " + md.getName())
                    .endControlFlow();
        }
        cls.addMethod(fix.build());
    }

    /**
     * Adds a static method to clear bidirectional relationships for the given entity type.
     * This method iterates over all relationship fields and clears them using the
     * appropriate handler.
     *
     * @param cls The TypeSpec.Builder for the class being generated.
     * @param md The ModelDescriptor containing metadata about the model.
     * @param entityType The ClassName of the entity type for which relationships are being cleared.
     * @param relFields The list of relationship fields to be cleared.
     */
    private void addClearMethod(TypeSpec.Builder cls,
                                ModelDescriptor md,
                                ClassName entityType,
                                List<FieldDescriptor> relFields) {
        MethodSpec.Builder clear = MethodSpec.methodBuilder("clear")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(entityType, "entity");

        if (relFields.isEmpty()) {
            clear.addStatement("// no bidirectional relationships to clear");
        } else {
            clear.beginControlFlow("try");
            for (FieldDescriptor fd : relFields) {
                RelationshipHandler handler = handlers.get(fd.getRelType());
                if (handler != null) {
                    handler.addClear(md, fd, clear, entityType);
                }
            }
            clear.nextControlFlow("catch ($T e)", IllegalAccessException.class)
                    .addStatement("throw new $T($S, e)",
                            ClassName.get("nl.datasteel.crudcraft.runtime.exception",
                                    "RelationshipException"),
                            "Failed to clear relationships for " + md.getName())
                    .endControlFlow();
        }
        cls.addMethod(clear.build());
    }
}

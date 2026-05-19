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

import com.google.testing.compile.JavaFileObjects;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.DtoOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.EnumOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Identity;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Relationship;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SchemaMetadata;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SearchOptions;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Security;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.Validation;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class MapperGeneratorTest {

    private FieldDescriptor parentField(TypeMirror type) {
        return new FieldDescriptor(
                new Identity("parent", type, null, SchemaMetadata.empty()),
                new DtoOptions(true, true, true, new String[0], false),
                new EnumOptions(false, List.of()),
                new Relationship(
                        RelationshipType.MANY_TO_ONE, "", "nl.other.Parent", true, false, false),
                new Validation(List.of()),
                new SearchOptions(false, List.of(), 0),
                new Security(false, null, null));
    }

    private FieldDescriptor childrenField(TypeMirror type) {
        return new FieldDescriptor(
                new Identity("children", type, null, SchemaMetadata.empty()),
                new DtoOptions(true, true, true, new String[0], false),
                new EnumOptions(false, List.of()),
                new Relationship(
                        RelationshipType.ONE_TO_MANY, "", "nl.other.Child", true, false, false),
                new Validation(List.of()),
                new SearchOptions(false, List.of(), 0),
                new Security(false, null, null));
    }

    private FieldDescriptor simpleField(TypeMirror type) {
        return new FieldDescriptor(
                new Identity("name", type, null, SchemaMetadata.empty()),
                new DtoOptions(true, true, true, new String[0], false),
                new EnumOptions(false, List.of()),
                new Relationship(RelationshipType.NONE, "", null, false, false, false),
                new Validation(List.of()),
                new SearchOptions(false, List.of(), 0),
                new Security(false, null, null));
    }

    private ModelDescriptor descriptor(List<FieldDescriptor> fields) {
        return descriptor(fields, false);
    }

    private ModelDescriptor descriptor(List<FieldDescriptor> fields, boolean isAbstract) {
        ModelIdentity id = new ModelIdentity("Sample", "com.example", fields, "com.example");
        ModelFlags flags = new ModelFlags(true, true, false, isAbstract);
        EndpointOptions ep =
                new EndpointOptions(
                        nl.datasteel.crudcraft.annotations.CrudTemplate.FULL,
                        new nl.datasteel.crudcraft.annotations.CrudEndpoint[0],
                        new nl.datasteel.crudcraft.annotations.CrudEndpoint[0],
                        nl.datasteel.crudcraft.annotations.CrudTemplate.class);
        ModelSecurity sec = new ModelSecurity(false, null, List.of());
        return new ModelDescriptor(id, flags, ep, sec);
    }

    @Test
    void buildGeneratesMappingsForRelations() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();
        FieldDescriptor parent = parentField(tf.type(String.class));
        FieldDescriptor children = childrenField(tf.setOf(String.class));
        ModelDescriptor md = descriptor(List.of(parent, children));
        JavaFile jf = gen.build(md, ctx);
        String code = jf.toString();
        assertTrue(code.contains("interface SampleMapper"));
        assertTrue(code.contains("@Mapper"));
        assertTrue(code.contains("qualifiedByName = \"SampleMapParent\""));
        assertTrue(code.contains("qualifiedByName = \"SampleMapChildSet\""));
        assertTrue(code.contains("mapParent"));
        assertTrue(code.contains("mapChildSet"));
        assertTrue(code.contains("Set<Child> mapChildSet(Set<UUID> ids)"));
        assertTrue(code.contains("Collectors.toSet()"));
        assertTrue(code.contains("ParentRef toParentRef(Parent parent)"));
        assertTrue(code.contains("uses = {ChildMapper.class}"));
        assertFalse(code.contains("ParentMapper.class"));
        assertEquals(4, occurrences(code, "SampleMapChildSet"));
        assertEquals(2, occurrences(code, "SampleToParentRef"));
        assertTrue(
                env.messager.notes.stream()
                        .anyMatch(note -> note.contains("Generating mapper for Sample")));
    }

    @Test
    void buildCoversListAndAbstractRelationBranches() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();

        FieldDescriptor listRelation =
                new FieldDescriptor(
                        new Identity(
                                "authors", tf.listOf(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.ONE_TO_MANY,
                                "",
                                "nl.other.Author",
                                true,
                                false,
                                false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        FieldDescriptor abstractRelation =
                new FieldDescriptor(
                        new Identity("owner", tf.type(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.MANY_TO_ONE,
                                "",
                                "nl.other.Owner",
                                true,
                                false,
                                true),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));

        JavaFile jf = gen.build(descriptor(List.of(listRelation, abstractRelation)), ctx);
        String code = jf.toString();

        assertTrue(code.contains("qualifiedByName = \"SampleMapAuthorList\""));
        assertTrue(code.contains("mapAuthorList"));
        assertTrue(code.contains("List<Author> mapAuthorList(List<UUID> ids)"));
        assertTrue(code.contains("Collectors.toList()"));
        assertTrue(code.contains("source = \"authorIds\""));
        assertTrue(code.contains("target = \"owner\""));
        assertTrue(code.contains("ignore = true"));
        assertEquals(4, occurrences(code, "SampleMapAuthorList"));
        assertFalse(code.contains("SampleMapAuthorSet"));
        assertFalse(code.contains("SampleToOwnerRef"));
        assertFalse(code.contains("toOwnerRef"));
    }

    @Test
    void buildWithoutRelationsHasNoUses() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();
        FieldDescriptor simple = simpleField(tf.type(String.class));
        ModelDescriptor md = descriptor(List.of(simple));
        JavaFile jf = gen.build(md, ctx);
        String code = jf.toString();
        assertFalse(code.contains("uses ="));
        assertFalse(code.contains("qualifiedByName"));
    }

    @Test
    void buildMapperAnnotationWithMultipleUses() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();
        FieldDescriptor children = childrenField(tf.setOf(String.class));
        FieldDescriptor tags =
                new FieldDescriptor(
                        new Identity("tags", tf.setOf(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.ONE_TO_MANY,
                                "",
                                "nl.other.Tag",
                                true,
                                false,
                                false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));

        String code = gen.build(descriptor(List.of(children, tags)), ctx).toString();

        assertTrue(code.contains("uses = {ChildMapper.class, TagMapper.class}"));
    }

    @Test
    void requiresCrudEntityAndOrder() {
        MapperGenerator gen = new MapperGenerator();
        assertTrue(gen.requiresCrudEntity());
        assertEquals(2, gen.order());
    }

    @Test
    void generateSkipsAbstractModels() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();
        FieldDescriptor parent = parentField(tf.type(String.class));
        assertTrue(gen.generate(null, ctx).isEmpty());
        assertTrue(gen.generate(descriptor(List.of(parent), true), ctx).isEmpty());
        assertTrue(
                env.messager.notes.stream()
                        .anyMatch(
                                note ->
                                        note.contains(
                                                "Skipping mapper generation for abstract entity:"
                                                        + " Sample")));
    }

    @Test
    void writeHandlesFilerException() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(true, false));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();
        FieldDescriptor parent = parentField(tf.type(String.class));
        gen.write(descriptor(List.of(parent)), ctx);
        assertFalse(env.messager.notes.isEmpty());
    }

    @Test
    void writeHandlesIOException() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, true));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();
        FieldDescriptor parent = parentField(tf.type(String.class));
        gen.write(descriptor(List.of(parent)), ctx);
        assertFalse(env.messager.errors.isEmpty());
    }

    @Test
    void buildGeneratesSpecializedDtoMappers() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();

        // Create a field with specialized DTO annotations
        FieldDescriptor fieldWithSpecializedDto =
                new FieldDescriptor(
                        new Identity("name", tf.type(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[] {"List", "Map"}, false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));

        ModelDescriptor md = descriptor(List.of(fieldWithSpecializedDto));
        JavaFile jf = gen.build(md, ctx);
        String code = jf.toString();

        // Verify specialized mapper methods are generated
        assertTrue(
                code.contains("SampleListResponseDto toListResponse(Sample entity)"),
                "Should generate toListResponse method");
        assertTrue(
                code.contains("SampleMapResponseDto toMapResponse(Sample entity)"),
                "Should generate toMapResponse method");
    }

    @Test
    void buildAddsForceLoadMappingForLobFields() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();

        FieldDescriptor lobField =
                new FieldDescriptor(
                        new Identity(
                                "attachment", tf.type(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, false, new String[0], true),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));

        FieldDescriptor simpleF = simpleField(tf.type(String.class));
        ModelDescriptor md = descriptor(List.of(simpleF, lobField));
        JavaFile jf = gen.build(md, ctx);
        String code = jf.toString();

        assertTrue(
                code.contains("expression = \"java(entity.getAttachment())\""),
                "Should force-load LOB field via explicit expression mapping");
        assertTrue(code.contains("target = \"attachment\""), "Should target the attachment field");
    }

    @Test
    void buildSkipsForceLoadMappingForLobFieldNotInDto() {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();

        // LOB field NOT in response DTO (inDto=false)
        FieldDescriptor lobFieldNotInDto =
                new FieldDescriptor(
                        new Identity(
                                "rawData", tf.type(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(false, true, false, new String[0], true),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));

        FieldDescriptor simpleF = simpleField(tf.type(String.class));
        ModelDescriptor md = descriptor(List.of(simpleF, lobFieldNotInDto));
        JavaFile jf = gen.build(md, ctx);
        String code = jf.toString();

        assertFalse(
                code.contains("expression = \"java(entity.getRawData())\""),
                "Should NOT force-load LOB field not in response DTO");
    }

    @Test
    void hasSingleIdHandlesSingleMultiAndEmbeddedIdentifiers() throws Exception {
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString(
                                "t.EmbeddedKey", "package t; public class EmbeddedKey {}"),
                        JavaFileObjects.forSourceString(
                                "t.OneId",
                                "package t; import jakarta.persistence.Id; public class OneId { @Id"
                                        + " public java.util.UUID id; }"),
                        JavaFileObjects.forSourceString(
                                "t.TwoIds",
                                "package t; import jakarta.persistence.Id; public class TwoIds {"
                                        + " @Id public java.util.UUID id; @Id public java.util.UUID"
                                        + " altId; }"),
                        JavaFileObjects.forSourceString(
                                "t.WithEmbedded",
                                "package t; import jakarta.persistence.EmbeddedId; public class"
                                        + " WithEmbedded { @EmbeddedId public EmbeddedKey id; }"),
                        JavaFileObjects.forSourceString(
                                "t.MethodId",
                                "package t; import jakarta.persistence.Id; public class MethodId"
                                        + " { @Id public java.util.UUID getId() { return null; }"
                                        + " }"),
                        JavaFileObjects.forSourceString(
                                "t.MethodEmbedded",
                                "package t; import jakarta.persistence.EmbeddedId; public class"
                                        + " MethodEmbedded { @EmbeddedId public EmbeddedKey getId()"
                                        + " { return null; } }"));
        WriteContext ctx = new WriteContext(new ElementsOnlyEnv(elements));
        MapperGenerator gen = new MapperGenerator();

        assertTrue(invokeHasSingleId(gen, "t.OneId", ctx));
        assertFalse(invokeHasSingleId(gen, "t.TwoIds", ctx));
        assertFalse(invokeHasSingleId(gen, "t.WithEmbedded", ctx));
        assertTrue(invokeHasSingleId(gen, "t.MethodId", ctx));
        assertFalse(invokeHasSingleId(gen, "t.MethodEmbedded", ctx));
        assertTrue(invokeHasSingleId(gen, "t.DoesNotExist", ctx));
    }

    @Test
    void isInRequestDtoCoversAllCombinations() throws Exception {
        MapperGenerator gen = new MapperGenerator();

        FieldDescriptor inRequest = mock(FieldDescriptor.class);
        when(inRequest.inRequest()).thenReturn(true);
        assertTrue(invokeIsInRequestDto(gen, inRequest));

        FieldDescriptor relationInDto = mock(FieldDescriptor.class);
        when(relationInDto.inRequest()).thenReturn(false);
        when(relationInDto.inDto()).thenReturn(true);
        when(relationInDto.getRelType()).thenReturn(RelationshipType.MANY_TO_ONE);
        when(relationInDto.isEmbedded()).thenReturn(false);
        assertTrue(invokeIsInRequestDto(gen, relationInDto));

        FieldDescriptor plainInDto = mock(FieldDescriptor.class);
        when(plainInDto.inRequest()).thenReturn(false);
        when(plainInDto.inDto()).thenReturn(true);
        when(plainInDto.getRelType()).thenReturn(RelationshipType.NONE);
        when(plainInDto.isEmbedded()).thenReturn(false);
        assertFalse(invokeIsInRequestDto(gen, plainInDto));

        FieldDescriptor embeddedRelation = mock(FieldDescriptor.class);
        when(embeddedRelation.inRequest()).thenReturn(false);
        when(embeddedRelation.inDto()).thenReturn(true);
        when(embeddedRelation.getRelType()).thenReturn(RelationshipType.MANY_TO_ONE);
        when(embeddedRelation.isEmbedded()).thenReturn(true);
        assertFalse(invokeIsInRequestDto(gen, embeddedRelation));
    }

    @Test
    void determineUsesSkipsManyToOneAndEmbeddedRelations() throws Exception {
        MapperGenerator gen = new MapperGenerator();
        ModelDescriptor model = mock(ModelDescriptor.class);

        FieldDescriptor collectionRel = mock(FieldDescriptor.class);
        when(collectionRel.isTargetCrud()).thenReturn(true);
        when(collectionRel.getRelType()).thenReturn(RelationshipType.ONE_TO_MANY);
        when(collectionRel.isEmbedded()).thenReturn(false);
        when(collectionRel.getTargetType()).thenReturn("x.y.Child");

        FieldDescriptor manyToOne = mock(FieldDescriptor.class);
        when(manyToOne.isTargetCrud()).thenReturn(true);
        when(manyToOne.getRelType()).thenReturn(RelationshipType.MANY_TO_ONE);
        when(manyToOne.isEmbedded()).thenReturn(false);
        when(manyToOne.getTargetType()).thenReturn("x.y.Parent");

        FieldDescriptor embedded = mock(FieldDescriptor.class);
        when(embedded.isTargetCrud()).thenReturn(true);
        when(embedded.getRelType()).thenReturn(RelationshipType.ONE_TO_ONE);
        when(embedded.isEmbedded()).thenReturn(true);
        when(embedded.getTargetType()).thenReturn("x.y.Address");

        when(model.getFields()).thenReturn(List.of(collectionRel, manyToOne, embedded));

        @SuppressWarnings("unchecked")
        List<TypeName> uses =
                (List<TypeName>)
                        invokePrivate(
                                gen,
                                "determineUses",
                                new Class<?>[] {ModelDescriptor.class},
                                model);

        assertEquals(1, uses.size());
        assertTrue(uses.get(0).toString().endsWith("ChildMapper"));
    }

    @Test
    void specializedMappersIncludeManyToOneForMatchingDtoAndIgnoreAbstractRelations()
            throws Exception {
        MapperGenerator gen = new MapperGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        TypeFactory tf = new TypeFactory();

        FieldDescriptor specializedManyToOne =
                new FieldDescriptor(
                        new Identity("author", tf.type(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[] {"List"}, false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.MANY_TO_ONE,
                                "",
                                "nl.other.Author",
                                true,
                                false,
                                false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        FieldDescriptor nonMatchingManyToOne =
                new FieldDescriptor(
                        new Identity(
                                "publisher", tf.type(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[] {"Map"}, false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.MANY_TO_ONE,
                                "",
                                "nl.other.Publisher",
                                true,
                                false,
                                false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        FieldDescriptor abstractRelation =
                new FieldDescriptor(
                        new Identity("owner", tf.type(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[] {"List"}, false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.MANY_TO_ONE,
                                "",
                                "nl.other.Owner",
                                true,
                                false,
                                true),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        ModelDescriptor md =
                descriptor(List.of(specializedManyToOne, nonMatchingManyToOne, abstractRelation));

        String code = gen.build(md, ctx).toString();
        assertTrue(code.contains("qualifiedByName = \"SampleToAuthorRef\""));
        assertTrue(code.contains("target = \"owner\""));
        assertTrue(code.contains("ignore = true"));

        @SuppressWarnings("unchecked")
        List<MethodSpec> specialized =
                (List<MethodSpec>)
                        invokePrivate(
                                gen,
                                "generateSpecializedMappers",
                                new Class<?>[] {
                                    ModelDescriptor.class,
                                    ClassName.class,
                                    String.class,
                                    String.class,
                                    ClassName.class,
                                    List.class,
                                    List.class
                                },
                                md,
                                ClassName.get("com.example", "Sample"),
                                "com.example",
                                "Sample",
                                ClassName.get("org.mapstruct", "Mapping"),
                                List.of(specializedManyToOne, nonMatchingManyToOne),
                                List.of(abstractRelation));

        String listMapper =
                specialized.stream()
                        .filter(method -> method.name().equals("toListResponse"))
                        .findFirst()
                        .orElseThrow()
                        .toString();
        String mapMapper =
                specialized.stream()
                        .filter(method -> method.name().equals("toMapResponse"))
                        .findFirst()
                        .orElseThrow()
                        .toString();
        assertTrue(listMapper.contains("SampleToAuthorRef"));
        assertFalse(listMapper.contains("SampleToPublisherRef"));
        assertTrue(mapMapper.contains("SampleToPublisherRef"));
        assertFalse(mapMapper.contains("SampleToAuthorRef"));
    }

    @Test
    void privateFilterHelpersCoverShortCircuitBranches() throws Exception {
        MapperGenerator gen = new MapperGenerator();
        TypeFactory tf = new TypeFactory();
        ModelDescriptor model = mock(ModelDescriptor.class);

        FieldDescriptor notCrud = mock(FieldDescriptor.class);
        when(notCrud.isTargetCrud()).thenReturn(false);

        FieldDescriptor noRelation = mock(FieldDescriptor.class);
        when(noRelation.isTargetCrud()).thenReturn(true);
        when(noRelation.getRelType()).thenReturn(RelationshipType.NONE);

        FieldDescriptor embedded = mock(FieldDescriptor.class);
        when(embedded.isTargetCrud()).thenReturn(true);
        when(embedded.getRelType()).thenReturn(RelationshipType.MANY_TO_ONE);
        when(embedded.isEmbedded()).thenReturn(true);

        FieldDescriptor abstractTarget = mock(FieldDescriptor.class);
        when(abstractTarget.isTargetCrud()).thenReturn(true);
        when(abstractTarget.getRelType()).thenReturn(RelationshipType.MANY_TO_ONE);
        when(abstractTarget.isEmbedded()).thenReturn(false);
        when(abstractTarget.isTargetAbstract()).thenReturn(true);

        FieldDescriptor notInRequest = mock(FieldDescriptor.class);
        when(notInRequest.isTargetCrud()).thenReturn(true);
        when(notInRequest.getRelType()).thenReturn(RelationshipType.MANY_TO_ONE);
        when(notInRequest.isEmbedded()).thenReturn(false);
        when(notInRequest.isTargetAbstract()).thenReturn(false);
        when(notInRequest.inRequest()).thenReturn(false);
        when(notInRequest.inDto()).thenReturn(false);

        FieldDescriptor relation =
                new FieldDescriptor(
                        new Identity("team", tf.listOf(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.ONE_TO_MANY,
                                "",
                                "nl.other.Team",
                                true,
                                false,
                                false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));

        when(model.getFields())
                .thenReturn(
                        List.of(
                                notCrud,
                                noRelation,
                                embedded,
                                abstractTarget,
                                notInRequest,
                                relation));

        @SuppressWarnings("unchecked")
        List<FieldDescriptor> relationFields =
                (List<FieldDescriptor>)
                        invokePrivate(
                                gen,
                                "relationFields",
                                new Class<?>[] {ModelDescriptor.class},
                                model);
        @SuppressWarnings("unchecked")
        List<FieldDescriptor> manyToOneFields =
                (List<FieldDescriptor>)
                        invokePrivate(
                                gen,
                                "manyToOneFields",
                                new Class<?>[] {ModelDescriptor.class},
                                model);
        @SuppressWarnings("unchecked")
        List<FieldDescriptor> abstractFields =
                (List<FieldDescriptor>)
                        invokePrivate(
                                gen,
                                "abstractRelationFields",
                                new Class<?>[] {ModelDescriptor.class},
                                model);
        @SuppressWarnings("unchecked")
        List<TypeName> uses =
                (List<TypeName>)
                        invokePrivate(
                                gen,
                                "determineUses",
                                new Class<?>[] {ModelDescriptor.class},
                                model);

        String idField =
                (String)
                        invokePrivate(
                                gen,
                                "idFieldName",
                                new Class<?>[] {FieldDescriptor.class},
                                relation);
        FieldDescriptor oneLetterPlural =
                new FieldDescriptor(
                        new Identity("s", tf.setOf(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.ONE_TO_MANY,
                                "",
                                "nl.other.Team",
                                true,
                                false,
                                false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        String oneLetterIdField =
                (String)
                        invokePrivate(
                                gen,
                                "idFieldName",
                                new Class<?>[] {FieldDescriptor.class},
                                oneLetterPlural);
        String simpleIdField =
                (String)
                        invokePrivate(
                                gen,
                                "idFieldName",
                                new Class<?>[] {FieldDescriptor.class},
                                simpleField(tf.type(String.class)));

        assertEquals(List.of(relation), relationFields);
        assertTrue(manyToOneFields.isEmpty());
        assertEquals(List.of(abstractTarget), abstractFields);
        assertEquals(1, uses.size());
        assertEquals("teamIds", idField);
        assertEquals("sIds", oneLetterIdField);
        assertEquals("nameId", simpleIdField);
    }

    @Test
    void helperGenerationCoversDuplicateReferencesAndCompositeIds() throws Exception {
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("t.Key", "package t; public class Key {}"),
                        JavaFileObjects.forSourceString(
                                "t.Composite",
                                "package t; import jakarta.persistence.EmbeddedId; public class"
                                        + " Composite { @EmbeddedId public Key id; }"));
        WriteContext ctx = new WriteContext(new ElementsOnlyEnv(elements));
        MapperGenerator gen = new MapperGenerator();
        TypeFactory tf = new TypeFactory();

        FieldDescriptor first = parentField(tf.type(String.class));
        FieldDescriptor duplicate = parentField(tf.type(String.class));
        @SuppressWarnings("unchecked")
        List<com.palantir.javapoet.MethodSpec> refHelpers =
                (List<com.palantir.javapoet.MethodSpec>)
                        invokePrivate(
                                gen,
                                "manyToOneRefHelpers",
                                new Class<?>[] {String.class, List.class},
                                "Sample",
                                List.of(first, duplicate));

        FieldDescriptor composite =
                new FieldDescriptor(
                        new Identity(
                                "composites", tf.setOf(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.ONE_TO_MANY,
                                "",
                                "t.Composite",
                                true,
                                false,
                                false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        @SuppressWarnings("unchecked")
        List<com.palantir.javapoet.MethodSpec> idHelpers =
                (List<com.palantir.javapoet.MethodSpec>)
                        invokePrivate(
                                gen,
                                "relationIdHelpers",
                                new Class<?>[] {
                                    String.class,
                                    List.class,
                                    com.palantir.javapoet.ClassName.class,
                                    WriteContext.class
                                },
                                "Sample",
                                List.of(composite),
                                com.palantir.javapoet.ClassName.get(java.util.UUID.class),
                                ctx);
        String idHelperCode = idHelpers.toString();

        assertEquals(1, refHelpers.size());
        assertTrue(idHelperCode.contains("mapCompositeSet"));
        assertFalse(idHelperCode.contains("entity.setId(id)"));
    }

    @Test
    void privateCollectionHelperCoversSuffixBranches() throws Exception {
        MapperGenerator gen = new MapperGenerator();
        TypeFactory tf = new TypeFactory();

        assertEquals(
                "Set",
                invokePrivate(
                        gen,
                        "collectionSuffix",
                        new Class<?>[] {FieldDescriptor.class},
                        childrenField(tf.setOf(String.class))));
        FieldDescriptor listRelation =
                new FieldDescriptor(
                        new Identity(
                                "authors", tf.listOf(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.ONE_TO_MANY,
                                "",
                                "nl.other.Author",
                                true,
                                false,
                                false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        assertEquals(
                "List",
                invokePrivate(
                        gen,
                        "collectionSuffix",
                        new Class<?>[] {FieldDescriptor.class},
                        listRelation));
        assertEquals(
                "",
                invokePrivate(
                        gen,
                        "collectionSuffix",
                        new Class<?>[] {FieldDescriptor.class},
                        parentField(tf.type(String.class))));
    }

    @Test
    void relationIdHelpersGenerateDistinctListAndSetCollectionMethods() throws Exception {
        MapperGenerator gen = new MapperGenerator();
        TypeFactory tf = new TypeFactory();
        WriteContext ctx =
                new WriteContext(
                        new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false)));

        FieldDescriptor children = childrenField(tf.setOf(String.class));
        FieldDescriptor authors =
                new FieldDescriptor(
                        new Identity(
                                "authors", tf.listOf(String.class), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(
                                RelationshipType.ONE_TO_MANY,
                                "",
                                "nl.other.Author",
                                true,
                                false,
                                false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));

        @SuppressWarnings("unchecked")
        List<MethodSpec> idHelpers =
                (List<MethodSpec>)
                        invokePrivate(
                                gen,
                                "relationIdHelpers",
                                new Class<?>[] {
                                    String.class, List.class, ClassName.class, WriteContext.class
                                },
                                "Sample",
                                List.of(children, authors),
                                ClassName.get(java.util.UUID.class),
                                ctx);
        String code = idHelpers.toString();

        assertTrue(code.contains("SampleMapChildSet"));
        assertTrue(code.contains("mapChildSet"));
        assertTrue(code.contains("Collectors.toSet()"));
        assertTrue(code.contains("SampleMapAuthorList"));
        assertTrue(code.contains("mapAuthorList"));
        assertTrue(code.contains("Collectors.toList()"));
        assertFalse(code.contains("SampleMapChildList"));
        assertFalse(code.contains("SampleMapAuthorSet"));
    }

    private static boolean invokeHasSingleId(MapperGenerator gen, String fqcn, WriteContext ctx)
            throws Exception {
        return (boolean)
                invokePrivate(
                        gen,
                        "hasSingleId",
                        new Class<?>[] {String.class, WriteContext.class},
                        fqcn,
                        ctx);
    }

    private static boolean invokeIsInRequestDto(MapperGenerator gen, FieldDescriptor fd)
            throws Exception {
        return (boolean)
                invokePrivate(gen, "isInRequestDto", new Class<?>[] {FieldDescriptor.class}, fd);
    }

    private static Object invokePrivate(
            MapperGenerator gen, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method m = MapperGenerator.class.getDeclaredMethod(methodName, parameterTypes);
        m.setAccessible(true);
        return m.invoke(gen, args);
    }

    private static int occurrences(String haystack, String needle) {
        int count = 0;
        int index = haystack.indexOf(needle);
        while (index >= 0) {
            count++;
            index = haystack.indexOf(needle, index + needle.length());
        }
        return count;
    }

    private static final class ElementsOnlyEnv implements ProcessingEnvironment {
        private final Elements elements;

        private ElementsOnlyEnv(Elements elements) {
            this.elements = elements;
        }

        @Override
        public Map<String, String> getOptions() {
            return Map.of();
        }

        @Override
        public Messager getMessager() {
            return null;
        }

        @Override
        public Filer getFiler() {
            return null;
        }

        @Override
        public Elements getElementUtils() {
            return elements;
        }

        @Override
        public Types getTypeUtils() {
            return null;
        }

        @Override
        public SourceVersion getSourceVersion() {
            return SourceVersion.latest();
        }

        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }
    }
}

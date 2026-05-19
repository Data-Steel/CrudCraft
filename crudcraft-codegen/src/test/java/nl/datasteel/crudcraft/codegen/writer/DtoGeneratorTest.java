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

package nl.datasteel.crudcraft.codegen.writer;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.WildcardTypeName;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/** Tests for DtoGenerator. */
class DtoGeneratorTest {

    private WriteContext ctx;
    private Messager messager;
    private ProcessingEnvironment env;

    @BeforeEach
    void setup() {
        env = mock(ProcessingEnvironment.class);
        messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);
        ctx = new WriteContext(env);
    }

    @Test
    void returnsEmptyListForInvalidModel() {
        DtoGenerator gen = new DtoGenerator();
        assertTrue(gen.generate(null, ctx).isEmpty());
    }

    @Test
    void generatesThreeBasicDtos() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields()).thenReturn(List.of());

        DtoGenerator gen = new DtoGenerator();
        List<JavaFile> files = gen.generate(md, ctx);
        assertEquals(0, gen.order());
        assertEquals(3, files.size());
        assertEquals("BookRequestDto", files.get(0).typeSpec().name());
        assertEquals("BookResponseDto", files.get(1).typeSpec().name());
        assertEquals("BookRef", files.get(2).typeSpec().name());
    }

    @Test
    void lobFieldInRequestDtoAllowsJsonDeserialization() {
        // Use real TypeMirrors from compiler
        javax.lang.model.util.Elements elems =
                nl.datasteel.crudcraft.codegen.CompilationTestUtils.elements(
                        "t.C",
                        "package t; class C { java.util.UUID id; byte[] attachment; String title;"
                                + " String ignored; }");
        javax.lang.model.element.TypeElement type = elems.getTypeElement("t.C");
        javax.lang.model.type.TypeMirror idType =
                type.getEnclosedElements().stream()
                        .filter(e -> e.getSimpleName().contentEquals("id"))
                        .findFirst()
                        .orElseThrow()
                        .asType();
        javax.lang.model.type.TypeMirror byteArrayType =
                type.getEnclosedElements().stream()
                        .filter(e -> e.getSimpleName().contentEquals("attachment"))
                        .findFirst()
                        .orElseThrow()
                        .asType();
        javax.lang.model.type.TypeMirror stringType =
                type.getEnclosedElements().stream()
                        .filter(e -> e.getSimpleName().contentEquals("title"))
                        .findFirst()
                        .orElseThrow()
                        .asType();
        javax.lang.model.type.TypeMirror ignoredType =
                type.getEnclosedElements().stream()
                        .filter(e -> e.getSimpleName().contentEquals("ignored"))
                        .findFirst()
                        .orElseThrow()
                        .asType();

        FieldDescriptor idField =
                new FieldDescriptor(
                        new Identity("id", idType, null, SchemaMetadata.empty()),
                        new DtoOptions(true, false, false, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));

        FieldDescriptor lobField =
                new FieldDescriptor(
                        new Identity("attachment", byteArrayType, null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, false, new String[0], true),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));

        FieldDescriptor normalField =
                new FieldDescriptor(
                        new Identity("title", stringType, null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, false, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));

        FieldDescriptor ignoredField =
                new FieldDescriptor(
                        new Identity("ignored", ignoredType, null, SchemaMetadata.empty()),
                        new DtoOptions(false, false, false, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));

        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Document");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields()).thenReturn(List.of(idField, normalField, lobField, ignoredField));

        DtoGenerator gen = new DtoGenerator();
        List<JavaFile> files = gen.generate(md, ctx);

        // Request DTO should NOT have @JsonIgnore on LOB fields
        // because bulk/validate endpoints still use @RequestBody and need JSON LOB data
        String requestCode = files.get(0).toString();
        assertFalse(
                requestCode.contains("@JsonIgnore"),
                "LOB field in request DTO should not have @JsonIgnore");

        // Response DTO should also not have @JsonIgnore
        String responseCode = files.get(1).toString();
        assertFalse(
                responseCode.contains("@JsonIgnore"),
                "Response DTO should not have @JsonIgnore on LOB fields");
        assertFalse(requestCode.contains("@SuppressFBWarnings"));
        assertFalse(responseCode.contains("@SuppressFBWarnings"));

        String refCode = files.get(2).toString();
        assertTrue(refCode.contains("UUID id"));
        assertFalse(refCode.contains("attachment"));
        assertFalse(refCode.contains("title"));
        assertFalse(requestCode.contains("ignored"));
    }

    @Test
    void generatedDtosExposeFieldSecurityMetadata() {
        javax.lang.model.util.Elements elems =
                nl.datasteel.crudcraft.codegen.CompilationTestUtils.elements(
                        "t.C", "package t; class C { String secret; }");
        javax.lang.model.element.TypeElement type = elems.getTypeElement("t.C");
        javax.lang.model.type.TypeMirror stringType =
                type.getEnclosedElements().stream()
                        .filter(e -> e.getSimpleName().contentEquals("secret"))
                        .findFirst()
                        .orElseThrow()
                        .asType();

        FieldDescriptor securedField =
                new FieldDescriptor(
                        new Identity("secret", stringType, null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, false, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(
                                true,
                                new String[] {"ADMIN"},
                                new String[] {"ADMIN"},
                                WritePolicy.FAIL_ON_DENIED));

        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Account");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields()).thenReturn(List.of(securedField));

        DtoGenerator gen = new DtoGenerator();
        List<JavaFile> files = gen.generate(md, ctx);
        String requestCode = files.get(0).toString();
        String responseCode = files.get(1).toString();

        assertTrue(requestCode.contains("fieldSecurityMetadata()"));
        assertFalse(requestCode.contains("@SuppressFBWarnings"));
        assertTrue(requestCode.contains("@FieldSecurity"));
        assertTrue(requestCode.contains("WritePolicy.FAIL_ON_DENIED"));
        assertTrue(responseCode.contains("fieldSecurityMetadata()"));
        assertFalse(responseCode.contains("@SuppressFBWarnings"));
        assertTrue(responseCode.contains("@FieldSecurity"));
        assertTrue(responseCode.contains("dto -> dto.secret()"));
    }

    @Test
    void generatedDtosCarryNullnessAnnotations() {
        Elements elems =
                nl.datasteel.crudcraft.codegen.CompilationTestUtils.elements(
                        "t.Book",
                        "package t; class Book { java.util.UUID id; String title; String"
                                + " required; String projected; int count; }");
        TypeElement book = elems.getTypeElement("t.Book");
        FieldDescriptor id =
                descriptorField(
                        "id",
                        type(book, "id"),
                        RelationshipType.NONE,
                        "",
                        true,
                        false,
                        true,
                        new String[0],
                        false);
        FieldDescriptor title =
                descriptorField(
                        "title",
                        type(book, "title"),
                        RelationshipType.NONE,
                        "",
                        true,
                        true,
                        false,
                        new String[0],
                        false);
        FieldDescriptor required =
                new FieldDescriptor(
                        new Identity("required", type(book, "required"), null, SchemaMetadata.empty()),
                        new DtoOptions(true, true, false, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of(notNullValidation())),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        FieldDescriptor projected =
                new FieldDescriptor(
                        new Identity(
                                "projected",
                                type(book, "projected"),
                                null,
                                SchemaMetadata.empty(),
                                "author.name"),
                        new DtoOptions(true, true, false, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        FieldDescriptor count =
                descriptorField(
                        "count",
                        type(book, "count"),
                        RelationshipType.NONE,
                        "",
                        true,
                        true,
                        false,
                        new String[0],
                        false);
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields()).thenReturn(List.of(id, title, required, projected, count));

        List<JavaFile> files = new DtoGenerator().generate(md, ctx);

        String responseCode = files.get(1).toString();
        assertTrue(responseCode.contains("@NotNull UUID id"));
        assertTrue(responseCode.contains("@Nullable String title"));
        assertTrue(responseCode.contains("@NotNull String required"));
        assertFalse(responseCode.contains("@Nullable @NotNull"));
        assertFalse(responseCode.contains("@Nullable int count"));
        assertTrue(responseCode.contains("@ProjectionField(\"author.name\") String projected"));
    }

    @Test
    void generatesSpecializedAndEmbeddedDtosForRelations() {
        Elements elems =
                nl.datasteel.crudcraft.codegen.CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString(
                                "t.Author",
                                "package t; public class Author { public java.util.UUID id; }"),
                        JavaFileObjects.forSourceString(
                                "t.Address",
                                "package t; public class Address { public String street; }"),
                        JavaFileObjects.forSourceString(
                                "t.Book",
                                "package t; public class Book { "
                                        + "public java.util.UUID id; "
                                        + "public String title; "
                                        + "public Author author; "
                                        + "public java.util.List<Author> coAuthors; "
                                        + "public Address address; }"));
        TypeElement book = elems.getTypeElement("t.Book");

        FieldDescriptor idField =
                descriptorField(
                        "id",
                        type(book, "id"),
                        RelationshipType.NONE,
                        null,
                        true,
                        false,
                        true,
                        new String[0],
                        false);
        FieldDescriptor titleField =
                descriptorField(
                        "title",
                        type(book, "title"),
                        RelationshipType.NONE,
                        null,
                        true,
                        true,
                        true,
                        new String[] {"summary"},
                        false);
        FieldDescriptor authorField =
                descriptorField(
                        "author",
                        type(book, "author"),
                        RelationshipType.MANY_TO_ONE,
                        "t.Author",
                        true,
                        false,
                        true,
                        new String[] {"summary"},
                        false);
        FieldDescriptor coAuthorsField =
                descriptorField(
                        "coAuthors",
                        type(book, "coAuthors"),
                        RelationshipType.ONE_TO_MANY,
                        "t.Author",
                        true,
                        false,
                        true,
                        new String[] {"summary"},
                        false);
        FieldDescriptor addressField =
                descriptorField(
                        "address",
                        type(book, "address"),
                        RelationshipType.NONE,
                        "t.Address",
                        true,
                        true,
                        true,
                        new String[] {"summary"},
                        true);

        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        when(md.getPackageName()).thenReturn("t");
        when(md.getFields())
                .thenReturn(
                        List.of(idField, titleField, authorField, coAuthorsField, addressField));
        when(md.isAbstract()).thenReturn(false);

        DtoGenerator gen = new DtoGenerator();
        List<JavaFile> files = gen.generate(md, ctx);

        assertEquals(4, files.size());
        String request = files.get(0).toString();
        String specialized = files.get(3).toString();

        assertTrue(request.contains("UUID authorId"));
        assertTrue(occurrences(request, "UUID authorId") >= 1);
        assertTrue(request.contains("List<UUID> coAuthorIds"));
        assertTrue(request.contains("AddressRequestDto address"));
        assertTrue(request.contains("@Valid"));
        assertTrue(request.contains("@Valid AddressRequestDto address"));
        assertFalse(request.contains("@Valid UUID authorId"));
        assertTrue(request.contains("public Builder authorId(@Nullable UUID authorId)"));
        assertTrue(
                request.contains(
                        "public Builder address(@Nullable @Valid AddressRequestDto address)"));
        assertFalse(request.contains("addressId"));
        assertTrue(request.contains("class Builder"));
        String response = files.get(1).toString();
        assertTrue(response.contains("class Builder"));
        assertFalse(response.contains("@Valid"));
        assertTrue(files.get(2).toString().contains("class Builder"));
        assertTrue(specialized.contains("record BookSummaryResponseDto"));
        assertTrue(specialized.contains("UUID id"));
    }

    @Test
    void canDisableGeneratedWithersForRecordDtos() {
        when(env.getOptions()).thenReturn(Map.of(DtoGenerator.GENERATE_WITHERS_OPTION, "false"));
        Elements elems =
                nl.datasteel.crudcraft.codegen.CompilationTestUtils.elements(
                        "t.Book", "package t; class Book { String title; }");
        TypeElement book = elems.getTypeElement("t.Book");
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields())
                .thenReturn(
                        List.of(
                                descriptorField(
                                        "title",
                                        type(book, "title"),
                                        RelationshipType.NONE,
                                        "",
                                        true,
                                        true,
                                        false,
                                        new String[0],
                                        false)));
        when(md.isAbstract()).thenReturn(false);

        List<JavaFile> files = new DtoGenerator().generate(md, ctx);

        String requestCode = files.get(0).toString();
        assertFalse(requestCode.contains("withTitle("));
        assertTrue(requestCode.contains("public static Builder builder()"));
    }

    @Test
    void generatedWithersAreOptInForRecordDtos() {
        Elements elems =
                nl.datasteel.crudcraft.codegen.CompilationTestUtils.elements(
                        "t.Book", "package t; class Book { String title; }");
        TypeElement book = elems.getTypeElement("t.Book");
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields())
                .thenReturn(
                        List.of(
                                descriptorField(
                                        "title",
                                        type(book, "title"),
                                        RelationshipType.NONE,
                                        "",
                                        true,
                                        true,
                                        false,
                                        new String[0],
                                        false)));
        when(md.isAbstract()).thenReturn(false);

        List<JavaFile> defaultFiles = new DtoGenerator().generate(md, ctx);
        assertFalse(defaultFiles.get(0).toString().contains("withTitle("));

        when(env.getOptions()).thenReturn(Map.of(DtoGenerator.GENERATE_WITHERS_OPTION, "true"));
        List<JavaFile> optInFiles = new DtoGenerator().generate(md, ctx);
        assertTrue(optInFiles.get(0).toString().contains("withTitle("));
    }

    @Test
    void shouldGenerateWithersHandlesNullContextAndExplicitOptions() throws Exception {
        DtoGenerator gen = new DtoGenerator();
        assertFalse(
                (boolean)
                        invokePrivate(
                                gen,
                                "shouldGenerateWithers",
                                new Class<?>[] {WriteContext.class},
                                (Object) null));

        ProcessingEnvironment noOptionsEnv = mock(ProcessingEnvironment.class);
        when(noOptionsEnv.getOptions()).thenReturn(null);
        assertFalse(
                (boolean)
                        invokePrivate(
                                gen,
                                "shouldGenerateWithers",
                                new Class<?>[] {WriteContext.class},
                                new WriteContext(noOptionsEnv)));

        ProcessingEnvironment withOptionsEnv = mock(ProcessingEnvironment.class);
        when(withOptionsEnv.getOptions())
                .thenReturn(Map.of(DtoGenerator.GENERATE_WITHERS_OPTION, "true"));
        assertTrue(
                (boolean)
                        invokePrivate(
                                gen,
                                "shouldGenerateWithers",
                                new Class<?>[] {WriteContext.class},
                                new WriteContext(withOptionsEnv)));
    }

    @Test
    void requestDtosWithLobFieldsKeepWithersForMultipartControllerCode() {
        when(env.getOptions()).thenReturn(Map.of());
        Elements elems =
                nl.datasteel.crudcraft.codegen.CompilationTestUtils.elements(
                        "t.Document", "package t; class Document { String title; byte[] file; }");
        TypeElement document = elems.getTypeElement("t.Document");
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Document");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.hasLobFields()).thenReturn(true);
        when(md.getFields())
                .thenReturn(
                        List.of(
                                descriptorField(
                                        "title",
                                        type(document, "title"),
                                        RelationshipType.NONE,
                                        "",
                                        true,
                                        true,
                                        false,
                                        new String[0],
                                        false),
                                descriptorField(
                                        "file",
                                        type(document, "file"),
                                        RelationshipType.NONE,
                                        "",
                                        true,
                                        true,
                                        false,
                                        new String[0],
                                        false)));
        when(md.isAbstract()).thenReturn(false);

        List<JavaFile> files = new DtoGenerator().generate(md, ctx);

        assertTrue(files.get(0).toString().contains("withFile("));
        assertTrue(
                files.get(0).toString().contains("return new DocumentRequestDto(this.title, file)"));
        assertFalse(files.get(1).toString().contains("withFile("));
    }

    @Test
    void abstractModelGeneratesOnlyRefDto() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("AuditEvent");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields()).thenReturn(List.of());
        when(md.isAbstract()).thenReturn(true);

        DtoGenerator gen = new DtoGenerator();
        List<JavaFile> files = gen.generate(md, ctx);

        assertEquals(1, files.size());
        assertEquals("AuditEventRef", files.get(0).typeSpec().name());
        verify(messager)
                .printMessage(
                        org.mockito.ArgumentMatchers.eq(Diagnostic.Kind.NOTE),
                        org.mockito.ArgumentMatchers.contains("Only generating Ref DTO"));
    }

    @Test
    void additionalResponseDtoCoversRelationSecurityEnumAndCopyBranches() {
        when(env.getOptions()).thenReturn(Map.of(DtoGenerator.GENERATE_WITHERS_OPTION, "true"));
        Elements elems =
                nl.datasteel.crudcraft.codegen.CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString(
                                "t.Author",
                                "package t; public class Author { public java.util.UUID id; }"),
                        JavaFileObjects.forSourceString(
                                "t.Address",
                                "package t; public class Address { public String street; }"),
                        JavaFileObjects.forSourceString(
                                "t.Source",
                                "package t; public class Source { "
                                        + "public java.util.Set<String> tags; "
                                        + "public java.util.List<String> names; "
                                        + "public byte[] bytes; "
                                        + "public int secureCount; "
                                        + "public Author author; "
                                        + "public Address address; "
                                        + "public String status; "
                                        + "public String plain; "
                                        + "public String unrelated; }"));
        TypeElement source = elems.getTypeElement("t.Source");

        FieldDescriptor tags =
                descriptorField(
                        "tags",
                        type(source, "tags"),
                        RelationshipType.NONE,
                        null,
                        true,
                        true,
                        true,
                        new String[] {"detail"},
                        false);
        FieldDescriptor names =
                descriptorField(
                        "names",
                        type(source, "names"),
                        RelationshipType.NONE,
                        null,
                        true,
                        true,
                        true,
                        new String[] {"detail"},
                        false);
        FieldDescriptor bytes =
                descriptorField(
                        "bytes",
                        type(source, "bytes"),
                        RelationshipType.NONE,
                        null,
                        true,
                        true,
                        true,
                        new String[] {"detail"},
                        false);
        FieldDescriptor securedPrimitive =
                new FieldDescriptor(
                        new Identity(
                                "secureCount",
                                type(source, "secureCount"),
                                "count",
                                SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[] {"detail"}, false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(
                                true,
                                new String[] {"ADMIN"},
                                new String[] {"ADMIN"},
                                WritePolicy.FAIL_ON_DENIED));
        FieldDescriptor author =
                descriptorField(
                        "author",
                        type(source, "author"),
                        RelationshipType.MANY_TO_ONE,
                        "t.Author",
                        true,
                        true,
                        true,
                        new String[] {"detail"},
                        false);
        FieldDescriptor embeddedAddress =
                descriptorField(
                        "address",
                        type(source, "address"),
                        RelationshipType.MANY_TO_ONE,
                        "t.Address",
                        true,
                        true,
                        true,
                        new String[] {"detail"},
                        true);
        FieldDescriptor enumString =
                new FieldDescriptor(
                        new Identity(
                                "status", type(source, "status"), "status", SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[] {"detail"}, false),
                        new EnumOptions(true, List.of("OPEN", "CLOSED")),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        FieldDescriptor plain =
                new FieldDescriptor(
                        new Identity("plain", type(source, "plain"), "   ", SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[] {"detail"}, false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        FieldDescriptor unrelated =
                new FieldDescriptor(
                        new Identity(
                                "unrelated",
                                type(source, "unrelated"),
                                "unrelated",
                                SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));

        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Source");
        when(md.getPackageName()).thenReturn("t");
        when(md.getFields())
                .thenReturn(
                        List.of(
                                tags,
                                names,
                                bytes,
                                securedPrimitive,
                                author,
                                embeddedAddress,
                                enumString,
                                plain,
                                unrelated));
        when(md.isAbstract()).thenReturn(false);

        DtoGenerator gen = new DtoGenerator();
        List<JavaFile> files = gen.generate(md, ctx);
        String detailed =
                files.stream()
                        .filter(f -> "SourceDetailResponseDto".equals(f.typeSpec().name()))
                        .findFirst()
                        .orElseThrow()
                        .toString();
        String request = files.get(0).toString();
        String response = files.get(1).toString();

        assertTrue(detailed.contains("Set.copyOf("));
        assertTrue(detailed.contains("List.copyOf("));
        assertTrue(detailed.contains("Arrays.copyOf("));
        assertTrue(detailed.contains("\n        tags = tags == null ? null : Set.copyOf(tags);\n"));
        assertTrue(detailed.contains("\n        names = names == null ? null : List.copyOf(names);\n"));
        assertTrue(
                detailed.contains(
                        "\n        bytes = bytes == null ? null : Arrays.copyOf(bytes,"
                                + " bytes.length);\n"));
        assertTrue(detailed.contains("public byte[] bytes()"));
        assertTrue(
                detailed.contains(
                        "return bytes == null ? null : Arrays.copyOf(bytes, bytes.length);"));
        assertTrue(
                detailed.contains(
                        "public SourceDetailResponseDto withTags(@Nullable Set<String> tags)"));
        assertTrue(
                detailed.contains(
                        "return new SourceDetailResponseDto(tags, this.names, this.bytes,"
                                + " this.secureCount, this.author, this.address, this.status,"
                                + " this.plain)"));
        assertTrue(detailed.contains("Integer secureCount"));
        assertTrue(detailed.contains("AuthorRef author"));
        assertTrue(detailed.contains("AddressResponseDto address"));
        assertTrue(detailed.contains("allowableValues"));
        assertTrue(detailed.contains("fieldSecurityMetadata()"));
        assertTrue(detailed.contains("class Builder"));
        assertFalse(detailed.contains("@SuppressFBWarnings"));
        assertEquals(1, occurrences(detailed, "@FieldSecurity("));
        assertFalse(detailed.contains("unrelated"));
        assertTrue(request.contains("Integer secureCount"));
        assertTrue(request.contains("allowableValues"));
        assertTrue(response.contains("Integer secureCount"));
        assertTrue(response.contains("allowableValues"));
        assertTrue(response.contains("Set.copyOf("));
        assertTrue(response.contains("List.copyOf("));
        assertTrue(response.contains("Arrays.copyOf("));
        assertTrue(response.contains("\n        tags = tags == null ? null : Set.copyOf(tags);\n"));
        assertTrue(response.contains("\n        names = names == null ? null : List.copyOf(names);\n"));
    }

    @Test
    void generatedSimpleDtosCompile() {
        Elements elems =
                nl.datasteel.crudcraft.codegen.CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString(
                                "t.Simple",
                                "package t; public class Simple { "
                                        + "public java.util.UUID id; "
                                        + "public java.util.Set<String> tags; "
                                        + "public java.util.List<String> names; "
                                        + "public byte[] bytes; "
                                        + "public int secureCount; "
                                        + "public String status; }"));
        TypeElement source = elems.getTypeElement("t.Simple");
        FieldDescriptor id =
                descriptorField(
                        "id",
                        type(source, "id"),
                        RelationshipType.NONE,
                        null,
                        true,
                        true,
                        false,
                        new String[] {"detail"},
                        false);
        FieldDescriptor tags =
                descriptorField(
                        "tags",
                        type(source, "tags"),
                        RelationshipType.NONE,
                        null,
                        true,
                        true,
                        true,
                        new String[] {"detail"},
                        false);
        FieldDescriptor names =
                descriptorField(
                        "names",
                        type(source, "names"),
                        RelationshipType.NONE,
                        null,
                        true,
                        true,
                        true,
                        new String[] {"detail"},
                        false);
        FieldDescriptor bytes =
                descriptorField(
                        "bytes",
                        type(source, "bytes"),
                        RelationshipType.NONE,
                        null,
                        true,
                        true,
                        true,
                        new String[] {"detail"},
                        false);
        FieldDescriptor securedPrimitive =
                new FieldDescriptor(
                        new Identity(
                                "secureCount",
                                type(source, "secureCount"),
                                "count",
                                SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[] {"detail"}, false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(
                                true,
                                new String[] {"ADMIN"},
                                new String[] {"ADMIN"},
                                WritePolicy.FAIL_ON_DENIED));
        FieldDescriptor enumString =
                new FieldDescriptor(
                        new Identity(
                                "status", type(source, "status"), "status", SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[] {"detail"}, false),
                        new EnumOptions(true, List.of("OPEN", "CLOSED")),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Simple");
        when(md.getPackageName()).thenReturn("t");
        when(md.getFields())
                .thenReturn(List.of(id, tags, names, bytes, securedPrimitive, enumString));

        List<JavaFileObject> sources = new ArrayList<>();
        for (JavaFile javaFile : new DtoGenerator().generate(md, ctx)) {
            sources.add(
                    JavaFileObjects.forSourceString(
                            javaFile.packageName() + "." + javaFile.typeSpec().name(),
                            javaFile.toString()));
        }
        sources.add(
                JavaFileObjects.forSourceString(
                        "edu.umd.cs.findbugs.annotations.SuppressFBWarnings",
                        "package edu.umd.cs.findbugs.annotations;"
                                + "public @interface SuppressFBWarnings {"
                                + "String[] value(); String justification() default \"\";}"));
        Compilation compilation = CompilationTestUtils.javac().compile(sources);

        assertEquals(Compilation.Status.SUCCESS, compilation.status(), compilation.errors().toString());
    }

    private static TypeMirror type(TypeElement owner, String fieldName) {
        return owner.getEnclosedElements().stream()
                .filter(e -> e.getSimpleName().contentEquals(fieldName))
                .findFirst()
                .orElseThrow()
                .asType();
    }

    private static FieldDescriptor descriptorField(
            String name,
            TypeMirror type,
            RelationshipType relationshipType,
            String targetType,
            boolean inDto,
            boolean inRequest,
            boolean inRef,
            String[] responseDtos,
            boolean embedded) {
        return new FieldDescriptor(
                new Identity(name, type, null, SchemaMetadata.empty()),
                new DtoOptions(inDto, inRequest, inRef, responseDtos, false),
                new EnumOptions(false, List.of()),
                new Relationship(relationshipType, "", targetType, false, embedded, false),
                new Validation(List.of()),
                new SearchOptions(false, List.of(), 0),
                new Security(false, null, null));
    }

    private static AnnotationSpec notNullValidation() {
        return AnnotationSpec.builder(ClassName.get("jakarta.validation.constraints", "NotNull"))
                .build();
    }

    @Test
    void additionalResponseDtoAppliesExposureSuppressionOnlyForMutableFields() throws Exception {
        Elements elements =
                nl.datasteel.crudcraft.codegen.CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString(
                                "t.DtoTypes",
                                "package t; class DtoTypes { java.util.List<String> names; "
                                        + "String title; }"));
        TypeElement owner = elements.getTypeElement("t.DtoTypes");
        FieldDescriptor names =
                descriptorField(
                        "names",
                        type(owner, "names"),
                        RelationshipType.NONE,
                        "",
                        true,
                        false,
                        false,
                        new String[] {"Detail"},
                        false);
        FieldDescriptor title =
                descriptorField(
                        "title",
                        type(owner, "title"),
                        RelationshipType.NONE,
                        "",
                        true,
                        false,
                        false,
                        new String[] {"Detail"},
                        false);
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        when(md.getPackageName()).thenReturn("t");

        DtoGenerator gen = new DtoGenerator();
        JavaFile mutable =
                (JavaFile)
                        invokePrivate(
                                gen,
                                "generateAdditionalResponseDto",
                                new Class<?>[] {
                                    ModelDescriptor.class, String.class, List.class, boolean.class
                                },
                                md,
                                "Detail",
                                List.of(names),
                                true);
        JavaFile immutable =
                (JavaFile)
                        invokePrivate(
                                gen,
                                "generateAdditionalResponseDto",
                                new Class<?>[] {
                                    ModelDescriptor.class, String.class, List.class, boolean.class
                                },
                                md,
                                "Detail",
                                List.of(title),
                                true);

        assertFalse(mutable.toString().contains("@SuppressFBWarnings"));
        assertFalse(immutable.toString().contains("@SuppressFBWarnings"));
    }

    @Test
    void privateHelpersCoverTypeAndNamingBranches() throws Exception {
        DtoGenerator gen = new DtoGenerator();

        TypeName req =
                (TypeName)
                        invokePrivate(
                                gen,
                                "resolveDtoType",
                                new Class<?>[] {
                                    TypeName.class, String.class, DtoType.class, boolean.class
                                },
                                ClassName.get(String.class),
                                "p.Target",
                                DtoType.REQUEST,
                                false);
        TypeName respEmbedded =
                (TypeName)
                        invokePrivate(
                                gen,
                                "resolveDtoType",
                                new Class<?>[] {
                                    TypeName.class, String.class, DtoType.class, boolean.class
                                },
                                ClassName.get(String.class),
                                "p.Target",
                                DtoType.RESPONSE,
                                true);
        TypeName respRef =
                (TypeName)
                        invokePrivate(
                                gen,
                                "resolveDtoType",
                                new Class<?>[] {
                                    TypeName.class, String.class, DtoType.class, boolean.class
                                },
                                ClassName.get(String.class),
                                "p.Target",
                                DtoType.RESPONSE,
                                false);
        TypeName listMapped =
                (TypeName)
                        invokePrivate(
                                gen,
                                "resolveDtoType",
                                new Class<?>[] {
                                    TypeName.class, String.class, DtoType.class, boolean.class
                                },
                                ParameterizedTypeName.get(
                                        ClassName.get(List.class), ClassName.get(String.class)),
                                "p.Target",
                                DtoType.RESPONSE,
                                true);

        assertEquals("p.dto.request.TargetRequestDto", req.toString());
        assertEquals("p.dto.response.TargetResponseDto", respEmbedded.toString());
        assertEquals("p.dto.ref.TargetRef", respRef.toString());
        assertEquals("java.util.List<p.dto.response.TargetResponseDto>", listMapped.toString());
        TypeName setIds =
                (TypeName)
                        invokePrivate(
                                gen,
                                "toIdType",
                                new Class<?>[] {TypeName.class},
                                ParameterizedTypeName.get(
                                        ClassName.get(java.util.Set.class),
                                        ClassName.get(String.class)));
        assertEquals("java.util.Set<java.util.UUID>", setIds.toString());

        boolean primitiveImmutable =
                (boolean)
                        invokePrivate(
                                gen,
                                "isSimpleImmutableType",
                                new Class<?>[] {TypeName.class},
                                TypeName.INT);
        boolean uuidImmutable =
                (boolean)
                        invokePrivate(
                                gen,
                                "isSimpleImmutableType",
                                new Class<?>[] {TypeName.class},
                                ClassName.get(UUID.class));
        boolean objectImmutable =
                (boolean)
                        invokePrivate(
                                gen,
                                "isSimpleImmutableType",
                                new Class<?>[] {TypeName.class},
                                ClassName.get(Object.class));
        boolean arrayImmutable =
                (boolean)
                        invokePrivate(
                                gen,
                                "isSimpleImmutableType",
                                new Class<?>[] {TypeName.class},
                                ArrayTypeName.of(TypeName.INT));
        boolean parameterizedImmutable =
                (boolean)
                        invokePrivate(
                                gen,
                                "isSimpleImmutableType",
                                new Class<?>[] {TypeName.class},
                                ParameterizedTypeName.get(
                                        ClassName.get(List.class), ClassName.get(String.class)));
        boolean wildcardImmutable =
                (boolean)
                        invokePrivate(
                                gen,
                                "isSimpleImmutableType",
                                new Class<?>[] {TypeName.class},
                                WildcardTypeName.subtypeOf(Object.class));

        assertTrue(primitiveImmutable);
        assertTrue(uuidImmutable);
        assertFalse(objectImmutable);
        assertFalse(arrayImmutable);
        assertFalse(parameterizedImmutable);
        assertFalse(wildcardImmutable);
    }

    @Test
    void privateHelpersCoverGetterAndIdFieldNamingBranches() throws Exception {
        DtoGenerator gen = new DtoGenerator();
        String boolGetter =
                (String)
                        invokePrivate(
                                gen,
                                "getterName",
                                new Class<?>[] {String.class, TypeName.class},
                                "active",
                                TypeName.BOOLEAN);
        String objectGetter =
                (String)
                        invokePrivate(
                                gen,
                                "getterName",
                                new Class<?>[] {String.class, TypeName.class},
                                "name",
                                ClassName.get(String.class));
        assertEquals("isActive", boolGetter);
        assertEquals("getName", objectGetter);

        FieldDescriptor simple =
                descriptorField(
                        "category",
                        null,
                        RelationshipType.NONE,
                        null,
                        true,
                        true,
                        true,
                        new String[0],
                        false);
        FieldDescriptor singleRel =
                descriptorField(
                        "owner",
                        null,
                        RelationshipType.MANY_TO_ONE,
                        "x.Owner",
                        true,
                        true,
                        true,
                        new String[0],
                        false);
        FieldDescriptor collectionRel =
                descriptorField(
                        "indices",
                        null,
                        RelationshipType.ONE_TO_MANY,
                        "x.Index",
                        true,
                        true,
                        true,
                        new String[0],
                        false);
        FieldDescriptor embeddedRel =
                descriptorField(
                        "address",
                        null,
                        RelationshipType.MANY_TO_ONE,
                        "x.Address",
                        true,
                        true,
                        true,
                        new String[0],
                        true);

        String unchanged =
                (String)
                        invokePrivate(
                                gen,
                                "idFieldName",
                                new Class<?>[] {FieldDescriptor.class, TypeName.class},
                                simple,
                                ClassName.get(String.class));
        String withId =
                (String)
                        invokePrivate(
                                gen,
                                "idFieldName",
                                new Class<?>[] {FieldDescriptor.class, TypeName.class},
                                singleRel,
                                ClassName.get(UUID.class));
        String withIds =
                (String)
                        invokePrivate(
                                gen,
                                "idFieldName",
                                new Class<?>[] {FieldDescriptor.class, TypeName.class},
                                collectionRel,
                                ParameterizedTypeName.get(
                                        ClassName.get(List.class), ClassName.get(UUID.class)));
        String withSetIds =
                (String)
                        invokePrivate(
                                gen,
                                "idFieldName",
                                new Class<?>[] {FieldDescriptor.class, TypeName.class},
                                collectionRel,
                                ParameterizedTypeName.get(
                                        ClassName.get(java.util.Set.class),
                                        ClassName.get(UUID.class)));
        String embeddedUnchanged =
                (String)
                        invokePrivate(
                                gen,
                                "idFieldName",
                                new Class<?>[] {FieldDescriptor.class, TypeName.class},
                                embeddedRel,
                                ClassName.get(UUID.class));

        assertEquals("category", unchanged);
        assertEquals("ownerId", withId);
        assertEquals("indexIds", withIds);
        assertEquals("indexIds", withSetIds);
        assertEquals("address", embeddedUnchanged);
    }

    @Test
    void privateResolveFieldCoversDtoNameBranches() throws Exception {
        Elements elems =
                nl.datasteel.crudcraft.codegen.CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString(
                                "t.C",
                                "package t; public class C { public Owner owner; public Address"
                                        + " address; public int secureCount; } class Owner {} class"
                                        + " Address {}"));
        TypeElement owner = elems.getTypeElement("t.C");
        FieldDescriptor relation =
                descriptorField(
                        "owner",
                        type(owner, "owner"),
                        RelationshipType.MANY_TO_ONE,
                        "t.Owner",
                        true,
                        true,
                        true,
                        new String[0],
                        false);
        FieldDescriptor embedded =
                descriptorField(
                        "address",
                        type(owner, "address"),
                        RelationshipType.NONE,
                        "t.Address",
                        true,
                        true,
                        true,
                        new String[0],
                        true);
        FieldDescriptor securePrimitive =
                new FieldDescriptor(
                        new Identity(
                                "secureCount",
                                type(owner, "secureCount"),
                                null,
                                SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(
                                true,
                                new String[] {"ADMIN"},
                                new String[] {"ADMIN"},
                                WritePolicy.FAIL_ON_DENIED));
        FieldDescriptor openPrimitive =
                new FieldDescriptor(
                        new Identity(
                                "plainCount",
                                type(owner, "secureCount"),
                                null,
                                SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        DtoGenerator gen = new DtoGenerator();

        Object requestRelation =
                invokePrivate(
                        gen,
                        "resolveField",
                        new Class<?>[] {DtoType.class, FieldDescriptor.class},
                        DtoType.REQUEST,
                        relation);
        Object requestEmbedded =
                invokePrivate(
                        gen,
                        "resolveField",
                        new Class<?>[] {DtoType.class, FieldDescriptor.class},
                        DtoType.REQUEST,
                        embedded);
        Object responseRelation =
                invokePrivate(
                        gen,
                        "resolveField",
                        new Class<?>[] {DtoType.class, FieldDescriptor.class},
                        DtoType.RESPONSE,
                        relation);
        Object secure =
                invokePrivate(
                        gen,
                        "resolveField",
                        new Class<?>[] {DtoType.class, FieldDescriptor.class},
                        DtoType.RESPONSE,
                        securePrimitive);
        TypeName openPrimitiveType =
                (TypeName)
                        invokePrivate(
                                gen,
                                "boxIfSecuredPrimitive",
                                new Class<?>[] {FieldDescriptor.class, TypeName.class},
                                openPrimitive,
                                TypeName.INT);
        boolean relationOrEmbedded =
                (boolean)
                        invokePrivate(
                                gen,
                                "hasRelationOrEmbedded",
                                new Class<?>[] {FieldDescriptor.class},
                                embedded);
        boolean referenceRelation =
                (boolean)
                        invokePrivate(
                                gen,
                                "isReferenceRelation",
                                new Class<?>[] {FieldDescriptor.class},
                                relation);
        boolean embeddedReference =
                (boolean)
                        invokePrivate(
                                gen,
                                "isReferenceRelation",
                                new Class<?>[] {FieldDescriptor.class},
                                embedded);

        assertTrue(requestRelation.toString().contains("ownerId"));
        assertTrue(requestRelation.toString().contains("java.util.UUID"));
        assertTrue(requestEmbedded.toString().contains("AddressRequestDto"));
        assertTrue(responseRelation.toString().contains("OwnerRef"));
        assertTrue(secure.toString().contains("java.lang.Integer"));
        assertEquals(TypeName.INT, openPrimitiveType);
        assertTrue(relationOrEmbedded);
        assertTrue(referenceRelation);
        assertFalse(embeddedReference);
    }

    @Test
    void privateSchemaHelpersCoverEnumNullableAndEmptyJavadocBranches() throws Exception {
        Elements elems =
                nl.datasteel.crudcraft.codegen.CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString(
                                "t.C",
                                "package t; public class C { public String status; public int"
                                        + " count; public String blank; }"));
        TypeElement owner = elems.getTypeElement("t.C");
        FieldDescriptor enumField =
                new FieldDescriptor(
                        new Identity(
                                "status",
                                type(owner, "status"),
                                "status",
                                SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(true, List.of("OPEN", "CLOSED")),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        FieldDescriptor primitiveField =
                new FieldDescriptor(
                        new Identity(
                                "count",
                                type(owner, "count"),
                                "count",
                                SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        FieldDescriptor blankField =
                new FieldDescriptor(
                        new Identity("blank", type(owner, "blank"), "   ", SchemaMetadata.empty()),
                        new DtoOptions(true, true, true, new String[0], false),
                        new EnumOptions(false, List.of()),
                        new Relationship(RelationshipType.NONE, "", null, false, false, false),
                        new Validation(List.of()),
                        new SearchOptions(false, List.of(), 0),
                        new Security(false, null, null));
        DtoGenerator gen = new DtoGenerator();

        AnnotationSpec enumSchema =
                (AnnotationSpec)
                        invokePrivate(
                                gen,
                                "schemaFromDescriptor",
                                new Class<?>[] {FieldDescriptor.class, TypeName.class},
                                enumField,
                                ClassName.get(String.class));
        AnnotationSpec primitiveSchema =
                (AnnotationSpec)
                        invokePrivate(
                                gen,
                                "schemaFromDescriptor",
                                new Class<?>[] {FieldDescriptor.class, TypeName.class},
                                primitiveField,
                                TypeName.INT);
        AnnotationSpec additionalEnum =
                (AnnotationSpec)
                        invokePrivate(
                                gen,
                                "additionalSchemaFromDescriptor",
                                new Class<?>[] {FieldDescriptor.class, TypeName.class},
                                enumField,
                                ClassName.get(String.class));
        Object additionalBlank =
                invokePrivate(
                        gen,
                        "additionalSchemaFromDescriptor",
                        new Class<?>[] {FieldDescriptor.class, TypeName.class},
                        blankField,
                        ClassName.get(String.class));
        AnnotationSpec additionalPrimitive =
                (AnnotationSpec)
                        invokePrivate(
                                gen,
                                "additionalSchemaFromDescriptor",
                                new Class<?>[] {FieldDescriptor.class, TypeName.class},
                                primitiveField,
                                TypeName.INT);

        assertTrue(enumSchema.toString().contains("allowableValues"));
        assertTrue(enumSchema.toString().contains("nullable = true"));
        assertFalse(primitiveSchema.toString().contains("nullable = true"));
        assertTrue(additionalEnum.toString().contains("allowableValues"));
        assertFalse(additionalPrimitive.toString().contains("nullable = true"));
        assertEquals(null, additionalBlank);
    }

    @Test
    void privateHelpersCoverRoleAndCollectionBranches() throws Exception {
        DtoGenerator gen = new DtoGenerator();

        Object emptyRoles =
                invokePrivate(
                        gen, "rolesCode", new Class<?>[] {String[].class}, (Object) new String[0]);
        Object multipleRoles =
                invokePrivate(
                        gen,
                        "rolesCode",
                        new Class<?>[] {String[].class},
                        (Object) new String[] {"USER", "ADMIN"});
        Object emptyRoleArray =
                invokePrivate(
                        gen, "rolesArray", new Class<?>[] {String[].class}, (Object) new String[0]);
        Object multipleRoleArray =
                invokePrivate(
                        gen,
                        "rolesArray",
                        new Class<?>[] {String[].class},
                        (Object) new String[] {"USER", "ADMIN"});
        TypeName primitiveCast =
                (TypeName)
                        invokePrivate(
                                gen,
                                "metadataCastType",
                                new Class<?>[] {TypeName.class},
                                TypeName.INT);
        TypeName objectCast =
                (TypeName)
                        invokePrivate(
                                gen,
                                "metadataCastType",
                                new Class<?>[] {TypeName.class},
                                ClassName.get(String.class));
        boolean listType =
                (boolean)
                        invokePrivate(
                                gen,
                                "isList",
                                new Class<?>[] {TypeName.class},
                                ParameterizedTypeName.get(
                                        ClassName.get(List.class), ClassName.get(String.class)));
        boolean nonListType =
                (boolean)
                        invokePrivate(
                                gen,
                                "isList",
                                new Class<?>[] {TypeName.class},
                                ClassName.get(String.class));
        boolean setType =
                (boolean)
                        invokePrivate(
                                gen,
                                "isSet",
                                new Class<?>[] {TypeName.class},
                                ParameterizedTypeName.get(
                                        ClassName.get(java.util.Set.class),
                                        ClassName.get(String.class)));
        boolean nonSetType =
                (boolean)
                        invokePrivate(
                                gen,
                                "isSet",
                                new Class<?>[] {TypeName.class},
                                ClassName.get(String.class));

        assertEquals("", emptyRoles.toString());
        assertEquals("\"USER\", \"ADMIN\"", multipleRoles.toString());
        assertEquals("{}", emptyRoleArray.toString());
        assertEquals("{\"USER\", \"ADMIN\"}", multipleRoleArray.toString());
        assertEquals(ClassName.get(Integer.class), primitiveCast);
        assertEquals(ClassName.get(String.class), objectCast);
        assertTrue(listType);
        assertFalse(nonListType);
        assertTrue(setType);
        assertFalse(nonSetType);
    }

    private static Object invokePrivate(
            DtoGenerator gen, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method m = DtoGenerator.class.getDeclaredMethod(methodName, parameterTypes);
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
}

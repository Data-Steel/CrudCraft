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

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ProjectionMetadataProcessorTest {

    @Test
    void skipsNonDtoClasses() {
        JavaFileObject notDto =
                JavaFileObjects.forSourceLines(
                        "com.example.Model", "package com.example;", "public class Model {}");
        JavaFileObject projectionMetadata =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.SomeProjectionMetadata",
                        "package com.example.dto;",
                        "public class SomeProjectionMetadata {}");
        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(notDto, projectionMetadata);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        assertTrue(compilation.generatedSourceFiles().isEmpty());
    }

    @Test
    void processSkipsNonClassRootsAndReturnsFalse() {
        ProjectionMetadataProcessor proc = new ProjectionMetadataProcessor();
        ProcessingEnvironment env = processingEnv(mock(Filer.class), mock(Messager.class), null);
        proc.init(env);
        Element field = mock(Element.class);
        when(field.getKind()).thenReturn(ElementKind.FIELD);
        RoundEnvironment round = mock(RoundEnvironment.class);
        Set<? extends Element> roots = Set.of(field);
        doReturn(roots).when(round).getRootElements();
        when(round.processingOver()).thenReturn(false);

        assertFalse(proc.process(Set.of(), round));
    }

    @Test
    void generatesMetadataForDto() throws Exception {
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "import nl.datasteel.crudcraft.annotations.fields.ProjectionField;",
                        "import java.util.List;",
                        "public class UserDto {",
                        "  @ProjectionField(\"user.email\")",
                        "  String email;",
                        "  List<String> tags;",
                        "  public void setTags(List<String> tags){this.tags=tags;}",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(dto);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        JavaFileObject generated =
                compilation
                        .generatedSourceFile("com.example.dto.UserDtoProjectionMetadata")
                        .orElseThrow();
        String content = generated.getCharContent(false).toString();
        assertTrue(content.contains("class UserDtoProjectionMetadata"));
        assertTrue(content.contains("\"user.email\""));
        assertTrue(content.contains("\"email\""));
        assertTrue(content.contains("\"tags\""));
    }

    @Test
    void warnsWhenProjectionFieldPathCannotBeResolvedAgainstEntity() {
        JavaFileObject entity =
                JavaFileObjects.forSourceLines(
                        "com.example.User",
                        "package com.example;",
                        "public class User {",
                        "  Address address;",
                        "}",
                        "class Address { String city; }");
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "import nl.datasteel.crudcraft.annotations.fields.ProjectionField;",
                        "public class UserDto {",
                        "  @ProjectionField(\"address.zip\")",
                        "  String zip;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(entity, dto);

        assertEquals(Compilation.Status.FAILURE, compilation.status());
        assertProjectionFieldError(compilation, "address.zip");
    }

    @Test
    void validatesProjectionPathForGeneratedCrudCraftDtos() {
        JavaFileObject entity =
                JavaFileObjects.forSourceLines(
                        "com.example.User",
                        "package com.example;",
                        "public class User {",
                        "  String username;",
                        "}");
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserRequestDto",
                        "package com.example.dto;",
                        "import nl.datasteel.crudcraft.annotations.fields.ProjectionField;",
                        "/**",
                        " * Generated model file for User; do not edit manually.",
                        " * @CrudCraft:generated",
                        " */",
                        "public record UserRequestDto(",
                        "  @ProjectionField(\"user.missing\") String username) {}");

        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(entity, dto);

        assertEquals(Compilation.Status.FAILURE, compilation.status());
        assertProjectionFieldError(compilation, "user.missing");
    }

    @Test
    void generatedCrudCraftDtosAcceptProjectionAliasPrefix() {
        JavaFileObject entity =
                JavaFileObjects.forSourceLines(
                        "com.example.User",
                        "package com.example;",
                        "public class User {",
                        "  String username;",
                        "}");
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserRequestDto",
                        "package com.example.dto;",
                        "import nl.datasteel.crudcraft.annotations.fields.ProjectionField;",
                        "/**",
                        " * Generated model file for User; do not edit manually.",
                        " * @CrudCraft:generated",
                        " */",
                        "public record UserRequestDto(",
                        "  @ProjectionField(\"user.username\") String username) {}");

        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(entity, dto);

        assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

    @Test
    void acceptsProjectionFieldPathResolvedThroughEntityFields() {
        JavaFileObject entity =
                JavaFileObjects.forSourceLines(
                        "com.example.User",
                        "package com.example;",
                        "public class User {",
                        "  Address address;",
                        "}",
                        "class Address { String city; }");
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "import nl.datasteel.crudcraft.annotations.fields.ProjectionField;",
                        "public class UserDto {",
                        "  @ProjectionField(\"address.city\")",
                        "  String city;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(entity, dto);

        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        assertFalse(
                compilation.warnings().stream()
                        .anyMatch(
                                warning ->
                                        warning.getMessage(null).contains("@ProjectionField")),
                compilation.warnings().toString());
    }

    @Test
    void warnsWhenProjectionFieldPathContainsBlankSegment() {
        JavaFileObject entity =
                JavaFileObjects.forSourceLines(
                        "com.example.User",
                        "package com.example;",
                        "public class User {",
                        "  Address address;",
                        "}",
                        "class Address { String city; }");
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "import nl.datasteel.crudcraft.annotations.fields.ProjectionField;",
                        "public class UserDto {",
                        "  @ProjectionField(\"address..city\")",
                        "  String city;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(entity, dto);

        assertEquals(Compilation.Status.FAILURE, compilation.status());
        assertProjectionFieldError(compilation, "address..city");
    }

    @Test
    void warnsWhenProjectionFieldPathContinuesThroughPrimitiveType() {
        JavaFileObject entity =
                JavaFileObjects.forSourceLines(
                        "com.example.User",
                        "package com.example;",
                        "public class User { int age; }");
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "import nl.datasteel.crudcraft.annotations.fields.ProjectionField;",
                        "public class UserDto {",
                        "  @ProjectionField(\"age.value\")",
                        "  String value;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(entity, dto);

        assertEquals(Compilation.Status.FAILURE, compilation.status());
        assertProjectionFieldError(compilation, "age.value");
    }

    @Test
    void acceptsProjectionFieldPathThroughCollectionElementType() {
        JavaFileObject entity =
                JavaFileObjects.forSourceLines(
                        "com.example.User",
                        "package com.example;",
                        "import java.util.List;",
                        "public class User { List<Address> addresses; }",
                        "class Address { String city; }");
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "import nl.datasteel.crudcraft.annotations.fields.ProjectionField;",
                        "public class UserDto {",
                        "  @ProjectionField(\"addresses.city\")",
                        "  String city;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(entity, dto);

        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        assertNoProjectionFieldWarning(compilation);
    }

    @Test
    void acceptsProjectionFieldPathResolvedThroughJavaBeanGetters() {
        JavaFileObject entity =
                JavaFileObjects.forSourceLines(
                        "com.example.User",
                        "package com.example;",
                        "public class User {",
                        "  public Address getAddress() { return null; }",
                        "  public boolean isActive() { return true; }",
                        "}",
                        "class Address { String city; }");
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "import nl.datasteel.crudcraft.annotations.fields.ProjectionField;",
                        "public class UserDto {",
                        "  @ProjectionField(\"address.city\")",
                        "  String city;",
                        "  @ProjectionField(\"active\")",
                        "  boolean active;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(entity, dto);

        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        assertNoProjectionFieldWarning(compilation);
    }

    @Test
    void ignoresGetterMethodsWithParametersWhenResolvingProjectionFieldPath() {
        JavaFileObject entity =
                JavaFileObjects.forSourceLines(
                        "com.example.User",
                        "package com.example;",
                        "public class User {",
                        "  public Address getAddress(String locale) { return null; }",
                        "}",
                        "class Address { String city; }");
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "import nl.datasteel.crudcraft.annotations.fields.ProjectionField;",
                        "public class UserDto {",
                        "  @ProjectionField(\"address.city\")",
                        "  String city;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(entity, dto);

        assertEquals(Compilation.Status.FAILURE, compilation.status());
        assertProjectionFieldError(compilation, "address.city");
    }

    @Test
    void acceptsProjectionFieldPathResolvedThroughRecordComponents() {
        JavaFileObject entity =
                JavaFileObjects.forSourceLines(
                        "com.example.User",
                        "package com.example;",
                        "public record User(Address address) {}",
                        "record Address(String city) {}");
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "import nl.datasteel.crudcraft.annotations.fields.ProjectionField;",
                        "public class UserDto {",
                        "  @ProjectionField(\"address.city\")",
                        "  String city;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(entity, dto);

        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        assertNoProjectionFieldWarning(compilation);
    }

    @Test
    void resolvesAdditionalResponseDtoEntityByLongestEntityPrefix() {
        JavaFileObject entity =
                JavaFileObjects.forSourceLines(
                        "com.example.Book",
                        "package com.example;",
                        "public class Book { String title; }");
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.BookSummaryResponseDto",
                        "package com.example.dto;",
                        "import nl.datasteel.crudcraft.annotations.fields.ProjectionField;",
                        "public class BookSummaryResponseDto {",
                        "  @ProjectionField(\"title\")",
                        "  String title;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(entity, dto);

        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        assertNoProjectionFieldWarning(compilation);
    }

    @Test
    void generatesRegistryForDtos() throws IOException {
        JavaFileObject dto1 =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "import java.util.List;",
                        "public class UserDto {",
                        "  List<String> tags;",
                        "  public void setTags(List<String> tags){this.tags=tags;}",
                        "}");
        JavaFileObject dto2 =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.RoleDto",
                        "package com.example.dto;",
                        "public class RoleDto {",
                        "  String name;",
                        "}");
        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(dto1, dto2);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        JavaFileObject registry =
                compilation
                        .generatedSourceFile(
                                "com.example.projection.GeneratedProjectionMetadataRegistry")
                        .orElseThrow();
        String content = registry.getCharContent(false).toString();
        assertTrue(content.contains("@Component"));
        assertTrue(content.contains("UserDtoProjectionMetadata"));
        assertTrue(content.contains("RoleDtoProjectionMetadata"));
    }

    @Test
    void generatesNestedDtoRegistryAtApplicationRoot() {
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.extra.UserDto",
                        "package com.example.dto.extra;",
                        "public class UserDto { String name; }");

        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(dto);

        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        assertTrue(
                compilation
                        .generatedSourceFile(
                                "com.example.projection.GeneratedProjectionMetadataRegistry")
                        .isPresent());
    }

    @Test
    void generatesMetadataForNestedDto() throws IOException {
        JavaFileObject address =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.AddressDto",
                        "package com.example.dto;",
                        "public class AddressDto { String city; }");
        JavaFileObject user =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "public class UserDto {",
                        "  AddressDto address;",
                        "}");
        Compilation compilation =
                CompilationTestUtils.javac()
                        .withProcessors(new ProjectionMetadataProcessor())
                        .compile(address, user);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        String content =
                compilation
                        .generatedSourceFile("com.example.dto.UserDtoProjectionMetadata")
                        .orElseThrow()
                        .getCharContent(false)
                        .toString();
        assertTrue(content.contains("AddressDtoProjectionMetadata"));
    }

    @Test
    void collectionFieldWithoutSetterFailsCompilation() {
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "import java.util.List;",
                        "public class UserDto {",
                        "  List<String> tags;",
                        "}");
        Compilation compilation =
                CompilationTestUtils.javac().withProcessors(new ProjectionMetadataProcessor()).compile(dto);
        assertEquals(Compilation.Status.FAILURE, compilation.status());
    }

    @Test
    void usesHashSetForSetCollections() throws IOException {
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "import java.util.Set;",
                        "public class UserDto {",
                        "  Set<String> tags;",
                        "  public void setTags(Set<String> tags){this.tags=tags;}",
                        "}");
        Compilation compilation =
                CompilationTestUtils.javac().withProcessors(new ProjectionMetadataProcessor()).compile(dto);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        String content =
                compilation
                        .generatedSourceFile("com.example.dto.UserDtoProjectionMetadata")
                        .orElseThrow()
                        .getCharContent(false)
                        .toString();
        assertTrue(content.contains("new HashSet"));
    }

    @Test
    void ignoresExistingProjectionMetadataClasses() {
        JavaFileObject pm =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserProjectionMetadata",
                        "package com.example.dto;",
                        "public class UserProjectionMetadata {}");
        Compilation compilation =
                CompilationTestUtils.javac().withProcessors(new ProjectionMetadataProcessor()).compile(pm);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        assertTrue(compilation.generatedSourceFiles().isEmpty());
    }

    @Test
    void metadataWriteFailureIsReported() throws IOException {
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.UserDto",
                        "package com.example.dto;",
                        "public class UserDto {}");
        Elements elements = CompilationTestUtils.elements(dto);
        Filer filer = failingFiler();
        Messager messager = mock(Messager.class);
        ProjectionMetadataProcessor proc = new ProjectionMetadataProcessor();
        proc.init(processingEnv(filer, messager, elements));
        RoundEnvironment round = mock(RoundEnvironment.class);
        Set<? extends Element> roots = Set.of(elements.getTypeElement("com.example.dto.UserDto"));
        doReturn(roots).when(round).getRootElements();
        when(round.processingOver()).thenReturn(false);

        proc.process(Set.of(), round);

        verify(messager)
                .printMessage(
                        eq(Diagnostic.Kind.ERROR),
                        contains("Failed to generate projection metadata"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void registryWriteFailureIsReported() throws Exception {
        Filer filer = failingFiler();
        Messager messager = mock(Messager.class);
        ProjectionMetadataProcessor proc = new ProjectionMetadataProcessor();
        proc.init(processingEnv(filer, messager, null));
        Field generatedMetadata =
                ProjectionMetadataProcessor.class.getDeclaredField("generatedMetadata");
        generatedMetadata.setAccessible(true);
        ((List<Object>) generatedMetadata.get(proc))
                .add(com.palantir.javapoet.ClassName.get("com.example.dto", "UserMetadata"));
        RoundEnvironment round = mock(RoundEnvironment.class);
        when(round.getRootElements()).thenReturn(Set.of());
        when(round.processingOver()).thenReturn(true);

        proc.process(Set.of(), round);

        verify(messager)
                .printMessage(
                        eq(Diagnostic.Kind.ERROR),
                        contains("Failed to generate ProjectionMetadata registry"));
    }

    @Test
    void metadataGenerationSkipsStaticFieldsAndHandlesRawCollectionsInNestedDtoPackage()
            throws IOException {
        JavaFileObject dto =
                JavaFileObjects.forSourceLines(
                        "com.example.dto.extra.RawDto",
                        "package com.example.dto.extra;",
                        "import java.util.List;",
                        "public class RawDto {",
                        "  static String ignored;",
                        "  int count;",
                        "  List rawValues;",
                        "  public void setRawValues(List rawValues){this.rawValues=rawValues;}",
                        "}");
        Compilation compilation =
                CompilationTestUtils.javac().withProcessors(new ProjectionMetadataProcessor()).compile(dto);

        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        String content =
                compilation
                        .generatedSourceFile("com.example.dto.extra.RawDtoProjectionMetadata")
                        .orElseThrow()
                        .getCharContent(false)
                        .toString();
        assertTrue(content.contains("\"count\""));
        assertTrue(content.contains("\"rawValues\""));
        assertFalse(content.contains("\"ignored\""));
    }

    @Test
    void capitalizeWorksAndThrowsOnEmpty() throws Exception {
        ProjectionMetadataProcessor proc = new ProjectionMetadataProcessor();
        Method m = ProjectionMetadataProcessor.class.getDeclaredMethod("capitalize", String.class);
        m.setAccessible(true);
        assertEquals("Test", m.invoke(proc, "test"));
        Exception ex = assertThrows(Exception.class, () -> m.invoke(proc, ""));
        assertTrue(ex.getCause() instanceof StringIndexOutOfBoundsException);
    }

    @Test
    void applicationRootPackageHandlesNestedDtoLeafAndNonDtoPackages() throws Exception {
        ProjectionMetadataProcessor proc = new ProjectionMetadataProcessor();
        Method method =
                ProjectionMetadataProcessor.class.getDeclaredMethod(
                        "applicationRootPackage", String.class);
        method.setAccessible(true);

        assertEquals("", method.invoke(proc, ".dto.generated"));
        assertEquals("com.example", method.invoke(proc, "com.example.dto.extra"));
        assertEquals("com.example", method.invoke(proc, "com.example.dto"));
        assertEquals("com.example.generated", method.invoke(proc, "com.example.generated"));
    }

    @Test
    void registryPackageFallsBackWhenNoMetadataWasGenerated() throws Exception {
        ProjectionMetadataProcessor proc = new ProjectionMetadataProcessor();
        Method method = ProjectionMetadataProcessor.class.getDeclaredMethod("registryPackage");
        method.setAccessible(true);

        assertEquals("nl.datasteel.crudcraft.projection", method.invoke(proc));
    }

    @Test
    void shouldGenerateRegistryRequiresGeneratedMetadata() throws Exception {
        ProjectionMetadataProcessor proc = new ProjectionMetadataProcessor();
        RoundEnvironment round = mock(RoundEnvironment.class);
        when(round.processingOver()).thenReturn(true);
        Method method =
                ProjectionMetadataProcessor.class.getDeclaredMethod(
                        "shouldGenerateRegistry", RoundEnvironment.class, boolean.class);
        method.setAccessible(true);

        assertEquals(false, method.invoke(proc, round, true));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGenerateRegistryRequiresNewMetadataAndRunsAtRoundEnd() throws Exception {
        ProjectionMetadataProcessor proc = new ProjectionMetadataProcessor();
        Field generatedMetadata =
                ProjectionMetadataProcessor.class.getDeclaredField("generatedMetadata");
        generatedMetadata.setAccessible(true);
        ((List<Object>) generatedMetadata.get(proc))
                .add(com.palantir.javapoet.ClassName.get("com.example.dto", "UserMetadata"));
        RoundEnvironment round = mock(RoundEnvironment.class);
        Method method =
                ProjectionMetadataProcessor.class.getDeclaredMethod(
                        "shouldGenerateRegistry", RoundEnvironment.class, boolean.class);
        method.setAccessible(true);

        when(round.processingOver()).thenReturn(false);
        assertEquals(true, method.invoke(proc, round, true));
        assertEquals(false, method.invoke(proc, round, false));

        when(round.processingOver()).thenReturn(true);
        assertEquals(true, method.invoke(proc, round, false));

        Field registryGenerated =
                ProjectionMetadataProcessor.class.getDeclaredField("registryGenerated");
        registryGenerated.setAccessible(true);
        ((java.util.concurrent.atomic.AtomicBoolean) registryGenerated.get(proc)).set(true);
        assertEquals(false, method.invoke(proc, round, true));
    }

    @Test
    void generatedCrudCraftTypeRequiresGeneratedMarkerInDocComment() throws Exception {
        Elements elements = mock(Elements.class);
        ProjectionMetadataProcessor proc =
                processorWithElementsAndTypes(elements, mock(Types.class));
        TypeElement dto = mock(TypeElement.class);
        Method method =
                ProjectionMetadataProcessor.class.getDeclaredMethod(
                        "isGeneratedCrudCraftType", TypeElement.class);
        method.setAccessible(true);

        when(elements.getDocComment(dto)).thenReturn(null);
        assertEquals(false, method.invoke(proc, dto));
        when(elements.getDocComment(dto)).thenReturn("plain documentation");
        assertEquals(false, method.invoke(proc, dto));
        when(elements.getDocComment(dto)).thenReturn("@CrudCraft:generated");
        assertEquals(true, method.invoke(proc, dto));
    }

    @Test
    void stripAliasPrefixOnlyRemovesWellFormedSinglePrefix() throws Exception {
        ProjectionMetadataProcessor proc = new ProjectionMetadataProcessor();
        Method method =
                ProjectionMetadataProcessor.class.getDeclaredMethod(
                        "stripAliasPrefix", String.class);
        method.setAccessible(true);

        assertEquals("username", method.invoke(proc, "user.username"));
        assertEquals("user.", method.invoke(proc, "user."));
        assertEquals(".username", method.invoke(proc, ".username"));
        assertEquals("user..username", method.invoke(proc, "user..username"));
    }

    @Test
    void resolveEntityTypeCoversFallbackAndInvalidCandidates() throws Exception {
        Elements elements = mock(Elements.class);
        ProjectionMetadataProcessor proc =
                processorWithElementsAndTypes(elements, mock(Types.class));
        TypeElement dto = mock(TypeElement.class);
        PackageElement dtoPackage = packageElement("com.example.dto");
        when(elements.getPackageOf(dto)).thenReturn(dtoPackage);
        when(dto.getSimpleName()).thenReturn(name("UserAccountResponseDto"));
        when(elements.getTypeElement("com.example.UserAccount")).thenReturn(null);

        TypeElement firstUser = typeElement("User", ElementKind.CLASS);
        TypeElement secondUser = typeElement("User", ElementKind.CLASS);
        TypeElement userAccount = typeElement("UserAccount", ElementKind.CLASS);
        PackageElement entityPackage = mock(PackageElement.class);
        doReturn(List.<Element>of(firstUser, secondUser, userAccount))
                .when(entityPackage)
                .getEnclosedElements();
        when(elements.getPackageElement("com.example")).thenReturn(entityPackage);

        assertSame(userAccount, resolveEntityType(proc, dto));

        when(dto.getSimpleName()).thenReturn(name("UserRef"));
        assertSame(firstUser, resolveEntityType(proc, dto));

        Element invalidCandidate = mock(Element.class);
        when(invalidCandidate.getKind()).thenReturn(ElementKind.FIELD);
        when(invalidCandidate.getSimpleName()).thenReturn(name("User"));
        doReturn(List.of(invalidCandidate)).when(entityPackage).getEnclosedElements();
        assertNull(resolveEntityType(proc, dto));

        when(elements.getPackageElement("com.example")).thenReturn(null);
        assertNull(resolveEntityType(proc, dto));
    }

    @Test
    void resolveEntityTypeHandlesBoundaryDtoPackage() throws Exception {
        Elements elements = mock(Elements.class);
        ProjectionMetadataProcessor proc =
                processorWithElementsAndTypes(elements, mock(Types.class));
        TypeElement dto = mock(TypeElement.class);
        TypeElement entity = typeElement("User", ElementKind.CLASS);
        PackageElement dtoPackage = packageElement(".dto");
        when(elements.getPackageOf(dto)).thenReturn(dtoPackage);
        when(dto.getSimpleName()).thenReturn(name("UserRef"));
        when(elements.getTypeElement(".User")).thenReturn(entity);

        assertSame(entity, resolveEntityType(proc, dto));
    }

    @Test
    void isCollectionUsesAssignableErasedTypes() throws Exception {
        Elements elements = mock(Elements.class);
        Types types = mock(Types.class);
        ProjectionMetadataProcessor proc = processorWithElementsAndTypes(elements, types);
        TypeMirror fieldType = mock(TypeMirror.class);
        TypeMirror erasedFieldType = mock(TypeMirror.class);
        TypeMirror collectionMirror = mock(TypeMirror.class);
        TypeMirror erasedCollectionType = mock(TypeMirror.class);
        TypeElement collectionElement = mock(TypeElement.class);
        when(fieldType.getKind()).thenReturn(TypeKind.DECLARED);
        when(elements.getTypeElement("java.util.Collection")).thenReturn(collectionElement);
        when(collectionElement.asType()).thenReturn(collectionMirror);
        when(types.erasure(fieldType)).thenReturn(erasedFieldType);
        when(types.erasure(collectionMirror)).thenReturn(erasedCollectionType);
        when(types.isAssignable(erasedFieldType, erasedCollectionType)).thenReturn(false);

        Method method =
                ProjectionMetadataProcessor.class.getDeclaredMethod("isCollection", TypeMirror.class);
        method.setAccessible(true);

        assertEquals(false, method.invoke(proc, fieldType));
    }

    @Test
    void findReadableMemberTypeAcceptsRecordComponents() throws Exception {
        TypeMirror componentType = mock(TypeMirror.class);
        Element component = mock(Element.class);
        when(component.getModifiers()).thenReturn(Set.of());
        when(component.getKind()).thenReturn(ElementKind.RECORD_COMPONENT);
        when(component.getSimpleName()).thenReturn(name("address"));
        when(component.asType()).thenReturn(componentType);
        TypeElement type = mock(TypeElement.class);
        doReturn(List.of(component)).when(type).getEnclosedElements();

        assertSame(
                componentType,
                findReadableMemberType(new ProjectionMetadataProcessor(), type, "address"));
    }

    @Test
    void findReadableMemberTypeAcceptsBooleanGetters() throws Exception {
        TypeMirror returnType = mock(TypeMirror.class);
        ExecutableElement getter = mock(ExecutableElement.class);
        when(getter.getModifiers()).thenReturn(Set.of());
        when(getter.getKind()).thenReturn(ElementKind.METHOD);
        when(getter.getSimpleName()).thenReturn(name("isActive"));
        when(getter.getParameters()).thenReturn(List.of());
        when(getter.getReturnType()).thenReturn(returnType);
        TypeElement type = mock(TypeElement.class);
        doReturn(List.of(getter)).when(type).getEnclosedElements();

        assertSame(
                returnType,
                findReadableMemberType(new ProjectionMetadataProcessor(), type, "active"));
    }

    @Test
    void findReadableMemberTypeWalksNonObjectSupertypes() throws Exception {
        Types types = mock(Types.class);
        ProjectionMetadataProcessor proc = processorWithElementsAndTypes(mock(Elements.class), types);
        TypeMirror childMirror = mock(TypeMirror.class);
        TypeMirror baseMirror = mock(TypeMirror.class);
        TypeMirror inheritedType = mock(TypeMirror.class);
        TypeElement child = mock(TypeElement.class);
        TypeElement base = mock(TypeElement.class);
        Element inherited = mock(Element.class);
        when(child.asType()).thenReturn(childMirror);
        doReturn(List.of()).when(child).getEnclosedElements();
        doReturn(List.of(baseMirror)).when(types).directSupertypes(childMirror);
        when(types.asElement(baseMirror)).thenReturn(base);
        when(base.getQualifiedName()).thenReturn(name("com.example.Base"));
        doReturn(List.of(inherited)).when(base).getEnclosedElements();
        when(inherited.getModifiers()).thenReturn(Set.of());
        when(inherited.getKind()).thenReturn(ElementKind.FIELD);
        when(inherited.getSimpleName()).thenReturn(name("inherited"));
        when(inherited.asType()).thenReturn(inheritedType);

        assertSame(inheritedType, findReadableMemberType(proc, child, "inherited"));
    }

    @Test
    void findReadableMemberTypeStopsAtObjectSupertype() throws Exception {
        Types types = mock(Types.class);
        ProjectionMetadataProcessor proc = processorWithElementsAndTypes(mock(Elements.class), types);
        TypeMirror childMirror = mock(TypeMirror.class);
        TypeMirror objectMirror = mock(TypeMirror.class);
        TypeElement child = mock(TypeElement.class);
        TypeElement object = mock(TypeElement.class);
        when(child.asType()).thenReturn(childMirror);
        doReturn(List.of()).when(child).getEnclosedElements();
        doReturn(List.of(objectMirror)).when(types).directSupertypes(childMirror);
        when(types.asElement(objectMirror)).thenReturn(object);
        when(object.getQualifiedName()).thenReturn(name("java.lang.Object"));

        assertNull(findReadableMemberType(proc, child, "missing"));
    }

    private static Filer failingFiler() throws IOException {
        Filer filer = mock(Filer.class);
        when(filer.createSourceFile(any(CharSequence.class), any(Element[].class)))
                .thenThrow(new IOException("boom"));
        return filer;
    }

    private static void assertProjectionFieldError(Compilation compilation, String path) {
        assertTrue(
                compilation.errors().stream()
                        .anyMatch(
                                error ->
                                        error.getMessage(null)
                                                .contains("@ProjectionField path '" + path + "'")),
                compilation.errors().toString());
    }

    private static void assertNoProjectionFieldWarning(Compilation compilation) {
        assertFalse(
                compilation.warnings().stream()
                        .anyMatch(
                                warning -> warning.getMessage(null).contains("@ProjectionField")),
                compilation.warnings().toString());
    }

    private static ProcessingEnvironment processingEnv(
            Filer filer, Messager messager, Elements elements) {
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        when(env.getFiler()).thenReturn(filer);
        when(env.getMessager()).thenReturn(messager);
        when(env.getElementUtils()).thenReturn(elements);
        when(env.getTypeUtils()).thenReturn(mock(Types.class));
        when(env.getOptions()).thenReturn(Map.of());
        when(env.getSourceVersion()).thenReturn(SourceVersion.latest());
        when(env.getLocale()).thenReturn(Locale.getDefault());
        return env;
    }

    private static ProjectionMetadataProcessor processorWithElementsAndTypes(
            Elements elements, Types types) {
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        when(env.getFiler()).thenReturn(mock(Filer.class));
        when(env.getMessager()).thenReturn(mock(Messager.class));
        when(env.getElementUtils()).thenReturn(elements);
        when(env.getTypeUtils()).thenReturn(types);
        when(env.getOptions()).thenReturn(Map.of());
        when(env.getSourceVersion()).thenReturn(SourceVersion.latest());
        when(env.getLocale()).thenReturn(Locale.getDefault());
        ProjectionMetadataProcessor proc = new ProjectionMetadataProcessor();
        proc.init(env);
        return proc;
    }

    private static TypeElement resolveEntityType(
            ProjectionMetadataProcessor proc, TypeElement dto) throws Exception {
        Method method =
                ProjectionMetadataProcessor.class.getDeclaredMethod(
                        "resolveEntityType", TypeElement.class);
        method.setAccessible(true);
        return (TypeElement) method.invoke(proc, dto);
    }

    private static TypeMirror findReadableMemberType(
            ProjectionMetadataProcessor proc, TypeElement type, String name) throws Exception {
        Method method =
                ProjectionMetadataProcessor.class.getDeclaredMethod(
                        "findReadableMemberType", TypeElement.class, String.class);
        method.setAccessible(true);
        return (TypeMirror) method.invoke(proc, type, name);
    }

    private static Name name(String value) {
        return new TestName(value);
    }

    private static PackageElement packageElement(String name) {
        PackageElement element = mock(PackageElement.class);
        when(element.getQualifiedName()).thenReturn(name(name));
        return element;
    }

    private static TypeElement typeElement(String name, ElementKind kind) {
        TypeElement element = mock(TypeElement.class);
        when(element.getKind()).thenReturn(kind);
        when(element.getSimpleName()).thenReturn(name(name));
        return element;
    }

    private record TestName(String value) implements Name {

        @Override
        public boolean contentEquals(CharSequence cs) {
            return value.contentEquals(cs);
        }

        @Override
        public int length() {
            return value.length();
        }

        @Override
        public char charAt(int index) {
            return value.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return value.subSequence(start, end);
        }

        @Override
        public String toString() {
            return value;
        }
    }
}

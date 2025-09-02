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

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.tools.JavaFileObject;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ProjectionMetadataProcessorTest {

    @Test
    void skipsNonDtoClasses() {
        JavaFileObject notDto = JavaFileObjects.forSourceLines(
                "com.example.Model",
                "package com.example;",
                "public class Model {}"
        );
        JavaFileObject projectionMetadata = JavaFileObjects.forSourceLines(
                "com.example.dto.SomeProjectionMetadata",
                "package com.example.dto;",
                "public class SomeProjectionMetadata {}"
        );
        Compilation compilation = Compiler.javac()
                .withProcessors(new ProjectionMetadataProcessor())
                .compile(notDto, projectionMetadata);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        assertTrue(compilation.generatedSourceFiles().isEmpty());
    }

    @Test
    void generatesMetadataForDto() throws Exception {
        JavaFileObject dto = JavaFileObjects.forSourceLines(
                "com.example.dto.UserDto",
                "package com.example.dto;",
                "import nl.datasteel.crudcraft.annotations.fields.ProjectionField;",
                "import java.util.List;",
                "public class UserDto {",
                "  @ProjectionField(\"user.email\")",
                "  String email;",
                "  List<String> tags;",
                "  public void setTags(List<String> tags){this.tags=tags;}",
                "}"
        );

        Compilation compilation = Compiler.javac()
                .withProcessors(new ProjectionMetadataProcessor())
                .compile(dto);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        JavaFileObject generated = compilation.generatedSourceFile(
                "com.example.dto.UserDtoProjectionMetadata").orElseThrow();
        String content = generated.getCharContent(false).toString();
        assertTrue(content.contains("class UserDtoProjectionMetadata"));
        assertTrue(content.contains("\"user.email\""));
        assertTrue(content.contains("\"tags\""));
    }

    @Test
    void generatesRegistryForDtos() throws IOException {
        JavaFileObject dto1 = JavaFileObjects.forSourceLines(
                "com.example.dto.UserDto",
                "package com.example.dto;",
                "import java.util.List;",
                "public class UserDto {",
                "  List<String> tags;",
                "  public void setTags(List<String> tags){this.tags=tags;}",
                "}"
        );
        JavaFileObject dto2 = JavaFileObjects.forSourceLines(
                "com.example.dto.RoleDto",
                "package com.example.dto;",
                "public class RoleDto {",
                "  String name;",
                "}"
        );
        Compilation compilation = Compiler.javac()
                .withProcessors(new ProjectionMetadataProcessor())
                .compile(dto1, dto2);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        JavaFileObject registry = compilation.generatedSourceFile(
                "nl.datasteel.crudcraft.projection.mapping.GeneratedProjectionMetadataRegistry")
                .orElseThrow();
        String content = registry.getCharContent(false).toString();
        assertTrue(content.contains("UserDtoProjectionMetadata"));
        assertTrue(content.contains("RoleDtoProjectionMetadata"));
    }

    @Test
    void generatesMetadataForNestedDto() throws IOException {
        JavaFileObject address = JavaFileObjects.forSourceLines(
                "com.example.dto.AddressDto",
                "package com.example.dto;",
                "public class AddressDto { String city; }"
        );
        JavaFileObject user = JavaFileObjects.forSourceLines(
                "com.example.dto.UserDto",
                "package com.example.dto;",
                "public class UserDto {",
                "  AddressDto address;",
                "}"
        );
        Compilation compilation = Compiler.javac()
                .withProcessors(new ProjectionMetadataProcessor())
                .compile(address, user);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        String content = compilation.generatedSourceFile(
                "com.example.dto.UserDtoProjectionMetadata").orElseThrow()
                .getCharContent(false).toString();
        assertTrue(content.contains("AddressDtoProjectionMetadata"));
    }

    @Test
    void collectionFieldWithoutSetterFailsCompilation() {
        JavaFileObject dto = JavaFileObjects.forSourceLines(
                "com.example.dto.UserDto",
                "package com.example.dto;",
                "import java.util.List;",
                "public class UserDto {",
                "  List<String> tags;",
                "}"
        );
        Compilation compilation = Compiler.javac()
                .withProcessors(new ProjectionMetadataProcessor())
                .compile(dto);
        assertEquals(Compilation.Status.FAILURE, compilation.status());
    }

    @Test
    void usesHashSetForSetCollections() throws IOException {
        JavaFileObject dto = JavaFileObjects.forSourceLines(
                "com.example.dto.UserDto",
                "package com.example.dto;",
                "import java.util.Set;",
                "public class UserDto {",
                "  Set<String> tags;",
                "  public void setTags(Set<String> tags){this.tags=tags;}",
                "}"
        );
        Compilation compilation = Compiler.javac()
                .withProcessors(new ProjectionMetadataProcessor())
                .compile(dto);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        String content = compilation.generatedSourceFile(
                "com.example.dto.UserDtoProjectionMetadata").orElseThrow()
                .getCharContent(false).toString();
        assertTrue(content.contains("new HashSet"));
    }

    @Test
    void ignoresExistingProjectionMetadataClasses() {
        JavaFileObject pm = JavaFileObjects.forSourceLines(
                "com.example.dto.UserProjectionMetadata",
                "package com.example.dto;",
                "public class UserProjectionMetadata {}"
        );
        Compilation compilation = Compiler.javac()
                .withProcessors(new ProjectionMetadataProcessor())
                .compile(pm);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        assertTrue(compilation.generatedSourceFiles().isEmpty());
    }

    @Test
    void capitalizeWorksAndThrowsOnEmpty() throws Exception {
        ProjectionMetadataProcessor proc = new ProjectionMetadataProcessor();
        Method m = ProjectionMetadataProcessor.class
                .getDeclaredMethod("capitalize", String.class);
        m.setAccessible(true);
        assertEquals("Test", m.invoke(proc, "test"));
        Exception ex = assertThrows(Exception.class, () -> m.invoke(proc, ""));
        assertTrue(ex.getCause() instanceof StringIndexOutOfBoundsException);
    }
}


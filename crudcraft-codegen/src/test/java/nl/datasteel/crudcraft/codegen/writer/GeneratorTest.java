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

import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import java.util.List;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/** Tests for Generator utility methods. */
class GeneratorTest {

    private WriteContext ctx;
    private Messager messager;

    @BeforeEach
    void setup() {
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);
        ctx = new WriteContext(env);
    }

    @Test
    void invalidWhenModelIsNull() {
        assertFalse(Generator.isValidModelDescriptor(null, ctx));
        verify(messager).printMessage(eq(Diagnostic.Kind.ERROR), contains("null"));
    }

    @Test
    void invalidWhenNameMissing() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn(null);
        when(md.getPackageName()).thenReturn("pkg");
        when(md.getFields()).thenReturn(List.of());
        assertFalse(Generator.isValidModelDescriptor(md, ctx));
        verify(messager).printMessage(eq(Diagnostic.Kind.ERROR), contains("missing name"));
    }

    @Test
    void invalidWhenPackageMissing() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Model");
        when(md.getPackageName()).thenReturn(" ");
        when(md.getFields()).thenReturn(List.of());
        assertFalse(Generator.isValidModelDescriptor(md, ctx));
        verify(messager)
                .printMessage(eq(Diagnostic.Kind.ERROR), contains("missing name or package"));
    }

    @Test
    void invalidWhenNameBlankOrPackageNull() {
        ModelDescriptor blankName = mock(ModelDescriptor.class);
        when(blankName.getName()).thenReturn(" ");
        when(blankName.getPackageName()).thenReturn("pkg");
        when(blankName.getFields()).thenReturn(List.of());
        assertFalse(Generator.isValidModelDescriptor(blankName, ctx));

        ModelDescriptor nullPackage = mock(ModelDescriptor.class);
        when(nullPackage.getName()).thenReturn("Model");
        when(nullPackage.getPackageName()).thenReturn(null);
        when(nullPackage.getFields()).thenReturn(List.of());
        assertFalse(Generator.isValidModelDescriptor(nullPackage, ctx));
    }

    @Test
    void invalidWhenFieldsNull() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Model");
        when(md.getPackageName()).thenReturn("pkg");
        when(md.getFields()).thenReturn(null);
        assertFalse(Generator.isValidModelDescriptor(md, ctx));
        verify(messager).printMessage(eq(Diagnostic.Kind.ERROR), contains("null fields"));
    }

    @Test
    void validDescriptor() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Model");
        when(md.getPackageName()).thenReturn("pkg");
        when(md.getFields()).thenReturn(List.of());
        assertTrue(Generator.isValidModelDescriptor(md, ctx));
    }

    @Test
    void defaultGeneratorDoesNotRequireCrudEntity() {
        Generator generator = (model, writeContext) -> List.of();

        assertFalse(generator.requiresCrudEntity());
    }

    @Test
    void defaultWriteReturnsWhenDescriptorInvalid() {
        Generator generator =
                (model, writeContext) -> {
                    throw new AssertionError(
                            "generate should not be called for invalid descriptors");
                };

        generator.write(null, ctx);

        verify(messager).printMessage(eq(Diagnostic.Kind.ERROR), contains("null"));
    }

    @Test
    void defaultWriteWritesAllGeneratedFiles() {
        WriteContext writeContext = spy(ctx);
        doNothing().when(writeContext).write(any(JavaFile.class));
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Model");
        when(md.getPackageName()).thenReturn("pkg");
        when(md.getFields()).thenReturn(List.of());
        JavaFile first =
                JavaFile.builder(
                                "pkg",
                                TypeSpec.classBuilder("First")
                                        .addModifiers(Modifier.PUBLIC)
                                        .build())
                        .build();
        JavaFile second =
                JavaFile.builder(
                                "pkg",
                                TypeSpec.classBuilder("Second")
                                        .addModifiers(Modifier.PUBLIC)
                                        .build())
                        .build();
        Generator generator = (model, context) -> List.of(first, second);

        generator.write(md, writeContext);

        verify(writeContext).write(first);
        verify(writeContext).write(second);
    }
}

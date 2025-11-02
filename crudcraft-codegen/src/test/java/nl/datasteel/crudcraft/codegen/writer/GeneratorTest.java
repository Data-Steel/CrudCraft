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

import java.util.List;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

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
        verify(messager).printMessage(eq(Diagnostic.Kind.ERROR), contains("missing name or package"));
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
}

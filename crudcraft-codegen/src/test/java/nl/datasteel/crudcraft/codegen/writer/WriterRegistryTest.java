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

import java.lang.reflect.Constructor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class WriterRegistryTest {

    private WriteContext ctx;
    private Messager messager;

    @BeforeEach
    void setup() {
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);
        ctx = new WriteContext(env);
        TestBasicGenerator.writes = 0;
        TestBasicGenerator.written = false;
        TestCrudGenerator.writes = 0;
        TestCrudGenerator.written = false;
        WriterRegistry.clearGeneratorOverridesForTests();
    }

    @AfterEach
    void tearDown() {
        WriterRegistry.clearGeneratorOverridesForTests();
    }

    @Test
    void nullModelLogsWarning() {
        WriterRegistry.writeAll(null, ctx);
        verify(messager).printMessage(eq(Diagnostic.Kind.WARNING), contains("null"));
        assertEquals(0, TestBasicGenerator.writes);
    }

    @Test
    void nonCrudEntityInvokesOnlyBasicGenerators() {
        WriterRegistry.setGeneratorOverridesForTests(
                java.util.List.of(new TestBasicGenerator(), new TestCrudGenerator()));
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.isCrudCraftEntity()).thenReturn(false);

        WriterRegistry.writeAll(md, ctx);

        assertEquals(1, TestBasicGenerator.writes);
        assertEquals(0, TestCrudGenerator.writes);
    }

    @Test
    void crudEntityInvokesBothGeneratorGroups() {
        WriterRegistry.setGeneratorOverridesForTests(
                java.util.List.of(new TestBasicGenerator(), new TestCrudGenerator()));
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.isCrudCraftEntity()).thenReturn(true);

        WriterRegistry.writeAll(md, ctx);

        assertEquals(1, TestBasicGenerator.writes);
        assertEquals(1, TestCrudGenerator.writes);
    }

    @Test
    void logsDiscoveredGeneratorCounts() {
        WriterRegistry.setGeneratorOverridesForTests(
                java.util.List.of(new TestBasicGenerator(), new TestCrudGenerator()));
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.isCrudCraftEntity()).thenReturn(true);

        WriterRegistry.writeAll(md, ctx);

        verify(messager).printMessage(eq(Diagnostic.Kind.NOTE), contains("Found"));
    }

    @Test
    void utilityConstructorIsPrivateButInvokableForCoverage() throws Exception {
        Constructor<WriterRegistry> constructor = WriterRegistry.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }
}

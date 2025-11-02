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

package nl.datasteel.crudcraft.codegen.writer.stubs;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class StubGeneratorTest {

    private ModelDescriptor validDescriptor() {
        ModelIdentity id = new ModelIdentity("Test", "com.example", List.of(), "com.example");
        ModelFlags flags = new ModelFlags(true, true, false, false);
        EndpointOptions ep = new EndpointOptions(nl.datasteel.crudcraft.annotations.CrudTemplate.FULL, new nl.datasteel.crudcraft.annotations.CrudEndpoint[0], new nl.datasteel.crudcraft.annotations.CrudEndpoint[0], nl.datasteel.crudcraft.annotations.CrudTemplate.class);
        ModelSecurity sec = new ModelSecurity(false, null, List.of());
        return new ModelDescriptor(id, flags, ep, sec);
    }

    private StubGenerator stub(java.util.concurrent.atomic.AtomicInteger counter) {
        return new StubGenerator() {
            @Override public JavaFile build(ModelDescriptor md, WriteContext ctx) {
                counter.incrementAndGet();
                return JavaFile.builder("com.example", TypeSpec.classBuilder("Generated").build()).build();
            }
        };
    }

    @Test
    void generateReturnsEmptyListWhenDescriptorInvalid() {
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        var gen = stub(new java.util.concurrent.atomic.AtomicInteger());
        List<JavaFile> files = gen.generate(null, ctx);
        assertTrue(files.isEmpty());
        assertFalse(env.messager.notes.isEmpty());
    }

    @Test
    void generateInvokesBuildForValidDescriptor() {
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
        var gen = stub(counter);
        List<JavaFile> files = gen.generate(validDescriptor(), ctx);
        assertEquals(1, files.size());
        assertEquals(1, counter.get());
    }

    @Test
    void writeWritesGeneratedFiles() {
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        TestUtils.RecordingWriteContext ctx = new TestUtils.RecordingWriteContext(env);
        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
        var gen = stub(counter);
        gen.write(validDescriptor(), ctx);
        assertEquals(1, ctx.files.size());
        assertEquals(1, counter.get());
    }

    @Test
    void writeSkipsWhenDescriptorInvalid() {
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        TestUtils.RecordingWriteContext ctx = new TestUtils.RecordingWriteContext(env);
        var gen = stub(new java.util.concurrent.atomic.AtomicInteger());
        gen.write(null, ctx);
        assertTrue(ctx.files.isEmpty());
    }

    @Test
    void writeHandlesFilerException() {
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(true, false));
        WriteContext ctx = new WriteContext(env);
        var gen = stub(new java.util.concurrent.atomic.AtomicInteger());
        gen.write(validDescriptor(), ctx);
        assertFalse(env.messager.notes.isEmpty());
    }

    @Test
    void writeHandlesIOException() {
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, true));
        WriteContext ctx = new WriteContext(env);
        var gen = stub(new java.util.concurrent.atomic.AtomicInteger());
        gen.write(validDescriptor(), ctx);
        assertFalse(env.messager.errors.isEmpty());
    }
}

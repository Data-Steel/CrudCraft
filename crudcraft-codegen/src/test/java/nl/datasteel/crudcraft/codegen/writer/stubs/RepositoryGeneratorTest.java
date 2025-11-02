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
import java.util.List;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RepositoryGeneratorTest {
    private ModelDescriptor descriptor() {
        ModelIdentity id = new ModelIdentity("Book", "com.example", List.of(), "com.example");
        ModelFlags flags = new ModelFlags(true, true, false, false);
        EndpointOptions ep = new EndpointOptions(nl.datasteel.crudcraft.annotations.CrudTemplate.FULL, new nl.datasteel.crudcraft.annotations.CrudEndpoint[0], new nl.datasteel.crudcraft.annotations.CrudEndpoint[0], nl.datasteel.crudcraft.annotations.CrudTemplate.class);
        ModelSecurity sec = new ModelSecurity(false, null, List.of());
        return new ModelDescriptor(id, flags, ep, sec);
    }

    @Test
    void buildCreatesRepositoryInterface() {
        RepositoryGenerator gen = new RepositoryGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        JavaFile jf = gen.build(descriptor(), ctx);
        String code = jf.toString();
        assertTrue(code.contains("interface BookRepository"));
        assertTrue(code.contains("@Repository"));
        assertEquals("com.example.repository", jf.packageName);
    }

    @Test
    void requiresCrudEntityAndOrder() {
        RepositoryGenerator gen = new RepositoryGenerator();
        assertTrue(gen.requiresCrudEntity());
        assertEquals(1, gen.order());
    }

    @Test
    void writeHandlesFilerException() {
        RepositoryGenerator gen = new RepositoryGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(true, false));
        WriteContext ctx = new WriteContext(env);
        gen.write(descriptor(), ctx);
        assertFalse(env.messager.notes.isEmpty());
    }

    @Test
    void writeHandlesIOException() {
        RepositoryGenerator gen = new RepositoryGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, true));
        WriteContext ctx = new WriteContext(env);
        gen.write(descriptor(), ctx);
        assertFalse(env.messager.errors.isEmpty());
    }
}

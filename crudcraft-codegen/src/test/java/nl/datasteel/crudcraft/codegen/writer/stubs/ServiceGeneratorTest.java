/*
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
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

class ServiceGeneratorTest {
    private ModelDescriptor descriptor(List<String> rowHandlers) {
        ModelIdentity id = new ModelIdentity("User", "com.example", List.of(), "com.example");
        ModelFlags flags = new ModelFlags(true, true, false, false);
        EndpointOptions ep = new EndpointOptions(nl.datasteel.crudcraft.annotations.CrudTemplate.FULL, new nl.datasteel.crudcraft.annotations.CrudEndpoint[0], new nl.datasteel.crudcraft.annotations.CrudEndpoint[0], nl.datasteel.crudcraft.annotations.CrudTemplate.class);
        ModelSecurity sec = new ModelSecurity(false, null, rowHandlers);
        return new ModelDescriptor(id, flags, ep, sec);
    }

    @Test
    void buildWithoutRowSecurityHandlers() {
        ServiceGenerator gen = new ServiceGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        JavaFile jf = gen.build(descriptor(List.of()), ctx);
        String code = jf.toString();
        assertFalse(code.contains("rowSecurityHandlers"));
        assertTrue(code.contains("postSave"));
        assertTrue(code.contains("preDelete"));
        assertTrue(code.contains("@Service"));
        assertTrue(code.contains("extends AbstractCrudService"));
        assertTrue(code.contains("UserRepository repository"));
        assertTrue(code.contains("UserMapper mapper"));
    }

    @Test
    void buildWithRowSecurityHandlersAddsFieldAndConstructorParams() {
        ServiceGenerator gen = new ServiceGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        JavaFile jf = gen.build(descriptor(List.of("com.example.A", "com.example.B")), ctx);
        String code = jf.toString();
        assertTrue(code.contains("rowSecurityHandlers"));
        assertTrue(code.contains("rowSecurity0"));
        assertTrue(code.contains("rowSecurity1"));
        assertTrue(code.contains("rowSecurityHandlers()"));
    }

    @Test
    void requiresCrudEntityAndOrder() {
        ServiceGenerator gen = new ServiceGenerator();
        assertTrue(gen.requiresCrudEntity());
        assertEquals(3, gen.order());
    }

    @Test
    void writeHandlesFilerException() {
        ServiceGenerator gen = new ServiceGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(true, false));
        WriteContext ctx = new WriteContext(env);
        gen.write(descriptor(List.of()), ctx);
        assertFalse(env.messager.notes.isEmpty());
    }

    @Test
    void writeHandlesIOException() {
        ServiceGenerator gen = new ServiceGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, true));
        WriteContext ctx = new WriteContext(env);
        gen.write(descriptor(List.of()), ctx);
        assertFalse(env.messager.errors.isEmpty());
    }
}

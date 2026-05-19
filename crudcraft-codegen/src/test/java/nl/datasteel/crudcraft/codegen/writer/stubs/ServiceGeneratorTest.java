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

import com.palantir.javapoet.JavaFile;
import java.util.List;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.RowScope;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ScopeKind;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ServiceGeneratorTest {
    private ModelDescriptor descriptor(List<String> rowHandlers) {
        ModelIdentity id = new ModelIdentity("User", "com.example", List.of(), "com.example");
        ModelFlags flags = new ModelFlags(true, true, false, false);
        EndpointOptions ep =
                new EndpointOptions(
                        nl.datasteel.crudcraft.annotations.CrudTemplate.FULL,
                        new nl.datasteel.crudcraft.annotations.CrudEndpoint[0],
                        new nl.datasteel.crudcraft.annotations.CrudEndpoint[0],
                        nl.datasteel.crudcraft.annotations.CrudTemplate.class);
        ModelSecurity sec = new ModelSecurity(false, null, rowHandlers);
        return new ModelDescriptor(id, flags, ep, sec);
    }

    private ModelDescriptor scopeDescriptor() {
        ModelIdentity id = new ModelIdentity("User", "com.example", List.of(), "com.example");
        ModelFlags flags = new ModelFlags(true, true, false, false);
        EndpointOptions ep =
                new EndpointOptions(
                        nl.datasteel.crudcraft.annotations.CrudTemplate.FULL,
                        new nl.datasteel.crudcraft.annotations.CrudEndpoint[0],
                        new nl.datasteel.crudcraft.annotations.CrudEndpoint[0],
                        nl.datasteel.crudcraft.annotations.CrudTemplate.class);
        ModelSecurity sec =
                new ModelSecurity(
                        false,
                        null,
                        List.of(),
                        List.of(new RowScope(ScopeKind.TENANT, "tenantId", "tenant_id")),
                        java.util.Map.of());
        return new ModelDescriptor(id, flags, ep, sec);
    }

    @Test
    void buildWithoutRowSecurityHandlers() {
        ServiceGenerator gen = new ServiceGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        JavaFile jf = gen.build(descriptor(List.of()), ctx);
        String code = jf.toString();
        assertFalse(code.contains("rowSecurityHandlerList"));
        assertFalse(code.contains("RowSecurityRuntimeExtension"));
        assertTrue(code.contains("postSave"));
        assertTrue(code.contains("preDelete"));
        assertTrue(code.contains("@Service"));
        assertTrue(code.contains("extends AbstractCrudService"));
        assertTrue(code.contains("UserRepository repository"));
        assertTrue(code.contains("UserMapper mapper"));
        assertTrue(
                env.messager.notes.stream()
                        .anyMatch(note -> note.contains("Generating service for User")));
    }

    @Test
    void buildWithRowSecurityHandlersAddsFieldAndConstructorParams() {
        ServiceGenerator gen = new ServiceGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        JavaFile jf = gen.build(descriptor(List.of("com.example.A", "com.example.B")), ctx);
        String code = jf.toString();
        assertTrue(code.contains("runtimeExtensions"));
        assertTrue(code.contains("rowSecurity0"));
        assertTrue(code.contains("rowSecurity1"));
        assertTrue(code.contains("RowSecurityRuntimeExtension"));
        assertTrue(code.contains("rowSecurityHandlerList.add(rowSecurity0)"));
        assertTrue(code.contains("rowSecurityHandlerList.add(rowSecurity1)"));
        assertTrue(code.contains("runtimeExtensions()"));
    }

    @Test
    void generateReturnsEmptyForInvalidModel() {
        ServiceGenerator gen = new ServiceGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);

        assertTrue(gen.generate(null, ctx).isEmpty());
    }

    @Test
    void generateSkipsAbstractServiceAndLogsReason() {
        ServiceGenerator gen = new ServiceGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        ModelIdentity id = new ModelIdentity("User", "com.example", List.of(), "com.example");
        ModelFlags flags = new ModelFlags(true, true, false, true);
        EndpointOptions ep =
                new EndpointOptions(
                        nl.datasteel.crudcraft.annotations.CrudTemplate.FULL,
                        new nl.datasteel.crudcraft.annotations.CrudEndpoint[0],
                        new nl.datasteel.crudcraft.annotations.CrudEndpoint[0],
                        nl.datasteel.crudcraft.annotations.CrudTemplate.class);
        ModelSecurity sec = new ModelSecurity(false, null, List.of());
        ModelDescriptor descriptor = new ModelDescriptor(id, flags, ep, sec);

        assertTrue(gen.generate(descriptor, ctx).isEmpty());
        assertTrue(
                env.messager.notes.stream()
                        .anyMatch(
                                note ->
                                        note.contains(
                                                "Skipping service generation for abstract entity:"
                                                        + " User")));
    }

    @Test
    void buildWithScopesAddsPrincipalAccessorAndClaimHandler() {
        ServiceGenerator gen = new ServiceGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        JavaFile jf = gen.build(scopeDescriptor(), ctx);
        String code = jf.toString();
        assertTrue(code.contains("PrincipalScopeAccessor principalScopeAccessor"));
        assertTrue(code.contains("ClaimScopedRowSecurityHandler"));
        assertTrue(code.contains("runtimeExtensions()"));
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

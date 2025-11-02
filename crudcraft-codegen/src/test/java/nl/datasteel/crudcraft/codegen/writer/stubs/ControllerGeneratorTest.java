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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ControllerGeneratorTest {

    private ModelDescriptor descriptor(boolean editable,
                                       CrudTemplate template,
                                       CrudEndpoint[] omit,
                                       CrudEndpoint[] include,
                                       Class<? extends CrudEndpointPolicy> policy,
                                       boolean secure,
                                       Class<? extends CrudSecurityPolicy> secPolicy) {
        ModelIdentity id = new ModelIdentity("Order", "com.example", List.of(), "com.example");
        ModelFlags flags = new ModelFlags(editable, true, false, false);
        EndpointOptions ep = new EndpointOptions(template, omit, include, policy);
        ModelSecurity sec = new ModelSecurity(secure, secPolicy, List.of());
        return new ModelDescriptor(id, flags, ep, sec);
    }

    static class OnlyGetOnePolicy implements CrudEndpointPolicy {
        @Override public Set<CrudEndpoint> resolveEndpoints() { return EnumSet.of(CrudEndpoint.GET_ONE); }

        /**
         * Returns the name of the policy.
         * This name is used to identify the policy in generated code.
         *
         * @return the name of the policy.
         */
        @Override
        public String name() {
            return null;
        }
    }

    static class BadPolicy implements CrudEndpointPolicy {
        private BadPolicy() {}
        @Override public Set<CrudEndpoint> resolveEndpoints() { return EnumSet.of(CrudEndpoint.GET_ALL); }

        /**
         * Returns the name of the policy.
         * This name is used to identify the policy in generated code.
         *
         * @return the name of the policy.
         */
        @Override
        public String name() {
            return null;
        }
    }

    static class BadSecurity implements CrudSecurityPolicy {
        private BadSecurity() {}
        @Override public String getSecurityExpression(CrudEndpoint endpoint) { return "permitAll()"; }
    }

    @Test
    void writeEditableAddsCommentForDisabledEndpoint() {
        ControllerGenerator gen = new ControllerGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        ModelDescriptor md = descriptor(true, CrudTemplate.FULL,
                new CrudEndpoint[]{CrudEndpoint.POST}, new CrudEndpoint[0], CrudTemplate.class, false, null);
        gen.write(md, ctx);
        String code = ((TestUtils.RecordingFiler) env.getFiler()).jfo.written;
        assertTrue(code.contains("Endpoint omitted by generation template"));
    }

    @Test
    void writeNonEditableDoesNotAddComment() {
        ControllerGenerator gen = new ControllerGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        ModelDescriptor md = descriptor(false, CrudTemplate.FULL,
                new CrudEndpoint[]{CrudEndpoint.POST}, new CrudEndpoint[0], CrudTemplate.class, false, null);
        gen.write(md, ctx);
        String code = ((TestUtils.RecordingFiler) env.getFiler()).jfo.written;
        assertFalse(code.contains("Endpoint omitted"));
    }

    @Test
    void includesEndpointsFromCustomPolicy() {
        ControllerGenerator gen = new ControllerGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        ModelDescriptor md = descriptor(false, CrudTemplate.FULL,
                new CrudEndpoint[0], new CrudEndpoint[0], OnlyGetOnePolicy.class, false, null);
        JavaFile jf = gen.build(md, ctx);
        String code = jf.toString();
        assertTrue(code.contains("getOne"));
        assertFalse(code.contains("getAll"));
    }

    @Test
    void includeEndpointsAreRespected() {
        ControllerGenerator gen = new ControllerGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        ModelDescriptor md = descriptor(false, CrudTemplate.READ_ONLY,
                new CrudEndpoint[0], new CrudEndpoint[]{CrudEndpoint.POST}, CrudTemplate.class, false, null);
        JavaFile jf = gen.build(md, ctx);
        String code = jf.toString();
        assertTrue(code.contains("post"));
    }

    @Test
    void buildAddsRequestMappingAndClampPageable() {
        ControllerGenerator gen = new ControllerGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        ModelDescriptor md = descriptor(false, CrudTemplate.FULL,
                new CrudEndpoint[0], new CrudEndpoint[0], CrudTemplate.class, false, null);
        JavaFile jf = gen.build(md, ctx);
        String code = jf.toString();
        assertTrue(code.contains("@RequestMapping(\"/orders\")"));
        assertTrue(code.contains("clampPageable"));
    }

    @Test
    void endpointPolicyInstantiationFailureThrows() {
        ControllerGenerator gen = new ControllerGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        ModelDescriptor md = descriptor(false, CrudTemplate.FULL,
                new CrudEndpoint[0], new CrudEndpoint[0], BadPolicy.class, false, null);
        assertThrows(IllegalStateException.class, () -> gen.build(md, ctx));
    }

    @Test
    void securityPolicyInstantiationFailureThrows() {
        ControllerGenerator gen = new ControllerGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, false));
        WriteContext ctx = new WriteContext(env);
        ModelDescriptor md = descriptor(false, CrudTemplate.FULL,
                new CrudEndpoint[0], new CrudEndpoint[0], CrudTemplate.class, true, BadSecurity.class);
        assertThrows(IllegalStateException.class, () -> gen.build(md, ctx));
    }

    @Test
    void filerExceptionIsHandled() {
        ControllerGenerator gen = new ControllerGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(true, false));
        WriteContext ctx = new WriteContext(env);
        ModelDescriptor md = descriptor(false, CrudTemplate.FULL,
                new CrudEndpoint[0], new CrudEndpoint[0], CrudTemplate.class, false, null);
        gen.write(md, ctx);
        assertFalse(env.messager.notes.isEmpty());
    }

    @Test
    void ioExceptionIsReported() {
        ControllerGenerator gen = new ControllerGenerator();
        var env = new TestUtils.ProcessingEnvStub(new TestUtils.RecordingFiler(false, true));
        WriteContext ctx = new WriteContext(env);
        ModelDescriptor md = descriptor(false, CrudTemplate.FULL,
                new CrudEndpoint[0], new CrudEndpoint[0], CrudTemplate.class, false, null);
        gen.write(md, ctx);
        assertFalse(env.messager.errors.isEmpty());
    }

    @Test
    void requiresCrudEntityAndOrder() {
        ControllerGenerator gen = new ControllerGenerator();
        assertTrue(gen.requiresCrudEntity());
        assertEquals(4, gen.order());
    }
}

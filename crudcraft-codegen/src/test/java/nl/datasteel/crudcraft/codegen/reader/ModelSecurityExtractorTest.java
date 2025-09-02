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

package nl.datasteel.crudcraft.codegen.reader;

import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import nl.datasteel.crudcraft.codegen.reader.model.ModelSecurityExtractor;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ModelSecurityExtractorTest {

    public static class Policy implements CrudSecurityPolicy {
        @Override public String getSecurityExpression(CrudEndpoint endpoint) { return ""; }
    }
    public static class Handler implements RowSecurityHandler<Object> {
        @Override public org.springframework.data.jpa.domain.Specification<Object> rowFilter(){ return null; }
    }

    @Test
    void readsSecurityPolicyAndRowHandlers() {
        String src = "package t; import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;" +
                "import nl.datasteel.crudcraft.annotations.security.*;" +
                "@RowSecurity(handlers=nl.datasteel.crudcraft.codegen.reader.ModelSecurityExtractorTest.Handler.class)" +
                "@CrudCrafted(securityPolicy=nl.datasteel.crudcraft.codegen.reader.ModelSecurityExtractorTest.Policy.class, secure=false) class C {}";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        ModelSecurity sec = ModelSecurityExtractor.INSTANCE.extract(te, new TestUtils.ProcessingEnvStub(elements));
        assertFalse(sec.isSecure());
        assertEquals(Policy.class, sec.getSecurityPolicy());
        assertEquals(List.of(Handler.class.getCanonicalName()), sec.getRowSecurityHandlers());
    }

    @Test
    void defaultsWhenAnnotationMissing() {
        String src = "package t; class C {}";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        ModelSecurity sec = ModelSecurityExtractor.INSTANCE.extract(te, new TestUtils.ProcessingEnvStub(elements));
        assertTrue(sec.isSecure());
        assertEquals(nl.datasteel.crudcraft.annotations.security.policy.PermitAllSecurityPolicy.class, sec.getSecurityPolicy());
        assertTrue(sec.getRowSecurityHandlers().isEmpty());
    }
}

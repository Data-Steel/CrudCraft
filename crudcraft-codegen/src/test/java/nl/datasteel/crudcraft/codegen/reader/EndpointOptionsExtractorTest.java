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
package nl.datasteel.crudcraft.codegen.reader;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.reader.model.EndpointOptionsExtractor;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EndpointOptionsExtractorTest {

    @Test
    void extractsTemplateOmitIncludeAndPolicy() {
        String src = "package t; import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;" +
                "import nl.datasteel.crudcraft.annotations.*;" +
                "@CrudCrafted(template=CrudTemplate.FULL, omitEndpoints=CrudEndpoint.DELETE, includeEndpoints=CrudEndpoint.GET_ONE) class C {}";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        EndpointOptions opts = EndpointOptionsExtractor.INSTANCE.extract(te, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(CrudTemplate.FULL, opts.getTemplate());
        assertArrayEquals(new CrudEndpoint[]{CrudEndpoint.DELETE}, opts.getOmitEndpoints());
        assertArrayEquals(new CrudEndpoint[]{CrudEndpoint.GET_ONE}, opts.getIncludeEndpoints());
        assertEquals(CrudTemplate.class, opts.getEndpointPolicy());
    }

    @Test
    void failsWhenPolicyCannotInstantiate() {
        String src = "package t; import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;" +
                "import nl.datasteel.crudcraft.annotations.*;" +
                "class Bad implements CrudEndpointPolicy { private Bad(){} public Set<CrudEndpoint> resolveEndpoints(){return Set.of();}}" +
                "@CrudCrafted(endpointPolicy=Bad.class) class C {}";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        assertThrows(IllegalStateException.class, () ->
                EndpointOptionsExtractor.INSTANCE.extract(te, new TestUtils.ProcessingEnvStub(elements)));
    }

    @Test
    void defaultsWhenAnnotationMissing() {
        String src = "package t; class C {}";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        EndpointOptions opts = EndpointOptionsExtractor.INSTANCE.extract(te, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(CrudTemplate.FULL, opts.getTemplate());
        assertEquals(0, opts.getOmitEndpoints().length);
        assertEquals(0, opts.getIncludeEndpoints().length);
        assertEquals(CrudTemplate.class, opts.getEndpointPolicy());
    }
}

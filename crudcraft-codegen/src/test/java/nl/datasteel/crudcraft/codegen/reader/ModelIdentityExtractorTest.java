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
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelIdentity;
import nl.datasteel.crudcraft.codegen.reader.model.IdentityExtractor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ModelIdentityExtractorTest {

    @Test
    void extractsFieldsIncludingEmbedded() {
        String src = "package t.model;" +
                "import jakarta.persistence.*;" +
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;" +
                "class Emb { String inner; }" +
                "@CrudCrafted class M { String a; @Embedded Emb emb; }";
        Elements elements = CompilationTestUtils.elements("t.model.M", src);
        TypeElement te = elements.getTypeElement("t.model.M");
        ModelIdentity mi = IdentityExtractor.INSTANCE.extract(te, new TestUtils.ProcessingEnvStub(elements));
        assertEquals("M", mi.getName());
        assertEquals("t.model", mi.getPackageName());
        assertEquals("t", mi.getBasePackage());
        assertEquals(2, mi.getFields().size());
    }
}

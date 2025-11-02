/**
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
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelFlags;
import nl.datasteel.crudcraft.codegen.reader.model.FlagsExtractor;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class FlagsExtractorTest {

    @Test
    void extractsFlags() {
        String src = "package t; import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;" +
                "@CrudCrafted(editable=true) class C {}";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        ModelFlags flags = FlagsExtractor.INSTANCE.extract(te, new TestUtils.ProcessingEnvStub(elements));
        assertTrue(flags.isEditable());
        assertTrue(flags.isCrudCraftEntity());
        assertFalse(flags.isEmbeddable());
    }

    @Test
    void detectsEmbeddable() {
        String src = "package t; import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;" +
                "import jakarta.persistence.Embeddable;" +
                "@Embeddable @CrudCrafted class E {}";
        Elements elements = CompilationTestUtils.elements("t.E", src);
        TypeElement te = elements.getTypeElement("t.E");
        ModelFlags flags = FlagsExtractor.INSTANCE.extract(te, new TestUtils.ProcessingEnvStub(elements));
        assertTrue(flags.isEmbeddable());
    }

    @Test
    void defaultsWhenCrudCraftAnnotationMissing() {
        String src = "package t; class P {}";
        Elements elements = CompilationTestUtils.elements("t.P", src);
        TypeElement te = elements.getTypeElement("t.P");
        ModelFlags flags = FlagsExtractor.INSTANCE.extract(te, new TestUtils.ProcessingEnvStub(elements));
        assertFalse(flags.isCrudCraftEntity());
        assertFalse(flags.isEditable());
        assertFalse(flags.isEmbeddable());
    }
}

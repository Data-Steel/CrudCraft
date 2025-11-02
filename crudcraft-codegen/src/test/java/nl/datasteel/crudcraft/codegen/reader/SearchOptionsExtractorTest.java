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

import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SearchOptions;
import nl.datasteel.crudcraft.codegen.reader.field.SearchOptionsExtractor;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SearchOptionsExtractorTest {
    private Elements elements;

    @BeforeEach
    void setup() {
        String src = "package t;" +
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;" +
                "import java.util.*;" +
                "class C {" +
                "@Searchable String s;" +
                "@Searchable int n;" +
                "@Searchable boolean b;" +
                "@Searchable java.util.UUID u;" +
                "@Searchable java.time.LocalDate d;" +
                "enum E {A;} @Searchable E e;" +
                "@Searchable List<String> list;" +
                "@Searchable Map<String,String> map;" +
                "@Searchable Object o;" +
                "}";
        elements = CompilationTestUtils.elements("t.C", src);
    }

    private SearchOptions extract(String name) {
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement ve = (VariableElement) type.getEnclosedElements().stream()
                .filter(e -> e.getSimpleName().contentEquals(name)).findFirst().orElseThrow();
        return SearchOptionsExtractor.INSTANCE.extract(ve, new TestUtils.ProcessingEnvStub(elements));
    }

    @Test
    void defaultsBasedOnType() {
        assertTrue(extract("s").getOperators().contains(SearchOperator.CONTAINS));
        assertTrue(extract("n").getOperators().contains(SearchOperator.GT));
        assertTrue(extract("b").getOperators().contains(SearchOperator.EQUALS));
        assertTrue(extract("u").getOperators().contains(SearchOperator.IN));
        assertTrue(extract("d").getOperators().contains(SearchOperator.BEFORE));
        assertTrue(extract("e").getOperators().contains(SearchOperator.IN));
        assertTrue(extract("list").getOperators().contains(SearchOperator.CONTAINS));
        assertTrue(extract("map").getOperators().contains(SearchOperator.CONTAINS_KEY));
        assertEquals(List.of(SearchOperator.EQUALS), extract("o").getOperators());
    }

    @Test
    void usesExplicitOperatorsAndDepth() {
        String src = "package t; import nl.datasteel.crudcraft.annotations.fields.Searchable;" +
                "import nl.datasteel.crudcraft.annotations.SearchOperator;" +
                "class C { @Searchable(operators={SearchOperator.CONTAINS}, depth=5) String f; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement ve = (VariableElement) type.getEnclosedElements().stream()
                .filter(e -> e.getSimpleName().contentEquals("f"))
                .findFirst().orElseThrow();
        SearchOptions so = SearchOptionsExtractor.INSTANCE.extract(ve, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(List.of(SearchOperator.CONTAINS), so.getOperators());
        assertEquals(5, so.getDepth());
    }

    @Test
    void defaultsWhenAnnotationMissing() {
        String src = "package t; class C { String f; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement ve = (VariableElement) type.getEnclosedElements().stream()
                .filter(e -> e.getSimpleName().contentEquals("f"))
                .findFirst().orElseThrow();
        SearchOptions so = SearchOptionsExtractor.INSTANCE.extract(ve, new TestUtils.ProcessingEnvStub(elements));
        assertFalse(so.isSearchable());
        assertTrue(so.getOperators().isEmpty());
        assertEquals(0, so.getDepth());
    }
}

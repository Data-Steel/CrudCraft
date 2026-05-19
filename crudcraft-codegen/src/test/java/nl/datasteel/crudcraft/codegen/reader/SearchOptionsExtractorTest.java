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

package nl.datasteel.crudcraft.codegen.reader;

import java.lang.reflect.Method;
import java.util.List;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.field.part.SearchOptions;
import nl.datasteel.crudcraft.codegen.reader.field.SearchOptionsExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class SearchOptionsExtractorTest {
    private Elements elements;

    @BeforeEach
    void setup() {
        String src =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "import java.util.*;"
                        + "class C {"
                        + "@Searchable String s;"
                        + "@Searchable int n;"
                        + "@Searchable boolean b;"
                        + "@Searchable java.util.UUID u;"
                        + "@Searchable java.time.LocalDate d;"
                        + "enum E {A;} @Searchable E e;"
                        + "@Searchable List<String> list;"
                        + "@Searchable Map<String,String> map;"
                        + "@Searchable Object o;"
                        + "}";
        elements = CompilationTestUtils.elements("t.C", src);
    }

    private SearchOptions extract(String name) {
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement ve =
                (VariableElement)
                        type.getEnclosedElements().stream()
                                .filter(e -> e.getSimpleName().contentEquals(name))
                                .findFirst()
                                .orElseThrow();
        return SearchOptionsExtractor.INSTANCE.extract(
                ve, new TestUtils.ProcessingEnvStub(elements));
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
        String src =
                "package t; import nl.datasteel.crudcraft.annotations.fields.Searchable;import"
                        + " nl.datasteel.crudcraft.annotations.SearchOperator;class C {"
                        + " @Searchable(operators={SearchOperator.CONTAINS}, depth=5) String f; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement ve =
                (VariableElement)
                        type.getEnclosedElements().stream()
                                .filter(e -> e.getSimpleName().contentEquals("f"))
                                .findFirst()
                                .orElseThrow();
        SearchOptions so =
                SearchOptionsExtractor.INSTANCE.extract(
                        ve, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(List.of(SearchOperator.CONTAINS), so.getOperators());
        assertEquals(5, so.getDepth());
    }

    @Test
    void defaultsWhenAnnotationMissing() {
        String src = "package t; class C { String f; }";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement ve =
                (VariableElement)
                        type.getEnclosedElements().stream()
                                .filter(e -> e.getSimpleName().contentEquals("f"))
                                .findFirst()
                                .orElseThrow();
        SearchOptions so =
                SearchOptionsExtractor.INSTANCE.extract(
                        ve, new TestUtils.ProcessingEnvStub(elements));
        assertFalse(so.isSearchable());
        assertTrue(so.getOperators().isEmpty());
        assertEquals(0, so.getDepth());
    }

    @Test
    void extractionLogsResolvedOptions() {
        TypeElement type = elements.getTypeElement("t.C");
        VariableElement field =
                (VariableElement)
                        type.getEnclosedElements().stream()
                                .filter(e -> e.getSimpleName().contentEquals("s"))
                                .findFirst()
                                .orElseThrow();
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        Messager messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);

        SearchOptionsExtractor.INSTANCE.extract(field, env);

        verify(messager)
                .printMessage(
                        eq(Diagnostic.Kind.NOTE),
                        contains("Extracting SearchOptions"));
    }

    @Test
    void relationshipFieldsGetEqualsOperator() {
        String src =
                "package t;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "import jakarta.persistence.*;"
                        + "class Related {}"
                        + "@Embeddable class Emb { String v; }"
                        + "class C {"
                        + "@Searchable @ManyToOne Related manyToOne;"
                        + "@Searchable @OneToOne Related oneToOne;"
                        + "@Searchable @ManyToMany java.util.Set<Related> manyToMany;"
                        + "@Searchable @OneToMany java.util.Set<Related> oneToMany;"
                        + "@Searchable @Embedded Emb embedded;"
                        + "}";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement type = elements.getTypeElement("t.C");

        // ManyToOne should get EQUALS, not collection operators
        VariableElement manyToOne =
                (VariableElement)
                        type.getEnclosedElements().stream()
                                .filter(e -> e.getSimpleName().contentEquals("manyToOne"))
                                .findFirst()
                                .orElseThrow();
        SearchOptions soManyToOne =
                SearchOptionsExtractor.INSTANCE.extract(
                        manyToOne, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(
                List.of(SearchOperator.EQUALS),
                soManyToOne.getOperators(),
                "ManyToOne relationships should use EQUALS operator");

        // OneToOne should get EQUALS
        VariableElement oneToOne =
                (VariableElement)
                        type.getEnclosedElements().stream()
                                .filter(e -> e.getSimpleName().contentEquals("oneToOne"))
                                .findFirst()
                                .orElseThrow();
        SearchOptions soOneToOne =
                SearchOptionsExtractor.INSTANCE.extract(
                        oneToOne, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(
                List.of(SearchOperator.EQUALS),
                soOneToOne.getOperators(),
                "OneToOne relationships should use EQUALS operator");

        // ManyToMany should get EQUALS, NOT CONTAINS
        VariableElement manyToMany =
                (VariableElement)
                        type.getEnclosedElements().stream()
                                .filter(e -> e.getSimpleName().contentEquals("manyToMany"))
                                .findFirst()
                                .orElseThrow();
        SearchOptions soManyToMany =
                SearchOptionsExtractor.INSTANCE.extract(
                        manyToMany, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(
                List.of(SearchOperator.EQUALS),
                soManyToMany.getOperators(),
                "ManyToMany relationships should use EQUALS operator, not CONTAINS");

        // OneToMany should get EQUALS, NOT CONTAINS
        VariableElement oneToMany =
                (VariableElement)
                        type.getEnclosedElements().stream()
                                .filter(e -> e.getSimpleName().contentEquals("oneToMany"))
                                .findFirst()
                                .orElseThrow();
        SearchOptions soOneToMany =
                SearchOptionsExtractor.INSTANCE.extract(
                        oneToMany, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(
                List.of(SearchOperator.EQUALS),
                soOneToMany.getOperators(),
                "OneToMany relationships should use EQUALS operator, not CONTAINS");

        // Embedded should also be treated as relationship
        VariableElement embedded =
                (VariableElement)
                        type.getEnclosedElements().stream()
                                .filter(e -> e.getSimpleName().contentEquals("embedded"))
                                .findFirst()
                                .orElseThrow();
        SearchOptions soEmbedded =
                SearchOptionsExtractor.INSTANCE.extract(
                        embedded, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(
                List.of(SearchOperator.EQUALS),
                soEmbedded.getOperators(),
                "Embedded fields should use relationship defaults");
    }

    @Test
    void privateTypePredicatesCoverAllSuffixAndFallbackPaths() throws Exception {
        assertTrue(invokeBooleanPredicate("isString", "java.lang.String"));
        assertTrue(invokeBooleanPredicate("isString", "java.lang.@x.NotBlank String"));
        assertFalse(invokeBooleanPredicate("isString", "java.lang.StringBuilder"));

        assertTrue(invokeBooleanPredicate("isBoolean", "boolean"));
        assertTrue(invokeBooleanPredicate("isBoolean", "java.lang.Boolean"));
        assertTrue(invokeBooleanPredicate("isBoolean", "java.lang.@x.Flag Boolean"));
        assertFalse(invokeBooleanPredicate("isBoolean", "java.lang.Integer"));

        assertTrue(invokeBooleanPredicate("isNumeric", "int"));
        assertTrue(invokeBooleanPredicate("isNumeric", "java.lang.Long"));
        assertTrue(invokeBooleanPredicate("isNumeric", "java.lang.@x.Range Double"));
        assertFalse(invokeBooleanPredicate("isNumeric", "java.math.BigDecimal"));

        assertTrue(invokeBooleanPredicate("isUuid", "java.util.UUID"));
        assertTrue(invokeBooleanPredicate("isUuid", "java.lang.@x.Id UUID"));
        assertFalse(invokeBooleanPredicate("isUuid", "java.lang.String"));

        assertTrue(invokeBooleanPredicate("isDateTime", "java.time.LocalDate"));
        assertTrue(invokeBooleanPredicate("isDateTime", "java.time.@x.Future OffsetDateTime"));
        assertFalse(invokeBooleanPredicate("isDateTime", "java.time.Year"));

        assertTrue(invokeBooleanPredicate("isCollection", "java.util.List<java.lang.String>"));
        assertTrue(
                invokeBooleanPredicate("isCollection", "java.util.Collection<java.lang.String>"));
        assertFalse(
                invokeBooleanPredicate(
                        "isCollection", "java.util.Map<java.lang.String,java.lang.String>"));

        assertTrue(
                invokeBooleanPredicate(
                        "isMap", "java.util.Map<java.lang.String,java.lang.String>"));
        assertFalse(
                invokeBooleanPredicate(
                        "isMap", "java.util.HashMap<java.lang.String,java.lang.String>"));
    }

    @Test
    void defaultOperatorResolverCoversEnumAndFallbackBranches() throws Exception {
        Method resolver =
                SearchOptionsExtractor.class.getDeclaredMethod(
                        "getDefaultOperatorsFor", TypeMirror.class, boolean.class);
        resolver.setAccessible(true);

        TypeMirror enumType = mock(DeclaredType.class);
        Element enumElement = mock(Element.class);
        when(((DeclaredType) enumType).asElement()).thenReturn(enumElement);
        when(enumElement.getKind()).thenReturn(ElementKind.ENUM);
        @SuppressWarnings("unchecked")
        List<SearchOperator> enumOps =
                (List<SearchOperator>) resolver.invoke(null, enumType, false);
        assertTrue(enumOps.contains(SearchOperator.NOT_IN));

        TypeMirror booleanType = mock(TypeMirror.class);
        when(booleanType.toString()).thenReturn("boolean");
        @SuppressWarnings("unchecked")
        List<SearchOperator> booleanOps =
                (List<SearchOperator>) resolver.invoke(null, booleanType, false);
        assertEquals(List.of(SearchOperator.EQUALS, SearchOperator.NOT_EQUALS), booleanOps);

        TypeMirror uuidType = mock(TypeMirror.class);
        when(uuidType.toString()).thenReturn("java.util.UUID");
        @SuppressWarnings("unchecked")
        List<SearchOperator> uuidOps =
                (List<SearchOperator>) resolver.invoke(null, uuidType, false);
        assertTrue(uuidOps.contains(SearchOperator.NOT_IN));

        TypeMirror collectionType = mock(TypeMirror.class);
        when(collectionType.toString()).thenReturn("java.util.List<java.lang.String>");
        @SuppressWarnings("unchecked")
        List<SearchOperator> collectionOps =
                (List<SearchOperator>) resolver.invoke(null, collectionType, false);
        assertTrue(collectionOps.contains(SearchOperator.CONTAINS_ALL));

        TypeMirror mapType = mock(TypeMirror.class);
        when(mapType.toString()).thenReturn("java.util.Map<java.lang.String,java.lang.String>");
        @SuppressWarnings("unchecked")
        List<SearchOperator> mapOps =
                (List<SearchOperator>) resolver.invoke(null, mapType, false);
        assertEquals(
                List.of(
                        SearchOperator.CONTAINS_KEY,
                        SearchOperator.CONTAINS_VALUE,
                        SearchOperator.SIZE_EQUALS),
                mapOps);

        TypeMirror relationType = mock(TypeMirror.class);
        when(relationType.toString()).thenReturn("com.example.Relation");
        @SuppressWarnings("unchecked")
        List<SearchOperator> relationOps =
                (List<SearchOperator>) resolver.invoke(null, relationType, true);
        assertEquals(List.of(SearchOperator.EQUALS), relationOps);

        TypeMirror fallbackType = mock(TypeMirror.class);
        when(fallbackType.toString()).thenReturn("com.example.CustomScalar");
        @SuppressWarnings("unchecked")
        List<SearchOperator> fallbackOps =
                (List<SearchOperator>) resolver.invoke(null, fallbackType, false);
        assertEquals(List.of(SearchOperator.EQUALS), fallbackOps);
    }

    @Test
    void numericAndDatePredicatesCoverAllExactAndSuffixVariants() throws Exception {
        String[] numericExact = {
            "int", "java.lang.Integer", "long", "java.lang.Long",
            "float", "java.lang.Float", "double", "java.lang.Double"
        };
        for (String value : numericExact) {
            assertTrue(
                    invokeBooleanPredicate("isNumeric", value), "Expected numeric for: " + value);
        }
        String[] numericSuffix = {
            "x.y.@A int", "x.y.@A Integer", "x.y.@A long", "x.y.@A Long",
            "x.y.@A float", "x.y.@A Float", "x.y.@A double", "x.y.@A Double"
        };
        for (String value : numericSuffix) {
            assertTrue(
                    invokeBooleanPredicate("isNumeric", value),
                    "Expected numeric suffix for: " + value);
        }
        assertFalse(invokeBooleanPredicate("isNumeric", "short"));

        String[] dateExact = {
            "java.time.LocalDate",
            "java.time.LocalDateTime",
            "java.time.Instant",
            "java.time.OffsetDateTime"
        };
        for (String value : dateExact) {
            assertTrue(
                    invokeBooleanPredicate("isDateTime", value), "Expected datetime for: " + value);
        }
        String[] dateSuffix = {
            "x.y.@A LocalDate", "x.y.@A LocalDateTime", "x.y.@A Instant", "x.y.@A OffsetDateTime"
        };
        for (String value : dateSuffix) {
            assertTrue(
                    invokeBooleanPredicate("isDateTime", value),
                    "Expected datetime suffix for: " + value);
        }
        assertFalse(invokeBooleanPredicate("isDateTime", "java.time.ZoneId"));
    }

    private static boolean invokeBooleanPredicate(String method, String value) throws Exception {
        Method m = SearchOptionsExtractor.class.getDeclaredMethod(method, String.class);
        m.setAccessible(true);
        return (boolean) m.invoke(null, value);
    }
}

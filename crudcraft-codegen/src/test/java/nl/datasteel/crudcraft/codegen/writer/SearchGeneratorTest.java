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

package nl.datasteel.crudcraft.codegen.writer;

import com.google.testing.compile.JavaFileObjects;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.JavaFile;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.reader.AnnotationModelReader;
import nl.datasteel.crudcraft.codegen.writer.search.PropertySpec;
import nl.datasteel.crudcraft.codegen.writer.search.SearchField;
import nl.datasteel.crudcraft.codegen.writer.search.SearchRequestGenerator;
import nl.datasteel.crudcraft.codegen.writer.search.SearchSpecificationGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/** Tests for SearchGenerator. */
class SearchGeneratorTest {

    private WriteContext ctx;

    private static final class NoOpMessager implements Messager {
        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            // Unused (for now)
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
            // Unused (for now)
        }

        @Override
        public void printMessage(
                Diagnostic.Kind kind,
                CharSequence msg,
                Element e,
                javax.lang.model.element.AnnotationMirror a) {
            // Unused (for now)
        }

        @Override
        public void printMessage(
                Diagnostic.Kind kind,
                CharSequence msg,
                Element e,
                javax.lang.model.element.AnnotationMirror a,
                javax.lang.model.element.AnnotationValue v) {
            // Unused (for now)
        }
    }

    // Helper ProcessingEnvironment stub for tests that need real compilation
    private static class EnvStub implements ProcessingEnvironment {
        private final Elements elements;
        private static final Messager MESSAGER = new NoOpMessager();

        EnvStub(Elements elements) {
            this.elements = elements;
        }

        @Override
        public Map<String, String> getOptions() {
            return Map.of();
        }

        @Override
        public Messager getMessager() {
            return MESSAGER;
        }

        @Override
        public Filer getFiler() {
            return null;
        }

        @Override
        public Elements getElementUtils() {
            return elements;
        }

        @Override
        public Types getTypeUtils() {
            return null;
        }

        @Override
        public SourceVersion getSourceVersion() {
            return SourceVersion.latest();
        }

        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }
    }

    @BeforeEach
    void setup() {
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        Messager messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);
        ctx = new WriteContext(env);
    }

    @Test
    void returnsEmptyForInvalidModel() {
        SearchGenerator gen = new SearchGenerator();
        assertTrue(gen.generate(null, ctx).isEmpty());
    }

    @Test
    void generatesRequestAndSpecificationWhenSearchableFieldsExist() {
        String entitySrc =
                "package test;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Book {"
                        + "  @Searchable String title;"
                        + "}";
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("test.Book", entitySrc));
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext testCtx = new WriteContext(env);
        ModelDescriptor md = AnnotationModelReader.parse(elements.getTypeElement("test.Book"), env);

        SearchGenerator gen = new SearchGenerator();
        List<JavaFile> files = gen.generate(md, testCtx);
        assertEquals(2, files.size());
        assertEquals("BookSearchRequest", files.get(0).typeSpec().name());
        assertEquals("BookSpecification", files.get(1).typeSpec().name());
        String requestCode = files.get(0).toString();
        assertTrue(requestCode.contains("@NotThreadSafe"));
        assertTrue(requestCode.contains("private String title;"));
        assertTrue(requestCode.contains("public String getTitle()"));
        assertTrue(requestCode.contains("public void setTitle(String title)"));
        assertTrue(requestCode.contains("public BookSearchRequest(BookSearchRequest other)"));
        assertTrue(requestCode.contains("this.title = other.title"));
        assertTrue(requestCode.contains("ALLOWED_SEARCH_PATHS = Set.of(\"title\")"));
        assertTrue(requestCode.contains("ALLOWED_SORT_PATHS = Set.of(\"title\")"));
    }

    @Test
    void skipsSearchArtifactsWhenNoSearchableFieldsExist() {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        when(md.getPackageName()).thenReturn("com.example");
        when(md.getFields()).thenReturn(List.of());

        SearchGenerator gen = new SearchGenerator();
        assertTrue(gen.generate(md, ctx).isEmpty());
    }

    @Test
    void specificationIncludesDistinctForManyToManyRelationships() {
        // Create a model with a ManyToMany relationship field
        String entitySrc =
                "package test;import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;import"
                        + " nl.datasteel.crudcraft.annotations.fields.Searchable;import"
                        + " jakarta.persistence.ManyToMany;@CrudCrafted class SystemEntity { "
                        + " @Searchable @ManyToMany java.util.Set<SystemEntityVariant>"
                        + " systemEntityVariants;}";
        String variantSrc =
                "package test;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "import jakarta.persistence.Entity;"
                        + "@CrudCrafted @Entity class SystemEntityVariant {"
                        + "  @Searchable Long id;"
                        + "}";

        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("test.SystemEntity", entitySrc),
                        JavaFileObjects.forSourceString("test.SystemEntityVariant", variantSrc));
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext testCtx = new WriteContext(env);

        var entityEl = elements.getTypeElement("test.SystemEntity");
        ModelDescriptor md = AnnotationModelReader.parse(entityEl, env);

        SearchGenerator gen = new SearchGenerator();
        List<JavaFile> files = gen.generate(md, testCtx);

        // Verify that both files are generated
        assertEquals(2, files.size(), "Should generate SearchRequest and Specification");

        // Get the Specification file (second one)
        JavaFile specFile = files.get(1);
        assertEquals("SystemEntitySpecification", specFile.typeSpec().name());

        // Convert to string and verify it contains the distinct call
        String specCode = specFile.toString();
        assertTrue(
                specCode.contains("query.distinct(true)"),
                "Specification should call query.distinct(true) when ManyToMany relationships are"
                        + " searched");

        // Verify it has the nested field for filtering (systemEntityVariantsId)
        assertTrue(
                specCode.contains("systemEntityVariantsId")
                        || specCode.contains("join(\"systemEntityVariants\")"),
                "Specification should include the nested field path with join");
    }

    @Test
    void specificationDoesNotIncludeDistinctForSimpleFields() {
        // Create a model with only simple fields (no relationships)
        String entitySrc =
                "package test;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class SimpleEntity {"
                        + "  @Searchable String name;"
                        + "  @Searchable Long id;"
                        + "}";

        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("test.SimpleEntity", entitySrc));
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext testCtx = new WriteContext(env);

        var entityEl = elements.getTypeElement("test.SimpleEntity");
        ModelDescriptor md = AnnotationModelReader.parse(entityEl, env);

        SearchGenerator gen = new SearchGenerator();
        List<JavaFile> files = gen.generate(md, testCtx);

        assertEquals(2, files.size());

        // Get the Specification file
        JavaFile specFile = files.get(1);
        String specCode = specFile.toString();

        // Should NOT contain distinct call for simple fields
        assertFalse(
                specCode.contains("query.distinct(true)"),
                "Specification should NOT call query.distinct(true) when only simple fields are"
                        + " searched");
    }

    @Test
    void generatedRequestHasSearchLogicField() {
        // Create a simple model
        String entitySrc =
                "package test;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class SimpleEntity {"
                        + "  @Searchable String name;"
                        + "}";

        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("test.SimpleEntity", entitySrc));
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext testCtx = new WriteContext(env);

        var entityEl = elements.getTypeElement("test.SimpleEntity");
        ModelDescriptor md = AnnotationModelReader.parse(entityEl, env);

        SearchGenerator gen = new SearchGenerator();
        List<JavaFile> files = gen.generate(md, testCtx);

        // Get the SearchRequest file
        JavaFile requestFile = files.get(0);
        String requestCode = requestFile.toString();

        // Should contain searchLogic field
        assertTrue(
                requestCode.contains("private SearchLogic searchLogic"),
                "SearchRequest should have a searchLogic field");
        assertTrue(
                requestCode.contains("public SearchLogic getSearchLogic()"),
                "SearchRequest should have a getSearchLogic() method");
        assertTrue(
                requestCode.contains("public void setSearchLogic(SearchLogic searchLogic)"),
                "SearchRequest should have a setSearchLogic() method");
        assertTrue(
                requestCode.contains("searchLogic != null ? searchLogic : SearchLogic.OR"),
                "getSearchLogic should default to OR");
        assertTrue(
                requestCode.contains("allowedSearchOperators"),
                "SearchRequest should expose allowed operators per search path");
        assertTrue(
                requestCode.contains("requestedSearchCriteria"),
                "SearchRequest should expose active criteria for runtime validation");
        assertTrue(
                requestCode.contains("public void validate()"),
                "SearchRequest should expose an explicit validation hook");
        assertTrue(
                requestCode.contains("private static final int MAX_SEARCH_PATH_DEPTH = 1"),
                "SearchRequest should expose the generated search path depth budget");
        assertTrue(
                requestCode.contains("public int maxSearchPathDepth()"),
                "SearchRequest should expose the generated depth budget through the runtime contract");
        assertTrue(
                requestCode.contains("SearchRequest.super.validate()"),
                "Generated validation hook should delegate to runtime metadata validation");
        assertTrue(
                requestCode.contains(
                        "SearchPathGuard.enforceMaxDepth(criterion.path(),"
                                + " MAX_SEARCH_PATH_DEPTH)"),
                "Generated validation hook should explicitly enforce search path depth");
        assertTrue(
                requestCode.contains("validate();"),
                "SearchRequest should validate before building a specification");
    }

    @Test
    void generatedRequestDerivesDepthBudgetFromNestedSearchFields() {
        String entitySrc =
                "package test;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Book {"
                        + "  @Searchable Category category;"
                        + "}";
        String categorySrc =
                "package test;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class Category {"
                        + "  @Searchable String name;"
                        + "}";

        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("test.Book", entitySrc),
                        JavaFileObjects.forSourceString("test.Category", categorySrc));
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext testCtx = new WriteContext(env);
        ModelDescriptor md = AnnotationModelReader.parse(elements.getTypeElement("test.Book"), env);

        SearchGenerator gen = new SearchGenerator();
        List<JavaFile> files = gen.generate(md, testCtx);
        String requestCode = files.getFirst().toString();

        assertTrue(requestCode.contains("\"categoryName\""));
        assertTrue(requestCode.contains("private static final int MAX_SEARCH_PATH_DEPTH = 2"));
    }

    @Test
    void generatedSpecificationUsesSearchLogic() {
        // Create a simple model
        String entitySrc =
                "package test;"
                        + "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;"
                        + "import nl.datasteel.crudcraft.annotations.fields.Searchable;"
                        + "@CrudCrafted class SimpleEntity {"
                        + "  @Searchable String name;"
                        + "}";

        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("test.SimpleEntity", entitySrc));
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext testCtx = new WriteContext(env);

        var entityEl = elements.getTypeElement("test.SimpleEntity");
        ModelDescriptor md = AnnotationModelReader.parse(entityEl, env);

        SearchGenerator gen = new SearchGenerator();
        List<JavaFile> files = gen.generate(md, testCtx);

        // Get the Specification file
        JavaFile specFile = files.get(1);
        String specCode = specFile.toString();

        // Should contain logic variable initialization
        assertTrue(
                specCode.contains("SearchLogic logic = request.getSearchLogic()"),
                "Specification should get logic from request");
        assertTrue(
                specCode.contains("logic == SearchLogic.AND ? cb.conjunction() : cb.disjunction()"),
                "Specification should initialize predicate based on logic");
        assertTrue(
                specCode.contains("logic == SearchLogic.AND ? cb.and(p,")
                        || specCode.contains("logic == SearchLogic.AND ? cb.and"),
                "Specification should use logic to combine predicates");
        assertTrue(
                specCode.contains("if (!hasCriteria && logic == SearchLogic.OR)"),
                "Specification should return match-all for empty OR searches");
    }

    @Test
    void generatedRequestBoxesPrimitivesAndCopiesRangeFields() {
        String entitySrc =
                "package test;import nl.datasteel.crudcraft.annotations.SearchOperator;import"
                        + " nl.datasteel.crudcraft.annotations.classes.CrudCrafted;import"
                        + " nl.datasteel.crudcraft.annotations.fields.Searchable;@CrudCrafted class"
                        + " RangedEntity {  @Searchable(operators = {SearchOperator.RANGE}) int"
                        + " count;}";

        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("test.RangedEntity", entitySrc));
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext testCtx = new WriteContext(env);
        ModelDescriptor md =
                AnnotationModelReader.parse(elements.getTypeElement("test.RangedEntity"), env);

        SearchGenerator gen = new SearchGenerator();
        List<JavaFile> files = gen.generate(md, testCtx);
        String requestCode = files.getFirst().toString();

        assertTrue(requestCode.contains("private Integer countStart;"));
        assertTrue(requestCode.contains("private Integer countEnd;"));
        assertTrue(requestCode.contains("this.countStart = other.countStart"));
        assertTrue(requestCode.contains("this.countEnd = other.countEnd"));
    }

    @Test
    void generatesIndependentSortAndSearchPathMetadata() {
        String entitySrc =
                "package test;import java.util.List;import"
                        + " nl.datasteel.crudcraft.annotations.SearchOperator;import"
                        + " nl.datasteel.crudcraft.annotations.classes.CrudCrafted;import"
                        + " nl.datasteel.crudcraft.annotations.fields.Searchable;@CrudCrafted class"
                        + " SizedEntity {  @Searchable(operators = {SearchOperator.SIZE_EQUALS})"
                        + " List<String> tags;}";

        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("test.SizedEntity", entitySrc));
        ProcessingEnvironment env = new EnvStub(elements);
        WriteContext testCtx = new WriteContext(env);
        ModelDescriptor md =
                AnnotationModelReader.parse(elements.getTypeElement("test.SizedEntity"), env);

        SearchGenerator gen = new SearchGenerator();
        List<JavaFile> files = gen.generate(md, testCtx);
        String requestCode = files.getFirst().toString();

        assertTrue(requestCode.contains("ALLOWED_SEARCH_PATHS"));
        assertTrue(requestCode.contains("\"tagsSize\""));
        assertTrue(requestCode.contains("private Integer tagsSize;"));
        assertTrue(requestCode.contains("public Integer getTagsSize()"));
        assertTrue(requestCode.contains("this.tagsSize = other.tagsSize"));
        assertTrue(
                requestCode.contains("ALLOWED_SORT_PATHS = Set.of()"),
                "Size-only fields should not be emitted as sortable paths");
    }

    @Test
    void privateGenerationHelpersCoverEmptyAndDefaultOperatorBranches() throws Exception {
        ModelDescriptor md = mock(ModelDescriptor.class);
        when(md.getName()).thenReturn("Book");
        when(md.getPackageName()).thenReturn("com.example");
        SearchGenerator gen = new SearchGenerator();
        SearchRequestGenerator requestGenerator = new SearchRequestGenerator();
        SearchSpecificationGenerator specificationGenerator = new SearchSpecificationGenerator();
        assertEquals(1, gen.order());

        JavaFile emptyRequest =
                (JavaFile)
                        invokePrivate(
                                requestGenerator,
                                "generate",
                                new Class<?>[] {ModelDescriptor.class, List.class},
                                md,
                                List.of());
        JavaFile emptySpecification =
                (JavaFile)
                        invokePrivate(
                                specificationGenerator,
                                "generate",
                                new Class<?>[] {ModelDescriptor.class, List.class},
                                md,
                                List.of());
        JavaFile noJoinSpecification =
                (JavaFile)
                        invokePrivate(
                                specificationGenerator,
                                "generate",
                                new Class<?>[] {ModelDescriptor.class, List.class},
                                md,
                                List.of(
                                        new SearchField(
                                                mock(FieldDescriptor.class),
                                                "title",
                                                "root.get(\"title\")",
                                                SearchOperator.EQUALS)));
        CodeBlock emptyOperators =
                (CodeBlock)
                        invokePrivate(
                                requestGenerator,
                                "buildAllowedOperatorsByPath",
                                new Class<?>[] {Map.class},
                                Map.of());
        Map<String, Set<SearchOperator>> operators = new LinkedHashMap<>();
        operators.put("flag", Set.of());
        CodeBlock emptyOperatorEntry =
                (CodeBlock)
                        invokePrivate(
                                requestGenerator,
                                "buildAllowedOperatorsByPath",
                                new Class<?>[] {Map.class},
                                operators);
        boolean notEmptySortable =
                (boolean)
                        invokePrivate(
                                requestGenerator,
                                "isSortable",
                                new Class<?>[] {Set.class, String.class},
                                Set.of(SearchOperator.NOT_EMPTY),
                                "flag");
        boolean equalsSortable =
                (boolean)
                        invokePrivate(
                                requestGenerator,
                                "isSortable",
                                new Class<?>[] {Set.class, String.class},
                                Set.of(SearchOperator.EQUALS),
                                "name");
        boolean sizePropertySortable =
                (boolean)
                        invokePrivate(
                                requestGenerator,
                                "isSortable",
                                new Class<?>[] {Set.class, String.class},
                                Set.of(SearchOperator.EQUALS),
                                "tagsSize");
        String capitalized =
                (String)
                        invokePrivate(
                                requestGenerator,
                                "capitalize",
                                new Class<?>[] {String.class},
                                "bookTitle");

        assertTrue(emptyRequest.toString().contains("ALLOWED_SEARCH_PATHS = Set.of()"));
        assertTrue(emptyRequest.toString().contains("return new BookSpecification()"));
        assertFalse(emptyRequest.toString().contains("BookSearchRequest(BookSearchRequest other)"));
        assertTrue(emptySpecification.toString().contains("public BookSpecification()"));
        assertFalse(emptySpecification.toString().contains("if (!hasCriteria"));
        assertFalse(noJoinSpecification.toString().contains("query.distinct(true)"));
        assertEquals("java.util.Map.of()", emptyOperators.toString());
        assertTrue(emptyOperatorEntry.toString().contains("Set.of()"));
        assertTrue(equalsSortable);
        assertFalse(notEmptySortable);
        assertFalse(sizePropertySortable);
        assertEquals("BookTitle", capitalized);
    }

    @Test
    void requestedCriteriaSkipsOperatorOnlyProperties() throws Exception {
        SearchRequestGenerator gen = new SearchRequestGenerator();
        FieldDescriptor descriptor = mock(FieldDescriptor.class);
        PropertySpec operatorOnly =
                new PropertySpec(descriptor, "flag", Set.of(SearchOperator.NOT_EMPTY));

        Object method =
                invokePrivate(
                        gen,
                        "buildRequestedCriteriaMethod",
                        new Class<?>[] {List.class, com.palantir.javapoet.ClassName.class},
                        List.of(operatorOnly),
                        com.palantir.javapoet.ClassName.get(
                                "nl.datasteel.crudcraft.runtime.search", "SearchRequest"));

        assertFalse(method.toString().contains("criteria.add"));
    }

    private static Object invokePrivate(
            Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}

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

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.codegen.descriptor.RelationshipType;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/** Tests for InsomniaGenerator. */
class InsomniaGeneratorTest {

    static class SingleEndpointPolicy implements CrudEndpointPolicy {
        @Override
        public Set<CrudEndpoint> resolveEndpoints() {
            return Set.of(CrudEndpoint.GET_ONE);
        }

        @Override
        public String name() {
            return "single";
        }
    }

    static class BrokenPolicy implements CrudEndpointPolicy {
        private BrokenPolicy() {}

        @Override
        public Set<CrudEndpoint> resolveEndpoints() {
            return Set.of(CrudEndpoint.DELETE);
        }

        @Override
        public String name() {
            return "broken";
        }
    }

    private final InsomniaGenerator generator = new InsomniaGenerator();
    private WriteContext ctx;
    private Messager messager;

    @TempDir private Path tempDir;

    @BeforeEach
    void clearState() {
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);
        ctx = new WriteContext(env);
    }

    @Test
    void generatorContractValuesAreStable() {
        assertTrue(generator.requiresCrudEntity());
        assertEquals(1000, generator.order());
    }

    @Test
    void writeReturnsEarlyForInvalidDescriptor() {
        generator.write(null, ctx);
        assertTrue(generator.generate(mock(ModelDescriptor.class), ctx).isEmpty());
    }

    @Test
    void writeCreatesInsomniaFileAndReportsLocation() throws Exception {
        InsomniaGenerator writingGenerator = new InsomniaGenerator(tempDir);
        ModelDescriptor model = configuredModel("Book", CrudTemplate.READ_ONLY, CrudTemplate.class);

        writingGenerator.write(model, ctx);

        Path output = tempDir.resolve("insomnia.json");
        assertTrue(Files.exists(output));
        String json = Files.readString(output);
        assertTrue(json.contains("\"name\": \"Books\""));
        assertTrue(json.contains("\"__export_date\": \"2026-01-01T00:00:00Z\""));
        verify(messager).printMessage(eq(Diagnostic.Kind.NOTE), contains("Generated Insomnia collection"));
    }

    @Test
    void writeReportsWarningWhenInsomniaFileCannotBeWritten() throws Exception {
        Path notDirectory = Files.createTempFile(tempDir, "insomnia-target", ".txt");
        InsomniaGenerator failingGenerator = new InsomniaGenerator(notDirectory);
        ModelDescriptor model = configuredModel("Book", CrudTemplate.READ_ONLY, CrudTemplate.class);

        failingGenerator.write(model, ctx);

        verify(messager).printMessage(eq(Diagnostic.Kind.WARNING), contains("Failed to generate Insomnia file"));
    }

    @Test
    void writeReportsWarningWhenOutputFileHasNoParent() {
        InsomniaGenerator failingGenerator = new InsomniaGenerator(Path.of(""));
        ModelDescriptor model = configuredModel("Book", CrudTemplate.READ_ONLY, CrudTemplate.class);

        failingGenerator.write(model, ctx);

        verify(messager).printMessage(eq(Diagnostic.Kind.WARNING), contains("no parent directory"));
    }

    @Test
    void writeReportsWarningWhenOutputDirectoryIsNotWritable() {
        InsomniaGenerator failingGenerator = new InsomniaGenerator(tempDir, path -> false);
        ModelDescriptor model = configuredModel("Book", CrudTemplate.READ_ONLY, CrudTemplate.class);

        failingGenerator.write(model, ctx);

        verify(messager).printMessage(eq(Diagnostic.Kind.WARNING), contains("not writable"));
    }

    @Test
    void resolveOutputDirectoryUsesConfiguredProcessorOption() throws Exception {
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        when(env.getOptions()).thenReturn(Map.of("crudcraft.insomnia.outputDir", tempDir.toString()));
        Path resolved =
                (Path)
                        invoke(
                                "resolveOutputDirectory",
                                new Class<?>[] {ProcessingEnvironmentProvider.class},
                                new WriteContext(env));

        assertEquals(tempDir.toAbsolutePath().normalize(), resolved);
    }

    @Test
    void resolveOutputDirectoryFallsBackToClassOutput() throws Exception {
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        Filer filer = mock(Filer.class);
        FileObject fileObject = mock(FileObject.class);
        Path generatedFile = tempDir.resolve("classes").resolve("insomnia.json");
        when(env.getOptions()).thenReturn(Map.of());
        when(env.getFiler()).thenReturn(filer);
        when(filer.getResource(StandardLocation.CLASS_OUTPUT, "", "insomnia.json"))
                .thenReturn(fileObject);
        when(fileObject.toUri()).thenReturn(generatedFile.toUri());
        Path resolved =
                (Path)
                        invoke(
                                "resolveOutputDirectory",
                                new Class<?>[] {ProcessingEnvironmentProvider.class},
                                new WriteContext(env));

        assertEquals(generatedFile.getParent().toAbsolutePath().normalize(), resolved);
    }

    @Test
    void resolveEndpointsUsesPolicyAndIncludeOmitRules() throws Exception {
        ModelDescriptor model = mock(ModelDescriptor.class);
        doReturn(policyClass(SingleEndpointPolicy.class)).when(model).getEndpointPolicy();
        when(model.getTemplate()).thenReturn(CrudTemplate.READ_ONLY);
        when(model.getOmitEndpoints()).thenReturn(new CrudEndpoint[] {CrudEndpoint.GET_ONE});
        when(model.getIncludeEndpoints()).thenReturn(new CrudEndpoint[] {CrudEndpoint.POST});

        @SuppressWarnings("unchecked")
        Set<CrudEndpoint> endpoints =
                (Set<CrudEndpoint>)
                        invoke("resolveEndpoints", new Class<?>[] {ModelDescriptor.class}, model);

        assertFalse(endpoints.contains(CrudEndpoint.GET_ONE));
        assertTrue(endpoints.contains(CrudEndpoint.POST));
    }

    @Test
    void resolveEndpointsFallsBackToTemplateWhenPolicyCannotInstantiate() throws Exception {
        ModelDescriptor model = mock(ModelDescriptor.class);
        doReturn(policyClass(BrokenPolicy.class)).when(model).getEndpointPolicy();
        when(model.getTemplate()).thenReturn(CrudTemplate.READ_ONLY);
        when(model.getOmitEndpoints()).thenReturn(new CrudEndpoint[0]);
        when(model.getIncludeEndpoints()).thenReturn(new CrudEndpoint[0]);

        @SuppressWarnings("unchecked")
        Set<CrudEndpoint> endpoints =
                (Set<CrudEndpoint>)
                        invoke("resolveEndpoints", new Class<?>[] {ModelDescriptor.class}, model);

        assertTrue(endpoints.contains(CrudEndpoint.GET_ONE));
        assertFalse(endpoints.contains(CrudEndpoint.POST));
    }

    @Test
    void generateModelResourcesCreatesFolderAndCrudRequests() throws Exception {
        ModelDescriptor model = mock(ModelDescriptor.class);
        when(model.getName()).thenReturn("Book");
        when(model.getTemplate()).thenReturn(CrudTemplate.FULL);
        doReturn(policyClass(CrudTemplate.class)).when(model).getEndpointPolicy();
        when(model.getOmitEndpoints()).thenReturn(new CrudEndpoint[0]);
        when(model.getIncludeEndpoints()).thenReturn(new CrudEndpoint[0]);
        when(model.getFields()).thenReturn(List.of());

        @SuppressWarnings("unchecked")
        List<String> resources =
                (List<String>)
                        invoke(
                                "generateModelResources",
                                new Class<?>[] {ModelDescriptor.class},
                                model);

        assertEquals(17, resources.size());
        String joined = String.join("\n", resources);
        assertTrue(joined.contains("\"name\": \"List\""));
        assertTrue(joined.contains("\"name\": \"List Ref\""));
        assertTrue(joined.contains("\"name\": \"Create\""));
        assertTrue(joined.contains("\"name\": \"Bulk Delete\""));
        assertTrue(joined.contains("/books/batch/delete"));
        assertTrue(joined.contains("\"name\": \"Exists\""));
        assertTrue(joined.contains("\"name\": \"Count\""));
        assertTrue(joined.contains("\"name\": \"Validate\""));
    }

    @Test
    void generateModelResourcesHonorsNarrowEndpointPolicies() throws Exception {
        ModelDescriptor model = mock(ModelDescriptor.class);
        when(model.getName()).thenReturn("Book");
        when(model.getTemplate()).thenReturn(CrudTemplate.FULL);
        doReturn(policyClass(SingleEndpointPolicy.class)).when(model).getEndpointPolicy();
        when(model.getOmitEndpoints()).thenReturn(new CrudEndpoint[0]);
        when(model.getIncludeEndpoints()).thenReturn(new CrudEndpoint[0]);
        when(model.getFields()).thenReturn(List.of());

        @SuppressWarnings("unchecked")
        List<String> resources =
                (List<String>)
                        invoke(
                                "generateModelResources",
                                new Class<?>[] {ModelDescriptor.class},
                                model);

        assertEquals(2, resources.size());
        String joined = String.join("\n", resources);
        assertTrue(joined.contains("\"name\": \"Get\""));
        assertFalse(joined.contains("\"name\": \"Create\""));
        assertFalse(joined.contains("\"name\": \"Bulk Delete\""));
    }

    @Test
    void individualRequestTemplatesContainExpectedMethodUrlsAndBodies() throws Exception {
        ModelDescriptor model = configuredModel("Book", CrudTemplate.FULL, CrudTemplate.class);
        String folderId = "fld_books";
        String endpoint = "books";
        String idVar = "book_id";

        assertRequest(
                "generateUpdateRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class, String.class},
                new Object[] {model, folderId, endpoint, idVar},
                "\"name\": \"Update\"",
                "\"method\": \"PUT\"",
                "/books/{{ book_id }}",
                "\"body\"");
        assertRequest(
                "generatePatchRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class, String.class},
                new Object[] {model, folderId, endpoint, idVar},
                "\"name\": \"Patch\"",
                "\"method\": \"PATCH\"",
                "/books/{{ book_id }}",
                "\"body\"");
        assertRequest(
                "generateDeleteRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class, String.class},
                new Object[] {model, folderId, endpoint, idVar},
                "\"name\": \"Delete\"",
                "\"method\": \"DELETE\"",
                "/books/{{ book_id }}");
        assertRequest(
                "generateBulkCreateRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class},
                new Object[] {model, folderId, endpoint},
                "\"name\": \"Bulk Create\"",
                "\"method\": \"POST\"",
                "/books/batch",
                "\"body\"");
        assertRequest(
                "generateBulkUpdateRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class, String.class},
                new Object[] {model, folderId, endpoint, idVar},
                "\"name\": \"Bulk Update\"",
                "\"method\": \"PUT\"",
                "/books/batch",
                "{{ book_id }}");
        assertRequest(
                "generateBulkPatchRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class, String.class},
                new Object[] {model, folderId, endpoint, idVar},
                "\"name\": \"Bulk Patch\"",
                "\"method\": \"PATCH\"",
                "/books/batch",
                "{{ book_id }}");
        assertRequest(
                "generateBulkUpsertRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class},
                new Object[] {model, folderId, endpoint},
                "\"name\": \"Bulk Upsert\"",
                "\"method\": \"POST\"",
                "/books/batch",
                "\"body\"");
        assertRequest(
                "generateFindByIdsRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class, String.class},
                new Object[] {model, folderId, endpoint, idVar},
                "\"name\": \"Bulk Ids\"",
                "\"method\": \"POST\"",
                "/books/batch/ids",
                "{{ book_id }}");
        assertRequest(
                "generateBulkDeleteRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class, String.class},
                new Object[] {model, folderId, endpoint, idVar},
                "\"name\": \"Bulk Delete\"",
                "\"method\": \"DELETE\"",
                "/books/batch/delete",
                "{{ book_id }}");
        assertRequest(
                "generateListRefRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class},
                new Object[] {model, folderId, endpoint},
                "\"name\": \"List Ref\"",
                "\"method\": \"GET\"",
                "/books/ref");
        assertRequest(
                "generateExistsRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class, String.class},
                new Object[] {model, folderId, endpoint, idVar},
                "\"name\": \"Exists\"",
                "\"method\": \"GET\"",
                "/books/exists/{{ book_id }}");
        assertRequest(
                "generateCountRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class},
                new Object[] {model, folderId, endpoint},
                "\"name\": \"Count\"",
                "\"method\": \"GET\"",
                "/books/count");
        assertRequest(
                "generateSearchRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class},
                new Object[] {model, folderId, endpoint},
                "\"name\": \"Search\"",
                "\"method\": \"GET\"",
                "/books/search");
        assertRequest(
                "generateValidateRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class},
                new Object[] {model, folderId, endpoint},
                "\"name\": \"Validate\"",
                "\"method\": \"POST\"",
                "/books/validate",
                "\"body\"");
        assertRequest(
                "generateExportRequest",
                new Class<?>[] {ModelDescriptor.class, String.class, String.class},
                new Object[] {model, folderId, endpoint},
                "\"name\": \"Export\"",
                "\"method\": \"GET\"",
                "/books/export?format=csv&limit=100");
    }

    @Test
    void buildInsomniaJsonAndBaseEnvironmentIncludeAllTrackedModels() throws Exception {
        ModelDescriptor book = configuredModel("Book", CrudTemplate.READ_ONLY, CrudTemplate.class);
        ModelDescriptor userAccount =
                configuredModel("UserAccount", CrudTemplate.READ_ONLY, CrudTemplate.class);
        generator.generate(book, ctx);
        generator.generate(userAccount, ctx);

        String baseEnvironment =
                (String)
                        invoke(
                                "generateBaseEnvironment",
                                new Class<?>[] {Set.class},
                                ctx.insomniaModels());
        assertTrue(baseEnvironment.contains("\"book_id\""));
        assertTrue(baseEnvironment.contains("\"user_account_id\""));

        String json =
                (String)
                        invoke(
                                "buildInsomniaJson",
                                new Class<?>[] {Set.class},
                                ctx.insomniaModels());
        assertTrue(json.contains("\"_type\": \"workspace\""));
        assertTrue(json.contains("\"_type\": \"environment\""));
        assertTrue(json.contains("\"name\": \"Books\""));
        assertTrue(json.contains("\"name\": \"User Accounts\""));
    }

    @Test
    void baseEnvironmentWithoutModelsContainsOnlyDefaultVariables() throws Exception {
        String baseEnvironment =
                (String)
                        invoke("generateBaseEnvironment", new Class<?>[] {Set.class}, Set.of());
        assertTrue(baseEnvironment.contains("\"base_url\""));
        assertTrue(baseEnvironment.contains("\"jwt_token\""));
        assertFalse(baseEnvironment.contains("_id\": \"\""));
    }

    @Test
    void sampleValueSupportsAllTypeBranches() throws Exception {
        assertEquals("\\\"\\\"", invokeSampleValue(field("title", "java.lang.String", false)));
        assertEquals("0", invokeSampleValue(field("amount", "java.lang.Integer", false)));
        assertEquals("0", invokeSampleValue(field("quantity", "int", false)));
        assertEquals("0", invokeSampleValue(field("count", "java.lang.Long", false)));
        assertEquals("0", invokeSampleValue(field("counter", "long", false)));
        assertEquals("0.0", invokeSampleValue(field("price", "java.math.BigDecimal", false)));
        assertEquals("0.0", invokeSampleValue(field("ratioExact", "double", false)));
        assertEquals("0.0", invokeSampleValue(field("ratio", "float", false)));
        assertEquals("0.0", invokeSampleValue(field("ratioWrapper", "java.lang.Float", false)));
        assertEquals("0.0", invokeSampleValue(field("score", "java.lang.Double", false)));
        assertEquals("false", invokeSampleValue(field("active", "boolean", false)));
        assertEquals("false", invokeSampleValue(field("enabled", "java.lang.Boolean", false)));
        assertEquals(
                "\\\"2024-01-01T00:00:00Z\\\"",
                invokeSampleValue(field("createdAt", "java.time.Instant", false)));
        assertEquals(
                "\\\"2024-01-01T00:00:00Z\\\"",
                invokeSampleValue(field("publishedAt", "java.time.OffsetDateTime", false)));
        assertEquals(
                "\\\"2024-01-01T00:00:00Z\\\"",
                invokeSampleValue(field("startDate", "java.time.LocalDate", false)));
        assertEquals(
                "\\\"2024-01-01T00:00:00Z\\\"",
                invokeSampleValue(field("zonedAt", "java.time.ZonedDateTime", false)));
        assertEquals(
                "[]", invokeSampleValue(field("tags", "java.util.List<com.example.Tag>", false)));
        assertEquals(
                "[]", invokeSampleValue(field("owners", "java.util.Set<com.example.User>", false)));
        assertEquals("null", invokeSampleValue(field("payload", "com.example.Payload", false)));
        assertEquals(
                "\\\"{{ author_id }}\\\"",
                invokeSampleValue(field("author", "com.example.Author", true)));
        assertEquals(
                "\\\"{{ author_id }}\\\"", invokeSampleValue(field("authorRef", "Author", true)));

        FieldDescriptor nullRelation = field("details", "com.example.Details", false);
        when(nullRelation.getRelType()).thenReturn(null);
        assertEquals("null", invokeSampleValue(nullRelation));
    }

    @Test
    void generateSampleRequestBodyIncludesOnlyWritableFields() throws Exception {
        FieldDescriptor requestField = field("title", "java.lang.String", false);
        when(requestField.inRequest()).thenReturn(true);
        when(requestField.inDto()).thenReturn(true);

        FieldDescriptor relationField = field("author", "com.example.Author", true);
        when(relationField.inRequest()).thenReturn(false);
        when(relationField.inDto()).thenReturn(true);
        when(relationField.isEmbedded()).thenReturn(false);

        FieldDescriptor ignoredField = field("ignored", "java.lang.String", false);
        when(ignoredField.inRequest()).thenReturn(false);
        when(ignoredField.inDto()).thenReturn(false);

        ModelDescriptor model = mock(ModelDescriptor.class);
        when(model.getFields()).thenReturn(List.of(requestField, relationField, ignoredField));

        String body =
                (String)
                        invoke(
                                "generateSampleRequestBody",
                                new Class<?>[] {ModelDescriptor.class},
                                model);

        assertTrue(body.contains("\\\"title\\\""));
        assertTrue(body.contains("\\\"author\\\""));
        assertTrue(body.contains("\\\"title\\\": \\\"\\\", \\\"author\\\""));
        assertTrue(body.startsWith("{\\\"title\\\""));
        assertFalse(body.startsWith("{, "));
        assertFalse(body.contains("\\\"ignored\\\""));
    }

    @Test
    void utilityFormattingMethodsBehaveAsExpected() throws Exception {
        String escaped =
                (String)
                        invoke(
                                "escapeJson",
                                new Class<?>[] {String.class},
                                "\"line\"\n\twith\\slashes");
        assertFalse(escaped.contains("\n"));
        assertFalse(escaped.contains("\t"));
        assertTrue(escaped.contains("\\n"));
        assertTrue(escaped.contains("\\t"));
        assertTrue(escaped.contains("\\\\"));

        String formatted =
                (String)
                        invoke(
                                "jsonFormat",
                                new Class<?>[] {String.class, Object[].class},
                                "%s-%s",
                                new Object[] {"a", "b"});
        assertEquals("a-b", formatted);

        String display =
                (String) invoke("formatDisplayName", new Class<?>[] {String.class}, "BookChapter");
        assertEquals("Book Chapter", display);
        assertEquals("", invoke("formatDisplayName", new Class<?>[] {String.class}, ""));
    }

    private String invokeSampleValue(FieldDescriptor field) throws Exception {
        return (String) invoke("getSampleValue", new Class<?>[] {FieldDescriptor.class}, field);
    }

    private void assertRequest(
            String methodName, Class<?>[] parameterTypes, Object[] args, String... expectedParts)
            throws Exception {
        String request = (String) invoke(methodName, parameterTypes, args);
        assertFalse(request.isBlank(), methodName + " should return a request template");
        for (String expectedPart : expectedParts) {
            assertTrue(
                    request.contains(expectedPart),
                    methodName + " should contain " + expectedPart);
        }
    }

    private FieldDescriptor field(String name, String typeName, boolean manyToOne) {
        FieldDescriptor fd = mock(FieldDescriptor.class);
        TypeMirror type = mock(TypeMirror.class);
        when(type.toString()).thenReturn(typeName);
        when(fd.getName()).thenReturn(name);
        when(fd.getType()).thenReturn(type);
        when(fd.getRelType())
                .thenReturn(manyToOne ? RelationshipType.MANY_TO_ONE : RelationshipType.NONE);
        when(fd.isEmbedded()).thenReturn(false);
        when(fd.inRequest()).thenReturn(true);
        when(fd.inDto()).thenReturn(true);
        return fd;
    }

    private ModelDescriptor configuredModel(
            String name, CrudTemplate template, Class<? extends CrudEndpointPolicy> policyClass) {
        ModelDescriptor model = mock(ModelDescriptor.class);
        when(model.getName()).thenReturn(name);
        when(model.getPackageName()).thenReturn("com.example");
        when(model.getTemplate()).thenReturn(template);
        doReturn(policyClass(policyClass)).when(model).getEndpointPolicy();
        when(model.getOmitEndpoints()).thenReturn(new CrudEndpoint[0]);
        when(model.getIncludeEndpoints()).thenReturn(new CrudEndpoint[0]);
        when(model.getFields()).thenReturn(List.of());
        return model;
    }

    private Object invoke(String method, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method m = InsomniaGenerator.class.getDeclaredMethod(method, parameterTypes);
        m.setAccessible(true);
        return m.invoke(generator, args);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Class<? extends CrudEndpointPolicy> policyClass(Class policyClass) {
        return policyClass;
    }
}

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

package nl.datasteel.crudcraft.codegen;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.tools.JavaFileObject;
import nl.datasteel.crudcraft.codegen.projection.ProjectionMetadataProcessor;
import nl.datasteel.crudcraft.codegen.writer.TestBasicGenerator;
import nl.datasteel.crudcraft.codegen.writer.TestCrudGenerator;
import nl.datasteel.crudcraft.codegen.writer.WriterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class CrudCraftProcessorIntegrationTest {

    private static final String INSOMNIA_OUTPUT_DIR =
            "-Acrudcraft.insomnia.outputDir=target/compile-testing-insomnia";

    @Test
    void consumerCompileGeneratesCrudSurfaceSearchAndProjectionMetadata() throws IOException {
        Compilation compilation =
                CompilationTestUtils.javac("-proc:only", INSOMNIA_OUTPUT_DIR)
                        .withProcessors(
                                new CrudCraftProcessor(), new ProjectionMetadataProcessor())
                        .compile(product(), category(), address(), sku());

        assertSuccess(compilation);

        assertGenerated(compilation, "demo.consumer.dto.request.ProductRequestDto");
        assertGenerated(compilation, "demo.consumer.dto.response.ProductResponseDto");
        assertGenerated(compilation, "demo.consumer.dto.response.ProductListResponseDto");
        assertGenerated(compilation, "demo.consumer.dto.ref.ProductRef");
        assertGenerated(compilation, "demo.consumer.repository.ProductRepository");
        assertGenerated(compilation, "demo.consumer.mapper.ProductMapper");
        assertGenerated(compilation, "demo.consumer.service.ProductService");
        assertGenerated(compilation, "demo.consumer.controller.ProductController");
        assertGenerated(compilation, "demo.consumer.search.ProductSearchRequest");
        assertGenerated(compilation, "demo.consumer.search.ProductSpecification");
        assertGenerated(compilation, "demo.consumer.meta.ProductRelationshipMeta");
        assertGenerated(compilation, "demo.consumer.projection.GeneratedProjectionMetadataRegistry");

        String request = content(compilation, "demo.consumer.dto.request.ProductRequestDto");
        assertTrue(request.contains("private String name;"));
        assertTrue(request.contains("private UUID categoryId;"));
        assertTrue(request.contains("private Set<UUID> skuIds;"));
        assertTrue(request.contains("@NotBlank"));
        assertTrue(request.contains("@Size("));
        assertTrue(request.contains("FIELD_SECURITY_METADATA"));
        assertFalse(request.contains("private Category category;"));
        assertFalse(request.contains("private Set<Sku> skus;"));

        String response = content(compilation, "demo.consumer.dto.response.ProductResponseDto");
        assertTrue(response.contains("private CategoryRef category;"));
        assertTrue(response.contains("private Set<SkuRef> skus;"));
        assertTrue(response.contains("private String city;"));

        String controller = content(compilation, "demo.consumer.controller.ProductController");
        assertTrue(controller.contains("@RequestMapping(\"/products\")"));
        assertTrue(controller.contains("private static final Logger LOG"));
        assertTrue(controller.contains("ObjectProvider<MeterRegistry> meterRegistry"));
        assertTrue(controller.contains("Timer.builder(\"crudcraft.generated.operation\")"));
        assertTrue(controller.contains("@GetMapping(\"/search\")"));
        assertTrue(controller.contains("ProductSearchRequest searchRequest, Pageable pageable"));
        assertTrue(controller.contains("recordOperation(\"SEARCH\", _crudcraftOutcome, _crudcraftStarted);"));
        assertFalse(controller.contains("@RequestParam(\"limit\") Integer limit"));
        assertFalse(controller.contains("if (limit == null || limit <= 0)"));
        assertTrue(
                controller.contains(
                        "ResponseEntity<PaginatedResponse<ProductListResponseDto>> getAllList("));
        assertTrue(controller.contains("@PostMapping(\"/batch\")"));
        assertTrue(controller.contains("@PostMapping(\"/validate\")"));
        assertFalse(controller.contains("@GetMapping(\"/export\")"));

        String search = content(compilation, "demo.consumer.search.ProductSearchRequest");
        assertTrue(search.contains("\"name\""));
        assertTrue(search.contains("\"categoryLabel\""));
        assertTrue(search.contains("SearchLogic.OR"));
        assertTrue(search.contains("requestedSearchCriteria"));

        String registry =
                content(
                        compilation,
                        "demo.consumer.projection.GeneratedProjectionMetadataRegistry");
        assertTrue(registry.contains("@Component"));
        assertTrue(registry.contains("ProductResponseDtoProjectionMetadata"));
        assertTrue(registry.contains("ProductListResponseDtoProjectionMetadata"));
    }

    @Test
    void abstractEntityOnlyGeneratesReferenceDto() throws IOException {
        Compilation compilation =
                CompilationTestUtils.javac("-proc:only", INSOMNIA_OUTPUT_DIR)
                        .withProcessors(new CrudCraftProcessor())
                        .compile(abstractContent());

        assertSuccess(compilation);
        assertGenerated(compilation, "demo.abstracts.dto.ref.ContentBaseRef");
        assertNotGenerated(compilation, "demo.abstracts.repository.ContentBaseRepository");
        assertNotGenerated(compilation, "demo.abstracts.mapper.ContentBaseMapper");
        assertNotGenerated(compilation, "demo.abstracts.service.ContentBaseService");
        assertNotGenerated(compilation, "demo.abstracts.controller.ContentBaseController");
    }

    @Test
    void readOnlyTemplateAndIncludeExportShapeGeneratedController() throws IOException {
        Compilation compilation =
                CompilationTestUtils.javac("-proc:only", INSOMNIA_OUTPUT_DIR)
                        .withProcessors(new CrudCraftProcessor())
                        .compile(readOnlyLookup());

        assertSuccess(compilation);
        String controller = content(compilation, "demo.lookup.controller.LookupController");
        assertTrue(controller.contains("@RequestMapping(\"/lookups\")"));
        assertTrue(controller.contains("@GetMapping(\"/export\")"));
        assertTrue(controller.contains("@GetMapping(\"/ref\")"));
        assertTrue(controller.contains("@GetMapping(\"/count\")"));
        assertFalse(controller.contains("@GetMapping(\"/search\")"));
        assertFalse(controller.contains("public ResponseEntity<LookupResponseDto> post"));
        assertFalse(controller.contains("@PatchMapping"));
        assertFalse(controller.contains("@DeleteMapping"));
    }

    @Test
    void basePackageMovesGeneratedStubsWithoutMovingModelDtos() throws IOException {
        Compilation compilation =
                CompilationTestUtils.javac("-proc:only", INSOMNIA_OUTPUT_DIR)
                        .withProcessors(new CrudCraftProcessor())
                        .compile(basePackageProduct());

        assertSuccess(compilation);

        assertGenerated(compilation, "demo.base.model.dto.request.ProductRequestDto");
        assertGenerated(compilation, "demo.base.model.dto.response.ProductResponseDto");
        assertGenerated(compilation, "demo.base.model.search.ProductSearchRequest");
        assertGenerated(compilation, "demo.custom.repository.ProductRepository");
        assertGenerated(compilation, "demo.custom.mapper.ProductMapper");
        assertGenerated(compilation, "demo.custom.service.ProductService");
        assertGenerated(compilation, "demo.custom.controller.ProductController");
        assertNotGenerated(compilation, "demo.base.repository.ProductRepository");
        assertNotGenerated(compilation, "demo.base.controller.ProductController");

        String controller = content(compilation, "demo.custom.controller.ProductController");
        assertTrue(controller.contains("import demo.base.model.dto.response.ProductResponseDto;"));
        assertTrue(controller.contains("import demo.base.model.search.ProductSearchRequest;"));
        assertTrue(controller.contains("import demo.custom.service.ProductService;"));
        assertTrue(controller.contains("@GetMapping(\"/export\")"));
    }

    @Test
    void customEndpointPolicyAndOmitIncludeDecideGeneratedControllerShape() throws IOException {
        Compilation compilation =
                CompilationTestUtils.javac("-proc:only", INSOMNIA_OUTPUT_DIR)
                        .withProcessors(new CrudCraftProcessor())
                        .compile(policyDrivenProduct());

        assertSuccess(compilation);

        String controller = content(compilation, "demo.policy.controller.PolicyProductController");
        assertTrue(controller.contains("@GetMapping(\"/count\")"));
        assertFalse(controller.contains("getOne(@PathVariable"));
        assertFalse(controller.contains("public ResponseEntity<PolicyProductResponseDto> post"));
        assertFalse(controller.contains("@GetMapping(\"/search\")"));
        assertFalse(controller.contains("@DeleteMapping"));
    }

    @Test
    void rejectsEndpointIncludedAndOmittedAtSameTime() {
        JavaFileObject broken =
                JavaFileObjects.forSourceLines(
                        "demo.conflict.ConflictingProduct",
                        "package demo.conflict;",
                        "import jakarta.persistence.Entity;",
                        "import jakarta.persistence.Id;",
                        "import java.util.UUID;",
                        "import nl.datasteel.crudcraft.annotations.CrudEndpoint;",
                        "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                        "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                        "@Entity",
                        "@CrudCrafted(",
                        "  omitEndpoints = CrudEndpoint.GET_ONE,",
                        "  includeEndpoints = CrudEndpoint.GET_ONE)",
                        "public class ConflictingProduct {",
                        "  @Id @Dto(ref = true) private UUID id;",
                        "  @Dto private String name;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac().withProcessors(new CrudCraftProcessor()).compile(broken);

        assertEquals(Compilation.Status.FAILURE, compilation.status());
        assertTrue(
                compilation.errors().stream()
                        .anyMatch(
                                error ->
                                        error.getMessage(null)
                                                .contains(
                                                        "Endpoint GET_ONE cannot appear in both"
                                                                + " omitEndpoints and"
                                                                + " includeEndpoints")));
    }

    @Test
    void rejectsExplicitBulkEndpointWhenSingleCounterpartIsOmitted() {
        JavaFileObject broken =
                JavaFileObjects.forSourceLines(
                        "demo.conflict.BulkOnlyProduct",
                        "package demo.conflict;",
                        "import jakarta.persistence.Entity;",
                        "import jakarta.persistence.Id;",
                        "import java.util.UUID;",
                        "import nl.datasteel.crudcraft.annotations.CrudEndpoint;",
                        "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                        "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                        "@Entity",
                        "@CrudCrafted(",
                        "  omitEndpoints = CrudEndpoint.POST,",
                        "  includeEndpoints = CrudEndpoint.BULK_CREATE)",
                        "public class BulkOnlyProduct {",
                        "  @Id @Dto(ref = true) private UUID id;",
                        "  @Dto private String name;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac().withProcessors(new CrudCraftProcessor()).compile(broken);

        assertEquals(Compilation.Status.FAILURE, compilation.status());
        assertTrue(
                compilation.errors().stream()
                        .anyMatch(
                                error ->
                                        error.getMessage(null)
                                                .contains(
                                                        "BULK_CREATE cannot be explicitly included"
                                                                + " while its single-item"
                                                                + " counterpart POST is"
                                                                + " explicitly omitted")));
    }

    @Test
    void processorUsesRegisteredGeneratorsFromWriterRegistry() throws Exception {
        setWriterRegistry(List.of(new TestBasicGenerator()), List.of(new TestCrudGenerator()));
        TestBasicGenerator.writes = 0;
        TestBasicGenerator.written = false;
        TestCrudGenerator.writes = 0;
        TestCrudGenerator.written = false;

        try {
            Compilation compilation =
                    CompilationTestUtils.javac("-proc:only", INSOMNIA_OUTPUT_DIR)
                            .withProcessors(new CrudCraftProcessor())
                            .compile(serviceLoaderProduct());

            assertSuccess(compilation);
            assertTrue(TestBasicGenerator.written);
            assertTrue(TestCrudGenerator.written);
        } finally {
            resetWriterRegistry();
        }
    }

    @Test
    void malformedModelIsReportedAsCompilerFailure() {
        JavaFileObject broken =
                JavaFileObjects.forSourceLines(
                        "demo.bad.Broken",
                        "package demo.bad;",
                        "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                        "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                        "@CrudCrafted",
                        "public class Broken {",
                        "  @Dto MissingType value;",
                        "}");

        Compilation compilation =
                CompilationTestUtils.javac().withProcessors(new CrudCraftProcessor()).compile(broken);

        assertEquals(Compilation.Status.FAILURE, compilation.status());
        assertTrue(
                compilation.diagnostics().stream()
                        .anyMatch(diagnostic -> diagnostic.getMessage(null).contains("MissingType")));
    }

    @Test
    void dtoFieldOrderIsDeterministicAcrossRepeatedGeneration() throws IOException {
        Compilation first =
                CompilationTestUtils.javac("-proc:only", INSOMNIA_OUTPUT_DIR)
                        .withProcessors(new CrudCraftProcessor())
                        .compile(product(), category(), address(), sku());
        Compilation second =
                CompilationTestUtils.javac("-proc:only", INSOMNIA_OUTPUT_DIR)
                        .withProcessors(new CrudCraftProcessor())
                        .compile(product(), category(), address(), sku());

        assertSuccess(first);
        assertSuccess(second);

        String firstRequest = content(first, "demo.consumer.dto.request.ProductRequestDto");
        String secondRequest = content(second, "demo.consumer.dto.request.ProductRequestDto");
        assertEquals(fieldDeclarationOrder(firstRequest), fieldDeclarationOrder(secondRequest));
    }

    @Test
    void dtoFieldOrderIsDeterministic() throws IOException {
        dtoFieldOrderIsDeterministicAcrossRepeatedGeneration();
    }

    @Test
    void supportsEntityNamesWithUnderscoresAndDigits() throws IOException {
        Compilation compilation =
                CompilationTestUtils.javac("-proc:only", INSOMNIA_OUTPUT_DIR)
                        .withProcessors(new CrudCraftProcessor())
                        .compile(specialNameEntity());

        assertSuccess(compilation);
        assertGenerated(compilation, "demo.special.dto.request.Report_2024RequestDto");
        assertGenerated(compilation, "demo.special.controller.Report_2024Controller");

        String controller = content(compilation, "demo.special.controller.Report_2024Controller");
        assertTrue(controller.contains("@RequestMapping(\"/"));
    }

    @Test
    void autoServiceFilesExposeBothAnnotationProcessors() throws IOException {
        Path services =
                Path.of(
                        "target",
                        "classes",
                        "META-INF",
                        "services",
                        "javax.annotation.processing.Processor");

        assertTrue(Files.exists(services));
        String content = Files.readString(services);
        assertTrue(content.contains(CrudCraftProcessor.class.getName()));
        assertTrue(content.contains(ProjectionMetadataProcessor.class.getName()));
    }

    @Test
    void nonUuidIdTypesGenerateCorrectSignaturesAndRoundTripCompile() throws IOException {
        Compilation compilation =
                CompilationTestUtils.javac("-proc:only", INSOMNIA_OUTPUT_DIR)
                        .withProcessors(new CrudCraftProcessor())
                        .compile(longOrder(), longCustomer(), naturalKeyDevice(), secureLongInvoice());

        assertSuccess(compilation);

        assertGenerated(compilation, "demo.nonuuid.dto.request.LongOrderRequestDto");
        assertGenerated(compilation, "demo.nonuuid.repository.LongOrderRepository");
        assertGenerated(compilation, "demo.nonuuid.mapper.LongOrderMapper");
        assertGenerated(compilation, "demo.nonuuid.service.LongOrderService");
        assertGenerated(compilation, "demo.nonuuid.controller.LongOrderController");

        assertGenerated(compilation, "demo.nonuuid.dto.request.NaturalKeyDeviceRequestDto");
        assertGenerated(compilation, "demo.nonuuid.repository.NaturalKeyDeviceRepository");
        assertGenerated(compilation, "demo.nonuuid.mapper.NaturalKeyDeviceMapper");
        assertGenerated(compilation, "demo.nonuuid.service.NaturalKeyDeviceService");
        assertGenerated(compilation, "demo.nonuuid.controller.NaturalKeyDeviceController");
        assertGenerated(compilation, "demo.nonuuid.controller.SecureLongInvoiceController");

        String longRequest = content(compilation, "demo.nonuuid.dto.request.LongOrderRequestDto");
        String longRepository = content(compilation, "demo.nonuuid.repository.LongOrderRepository");
        String longMapper = content(compilation, "demo.nonuuid.mapper.LongOrderMapper");
        String longService = content(compilation, "demo.nonuuid.service.LongOrderService");
        String longController = content(compilation, "demo.nonuuid.controller.LongOrderController");

        assertTrue(longRequest.contains("private Long customerId;"));
        assertTrue(longRepository.contains("JpaRepository<LongOrder, Long>"));
        assertTrue(
                longMapper.contains(
                        "EntityMapper<LongOrder, LongOrderRequestDto, LongOrderResponseDto, LongOrderRef, Long>"));
        assertTrue(
                longService.contains(
                        "AbstractCrudService<LongOrder, LongOrderRequestDto, LongOrderResponseDto, LongOrderRef, Long>"));
        assertTrue(longController.contains("getOne(@PathVariable Long id)"));
        assertTrue(longController.contains("@PathVariable Long id"));
        assertTrue(longController.contains("@DeleteMapping(\"/{id}\")"));
        assertFalse(longRequest.contains("UUID customerId"));
        assertFalse(longRepository.contains("UUID"));
        assertFalse(longMapper.contains("UUID"));
        assertFalse(longService.contains("UUID"));
        assertFalse(longController.contains("@PathVariable UUID id"));

        String stringRequest =
                content(compilation, "demo.nonuuid.dto.request.NaturalKeyDeviceRequestDto");
        String stringRepository =
                content(compilation, "demo.nonuuid.repository.NaturalKeyDeviceRepository");
        String stringMapper = content(compilation, "demo.nonuuid.mapper.NaturalKeyDeviceMapper");
        String stringService = content(compilation, "demo.nonuuid.service.NaturalKeyDeviceService");
        String stringController =
                content(compilation, "demo.nonuuid.controller.NaturalKeyDeviceController");

        assertTrue(stringRepository.contains("JpaRepository<NaturalKeyDevice, String>"));
        assertTrue(
                stringMapper.contains(
                        "EntityMapper<NaturalKeyDevice, NaturalKeyDeviceRequestDto, NaturalKeyDeviceResponseDto, NaturalKeyDeviceRef, String>"));
        assertTrue(
                stringService.contains(
                        "AbstractCrudService<NaturalKeyDevice, NaturalKeyDeviceRequestDto, NaturalKeyDeviceResponseDto, NaturalKeyDeviceRef, String>"));
        assertTrue(stringController.contains("getOne(@PathVariable String id)"));
        assertTrue(stringController.contains("@PathVariable String id"));
        assertTrue(stringController.contains("@DeleteMapping(\"/{id}\")"));
        assertFalse(stringRequest.contains("UUID"));
        assertFalse(stringRepository.contains("UUID"));
        assertFalse(stringMapper.contains("UUID"));
        assertFalse(stringService.contains("UUID"));
        assertFalse(stringController.contains("@PathVariable UUID id"));

        String secureController =
                content(compilation, "demo.nonuuid.controller.SecureLongInvoiceController");
        assertTrue(
                secureController.contains(
                        "ResponseEntity<PaginatedResponse<SecureLongInvoiceListResponseDto>>"
                                + " getAllList("));
        assertTrue(secureController.contains("ResponseEntity<SecureLongInvoiceListResponseDto>"
                + " getListById("));
        assertTrue(secureController.contains("getListById(@PathVariable Long id)"));
        assertTrue(
                secureController.contains(
                        "@PreAuthorize(\"isAuthenticated()\")\n"
                                + "    public ResponseEntity<PaginatedResponse<"
                                + "SecureLongInvoiceListResponseDto>> getAllList("));
        assertTrue(
                secureController.contains(
                        "@PreAuthorize(\"isAuthenticated()\")\n"
                                + "    public ResponseEntity<SecureLongInvoiceListResponseDto>"
                                + " getListById("));
        assertFalse(secureController.contains("@PathVariable UUID id"));

        List<JavaFileObject> roundTripSources = new ArrayList<>();
        roundTripSources.add(longOrder());
        roundTripSources.add(longCustomer());
        roundTripSources.add(naturalKeyDevice());
        roundTripSources.add(secureLongInvoice());
        roundTripSources.addAll(compilation.generatedSourceFiles());
        Compilation roundTrip =
                CompilationTestUtils.javac("-proc:none", "-Xlint:all").compile(roundTripSources);
        assertSuccess(roundTrip);
    }

    private static void assertGenerated(Compilation compilation, String className) {
        assertTrue(compilation.generatedSourceFile(className).isPresent(), className);
    }

    private static void assertSuccess(Compilation compilation) {
        assertEquals(
                Compilation.Status.SUCCESS,
                compilation.status(),
                () -> compilation.diagnostics().toString());
    }

    private static void assertNotGenerated(Compilation compilation, String className) {
        assertFalse(compilation.generatedSourceFile(className).isPresent(), className);
    }

    private static String content(Compilation compilation, String className) throws IOException {
        return compilation
                .generatedSourceFile(className)
                .orElseThrow()
                .getCharContent(false)
                .toString();
    }

    private static List<String> fieldDeclarationOrder(String source) {
        return source.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("private ") && line.endsWith(";"))
                .toList();
    }

    private static void resetWriterRegistry() throws Exception {
        Method clear = WriterRegistry.class.getDeclaredMethod("clearGeneratorOverridesForTests");
        clear.setAccessible(true);
        clear.invoke(null);
    }

    private static void setWriterRegistry(List<?> basicGenerators, List<?> crudGenerators)
            throws Exception {
        List<Object> combined = new java.util.ArrayList<>();
        if (basicGenerators != null) {
            combined.addAll(basicGenerators);
        }
        if (crudGenerators != null) {
            combined.addAll(crudGenerators);
        }
        Method setter =
                WriterRegistry.class.getDeclaredMethod(
                        "setGeneratorOverridesForTests", List.class);
        setter.setAccessible(true);
        setter.invoke(null, combined);
    }

    private static JavaFileObject product() {
        return JavaFileObjects.forSourceLines(
                "demo.consumer.Product",
                "package demo.consumer;",
                "import jakarta.persistence.Embedded;",
                "import jakarta.persistence.Entity;",
                "import jakarta.persistence.Id;",
                "import jakarta.persistence.ManyToMany;",
                "import jakarta.persistence.ManyToOne;",
                "import jakarta.validation.constraints.NotBlank;",
                "import jakarta.validation.constraints.Size;",
                "import java.util.Set;",
                "import java.util.UUID;",
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "import nl.datasteel.crudcraft.annotations.fields.Request;",
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;",
                "@Entity",
                "@CrudCrafted",
                "public class Product {",
                "  @Id @Dto(ref = true) private UUID id;",
                "  @Dto({\"List\"}) @Request @Searchable @NotBlank @Size(min = 2, max = 80)",
                "  private String name;",
                "  @Dto @Request @Embedded private Address address;",
                "  @Dto @Request @Searchable(depth = 2) @ManyToOne private Category category;",
                "  @Dto @Request @ManyToMany private Set<Sku> skus;",
                "}");
    }

    private static JavaFileObject category() {
        return JavaFileObjects.forSourceLines(
                "demo.consumer.Category",
                "package demo.consumer;",
                "import jakarta.persistence.Entity;",
                "import jakarta.persistence.Id;",
                "import java.util.UUID;",
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;",
                "@Entity",
                "@CrudCrafted",
                "public class Category {",
                "  @Id @Dto(ref = true) private UUID id;",
                "  @Dto @Searchable private String label;",
                "}");
    }

    private static JavaFileObject sku() {
        return JavaFileObjects.forSourceLines(
                "demo.consumer.Sku",
                "package demo.consumer;",
                "import jakarta.persistence.Entity;",
                "import jakarta.persistence.Id;",
                "import java.util.UUID;",
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "@Entity",
                "@CrudCrafted",
                "public class Sku {",
                "  @Id @Dto(ref = true) private UUID id;",
                "  @Dto private String code;",
                "}");
    }

    private static JavaFileObject address() {
        return JavaFileObjects.forSourceLines(
                "demo.consumer.Address",
                "package demo.consumer;",
                "import jakarta.persistence.Embeddable;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "import nl.datasteel.crudcraft.annotations.fields.Request;",
                "@Embeddable",
                "public class Address {",
                "  @Dto @Request private String city;",
                "}");
    }

    private static JavaFileObject abstractContent() {
        return JavaFileObjects.forSourceLines(
                "demo.abstracts.ContentBase",
                "package demo.abstracts;",
                "import jakarta.persistence.Id;",
                "import jakarta.persistence.MappedSuperclass;",
                "import java.util.UUID;",
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "@MappedSuperclass",
                "@CrudCrafted",
                "public abstract class ContentBase {",
                "  @Id @Dto(ref = true) private UUID id;",
                "  @Dto private String title;",
                "}");
    }

    private static JavaFileObject readOnlyLookup() {
        return JavaFileObjects.forSourceLines(
                "demo.lookup.Lookup",
                "package demo.lookup;",
                "import jakarta.persistence.Entity;",
                "import jakarta.persistence.Id;",
                "import java.util.UUID;",
                "import nl.datasteel.crudcraft.annotations.CrudEndpoint;",
                "import nl.datasteel.crudcraft.annotations.CrudTemplate;",
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "@Entity",
                "@CrudCrafted(template = CrudTemplate.READ_ONLY, includeEndpoints = CrudEndpoint.EXPORT)",
                "public class Lookup {",
                "  @Id @Dto(ref = true) private UUID id;",
                "  @Dto private String name;",
                "}");
    }

    private static JavaFileObject basePackageProduct() {
        return JavaFileObjects.forSourceLines(
                "demo.base.model.Product",
                "package demo.base.model;",
                "import jakarta.persistence.Entity;",
                "import jakarta.persistence.Id;",
                "import java.util.UUID;",
                "import nl.datasteel.crudcraft.annotations.CrudEndpoint;",
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "import nl.datasteel.crudcraft.annotations.fields.Request;",
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;",
                "@Entity",
                "@CrudCrafted(basePackage = \"demo.custom\", includeEndpoints = CrudEndpoint.EXPORT)",
                "public class Product {",
                "  @Id @Dto(ref = true) private UUID id;",
                "  @Dto @Request @Searchable private String name;",
                "}");
    }

    private static JavaFileObject policyDrivenProduct() {
        return JavaFileObjects.forSourceLines(
                "demo.policy.PolicyProduct",
                "package demo.policy;",
                "import jakarta.persistence.Entity;",
                "import jakarta.persistence.Id;",
                "import java.util.UUID;",
                "import nl.datasteel.crudcraft.annotations.CrudEndpoint;",
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "import nl.datasteel.crudcraft.codegen.reader.TestEndpointPolicy;",
                "@Entity",
                "@CrudCrafted(",
                "  endpointPolicy = TestEndpointPolicy.class,",
                "  omitEndpoints = CrudEndpoint.GET_ONE,",
                "  includeEndpoints = CrudEndpoint.COUNT)",
                "public class PolicyProduct {",
                "  @Id @Dto(ref = true) private UUID id;",
                "  @Dto private String name;",
                "}");
    }

    private static JavaFileObject serviceLoaderProduct() {
        return JavaFileObjects.forSourceLines(
                "demo.loader.LoaderProduct",
                "package demo.loader;",
                "import jakarta.persistence.Entity;",
                "import jakarta.persistence.Id;",
                "import java.util.UUID;",
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "@Entity",
                "@CrudCrafted",
                "public class LoaderProduct {",
                "  @Id @Dto(ref = true) private UUID id;",
                "  @Dto private String name;",
                "}");
    }

    private static JavaFileObject specialNameEntity() {
        return JavaFileObjects.forSourceLines(
                "demo.special.Report_2024",
                "package demo.special;",
                "import jakarta.persistence.Entity;",
                "import jakarta.persistence.Id;",
                "import java.util.UUID;",
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "@Entity",
                "@CrudCrafted",
                "public class Report_2024 {",
                "  @Id @Dto(ref = true) private UUID id;",
                "  @Dto private String title;",
                "}");
    }

    private static JavaFileObject longOrder() {
        return JavaFileObjects.forSourceLines(
                "demo.nonuuid.LongOrder",
                "package demo.nonuuid;",
                "import jakarta.persistence.Entity;",
                "import jakarta.persistence.Id;",
                "import jakarta.persistence.ManyToOne;",
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "import nl.datasteel.crudcraft.annotations.fields.Request;",
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;",
                "@Entity",
                "@CrudCrafted",
                "public class LongOrder {",
                "  @Id @Dto(ref = true) private Long id;",
                "  @Dto @Request @Searchable private String title;",
                "  @Dto @Request @ManyToOne private LongCustomer customer;",
                "}");
    }

    private static JavaFileObject longCustomer() {
        return JavaFileObjects.forSourceLines(
                "demo.nonuuid.LongCustomer",
                "package demo.nonuuid;",
                "import jakarta.persistence.Entity;",
                "import jakarta.persistence.Id;",
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "@Entity",
                "@CrudCrafted",
                "public class LongCustomer {",
                "  @Id @Dto(ref = true) private Long id;",
                "  @Dto private String name;",
                "  public void setId(Long id) { this.id = id; }",
                "  public Long getId() { return id; }",
                "}");
    }

    private static JavaFileObject naturalKeyDevice() {
        return JavaFileObjects.forSourceLines(
                "demo.nonuuid.NaturalKeyDevice",
                "package demo.nonuuid;",
                "import jakarta.persistence.Entity;",
                "import jakarta.persistence.Id;",
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "import nl.datasteel.crudcraft.annotations.fields.Request;",
                "import nl.datasteel.crudcraft.annotations.fields.Searchable;",
                "@Entity",
                "@CrudCrafted",
                "public class NaturalKeyDevice {",
                "  @Id @Dto(ref = true) private String id;",
                "  @Dto @Request @Searchable private String label;",
                "}");
    }

    private static JavaFileObject secureLongInvoice() {
        return JavaFileObjects.forSourceLines(
                "demo.nonuuid.SecureLongInvoice",
                "package demo.nonuuid;",
                "import jakarta.persistence.Entity;",
                "import jakarta.persistence.Id;",
                "import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;",
                "import nl.datasteel.crudcraft.annotations.fields.Dto;",
                "import nl.datasteel.crudcraft.annotations.fields.Request;",
                "@Entity",
                "@CrudCrafted(secure = true)",
                "public class SecureLongInvoice {",
                "  @Id @Dto(ref = true) private Long id;",
                "  @Dto({\"List\"}) @Request private String name;",
                "}");
    }
}

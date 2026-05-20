package demo.golden.readonly.controller;

import demo.golden.readonly.Lookup;
import demo.golden.readonly.dto.ref.LookupRef;
import demo.golden.readonly.dto.response.LookupResponseDto;
import demo.golden.readonly.service.LookupService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import nl.datasteel.crudcraft.runtime.export.service.EnhancedExportService;
import nl.datasteel.crudcraft.runtime.export.service.EnhancedExportServiceFactory;
import nl.datasteel.crudcraft.runtime.export.service.ExportService;
import nl.datasteel.crudcraft.runtime.service.CrudQueryOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Generated model file for Lookup; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: Lookup
 * - Package: demo.golden.readonly.controller
 * - Generator: ControllerGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * To make changes, edit the entity model class and rebuild the project.
 * Do not edit or rename this file manually.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@SuppressWarnings("PMD")
@RestController
@RequestMapping("/lookups")
public class LookupController {
    private static final Logger LOG = LoggerFactory.getLogger(LookupController.class);

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final LookupService service;

    private final MeterRegistry meterRegistry;

    protected int maxPageSize;

    protected int maxRows;

    protected int maxCsvRows;

    protected int maxJsonRows;

    protected int maxXlsxRows;

    protected int maxDepth;

    private final EnhancedExportService<LookupResponseDto, Lookup, Object> exportService;

    @SuppressFBWarnings(
            value = "CT_CONSTRUCTOR_THROW",
            justification = "Generated controllers may validate optional infrastructure during construction."
    )
    public LookupController(LookupService service, ObjectProvider<MeterRegistry> meterRegistry,
            @Value("${crudcraft.api.max-page-size:100}") int maxPageSize,
            @Value("${crudcraft.export.max-rows:-1}") int maxRows,
            @Value("${crudcraft.export.max-csv-rows:100000}") int maxCsvRows,
            @Value("${crudcraft.export.max-json-rows:50000}") int maxJsonRows,
            @Value("${crudcraft.export.max-xlsx-rows:25000}") int maxXlsxRows,
            @Value("${crudcraft.export.max-depth:5}") int maxDepth,
            ObjectProvider<EnhancedExportServiceFactory> exportServiceFactoryProvider) {
        if (maxPageSize <= 0) {
            throw new IllegalArgumentException("crudcraft.api.max-page-size must be positive; got " + maxPageSize);
        }
        this.service = service;
        this.meterRegistry = resolveMeterRegistry(meterRegistry);
        this.maxPageSize = maxPageSize;
        this.maxRows = maxRows;
        this.maxCsvRows = maxCsvRows;
        this.maxJsonRows = maxJsonRows;
        this.maxXlsxRows = maxXlsxRows;
        this.maxDepth = maxDepth;
        this.exportService = createExportService(exportServiceFactoryProvider, maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize, Lookup.class);
    }

    private static MeterRegistry resolveMeterRegistry(ObjectProvider<MeterRegistry> provider) {
        if (provider == null) {
            return null;
        }
        try {
            return provider.getIfAvailable();
        } catch (RuntimeException ex) {
            LOG.debug("Micrometer registry lookup failed; generated controller metrics disabled", ex);
            return null;
        }
    }

    private Pageable clampPageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, maxPageSize);
        }
        int size = Math.clamp(pageable.getPageSize(), 1, maxPageSize);
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }

    private void recordOperation(String operation, String outcome, long started) {
        long durationNanos = System.nanoTime() - started;
        LOG.info("crudcraft.generated.operation model={} operation={} outcome={} duration_ms={}", "Lookup", operation, outcome, TimeUnit.NANOSECONDS.toMillis(durationNanos));
        if (meterRegistry != null) {
            Timer.builder("crudcraft.generated.operation").tag("model", "Lookup").tag("operation", operation).tag("outcome", outcome).register(meterRegistry).record(durationNanos, TimeUnit.NANOSECONDS);
        }
    }

    @SuppressWarnings("unchecked")
    private static EnhancedExportService<LookupResponseDto, Lookup, Object> createExportService(
            ObjectProvider<EnhancedExportServiceFactory> provider, int maxRows, int maxCsvRows,
            int maxJsonRows, int maxXlsxRows, int maxPageSize, Class<Lookup> entityType) {
        if (provider != null) {
            EnhancedExportServiceFactory factory = provider.getIfAvailable();
            if (factory != null) {
                return factory.create(maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize, entityType);
            }
        }
        return new EnhancedExportService<>(new ExportService.ExportConfig(maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize), null, entityType, false);
    }

    @SuppressWarnings("unchecked")
    private Specification<Lookup> effectiveReadSpecification(Object searchRequest) {
        return ((CrudQueryOperations<Lookup, ?, ?>) service).effectiveReadSpecification(searchRequest);
    }

    @GetMapping
    @Operation(
            operationId = "lookupGetAll",
            summary = "Get all Lookup entities with pagination",
            description = "Retrieves all Lookup entities with support for pagination, sorting, and filtering via search parameters."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of Lookup entities"))
    public ResponseEntity<PaginatedResponse<LookupResponseDto>> getAll(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<LookupResponseDto> page = service.findAll(clampPageable(pageable));
            PaginatedResponse<LookupResponseDto> response = new PaginatedResponse<>(
                page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalPages(), page.getTotalElements(),
                page.isFirst(), page.isLast()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("GET_ALL", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @GetMapping("/ref")
    @Operation(
            operationId = "lookupGetAllRef",
            summary = "Get all Lookup references with pagination",
            description = "Retrieves lightweight references to Lookup entities with support for pagination and filtering."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of Lookup references"))
    public ResponseEntity<PaginatedResponse<LookupRef>> getAllRef(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<LookupRef> page = service.findAllRef(clampPageable(pageable));
            PaginatedResponse<LookupRef> response = new PaginatedResponse<>(
                page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalPages(), page.getTotalElements(),
                page.isFirst(), page.isLast()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("GET_ALL_REF", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "lookupGetOne",
            summary = "Get a single Lookup by ID",
            description = "Retrieves a single Lookup entity by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lookup retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Lookup with the specified ID was not found")
    })
    public ResponseEntity<LookupResponseDto> getOne(@PathVariable UUID id) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            LookupResponseDto dto = service.findById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("GET_ONE", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PostMapping("/batch/ids")
    @Operation(
            operationId = "lookupFindByIds",
            summary = "Find Lookup entities by IDs",
            description = "Retrieves multiple Lookup entities by their IDs in a single request."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lookup entities retrieved successfully"))
    public ResponseEntity<PaginatedResponse<LookupResponseDto>> findByIds(
            @RequestBody List<UUID> ids) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            var dtos = service.findByIds(ids);
            PaginatedResponse<LookupResponseDto> response = new PaginatedResponse<>(
                dtos,
                0,
                dtos.size(),
                1,
                dtos.size(),
                true,
                true
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("FIND_BY_IDS", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @RequestMapping(
            value = "/exists/{id}",
            method = {RequestMethod.HEAD, RequestMethod.GET}
    )
    @Operation(
            operationId = "lookupExists",
            summary = "Check if a Lookup exists",
            description = "Checks whether a Lookup entity with the given ID exists in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lookup existence check result"),
            @ApiResponse(responseCode = "404", description = "Lookup with the specified ID was not found")
    })
    public ResponseEntity<Void> exists(@PathVariable("id") UUID id) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            return service.existsById(id)
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("EXISTS", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @GetMapping("/count")
    @Operation(
            operationId = "lookupCount",
            summary = "Count Lookup entities",
            description = "Counts the total number of Lookup entities matching the search criteria."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Count of Lookup entities"))
    public ResponseEntity<Map<String, Long>> count() {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            long total = service.count();
            return ResponseEntity.ok(Map.of("count", total));
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("COUNT", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @GetMapping("/export")
    @Operation(
            operationId = "lookupExport",
            summary = "Export Lookup entities",
            description = "Exports Lookup entities in the requested format."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lookup data exported"))
    public ResponseEntity<StreamingResponseBody> export(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "format", required = true) String format,
            @ModelAttribute ExportRequest exportRequest) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            return exportService.export(
                null,
                limit,
                format,
                exportRequest,
                pageable -> service.findAll(pageable),
                Function.identity(),
                effectiveReadSpecification(null)
            );
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("EXPORT", _crudcraftOutcome, _crudcraftStarted);
        }
    }
}

package demo.golden.endpointmatrix.controller;

import demo.golden.endpointmatrix.NoDeleteRecord;
import demo.golden.endpointmatrix.dto.ref.NoDeleteRecordRef;
import demo.golden.endpointmatrix.dto.request.NoDeleteRecordRequestDto;
import demo.golden.endpointmatrix.dto.response.NoDeleteRecordResponseDto;
import demo.golden.endpointmatrix.search.NoDeleteRecordSearchRequest;
import demo.golden.endpointmatrix.service.NoDeleteRecordService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import nl.datasteel.crudcraft.runtime.Identified;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import nl.datasteel.crudcraft.runtime.export.service.EnhancedExportService;
import nl.datasteel.crudcraft.runtime.export.service.EnhancedExportServiceFactory;
import nl.datasteel.crudcraft.runtime.export.service.ExportService;
import nl.datasteel.crudcraft.runtime.search.SearchOperations;
import nl.datasteel.crudcraft.runtime.service.BulkResult;
import nl.datasteel.crudcraft.runtime.service.CrudQueryOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Generated model file for NoDeleteRecord; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: NoDeleteRecord
 * - Package: demo.golden.endpointmatrix.controller
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
@RestController
@RequestMapping("/nodeleterecords")
public class NoDeleteRecordController {
    private static final Logger LOG = LoggerFactory.getLogger(NoDeleteRecordController.class);

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final NoDeleteRecordService service;

    private final MeterRegistry meterRegistry;

    protected int maxPageSize;

    protected int maxRows;

    protected int maxCsvRows;

    protected int maxJsonRows;

    protected int maxXlsxRows;

    protected int maxDepth;

    private final EnhancedExportService<NoDeleteRecordResponseDto, NoDeleteRecord, NoDeleteRecordSearchRequest> exportService;

    @SuppressFBWarnings(
            value = "CT_CONSTRUCTOR_THROW",
            justification = "Generated controllers may validate optional infrastructure during construction."
    )
    public NoDeleteRecordController(NoDeleteRecordService service,
            ObjectProvider<MeterRegistry> meterRegistry,
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
        this.exportService = createExportService(exportServiceFactoryProvider, maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize, NoDeleteRecord.class);
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
        LOG.info("crudcraft.generated.operation model={} operation={} outcome={} duration_ms={}", "NoDeleteRecord", operation, outcome, TimeUnit.NANOSECONDS.toMillis(durationNanos));
        if (meterRegistry != null) {
            Timer.builder("crudcraft.generated.operation").tag("model", "NoDeleteRecord").tag("operation", operation).tag("outcome", outcome).register(meterRegistry).record(durationNanos, TimeUnit.NANOSECONDS);
        }
    }

    @SuppressWarnings("unchecked")
    private static EnhancedExportService<NoDeleteRecordResponseDto, NoDeleteRecord, NoDeleteRecordSearchRequest> createExportService(
            ObjectProvider<EnhancedExportServiceFactory> provider, int maxRows, int maxCsvRows,
            int maxJsonRows, int maxXlsxRows, int maxPageSize, Class<NoDeleteRecord> entityType) {
        if (provider != null) {
            EnhancedExportServiceFactory factory = provider.getIfAvailable();
            if (factory != null) {
                return factory.create(maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize, entityType);
            }
        }
        return new EnhancedExportService<>(new ExportService.ExportConfig(maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize), null, entityType, false);
    }

    @SuppressWarnings("unchecked")
    private Specification<NoDeleteRecord> effectiveReadSpecification(Object searchRequest) {
        return ((CrudQueryOperations<NoDeleteRecord, ?, ?>) service).effectiveReadSpecification(searchRequest);
    }

    @GetMapping
    @Operation(
            operationId = "noDeleteRecordGetAll",
            summary = "Get all NoDeleteRecord entities with pagination",
            description = "Retrieves all NoDeleteRecord entities with support for pagination, sorting, and filtering via search parameters."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of NoDeleteRecord entities"))
    public ResponseEntity<PaginatedResponse<NoDeleteRecordResponseDto>> getAll(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<NoDeleteRecordResponseDto> page = service.findAll(clampPageable(pageable));
            PaginatedResponse<NoDeleteRecordResponseDto> response = new PaginatedResponse<>(
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
            operationId = "noDeleteRecordGetAllRef",
            summary = "Get all NoDeleteRecord references with pagination",
            description = "Retrieves lightweight references to NoDeleteRecord entities with support for pagination and filtering."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of NoDeleteRecord references"))
    public ResponseEntity<PaginatedResponse<NoDeleteRecordRef>> getAllRef(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<NoDeleteRecordRef> page = service.findAllRef(clampPageable(pageable));
            PaginatedResponse<NoDeleteRecordRef> response = new PaginatedResponse<>(
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
            operationId = "noDeleteRecordGetOne",
            summary = "Get a single NoDeleteRecord by ID",
            description = "Retrieves a single NoDeleteRecord entity by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoDeleteRecord retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "NoDeleteRecord with the specified ID was not found")
    })
    public ResponseEntity<NoDeleteRecordResponseDto> getOne(@PathVariable UUID id) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            NoDeleteRecordResponseDto dto = service.findById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("GET_ONE", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PostMapping
    @Operation(
            operationId = "noDeleteRecordCreate",
            summary = "Create a new NoDeleteRecord",
            description = "Creates a new NoDeleteRecord entity with the provided data. Returns the created entity with generated ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "NoDeleteRecord created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<NoDeleteRecordResponseDto> post(
            @Valid @NotNull @RequestBody NoDeleteRecordRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            NoDeleteRecordResponseDto created = service.create(request);
            return ResponseEntity.status(201).body(created);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("POST", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PutMapping("/{id}")
    @Operation(
            operationId = "noDeleteRecordUpdate",
            summary = "Update an existing NoDeleteRecord",
            description = "Updates an existing NoDeleteRecord entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoDeleteRecord updated successfully"),
            @ApiResponse(responseCode = "404", description = "NoDeleteRecord with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<NoDeleteRecordResponseDto> update(@PathVariable("id") UUID id,
            @Valid @NotNull @RequestBody NoDeleteRecordRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            NoDeleteRecordResponseDto updated = service.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("PUT", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PatchMapping("/{id}")
    @Operation(
            operationId = "noDeleteRecordPatch",
            summary = "Partially update an existing NoDeleteRecord",
            description = "Partially updates an existing NoDeleteRecord entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoDeleteRecord partially updated successfully"),
            @ApiResponse(responseCode = "404", description = "NoDeleteRecord with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<NoDeleteRecordResponseDto> patch(@PathVariable("id") UUID id,
            @NotNull @RequestBody NoDeleteRecordRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            NoDeleteRecordResponseDto patched = service.patch(id, request);
            return ResponseEntity.ok(patched);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("PATCH", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PostMapping("/batch")
    @Operation(
            operationId = "noDeleteRecordBulkCreate",
            summary = "Create multiple NoDeleteRecord entities",
            description = "Creates multiple NoDeleteRecord entities in a single request. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "NoDeleteRecord entities created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    public ResponseEntity<BulkResult<NoDeleteRecordResponseDto>> createAll(
            @Valid @NotNull @RequestBody List<NoDeleteRecordRequestDto> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            BulkResult<NoDeleteRecordResponseDto> result = service.createAllResult(requests);
            return ResponseEntity.status(result.hasFailures()
                    ? HttpStatus.MULTI_STATUS
                    : HttpStatus.CREATED)
                .body(result);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("BULK_CREATE", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PutMapping("/batch")
    @Operation(
            operationId = "noDeleteRecordBulkUpdate",
            summary = "Update multiple NoDeleteRecord entities",
            description = "Updates multiple NoDeleteRecord entities in a single request. Each entity must include its ID. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoDeleteRecord entities updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    public ResponseEntity<BulkResult<NoDeleteRecordResponseDto>> updateAll(
            @Valid @NotNull @RequestBody List<Identified<UUID, NoDeleteRecordRequestDto>> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            BulkResult<NoDeleteRecordResponseDto> result = service.updateAllResult(requests);
            return ResponseEntity.status(result.hasFailures()
                    ? HttpStatus.MULTI_STATUS
                    : HttpStatus.OK)
                .body(result);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("BULK_UPDATE", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PatchMapping("/batch")
    @Operation(
            operationId = "noDeleteRecordBulkPatch",
            summary = "Partially update multiple NoDeleteRecord entities",
            description = "Partially updates multiple NoDeleteRecord entities in a single request. Each entity must include its ID. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoDeleteRecord entities partially updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    public ResponseEntity<BulkResult<NoDeleteRecordResponseDto>> patchAll(
            @Valid @NotNull @RequestBody List<Identified<UUID, NoDeleteRecordRequestDto>> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            BulkResult<NoDeleteRecordResponseDto> result = service.patchAllResult(requests);
            return ResponseEntity.status(result.hasFailures()
                    ? HttpStatus.MULTI_STATUS
                    : HttpStatus.OK)
                .body(result);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("BULK_PATCH", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PostMapping("/batch/upsert")
    @Operation(
            operationId = "noDeleteRecordBulkUpsert",
            summary = "Create or update multiple NoDeleteRecord entities",
            description = "Creates or updates multiple NoDeleteRecord entities in a single request. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "NoDeleteRecord entities created or updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    public ResponseEntity<BulkResult<NoDeleteRecordResponseDto>> upsertAll(
            @RequestBody Collection<NoDeleteRecordRequestDto> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            BulkResult<NoDeleteRecordResponseDto> result = service.upsertAllResult(requests);
            return ResponseEntity.status(result.hasFailures()
                    ? HttpStatus.MULTI_STATUS
                    : HttpStatus.CREATED)
                .body(result);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("BULK_UPSERT", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PostMapping("/batch/ids")
    @Operation(
            operationId = "noDeleteRecordFindByIds",
            summary = "Find NoDeleteRecord entities by IDs",
            description = "Retrieves multiple NoDeleteRecord entities by their IDs in a single request."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "NoDeleteRecord entities retrieved successfully"))
    public ResponseEntity<PaginatedResponse<NoDeleteRecordResponseDto>> findByIds(
            @RequestBody List<UUID> ids) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            var dtos = service.findByIds(ids);
            PaginatedResponse<NoDeleteRecordResponseDto> response = new PaginatedResponse<>(
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
            operationId = "noDeleteRecordExists",
            summary = "Check if a NoDeleteRecord exists",
            description = "Checks whether a NoDeleteRecord entity with the given ID exists in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoDeleteRecord existence check result"),
            @ApiResponse(responseCode = "404", description = "NoDeleteRecord with the specified ID was not found")
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
            operationId = "noDeleteRecordCount",
            summary = "Count NoDeleteRecord entities",
            description = "Counts the total number of NoDeleteRecord entities matching the search criteria."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Count of NoDeleteRecord entities"))
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

    @PostMapping("/validate")
    @Operation(
            operationId = "noDeleteRecordValidate",
            summary = "Validate NoDeleteRecord data",
            description = "Validates NoDeleteRecord data without persisting it. Returns validation errors if any."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Validation results"))
    public ResponseEntity<Void> validate(
            @Valid @NotNull @RequestBody NoDeleteRecordRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("VALIDATE", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @GetMapping("/export")
    @Operation(
            operationId = "noDeleteRecordExport",
            summary = "Export NoDeleteRecord entities",
            description = "Exports NoDeleteRecord entities in the requested format."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "NoDeleteRecord data exported"))
    public ResponseEntity<StreamingResponseBody> export(
            @ModelAttribute NoDeleteRecordSearchRequest searchRequest,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "format", required = true) String format,
            @ModelAttribute ExportRequest exportRequest) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            return exportService.export(
                searchRequest,
                limit,
                format,
                exportRequest,
                pageable -> SearchOperations.search(service, searchRequest, pageable, NoDeleteRecordResponseDto.class),
                Function.identity(),
                effectiveReadSpecification(searchRequest)
            );
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("EXPORT", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @GetMapping("/search")
    @Operation(
            operationId = "noDeleteRecordSearch",
            summary = "Search NoDeleteRecord entities",
            description = "Searches for NoDeleteRecord entities based on the provided search criteria."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Search results for NoDeleteRecord entities"))
    public ResponseEntity<PaginatedResponse<NoDeleteRecordResponseDto>> search(
            @ModelAttribute NoDeleteRecordSearchRequest searchRequest, Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Pageable clamped = clampPageable(pageable);
            Page<NoDeleteRecordResponseDto> page = SearchOperations.search(service, searchRequest, clamped, NoDeleteRecordResponseDto.class);
            PaginatedResponse<NoDeleteRecordResponseDto> response = new PaginatedResponse<>(
                page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalPages(), page.getTotalElements(),
                page.isFirst(), page.isLast()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("SEARCH", _crudcraftOutcome, _crudcraftStarted);
        }
    }
}

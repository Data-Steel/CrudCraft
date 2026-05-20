package demo.golden.endpointmatrix.controller;

import demo.golden.endpointmatrix.dto.ref.NoBatchTicketRef;
import demo.golden.endpointmatrix.dto.request.NoBatchTicketRequestDto;
import demo.golden.endpointmatrix.dto.response.NoBatchTicketResponseDto;
import demo.golden.endpointmatrix.service.NoBatchTicketService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Generated model file for NoBatchTicket; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: NoBatchTicket
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
@SuppressWarnings("PMD")
@RestController
@RequestMapping("/nobatchtickets")
public class NoBatchTicketController {
    private static final Logger LOG = LoggerFactory.getLogger(NoBatchTicketController.class);

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final NoBatchTicketService service;

    private final MeterRegistry meterRegistry;

    protected int maxPageSize;

    protected int maxRows;

    protected int maxCsvRows;

    protected int maxJsonRows;

    protected int maxXlsxRows;

    protected int maxDepth;

    @SuppressFBWarnings(
            value = "CT_CONSTRUCTOR_THROW",
            justification = "Generated controllers may validate optional infrastructure during construction."
    )
    public NoBatchTicketController(NoBatchTicketService service,
            ObjectProvider<MeterRegistry> meterRegistry,
            @Value("${crudcraft.api.max-page-size:100}") int maxPageSize,
            @Value("${crudcraft.export.max-rows:-1}") int maxRows,
            @Value("${crudcraft.export.max-csv-rows:100000}") int maxCsvRows,
            @Value("${crudcraft.export.max-json-rows:50000}") int maxJsonRows,
            @Value("${crudcraft.export.max-xlsx-rows:25000}") int maxXlsxRows,
            @Value("${crudcraft.export.max-depth:5}") int maxDepth) {
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
        LOG.info("crudcraft.generated.operation model={} operation={} outcome={} duration_ms={}", "NoBatchTicket", operation, outcome, TimeUnit.NANOSECONDS.toMillis(durationNanos));
        if (meterRegistry != null) {
            Timer.builder("crudcraft.generated.operation").tag("model", "NoBatchTicket").tag("operation", operation).tag("outcome", outcome).register(meterRegistry).record(durationNanos, TimeUnit.NANOSECONDS);
        }
    }

    @GetMapping
    @Operation(
            operationId = "noBatchTicketGetAll",
            summary = "Get all NoBatchTicket entities with pagination",
            description = "Retrieves all NoBatchTicket entities with support for pagination, sorting, and filtering via search parameters."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of NoBatchTicket entities"))
    public ResponseEntity<PaginatedResponse<NoBatchTicketResponseDto>> getAll(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<NoBatchTicketResponseDto> page = service.findAll(clampPageable(pageable));
            PaginatedResponse<NoBatchTicketResponseDto> response = new PaginatedResponse<>(
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
            operationId = "noBatchTicketGetAllRef",
            summary = "Get all NoBatchTicket references with pagination",
            description = "Retrieves lightweight references to NoBatchTicket entities with support for pagination and filtering."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of NoBatchTicket references"))
    public ResponseEntity<PaginatedResponse<NoBatchTicketRef>> getAllRef(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<NoBatchTicketRef> page = service.findAllRef(clampPageable(pageable));
            PaginatedResponse<NoBatchTicketRef> response = new PaginatedResponse<>(
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
            operationId = "noBatchTicketGetOne",
            summary = "Get a single NoBatchTicket by ID",
            description = "Retrieves a single NoBatchTicket entity by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoBatchTicket retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "NoBatchTicket with the specified ID was not found")
    })
    public ResponseEntity<NoBatchTicketResponseDto> getOne(@PathVariable UUID id) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            NoBatchTicketResponseDto dto = service.findById(id);
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
            operationId = "noBatchTicketCreate",
            summary = "Create a new NoBatchTicket",
            description = "Creates a new NoBatchTicket entity with the provided data. Returns the created entity with generated ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "NoBatchTicket created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<NoBatchTicketResponseDto> post(
            @Valid @NotNull @RequestBody NoBatchTicketRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            NoBatchTicketResponseDto created = service.create(request);
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
            operationId = "noBatchTicketUpdate",
            summary = "Update an existing NoBatchTicket",
            description = "Updates an existing NoBatchTicket entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoBatchTicket updated successfully"),
            @ApiResponse(responseCode = "404", description = "NoBatchTicket with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<NoBatchTicketResponseDto> update(@PathVariable("id") UUID id,
            @Valid @NotNull @RequestBody NoBatchTicketRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            NoBatchTicketResponseDto updated = service.update(id, request);
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
            operationId = "noBatchTicketPatch",
            summary = "Partially update an existing NoBatchTicket",
            description = "Partially updates an existing NoBatchTicket entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoBatchTicket partially updated successfully"),
            @ApiResponse(responseCode = "404", description = "NoBatchTicket with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<NoBatchTicketResponseDto> patch(@PathVariable("id") UUID id,
            @NotNull @RequestBody NoBatchTicketRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            NoBatchTicketResponseDto patched = service.patch(id, request);
            return ResponseEntity.ok(patched);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("PATCH", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            operationId = "noBatchTicketDelete",
            summary = "Delete a NoBatchTicket",
            description = "Permanently deletes a NoBatchTicket entity identified by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "NoBatchTicket deleted successfully"),
            @ApiResponse(responseCode = "404", description = "NoBatchTicket with the specified ID was not found")
    })
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("DELETE", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @RequestMapping(
            value = "/exists/{id}",
            method = {RequestMethod.HEAD, RequestMethod.GET}
    )
    @Operation(
            operationId = "noBatchTicketExists",
            summary = "Check if a NoBatchTicket exists",
            description = "Checks whether a NoBatchTicket entity with the given ID exists in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NoBatchTicket existence check result"),
            @ApiResponse(responseCode = "404", description = "NoBatchTicket with the specified ID was not found")
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
            operationId = "noBatchTicketCount",
            summary = "Count NoBatchTicket entities",
            description = "Counts the total number of NoBatchTicket entities matching the search criteria."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Count of NoBatchTicket entities"))
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
            operationId = "noBatchTicketValidate",
            summary = "Validate NoBatchTicket data",
            description = "Validates NoBatchTicket data without persisting it. Returns validation errors if any."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Validation results"))
    public ResponseEntity<Void> validate(
            @Valid @NotNull @RequestBody NoBatchTicketRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            request.getClass();
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("VALIDATE", _crudcraftOutcome, _crudcraftStarted);
        }
    }
}

package demo.golden.endpointmatrix.controller;

import demo.golden.endpointmatrix.dto.ref.SecureInternalSecretRef;
import demo.golden.endpointmatrix.dto.request.SecureInternalSecretRequestDto;
import demo.golden.endpointmatrix.dto.response.SecureInternalSecretResponseDto;
import demo.golden.endpointmatrix.service.SecureInternalSecretService;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Generated model file for SecureInternalSecret; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: SecureInternalSecret
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
@RequestMapping("/secureinternalsecrets")
public class SecureInternalSecretController {
    private static final Logger LOG = LoggerFactory.getLogger(SecureInternalSecretController.class);

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final SecureInternalSecretService service;

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
    public SecureInternalSecretController(SecureInternalSecretService service,
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
        LOG.info("crudcraft.generated.operation model={} operation={} outcome={} duration_ms={}", "SecureInternalSecret", operation, outcome, TimeUnit.NANOSECONDS.toMillis(durationNanos));
        if (meterRegistry != null) {
            Timer.builder("crudcraft.generated.operation").tag("model", "SecureInternalSecret").tag("operation", operation).tag("outcome", outcome).register(meterRegistry).record(durationNanos, TimeUnit.NANOSECONDS);
        }
    }

    @GetMapping
    @Operation(
            operationId = "secureInternalSecretGetAll",
            summary = "Get all SecureInternalSecret entities with pagination",
            description = "Retrieves all SecureInternalSecret entities with support for pagination, sorting, and filtering via search parameters."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of SecureInternalSecret entities"))
    @PreAuthorize("hasAuthority('ENDPOINT_GET_ALL')")
    public ResponseEntity<PaginatedResponse<SecureInternalSecretResponseDto>> getAll(
            Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<SecureInternalSecretResponseDto> page = service.findAll(clampPageable(pageable));
            PaginatedResponse<SecureInternalSecretResponseDto> response = new PaginatedResponse<>(
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
            operationId = "secureInternalSecretGetAllRef",
            summary = "Get all SecureInternalSecret references with pagination",
            description = "Retrieves lightweight references to SecureInternalSecret entities with support for pagination and filtering."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of SecureInternalSecret references"))
    @PreAuthorize("hasAuthority('ENDPOINT_GET_ALL_REF')")
    public ResponseEntity<PaginatedResponse<SecureInternalSecretRef>> getAllRef(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<SecureInternalSecretRef> page = service.findAllRef(clampPageable(pageable));
            PaginatedResponse<SecureInternalSecretRef> response = new PaginatedResponse<>(
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
            operationId = "secureInternalSecretGetOne",
            summary = "Get a single SecureInternalSecret by ID",
            description = "Retrieves a single SecureInternalSecret entity by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SecureInternalSecret retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "SecureInternalSecret with the specified ID was not found")
    })
    @PreAuthorize("hasAuthority('ENDPOINT_GET_ONE')")
    public ResponseEntity<SecureInternalSecretResponseDto> getOne(@PathVariable UUID id) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            SecureInternalSecretResponseDto dto = service.findById(id);
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
            operationId = "secureInternalSecretCreate",
            summary = "Create a new SecureInternalSecret",
            description = "Creates a new SecureInternalSecret entity with the provided data. Returns the created entity with generated ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "SecureInternalSecret created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("hasAuthority('ENDPOINT_POST')")
    public ResponseEntity<SecureInternalSecretResponseDto> post(
            @Valid @NotNull @RequestBody SecureInternalSecretRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            SecureInternalSecretResponseDto created = service.create(request);
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
            operationId = "secureInternalSecretUpdate",
            summary = "Update an existing SecureInternalSecret",
            description = "Updates an existing SecureInternalSecret entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SecureInternalSecret updated successfully"),
            @ApiResponse(responseCode = "404", description = "SecureInternalSecret with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("hasAuthority('ENDPOINT_PUT')")
    public ResponseEntity<SecureInternalSecretResponseDto> update(@PathVariable("id") UUID id,
            @Valid @NotNull @RequestBody SecureInternalSecretRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            SecureInternalSecretResponseDto updated = service.update(id, request);
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
            operationId = "secureInternalSecretPatch",
            summary = "Partially update an existing SecureInternalSecret",
            description = "Partially updates an existing SecureInternalSecret entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SecureInternalSecret partially updated successfully"),
            @ApiResponse(responseCode = "404", description = "SecureInternalSecret with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("hasAuthority('ENDPOINT_PATCH')")
    public ResponseEntity<SecureInternalSecretResponseDto> patch(@PathVariable("id") UUID id,
            @NotNull @RequestBody SecureInternalSecretRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            SecureInternalSecretResponseDto patched = service.patch(id, request);
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
            operationId = "secureInternalSecretDelete",
            summary = "Delete a SecureInternalSecret",
            description = "Permanently deletes a SecureInternalSecret entity identified by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "SecureInternalSecret deleted successfully"),
            @ApiResponse(responseCode = "404", description = "SecureInternalSecret with the specified ID was not found")
    })
    @PreAuthorize("hasAuthority('ENDPOINT_DELETE')")
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
            operationId = "secureInternalSecretExists",
            summary = "Check if a SecureInternalSecret exists",
            description = "Checks whether a SecureInternalSecret entity with the given ID exists in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SecureInternalSecret existence check result"),
            @ApiResponse(responseCode = "404", description = "SecureInternalSecret with the specified ID was not found")
    })
    @PreAuthorize("hasAuthority('ENDPOINT_EXISTS')")
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
            operationId = "secureInternalSecretCount",
            summary = "Count SecureInternalSecret entities",
            description = "Counts the total number of SecureInternalSecret entities matching the search criteria."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Count of SecureInternalSecret entities"))
    @PreAuthorize("hasAuthority('ENDPOINT_COUNT')")
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
            operationId = "secureInternalSecretValidate",
            summary = "Validate SecureInternalSecret data",
            description = "Validates SecureInternalSecret data without persisting it. Returns validation errors if any."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Validation results"))
    @PreAuthorize("hasAuthority('ENDPOINT_VALIDATE')")
    public ResponseEntity<Void> validate(
            @Valid @NotNull @RequestBody SecureInternalSecretRequestDto request) {
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
}

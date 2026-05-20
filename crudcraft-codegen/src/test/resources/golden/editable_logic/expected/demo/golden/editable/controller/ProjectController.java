package demo.golden.editable.controller;

import demo.golden.editable.dto.ref.ProjectRef;
import demo.golden.editable.dto.request.ProjectRequestDto;
import demo.golden.editable.dto.response.ProjectResponseDto;
import demo.golden.editable.service.ProjectService;
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
import nl.datasteel.crudcraft.runtime.Identified;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
import nl.datasteel.crudcraft.runtime.service.BulkResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
 * Generated Controller layer stub for Project.
 * @CrudCraft:generated
 * @CrudCraft:editable
 *
 * This Controller stub extends CrudCraft's base implementation. Override protected hooks and add custom endpoints here. Avoid overriding generated public endpoint methods unless you intentionally replace the HTTP contract.
 *
 * You are allowed to modify this file. It extends CrudCraft's abstract base (ProjectControllerBase)
 * which already implements full CRUD logic.
 *
 * This file was generated only once. CrudCraft will not overwrite it in future
 * builds. If you delete it, it will be regenerated.
 *
 * Features provided by CrudCraft:
 * - Standard CRUD workflow already implemented
 * - DTO mapping and repository calls wired up
 *
 * Generation context:
 * - Source model: Project
 * - Package: demo.golden.editable.controller
 * - Generator: ControllerGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * Recommendations:
 * - You may customize method behavior, add validation, or extend with additional endpoints.
 * - Signature changes are allowed, but may desync from service or mapper layer—proceed with care.
 * - Do not manually copy or paste other CrudCraft stubs into this class.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@SuppressWarnings("PMD")
@RestController
@RequestMapping("/projects")
public class ProjectController {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final ProjectService service;

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
    public ProjectController(ProjectService service, ObjectProvider<MeterRegistry> meterRegistry,
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
        LOG.info("crudcraft.generated.operation model={} operation={} outcome={} duration_ms={}", "Project", operation, outcome, TimeUnit.NANOSECONDS.toMillis(durationNanos));
        if (meterRegistry != null) {
            Timer.builder("crudcraft.generated.operation").tag("model", "Project").tag("operation", operation).tag("outcome", outcome).register(meterRegistry).record(durationNanos, TimeUnit.NANOSECONDS);
        }
    }

    @GetMapping
    @Operation(
            operationId = "projectGetAll",
            summary = "Get all Project entities with pagination",
            description = "Retrieves all Project entities with support for pagination, sorting, and filtering via search parameters."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of Project entities"))
    public ResponseEntity<PaginatedResponse<ProjectResponseDto>> getAll(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<ProjectResponseDto> page = service.findAll(clampPageable(pageable));
            PaginatedResponse<ProjectResponseDto> response = new PaginatedResponse<>(
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
            operationId = "projectGetAllRef",
            summary = "Get all Project references with pagination",
            description = "Retrieves lightweight references to Project entities with support for pagination and filtering."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of Project references"))
    public ResponseEntity<PaginatedResponse<ProjectRef>> getAllRef(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<ProjectRef> page = service.findAllRef(clampPageable(pageable));
            PaginatedResponse<ProjectRef> response = new PaginatedResponse<>(
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
            operationId = "projectGetOne",
            summary = "Get a single Project by ID",
            description = "Retrieves a single Project entity by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project with the specified ID was not found")
    })
    public ResponseEntity<ProjectResponseDto> getOne(@PathVariable UUID id) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            ProjectResponseDto dto = service.findById(id);
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
            operationId = "projectCreate",
            summary = "Create a new Project",
            description = "Creates a new Project entity with the provided data. Returns the created entity with generated ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ProjectResponseDto> post(
            @Valid @NotNull @RequestBody ProjectRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            ProjectResponseDto created = service.create(request);
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
            operationId = "projectUpdate",
            summary = "Update an existing Project",
            description = "Updates an existing Project entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ProjectResponseDto> update(@PathVariable("id") UUID id,
            @Valid @NotNull @RequestBody ProjectRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            ProjectResponseDto updated = service.update(id, request);
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
            operationId = "projectPatch",
            summary = "Partially update an existing Project",
            description = "Partially updates an existing Project entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project partially updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ProjectResponseDto> patch(@PathVariable("id") UUID id,
            @NotNull @RequestBody ProjectRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            ProjectResponseDto patched = service.patch(id, request);
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
            operationId = "projectDelete",
            summary = "Delete a Project",
            description = "Permanently deletes a Project entity identified by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project with the specified ID was not found")
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

    @PostMapping("/batch")
    @Operation(
            operationId = "projectBulkCreate",
            summary = "Create multiple Project entities",
            description = "Creates multiple Project entities in a single request. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project entities created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    public ResponseEntity<BulkResult<ProjectResponseDto>> createAll(
            @Valid @NotNull @RequestBody List<ProjectRequestDto> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            BulkResult<ProjectResponseDto> result = service.createAllResult(requests);
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
            operationId = "projectBulkUpdate",
            summary = "Update multiple Project entities",
            description = "Updates multiple Project entities in a single request. Each entity must include its ID. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project entities updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    public ResponseEntity<BulkResult<ProjectResponseDto>> updateAll(
            @Valid @NotNull @RequestBody List<Identified<UUID, ProjectRequestDto>> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            BulkResult<ProjectResponseDto> result = service.updateAllResult(requests);
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
            operationId = "projectBulkPatch",
            summary = "Partially update multiple Project entities",
            description = "Partially updates multiple Project entities in a single request. Each entity must include its ID. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project entities partially updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    public ResponseEntity<BulkResult<ProjectResponseDto>> patchAll(
            @Valid @NotNull @RequestBody List<Identified<UUID, ProjectRequestDto>> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            BulkResult<ProjectResponseDto> result = service.patchAllResult(requests);
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
            operationId = "projectBulkUpsert",
            summary = "Create or update multiple Project entities",
            description = "Creates or updates multiple Project entities in a single request. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project entities created or updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    public ResponseEntity<BulkResult<ProjectResponseDto>> upsertAll(
            @RequestBody Collection<ProjectRequestDto> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            BulkResult<ProjectResponseDto> result = service.upsertAllResult(requests);
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

    @DeleteMapping("/batch/delete")
    @Operation(
            operationId = "projectBulkDelete",
            summary = "Delete multiple Project entities",
            description = "Permanently deletes multiple Project entities by their IDs. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project entities deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    public ResponseEntity<BulkResult<UUID>> deleteAllByIds(@RequestBody Collection<UUID> ids) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            BulkResult<UUID> result = service.deleteAllByIdsResult(ids);
            return ResponseEntity.status(result.hasFailures()
                    ? HttpStatus.MULTI_STATUS
                    : HttpStatus.OK)
                .body(result);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("BULK_DELETE", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PostMapping("/batch/ids")
    @Operation(
            operationId = "projectFindByIds",
            summary = "Find Project entities by IDs",
            description = "Retrieves multiple Project entities by their IDs in a single request."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Project entities retrieved successfully"))
    public ResponseEntity<PaginatedResponse<ProjectResponseDto>> findByIds(
            @RequestBody List<UUID> ids) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            var dtos = service.findByIds(ids);
            PaginatedResponse<ProjectResponseDto> response = new PaginatedResponse<>(
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
            operationId = "projectExists",
            summary = "Check if a Project exists",
            description = "Checks whether a Project entity with the given ID exists in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project existence check result"),
            @ApiResponse(responseCode = "404", description = "Project with the specified ID was not found")
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
            operationId = "projectCount",
            summary = "Count Project entities",
            description = "Counts the total number of Project entities matching the search criteria."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Count of Project entities"))
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
            operationId = "projectValidate",
            summary = "Validate Project data",
            description = "Validates Project data without persisting it. Returns validation errors if any."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Validation results"))
    public ResponseEntity<Void> validate(@Valid @NotNull @RequestBody ProjectRequestDto request) {
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

    /*
    Endpoint omitted by generation template (+ include/exclude). Since this stub is editable, it is commented out, so it can easily be added later.
    @org.springframework.web.bind.annotation.GetMapping("/export")
    @io.swagger.v3.oas.annotations.Operation(
        operationId = "projectExport",
        summary = "Export Project entities",
        description = "Exports Project entities in the requested format."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project data exported"))
    public org.springframework.http.ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody> export(
        @org.springframework.web.bind.annotation.RequestParam(value = "limit", required = false) java.lang.Integer limit,
        @org.springframework.web.bind.annotation.RequestParam(value = "format", required = true) java.lang.String format,
        @org.springframework.web.bind.annotation.ModelAttribute nl.datasteel.crudcraft.runtime.export.ExportRequest exportRequest) {
      long _crudcraftStarted = java.lang.System.nanoTime();
      String _crudcraftOutcome = "success";
      try {
        return exportService.export(
            null,
            limit,
            format,
            exportRequest,
            pageable -> service.findAll(pageable),
            java.util.function.Function.identity(),
            effectiveReadSpecification(null)
        );
      } catch (java.lang.RuntimeException ex) {
        _crudcraftOutcome = "error";
        throw ex;
      } finally {
        recordOperation("EXPORT", _crudcraftOutcome, _crudcraftStarted);
      }
    }
    */

}

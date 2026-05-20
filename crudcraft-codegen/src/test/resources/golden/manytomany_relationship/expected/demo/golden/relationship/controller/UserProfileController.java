package demo.golden.relationship.controller;

import demo.golden.relationship.dto.ref.UserProfileRef;
import demo.golden.relationship.dto.request.UserProfileRequestDto;
import demo.golden.relationship.dto.response.UserProfileResponseDto;
import demo.golden.relationship.service.UserProfileService;
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
 * Generated model file for UserProfile; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: UserProfile
 * - Package: demo.golden.relationship.controller
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
@RequestMapping("/userprofiles")
public class UserProfileController {
    private static final Logger LOG = LoggerFactory.getLogger(UserProfileController.class);

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final UserProfileService service;

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
    public UserProfileController(UserProfileService service,
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
        LOG.info("crudcraft.generated.operation model={} operation={} outcome={} duration_ms={}", "UserProfile", operation, outcome, TimeUnit.NANOSECONDS.toMillis(durationNanos));
        if (meterRegistry != null) {
            Timer.builder("crudcraft.generated.operation").tag("model", "UserProfile").tag("operation", operation).tag("outcome", outcome).register(meterRegistry).record(durationNanos, TimeUnit.NANOSECONDS);
        }
    }

    @GetMapping
    @Operation(
            operationId = "userProfileGetAll",
            summary = "Get all UserProfile entities with pagination",
            description = "Retrieves all UserProfile entities with support for pagination, sorting, and filtering via search parameters."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of UserProfile entities"))
    public ResponseEntity<PaginatedResponse<UserProfileResponseDto>> getAll(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<UserProfileResponseDto> page = service.findAll(clampPageable(pageable));
            PaginatedResponse<UserProfileResponseDto> response = new PaginatedResponse<>(
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
            operationId = "userProfileGetAllRef",
            summary = "Get all UserProfile references with pagination",
            description = "Retrieves lightweight references to UserProfile entities with support for pagination and filtering."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of UserProfile references"))
    public ResponseEntity<PaginatedResponse<UserProfileRef>> getAllRef(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<UserProfileRef> page = service.findAllRef(clampPageable(pageable));
            PaginatedResponse<UserProfileRef> response = new PaginatedResponse<>(
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
            operationId = "userProfileGetOne",
            summary = "Get a single UserProfile by ID",
            description = "Retrieves a single UserProfile entity by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UserProfile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "UserProfile with the specified ID was not found")
    })
    public ResponseEntity<UserProfileResponseDto> getOne(@PathVariable UUID id) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            UserProfileResponseDto dto = service.findById(id);
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
            operationId = "userProfileCreate",
            summary = "Create a new UserProfile",
            description = "Creates a new UserProfile entity with the provided data. Returns the created entity with generated ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "UserProfile created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<UserProfileResponseDto> post(
            @Valid @NotNull @RequestBody UserProfileRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            UserProfileResponseDto created = service.create(request);
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
            operationId = "userProfileUpdate",
            summary = "Update an existing UserProfile",
            description = "Updates an existing UserProfile entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UserProfile updated successfully"),
            @ApiResponse(responseCode = "404", description = "UserProfile with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<UserProfileResponseDto> update(@PathVariable("id") UUID id,
            @Valid @NotNull @RequestBody UserProfileRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            UserProfileResponseDto updated = service.update(id, request);
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
            operationId = "userProfilePatch",
            summary = "Partially update an existing UserProfile",
            description = "Partially updates an existing UserProfile entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UserProfile partially updated successfully"),
            @ApiResponse(responseCode = "404", description = "UserProfile with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<UserProfileResponseDto> patch(@PathVariable("id") UUID id,
            @NotNull @RequestBody UserProfileRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            UserProfileResponseDto patched = service.patch(id, request);
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
            operationId = "userProfileDelete",
            summary = "Delete a UserProfile",
            description = "Permanently deletes a UserProfile entity identified by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "UserProfile deleted successfully"),
            @ApiResponse(responseCode = "404", description = "UserProfile with the specified ID was not found")
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
            operationId = "userProfileBulkCreate",
            summary = "Create multiple UserProfile entities",
            description = "Creates multiple UserProfile entities in a single request. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "UserProfile entities created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    public ResponseEntity<BulkResult<UserProfileResponseDto>> createAll(
            @Valid @NotNull @RequestBody List<UserProfileRequestDto> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            BulkResult<UserProfileResponseDto> result = service.createAllResult(requests);
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
            operationId = "userProfileBulkUpdate",
            summary = "Update multiple UserProfile entities",
            description = "Updates multiple UserProfile entities in a single request. Each entity must include its ID. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UserProfile entities updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    public ResponseEntity<BulkResult<UserProfileResponseDto>> updateAll(
            @Valid @NotNull @RequestBody List<Identified<UUID, UserProfileRequestDto>> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            BulkResult<UserProfileResponseDto> result = service.updateAllResult(requests);
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
            operationId = "userProfileBulkPatch",
            summary = "Partially update multiple UserProfile entities",
            description = "Partially updates multiple UserProfile entities in a single request. Each entity must include its ID. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UserProfile entities partially updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    public ResponseEntity<BulkResult<UserProfileResponseDto>> patchAll(
            @Valid @NotNull @RequestBody List<Identified<UUID, UserProfileRequestDto>> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            BulkResult<UserProfileResponseDto> result = service.patchAllResult(requests);
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
            operationId = "userProfileBulkUpsert",
            summary = "Create or update multiple UserProfile entities",
            description = "Creates or updates multiple UserProfile entities in a single request. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "UserProfile entities created or updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    public ResponseEntity<BulkResult<UserProfileResponseDto>> upsertAll(
            @RequestBody Collection<UserProfileRequestDto> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            BulkResult<UserProfileResponseDto> result = service.upsertAllResult(requests);
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
            operationId = "userProfileBulkDelete",
            summary = "Delete multiple UserProfile entities",
            description = "Permanently deletes multiple UserProfile entities by their IDs. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UserProfile entities deleted successfully"),
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
            operationId = "userProfileFindByIds",
            summary = "Find UserProfile entities by IDs",
            description = "Retrieves multiple UserProfile entities by their IDs in a single request."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "UserProfile entities retrieved successfully"))
    public ResponseEntity<PaginatedResponse<UserProfileResponseDto>> findByIds(
            @RequestBody List<UUID> ids) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            var dtos = service.findByIds(ids);
            PaginatedResponse<UserProfileResponseDto> response = new PaginatedResponse<>(
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
            operationId = "userProfileExists",
            summary = "Check if a UserProfile exists",
            description = "Checks whether a UserProfile entity with the given ID exists in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UserProfile existence check result"),
            @ApiResponse(responseCode = "404", description = "UserProfile with the specified ID was not found")
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
            operationId = "userProfileCount",
            summary = "Count UserProfile entities",
            description = "Counts the total number of UserProfile entities matching the search criteria."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Count of UserProfile entities"))
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
            operationId = "userProfileValidate",
            summary = "Validate UserProfile data",
            description = "Validates UserProfile data without persisting it. Returns validation errors if any."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Validation results"))
    public ResponseEntity<Void> validate(
            @Valid @NotNull @RequestBody UserProfileRequestDto request) {
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

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
package demo.golden.umbrella.controller;

import demo.golden.umbrella.Account;
import demo.golden.umbrella.dto.ref.AccountRef;
import demo.golden.umbrella.dto.request.AccountRequestDto;
import demo.golden.umbrella.dto.response.AccountDetailResponseDto;
import demo.golden.umbrella.dto.response.AccountListResponseDto;
import demo.golden.umbrella.dto.response.AccountResponseDto;
import demo.golden.umbrella.search.AccountSearchRequest;
import demo.golden.umbrella.service.AccountService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import nl.datasteel.crudcraft.runtime.Identified;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import nl.datasteel.crudcraft.runtime.export.service.EnhancedExportService;
import nl.datasteel.crudcraft.runtime.export.service.EnhancedExportServiceFactory;
import nl.datasteel.crudcraft.runtime.export.service.ExportService;
import nl.datasteel.crudcraft.runtime.search.SearchOperations;
import nl.datasteel.crudcraft.runtime.security.FieldSecurityUtil;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Generated model file for Account; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: Account
 * - Package: demo.golden.umbrella.controller
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
@RequestMapping("/accounts")
public class AccountController {
    private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final AccountService service;

    private final MeterRegistry meterRegistry;

    protected int maxPageSize;

    protected int maxRows;

    protected int maxCsvRows;

    protected int maxJsonRows;

    protected int maxXlsxRows;

    protected int maxDepth;

    private final EnhancedExportService<AccountResponseDto, Account, AccountSearchRequest> exportService;

    @SuppressFBWarnings(
            value = "CT_CONSTRUCTOR_THROW",
            justification = "Generated controllers may validate optional infrastructure during construction."
    )
    public AccountController(AccountService service, ObjectProvider<MeterRegistry> meterRegistry,
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
        this.exportService = createExportService(exportServiceFactoryProvider, maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize, Account.class);
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
        LOG.info("crudcraft.generated.operation model={} operation={} outcome={} duration_ms={}", "Account", operation, outcome, TimeUnit.NANOSECONDS.toMillis(durationNanos));
        if (meterRegistry != null) {
            Timer.builder("crudcraft.generated.operation").tag("model", "Account").tag("operation", operation).tag("outcome", outcome).register(meterRegistry).record(durationNanos, TimeUnit.NANOSECONDS);
        }
    }

    @SuppressWarnings("unchecked")
    private static EnhancedExportService<AccountResponseDto, Account, AccountSearchRequest> createExportService(
            ObjectProvider<EnhancedExportServiceFactory> provider, int maxRows, int maxCsvRows,
            int maxJsonRows, int maxXlsxRows, int maxPageSize, Class<Account> entityType) {
        if (provider != null) {
            EnhancedExportServiceFactory factory = provider.getIfAvailable();
            if (factory != null) {
                return factory.create(maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize, entityType);
            }
        }
        return new EnhancedExportService<>(new ExportService.ExportConfig(maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize), null, entityType, false);
    }

    @SuppressWarnings("unchecked")
    private Specification<Account> effectiveReadSpecification(Object searchRequest) {
        return ((CrudQueryOperations<Account, ?, ?>) service).effectiveReadSpecification(searchRequest);
    }

    @GetMapping
    @Operation(
            operationId = "accountGetAll",
            summary = "Get all Account entities with pagination",
            description = "Retrieves all Account entities with support for pagination, sorting, and filtering via search parameters."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of Account entities"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<AccountResponseDto>> getAll(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<AccountResponseDto> page = service.findAll(clampPageable(pageable));
            Page<AccountResponseDto> dtoPage = page.map(FieldSecurityUtil::filterRead);
            PaginatedResponse<AccountResponseDto> response = new PaginatedResponse<>(
                dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
                dtoPage.getTotalPages(), dtoPage.getTotalElements(),
                dtoPage.isFirst(), dtoPage.isLast()
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
            operationId = "accountGetAllRef",
            summary = "Get all Account references with pagination",
            description = "Retrieves lightweight references to Account entities with support for pagination and filtering."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of Account references"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<AccountRef>> getAllRef(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Page<AccountRef> page = service.findAllRef(clampPageable(pageable));
            Page<AccountRef> dtoPage = page.map(FieldSecurityUtil::filterRead);
            PaginatedResponse<AccountRef> response = new PaginatedResponse<>(
                dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
                dtoPage.getTotalPages(), dtoPage.getTotalElements(),
                dtoPage.isFirst(), dtoPage.isLast()
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
            operationId = "accountGetOne",
            summary = "Get a single Account by ID",
            description = "Retrieves a single Account entity by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account with the specified ID was not found")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountResponseDto> getOne(@PathVariable UUID id) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            AccountResponseDto dto = service.findById(id);
            return ResponseEntity.ok(FieldSecurityUtil.filterRead(dto));
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("GET_ONE", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            operationId = "accountCreate",
            summary = "Create a new Account",
            description = "Creates a new Account entity with the provided data. Returns the created entity with generated ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountResponseDto> post(
            @Valid @NotNull @RequestPart("data") AccountRequestDto request,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            if (logo != null) {
                if (logo.isEmpty()) {
                    request = request.withLogo(null);
                } else {
                    try {
                        request = request.withLogo(logo.getBytes());
                    } catch (IOException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded file for field: " + "logo", e);
                    }
                }
            }
            request = FieldSecurityUtil.filterWrite(request);
            AccountResponseDto created = service.create(request);
            return ResponseEntity.status(201).body(FieldSecurityUtil.filterRead(created));
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("POST", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            operationId = "accountUpdate",
            summary = "Update an existing Account",
            description = "Updates an existing Account entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "404", description = "Account with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountResponseDto> update(@PathVariable("id") UUID id,
            @Valid @NotNull @RequestPart("data") AccountRequestDto request,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            if (logo != null) {
                if (logo.isEmpty()) {
                    request = request.withLogo(null);
                } else {
                    try {
                        request = request.withLogo(logo.getBytes());
                    } catch (IOException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded file for field: " + "logo", e);
                    }
                }
            }
            request = FieldSecurityUtil.filterWrite(request);
            AccountResponseDto updated = service.update(id, request);
            return ResponseEntity.ok(FieldSecurityUtil.filterRead(updated));
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("PUT", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PatchMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            operationId = "accountPatch",
            summary = "Partially update an existing Account",
            description = "Partially updates an existing Account entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account partially updated successfully"),
            @ApiResponse(responseCode = "404", description = "Account with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountResponseDto> patch(@PathVariable("id") UUID id,
            @NotNull @RequestPart("data") AccountRequestDto request,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            if (logo != null) {
                if (logo.isEmpty()) {
                    request = request.withLogo(null);
                } else {
                    try {
                        request = request.withLogo(logo.getBytes());
                    } catch (IOException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded file for field: " + "logo", e);
                    }
                }
            }
            request = FieldSecurityUtil.filterWrite(request);
            AccountResponseDto patched = service.patch(id, request);
            return ResponseEntity.ok(FieldSecurityUtil.filterRead(patched));
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("PATCH", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            operationId = "accountDelete",
            summary = "Delete a Account",
            description = "Permanently deletes a Account entity identified by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account with the specified ID was not found")
    })
    @PreAuthorize("isAuthenticated()")
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
            operationId = "accountBulkCreate",
            summary = "Create multiple Account entities",
            description = "Creates multiple Account entities in a single request. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account entities created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BulkResult<AccountResponseDto>> createAll(
            @Valid @NotNull @RequestBody List<AccountRequestDto> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            requests = requests.stream()
                .map(FieldSecurityUtil::filterWrite)
                .toList();
            BulkResult<AccountResponseDto> result = service.createAllResult(requests);
            result = new BulkResult<>(
                result.succeeded().stream()
                    .map(FieldSecurityUtil::filterRead)
                    .toList(),
                result.failed());
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
            operationId = "accountBulkUpdate",
            summary = "Update multiple Account entities",
            description = "Updates multiple Account entities in a single request. Each entity must include its ID. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account entities updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BulkResult<AccountResponseDto>> updateAll(
            @Valid @NotNull @RequestBody List<Identified<UUID, AccountRequestDto>> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            requests = requests.stream()
                .map(request -> new Identified<>(
                    request.getId(),
                    FieldSecurityUtil.filterWrite(request.getData())))
                .toList();
            BulkResult<AccountResponseDto> result = service.updateAllResult(requests);
            result = new BulkResult<>(
                result.succeeded().stream()
                    .map(FieldSecurityUtil::filterRead)
                    .toList(),
                result.failed());
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
            operationId = "accountBulkPatch",
            summary = "Partially update multiple Account entities",
            description = "Partially updates multiple Account entities in a single request. Each entity must include its ID. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account entities partially updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BulkResult<AccountResponseDto>> patchAll(
            @Valid @NotNull @RequestBody List<Identified<UUID, AccountRequestDto>> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            requests = requests.stream()
                .map(request -> new Identified<>(
                    request.getId(),
                    FieldSecurityUtil.filterWrite(request.getData())))
                .toList();
            BulkResult<AccountResponseDto> result = service.patchAllResult(requests);
            result = new BulkResult<>(
                result.succeeded().stream()
                    .map(FieldSecurityUtil::filterRead)
                    .toList(),
                result.failed());
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
            operationId = "accountBulkUpsert",
            summary = "Create or update multiple Account entities",
            description = "Creates or updates multiple Account entities in a single request. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account entities created or updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BulkResult<AccountResponseDto>> upsertAll(
            @RequestBody Collection<AccountRequestDto> requests) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            requests = requests.stream()
                .map(FieldSecurityUtil::filterWrite)
                .toList();
            BulkResult<AccountResponseDto> result = service.upsertAllResult(requests);
            result = new BulkResult<>(
                result.succeeded().stream()
                    .map(FieldSecurityUtil::filterRead)
                    .toList(),
                result.failed());
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
            operationId = "accountBulkDelete",
            summary = "Delete multiple Account entities",
            description = "Permanently deletes multiple Account entities by their IDs. Returns per-item success and failure details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account entities deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "207", description = "Bulk request completed with one or more per-item failures")
    })
    @PreAuthorize("isAuthenticated()")
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
            operationId = "accountFindByIds",
            summary = "Find Account entities by IDs",
            description = "Retrieves multiple Account entities by their IDs in a single request."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Account entities retrieved successfully"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<AccountResponseDto>> findByIds(
            @RequestBody List<UUID> ids) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            var dtos = service.findByIds(ids).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
            PaginatedResponse<AccountResponseDto> response = new PaginatedResponse<>(
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
            operationId = "accountExists",
            summary = "Check if a Account exists",
            description = "Checks whether a Account entity with the given ID exists in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account existence check result"),
            @ApiResponse(responseCode = "404", description = "Account with the specified ID was not found")
    })
    @PreAuthorize("isAuthenticated()")
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
            operationId = "accountCount",
            summary = "Count Account entities",
            description = "Counts the total number of Account entities matching the search criteria."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Count of Account entities"))
    @PreAuthorize("isAuthenticated()")
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
            operationId = "accountValidate",
            summary = "Validate Account data",
            description = "Validates Account data without persisting it. Returns validation errors if any."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Validation results"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> validate(@Valid @NotNull @RequestBody AccountRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            FieldSecurityUtil.filterWrite(request);
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
            operationId = "accountExport",
            summary = "Export Account entities",
            description = "Exports Account entities in the requested format."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Account data exported"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StreamingResponseBody> export(
            @ModelAttribute AccountSearchRequest searchRequest,
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
                pageable -> SearchOperations.search(service, searchRequest, pageable, AccountResponseDto.class),
                FieldSecurityUtil::filterRead,
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
            operationId = "accountSearch",
            summary = "Search Account entities",
            description = "Searches for Account entities based on the provided search criteria."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Search results for Account entities"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<AccountResponseDto>> search(
            @ModelAttribute AccountSearchRequest searchRequest, Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Pageable clamped = clampPageable(pageable);
            Page<AccountResponseDto> page = SearchOperations.search(service, searchRequest, clamped, AccountResponseDto.class);
            Page<AccountResponseDto> dtoPage = page.map(FieldSecurityUtil::filterRead);
            PaginatedResponse<AccountResponseDto> response = new PaginatedResponse<>(
                dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
                dtoPage.getTotalPages(), dtoPage.getTotalElements(),
                dtoPage.isFirst(), dtoPage.isLast()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("SEARCH", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @GetMapping("/list")
    @Operation(
            summary = "Get all Account entities as List projection",
            description = "Retrieves all Account entities with support for pagination, projected to List DTO."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of Account entities"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<AccountListResponseDto>> getAllList(Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Pageable clamped = clampPageable(pageable);
            Page<AccountListResponseDto> page = service.findAll(clamped, AccountListResponseDto.class);
            PaginatedResponse<AccountListResponseDto> response = new PaginatedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("GET_ALL_LIST", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @GetMapping("/list/{id}")
    @Operation(
            summary = "Get a single Account by ID as List projection",
            description = "Retrieves a single Account entity by its unique identifier, projected to List DTO."
    )
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Account retrieved successfully"), @ApiResponse(responseCode = "404", description = "Account with the specified ID was not found")})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountListResponseDto> getListById(@PathVariable UUID id) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            AccountListResponseDto dto = service.findById(id, AccountListResponseDto.class);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("GET_ONE_LIST", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @GetMapping("/detail")
    @Operation(
            summary = "Get all Account entities as Detail projection",
            description = "Retrieves all Account entities with support for pagination, projected to Detail DTO."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of Account entities"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<AccountDetailResponseDto>> getAllDetail(
            Pageable pageable) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            Pageable clamped = clampPageable(pageable);
            Page<AccountDetailResponseDto> page = service.findAll(clamped, AccountDetailResponseDto.class);
            PaginatedResponse<AccountDetailResponseDto> response = new PaginatedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("GET_ALL_DETAIL", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @GetMapping("/detail/{id}")
    @Operation(
            summary = "Get a single Account by ID as Detail projection",
            description = "Retrieves a single Account entity by its unique identifier, projected to Detail DTO."
    )
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Account retrieved successfully"), @ApiResponse(responseCode = "404", description = "Account with the specified ID was not found")})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountDetailResponseDto> getDetailById(@PathVariable UUID id) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            AccountDetailResponseDto dto = service.findById(id, AccountDetailResponseDto.class);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("GET_ONE_DETAIL", _crudcraftOutcome, _crudcraftStarted);
        }
    }
}

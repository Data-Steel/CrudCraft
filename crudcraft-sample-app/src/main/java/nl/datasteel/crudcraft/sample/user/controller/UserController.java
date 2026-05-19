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

package nl.datasteel.crudcraft.sample.user.controller;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.BiConsumer;
import nl.datasteel.crudcraft.runtime.Identified;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
import nl.datasteel.crudcraft.runtime.exception.ExportLimitExceededException;
import nl.datasteel.crudcraft.runtime.export.util.ExportUtil;
import nl.datasteel.crudcraft.runtime.search.SearchOperations;
import nl.datasteel.crudcraft.runtime.security.FieldSecurityUtil;
import nl.datasteel.crudcraft.runtime.service.BulkResult;
import nl.datasteel.crudcraft.sample.user.dto.ref.UserRef;
import nl.datasteel.crudcraft.sample.user.dto.request.UserRequestDto;
import nl.datasteel.crudcraft.sample.user.dto.response.UserResponseDto;
import nl.datasteel.crudcraft.sample.user.search.UserSearchRequest;
import nl.datasteel.crudcraft.sample.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


/**
 * Generated Controller layer stub for User. @CrudCraft:generated @CrudCraft:editable
 *
 * <p>This Controller stub extends CrudCraft's base implementation. Override methods to customise
 * behaviour.
 *
 * <p>You are allowed to modify this file. It extends CrudCraft's abstract base (UserControllerBase)
 * which already implements full CRUD logic.
 *
 * <p>This file was generated only once. CrudCraft will not overwrite it in future builds. If you
 * delete it, it will be regenerated.
 *
 * <p>Features provided by CrudCraft: - Standard CRUD workflow already implemented - DTO mapping and
 * repository calls wired up
 *
 * <p>Generation context: - Source model: User - Package:
 * nl.datasteel.crudcraft.sample.user.controller - Generator: ControllerGenerator - Generation time:
 * 2025-11-16T21:29:13.682623464Z - CrudCraft version: null
 *
 * <p>Recommendations: - You may customize method behavior, add validation, or extend with
 * additional endpoints. - Signature changes are allowed, but may desync from service or mapper
 * layer—proceed with care. - Do not manually copy or paste other CrudCraft stubs into this class.
 *
 * <p>Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@RestController
@RequestMapping("/users")
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification =
                "Controller stores injected collaborators and configuration values by design")
// Generated editable stub: OpenAPI annotations document endpoint methods for the sample app.
@SuppressWarnings("checkstyle:MissingJavadocMethod")
public class UserController {
    private final UserService service;

    @Value("${crudcraft.api.max-page-size:100}")
    protected int maxPageSize;

    @Value("${crudcraft.export.max-rows:-1}")
    protected int maxRows;

    @Value("${crudcraft.export.max-csv-rows:100000}")
    protected int maxCsvRows;

    @Value("${crudcraft.export.max-json-rows:50000}")
    protected int maxJsonRows;

    @Value("${crudcraft.export.max-xlsx-rows:25000}")
    protected int maxXlsxRows;

    public UserController(UserService service) {
        this.service = service;
    }

    private Pageable clampPageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, maxPageSize);
        }
        int size = Math.min(pageable.getPageSize(), maxPageSize);
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Partially update an existing User",
            description =
                    "Partially updates an existing User entity identified by ID. Returns the"
                            + " updated entity.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User partially updated successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "User with the specified ID was not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> patch(
            @PathVariable("id") UUID id, @RequestBody UserRequestDto request) {
        request = FieldSecurityUtil.filterWrite(request);
        UserResponseDto patched = service.patch(id, request);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(patched));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a User",
            description = "Permanently deletes a User entity identified by ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User with the specified ID was not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search User entities",
            description = "Searches for User entities based on the provided search criteria.")
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Search results for User entities"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<UserResponseDto>> search(
            @ModelAttribute UserSearchRequest searchRequest, Pageable pageable) {
        Pageable clamped = clampPageable(pageable);
        Page<UserResponseDto> page =
                SearchOperations.search(service, searchRequest, clamped, UserResponseDto.class);
        Page<UserResponseDto> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<UserResponseDto> response =
                new PaginatedResponse<>(
                        dtoPage.getContent(),
                        dtoPage.getNumber(),
                        dtoPage.getSize(),
                        dtoPage.getTotalPages(),
                        dtoPage.getTotalElements(),
                        dtoPage.isFirst(),
                        dtoPage.isLast());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/batch")
    @Operation(
            summary = "Update multiple User entities",
            description =
                    "Updates multiple User entities in a single request. Each entity must include"
                            + " its ID. Returns all updated entities.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User entities updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkResult<UserResponseDto>> updateAll(
            @Valid @RequestBody List<Identified<UUID, UserRequestDto>> requests) {
        requests =
                requests.stream()
                        .map(
                                request ->
                                        new Identified<>(
                                                request.getId(),
                                                FieldSecurityUtil.filterWrite(request.getData())))
                        .toList();
        BulkResult<UserResponseDto> result = service.updateAllResult(requests);
        result =
                new BulkResult<>(
                        result.succeeded().stream()
                                .map(FieldSecurityUtil::filterRead)
                                .toList(),
                        result.failed());
        return ResponseEntity.status(result.hasFailures() ? HttpStatus.MULTI_STATUS : HttpStatus.OK)
                .body(result);
    }

    @PostMapping("/batch/upsert")
    @Operation(
            summary = "Create or update multiple User entities",
            description =
                    "Creates or updates multiple User entities in a single request. Returns all"
                            + " created or updated entities.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "User entities created or updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkResult<UserResponseDto>> upsertAll(
            @RequestBody Collection<UserRequestDto> requests) {
        requests = requests.stream().map(FieldSecurityUtil::filterWrite).toList();
        BulkResult<UserResponseDto> result = service.upsertAllResult(requests);
        result =
                new BulkResult<>(
                        result.succeeded().stream()
                                .map(FieldSecurityUtil::filterRead)
                                .toList(),
                        result.failed());
        return ResponseEntity.status(
                        result.hasFailures() ? HttpStatus.MULTI_STATUS : HttpStatus.CREATED)
                .body(result);
    }

    @GetMapping("/ref")
    @Operation(
            summary = "Get all User references with pagination",
            description =
                    "Retrieves lightweight references to User entities with support for pagination"
                            + " and filtering.")
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Paginated list of User references"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<UserRef>> getAllRef(Pageable pageable) {
        Page<UserRef> page = service.findAllRef(clampPageable(pageable));
        Page<UserRef> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<UserRef> response =
                new PaginatedResponse<>(
                        dtoPage.getContent(),
                        dtoPage.getNumber(),
                        dtoPage.getSize(),
                        dtoPage.getTotalPages(),
                        dtoPage.getTotalElements(),
                        dtoPage.isFirst(),
                        dtoPage.isLast());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/batch/delete")
    @Operation(
            summary = "Delete multiple User entities",
            description = "Permanently deletes multiple User entities by their IDs.")
    @ApiResponses(
            @ApiResponse(responseCode = "204", description = "User entities deleted successfully"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkResult<UUID>> deleteAllByIds(@RequestBody Collection<UUID> ids) {
        BulkResult<UUID> result = service.deleteAllByIdsResult(ids);
        return ResponseEntity.status(result.hasFailures() ? HttpStatus.MULTI_STATUS : HttpStatus.OK)
                .body(result);
    }

    @PatchMapping("/batch")
    @Operation(
            summary = "Partially update multiple User entities",
            description =
                    "Partially updates multiple User entities in a single request. Each entity must"
                            + " include its ID. Returns all updated entities.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "User entities partially updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkResult<UserResponseDto>> patchAll(
            @Valid @RequestBody List<Identified<UUID, UserRequestDto>> requests) {
        requests =
                requests.stream()
                        .map(
                                request ->
                                        new Identified<>(
                                                request.getId(),
                                                FieldSecurityUtil.filterWrite(request.getData())))
                        .toList();
        BulkResult<UserResponseDto> result = service.patchAllResult(requests);
        result =
                new BulkResult<>(
                        result.succeeded().stream()
                                .map(FieldSecurityUtil::filterRead)
                                .toList(),
                        result.failed());
        return ResponseEntity.status(result.hasFailures() ? HttpStatus.MULTI_STATUS : HttpStatus.OK)
                .body(result);
    }

    @PostMapping("/batch/ids")
    @Operation(
            summary = "Find User entities by IDs",
            description = "Retrieves multiple User entities by their IDs in a single request.")
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "User entities retrieved successfully"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<UserResponseDto>> findByIds(
            @RequestBody List<UUID> ids) {
        var dtos = service.findByIds(ids).stream().map(FieldSecurityUtil::filterRead).toList();
        PaginatedResponse<UserResponseDto> response =
                new PaginatedResponse<>(dtos, 0, dtos.size(), 1, dtos.size(), true, true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a single User by ID",
            description = "Retrieves a single User entity by its unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User with the specified ID was not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getOne(@PathVariable UUID id) {
        UserResponseDto dto = service.findById(id);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(dto));
    }

    @RequestMapping(
            value = "/exists/{id}",
            method = {RequestMethod.HEAD, RequestMethod.GET})
    @Operation(
            summary = "Check if a User exists",
            description = "Checks whether a User entity with the given ID exists in the system.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User existence check result"),
        @ApiResponse(responseCode = "404", description = "User with the specified ID was not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> exists(@PathVariable("id") UUID id) {
        return service.existsById(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(
            summary = "Create a new User",
            description =
                    "Creates a new User entity with the provided data. Returns the created entity"
                            + " with generated ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> post(@RequestBody UserRequestDto request) {
        request = FieldSecurityUtil.filterWrite(request);
        UserResponseDto created = service.create(request);
        return ResponseEntity.status(201).body(FieldSecurityUtil.filterRead(created));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing User",
            description =
                    "Updates an existing User entity identified by ID. Returns the updated entity.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "User with the specified ID was not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable("id") UUID id, @RequestBody UserRequestDto request) {
        request = FieldSecurityUtil.filterWrite(request);
        UserResponseDto updated = service.update(id, request);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(updated));
    }

    @PostMapping("/batch")
    @Operation(
            summary = "Create multiple User entities",
            description =
                    "Creates multiple User entities in a single request. Returns all created"
                            + " entities with generated IDs.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User entities created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkResult<UserResponseDto>> createAll(
            @Valid @RequestBody List<UserRequestDto> requests) {
        requests = requests.stream().map(FieldSecurityUtil::filterWrite).toList();
        BulkResult<UserResponseDto> result = service.createAllResult(requests);
        result =
                new BulkResult<>(
                        result.succeeded().stream()
                                .map(FieldSecurityUtil::filterRead)
                                .toList(),
                        result.failed());
        return ResponseEntity.status(
                        result.hasFailures() ? HttpStatus.MULTI_STATUS : HttpStatus.CREATED)
                .body(result);
    }

    @GetMapping
    @Operation(
            summary = "Get all User entities with pagination",
            description =
                    "Retrieves all User entities with support for pagination, sorting, and"
                            + " filtering via search parameters.")
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Paginated list of User entities"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<UserResponseDto>> getAll(Pageable pageable) {
        Page<UserResponseDto> page = service.findAll(clampPageable(pageable));
        Page<UserResponseDto> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<UserResponseDto> response =
                new PaginatedResponse<>(
                        dtoPage.getContent(),
                        dtoPage.getNumber(),
                        dtoPage.getSize(),
                        dtoPage.getTotalPages(),
                        dtoPage.getTotalElements(),
                        dtoPage.isFirst(),
                        dtoPage.isLast());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    @Operation(
            summary = "Export User entities",
            description = "Exports User entities in the requested format.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "User data exported"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StreamingResponseBody> export(
            @ModelAttribute UserSearchRequest searchRequest,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "format", required = true) String format) {
        if (limit != null && limit < 0) {
            return ResponseEntity.badRequest().build();
        }
        final int effectiveLimit = limit != null ? limit : 1000;
        String lower = format == null ? "" : format.toLowerCase();
        int max;
        String contentType;
        String extension;
        BiConsumer<Iterator<UserResponseDto>, OutputStream> exporter;
        switch (lower) {
            case "csv" -> {
                max = effectiveMaxRows(maxCsvRows);
                contentType = "text/csv";
                extension = "csv";
                exporter = ExportUtil::streamCsv;
            }
            case "json" -> {
                max = effectiveMaxRows(maxJsonRows);
                contentType = "application/json";
                extension = "json";
                exporter = ExportUtil::streamJson;
            }
            case "xlsx" -> {
                max = effectiveMaxRows(maxXlsxRows);
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                extension = "xlsx";
                exporter = ExportUtil::streamXlsx;
            }
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }
        if (effectiveLimit > max) {
            throw new ExportLimitExceededException(
                    "Export limit exceeded for "
                            + lower
                            + ": requested "
                            + effectiveLimit
                            + " rows, maximum is "
                            + max
                            + ". Reduce the limit parameter or export in smaller batches.");
        }
        int clamped = Math.min(effectiveLimit, max);
        int pageSize = Math.min(maxPageSize, clamped);
        StreamingResponseBody body =
                out -> {
                    Iterator<UserResponseDto> iterator =
                            new Iterator<>() {
                                int page = 0;
                                int index = 0;
                                List<UserResponseDto> current = Collections.emptyList();
                                int fetched = 0;

                                private void fetch() {
                                    if (fetched >= clamped) {
                                        current = Collections.emptyList();
                                        return;
                                    }
                                    Page<UserResponseDto> p =
                                            SearchOperations.search(
                                                    service,
                                                    searchRequest,
                                                    PageRequest.of(page++, pageSize),
                                                    UserResponseDto.class);
                                    List<UserResponseDto> dtos =
                                            p.getContent().stream()
                                                    .map(FieldSecurityUtil::filterRead)
                                                    .toList();
                                    if (dtos.isEmpty()) {
                                        current = Collections.emptyList();
                                        fetched = clamped;
                                        return;
                                    }
                                    if (fetched + dtos.size() > clamped) {
                                        dtos = dtos.subList(0, clamped - fetched);
                                        fetched = clamped;
                                    } else {
                                        fetched += dtos.size();
                                    }
                                    current = dtos;
                                    index = 0;
                                }

                                @Override
                                public boolean hasNext() {
                                    if (index >= current.size()) {
                                        fetch();
                                    }
                                    return index < current.size();
                                }

                                @Override
                                public UserResponseDto next() {
                                    if (!hasNext()) {
                                        throw new NoSuchElementException();
                                    }
                                    return current.get(index++);
                                }
                            };
                    exporter.accept(iterator, out);
                };
        String filename = "export-" + System.currentTimeMillis() + "." + extension;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(body);
    }

    private int effectiveMaxRows(int formatMaxRows) {
        if (maxRows <= 0) {
            return formatMaxRows;
        }
        return Math.min(maxRows, formatMaxRows);
    }

    @PostMapping("/validate")
    @Operation(
            summary = "Validate User data",
            description =
                    "Validates User data without persisting it. Returns validation errors if any.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Validation results"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> validate(@Valid @RequestBody UserRequestDto request) {
        FieldSecurityUtil.filterWrite(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    @Operation(
            summary = "Count User entities",
            description = "Counts the total number of User entities matching the search criteria.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Count of User entities"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> count() {
        long total = service.count();
        return ResponseEntity.ok(Map.of("count", total));
    }
}

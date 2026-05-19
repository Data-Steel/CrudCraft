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

package nl.datasteel.crudcraft.sample.blog.controller;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.Identified;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import nl.datasteel.crudcraft.runtime.export.service.EnhancedExportService;
import nl.datasteel.crudcraft.runtime.export.service.EnhancedExportServiceFactory;
import nl.datasteel.crudcraft.runtime.export.service.ExportService;
import nl.datasteel.crudcraft.runtime.search.SearchOperations;
import nl.datasteel.crudcraft.runtime.security.FieldSecurityUtil;
import nl.datasteel.crudcraft.runtime.service.BulkResult;
import nl.datasteel.crudcraft.sample.blog.Post;
import nl.datasteel.crudcraft.sample.blog.dto.ref.PostRef;
import nl.datasteel.crudcraft.sample.blog.dto.request.PostRequestDto;
import nl.datasteel.crudcraft.sample.blog.dto.response.PostListResponseDto;
import nl.datasteel.crudcraft.sample.blog.dto.response.PostResponseDto;
import nl.datasteel.crudcraft.sample.blog.search.PostSearchRequest;
import nl.datasteel.crudcraft.sample.blog.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
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
 * Generated Controller layer stub for Post. @CrudCraft:generated @CrudCraft:editable
 *
 * <p>This Controller stub extends CrudCraft's base implementation. Override methods to customise
 * behaviour.
 *
 * <p>You are allowed to modify this file. It extends CrudCraft's abstract base (PostControllerBase)
 * which already implements full CRUD logic.
 *
 * <p>This file was generated only once. CrudCraft will not overwrite it in future builds. If you
 * delete it, it will be regenerated.
 *
 * <p>Features provided by CrudCraft: - Standard CRUD workflow already implemented - DTO mapping and
 * repository calls wired up
 *
 * <p>Generation context: - Source model: Post - Package:
 * nl.datasteel.crudcraft.sample.blog.controller - Generator: ControllerGenerator - Generation time:
 * 2026-02-10T11:14:04.084729493Z - CrudCraft version: 1.0.1-SNAPSHOT
 *
 * <p>Recommendations: - You may customize method behavior, add validation, or extend with
 * additional endpoints. - Signature changes are allowed, but may desync from service or mapper
 * layer—proceed with care. - Do not manually copy or paste other CrudCraft stubs into this class.
 *
 * <p>Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@RestController
@RequestMapping("/posts")
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2", "CT_CONSTRUCTOR_THROW"},
        justification =
                "Controller stores injected collaborators and configuration values by design")
// Generated editable stub: OpenAPI annotations document endpoint methods for the sample app.
@SuppressWarnings("checkstyle:MissingJavadocMethod")
public class PostController {
    private static final Logger LOG = LoggerFactory.getLogger(PostController.class);

    private final PostService service;

    private final ObjectProvider<EnhancedExportServiceFactory> exportServiceFactoryProvider;

    private volatile EnhancedExportService<PostResponseDto, Post, PostSearchRequest> exportService;

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

    @Value("${crudcraft.export.max-depth:5}")
    protected int maxDepth;

    public PostController(
            PostService service,
            ObjectProvider<EnhancedExportServiceFactory> exportServiceFactoryProvider,
            @Value("${crudcraft.api.max-page-size:100}") int maxPageSize,
            @Value("${crudcraft.export.max-rows:-1}") int maxRows,
            @Value("${crudcraft.export.max-csv-rows:100000}") int maxCsvRows,
            @Value("${crudcraft.export.max-json-rows:50000}") int maxJsonRows,
            @Value("${crudcraft.export.max-xlsx-rows:25000}") int maxXlsxRows,
            @Value("${crudcraft.export.max-depth:5}") int maxDepth) {
        this.service = service;
        this.exportServiceFactoryProvider = exportServiceFactoryProvider;
        this.maxPageSize = maxPageSize;
        this.maxRows = maxRows;
        this.maxCsvRows = maxCsvRows;
        this.maxJsonRows = maxJsonRows;
        this.maxXlsxRows = maxXlsxRows;
        this.maxDepth = maxDepth;
    }

    private static EnhancedExportService<PostResponseDto, Post, PostSearchRequest>
            createExportServiceSafely(
                    ObjectProvider<EnhancedExportServiceFactory> exportServiceFactoryProvider,
                    int maxRows,
                    int maxCsvRows,
                    int maxJsonRows,
                    int maxXlsxRows,
                    int maxPageSize,
                    int maxDepth) {
        ExportService.ExportConfig exportConfig =
                new ExportService.ExportConfig(
                        maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize, maxDepth);
        try {
            EnhancedExportServiceFactory exportFactory =
                    exportServiceFactoryProvider.getIfAvailable();
            if (exportFactory != null) {
                return exportFactory.create(
                        maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize, Post.class);
            }
        } catch (IllegalStateException | org.springframework.beans.BeansException ex) {
            LOG.warn(
                    "Falling back to DTO-only export service because enhanced export factory "
                            + "could not be created",
                    ex);
        }
        return new EnhancedExportService<PostResponseDto, Post, PostSearchRequest>(
                exportConfig, null, Post.class, false);
    }

    private EnhancedExportService<PostResponseDto, Post, PostSearchRequest> exportService() {
        EnhancedExportService<PostResponseDto, Post, PostSearchRequest> current =
                this.exportService;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            current = this.exportService;
            if (current == null) {
                current =
                        createExportServiceSafely(
                                exportServiceFactoryProvider,
                                maxRows,
                                maxCsvRows,
                                maxJsonRows,
                                maxXlsxRows,
                                maxPageSize,
                                maxDepth);
                this.exportService = current;
            }
            return current;
        }
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
            operationId = "postPatch",
            summary = "Partially update an existing Post",
            description =
                    "Partially updates an existing Post entity identified by ID. Returns the"
                            + " updated entity.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post partially updated successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "Post with the specified ID was not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<PostResponseDto> patch(
            @PathVariable("id") UUID id, @NotNull @RequestBody PostRequestDto request) {
        request = FieldSecurityUtil.filterWrite(request);
        PostResponseDto patched = service.patch(id, request);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(patched));
    }

    @PostMapping("/batch")
    @Operation(
            operationId = "postBulkCreate",
            summary = "Create multiple Post entities",
            description =
                    "Creates multiple Post entities in a single request. Returns all created"
                            + " entities with generated IDs.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Post entities created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<BulkResult<PostResponseDto>> createAll(
            @Valid @NotNull @RequestBody List<PostRequestDto> requests) {
        requests = requests.stream().map(FieldSecurityUtil::filterWrite).toList();
        BulkResult<PostResponseDto> result = service.createAllResult(requests);
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

    @RequestMapping(
            value = "/exists/{id}",
            method = {RequestMethod.HEAD, RequestMethod.GET})
    @Operation(
            operationId = "postExists",
            summary = "Check if a Post exists",
            description = "Checks whether a Post entity with the given ID exists in the system.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post existence check result"),
        @ApiResponse(responseCode = "404", description = "Post with the specified ID was not found")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> exists(@PathVariable("id") UUID id) {
        return service.existsById(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "postGetOne",
            summary = "Get a single Post by ID",
            description = "Retrieves a single Post entity by its unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Post with the specified ID was not found")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<PostResponseDto> getOne(@PathVariable UUID id) {
        PostResponseDto dto = service.findById(id);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(dto));
    }

    @PostMapping("/validate")
    @Operation(
            operationId = "postValidate",
            summary = "Validate Post data",
            description =
                    "Validates Post data without persisting it. Returns validation errors if any.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Validation results"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> validate(@Valid @NotNull @RequestBody PostRequestDto request) {
        FieldSecurityUtil.filterWrite(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ref")
    @Operation(
            operationId = "postGetAllRef",
            summary = "Get all Post references with pagination",
            description =
                    "Retrieves lightweight references to Post entities with support for pagination"
                            + " and filtering.")
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Paginated list of Post references"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginatedResponse<PostRef>> getAllRef(Pageable pageable) {
        Page<PostRef> page = service.findAllRef(clampPageable(pageable));
        Page<PostRef> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<PostRef> response =
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

    @DeleteMapping("/{id}")
    @Operation(
            operationId = "postDelete",
            summary = "Delete a Post",
            description = "Permanently deletes a Post entity identified by ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Post with the specified ID was not found")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            operationId = "postGetAll",
            summary = "Get all Post entities with pagination",
            description =
                    "Retrieves all Post entities with support for pagination, sorting, and"
                            + " filtering via search parameters.")
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Paginated list of Post entities"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginatedResponse<PostResponseDto>> getAll(Pageable pageable) {
        Page<PostResponseDto> page = service.findAll(clampPageable(pageable));
        Page<PostResponseDto> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<PostResponseDto> response =
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
            operationId = "postBulkUpdate",
            summary = "Update multiple Post entities",
            description =
                    "Updates multiple Post entities in a single request. Each entity must include"
                            + " its ID. Returns all updated entities.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post entities updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<BulkResult<PostResponseDto>> updateAll(
            @Valid @NotNull @RequestBody List<Identified<UUID, PostRequestDto>> requests) {
        requests =
                requests.stream()
                        .map(
                                request ->
                                        new Identified<>(
                                                request.getId(),
                                                FieldSecurityUtil.filterWrite(request.getData())))
                        .toList();
        BulkResult<PostResponseDto> result = service.updateAllResult(requests);
        result =
                new BulkResult<>(
                        result.succeeded().stream()
                                .map(FieldSecurityUtil::filterRead)
                                .toList(),
                        result.failed());
        return ResponseEntity.status(result.hasFailures() ? HttpStatus.MULTI_STATUS : HttpStatus.OK)
                .body(result);
    }

    @GetMapping("/search")
    @Operation(
            operationId = "postSearch",
            summary = "Search Post entities",
            description = "Searches for Post entities based on the provided search criteria.")
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Search results for Post entities"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginatedResponse<PostResponseDto>> search(
            @ModelAttribute PostSearchRequest searchRequest,
            @RequestParam MultiValueMap<String, String> queryParameters,
            Pageable pageable) {
        validateSearchQueryParameters(searchRequest, queryParameters);
        Pageable clamped = clampPageable(pageable);
        Page<PostResponseDto> page =
                SearchOperations.search(service, searchRequest, clamped, PostResponseDto.class);
        Page<PostResponseDto> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<PostResponseDto> response =
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

    private void validateSearchQueryParameters(
            PostSearchRequest searchRequest, MultiValueMap<String, String> queryParameters) {
        if (queryParameters == null || queryParameters.isEmpty()) {
            return;
        }
        Set<String> allowedSearchPaths = searchRequest.allowedSearchPaths();
        Set<String> allowedInfrastructureParameters =
                Set.of("page", "size", "sort", "searchLogic");
        for (String parameter : queryParameters.keySet()) {
            if (allowedInfrastructureParameters.contains(parameter)) {
                continue;
            }
            String requestedPath = searchPath(parameter);
            if (!allowedSearchPaths.contains(requestedPath)) {
                throw new BadRequestException(
                        "Unsupported search field. Use one of the generated searchable paths.",
                        Map.of(
                                "requested",
                                requestedPath,
                                "allowed",
                                allowedSearchPaths.toString(),
                                "docs",
                                "docs/feature-guides/search/filtering.md"));
            }
        }
    }

    private String searchPath(String parameter) {
        for (String suffix : List.of("Start", "End", "Op")) {
            if (parameter.endsWith(suffix) && parameter.length() > suffix.length()) {
                return parameter.substring(0, parameter.length() - suffix.length());
            }
        }
        return parameter;
    }

    @PostMapping
    @Operation(
            operationId = "postCreate",
            summary = "Create a new Post",
            description =
                    "Creates a new Post entity with the provided data. Returns the created entity"
                            + " with generated ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Post created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<PostResponseDto> post(
            @Valid @NotNull @RequestBody PostRequestDto request) {
        request = FieldSecurityUtil.filterWrite(request);
        PostResponseDto created = service.create(request);
        return ResponseEntity.status(201).body(FieldSecurityUtil.filterRead(created));
    }

    @PutMapping("/{id}")
    @Operation(
            operationId = "postUpdate",
            summary = "Update an existing Post",
            description =
                    "Updates an existing Post entity identified by ID. Returns the updated entity.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post updated successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "Post with the specified ID was not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<PostResponseDto> update(
            @PathVariable("id") UUID id,
            @Valid @NotNull @RequestBody PostRequestDto request) {
        request = FieldSecurityUtil.filterWrite(request);
        PostResponseDto updated = service.update(id, request);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(updated));
    }

    @DeleteMapping("/batch/delete")
    @Operation(
            operationId = "postBulkDelete",
            summary = "Delete multiple Post entities",
            description = "Permanently deletes multiple Post entities by their IDs.")
    @ApiResponses(
            @ApiResponse(responseCode = "204", description = "Post entities deleted successfully"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<BulkResult<UUID>> deleteAllByIds(@RequestBody Collection<UUID> ids) {
        BulkResult<UUID> result = service.deleteAllByIdsResult(ids);
        return ResponseEntity.status(result.hasFailures() ? HttpStatus.MULTI_STATUS : HttpStatus.OK)
                .body(result);
    }

    @GetMapping("/export")
    @Operation(
            operationId = "postExport",
            summary = "Export Post entities",
            description = "Exports Post entities in the requested format.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Post data exported"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<StreamingResponseBody> export(
            @ModelAttribute PostSearchRequest searchRequest,
            @ModelAttribute ExportRequest exportRequest,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "format", required = true) String format) {
        return exportService().export(
                searchRequest,
                limit,
                format,
                exportRequest,
                pageable ->
                        SearchOperations.search(
                                service, searchRequest, pageable, PostResponseDto.class),
                FieldSecurityUtil::filterRead,
                service.effectiveReadSpecification(searchRequest));
    }

    @PostMapping("/batch/ids")
    @Operation(
            operationId = "postFindByIds",
            summary = "Find Post entities by IDs",
            description = "Retrieves multiple Post entities by their IDs in a single request.")
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "Post entities retrieved successfully"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginatedResponse<PostResponseDto>> findByIds(
            @RequestBody List<UUID> ids) {
        var dtos = service.findByIds(ids).stream().map(FieldSecurityUtil::filterRead).toList();
        PaginatedResponse<PostResponseDto> response =
                new PaginatedResponse<>(dtos, 0, dtos.size(), 1, dtos.size(), true, true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    @Operation(
            operationId = "postCount",
            summary = "Count Post entities",
            description = "Counts the total number of Post entities matching the search criteria.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Count of Post entities"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Long>> count() {
        long total = service.count();
        return ResponseEntity.ok(Map.of("count", total));
    }

    @PatchMapping("/batch")
    @Operation(
            operationId = "postBulkPatch",
            summary = "Partially update multiple Post entities",
            description =
                    "Partially updates multiple Post entities in a single request. Each entity must"
                            + " include its ID. Returns all updated entities.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Post entities partially updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<BulkResult<PostResponseDto>> patchAll(
            @Valid @NotNull @RequestBody List<Identified<UUID, PostRequestDto>> requests) {
        requests =
                requests.stream()
                        .map(
                                request ->
                                        new Identified<>(
                                                request.getId(),
                                                FieldSecurityUtil.filterWrite(request.getData())))
                        .toList();
        BulkResult<PostResponseDto> result = service.patchAllResult(requests);
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
            operationId = "postBulkUpsert",
            summary = "Create or update multiple Post entities",
            description =
                    "Creates or updates multiple Post entities in a single request. Returns all"
                            + " created or updated entities.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Post entities created or updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<BulkResult<PostResponseDto>> upsertAll(
            @Valid @NotNull @RequestBody Collection<PostRequestDto> requests) {
        requests = requests.stream().map(FieldSecurityUtil::filterWrite).toList();
        BulkResult<PostResponseDto> result = service.upsertAllResult(requests);
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

    @GetMapping("/list")
    @Operation(
            summary = "Get all Post entities as List projection",
            description =
                    "Retrieves all Post entities with support for pagination, projected to List"
                            + " DTO.")
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Paginated list of Post entities"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginatedResponse<PostListResponseDto>> getAllList(Pageable pageable) {
        Pageable clamped = clampPageable(pageable);
        Page<PostListResponseDto> page = service.findAll(clamped, PostListResponseDto.class);
        PaginatedResponse<PostListResponseDto> response =
                new PaginatedResponse<>(
                        page.getContent(),
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalPages(),
                        page.getTotalElements(),
                        page.isFirst(),
                        page.isLast());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list/{id}")
    @Operation(
            summary = "Get a single Post by ID as List projection",
            description =
                    "Retrieves a single Post entity by its unique identifier, projected to List"
                            + " DTO.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Post retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Post with the specified ID was not found")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity getListById(@PathVariable UUID id) {
        PostListResponseDto dto = service.findById(id, PostListResponseDto.class);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(dto));
    }
}

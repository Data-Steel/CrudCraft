// Copyright (c) 2025 CrudCraft contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package nl.datasteel.crudcraft.sample.blog.controller;

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
import nl.datasteel.crudcraft.runtime.security.FieldSecurityUtil;
import nl.datasteel.crudcraft.runtime.util.ExportUtil;
import nl.datasteel.crudcraft.sample.blog.dto.ref.PostRef;
import nl.datasteel.crudcraft.sample.blog.dto.request.PostRequestDto;
import nl.datasteel.crudcraft.sample.blog.dto.response.PostListResponseDto;
import nl.datasteel.crudcraft.sample.blog.dto.response.PostResponseDto;
import nl.datasteel.crudcraft.sample.blog.search.PostSearchRequest;
import nl.datasteel.crudcraft.sample.blog.service.PostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
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
 * Generated Controller layer stub for Post.
 * @CrudCraft:generated
 * @CrudCraft:editable
 *
 * This Controller stub extends CrudCraft's base implementation. Override methods to customise behaviour.
 *
 * You are allowed to modify this file. It extends CrudCraft's abstract base (PostControllerBase)
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
 * - Source model: Post
 * - Package: nl.datasteel.crudcraft.sample.blog.controller
 * - Generator: ControllerGenerator
 * - Generation time: 2025-11-16T21:29:13.682623464Z
 * - CrudCraft version: null
 *
 * Recommendations:
 * - You may customize method behavior, add validation, or extend with additional endpoints.
 * - Signature changes are allowed, but may desync from service or mapper layer—proceed with care.
 * - Do not manually copy or paste other CrudCraft stubs into this class.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService service;

    @Value("${crudcraft.api.max-page-size:100}")
    protected int maxPageSize;

    @Value("${crudcraft.export.max-csv-rows:100000}")
    protected int maxCsvRows;

    @Value("${crudcraft.export.max-json-rows:50000}")
    protected int maxJsonRows;

    @Value("${crudcraft.export.max-xlsx-rows:25000}")
    protected int maxXlsxRows;

    public PostController(PostService service) {
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
            summary = "Partially update an existing Post",
            description = "Partially updates an existing Post entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post partially updated successfully"),
            @ApiResponse(responseCode = "404", description = "Post with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<PostResponseDto> patch(@PathVariable("id") UUID id,
            @RequestBody PostRequestDto request) {
        FieldSecurityUtil.filterWrite(request);
        PostResponseDto patched = service.patch(id, request);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(patched));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a Post",
            description = "Permanently deletes a Post entity identified by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Post with the specified ID was not found")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search Post entities",
            description = "Searches for Post entities based on the provided search criteria."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Search results for Post entities"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginatedResponse<PostResponseDto>> search(
            @ModelAttribute PostSearchRequest searchRequest, @RequestParam("limit") Integer limit) {
        if (limit == null || limit <= 0) {
            return ResponseEntity.badRequest().build();
        }
        int clamped = Math.min(limit, maxPageSize);
        Page<PostResponseDto> page = service.search(searchRequest, PageRequest.of(0, clamped));
        // service.search(searchRequest, PageRequest.of(0, clamped))
        Page<PostResponseDto> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<PostResponseDto> response = new PaginatedResponse<>(
            dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
            dtoPage.getTotalPages(), dtoPage.getTotalElements(),
            dtoPage.isFirst(), dtoPage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/batch")
    @Operation(
            summary = "Update multiple Post entities",
            description = "Updates multiple Post entities in a single request. Each entity must include its ID. Returns all updated entities."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post entities updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PostResponseDto>> updateAll(
            @Valid @RequestBody List<Identified<UUID, PostRequestDto>> requests) {
        requests.forEach(r -> FieldSecurityUtil.filterWrite(r.getData()));
        List<PostResponseDto> dtos = service.updateAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/batch/upsert")
    @Operation(
            summary = "Create or update multiple Post entities",
            description = "Creates or updates multiple Post entities in a single request. Returns all created or updated entities."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post entities created or updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PostResponseDto>> upsertAll(
            @RequestBody Collection<PostRequestDto> requests) {
        requests.forEach(FieldSecurityUtil::filterWrite);
        List<PostResponseDto> dtos = service.upsertAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/ref")
    @Operation(
            summary = "Get all Post references with pagination",
            description = "Retrieves lightweight references to Post entities with support for pagination and filtering."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of Post references"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginatedResponse<PostRef>> getAllRef(Pageable pageable,
            @ModelAttribute PostSearchRequest searchRequest) {
        Page<PostRef> page = service.searchRef(searchRequest, clampPageable(pageable));
        Page<PostRef> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<PostRef> response = new PaginatedResponse<>(
            dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
            dtoPage.getTotalPages(), dtoPage.getTotalElements(),
            dtoPage.isFirst(), dtoPage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/batch/delete")
    @Operation(
            summary = "Delete multiple Post entities",
            description = "Permanently deletes multiple Post entities by their IDs."
    )
    @ApiResponses(@ApiResponse(responseCode = "204", description = "Post entities deleted successfully"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody Collection<UUID> ids) {
        service.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/batch")
    @Operation(
            summary = "Partially update multiple Post entities",
            description = "Partially updates multiple Post entities in a single request. Each entity must include its ID. Returns all updated entities."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post entities partially updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PostResponseDto>> patchAll(
            @Valid @RequestBody List<Identified<UUID, PostRequestDto>> requests) {
        requests.forEach(r -> FieldSecurityUtil.filterWrite(r.getData()));
        List<PostResponseDto> dtos = service.patchAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/batch/ids")
    @Operation(
            summary = "Find Post entities by IDs",
            description = "Retrieves multiple Post entities by their IDs in a single request."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Post entities retrieved successfully"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginatedResponse<PostResponseDto>> findByIds(
            @RequestBody List<UUID> ids) {
        var dtos = service.findByIds(ids).stream()
            .map(FieldSecurityUtil::filterRead)
            .toList();
        PaginatedResponse<PostResponseDto> response = new PaginatedResponse<>(
            dtos,
            0,
            dtos.size(),
            1,
            dtos.size(),
            true,
            true
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a single Post by ID",
            description = "Retrieves a single Post entity by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Post with the specified ID was not found")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<PostResponseDto> getOne(@PathVariable UUID id) {
        PostResponseDto dto = service.findById(id);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(dto));
    }

    @RequestMapping(
            value = "/exists/{id}",
            method = {RequestMethod.HEAD, RequestMethod.GET}
    )
    @Operation(
            summary = "Check if a Post exists",
            description = "Checks whether a Post entity with the given ID exists in the system."
    )
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

    @PostMapping
    @Operation(
            summary = "Create a new Post",
            description = "Creates a new Post entity with the provided data. Returns the created entity with generated ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<PostResponseDto> post(@RequestBody PostRequestDto request) {
        FieldSecurityUtil.filterWrite(request);
        PostResponseDto created = service.create(request);
        return ResponseEntity.status(201).body(FieldSecurityUtil.filterRead(created));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing Post",
            description = "Updates an existing Post entity identified by ID. Returns the updated entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post updated successfully"),
            @ApiResponse(responseCode = "404", description = "Post with the specified ID was not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<PostResponseDto> update(@PathVariable("id") UUID id,
            @RequestBody PostRequestDto request) {
        FieldSecurityUtil.filterWrite(request);
        PostResponseDto updated = service.update(id, request);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(updated));
    }

    @PostMapping("/batch")
    @Operation(
            summary = "Create multiple Post entities",
            description = "Creates multiple Post entities in a single request. Returns all created entities with generated IDs."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post entities created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PostResponseDto>> createAll(
            @Valid @RequestBody List<PostRequestDto> requests) {
        requests.forEach(FieldSecurityUtil::filterWrite);
        List<PostResponseDto> dtos = service.createAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.status(201).body(dtos);
    }

    @GetMapping
    @Operation(
            summary = "Get all Post entities with pagination",
            description = "Retrieves all Post entities with support for pagination, sorting, and filtering via search parameters."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of Post entities"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginatedResponse<PostResponseDto>> getAll(Pageable pageable,
            @ModelAttribute PostSearchRequest searchRequest) {
        Page<PostResponseDto> page = service.search(searchRequest, clampPageable(pageable));
        Page<PostResponseDto> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<PostResponseDto> response = new PaginatedResponse<>(
            dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
            dtoPage.getTotalPages(), dtoPage.getTotalElements(),
            dtoPage.isFirst(), dtoPage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    @Operation(
            summary = "Export Post entities",
            description = "Exports Post entities in the requested format."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Post data exported"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<StreamingResponseBody> export(
            @ModelAttribute PostSearchRequest searchRequest,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "format", required = true) String format) {
        final int effectiveLimit = limit != null ? limit : 1000;
        String lower = format == null ? "" : format.toLowerCase();
        int max;
        String contentType;
        String extension;
        BiConsumer<Iterator<PostResponseDto>, OutputStream> exporter;
        switch (lower) {
            case "csv" -> {
                max = maxCsvRows;
                contentType = "text/csv";
                extension = "csv";
                exporter = ExportUtil::streamCsv;
            }
            case "json" -> {
                max = maxJsonRows;
                contentType = "application/json";
                extension = "json";
                exporter = ExportUtil::streamJson;
            }
            case "xlsx" -> {
                max = maxXlsxRows;
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                extension = "xlsx";
                exporter = ExportUtil::streamXlsx;
            }
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }
        int clamped = Math.min(effectiveLimit, max);
        int pageSize = Math.min(maxPageSize, clamped);
        StreamingResponseBody body = out -> {
            Iterator iterator = new Iterator<>() {
                int page = 0;
                int index = 0;
                List<PostResponseDto> current = Collections.emptyList();
                int fetched = 0;
                private void fetch() {
                    if (fetched >= clamped) {
                        current = Collections.emptyList();
                        return;
                    }
                    Page<PostResponseDto> p = service.search(searchRequest, PageRequest.of(page++, pageSize));
                    List<PostResponseDto> dtos = p.getContent().stream()
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
                public PostResponseDto next() {
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

    @PostMapping("/validate")
    @Operation(
            summary = "Validate Post data",
            description = "Validates Post data without persisting it. Returns validation errors if any."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Validation results"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> validate(@Valid @RequestBody PostRequestDto request) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    @Operation(
            summary = "Count Post entities",
            description = "Counts the total number of Post entities matching the search criteria."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Count of Post entities"))
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Long>> count() {
        long total = service.count();
        return ResponseEntity.ok(Map.of("count", total));
    }

    @GetMapping("/list")
    @Operation(
            summary = "Get all Post entities as List projection",
            description = "Retrieves all Post entities with support for pagination, projected to List DTO."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paginated list of Post entities"))
    public ResponseEntity getAllList(Pageable pageable) {
        Pageable clamped = clampPageable(pageable);
        Page<PostListResponseDto> page = service.search(null, clamped, PostListResponseDto.class);
        PaginatedResponse<PostListResponseDto> response = new PaginatedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.isFirst(),
            page.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list/{id}")
    @Operation(
            summary = "Get a single Post by ID as List projection",
            description = "Retrieves a single Post entity by its unique identifier, projected to List DTO."
    )
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Post retrieved successfully"), @ApiResponse(responseCode = "404", description = "Post with the specified ID was not found")})
    public ResponseEntity getListById(@PathVariable UUID id) {
        PostListResponseDto dto = service.findById(id, PostListResponseDto.class);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(dto));
    }
}

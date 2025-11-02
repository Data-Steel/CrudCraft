/**
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.runtime.controller;

import jakarta.validation.Valid;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import nl.datasteel.crudcraft.runtime.Identified;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
import nl.datasteel.crudcraft.runtime.search.SearchRequest;
import nl.datasteel.crudcraft.runtime.security.FieldSecurityUtil;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import nl.datasteel.crudcraft.runtime.util.ExportUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Generic abstract REST controller exposing a full suite of CRUD endpoints.
 *
 * @param <T> the JPA entity type
 * @param <U> the upsert/patch request DTO type
 * @param <R> the full response DTO type
 * @param <F> the reference DTO type
 * @param <Q> the search request type extending {@code SearchRequest<T>}
 * @param <ID> the identifier type
 */
public abstract class AbstractCrudController<
        T, U, R, F, Q extends SearchRequest<T>, ID> {

    /**
     * The service that handles CRUD operations.
     */
    protected final AbstractCrudService<T, U, R, F, ID> service;

    /**
     * Maximum page size for pagination.
     * Default is 100, can be overridden in application properties.
     */
    @Value("${crudcraft.api.max-page-size:100}")
    protected int maxPageSize;

    /**
     * Maximum number of rows for CSV export.
     * Default is 100,000, can be overridden in application properties.
     */
    @Value("${crudcraft.export.max-csv-rows:100000}")
    protected int maxCsvRows;

    /**
     * Maximum number of rows for JSON export.
     * Default is 50,000, can be overridden in application properties.
     */
    @Value("${crudcraft.export.max-json-rows:50000}")
    protected int maxJsonRows;

    /**
     * Maximum number of rows for XLSX export.
     * Default is 25,000, can be overridden in application properties.
     */
    @Value("${crudcraft.export.max-xlsx-rows:25000}")
    protected int maxXlsxRows;

    /**
     * Constructor to initialize the controller with the service.
     *
     * @param service the service that handles CRUD operations
     */
    protected AbstractCrudController(
            AbstractCrudService<T, U, R, F, ID> service
    ) {
        this.service = service;
    }

    /**
     * Clamps the pageable parameters to ensure they do not exceed the maximum page size.
     * If pageable is null, defaults to the first page with the maximum page size.
     *
     * @param pageable the original pageable request
     * @return a clamped pageable object
     */
    protected Pageable clampPageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, maxPageSize);
        }
        int size = Math.min(pageable.getPageSize(), maxPageSize);
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }

    // ─── BASIC CRUD ───────────────────────────────────────────────────────

    /**
     * Paginated list (optional search).
     * {@code GET /?page=...&size=...&q=...}
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<R>> getAll(
            Pageable pageable,
            @ModelAttribute Q searchRequest
    ) {
        Page<R> page = service.search(searchRequest, clampPageable(pageable));
        Page<R> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<R> response = new PaginatedResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements(),
                dtoPage.isFirst(),
                dtoPage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Paginated list of reference DTOs.
     * {@code GET /ref?page=...&size=...&q=...}
     */
    @GetMapping("/ref")
    public ResponseEntity<PaginatedResponse<F>> getAllRef(
            Pageable pageable,
            @ModelAttribute Q searchRequest
    ) {
        Page<F> page = service.searchRef(searchRequest, clampPageable(pageable));
        Page<F> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<F> response = new PaginatedResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements(),
                dtoPage.isFirst(),
                dtoPage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    /** Find by ID. {@code GET /{id}} */
    @GetMapping("/{id}")
    public ResponseEntity<R> getById(@PathVariable("id") ID id) {
        R dto = service.findById(id);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(dto));
    }

    /** Create new. {@code POST /} */
    @PostMapping
    public ResponseEntity<R> create(@Valid @RequestBody U request) {
        FieldSecurityUtil.filterWrite(request);
        R created = service.create(request);
        return ResponseEntity.status(201)
                .body(FieldSecurityUtil.filterRead(created));
    }

    /** Full update. {@code PUT /{id}} */
    @PutMapping("/{id}")
    public ResponseEntity<R> update(
            @PathVariable("id") ID id,
            @Valid @RequestBody U request
    ) {
        FieldSecurityUtil.filterWrite(request);
        R updated = service.update(id, request);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(updated));
    }

    /** Partial update. {@code PATCH /{id}} */
    @PatchMapping("/{id}")
    public ResponseEntity<R> patch(
            @PathVariable("id") ID id,
            @RequestBody U request
    ) {
        FieldSecurityUtil.filterWrite(request);
        R patched = service.patch(id, request);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(patched));
    }

    /** Delete. {@code DELETE /{id}} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") ID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── BULK ────────────────────────────────────────────────────

    /** Bulk create. {@code POST /batch} */
    @PostMapping("/batch")
    public ResponseEntity<List<R>> createAll(@Valid @RequestBody List<U> requests) {
        requests.forEach(FieldSecurityUtil::filterWrite);
        List<R> dtos = service.createAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.status(201).body(dtos);
    }

    /** Bulk update. {@code PUT /batch} */
    @PutMapping("/batch")
    public ResponseEntity<List<R>> updateAll(
            @Valid @RequestBody List<Identified<ID, U>> requests) {
        requests.forEach(r -> FieldSecurityUtil.filterWrite(r.getData()));
        List<R> dtos = service.updateAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /** Bulk patch. {@code PATCH /batch} */
    @PatchMapping("/batch")
    public ResponseEntity<List<R>> patchAll(
            @Valid @RequestBody List<Identified<ID, U>> requests) {
        requests.forEach(r -> FieldSecurityUtil.filterWrite(r.getData()));
        List<R> dtos = service.patchAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /** Bulk upsert. {@code POST /batch/upsert} */
    @PostMapping("/batch/upsert")
    public ResponseEntity<List<R>> upsertAll(@RequestBody Collection<U> requests) {
        requests.forEach(FieldSecurityUtil::filterWrite);
        List<R> dtos = service.upsertAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /** Bulk delete. {@code DELETE /batch/delete} */
    @DeleteMapping("/batch/delete")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody Collection<ID> ids) {
        service.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }

    // ─── OTHER OPERATIONS ─────────────────────────────────────────────────

    /** Exists check. {@code HEAD /{id}} or {@code GET /exists/{id}} */
    @RequestMapping(value = "/exists/{id}", method = {RequestMethod.HEAD, RequestMethod.GET})
    public ResponseEntity<Void> exists(@PathVariable("id") ID id) {
        return service.existsById(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    /** Count total. {@code GET /count} */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count() {
        long total = service.count();
        return ResponseEntity.ok(Map.of("count", total));
    }

    /** Find by multiple IDs. {@code POST /batch/ids} */
    @PostMapping("/batch/ids")
    public ResponseEntity<List<R>> findByIds(@RequestBody Collection<ID> ids) {
        List<R> dtos = service.findByIds(ids).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /** Dedicated search endpoint. {@code GET /search?limit=...} */
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<R>> search(
            @ModelAttribute Q searchRequest,
            @RequestParam(value = "limit", required = true) Integer limit
    ) {
        if (limit == null || limit <= 0) {
            return ResponseEntity.badRequest().build();
        }

        int clamped = Math.min(limit, maxPageSize);

        Page<R> page = service.search(searchRequest, PageRequest.of(0, clamped));
        Page<R> dtoPage = page.map(FieldSecurityUtil::filterRead);

        // Ensure we never return more than `clamped` items, even if the service ignored page size.
        List<R> content = dtoPage.getContent();
        if (content.size() > clamped) {
            content = content.subList(0, clamped);
        }

        PaginatedResponse<R> response = new PaginatedResponse<>(
                content,
                dtoPage.getNumber(),
                Math.min(dtoPage.getSize(), clamped),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements(),
                dtoPage.isFirst(),
                dtoPage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    /** Validate an upsert request body without persisting. {@code POST /validate} */
    @PostMapping("/validate")
    public ResponseEntity<Void> validate(@Valid @RequestBody U request) {
        return ResponseEntity.ok().build();
    }

    /** Export search results as CSV, JSON or XLSX. {@code GET /export} */
    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> export(
            @ModelAttribute Q searchRequest,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "format", required = true) String format
    ) {
        if (limit != null && limit < 0) {
            throw new IllegalArgumentException("limit must be non-negative");
        }

        final int effectiveLimit = limit != null ? limit : 1000;

        String lower = format == null ? "" : format.toLowerCase();
        int max;
        String contentType;
        String extension;
        BiConsumer<Iterator<R>, OutputStream> exporter;

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
            Iterator<R> iterator = new Iterator<>() {
                int page = 0;
                int index = 0;
                List<R> current = Collections.emptyList();
                int fetched = 0;

                private void fetch() {
                    if (fetched >= clamped) {
                        current = Collections.emptyList();
                        return;
                    }
                    Page<R> p = service.search(searchRequest, PageRequest.of(page++, pageSize));
                    List<R> dtos = p.getContent().stream()
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
                public R next() {
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
}

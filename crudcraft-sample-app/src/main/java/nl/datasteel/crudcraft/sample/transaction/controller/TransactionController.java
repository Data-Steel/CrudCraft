/*
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
package nl.datasteel.crudcraft.sample.transaction.controller;

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
import nl.datasteel.crudcraft.sample.transaction.dto.ref.TransactionRef;
import nl.datasteel.crudcraft.sample.transaction.dto.request.TransactionRequestDto;
import nl.datasteel.crudcraft.sample.transaction.dto.response.TransactionResponseDto;
import nl.datasteel.crudcraft.sample.transaction.search.TransactionSearchRequest;
import nl.datasteel.crudcraft.sample.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Generated Controller layer stub for Transaction.
 * @CrudCraft:generated
 * @CrudCraft:editable
 *
 * This Controller stub extends CrudCraft's base implementation. Override methods to customise behaviour.
 *
 * You are allowed to modify this file. It extends CrudCraft's abstract base (TransactionControllerBase)
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
 * - Source model: Transaction
 * - Package: nl.datasteel.crudcraft.sample.transaction.controller
 * - Generator: ControllerGenerator
 * - Generation time: 2025-09-02T09:10:33.9334971+02:00
 * - CrudCraft version: 0.1.0
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
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService service;

    @Value("${crudcraft.api.max-page-size:100}")
    protected int maxPageSize;

    @Value("${crudcraft.export.max-csv-rows:100000}")
    protected int maxCsvRows;

    @Value("${crudcraft.export.max-json-rows:50000}")
    protected int maxJsonRows;

    @Value("${crudcraft.export.max-xlsx-rows:25000}")
    protected int maxXlsxRows;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    private Pageable clampPageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, maxPageSize);
        }
        int size = Math.min(pageable.getPageSize(), maxPageSize);
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }

    @PutMapping("/batch")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<TransactionResponseDto>> updateAll(
            @Valid @RequestBody List<Identified<UUID, TransactionRequestDto>> requests) {
        requests.forEach(r -> FieldSecurityUtil.filterWrite(r.getData()));
        List<TransactionResponseDto> dtos = service.updateAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginatedResponse<TransactionResponseDto>> getAll(Pageable pageable,
            @ModelAttribute TransactionSearchRequest searchRequest) {
        Page<TransactionResponseDto> page = service.search(searchRequest, clampPageable(pageable));
        Page<TransactionResponseDto> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<TransactionResponseDto> response = new PaginatedResponse<>(
            dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
            dtoPage.getTotalPages(), dtoPage.getTotalElements(),
            dtoPage.isFirst(), dtoPage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<TransactionResponseDto>> createAll(
            @Valid @RequestBody List<TransactionRequestDto> requests) {
        requests.forEach(FieldSecurityUtil::filterWrite);
        List<TransactionResponseDto> dtos = service.createAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.status(201).body(dtos);
    }

    @GetMapping("/export")
    @PreAuthorize("permitAll()")
    public ResponseEntity<StreamingResponseBody> export(
            @ModelAttribute TransactionSearchRequest searchRequest,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "format", required = true) String format) {
        final int effectiveLimit = limit != null ? limit : 1000;
        String lower = format == null ? "" : format.toLowerCase();
        int max;
        String contentType;
        String extension;
        BiConsumer<Iterator<TransactionResponseDto>, OutputStream> exporter;
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
                List<TransactionResponseDto> current = Collections.emptyList();
                int fetched = 0;
                private void fetch() {
                    if (fetched >= clamped) {
                        current = Collections.emptyList();
                        return;
                    }
                    Page<TransactionResponseDto> p = service.search(searchRequest, PageRequest.of(page++, pageSize));
                    List<TransactionResponseDto> dtos = p.getContent().stream()
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
                public TransactionResponseDto next() {
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
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> validate(@Valid @RequestBody TransactionRequestDto request) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Long>> count() {
        long total = service.count();
        return ResponseEntity.ok(Map.of("count", total));
    }

    @PatchMapping("/batch")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<TransactionResponseDto>> patchAll(
            @Valid @RequestBody List<Identified<UUID, TransactionRequestDto>> requests) {
        requests.forEach(r -> FieldSecurityUtil.filterWrite(r.getData()));
        List<TransactionResponseDto> dtos = service.patchAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TransactionResponseDto> patch(@PathVariable("id") UUID id,
            @RequestBody TransactionRequestDto request) {
        FieldSecurityUtil.filterWrite(request);
        TransactionResponseDto patched = service.patch(id, request);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(patched));
    }

    @RequestMapping(
            value = "/exists/{id}",
            method = {RequestMethod.HEAD, RequestMethod.GET}
    )
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> exists(@PathVariable("id") UUID id) {
        return service.existsById(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginatedResponse<TransactionResponseDto>> search(
            @ModelAttribute TransactionSearchRequest searchRequest,
            @RequestParam("limit") Integer limit) {
        if (limit == null || limit <= 0) {
            return ResponseEntity.badRequest().build();
        }
        int clamped = Math.min(limit, maxPageSize);
        Page<TransactionResponseDto> page = service.search(searchRequest, PageRequest.of(0, clamped));
        // service.search(searchRequest, PageRequest.of(0, clamped))
        Page<TransactionResponseDto> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<TransactionResponseDto> response = new PaginatedResponse<>(
            dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
            dtoPage.getTotalPages(), dtoPage.getTotalElements(),
            dtoPage.isFirst(), dtoPage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TransactionResponseDto> update(@PathVariable("id") UUID id,
            @RequestBody TransactionRequestDto request) {
        FieldSecurityUtil.filterWrite(request);
        TransactionResponseDto updated = service.update(id, request);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(updated));
    }

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<TransactionResponseDto> post(@RequestBody TransactionRequestDto request) {
        FieldSecurityUtil.filterWrite(request);
        TransactionResponseDto created = service.create(request);
        return ResponseEntity.status(201).body(FieldSecurityUtil.filterRead(created));
    }

    @GetMapping("/ref")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginatedResponse<TransactionRef>> getAllRef(Pageable pageable,
            @ModelAttribute TransactionSearchRequest searchRequest) {
        Page<TransactionRef> page = service.searchRef(searchRequest, clampPageable(pageable));
        Page<TransactionRef> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<TransactionRef> response = new PaginatedResponse<>(
            dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
            dtoPage.getTotalPages(), dtoPage.getTotalElements(),
            dtoPage.isFirst(), dtoPage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TransactionResponseDto> getOne(@PathVariable UUID id) {
        TransactionResponseDto dto = service.findById(id);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(dto));
    }

    @PostMapping("/batch/upsert")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<TransactionResponseDto>> upsertAll(
            @RequestBody Collection<TransactionRequestDto> requests) {
        requests.forEach(FieldSecurityUtil::filterWrite);
        List<TransactionResponseDto> dtos = service.upsertAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/batch/ids")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginatedResponse<TransactionResponseDto>> findByIds(
            @RequestBody List<UUID> ids) {
        var dtos = service.findByIds(ids).stream()
            .map(FieldSecurityUtil::filterRead)
            .toList();
        PaginatedResponse<TransactionResponseDto> response = new PaginatedResponse<>(
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

    /*
    Endpoint omitted by generation template (+ include/exclude). Since this stub is editable, it is commented out, so it can easily be added later.
    @org.springframework.web.bind.annotation.DeleteMapping("/batch/delete")
    @org.springframework.security.access.prepost.PreAuthorize("permitAll()")
    public org.springframework.http.ResponseEntity<java.lang.Void> deleteAllByIds(
        @org.springframework.web.bind.annotation.RequestBody java.util.Collection<java.util.UUID> ids) {
      service.deleteAllByIds(ids);
      return org.springframework.http.ResponseEntity.noContent().build();
    }
    */

    /*
    Endpoint omitted by generation template (+ include/exclude). Since this stub is editable, it is commented out, so it can easily be added later.
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("permitAll()")
    public org.springframework.http.ResponseEntity<java.lang.Void> delete(
        @org.springframework.web.bind.annotation.PathVariable("id") java.util.UUID id) {
      service.delete(id);
      return org.springframework.http.ResponseEntity.noContent().build();
    }
    */

}

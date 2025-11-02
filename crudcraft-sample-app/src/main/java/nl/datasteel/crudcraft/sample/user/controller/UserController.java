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
//  Copyright (c) 2025 CrudCraft contributors
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
// /
package nl.datasteel.crudcraft.sample.user.controller;

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
 * Generated Controller layer stub for User.
 * @CrudCraft:generated
 * @CrudCraft:editable
 *
 * This Controller stub extends CrudCraft's base implementation. Override methods to customise behaviour.
 *
 * You are allowed to modify this file. It extends CrudCraft's abstract base (UserControllerBase)
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
 * - Source model: User
 * - Package: nl.datasteel.crudcraft.sample.user.controller
 * - Generator: ControllerGenerator
 * - Generation time: 2025-10-24T15:28:06.6370404+02:00
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
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    @Value("${crudcraft.api.max-page-size:100}")
    protected int maxPageSize;

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

    @PutMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> updateAll(
            @Valid @RequestBody List<Identified<UUID, UserRequestDto>> requests) {
        requests.forEach(r -> FieldSecurityUtil.filterWrite(r.getData()));
        List<UserResponseDto> dtos = service.updateAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> patch(@PathVariable("id") UUID id,
            @RequestBody UserRequestDto request) {
        FieldSecurityUtil.filterWrite(request);
        UserResponseDto patched = service.patch(id, request);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(patched));
    }

    @DeleteMapping("/batch/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAllByIds(@RequestBody Collection<UUID> ids) {
        service.deleteAllByIds(ids);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(
            value = "/exists/{id}",
            method = {RequestMethod.HEAD, RequestMethod.GET}
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> exists(@PathVariable("id") UUID id) {
        return service.existsById(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<UserResponseDto>> getAll(Pageable pageable,
            @ModelAttribute UserSearchRequest searchRequest) {
        Page<UserResponseDto> page = service.search(searchRequest, clampPageable(pageable));
        Page<UserResponseDto> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<UserResponseDto> response = new PaginatedResponse<>(
            dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
            dtoPage.getTotalPages(), dtoPage.getTotalElements(),
            dtoPage.isFirst(), dtoPage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> post(@RequestBody UserRequestDto request) {
        FieldSecurityUtil.filterWrite(request);
        UserResponseDto created = service.create(request);
        return ResponseEntity.status(201).body(FieldSecurityUtil.filterRead(created));
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> count() {
        long total = service.count();
        return ResponseEntity.ok(Map.of("count", total));
    }

    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> validate(@Valid @RequestBody UserRequestDto request) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> createAll(
            @Valid @RequestBody List<UserRequestDto> requests) {
        requests.forEach(FieldSecurityUtil::filterWrite);
        List<UserResponseDto> dtos = service.createAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.status(201).body(dtos);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch/upsert")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> upsertAll(
            @RequestBody Collection<UserRequestDto> requests) {
        requests.forEach(FieldSecurityUtil::filterWrite);
        List<UserResponseDto> dtos = service.upsertAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getOne(@PathVariable UUID id) {
        UserResponseDto dto = service.findById(id);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(dto));
    }

    @PostMapping("/batch/ids")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<UserResponseDto>> findByIds(
            @RequestBody List<UUID> ids) {
        var dtos = service.findByIds(ids).stream()
            .map(FieldSecurityUtil::filterRead)
            .toList();
        PaginatedResponse<UserResponseDto> response = new PaginatedResponse<>(
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

    @GetMapping("/ref")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<UserRef>> getAllRef(Pageable pageable,
            @ModelAttribute UserSearchRequest searchRequest) {
        Page<UserRef> page = service.searchRef(searchRequest, clampPageable(pageable));
        Page<UserRef> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<UserRef> response = new PaginatedResponse<>(
            dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
            dtoPage.getTotalPages(), dtoPage.getTotalElements(),
            dtoPage.isFirst(), dtoPage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> update(@PathVariable("id") UUID id,
            @RequestBody UserRequestDto request) {
        FieldSecurityUtil.filterWrite(request);
        UserResponseDto updated = service.update(id, request);
        return ResponseEntity.ok(FieldSecurityUtil.filterRead(updated));
    }

    @PatchMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> patchAll(
            @Valid @RequestBody List<Identified<UUID, UserRequestDto>> requests) {
        requests.forEach(r -> FieldSecurityUtil.filterWrite(r.getData()));
        List<UserResponseDto> dtos = service.patchAll(requests).stream()
                .map(FieldSecurityUtil::filterRead)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<UserResponseDto>> search(
            @ModelAttribute UserSearchRequest searchRequest, @RequestParam("limit") Integer limit) {
        if (limit == null || limit <= 0) {
            return ResponseEntity.badRequest().build();
        }
        int clamped = Math.min(limit, maxPageSize);
        Page<UserResponseDto> page = service.search(searchRequest, PageRequest.of(0, clamped));
        // service.search(searchRequest, PageRequest.of(0, clamped))
        Page<UserResponseDto> dtoPage = page.map(FieldSecurityUtil::filterRead);
        PaginatedResponse<UserResponseDto> response = new PaginatedResponse<>(
            dtoPage.getContent(), dtoPage.getNumber(), dtoPage.getSize(),
            dtoPage.getTotalPages(), dtoPage.getTotalElements(),
            dtoPage.isFirst(), dtoPage.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StreamingResponseBody> export(
            @ModelAttribute UserSearchRequest searchRequest,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "format", required = true) String format) {
        final int effectiveLimit = limit != null ? limit : 1000;
        String lower = format == null ? "" : format.toLowerCase();
        int max;
        String contentType;
        String extension;
        BiConsumer<Iterator<UserResponseDto>, OutputStream> exporter;
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
                List<UserResponseDto> current = Collections.emptyList();
                int fetched = 0;
                private void fetch() {
                    if (fetched >= clamped) {
                        current = Collections.emptyList();
                        return;
                    }
                    Page<UserResponseDto> p = service.search(searchRequest, PageRequest.of(page++, pageSize));
                    List<UserResponseDto> dtos = p.getContent().stream()
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
}

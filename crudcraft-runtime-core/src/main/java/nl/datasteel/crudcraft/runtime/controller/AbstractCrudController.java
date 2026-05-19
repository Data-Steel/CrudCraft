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

package nl.datasteel.crudcraft.runtime.controller;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import nl.datasteel.crudcraft.runtime.Identified;
import nl.datasteel.crudcraft.runtime.controller.response.PaginatedResponse;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import nl.datasteel.crudcraft.runtime.service.BulkResult;
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


/**
 * Generic abstract REST controller for core CRUD endpoints.
 *
 * <p>Generated editable controllers can extend or mirror this base to expose list, reference list,
 * get-one, create, update, patch, delete, bulk, existence, validation, and count endpoints.
 * Override protected helpers such as {@link #clampPageable(Pageable)} for common behaviour, or add
 * new public endpoint methods in the editable stub when application-specific routes are needed.
 *
 * @param <T> entity type
 * @param <U> request DTO type
 * @param <R> full response DTO type
 * @param <F> reference DTO type
 * @param <ID> identifier type
 */
// Generic CRUD signatures intentionally use short type parameters to keep generated stubs readable.
@SuppressWarnings("java:S119")
public abstract class AbstractCrudController<T, U, R, F, ID> {

    protected final AbstractCrudService<T, U, R, F, ID> service;

    protected int maxPageSize;

    /**
     * Creates a controller backed by the given service.
     *
     * @param service CRUD service
     * @param maxPageSize configured maximum page size
     */
    protected AbstractCrudController(
            AbstractCrudService<T, U, R, F, ID> service,
            @Value("${crudcraft.api.max-page-size:100}") int maxPageSize) {
        this.service = service;
        this.maxPageSize = maxPageSize;
    }

    /**
     * Validates controller pagination configuration during bean initialization.
     *
     * <p>Failing fast avoids silently turning invalid values such as {@code 0} or negative numbers
     * into one-item pages.
     */
    @PostConstruct
    public void validateConfiguration() {
        if (maxPageSize <= 0) {
            throw new IllegalArgumentException(
                    "crudcraft.api.max-page-size must be positive; got " + maxPageSize);
        }
    }

    /**
     * Clamps the requested page size to the configured maximum.
     *
     * @implSpec Overrides must preserve the configured upper bound and must return a non-null
     *     {@link Pageable}. Returning an unbounded or larger page defeats the runtime guard used by
     *     generated list, reference-list, and search-adjacent endpoints.
     *
     * @param pageable requested pageable
     * @return pageable within configured bounds
     */
    protected Pageable clampPageable(Pageable pageable) {
        int effectiveMax = Math.max(1, maxPageSize);
        if (pageable == null) {
            return PageRequest.of(0, effectiveMax);
        }
        int size = Math.clamp(pageable.getPageSize(), 1, effectiveMax);
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }

    /**
     * Lists full DTOs.
     *
     * @param pageable requested page and sort
     * @return paginated full DTO response
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<R>> getAll(Pageable pageable) {
        Page<R> page = service.findAll(clampPageable(pageable));
        return ResponseEntity.ok(toResponse(page));
    }

    /**
     * Lists reference DTOs.
     *
     * @param pageable requested page and sort
     * @return paginated reference DTO response
     */
    @GetMapping("/ref")
    public ResponseEntity<PaginatedResponse<F>> getAllRef(Pageable pageable) {
        Page<F> page = service.findAllRef(clampPageable(pageable));
        return ResponseEntity.ok(toResponse(page));
    }

    /**
     * Fetches one entity by ID.
     *
     * @param id identifier
     * @return response DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<R> getById(@PathVariable("id") @NotNull ID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Creates one entity.
     *
     * @param request create request DTO
     * @return created response DTO
     */
    @PostMapping
    public ResponseEntity<R> create(@Valid @RequestBody @NotNull U request) {
        return ResponseEntity.status(201).body(service.create(request));
    }

    /**
     * Replaces one entity.
     *
     * @param id identifier
     * @param request update request DTO
     * @return updated response DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<R> update(
            @PathVariable("id") @NotNull ID id, @Valid @RequestBody @NotNull U request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    /**
     * Partially updates one entity.
     *
     * @param id identifier
     * @param request patch request DTO
     * @return patched response DTO
     */
    @PatchMapping("/{id}")
    public ResponseEntity<R> patch(
            @PathVariable("id") @NotNull ID id, @RequestBody @NotNull U request) {
        return ResponseEntity.ok(service.patch(id, request));
    }

    /**
     * Deletes one entity.
     *
     * @param id identifier
     * @return empty response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull ID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Creates multiple entities.
     *
     * @param requests create request DTOs
     * @return per-item bulk result
     */
    @PostMapping("/batch")
    public ResponseEntity<BulkResult<R>> createAll(@Valid @RequestBody @NotNull List<U> requests) {
        BulkResult<R> result = service.createAllResult(requests);
        return ResponseEntity.status(
                        result.hasFailures() ? HttpStatus.MULTI_STATUS : HttpStatus.CREATED)
                .body(result);
    }

    /**
     * Replaces multiple entities.
     *
     * @param requests identified update request DTOs
     * @return per-item bulk result
     */
    @PutMapping("/batch")
    public ResponseEntity<BulkResult<R>> updateAll(
            @Valid @RequestBody @NotNull List<Identified<ID, U>> requests) {
        BulkResult<R> result = service.updateAllResult(requests);
        return ResponseEntity.status(result.hasFailures() ? HttpStatus.MULTI_STATUS : HttpStatus.OK)
                .body(result);
    }

    /**
     * Partially updates multiple entities.
     *
     * @param requests identified patch request DTOs
     * @return per-item bulk result
     */
    @PatchMapping("/batch")
    public ResponseEntity<BulkResult<R>> patchAll(
            @Valid @RequestBody @NotNull List<Identified<ID, U>> requests) {
        BulkResult<R> result = service.patchAllResult(requests);
        return ResponseEntity.status(result.hasFailures() ? HttpStatus.MULTI_STATUS : HttpStatus.OK)
                .body(result);
    }

    /**
     * Upserts multiple entities.
     *
     * @param requests upsert request DTOs
     * @return per-item bulk result
     */
    @PostMapping("/batch/upsert")
    public ResponseEntity<BulkResult<R>> upsertAll(@RequestBody @NotNull Collection<U> requests) {
        BulkResult<R> result = service.upsertAllResult(requests);
        return ResponseEntity.status(
                        result.hasFailures() ? HttpStatus.MULTI_STATUS : HttpStatus.CREATED)
                .body(result);
    }

    /**
     * Deletes multiple entities by ID.
     *
     * @param ids identifiers to delete
     * @return per-item bulk result
     */
    @DeleteMapping("/batch/delete")
    public ResponseEntity<BulkResult<ID>> deleteAllByIds(@RequestBody @NotNull Collection<ID> ids) {
        BulkResult<ID> result = service.deleteAllByIdsResult(ids);
        return ResponseEntity.status(result.hasFailures() ? HttpStatus.MULTI_STATUS : HttpStatus.OK)
                .body(result);
    }

    /**
     * Checks whether one entity exists.
     *
     * @param id identifier
     * @return 200 when found, otherwise 404
     */
    @RequestMapping(
            value = "/exists/{id}",
            method = {RequestMethod.HEAD, RequestMethod.GET})
    public ResponseEntity<Void> exists(@PathVariable("id") @NotNull ID id) {
        return service.existsById(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Returns entity count.
     *
     * @return map containing count value
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("count", service.count()));
    }

    /**
     * Fetches multiple entities by ID.
     *
     * @param ids identifiers to fetch
     * @return response DTOs
     */
    @PostMapping("/batch/ids")
    public ResponseEntity<List<R>> findByIds(@RequestBody @NotNull Collection<ID> ids) {
        return ResponseEntity.ok(service.findByIds(ids));
    }

    /**
     * Validates request payload shape/rules.
     *
     * @param request request DTO
     * @return empty successful response
     */
    @PostMapping("/validate")
    public ResponseEntity<Void> validate(@Valid @RequestBody @NotNull U request) {
        return ResponseEntity.ok().build();
    }

    private <P> PaginatedResponse<P> toResponse(Page<P> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast());
    }
}

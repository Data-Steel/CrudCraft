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

package nl.datasteel.crudcraft.runtime.export.service;

// CHECKSTYLE.SUPPRESS: Indentation for +1000 lines

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import nl.datasteel.crudcraft.runtime.exception.ExportLimitExceededException;
import nl.datasteel.crudcraft.runtime.export.EntityExportAdapter;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import nl.datasteel.crudcraft.runtime.export.util.ExportUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


/**
 * Enhanced export service that supports both DTO-based and entity-based export modes. This service
 * delegates to either the standard DTO export or entity export based on the ExportRequest mode.
 *
 * @param <R> the response DTO type for DTO mode exports
 * @param <E> the entity type for entity mode exports
 * @param <S> the search request type
 */
public class EnhancedExportService<R, E, S> {

    private final ExportService<R, S> dtoExportService;
    private final EntityExportAdapter entityExportAdapter;
    private final Class<E> entityClass;
    private final ExportService.ExportConfig config;
    private final boolean allowEntityMode;

    /**
     * Creates a new enhanced export service.
     *
     * @param config the export configuration
     * @param entityExportAdapter the entity export adapter (nullable if entity mode not needed)
     * @param entityClass the entity class for entity mode exports
     */
    public EnhancedExportService(
            @NonNull ExportService.ExportConfig config,
            @Nullable EntityExportAdapter entityExportAdapter,
            @NonNull Class<E> entityClass) {
        this(config, entityExportAdapter, entityClass, true);
    }

    /**
     * Creates a new enhanced export service.
     *
     * @param config the export configuration
     * @param entityExportAdapter the entity export adapter (nullable if entity mode not needed)
     * @param entityClass the entity class for entity mode exports
     * @param allowEntityMode whether entity export mode is allowed for this service
     */
    public EnhancedExportService(
            @NonNull ExportService.ExportConfig config,
            @Nullable EntityExportAdapter entityExportAdapter,
            @NonNull Class<E> entityClass,
            boolean allowEntityMode) {
        this.dtoExportService = new ExportService<>(config);
        this.entityExportAdapter = entityExportAdapter;
        this.entityClass = entityClass;
        this.config = config;
        this.allowEntityMode = allowEntityMode;
    }

    /**
     * Exports data in the specified format, using either DTO or entity mode based on ExportRequest.
     *
     * @param searchRequest the search request for filtering data
     * @param limit the maximum number of rows to export (null for default)
     * @param format the export format (csv, json, xlsx)
     * @param exportRequest the export request for field filtering and mode selection
     * @param searchFunction function to search and fetch data with pagination (for DTO mode)
     * @param securityFilter function to apply security filtering to each DTO (for DTO mode)
     * @param entitySpecification optional specification used in entity export mode
     * @return ResponseEntity with streaming response body
     */
    public @NonNull ResponseEntity<StreamingResponseBody> export(
            @Nullable S searchRequest,
            @Nullable Integer limit,
            @Nullable String format,
            @Nullable ExportRequest exportRequest,
            @NonNull Function<PageRequest, Page<R>> searchFunction,
            @NonNull Function<R, R> securityFilter,
            @Nullable Specification<E> entitySpecification) {

        // If export request is null or DTO mode, use standard DTO export
        if (exportRequest == null || !exportRequest.isEntityModeEnabled()) {
            return dtoExportService.export(
                    searchRequest, limit, format, exportRequest, searchFunction, securityFilter);
        }

        if (!allowEntityMode) {
            throw new IllegalStateException(
                    "Entity export mode is disabled. Set crudcraft.export.allow-entity-mode=true"
                            + " to enable trusted entity exports.");
        }

        // Entity mode
        if (entityExportAdapter == null) {
            throw new IllegalStateException(
                    "Entity export mode is not available. Ensure EntityExportAdapter is"
                            + " configured.");
        }

        return exportEntityMode(limit, format, exportRequest, entitySpecification);
    }

    /**
     * Backward-compatible overload without an explicit entity specification.
     *
     * @param searchRequest search request
     * @param limit export limit
     * @param format export format
     * @param exportRequest export request
     * @param searchFunction DTO search function
     * @param securityFilter DTO security filter
     * @return streaming response
     */
    public @NonNull ResponseEntity<StreamingResponseBody> export(
            @Nullable S searchRequest,
            @Nullable Integer limit,
            @Nullable String format,
            @Nullable ExportRequest exportRequest,
            @NonNull Function<PageRequest, Page<R>> searchFunction,
            @NonNull Function<R, R> securityFilter) {
        return export(
                searchRequest,
                limit,
                format,
                exportRequest,
                searchFunction,
                securityFilter,
                null);
    }

    /**
     * Exports entities in entity mode.
     *
     * @param limit the maximum number of rows to export
     * @param format the export format
     * @param exportRequest the export request
     * @return ResponseEntity with streaming response body
     */
    private @NonNull ResponseEntity<StreamingResponseBody> exportEntityMode(
            @Nullable Integer limit,
            @Nullable String format,
            @NonNull ExportRequest exportRequest,
            @Nullable Specification<E> entitySpecification) {

        // Validate limit parameter
        if (limit != null && limit < 0) {
            return ResponseEntity.badRequest().build();
        }

        String lower;
        try {
            lower = ExportRequest.requireSupportedFormat(format);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        // Get format info
        FormatInfo formatInfo = getFormatInfo(lower);
        if (formatInfo == null) {
            return ResponseEntity.badRequest().build();
        }
        final int effectiveLimit = limit != null ? limit : Math.min(1000, formatInfo.maxRows);

        // Handle empty export
        if (effectiveLimit == 0) {
            Iterator<Map<String, Object>> emptyIterator = java.util.Collections.emptyIterator();
            BiConsumer<Iterator<Map<String, Object>>, OutputStream> exporter =
                    getMapExporter(lower, exportRequest);
            StreamingResponseBody body = out -> exporter.accept(emptyIterator, out);

            return createResponse(formatInfo, body);
        }

        if (effectiveLimit > formatInfo.maxRows) {
            throw limitExceeded(lower, effectiveLimit, formatInfo.maxRows);
        }
        validateEntityFieldDepth(exportRequest);
        int clamped = effectiveLimit;
        int pageSize = Math.min(config.getMaxPageSize(), clamped);
        pageSize = Math.max(1, pageSize);

        // Create entity iterator
        Iterator<Map<String, Object>> iterator =
                entityExportAdapter.createIterator(
                        entityClass, exportRequest, clamped, pageSize, entitySpecification);

        // Create streaming response
        BiConsumer<Iterator<Map<String, Object>>, OutputStream> exporter =
                getMapExporter(lower, exportRequest);
        StreamingResponseBody body = out -> exporter.accept(iterator, out);

        return createResponse(formatInfo, body);
    }

    /**
     * Gets the exporter for map-based data.
     *
     * @param format the format string
     * @param exportRequest the export request
     * @return the exporter function
     */
    @SuppressWarnings("unchecked")
    private BiConsumer<Iterator<Map<String, Object>>, OutputStream> getMapExporter(
            String format, ExportRequest exportRequest) {
        return switch (format) {
            case "csv" -> (iter, out) -> ExportUtil.streamCsv((Iterator) iter, out, exportRequest);
            case "json" ->
                    (iter, out) -> ExportUtil.streamJson((Iterator) iter, out, exportRequest);
            case "xlsx" ->
                    (iter, out) -> ExportUtil.streamXlsx((Iterator) iter, out, exportRequest);
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }

    /**
     * Creates the HTTP response with appropriate headers.
     *
     * @param formatInfo the format information
     * @param body the streaming response body
     * @return the response entity
     */
    private ResponseEntity<StreamingResponseBody> createResponse(
            FormatInfo formatInfo, StreamingResponseBody body) {
        String filename = "export-" + System.currentTimeMillis() + "." + formatInfo.extension;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, formatInfo.contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(body);
    }

    /** Format information for export. */
    private static class FormatInfo {
        final int maxRows;
        final String contentType;
        final String extension;

        FormatInfo(int maxRows, String contentType, String extension) {
            this.maxRows = maxRows;
            this.contentType = contentType;
            this.extension = extension;
        }
    }

    /**
     * Gets format information for the specified format string.
     *
     * @param format the format string (csv, json, xlsx)
     * @return FormatInfo or null if format is invalid
     */
    private FormatInfo getFormatInfo(String format) {
        return switch (format) {
            case "csv" ->
                    new FormatInfo(effectiveMaxRows(config.getMaxCsvRows()), "text/csv", "csv");
            case "json" ->
                    new FormatInfo(
                            effectiveMaxRows(config.getMaxJsonRows()),
                            "application/json",
                            "json");
            case "xlsx" ->
                    new FormatInfo(
                            effectiveMaxRows(config.getMaxXlsxRows()),
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "xlsx");
            default -> null;
        };
    }

    private int effectiveMaxRows(int formatMaxRows) {
        if (config.getMaxRows() <= 0) {
            return formatMaxRows;
        }
        return Math.min(config.getMaxRows(), formatMaxRows);
    }

    private void validateEntityFieldDepth(ExportRequest exportRequest) {
        int maxDepth = exportRequest.getEffectiveMaxDepth(config.getMaxDepth());
        exportRequest.getIncludeFields().forEach(path -> validateEntityFieldDepth(path, maxDepth));
        exportRequest.getExcludeFields().forEach(path -> validateEntityFieldDepth(path, maxDepth));
    }

    private void validateEntityFieldDepth(String fieldPath, int maxDepth) {
        int requestedDepth = requestedDepth(fieldPath);
        if (requestedDepth <= maxDepth) {
            return;
        }
        throw new nl.datasteel.crudcraft.runtime.export.ExportDepthExceededException(
                "Requested entity export field depth exceeds the configured maximum.",
                Map.of(
                        "requested_field",
                        fieldPath,
                        "requested_depth",
                        String.valueOf(requestedDepth),
                        "max_depth",
                        String.valueOf(maxDepth),
                        "docs",
                        "docs/feature-guides/export/README.md"));
    }

    private int requestedDepth(String fieldPath) {
        if (fieldPath == null || fieldPath.isBlank()) {
            return 0;
        }
        return Math.max(0, fieldPath.trim().split("\\.").length - 1);
    }

    private ExportLimitExceededException limitExceeded(
            String format, int requestedRows, int maxRows) {
        return new ExportLimitExceededException(
                "Export limit exceeded for "
                        + format
                        + ": requested "
                        + requestedRows
                        + " rows, maximum is "
                        + maxRows
                        + ". Reduce the limit parameter or export in smaller batches.");
    }

}

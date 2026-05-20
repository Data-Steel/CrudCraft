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
import nl.datasteel.crudcraft.runtime.export.ExportDepthExceededException;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import nl.datasteel.crudcraft.runtime.export.config.ExportProperties;
import nl.datasteel.crudcraft.runtime.export.util.ExportUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


/**
 * Service for exporting data in various formats (CSV, JSON, XLSX). Handles pagination, format
 * selection, and streaming of large datasets.
 *
 * <p>The format parameter is validated case-insensitively after trimming and must be one of
 * {@code csv}, {@code json}, or {@code xlsx}. Unsupported or {@code null} formats produce a
 * {@code 400 Bad Request}. Data is fetched page by page and written to the response stream. JSON is
 * row-streamed. CSV and XLSX are row-streamed when {@link ExportRequest#getIncludeFields()} defines
 * the output schema; otherwise they buffer flattened rows for the current request so they can
 * produce stable union headers.
 *
 * @param <R> the response DTO type to export
 * @param <S> the search request type for filtering data
 */
public class ExportService<R, S> {

    /** Configuration for export limits per format. */
    public static class ExportConfig {
        private final int maxRows;
        private final int maxCsvRows;
        private final int maxJsonRows;
        private final int maxXlsxRows;
        private final int maxPageSize;
        private final int maxDepth;

        /**
         * Creates a new export configuration.
         *
         * @param maxCsvRows maximum rows for CSV export
         * @param maxJsonRows maximum rows for JSON export
         * @param maxXlsxRows maximum rows for XLSX export
         * @param maxPageSize maximum page size for pagination
         */
        public ExportConfig(int maxCsvRows, int maxJsonRows, int maxXlsxRows, int maxPageSize) {
            this(
                    -1,
                    maxCsvRows,
                    maxJsonRows,
                    maxXlsxRows,
                    maxPageSize,
                    ExportProperties.DEFAULT_MAX_DEPTH);
        }

        /**
         * Creates a new export configuration with a global row cap.
         *
         * <p>When {@code maxRows} is positive, every format is capped by the lower value of the
         * global cap and its format-specific cap. Non-positive values disable the global cap and
         * preserve per-format behavior.
         *
         * @param maxRows global maximum rows for every export format, or non-positive to disable
         * @param maxCsvRows maximum rows for CSV export
         * @param maxJsonRows maximum rows for JSON export
         * @param maxXlsxRows maximum rows for XLSX export
         * @param maxPageSize maximum page size for pagination
         */
        public ExportConfig(
                int maxRows,
                int maxCsvRows,
                int maxJsonRows,
                int maxXlsxRows,
                int maxPageSize) {
            this(
                    maxRows,
                    maxCsvRows,
                    maxJsonRows,
                    maxXlsxRows,
                    maxPageSize,
                    ExportProperties.DEFAULT_MAX_DEPTH);
        }

        /**
         * Creates a new export configuration with row and depth limits.
         *
         * @param maxRows global maximum rows for every export format, or non-positive to disable
         * @param maxCsvRows maximum rows for CSV export
         * @param maxJsonRows maximum rows for JSON export
         * @param maxXlsxRows maximum rows for XLSX export
         * @param maxPageSize maximum page size for pagination
         * @param maxDepth maximum nested field depth when request max depth is omitted
         */
        public ExportConfig(
                int maxRows,
                int maxCsvRows,
                int maxJsonRows,
                int maxXlsxRows,
                int maxPageSize,
                int maxDepth) {
            this.maxRows = maxRows;
            this.maxCsvRows = maxCsvRows;
            this.maxJsonRows = maxJsonRows;
            this.maxXlsxRows = maxXlsxRows;
            this.maxPageSize = maxPageSize;
            this.maxDepth = Math.max(0, maxDepth);
        }

        /**
         * Returns the global maximum rows for all formats.
         *
         * @return global row cap, or non-positive when disabled
         */
        public int getMaxRows() {
            return maxRows;
        }

        /**
         * Returns maximum CSV rows.
         *
         * @return max CSV rows
         */
        public int getMaxCsvRows() {
            return maxCsvRows;
        }

        /**
         * Returns maximum JSON rows.
         *
         * @return max JSON rows
         */
        public int getMaxJsonRows() {
            return maxJsonRows;
        }

        /**
         * Returns maximum XLSX rows.
         *
         * @return max XLSX rows
         */
        public int getMaxXlsxRows() {
            return maxXlsxRows;
        }

        /**
         * Returns maximum page size used for export pagination.
         *
         * @return max page size
         */
        public int getMaxPageSize() {
            return maxPageSize;
        }

        /**
         * Returns maximum nested export field depth.
         *
         * @return configured max depth
         */
        public int getMaxDepth() {
            return maxDepth;
        }
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

    private final ExportConfig config;

    /**
     * Creates a new export service with the specified configuration.
     *
     * @param config the export configuration
     */
    public ExportService(@NonNull ExportConfig config) {
        this.config = config;
    }

    /**
     * Exports data in the specified format with streaming support.
     *
     * <p>Negative limits are rejected. A zero limit returns a valid empty export for the requested
     * format. Positive limits are capped by the configured per-format maximum and fetched using the
     * configured maximum page size. The {@code securityFilter} is applied to every DTO before it is
     * written to the stream.
     *
     * @param searchRequest the search request for filtering data
     * @param limit the maximum number of rows to export (null for default)
     * @param format the export format ({@code csv}, {@code json}, or {@code xlsx})
     * @param exportRequest the export request for field filtering (null for no filtering)
     * @param searchFunction function to search and fetch data with pagination
     * @param securityFilter function to apply security filtering to each DTO
     * @return response entity with streaming response body, or {@code 400 Bad Request} for invalid
     *     format or limit values
     */
    public @NonNull ResponseEntity<StreamingResponseBody> export(
            @Nullable S searchRequest,
            @Nullable Integer limit,
            @Nullable String format,
            @Nullable ExportRequest exportRequest,
            @NonNull Function<PageRequest, Page<R>> searchFunction,
            @NonNull Function<R, R> securityFilter) {
        if (searchRequest != null) {
            searchRequest.getClass();
        }

        // Validate limit parameter - reject negative values, but allow 0 for empty export
        if (limit != null && limit < 0) {
            return ResponseEntity.badRequest().build();
        }

        String lower;
        try {
            lower = ExportRequest.requireSupportedFormat(format);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        // Determine format configuration
        FormatInfo formatInfo = getFormatInfo(lower);
        if (formatInfo == null) {
            return ResponseEntity.badRequest().build();
        }
        final int effectiveLimit = limit != null ? limit : Math.min(1000, formatInfo.maxRows);

        // Handle limit == 0 as empty export
        if (effectiveLimit == 0) {
            // Create empty iterator for empty export
            Iterator<R> emptyIterator = java.util.Collections.emptyIterator();
            BiConsumer<Iterator<R>, OutputStream> exporter = getExporter(lower, exportRequest);
            StreamingResponseBody body = out -> exporter.accept(emptyIterator, out);

            String filename = "export-" + System.currentTimeMillis() + "." + formatInfo.extension;
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, formatInfo.contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .body(body);
        }

        if (effectiveLimit > formatInfo.maxRows) {
            throw limitExceeded(lower, effectiveLimit, formatInfo.maxRows);
        }
        validateDtoDepth(exportRequest);
        int clamped = effectiveLimit;
        int pageSize = Math.min(config.maxPageSize, clamped);

        // Ensure pageSize is at least 1
        if (pageSize <= 0) {
            pageSize = 1;
        }

        // Create paginated iterator
        Iterator<R> iterator =
                new PaginatedIterator<>(searchFunction, securityFilter, clamped, pageSize);

        // Create streaming response
        BiConsumer<Iterator<R>, OutputStream> exporter = getExporter(lower, exportRequest);
        StreamingResponseBody body = out -> exporter.accept(iterator, out);

        String filename = "export-" + System.currentTimeMillis() + "." + formatInfo.extension;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, formatInfo.contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(body);
    }

    /**
     * Exports data in the specified format with streaming support.
     *
     * @param searchRequest the search request for filtering data
     * @param limit the maximum number of rows to export (null for default)
     * @param format the export format (csv, json, xlsx)
     * @param searchFunction function to search and fetch data with pagination
     * @param securityFilter function to apply security filtering to each DTO
     * @return ResponseEntity with streaming response body
     */
    public @NonNull ResponseEntity<StreamingResponseBody> export(
            @Nullable S searchRequest,
            @Nullable Integer limit,
            @Nullable String format,
            @NonNull Function<PageRequest, Page<R>> searchFunction,
            @NonNull Function<R, R> securityFilter) {
        return export(searchRequest, limit, format, null, searchFunction, securityFilter);
    }

    /**
     * Gets the appropriate exporter for the format.
     *
     * @param format the format string
     * @param exportRequest the export request for field filtering (applied to all formats)
     * @return the exporter function
     */
    private BiConsumer<Iterator<R>, OutputStream> getExporter(
            String format, ExportRequest exportRequest) {
        return switch (format) {
            case "csv" -> (iter, out) -> ExportUtil.streamCsv(iter, out, exportRequest);
            case "json" -> (iter, out) -> ExportUtil.streamJson(iter, out, exportRequest);
            case "xlsx" -> (iter, out) -> ExportUtil.streamXlsx(iter, out, exportRequest);
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }

    /**
     * Gets format information for the specified format string.
     *
     * @param format the format string (csv, json, xlsx)
     * @return FormatInfo or null if format is invalid
     */
    private FormatInfo getFormatInfo(String format) {
        return switch (format) {
            case "csv" -> new FormatInfo(effectiveMaxRows(config.maxCsvRows), "text/csv", "csv");
            case "json" ->
                    new FormatInfo(
                            effectiveMaxRows(config.maxJsonRows), "application/json", "json");
            case "xlsx" ->
                    new FormatInfo(
                            effectiveMaxRows(config.maxXlsxRows),
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "xlsx");
            default -> null;
        };
    }

    private int effectiveMaxRows(int formatMaxRows) {
        if (config.maxRows <= 0) {
            return formatMaxRows;
        }
        return Math.min(config.maxRows, formatMaxRows);
    }

    private void validateDtoDepth(ExportRequest exportRequest) {
        if (exportRequest == null) {
            return;
        }
        int maxDepth = exportRequest.getEffectiveMaxDepth(config.getMaxDepth());
        exportRequest.getIncludeFields().forEach(path -> validateDtoDepth(path, maxDepth));
        exportRequest.getExcludeFields().forEach(path -> validateDtoDepth(path, maxDepth));
    }

    private void validateDtoDepth(String fieldPath, int maxDepth) {
        int requestedDepth = requestedDepth(fieldPath);
        if (requestedDepth <= maxDepth) {
            return;
        }
        throw new ExportDepthExceededException(
                "Requested export field depth exceeds the configured maximum.",
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

    /**
     * Iterator that lazily fetches data in pages and applies security filtering.
     *
     * @param <R> the response DTO type
     */
    private static class PaginatedIterator<R> implements Iterator<R> {
        private final Function<PageRequest, Page<R>> searchFunction;
        private final Function<R, R> securityFilter;
        private final int maxItems;
        private final int pageSize;

        private int page = 0;
        private int index = 0;
        private java.util.List<R> current = java.util.Collections.emptyList();
        private int fetched = 0;

        PaginatedIterator(
                Function<PageRequest, Page<R>> searchFunction,
                Function<R, R> securityFilter,
                int maxItems,
                int pageSize) {
            this.searchFunction = searchFunction;
            this.securityFilter = securityFilter;
            this.maxItems = maxItems;
            this.pageSize = pageSize;
        }

        private void fetch() {
            if (fetched >= maxItems) {
                current = java.util.Collections.emptyList();
                return;
            }

            Page<R> p = searchFunction.apply(PageRequest.of(page++, pageSize));
            java.util.List<R> dtos = p.getContent().stream().map(securityFilter).toList();

            if (dtos.isEmpty()) {
                current = java.util.Collections.emptyList();
                fetched = maxItems;
                return;
            }

            if (fetched + dtos.size() > maxItems) {
                dtos = dtos.subList(0, maxItems - fetched);
                fetched = maxItems;
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
                throw new java.util.NoSuchElementException();
            }
            return current.get(index++);
        }
    }
}

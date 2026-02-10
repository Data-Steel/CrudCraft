/*
 * Copyright (c) 2025 CrudCraft contributors
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
package nl.datasteel.crudcraft.runtime.service;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Function;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import nl.datasteel.crudcraft.runtime.util.ExportUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Service for exporting data in various formats (CSV, JSON, XLSX).
 * Handles pagination, format selection, and streaming of large datasets.
 *
 * @param <R> the response DTO type to export
 * @param <S> the search request type for filtering data
 */
public class ExportService<R, S> {

    /**
     * Configuration for export limits per format.
     */
    public static class ExportConfig {
        private final int maxCsvRows;
        private final int maxJsonRows;
        private final int maxXlsxRows;
        private final int maxPageSize;

        /**
         * Creates a new export configuration.
         *
         * @param maxCsvRows maximum rows for CSV export
         * @param maxJsonRows maximum rows for JSON export
         * @param maxXlsxRows maximum rows for XLSX export
         * @param maxPageSize maximum page size for pagination
         */
        public ExportConfig(int maxCsvRows, int maxJsonRows, int maxXlsxRows, int maxPageSize) {
            this.maxCsvRows = maxCsvRows;
            this.maxJsonRows = maxJsonRows;
            this.maxXlsxRows = maxXlsxRows;
            this.maxPageSize = maxPageSize;
        }

        public int getMaxCsvRows() {
            return maxCsvRows;
        }

        public int getMaxJsonRows() {
            return maxJsonRows;
        }

        public int getMaxXlsxRows() {
            return maxXlsxRows;
        }

        public int getMaxPageSize() {
            return maxPageSize;
        }
    }

    /**
     * Format information for export.
     */
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
    public ExportService(ExportConfig config) {
        this.config = config;
    }

    /**
     * Exports data in the specified format with streaming support.
     *
     * @param searchRequest the search request for filtering data
     * @param limit the maximum number of rows to export (null for default)
     * @param format the export format (csv, json, xlsx)
     * @param exportRequest the export request for field filtering (null for no filtering)
     * @param searchFunction function to search and fetch data with pagination
     * @param securityFilter function to apply security filtering to each DTO
     * @return ResponseEntity with streaming response body
     */
    public ResponseEntity<StreamingResponseBody> export(
            S searchRequest,
            Integer limit,
            String format,
            ExportRequest exportRequest,
            Function<PageRequest, Page<R>> searchFunction,
            Function<R, R> securityFilter) {

        // Validate limit parameter - reject negative values, but allow 0 for empty export
        if (limit != null && limit < 0) {
            return ResponseEntity.badRequest().build();
        }

        final int effectiveLimit = limit != null ? limit : 1000;
        String lower = format == null ? "" : format.toLowerCase();

        // Determine format configuration
        FormatInfo formatInfo = getFormatInfo(lower);
        if (formatInfo == null) {
            return ResponseEntity.badRequest().build();
        }

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

        // Clamp limit to format maximum
        int clamped = Math.min(effectiveLimit, formatInfo.maxRows);
        int pageSize = Math.min(config.maxPageSize, clamped);
        
        // Ensure pageSize is at least 1
        if (pageSize <= 0) {
            pageSize = 1;
        }

        // Create paginated iterator
        Iterator<R> iterator = new PaginatedIterator<>(searchFunction, securityFilter, clamped, pageSize);

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
    public ResponseEntity<StreamingResponseBody> export(
            S searchRequest,
            Integer limit,
            String format,
            Function<PageRequest, Page<R>> searchFunction,
            Function<R, R> securityFilter) {
        return export(searchRequest, limit, format, null, searchFunction, securityFilter);
    }

    /**
     * Gets the appropriate exporter for the format.
     *
     * @param format the format string
     * @param exportRequest the export request for field filtering (null for no filtering)
     * @return the exporter function
     */
    private BiConsumer<Iterator<R>, OutputStream> getExporter(String format, ExportRequest exportRequest) {
        return switch (format) {
            case "csv" -> (iter, out) -> ExportUtil.streamCsv(iter, out, exportRequest);
            case "json" -> (iter, out) -> ExportUtil.streamJson(iter, out);
            case "xlsx" -> (iter, out) -> ExportUtil.streamXlsx(iter, out, exportRequest);
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }

    /**
     * Gets the appropriate exporter for the format.
     *
     * @param format the format string
     * @return the exporter function
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private BiConsumer<Iterator<R>, OutputStream> getExporter(String format) {
        return getExporter(format, null);
    }

    /**
     * Gets format information for the specified format string.
     *
     * @param format the format string (csv, json, xlsx)
     * @return FormatInfo or null if format is invalid
     */
    private FormatInfo getFormatInfo(String format) {
        return switch (format) {
            case "csv" -> new FormatInfo(config.maxCsvRows, "text/csv", "csv");
            case "json" -> new FormatInfo(config.maxJsonRows, "application/json", "json");
            case "xlsx" -> new FormatInfo(
                    config.maxXlsxRows,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "xlsx"
            );
            default -> null;
        };
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

        PaginatedIterator(Function<PageRequest, Page<R>> searchFunction,
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
            java.util.List<R> dtos = p.getContent().stream()
                    .map(securityFilter)
                    .toList();

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

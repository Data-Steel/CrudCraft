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
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import nl.datasteel.crudcraft.runtime.export.EntityExportAdapter;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import nl.datasteel.crudcraft.runtime.util.ExportUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Enhanced export service that supports both DTO-based and entity-based export modes.
 * This service delegates to either the standard DTO export or entity export based on
 * the ExportRequest mode.
 *
 * @param <R> the response DTO type for DTO mode exports
 * @param <E> the entity type for entity mode exports
 * @param <S> the search request type
 */
public class EnhancedExportService<R, E, S> {
    
    private final ExportService<R, S> dtoExportService;
    private final EntityExportAdapter entityExportAdapter;
    private final Class<E> entityClass;
    
    /**
     * Creates a new enhanced export service.
     *
     * @param config the export configuration
     * @param entityExportAdapter the entity export adapter (nullable if entity mode not needed)
     * @param entityClass the entity class for entity mode exports
     */
    public EnhancedExportService(ExportService.ExportConfig config,
                                  EntityExportAdapter entityExportAdapter,
                                  Class<E> entityClass) {
        this.dtoExportService = new ExportService<>(config);
        this.entityExportAdapter = entityExportAdapter;
        this.entityClass = entityClass;
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
     * @return ResponseEntity with streaming response body
     */
    public ResponseEntity<StreamingResponseBody> export(
            S searchRequest,
            Integer limit,
            String format,
            ExportRequest exportRequest,
            Function<PageRequest, Page<R>> searchFunction,
            Function<R, R> securityFilter) {
        
        // If export request is null or DTO mode, use standard DTO export
        if (exportRequest == null || !exportRequest.isEntityModeEnabled()) {
            return dtoExportService.export(
                searchRequest, limit, format, exportRequest, searchFunction, securityFilter);
        }
        
        // Entity mode
        if (entityExportAdapter == null) {
            throw new IllegalStateException(
                "Entity export mode is not available. Ensure EntityExportAdapter is configured.");
        }
        
        return exportEntityMode(limit, format, exportRequest);
    }
    
    /**
     * Exports entities in entity mode.
     *
     * @param limit the maximum number of rows to export
     * @param format the export format
     * @param exportRequest the export request
     * @return ResponseEntity with streaming response body
     */
    private ResponseEntity<StreamingResponseBody> exportEntityMode(
            Integer limit,
            String format,
            ExportRequest exportRequest) {
        
        // Validate limit parameter
        if (limit != null && limit < 0) {
            return ResponseEntity.badRequest().build();
        }
        
        final int effectiveLimit = limit != null ? limit : 1000;
        String lower = format == null ? "" : format.toLowerCase();
        
        // Get format info
        FormatInfo formatInfo = getFormatInfo(lower);
        if (formatInfo == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Handle empty export
        if (effectiveLimit == 0) {
            Iterator<Map<String, Object>> emptyIterator = java.util.Collections.emptyIterator();
            BiConsumer<Iterator<Map<String, Object>>, OutputStream> exporter = getMapExporter(lower, exportRequest);
            StreamingResponseBody body = out -> exporter.accept(emptyIterator, out);
            
            return createResponse(formatInfo, body);
        }
        
        // Clamp limit and calculate page size
        int clamped = Math.min(effectiveLimit, formatInfo.maxRows);
        int pageSize = Math.min(100, clamped); // Default max page size of 100
        pageSize = Math.max(1, pageSize);
        
        // Create entity iterator
        Iterator<Map<String, Object>> iterator = entityExportAdapter.createIterator(
            entityClass, exportRequest, clamped, pageSize);
        
        // Create streaming response
        BiConsumer<Iterator<Map<String, Object>>, OutputStream> exporter = getMapExporter(lower, exportRequest);
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
            case "json" -> (iter, out) -> ExportUtil.streamJson((Iterator) iter, out, exportRequest);
            case "xlsx" -> (iter, out) -> ExportUtil.streamXlsx((Iterator) iter, out, exportRequest);
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
    
    /**
     * Gets format information for the specified format string.
     *
     * @param format the format string (csv, json, xlsx)
     * @return FormatInfo or null if format is invalid
     */
    private FormatInfo getFormatInfo(String format) {
        return switch (format) {
            case "csv" -> new FormatInfo(100000, "text/csv", "csv");
            case "json" -> new FormatInfo(50000, "application/json", "json");
            case "xlsx" -> new FormatInfo(25000,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
            default -> null;
        };
    }
}
